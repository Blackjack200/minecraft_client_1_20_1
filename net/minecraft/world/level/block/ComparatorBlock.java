package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

public class ComparatorBlock extends DiodeBlock implements EntityBlock {
   public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

   public ComparatorBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(MODE, ComparatorMode.COMPARE));
   }

   protected int getDelay(BlockState blockstate) {
      return 2;
   }

   protected int getOutputSignal(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
      return blockentity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockentity).getOutputSignal() : 0;
   }

   private int calculateOutputSignal(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.getInputSignal(level, blockpos, blockstate);
      if (i == 0) {
         return 0;
      } else {
         int j = this.getAlternateSignal(level, blockpos, blockstate);
         if (j > i) {
            return 0;
         } else {
            return blockstate.getValue(MODE) == ComparatorMode.SUBTRACT ? i - j : i;
         }
      }
   }

   protected boolean shouldTurnOn(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.getInputSignal(level, blockpos, blockstate);
      if (i == 0) {
         return false;
      } else {
         int j = this.getAlternateSignal(level, blockpos, blockstate);
         if (i > j) {
            return true;
         } else {
            return i == j && blockstate.getValue(MODE) == ComparatorMode.COMPARE;
         }
      }
   }

   protected int getInputSignal(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = super.getInputSignal(level, blockpos, blockstate);
      Direction direction = blockstate.getValue(FACING);
      BlockPos blockpos1 = blockpos.relative(direction);
      BlockState blockstate1 = level.getBlockState(blockpos1);
      if (blockstate1.hasAnalogOutputSignal()) {
         i = blockstate1.getAnalogOutputSignal(level, blockpos1);
      } else if (i < 15 && blockstate1.isRedstoneConductor(level, blockpos1)) {
         blockpos1 = blockpos1.relative(direction);
         blockstate1 = level.getBlockState(blockpos1);
         ItemFrame itemframe = this.getItemFrame(level, direction, blockpos1);
         int j = Math.max(itemframe == null ? Integer.MIN_VALUE : itemframe.getAnalogOutput(), blockstate1.hasAnalogOutputSignal() ? blockstate1.getAnalogOutputSignal(level, blockpos1) : Integer.MIN_VALUE);
         if (j != Integer.MIN_VALUE) {
            i = j;
         }
      }

      return i;
   }

   @Nullable
   private ItemFrame getItemFrame(Level level, Direction direction, BlockPos blockpos) {
      List<ItemFrame> list = level.getEntitiesOfClass(ItemFrame.class, new AABB((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), (double)(blockpos.getX() + 1), (double)(blockpos.getY() + 1), (double)(blockpos.getZ() + 1)), (itemframe) -> itemframe != null && itemframe.getDirection() == direction);
      return list.size() == 1 ? list.get(0) : null;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         blockstate = blockstate.cycle(MODE);
         float f = blockstate.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
         level.playSound(player, blockpos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
         level.setBlock(blockpos, blockstate, 2);
         this.refreshOutputState(level, blockpos, blockstate);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   protected void checkTickOnNeighbor(Level level, BlockPos blockpos, BlockState blockstate) {
      if (!level.getBlockTicks().willTickThisTick(blockpos, this)) {
         int i = this.calculateOutputSignal(level, blockpos, blockstate);
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         int j = blockentity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockentity).getOutputSignal() : 0;
         if (i != j || blockstate.getValue(POWERED) != this.shouldTurnOn(level, blockpos, blockstate)) {
            TickPriority tickpriority = this.shouldPrioritize(level, blockpos, blockstate) ? TickPriority.HIGH : TickPriority.NORMAL;
            level.scheduleTick(blockpos, this, 2, tickpriority);
         }

      }
   }

   private void refreshOutputState(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.calculateOutputSignal(level, blockpos, blockstate);
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      int j = 0;
      if (blockentity instanceof ComparatorBlockEntity comparatorblockentity) {
         j = comparatorblockentity.getOutputSignal();
         comparatorblockentity.setOutputSignal(i);
      }

      if (j != i || blockstate.getValue(MODE) == ComparatorMode.COMPARE) {
         boolean flag = this.shouldTurnOn(level, blockpos, blockstate);
         boolean flag1 = blockstate.getValue(POWERED);
         if (flag1 && !flag) {
            level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag1 && flag) {
            level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)), 2);
         }

         this.updateNeighborsInFront(level, blockpos, blockstate);
      }

   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.refreshOutputState(serverlevel, blockpos, blockstate);
   }

   public boolean triggerEvent(BlockState blockstate, Level level, BlockPos blockpos, int i, int j) {
      super.triggerEvent(blockstate, level, blockpos, i, j);
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      return blockentity != null && blockentity.triggerEvent(i, j);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new ComparatorBlockEntity(blockpos, blockstate);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, MODE, POWERED);
   }
}
