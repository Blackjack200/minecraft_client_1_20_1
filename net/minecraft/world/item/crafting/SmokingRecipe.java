package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class SmokingRecipe extends AbstractCookingRecipe {
   public SmokingRecipe(ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, ItemStack itemstack, float f, int i) {
      super(RecipeType.SMOKING, resourcelocation, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.SMOKER);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SMOKING_RECIPE;
   }
}
