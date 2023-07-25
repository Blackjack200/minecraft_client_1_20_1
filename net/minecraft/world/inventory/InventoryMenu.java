package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class InventoryMenu extends RecipeBookMenu<CraftingContainer> {
   public static final int CONTAINER_ID = 0;
   public static final int RESULT_SLOT = 0;
   public static final int CRAFT_SLOT_START = 1;
   public static final int CRAFT_SLOT_END = 5;
   public static final int ARMOR_SLOT_START = 5;
   public static final int ARMOR_SLOT_END = 9;
   public static final int INV_SLOT_START = 9;
   public static final int INV_SLOT_END = 36;
   public static final int USE_ROW_SLOT_START = 36;
   public static final int USE_ROW_SLOT_END = 45;
   public static final int SHIELD_SLOT = 45;
   public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
   static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
   private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
   private final ResultContainer resultSlots = new ResultContainer();
   public final boolean active;
   private final Player owner;

   public InventoryMenu(Inventory inventory, boolean flag, final Player player) {
      super((MenuType<?>)null, 0);
      this.active = flag;
      this.owner = player;
      this.addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(int k = 0; k < 4; ++k) {
         final EquipmentSlot equipmentslot = SLOT_IDS[k];
         this.addSlot(new Slot(inventory, 39 - k, 8, 8 + k * 18) {
            public void setByPlayer(ItemStack itemstack) {
               InventoryMenu.onEquipItem(player, equipmentslot, itemstack, this.getItem());
               super.setByPlayer(itemstack);
            }

            public int getMaxStackSize() {
               return 1;
            }

            public boolean mayPlace(ItemStack itemstack) {
               return equipmentslot == Mob.getEquipmentSlotForItem(itemstack);
            }

            public boolean mayPickup(Player playerx) {
               ItemStack itemstack = this.getItem();
               return !itemstack.isEmpty() && !player.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(player);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
               return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
            }
         });
      }

      for(int l = 0; l < 3; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1 + (l + 1) * 9, 8 + i1 * 18, 84 + l * 18));
         }
      }

      for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 142));
      }

      this.addSlot(new Slot(inventory, 40, 77, 62) {
         public void setByPlayer(ItemStack itemstack) {
            InventoryMenu.onEquipItem(player, EquipmentSlot.OFFHAND, itemstack, this.getItem());
            super.setByPlayer(itemstack);
         }

         public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
         }
      });
   }

   static void onEquipItem(Player player, EquipmentSlot equipmentslot, ItemStack itemstack, ItemStack itemstack1) {
      Equipable equipable = Equipable.get(itemstack);
      if (equipable != null) {
         player.onEquipItem(equipmentslot, itemstack1, itemstack);
      }

   }

   public static boolean isHotbarSlot(int i) {
      return i >= 36 && i < 45 || i == 45;
   }

   public void fillCraftSlotsStackedContents(StackedContents stackedcontents) {
      this.craftSlots.fillStackedContents(stackedcontents);
   }

   public void clearCraftingContent() {
      this.resultSlots.clearContent();
      this.craftSlots.clearContent();
   }

   public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
      return recipe.matches(this.craftSlots, this.owner.level());
   }

   public void slotsChanged(Container container) {
      CraftingMenu.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots);
   }

   public void removed(Player player) {
      super.removed(player);
      this.resultSlots.clearContent();
      if (!player.level().isClientSide) {
         this.clearContainer(player, this.craftSlots);
      }
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
         if (i == 0) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i >= 1 && i < 5) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 5 && i < 9) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentslot.getIndex()).hasItem()) {
            int j = 8 - equipmentslot.getIndex();
            if (!this.moveItemStackTo(itemstack1, j, j + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
            if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 9 && i < 36) {
            if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 36 && i < 45) {
            if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
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
         if (i == 0) {
            player.drop(itemstack1, false);
         }
      }

      return itemstack;
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
   }

   public int getResultSlotIndex() {
      return 0;
   }

   public int getGridWidth() {
      return this.craftSlots.getWidth();
   }

   public int getGridHeight() {
      return this.craftSlots.getHeight();
   }

   public int getSize() {
      return 5;
   }

   public CraftingContainer getCraftSlots() {
      return this.craftSlots;
   }

   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }

   public boolean shouldMoveToInventory(int i) {
      return i != this.getResultSlotIndex();
   }
}
