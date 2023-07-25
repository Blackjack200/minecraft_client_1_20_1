package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CraftingMenu extends RecipeBookMenu<CraftingContainer> {
   public static final int RESULT_SLOT = 0;
   private static final int CRAFT_SLOT_START = 1;
   private static final int CRAFT_SLOT_END = 10;
   private static final int INV_SLOT_START = 10;
   private static final int INV_SLOT_END = 37;
   private static final int USE_ROW_SLOT_START = 37;
   private static final int USE_ROW_SLOT_END = 46;
   private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
   private final ResultContainer resultSlots = new ResultContainer();
   private final ContainerLevelAccess access;
   private final Player player;

   public CraftingMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public CraftingMenu(int i, Inventory inventory, ContainerLevelAccess containerlevelaccess) {
      super(MenuType.CRAFTING, i);
      this.access = containerlevelaccess;
      this.player = inventory.player;
      this.addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

      for(int j = 0; j < 3; ++j) {
         for(int k = 0; k < 3; ++k) {
            this.addSlot(new Slot(this.craftSlots, k + j * 3, 30 + k * 18, 17 + j * 18));
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

   protected static void slotChangedCraftingGrid(AbstractContainerMenu abstractcontainermenu, Level level, Player player, CraftingContainer craftingcontainer, ResultContainer resultcontainer) {
      if (!level.isClientSide) {
         ServerPlayer serverplayer = (ServerPlayer)player;
         ItemStack itemstack = ItemStack.EMPTY;
         Optional<CraftingRecipe> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingcontainer, level);
         if (optional.isPresent()) {
            CraftingRecipe craftingrecipe = optional.get();
            if (resultcontainer.setRecipeUsed(level, serverplayer, craftingrecipe)) {
               ItemStack itemstack1 = craftingrecipe.assemble(craftingcontainer, level.registryAccess());
               if (itemstack1.isItemEnabled(level.enabledFeatures())) {
                  itemstack = itemstack1;
               }
            }
         }

         resultcontainer.setItem(0, itemstack);
         abstractcontainermenu.setRemoteSlot(0, itemstack);
         serverplayer.connection.send(new ClientboundContainerSetSlotPacket(abstractcontainermenu.containerId, abstractcontainermenu.incrementStateId(), 0, itemstack));
      }
   }

   public void slotsChanged(Container container) {
      this.access.execute((level, blockpos) -> slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots));
   }

   public void fillCraftSlotsStackedContents(StackedContents stackedcontents) {
      this.craftSlots.fillStackedContents(stackedcontents);
   }

   public void clearCraftingContent() {
      this.craftSlots.clearContent();
      this.resultSlots.clearContent();
   }

   public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
      return recipe.matches(this.craftSlots, this.player.level());
   }

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.craftSlots));
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.CRAFTING_TABLE);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == 0) {
            this.access.execute((level, blockpos) -> itemstack1.getItem().onCraftedBy(itemstack1, level, player));
            if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i >= 10 && i < 46) {
            if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
               if (i < 37) {
                  if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
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
      return 10;
   }

   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }

   public boolean shouldMoveToInventory(int i) {
      return i != this.getResultSlotIndex();
   }
}
