package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity {
   private int output;

   public ComparatorBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.COMPARATOR, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putInt("OutputSignal", this.output);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.output = compoundtag.getInt("OutputSignal");
   }

   public int getOutputSignal() {
      return this.output;
   }

   public void setOutputSignal(int i) {
      this.output = i;
   }
}
