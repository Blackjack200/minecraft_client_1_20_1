package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ChestMenu extends AbstractContainerMenu {
   private static final int SLOTS_PER_ROW = 9;
   private final Container container;
   private final int containerRows;

   private ChestMenu(MenuType<?> menutype, int i, Inventory inventory, int j) {
      this(menutype, i, inventory, new SimpleContainer(9 * j), j);
   }

   public static ChestMenu oneRow(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x1, i, inventory, 1);
   }

   public static ChestMenu twoRows(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x2, i, inventory, 2);
   }

   public static ChestMenu threeRows(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, 3);
   }

   public static ChestMenu fourRows(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x4, i, inventory, 4);
   }

   public static ChestMenu fiveRows(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x5, i, inventory, 5);
   }

   public static ChestMenu sixRows(int i, Inventory inventory) {
      return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, 6);
   }

   public static ChestMenu threeRows(int i, Inventory inventory, Container container) {
      return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, container, 3);
   }

   public static ChestMenu sixRows(int i, Inventory inventory, Container container) {
      return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, container, 6);
   }

   public ChestMenu(MenuType<?> menutype, int i, Inventory inventory, Container container, int j) {
      super(menutype, i);
      checkContainerSize(container, j * 9);
      this.container = container;
      this.containerRows = j;
      container.startOpen(inventory.player);
      int k = (this.containerRows - 4) * 18;

      for(int l = 0; l < this.containerRows; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(container, i1 + l * 9, 8 + i1 * 18, 18 + l * 18));
         }
      }

      for(int j1 = 0; j1 < 3; ++j1) {
         for(int k1 = 0; k1 < 9; ++k1) {
            this.addSlot(new Slot(inventory, k1 + j1 * 9 + 9, 8 + k1 * 18, 103 + j1 * 18 + k));
         }
      }

      for(int l1 = 0; l1 < 9; ++l1) {
         this.addSlot(new Slot(inventory, l1, 8 + l1 * 18, 161 + k));
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
         if (i < this.containerRows * 9) {
            if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
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

   public Container getContainer() {
      return this.container;
   }

   public int getRowCount() {
      return this.containerRows;
   }
}
