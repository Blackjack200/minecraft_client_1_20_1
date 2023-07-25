package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe extends Recipe<Container> {
   default RecipeType<?> getType() {
      return RecipeType.SMITHING;
   }

   default boolean canCraftInDimensions(int i, int j) {
      return i >= 3 && j >= 1;
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.SMITHING_TABLE);
   }

   boolean isTemplateIngredient(ItemStack itemstack);

   boolean isBaseIngredient(ItemStack itemstack);

   boolean isAdditionIngredient(ItemStack itemstack);
}
