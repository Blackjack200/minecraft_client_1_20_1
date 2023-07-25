package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.CraftingBookCategory;

public abstract class CraftingRecipeBuilder {
   protected static CraftingBookCategory determineBookCategory(RecipeCategory recipecategory) {
      CraftingBookCategory var10000;
      switch (recipecategory) {
         case BUILDING_BLOCKS:
            var10000 = CraftingBookCategory.BUILDING;
            break;
         case TOOLS:
         case COMBAT:
            var10000 = CraftingBookCategory.EQUIPMENT;
            break;
         case REDSTONE:
            var10000 = CraftingBookCategory.REDSTONE;
            break;
         default:
            var10000 = CraftingBookCategory.MISC;
      }

      return var10000;
   }

   protected abstract static class CraftingResult implements FinishedRecipe {
      private final CraftingBookCategory category;

      protected CraftingResult(CraftingBookCategory craftingbookcategory) {
         this.category = craftingbookcategory;
      }

      public void serializeRecipeData(JsonObject jsonobject) {
         jsonobject.addProperty("category", this.category.getSerializedName());
      }
   }
}
