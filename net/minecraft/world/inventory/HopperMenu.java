package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HopperMenu extends AbstractContainerMenu {
   public static final int CONTAINER_SIZE = 5;
   private final Container hopper;

   public HopperMenu(int i, Inventory inventory) {
      this(i, inventory, new SimpleContainer(5));
   }

   public HopperMenu(int i, Inventory inventory, Container container) {
      super(MenuType.HOPPER, i);
      this.hopper = container;
      checkContainerSize(container, 5);
      container.startOpen(inventory.player);
      int j = 51;

      for(int k = 0; k < 5; ++k) {
         this.addSlot(new Slot(container, k, 44 + k * 18, 20));
      }

      for(int l = 0; l < 3; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1 + l * 9 + 9, 8 + i1 * 18, l * 18 + 51));
         }
      }

      for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 109));
      }

   }

   public boolean stillValid(Player player) {
      return this.hopper.stillValid(player);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i < this.hopper.getContainerSize()) {
            if (!this.moveItemStackTo(itemstack1, this.hopper.getContainerSize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.hopper.getContainerSize(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   public void removed(Player player) {
      super.removed(player);
      this.hopper.stopOpen(player);
   }
}
