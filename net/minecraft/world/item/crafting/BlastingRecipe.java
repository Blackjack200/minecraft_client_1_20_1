package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, ItemStack itemstack, float f, int i) {
      super(RecipeType.BLASTING, resourcelocation, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.BLAST_FURNACE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BLASTING_RECIPE;
   }
}
