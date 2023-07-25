package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class ShapedRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final int count;
   private final List<String> rows = Lists.newArrayList();
   private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private boolean showNotification = true;

   public ShapedRecipeBuilder(RecipeCategory recipecategory, ItemLike itemlike, int i) {
      this.category = recipecategory;
      this.result = itemlike.asItem();
      this.count = i;
   }

   public static ShapedRecipeBuilder shaped(RecipeCategory recipecategory, ItemLike itemlike) {
      return shaped(recipecategory, itemlike, 1);
   }

   public static ShapedRecipeBuilder shaped(RecipeCategory recipecategory, ItemLike itemlike, int i) {
      return new ShapedRecipeBuilder(recipecategory, itemlike, i);
   }

   public ShapedRecipeBuilder define(Character character, TagKey<Item> tagkey) {
      return this.define(character, Ingredient.of(tagkey));
   }

   public ShapedRecipeBuilder define(Character character, ItemLike itemlike) {
      return this.define(character, Ingredient.of(itemlike));
   }

   public ShapedRecipeBuilder define(Character character, Ingredient ingredient) {
      if (this.key.containsKey(character)) {
         throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
      } else if (character == ' ') {
         throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      } else {
         this.key.put(character, ingredient);
         return this;
      }
   }

   public ShapedRecipeBuilder pattern(String s) {
      if (!this.rows.isEmpty() && s.length() != this.rows.get(0).length()) {
         throw new IllegalArgumentException("Pattern must be the same width on every line!");
      } else {
         this.rows.add(s);
         return this;
      }
   }

   public ShapedRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criteriontriggerinstance) {
      this.advancement.addCriterion(s, criteriontriggerinstance);
      return this;
   }

   public ShapedRecipeBuilder group(@Nullable String s) {
      this.group = s;
      return this;
   }

   public ShapedRecipeBuilder showNotification(boolean flag) {
      this.showNotification = flag;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourcelocation) {
      this.ensureValid(resourcelocation);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourcelocation)).rewards(AdvancementRewards.Builder.recipe(resourcelocation)).requirements(RequirementsStrategy.OR);
      consumer.accept(new ShapedRecipeBuilder.Result(resourcelocation, this.result, this.count, this.group == null ? "" : this.group, determineBookCategory(this.category), this.rows, this.key, this.advancement, resourcelocation.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.showNotification));
   }

   private void ensureValid(ResourceLocation resourcelocation) {
      if (this.rows.isEmpty()) {
         throw new IllegalStateException("No pattern is defined for shaped recipe " + resourcelocation + "!");
      } else {
         Set<Character> set = Sets.newHashSet(this.key.keySet());
         set.remove(' ');

         for(String s : this.rows) {
            for(int i = 0; i < s.length(); ++i) {
               char c0 = s.charAt(i);
               if (!this.key.containsKey(c0) && c0 != ' ') {
                  throw new IllegalStateException("Pattern in recipe " + resourcelocation + " uses undefined symbol '" + c0 + "'");
               }

               set.remove(c0);
            }
         }

         if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + resourcelocation);
         } else if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + resourcelocation + " only takes in a single item - should it be a shapeless recipe instead?");
         } else if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourcelocation);
         }
      }
   }

   static class Result extends CraftingRecipeBuilder.CraftingResult {
      private final ResourceLocation id;
      private final Item result;
      private final int count;
      private final String group;
      private final List<String> pattern;
      private final Map<Character, Ingredient> key;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final boolean showNotification;

      public Result(ResourceLocation resourcelocation, Item item, int i, String s, CraftingBookCategory craftingbookcategory, List<String> list, Map<Character, Ingredient> map, Advancement.Builder advancement_builder, ResourceLocation resourcelocation1, boolean flag) {
         super(craftingbookcategory);
         this.id = resourcelocation;
         this.result = item;
         this.count = i;
         this.group = s;
         this.pattern = list;
         this.key = map;
         this.advancement = advancement_builder;
         this.advancementId = resourcelocation1;
         this.showNotification = flag;
      }

      public void serializeRecipeData(JsonObject jsonobject) {
         super.serializeRecipeData(jsonobject);
         if (!this.group.isEmpty()) {
            jsonobject.addProperty("group", this.group);
         }

         JsonArray jsonarray = new JsonArray();

         for(String s : this.pattern) {
            jsonarray.add(s);
         }

         jsonobject.add("pattern", jsonarray);
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<Character, Ingredient> map_entry : this.key.entrySet()) {
            jsonobject1.add(String.valueOf(map_entry.getKey()), map_entry.getValue().toJson());
         }

         jsonobject.add("key", jsonobject1);
         JsonObject jsonobject2 = new JsonObject();
         jsonobject2.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         if (this.count > 1) {
            jsonobject2.addProperty("count", this.count);
         }

         jsonobject.add("result", jsonobject2);
         jsonobject.addProperty("show_notification", this.showNotification);
      }

      public RecipeSerializer<?> getType() {
         return RecipeSerializer.SHAPED_RECIPE;
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
