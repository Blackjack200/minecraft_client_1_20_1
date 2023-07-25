package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private boolean success = true;

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean flag) {
      this.success = flag;
   }

   protected void playSound(BlockSource blocksource) {
      blocksource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, blocksource.getPos(), 0);
   }
}
