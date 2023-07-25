package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   protected static final int WIRE_DIST_MIN = 1;
   protected static final int WIRE_DIST_MAX = 42;
   private static final int RECHECK_PERIOD = 10;
   protected static final int AABB_OFFSET = 3;
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 0.0D, 10.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 10.0D, 6.0D);
   protected static final VoxelShape WEST_AABB = Block.box(10.0D, 0.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 5.0D, 6.0D, 10.0D, 11.0D);

   public TripWireHookBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch ((Direction)blockstate.getValue(FACING)) {
         case EAST:
         default:
            return EAST_AABB;
         case WEST:
            return WEST_AABB;
         case SOUTH:
            return SOUTH_AABB;
         case NORTH:
            return NORTH_AABB;
      }
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      Direction direction = blockstate.getValue(FACING);
      BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      return direction.getAxis().isHorizontal() && blockstate1.isFaceSturdy(levelreader, blockpos1, direction);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction.getOpposite() == blockstate.getValue(FACING) && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false));
      LevelReader levelreader = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Direction[] adirection = blockplacecontext.getNearestLookingDirections();

      for(Direction direction : adirection) {
         if (direction.getAxis().isHorizontal()) {
            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(levelreader, blockpos)) {
               return blockstate;
            }
         }
      }

      return null;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      this.calculateState(level, blockpos, blockstate, false, false, -1, (BlockState)null);
   }

   public void calculateState(Level level, BlockPos blockpos, BlockState blockstate, boolean flag, boolean flag1, int i, @Nullable BlockState blockstate1) {
      Direction direction = blockstate.getValue(FACING);
      boolean flag2 = blockstate.getValue(ATTACHED);
      boolean flag3 = blockstate.getValue(POWERED);
      boolean flag4 = !flag;
      boolean flag5 = false;
      int j = 0;
      BlockState[] ablockstate = new BlockState[42];

      for(int k = 1; k < 42; ++k) {
         BlockPos blockpos1 = blockpos.relative(direction, k);
         BlockState blockstate2 = level.getBlockState(blockpos1);
         if (blockstate2.is(Blocks.TRIPWIRE_HOOK)) {
            if (blockstate2.getValue(FACING) == direction.getOpposite()) {
               j = k;
            }
            break;
         }

         if (!blockstate2.is(Blocks.TRIPWIRE) && k != i) {
            ablockstate[k] = null;
            flag4 = false;
         } else {
            if (k == i) {
               blockstate2 = MoreObjects.firstNonNull(blockstate1, blockstate2);
            }

            boolean flag6 = !blockstate2.getValue(TripWireBlock.DISARMED);
            boolean flag7 = blockstate2.getValue(TripWireBlock.POWERED);
            flag5 |= flag6 && flag7;
            ablockstate[k] = blockstate2;
            if (k == i) {
               level.scheduleTick(blockpos, this, 10);
               flag4 &= flag6;
            }
         }
      }

      flag4 &= j > 1;
      flag5 &= flag4;
      BlockState blockstate3 = this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(flag4)).setValue(POWERED, Boolean.valueOf(flag5));
      if (j > 0) {
         BlockPos blockpos2 = blockpos.relative(direction, j);
         Direction direction1 = direction.getOpposite();
         level.setBlock(blockpos2, blockstate3.setValue(FACING, direction1), 3);
         this.notifyNeighbors(level, blockpos2, direction1);
         this.emitState(level, blockpos2, flag4, flag5, flag2, flag3);
      }

      this.emitState(level, blockpos, flag4, flag5, flag2, flag3);
      if (!flag) {
         level.setBlock(blockpos, blockstate3.setValue(FACING, direction), 3);
         if (flag1) {
            this.notifyNeighbors(level, blockpos, direction);
         }
      }

      if (flag2 != flag4) {
         for(int l = 1; l < j; ++l) {
            BlockPos blockpos3 = blockpos.relative(direction, l);
            BlockState blockstate4 = ablockstate[l];
            if (blockstate4 != null) {
               level.setBlock(blockpos3, blockstate4.setValue(ATTACHED, Boolean.valueOf(flag4)), 3);
               if (!level.getBlockState(blockpos3).isAir()) {
               }
            }
         }
      }

   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.calculateState(serverlevel, blockpos, blockstate, false, true, -1, (BlockState)null);
   }

   private void emitState(Level level, BlockPos blockpos, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      if (flag1 && !flag3) {
         level.playSound((Player)null, blockpos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
         level.gameEvent((Entity)null, GameEvent.BLOCK_ACTIVATE, blockpos);
      } else if (!flag1 && flag3) {
         level.playSound((Player)null, blockpos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
         level.gameEvent((Entity)null, GameEvent.BLOCK_DEACTIVATE, blockpos);
      } else if (flag && !flag2) {
         level.playSound((Player)null, blockpos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
         level.gameEvent((Entity)null, GameEvent.BLOCK_ATTACH, blockpos);
      } else if (!flag && flag2) {
         level.playSound((Player)null, blockpos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (level.random.nextFloat() * 0.2F + 0.9F));
         level.gameEvent((Entity)null, GameEvent.BLOCK_DETACH, blockpos);
      }

   }

   private void notifyNeighbors(Level level, BlockPos blockpos, Direction direction) {
      level.updateNeighborsAt(blockpos, this);
      level.updateNeighborsAt(blockpos.relative(direction.getOpposite()), this);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         boolean flag1 = blockstate.getValue(ATTACHED);
         boolean flag2 = blockstate.getValue(POWERED);
         if (flag1 || flag2) {
            this.calculateState(level, blockpos, blockstate, true, false, -1, (BlockState)null);
         }

         if (flag2) {
            level.updateNeighborsAt(blockpos, this);
            level.updateNeighborsAt(blockpos.relative(blockstate.getValue(FACING).getOpposite()), this);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (!blockstate.getValue(POWERED)) {
         return 0;
      } else {
         return blockstate.getValue(FACING) == direction ? 15 : 0;
      }
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, POWERED, ATTACHED);
   }
}
