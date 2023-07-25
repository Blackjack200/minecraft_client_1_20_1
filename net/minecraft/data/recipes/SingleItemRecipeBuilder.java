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
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final Ingredient ingredient;
   private final int count;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<?> type;

   public SingleItemRecipeBuilder(RecipeCategory recipecategory, RecipeSerializer<?> recipeserializer, Ingredient ingredient, ItemLike itemlike, int i) {
      this.category = recipecategory;
      this.type = recipeserializer;
      this.result = itemlike.asItem();
      this.ingredient = ingredient;
      this.count = i;
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike) {
      return new SingleItemRecipeBuilder(recipecategory, RecipeSerializer.STONECUTTER, ingredient, itemlike, 1);
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, int i) {
      return new SingleItemRecipeBuilder(recipecategory, RecipeSerializer.STONECUTTER, ingredient, itemlike, i);
   }

   public SingleItemRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public SingleItemRecipeBuilder group(@Nullable String s) {
      this.group = s;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new SingleItemRecipeBuilder.Result(resourcelocation, this.type, this.group == null ? "" : this.group, this.ingredient, this.result, this.count, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
      }
   }

   public static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final Ingredient ingredient;
      private final Item result;
      private final int count;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final RecipeSerializer<?> type;

      public Result(ResourceLocation resourcelocation, RecipeSerializer<?> recipeserializer, String s, Ingredient ingredient, Item item, int i, Advancement.Builder advancement_builder, ResourceLocation resourcelocation1) {
         this.id = resourcelocation;
         this.type = recipeserializer;
         this.group = s;
         this.ingredient = ingredient;
         this.result = item;
         this.count = i;
         this.advancement = advancement_builder;
         this.advancementId = resourcelocation1;
      }

      public void serializeRecipeData(JsonObject jsonobject) {
         if (!this.group.isEmpty()) {
            jsonobject.addProperty("group", this.group);
         }

         jsonobject.add("ingredient", this.ingredient.toJson());
         jsonobject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         jsonobject.addProperty("count", this.count);
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
