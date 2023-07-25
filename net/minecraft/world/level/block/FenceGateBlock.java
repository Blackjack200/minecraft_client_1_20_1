package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
   protected static final VoxelShape Z_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   protected static final VoxelShape X_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
   protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 13.0D, 10.0D);
   protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 13.0D, 16.0D);
   protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 24.0D, 10.0D);
   protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 24.0D, 16.0D);
   protected static final VoxelShape Z_SUPPORT_SHAPE = Block.box(0.0D, 5.0D, 6.0D, 16.0D, 24.0D, 10.0D);
   protected static final VoxelShape X_SUPPORT_SHAPE = Block.box(6.0D, 5.0D, 0.0D, 10.0D, 24.0D, 16.0D);
   protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0D, 5.0D, 7.0D, 2.0D, 16.0D, 9.0D), Block.box(14.0D, 5.0D, 7.0D, 16.0D, 16.0D, 9.0D));
   protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0D, 5.0D, 0.0D, 9.0D, 16.0D, 2.0D), Block.box(7.0D, 5.0D, 14.0D, 9.0D, 16.0D, 16.0D));
   protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0D, 2.0D, 7.0D, 2.0D, 13.0D, 9.0D), Block.box(14.0D, 2.0D, 7.0D, 16.0D, 13.0D, 9.0D));
   protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0D, 2.0D, 0.0D, 9.0D, 13.0D, 2.0D), Block.box(7.0D, 2.0D, 14.0D, 9.0D, 13.0D, 16.0D));
   private final WoodType type;

   public FenceGateBlock(BlockBehaviour.Properties blockbehaviour_properties, WoodType woodtype) {
      super(blockbehaviour_properties.sound(woodtype.soundType()));
      this.type = woodtype;
      this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)).setValue(IN_WALL, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(IN_WALL)) {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
      } else {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      Direction.Axis direction_axis = direction.getAxis();
      if (blockstate.getValue(FACING).getClockWise().getAxis() != direction_axis) {
         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         boolean flag = this.isWall(blockstate1) || this.isWall(levelaccessor.getBlockState(blockpos.relative(direction.getOpposite())));
         return blockstate.setValue(IN_WALL, Boolean.valueOf(flag));
      }
   }

   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      if (blockstate.getValue(OPEN)) {
         return Shapes.empty();
      } else {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_SUPPORT_SHAPE : X_SUPPORT_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(OPEN)) {
         return Shapes.empty();
      } else {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
      }
   }

   public VoxelShape getOcclusionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      if (blockstate.getValue(IN_WALL)) {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
      } else {
         return blockstate.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
      }
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      switch (pathcomputationtype) {
         case LAND:
            return blockstate.getValue(OPEN);
         case WATER:
            return false;
         case AIR:
            return blockstate.getValue(OPEN);
         default:
            return false;
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Level level = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      boolean flag = level.hasNeighborSignal(blockpos);
      Direction direction = blockplacecontext.getHorizontalDirection();
      Direction.Axis direction_axis = direction.getAxis();
      boolean flag1 = direction_axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockpos.west())) || this.isWall(level.getBlockState(blockpos.east()))) || direction_axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockpos.north())) || this.isWall(level.getBlockState(blockpos.south())));
      return this.defaultBlockState().setValue(FACING, direction).setValue(OPEN, Boolean.valueOf(flag)).setValue(POWERED, Boolean.valueOf(flag)).setValue(IN_WALL, Boolean.valueOf(flag1));
   }

   private boolean isWall(BlockState blockstate) {
      return blockstate.is(BlockTags.WALLS);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (blockstate.getValue(OPEN)) {
         blockstate = blockstate.setValue(OPEN, Boolean.valueOf(false));
         level.setBlock(blockpos, blockstate, 10);
      } else {
         Direction direction = player.getDirection();
         if (blockstate.getValue(FACING) == direction.getOpposite()) {
            blockstate = blockstate.setValue(FACING, direction);
         }

         blockstate = blockstate.setValue(OPEN, Boolean.valueOf(true));
         level.setBlock(blockpos, blockstate, 10);
      }

      boolean flag = blockstate.getValue(OPEN);
      level.playSound(player, blockpos, flag ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
      level.gameEvent(player, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockpos);
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (!level.isClientSide) {
         boolean flag1 = level.hasNeighborSignal(blockpos);
         if (blockstate.getValue(POWERED) != flag1) {
            level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag1)).setValue(OPEN, Boolean.valueOf(flag1)), 2);
            if (blockstate.getValue(OPEN) != flag1) {
               level.playSound((Player)null, blockpos, flag1 ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
               level.gameEvent((Entity)null, flag1 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockpos);
            }
         }

      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, OPEN, POWERED, IN_WALL);
   }

   public static boolean connectsToDirection(BlockState blockstate, Direction direction) {
      return blockstate.getValue(FACING).getAxis() == direction.getClockWise().getAxis();
   }
}
