package net.minecraft.world.inventory;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;

public class BrewingStandMenu extends AbstractContainerMenu {
   private static final int BOTTLE_SLOT_START = 0;
   private static final int BOTTLE_SLOT_END = 2;
   private static final int INGREDIENT_SLOT = 3;
   private static final int FUEL_SLOT = 4;
   private static final int SLOT_COUNT = 5;
   private static final int DATA_COUNT = 2;
   private static final int INV_SLOT_START = 5;
   private static final int INV_SLOT_END = 32;
   private static final int USE_ROW_SLOT_START = 32;
   private static final int USE_ROW_SLOT_END = 41;
   private final Container brewingStand;
   private final ContainerData brewingStandData;
   private final Slot ingredientSlot;

   public BrewingStandMenu(int i, Inventory inventory) {
      this(i, inventory, new SimpleContainer(5), new SimpleContainerData(2));
   }

   public BrewingStandMenu(int i, Inventory inventory, Container container, ContainerData containerdata) {
      super(MenuType.BREWING_STAND, i);
      checkContainerSize(container, 5);
      checkContainerDataCount(containerdata, 2);
      this.brewingStand = container;
      this.brewingStandData = containerdata;
      this.addSlot(new BrewingStandMenu.PotionSlot(container, 0, 56, 51));
      this.addSlot(new BrewingStandMenu.PotionSlot(container, 1, 79, 58));
      this.addSlot(new BrewingStandMenu.PotionSlot(container, 2, 102, 51));
      this.ingredientSlot = this.addSlot(new BrewingStandMenu.IngredientsSlot(container, 3, 79, 17));
      this.addSlot(new BrewingStandMenu.FuelSlot(container, 4, 17, 17));
      this.addDataSlots(containerdata);

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
      return this.brewingStand.stillValid(player);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if ((i < 0 || i > 2) && i != 3 && i != 4) {
            if (BrewingStandMenu.FuelSlot.mayPlaceItem(itemstack)) {
               if (this.moveItemStackTo(itemstack1, 4, 5, false) || this.ingredientSlot.mayPlace(itemstack1) && !this.moveItemStackTo(itemstack1, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.ingredientSlot.mayPlace(itemstack1)) {
               if (!this.moveItemStackTo(itemstack1, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (BrewingStandMenu.PotionSlot.mayPlaceItem(itemstack) && itemstack.getCount() == 1) {
               if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= 5 && i < 32) {
               if (!this.moveItemStackTo(itemstack1, 32, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= 32 && i < 41) {
               if (!this.moveItemStackTo(itemstack1, 5, 32, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 5, 41, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
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

   public int getFuel() {
      return this.brewingStandData.get(1);
   }

   public int getBrewingTicks() {
      return this.brewingStandData.get(0);
   }

   static class FuelSlot extends Slot {
      public FuelSlot(Container container, int i, int j, int k) {
         super(container, i, j, k);
      }

      public boolean mayPlace(ItemStack itemstack) {
         return mayPlaceItem(itemstack);
      }

      public static boolean mayPlaceItem(ItemStack itemstack) {
         return itemstack.is(Items.BLAZE_POWDER);
      }

      public int getMaxStackSize() {
         return 64;
      }
   }

   static class IngredientsSlot extends Slot {
      public IngredientsSlot(Container container, int i, int j, int k) {
         super(container, i, j, k);
      }

      public boolean mayPlace(ItemStack itemstack) {
         return PotionBrewing.isIngredient(itemstack);
      }

      public int getMaxStackSize() {
         return 64;
      }
   }

   static class PotionSlot extends Slot {
      public PotionSlot(Container container, int i, int j, int k) {
         super(container, i, j, k);
      }

      public boolean mayPlace(ItemStack itemstack) {
         return mayPlaceItem(itemstack);
      }

      public int getMaxStackSize() {
         return 1;
      }

      public void onTake(Player player, ItemStack itemstack) {
         Potion potion = PotionUtils.getPotion(itemstack);
         if (player instanceof ServerPlayer) {
            CriteriaTriggers.BREWED_POTION.trigger((ServerPlayer)player, potion);
         }

         super.onTake(player, itemstack);
      }

      public static boolean mayPlaceItem(ItemStack itemstack) {
         return itemstack.is(Items.POTION) || itemstack.is(Items.SPLASH_POTION) || itemstack.is(Items.LINGERING_POTION) || itemstack.is(Items.GLASS_BOTTLE);
      }
   }
}
