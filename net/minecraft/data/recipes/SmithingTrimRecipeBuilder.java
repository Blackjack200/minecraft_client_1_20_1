package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
   private final RecipeCategory category;
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTrimRecipeBuilder(RecipeSerializer<?> recipeserializer, RecipeCategory recipecategory, Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2) {
      this.category = recipecategory;
      this.type = recipeserializer;
      this.template = ingredient;
      this.base = ingredient1;
      this.addition = ingredient2;
   }

   public static SmithingTrimRecipeBuilder smithingTrim(Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2, RecipeCategory recipecategory) {
      return new SmithingTrimRecipeBuilder(RecipeSerializer.SMITHING_TRIM, recipecategory, ingredient, ingredient1, ingredient2);
   }

   public SmithingTrimRecipeBuilder unlocks(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new SmithingTrimRecipeBuilder.Result(resourcelocation, this.type, this.template, this.base, this.addition, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject jsonobject) {
         jsonobject.add("template", this.template.toJson());
         jsonobject.add("base", this.base.toJson());
         jsonobject.add("addition", this.addition.toJson());
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
