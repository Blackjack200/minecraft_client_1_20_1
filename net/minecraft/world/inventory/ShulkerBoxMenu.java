package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxMenu extends AbstractContainerMenu {
   private static final int CONTAINER_SIZE = 27;
   private final Container container;

   public ShulkerBoxMenu(int i, Inventory inventory) {
      this(i, inventory, new SimpleContainer(27));
   }

   public ShulkerBoxMenu(int i, Inventory inventory, Container container) {
      super(MenuType.SHULKER_BOX, i);
      checkContainerSize(container, 27);
      this.container = container;
      container.startOpen(inventory.player);
      int j = 3;
      int k = 9;

      for(int l = 0; l < 3; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new ShulkerBoxSlot(container, i1 + l * 9, 8 + i1 * 18, 18 + l * 18));
         }
      }

      for(int j1 = 0; j1 < 3; ++j1) {
         for(int k1 = 0; k1 < 9; ++k1) {
            this.addSlot(new Slot(inventory, k1 + j1 * 9 + 9, 8 + k1 * 18, 84 + j1 * 18));
         }
      }

      for(int l1 = 0; l1 < 9; ++l1) {
         this.addSlot(new Slot(inventory, l1, 8 + l1 * 18, 142));
      }

   }

   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i < this.container.getContainerSize()) {
            if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
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
      this.container.stopOpen(player);
   }
}
