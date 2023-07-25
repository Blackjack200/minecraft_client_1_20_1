package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {
   void sendInitialData(AbstractContainerMenu abstractcontainermenu, NonNullList<ItemStack> nonnulllist, ItemStack itemstack, int[] aint);

   void sendSlotChange(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack);

   void sendCarriedChange(AbstractContainerMenu abstractcontainermenu, ItemStack itemstack);

   void sendDataChange(AbstractContainerMenu abstractcontainermenu, int i, int j);
}
