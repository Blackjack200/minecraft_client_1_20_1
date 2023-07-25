package net.minecraft.world.level.block;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireBlock extends Block {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
   protected static final VoxelShape AABB = Block.box(0.0D, 1.0D, 0.0D, 16.0D, 2.5D, 16.0D);
   protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private static final int RECHECK_PERIOD = 10;
   private final TripWireHookBlock hook;

   public TripWireBlock(TripWireHookBlock tripwirehookblock, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false)).setValue(DISARMED, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)));
      this.hook = tripwirehookblock;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return blockstate.getValue(ATTACHED) ? AABB : NOT_ATTACHED_AABB;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockGetter blockgetter = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      return this.defaultBlockState().setValue(NORTH, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.north()), Direction.NORTH))).setValue(EAST, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.east()), Direction.EAST))).setValue(SOUTH, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.south()), Direction.SOUTH))).setValue(WEST, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.west()), Direction.WEST)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction.getAxis().isHorizontal() ? blockstate.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(this.shouldConnectTo(blockstate1, direction))) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         this.updateSource(level, blockpos, blockstate);
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         this.updateSource(level, blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)));
      }
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide && !player.getMainHandItem().isEmpty() && player.getMainHandItem().is(Items.SHEARS)) {
         level.setBlock(blockpos, blockstate.setValue(DISARMED, Boolean.valueOf(true)), 4);
         level.gameEvent(player, GameEvent.SHEAR, blockpos);
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   private void updateSource(Level level, BlockPos blockpos, BlockState blockstate) {
      for(Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
         for(int i = 1; i < 42; ++i) {
            BlockPos blockpos1 = blockpos.relative(direction, i);
            BlockState blockstate1 = level.getBlockState(blockpos1);
            if (blockstate1.is(this.hook)) {
               if (blockstate1.getValue(TripWireHookBlock.FACING) == direction.getOpposite()) {
                  this.hook.calculateState(level, blockpos1, blockstate1, false, true, i, blockstate);
               }
               break;
            }

            if (!blockstate1.is(this)) {
               break;
            }
         }
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide) {
         if (!blockstate.getValue(POWERED)) {
            this.checkPressed(level, blockpos);
         }
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getBlockState(blockpos).getValue(POWERED)) {
         this.checkPressed(serverlevel, blockpos);
      }
   }

   private void checkPressed(Level level, BlockPos blockpos) {
      BlockState blockstate = level.getBlockState(blockpos);
      boolean flag = blockstate.getValue(POWERED);
      boolean flag1 = false;
      List<? extends Entity> list = level.getEntities((Entity)null, blockstate.getShape(level, blockpos).bounds().move(blockpos));
      if (!list.isEmpty()) {
         for(Entity entity : list) {
            if (!entity.isIgnoringBlockTriggers()) {
               flag1 = true;
               break;
            }
         }
      }

      if (flag1 != flag) {
         blockstate = blockstate.setValue(POWERED, Boolean.valueOf(flag1));
         level.setBlock(blockpos, blockstate, 3);
         this.updateSource(level, blockpos, blockstate);
      }

      if (flag1) {
         level.scheduleTick(new BlockPos(blockpos), this, 10);
      }

   }

   public boolean shouldConnectTo(BlockState blockstate, Direction direction) {
      if (blockstate.is(this.hook)) {
         return blockstate.getValue(TripWireHookBlock.FACING) == direction.getOpposite();
      } else {
         return blockstate.is(this);
      }
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(EAST, blockstate.getValue(WEST)).setValue(SOUTH, blockstate.getValue(NORTH)).setValue(WEST, blockstate.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(EAST)).setValue(EAST, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(NORTH));
         case CLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(WEST)).setValue(EAST, blockstate.getValue(NORTH)).setValue(SOUTH, blockstate.getValue(EAST)).setValue(WEST, blockstate.getValue(SOUTH));
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(NORTH));
         case FRONT_BACK:
            return blockstate.setValue(EAST, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(EAST));
         default:
            return super.mirror(blockstate, mirror);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
   }
}
