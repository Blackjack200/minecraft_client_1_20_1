package net.minecraft.world.inventory;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends AbstractContainerMenu {
   public static final int MAP_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final ContainerLevelAccess access;
   long lastSoundTime;
   public final Container container = new SimpleContainer(2) {
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };
   private final ResultContainer resultContainer = new ResultContainer() {
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };

   public CartographyTableMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public CartographyTableMenu(int i, Inventory inventory, final ContainerLevelAccess containerlevelaccess) {
      super(MenuType.CARTOGRAPHY_TABLE, i);
      this.access = containerlevelaccess;
      this.addSlot(new Slot(this.container, 0, 15, 15) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.is(Items.FILLED_MAP);
         }
      });
      this.addSlot(new Slot(this.container, 1, 15, 52) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.is(Items.PAPER) || itemstack.is(Items.MAP) || itemstack.is(Items.GLASS_PANE);
         }
      });
      this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }

         public void onTake(Player player, ItemStack itemstack) {
            CartographyTableMenu.this.slots.get(0).remove(1);
            CartographyTableMenu.this.slots.get(1).remove(1);
            itemstack.getItem().onCraftedBy(itemstack, player.level(), player);
            containerlevelaccess.execute((level, blockpos) -> {
               long i = level.getGameTime();
               if (CartographyTableMenu.this.lastSoundTime != i) {
                  level.playSound((Player)null, blockpos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  CartographyTableMenu.this.lastSoundTime = i;
               }

            });
            super.onTake(player, itemstack);
         }
      });

      for(int j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
      }

   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.CARTOGRAPHY_TABLE);
   }

   public void slotsChanged(Container container) {
      ItemStack itemstack = this.container.getItem(0);
      ItemStack itemstack1 = this.container.getItem(1);
      ItemStack itemstack2 = this.resultContainer.getItem(2);
      if (itemstack2.isEmpty() || !itemstack.isEmpty() && !itemstack1.isEmpty()) {
         if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            this.setupResultSlot(itemstack, itemstack1, itemstack2);
         }
      } else {
         this.resultContainer.removeItemNoUpdate(2);
      }

   }

   private void setupResultSlot(ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2) {
      this.access.execute((level, blockpos) -> {
         MapItemSavedData mapitemsaveddata = MapItem.getSavedData(itemstack, level);
         if (mapitemsaveddata != null) {
            ItemStack itemstack6;
            if (itemstack1.is(Items.PAPER) && !mapitemsaveddata.locked && mapitemsaveddata.scale < 4) {
               itemstack6 = itemstack.copyWithCount(1);
               itemstack6.getOrCreateTag().putInt("map_scale_direction", 1);
               this.broadcastChanges();
            } else if (itemstack1.is(Items.GLASS_PANE) && !mapitemsaveddata.locked) {
               itemstack6 = itemstack.copyWithCount(1);
               itemstack6.getOrCreateTag().putBoolean("map_to_lock", true);
               this.broadcastChanges();
            } else {
               if (!itemstack1.is(Items.MAP)) {
                  this.resultContainer.removeItemNoUpdate(2);
                  this.broadcastChanges();
                  return;
               }

               itemstack6 = itemstack.copyWithCount(2);
               this.broadcastChanges();
            }

            if (!ItemStack.matches(itemstack6, itemstack2)) {
               this.resultContainer.setItem(2, itemstack6);
               this.broadcastChanges();
            }

         }
      });
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemstack, slot);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == 2) {
            itemstack1.getItem().onCraftedBy(itemstack1, player.level(), player);
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i != 1 && i != 0) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!itemstack1.is(Items.PAPER) && !itemstack1.is(Items.MAP) && !itemstack1.is(Items.GLASS_PANE)) {
               if (i >= 3 && i < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   public void removed(Player player) {
      super.removed(player);
      this.resultContainer.removeItemNoUpdate(2);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.container));
   }
}
