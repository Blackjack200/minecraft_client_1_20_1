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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final CookingBookCategory bookCategory;
   private final Item result;
   private final Ingredient ingredient;
   private final float experience;
   private final int cookingTime;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

   private SimpleCookingRecipeBuilder(RecipeCategory recipecategory, CookingBookCategory cookingbookcategory, ItemLike itemlike, Ingredient ingredient, float f, int i, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer) {
      this.category = recipecategory;
      this.bookCategory = cookingbookcategory;
      this.result = itemlike.asItem();
      this.ingredient = ingredient;
      this.experience = f;
      this.cookingTime = i;
      this.serializer = recipeserializer;
   }

   public static SimpleCookingRecipeBuilder generic(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, float f, int i, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer) {
      return new SimpleCookingRecipeBuilder(recipecategory, determineRecipeCategory(recipeserializer, itemlike), itemlike, ingredient, f, i, recipeserializer);
   }

   public static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, float f, int i) {
      return new SimpleCookingRecipeBuilder(recipecategory, CookingBookCategory.FOOD, itemlike, ingredient, f, i, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, float f, int i) {
      return new SimpleCookingRecipeBuilder(recipecategory, determineBlastingRecipeCategory(itemlike), itemlike, ingredient, f, i, RecipeSerializer.BLASTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, float f, int i) {
      return new SimpleCookingRecipeBuilder(recipecategory, determineSmeltingRecipeCategory(itemlike), itemlike, ingredient, f, i, RecipeSerializer.SMELTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory recipecategory, ItemLike itemlike, float f, int i) {
      return new SimpleCookingRecipeBuilder(recipecategory, CookingBookCategory.FOOD, itemlike, ingredient, f, i, RecipeSerializer.SMOKING_RECIPE);
   }

   public SimpleCookingRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public SimpleCookingRecipeBuilder group(@Nullable String s) {
      this.group = s;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new SimpleCookingRecipeBuilder.Result(resourcelocation, this.group == null ? "" : this.group, this.bookCategory, this.ingredient, this.result, this.experience, this.cookingTime, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.serializer));
   }

   private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike itemlike) {
      if (itemlike.asItem().isEdible()) {
         return CookingBookCategory.FOOD;
      } else {
         return itemlike.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
      }
   }

   private static CookingBookCategory determineBlastingRecipeCategory(ItemLike itemlike) {
      return itemlike.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
   }

   private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer, ItemLike itemlike) {
      if (recipeserializer == RecipeSerializer.SMELTING_RECIPE) {
         return determineSmeltingRecipeCategory(itemlike);
      } else if (recipeserializer == RecipeSerializer.BLASTING_RECIPE) {
         return determineBlastingRecipeCategory(itemlike);
      } else if (recipeserializer != RecipeSerializer.SMOKING_RECIPE && recipeserializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
         throw new IllegalStateException("Unknown cooking recipe type");
      } else {
         return CookingBookCategory.FOOD;
      }
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
      }
   }

   static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final CookingBookCategory category;
      private final Ingredient ingredient;
      private final Item result;
      private final float experience;
      private final int cookingTime;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

      public Result(ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, Item item, float f, int i, Advancement.Builder advancement_builder, ResourceLocation resourcelocation1, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer) {
         this.id = resourcelocation;
         this.group = s;
         this.category = cookingbookcategory;
         this.ingredient = ingredient;
         this.result = item;
         this.experience = f;
         this.cookingTime = i;
         this.advancement = advancement_builder;
         this.advancementId = resourcelocation1;
         this.serializer = recipeserializer;
      }

      public void serializeRecipeData(JsonObject jsonobject) {
         if (!this.group.isEmpty()) {
            jsonobject.addProperty("group", this.group);
         }

         jsonobject.addProperty("category", this.category.getSerializedName());
         jsonobject.add("ingredient", this.ingredient.toJson());
         jsonobject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         jsonobject.addProperty("experience", this.experience);
         jsonobject.addProperty("cookingtime", this.cookingTime);
      }

      public RecipeSerializer<?> getType() {
         return this.serializer;
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
