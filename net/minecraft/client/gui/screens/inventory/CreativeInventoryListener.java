package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class CreativeInventoryListener implements ContainerListener {
   private final Minecraft minecraft;

   public CreativeInventoryListener(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
      this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack, i);
   }

   public void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j) {
   }
}
