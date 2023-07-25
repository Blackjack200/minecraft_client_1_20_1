package net.minecraft.world.inventory;

import net.minecraft.world.item.ItemStack;

public interface ContainerListener {
   void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack);

   void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j);
}
