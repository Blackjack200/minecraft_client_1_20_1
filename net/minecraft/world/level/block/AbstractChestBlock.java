package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractChestBlock<E extends BlockEntity> extends BaseEntityBlock {
   protected final Supplier<BlockEntityType<? extends E>> blockEntityType;

   protected AbstractChestBlock(BlockBehaviour.Properties blockbehaviour_properties, Supplier<BlockEntityType<? extends E>> supplier) {
      super(blockbehaviour_properties);
      this.blockEntityType = supplier;
   }

   public abstract DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockstate, Level level, BlockPos blockpos, boolean flag);
}
