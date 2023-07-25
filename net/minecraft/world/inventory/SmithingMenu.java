package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
   public static final int TEMPLATE_SLOT = 0;
   public static final int BASE_SLOT = 1;
   public static final int ADDITIONAL_SLOT = 2;
   public static final int RESULT_SLOT = 3;
   public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
   public static final int BASE_SLOT_X_PLACEMENT = 26;
   public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
   private static final int RESULT_SLOT_X_PLACEMENT = 98;
   public static final int SLOT_Y_PLACEMENT = 48;
   private final Level level;
   @Nullable
   private SmithingRecipe selectedRecipe;
   private final List<SmithingRecipe> recipes;

   public SmithingMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public SmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerlevelaccess) {
      super(MenuType.SMITHING, i, inventory, containerlevelaccess);
      this.level = inventory.player.level();
      this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
   }

   protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
      return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, (itemstack4) -> this.recipes.stream().anyMatch((smithingrecipe2) -> smithingrecipe2.isTemplateIngredient(itemstack4))).withSlot(1, 26, 48, (itemstack2) -> this.recipes.stream().anyMatch((smithingrecipe1) -> smithingrecipe1.isBaseIngredient(itemstack2))).withSlot(2, 44, 48, (itemstack) -> this.recipes.stream().anyMatch((smithingrecipe) -> smithingrecipe.isAdditionIngredient(itemstack))).withResultSlot(3, 98, 48).build();
   }

   protected boolean isValidBlock(BlockState blockstate) {
      return blockstate.is(Blocks.SMITHING_TABLE);
   }

   protected boolean mayPickup(Player player, boolean flag) {
      return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
   }

   protected void onTake(Player player, ItemStack itemstack) {
      itemstack.onCraftedBy(player.level(), player, itemstack.getCount());
      this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
      this.shrinkStackInSlot(0);
      this.shrinkStackInSlot(1);
      this.shrinkStackInSlot(2);
      this.access.execute((level, blockpos) -> level.levelEvent(1044, blockpos, 0));
   }

   private List<ItemStack> getRelevantItems() {
      return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
   }

   private void shrinkStackInSlot(int i) {
      ItemStack itemstack = this.inputSlots.getItem(i);
      if (!itemstack.isEmpty()) {
         itemstack.shrink(1);
         this.inputSlots.setItem(i, itemstack);
      }

   }

   public void createResult() {
      List<SmithingRecipe> list = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
      if (list.isEmpty()) {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
      } else {
         SmithingRecipe smithingrecipe = list.get(0);
         ItemStack itemstack = smithingrecipe.assemble(this.inputSlots, this.level.registryAccess());
         if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
            this.selectedRecipe = smithingrecipe;
            this.resultSlots.setRecipeUsed(smithingrecipe);
            this.resultSlots.setItem(0, itemstack);
         }
      }

   }

   public int getSlotToQuickMoveTo(ItemStack itemstack) {
      return this.recipes.stream().map((smithingrecipe) -> findSlotMatchingIngredient(smithingrecipe, itemstack)).filter(Optional::isPresent).findFirst().orElse(Optional.of(0)).get();
   }

   private static Optional<Integer> findSlotMatchingIngredient(SmithingRecipe smithingrecipe, ItemStack itemstack) {
      if (smithingrecipe.isTemplateIngredient(itemstack)) {
         return Optional.of(0);
      } else if (smithingrecipe.isBaseIngredient(itemstack)) {
         return Optional.of(1);
      } else {
         return smithingrecipe.isAdditionIngredient(itemstack) ? Optional.of(2) : Optional.empty();
      }
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemstack, slot);
   }

   public boolean canMoveIntoInputSlots(ItemStack itemstack) {
      return this.recipes.stream().map((smithingrecipe) -> findSlotMatchingIngredient(smithingrecipe, itemstack)).anyMatch(Optional::isPresent);
   }
}
