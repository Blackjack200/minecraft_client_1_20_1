package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterMenu extends AbstractContainerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int RESULT_SLOT = 1;
   private static final int INV_SLOT_START = 2;
   private static final int INV_SLOT_END = 29;
   private static final int USE_ROW_SLOT_START = 29;
   private static final int USE_ROW_SLOT_END = 38;
   private final ContainerLevelAccess access;
   private final DataSlot selectedRecipeIndex = DataSlot.standalone();
   private final Level level;
   private List<StonecutterRecipe> recipes = Lists.newArrayList();
   private ItemStack input = ItemStack.EMPTY;
   long lastSoundTime;
   final Slot inputSlot;
   final Slot resultSlot;
   Runnable slotUpdateListener = () -> {
   };
   public final Container container = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         StonecutterMenu.this.slotsChanged(this);
         StonecutterMenu.this.slotUpdateListener.run();
      }
   };
   final ResultContainer resultContainer = new ResultContainer();

   public StonecutterMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public StonecutterMenu(int i, Inventory inventory, final ContainerLevelAccess containerlevelaccess) {
      super(MenuType.STONECUTTER, i);
      this.access = containerlevelaccess;
      this.level = inventory.player.level();
      this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }

         public void onTake(Player player, ItemStack itemstack) {
            itemstack.onCraftedBy(player.level(), player, itemstack.getCount());
            StonecutterMenu.this.resultContainer.awardUsedRecipes(player, this.getRelevantItems());
            ItemStack itemstack1 = StonecutterMenu.this.inputSlot.remove(1);
            if (!itemstack1.isEmpty()) {
               StonecutterMenu.this.setupResultSlot();
            }

            containerlevelaccess.execute((level, blockpos) -> {
               long i = level.getGameTime();
               if (StonecutterMenu.this.lastSoundTime != i) {
                  level.playSound((Player)null, blockpos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  StonecutterMenu.this.lastSoundTime = i;
               }

            });
            super.onTake(player, itemstack);
         }

         private List<ItemStack> getRelevantItems() {
            return List.of(StonecutterMenu.this.inputSlot.getItem());
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

      this.addDataSlot(this.selectedRecipeIndex);
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public List<StonecutterRecipe> getRecipes() {
      return this.recipes;
   }

   public int getNumRecipes() {
      return this.recipes.size();
   }

   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && !this.recipes.isEmpty();
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.STONECUTTER);
   }

   public boolean clickMenuButton(Player player, int i) {
      if (this.isValidRecipeIndex(i)) {
         this.selectedRecipeIndex.set(i);
         this.setupResultSlot();
      }

      return true;
   }

   private boolean isValidRecipeIndex(int i) {
      return i >= 0 && i < this.recipes.size();
   }

   public void slotsChanged(Container container) {
      ItemStack itemstack = this.inputSlot.getItem();
      if (!itemstack.is(this.input.getItem())) {
         this.input = itemstack.copy();
         this.setupRecipeList(container, itemstack);
      }

   }

   private void setupRecipeList(Container container, ItemStack itemstack) {
      this.recipes.clear();
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (!itemstack.isEmpty()) {
         this.recipes = this.level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, container, this.level);
      }

   }

   void setupResultSlot() {
      if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
         StonecutterRecipe stonecutterrecipe = this.recipes.get(this.selectedRecipeIndex.get());
         ItemStack itemstack = stonecutterrecipe.assemble(this.container, this.level.registryAccess());
         if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
            this.resultContainer.setRecipeUsed(stonecutterrecipe);
            this.resultSlot.set(itemstack);
         } else {
            this.resultSlot.set(ItemStack.EMPTY);
         }
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
      }

      this.broadcastChanges();
   }

   public MenuType<?> getType() {
      return MenuType.STONECUTTER;
   }

   public void registerUpdateListener(Runnable runnable) {
      this.slotUpdateListener = runnable;
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemstack, slot);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         Item item = itemstack1.getItem();
         itemstack = itemstack1.copy();
         if (i == 1) {
            item.onCraftedBy(itemstack1, player.level(), player);
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(itemstack1), this.level).isPresent()) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 2 && i < 29) {
            if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 29 && i < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
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
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.container));
   }
}
