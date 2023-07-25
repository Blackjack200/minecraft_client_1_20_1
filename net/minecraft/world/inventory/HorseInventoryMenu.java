package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
   private final Container horseContainer;
   private final AbstractHorse horse;

   public HorseInventoryMenu(int i, Inventory inventory, Container container, final AbstractHorse abstracthorse) {
      super((MenuType<?>)null, i);
      this.horseContainer = container;
      this.horse = abstracthorse;
      int j = 3;
      container.startOpen(inventory.player);
      int k = -18;
      this.addSlot(new Slot(container, 0, 8, 18) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.is(Items.SADDLE) && !this.hasItem() && abstracthorse.isSaddleable();
         }

         public boolean isActive() {
            return abstracthorse.isSaddleable();
         }
      });
      this.addSlot(new Slot(container, 1, 8, 36) {
         public boolean mayPlace(ItemStack itemstack) {
            return abstracthorse.isArmor(itemstack);
         }

         public boolean isActive() {
            return abstracthorse.canWearArmor();
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      if (this.hasChest(abstracthorse)) {
         for(int l = 0; l < 3; ++l) {
            for(int i1 = 0; i1 < ((AbstractChestedHorse)abstracthorse).getInventoryColumns(); ++i1) {
               this.addSlot(new Slot(container, 2 + i1 + l * ((AbstractChestedHorse)abstracthorse).getInventoryColumns(), 80 + i1 * 18, 18 + l * 18));
            }
         }
      }

      for(int j1 = 0; j1 < 3; ++j1) {
         for(int k1 = 0; k1 < 9; ++k1) {
            this.addSlot(new Slot(inventory, k1 + j1 * 9 + 9, 8 + k1 * 18, 102 + j1 * 18 + -18));
         }
      }

      for(int l1 = 0; l1 < 9; ++l1) {
         this.addSlot(new Slot(inventory, l1, 8 + l1 * 18, 142));
      }

   }

   public boolean stillValid(Player player) {
      return !this.horse.hasInventoryChanged(this.horseContainer) && this.horseContainer.stillValid(player) && this.horse.isAlive() && this.horse.distanceTo(player) < 8.0F;
   }

   private boolean hasChest(AbstractHorse abstracthorse) {
      return abstracthorse instanceof AbstractChestedHorse && ((AbstractChestedHorse)abstracthorse).hasChest();
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         int j = this.horseContainer.getContainerSize();
         if (i < j) {
            if (!this.moveItemStackTo(itemstack1, j, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
            if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(0).mayPlace(itemstack1)) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (j <= 2 || !this.moveItemStackTo(itemstack1, 2, j, false)) {
            int l = j + 27;
            int j1 = l + 9;
            if (i >= l && i < j1) {
               if (!this.moveItemStackTo(itemstack1, j, l, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= j && i < l) {
               if (!this.moveItemStackTo(itemstack1, l, j1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, l, l, false)) {
               return ItemStack.EMPTY;
            }

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
      this.horseContainer.stopOpen(player);
   }
}
