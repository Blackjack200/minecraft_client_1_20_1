package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final int count;
   private final List<Ingredient> ingredients = Lists.newArrayList();
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;

   public ShapelessRecipeBuilder(RecipeCategory recipecategory, ItemLike itemlike, int i) {
      this.category = recipecategory;
      this.result = itemlike.asItem();
      this.count = i;
   }

   public static ShapelessRecipeBuilder shapeless(RecipeCategory recipecategory, ItemLike itemlike) {
      return new ShapelessRecipeBuilder(recipecategory, itemlike, 1);
   }

   public static ShapelessRecipeBuilder shapeless(RecipeCategory recipecategory, ItemLike itemlike, int i) {
      return new ShapelessRecipeBuilder(recipecategory, itemlike, i);
   }

   public ShapelessRecipeBuilder requires(TagKey<Item> tagkey) {
      return this.requires(Ingredient.of(tagkey));
   }

   public ShapelessRecipeBuilder requires(ItemLike itemlike) {
      return this.requires(itemlike, 1);
   }

   public ShapelessRecipeBuilder requires(ItemLike itemlike, int i) {
      for(int j = 0; j < i; ++j) {
         this.requires(Ingredient.of(itemlike));
      }

      return this;
   }

   public ShapelessRecipeBuilder requires(Ingredient ingredient) {
      return this.requires(ingredient, 1);
   }

   public ShapelessRecipeBuilder requires(Ingredient ingredient, int i) {
      for(int j = 0; j < i; ++j) {
         this.ingredients.add(ingredient);
      }

      return this;
   }

   public ShapelessRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public ShapelessRecipeBuilder group(@Nullable String s) {
      this.group = s;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new ShapelessRecipeBuilder.Result(resourcelocation, this.result, this.count, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
      }
   }

   public static class Result extends CraftingRecipeBuilder.CraftingResult {
      private final ResourceLocation id;
      private final Item result;
      private final int count;
      private final String group;
      private final List<Ingredient> ingredients;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;

      public Result(ResourceLocation resourcelocation, Item item, int i, String s, CraftingBookCategory craftingbookcategory, List<Ingredient> list, Advancement.Builder advancement_builder, ResourceLocation resourcelocation1) {
         super(craftingbookcategory);
         this.id = resourcelocation;
         this.result = item;
         this.count = i;
         this.group = s;
         this.ingredients = list;
         this.advancement = advancement_builder;
         this.advancementId = resourcelocation1;
      }

      public void serializeRecipeData(JsonObject jsonobject) {
         super.serializeRecipeData(jsonobject);
         if (!this.group.isEmpty()) {
            jsonobject.addProperty("group", this.group);
         }

         JsonArray jsonarray = new JsonArray();

         for(Ingredient ingredient : this.ingredients) {
            jsonarray.add(ingredient.toJson());
         }

         jsonobject.add("ingredients", jsonarray);
         JsonObject jsonobject1 = new JsonObject();
         jsonobject1.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         if (this.count > 1) {
            jsonobject1.addProperty("count", this.count);
         }

         jsonobject.add("result", jsonobject1);
      }

      public RecipeSerializer<?> getType() {
         return RecipeSerializer.SHAPELESS_RECIPE;
      }

      public ResourceLocation getId() {
         return this.id;
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
