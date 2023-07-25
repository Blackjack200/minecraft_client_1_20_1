package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
   public CampfireCookingRecipe(ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, ItemStack itemstack, float f, int i) {
      super(RecipeType.CAMPFIRE_COOKING, resourcelocation, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.CAMPFIRE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
   }
}
