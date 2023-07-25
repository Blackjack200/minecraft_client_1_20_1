package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
   ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

   RecipeBuilder unlockedBy(String s, CriterionTriggerInstance criteriontriggerinstance);

   RecipeBuilder group(@Nullable String s);

   Item getResult();

   void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation);

   default void save(Consumer<FinishedRecipe> consumer) {
      this.save(consumer, getDefaultRecipeId(this.getResult()));
   }

   default void save(Consumer<FinishedRecipe> consumer, String s) {
      ResourceLocation resourcelocation = getDefaultRecipeId(this.getResult());
      ResourceLocation resourcelocation1 = new ResourceLocation(s);
      if (resourcelocation1.equals(resourcelocation)) {
         throw new IllegalStateException("Recipe " + s + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.save(consumer, resourcelocation1);
      }
   }

   static ResourceLocation getDefaultRecipeId(ItemLike itemlike) {
      return BuiltInRegistries.ITEM.getKey(itemlike.asItem());
   }
}
