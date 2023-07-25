package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTransformRecipeBuilder {
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final RecipeCategory category;
   private final Item result;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTransformRecipeBuilder(RecipeSerializer<?> recipeserializer, Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2, RecipeCategory recipecategory, Item item) {
      this.category = recipecategory;
      this.type = recipeserializer;
      this.template = ingredient;
      this.base = ingredient1;
      this.addition = ingredient2;
      this.result = item;
   }

   public static SmithingTransformRecipeBuilder smithing(Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2, RecipeCategory recipecategory, Item item) {
      return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, ingredient, ingredient1, ingredient2, recipecategory, item);
   }

   public SmithingTransformRecipeBuilder unlocks(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public void save(Consumer<FinishedRecipe> consumer, String s) {
      this.save(consumer, new ResourceLocation(s));
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new SmithingTransformRecipeBuilder.Result(resourcelocation, this.type, this.template, this.base, this.addition, this.result, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Item result, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject jsonobject) {
         jsonobject.add("template", this.template.toJson());
         jsonobject.add("base", this.base.toJson());
         jsonobject.add("addition", this.addition.toJson());
         JsonObject jsonobject1 = new JsonObject();
         jsonobject1.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         jsonobject.add("result", jsonobject1);
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public RecipeSerializer<?> getType() {
         return this.type;
      }

      @Nullable
      public JsonObject serializeAdvancement() {
         return this.advancement.serializeToJson();
      }

      @Nullable
      public ResourceLocation getAdvancementId() {
         return this.advancementId;
      }
   }
}
