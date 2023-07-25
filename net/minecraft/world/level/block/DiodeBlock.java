package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public abstract class DiodeBlock extends HorizontalDirectionalBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   protected DiodeBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return canSupportRigidBlock(levelreader, blockpos.below());
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!this.isLocked(serverlevel, blockpos, blockstate)) {
         boolean flag = blockstate.getValue(POWERED);
         boolean flag1 = this.shouldTurnOn(serverlevel, blockpos, blockstate);
         if (flag && !flag1) {
            serverlevel.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag) {
            serverlevel.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)), 2);
            if (!flag1) {
               serverlevel.scheduleTick(blockpos, this, this.getDelay(blockstate), TickPriority.VERY_HIGH);
            }
         }

      }
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getSignal(blockgetter, blockpos, direction);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (!blockstate.getValue(POWERED)) {
         return 0;
      } else {
         return blockstate.getValue(FACING) == direction ? this.getOutputSignal(blockgetter, blockpos, blockstate) : 0;
      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (blockstate.canSurvive(level, blockpos)) {
         this.checkTickOnNeighbor(level, blockpos, blockstate);
      } else {
         BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(blockpos) : null;
         dropResources(blockstate, level, blockpos, blockentity);
         level.removeBlock(blockpos, false);

         for(Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockpos.relative(direction), this);
         }

      }
   }

   protected void checkTickOnNeighbor(Level level, BlockPos blockpos, BlockState blockstate) {
      if (!this.isLocked(level, blockpos, blockstate)) {
         boolean flag = blockstate.getValue(POWERED);
         boolean flag1 = this.shouldTurnOn(level, blockpos, blockstate);
         if (flag != flag1 && !level.getBlockTicks().willTickThisTick(blockpos, this)) {
            TickPriority tickpriority = TickPriority.HIGH;
            if (this.shouldPrioritize(level, blockpos, blockstate)) {
               tickpriority = TickPriority.EXTREMELY_HIGH;
            } else if (flag) {
               tickpriority = TickPriority.VERY_HIGH;
            }

            level.scheduleTick(blockpos, this, this.getDelay(blockstate), tickpriority);
         }

      }
   }

   public boolean isLocked(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
      return false;
   }

   protected boolean shouldTurnOn(Level level, BlockPos blockpos, BlockState blockstate) {
      return this.getInputSignal(level, blockpos, blockstate) > 0;
   }

   protected int getInputSignal(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      BlockPos blockpos1 = blockpos.relative(direction);
      int i = level.getSignal(blockpos1, direction);
      if (i >= 15) {
         return i;
      } else {
         BlockState blockstate1 = level.getBlockState(blockpos1);
         return Math.max(i, blockstate1.is(Blocks.REDSTONE_WIRE) ? blockstate1.getValue(RedStoneWireBlock.POWER) : 0);
      }
   }

   protected int getAlternateSignal(SignalGetter signalgetter, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      Direction direction1 = direction.getClockWise();
      Direction direction2 = direction.getCounterClockWise();
      boolean flag = this.sideInputDiodesOnly();
      return Math.max(signalgetter.getControlInputSignal(blockpos.relative(direction1), direction1, flag), signalgetter.getControlInputSignal(blockpos.relative(direction2), direction2, flag));
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite());
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (this.shouldTurnOn(level, blockpos, blockstate)) {
         level.scheduleTick(blockpos, this, 1);
      }

   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      this.updateNeighborsInFront(level, blockpos, blockstate);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
         this.updateNeighborsInFront(level, blockpos, blockstate);
      }
   }

   protected void updateNeighborsInFront(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
      level.neighborChanged(blockpos1, this, blockpos);
      level.updateNeighborsAtExceptFromFacing(blockpos1, this, direction);
   }

   protected boolean sideInputDiodesOnly() {
      return false;
   }

   protected int getOutputSignal(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return 15;
   }

   public static boolean isDiode(BlockState blockstate) {
      return blockstate.getBlock() instanceof DiodeBlock;
   }

   public boolean shouldPrioritize(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING).getOpposite();
      BlockState blockstate1 = blockgetter.getBlockState(blockpos.relative(direction));
      return isDiode(blockstate1) && blockstate1.getValue(FACING) != direction;
   }

   protected abstract int getDelay(BlockState blockstate);
}
