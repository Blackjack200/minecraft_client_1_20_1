package net.minecraft.client.gui.screens.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public abstract class AbstractFurnaceRecipeBookComponent extends RecipeBookComponent {
   @Nullable
   private Ingredient fuels;

   protected void initFilterButtonTextures() {
      this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
   }

   public void slotClicked(@Nullable Slot slot) {
      super.slotClicked(slot);
      if (slot != null && slot.index < this.menu.getSize()) {
         this.ghostRecipe.clear();
      }

   }

   public void setupGhostRecipe(Recipe<?> recipe, List<Slot> list) {
      ItemStack itemstack = recipe.getResultItem(this.minecraft.level.registryAccess());
      this.ghostRecipe.setRecipe(recipe);
      this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (list.get(2)).x, (list.get(2)).y);
      NonNullList<Ingredient> nonnulllist = recipe.getIngredients();
      Slot slot = list.get(1);
      if (slot.getItem().isEmpty()) {
         if (this.fuels == null) {
            this.fuels = Ingredient.of(this.getFuelItems().stream().filter((item) -> item.isEnabled(this.minecraft.level.enabledFeatures())).map(ItemStack::new));
         }

         this.ghostRecipe.addIngredient(this.fuels, slot.x, slot.y);
      }

      Iterator<Ingredient> iterator = nonnulllist.iterator();

      for(int i = 0; i < 2; ++i) {
         if (!iterator.hasNext()) {
            return;
         }

         Ingredient ingredient = iterator.next();
         if (!ingredient.isEmpty()) {
            Slot slot1 = list.get(i);
            this.ghostRecipe.addIngredient(ingredient, slot1.x, slot1.y);
         }
      }

   }

   protected abstract Set<Item> getFuelItems();
}
