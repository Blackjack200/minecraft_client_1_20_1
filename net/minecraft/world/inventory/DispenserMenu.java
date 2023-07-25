package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DispenserMenu extends AbstractContainerMenu {
   private static final int SLOT_COUNT = 9;
   private static final int INV_SLOT_START = 9;
   private static final int INV_SLOT_END = 36;
   private static final int USE_ROW_SLOT_START = 36;
   private static final int USE_ROW_SLOT_END = 45;
   private final Container dispenser;

   public DispenserMenu(int i, Inventory inventory) {
      this(i, inventory, new SimpleContainer(9));
   }

   public DispenserMenu(int i, Inventory inventory, Container container) {
      super(MenuType.GENERIC_3x3, i);
      checkContainerSize(container, 9);
      this.dispenser = container;
      container.startOpen(inventory.player);

      for(int j = 0; j < 3; ++j) {
         for(int k = 0; k < 3; ++k) {
            this.addSlot(new Slot(container, k + j * 3, 62 + k * 18, 17 + j * 18));
         }
      }

      for(int l = 0; l < 3; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
         }
      }

      for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 142));
      }

   }

   public boolean stillValid(Player player) {
      return this.dispenser.stillValid(player);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i < 9) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
      }

      return itemstack;
   }

   public void removed(Player player) {
      super.removed(player);
      this.dispenser.stopOpen(player);
   }
}
