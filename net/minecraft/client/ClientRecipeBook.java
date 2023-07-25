package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;

public class ClientRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
   private List<RecipeCollection> allCollections = ImmutableList.of();

   public void setupCollections(Iterable<Recipe<?>> iterable, RegistryAccess registryaccess) {
      Map<RecipeBookCategories, List<List<Recipe<?>>>> map = categorizeAndGroupRecipes(iterable);
      Map<RecipeBookCategories, List<RecipeCollection>> map1 = Maps.newHashMap();
      ImmutableList.Builder<RecipeCollection> immutablelist_builder = ImmutableList.builder();
      map.forEach((recipebookcategories2, list1) -> map1.put(recipebookcategories2, list1.stream().map((list2) -> new RecipeCollection(registryaccess, list2)).peek(immutablelist_builder::add).collect(ImmutableList.toImmutableList())));
      RecipeBookCategories.AGGREGATE_CATEGORIES.forEach((recipebookcategories, list) -> map1.put(recipebookcategories, list.stream().flatMap((recipebookcategories1) -> map1.getOrDefault(recipebookcategories1, ImmutableList.of()).stream()).collect(ImmutableList.toImmutableList())));
      this.collectionsByTab = ImmutableMap.copyOf(map1);
      this.allCollections = immutablelist_builder.build();
   }

   private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> iterable) {
      Map<RecipeBookCategories, List<List<Recipe<?>>>> map = Maps.newHashMap();
      Table<RecipeBookCategories, String, List<Recipe<?>>> table = HashBasedTable.create();

      for(Recipe<?> recipe : iterable) {
         if (!recipe.isSpecial() && !recipe.isIncomplete()) {
            RecipeBookCategories recipebookcategories = getCategory(recipe);
            String s = recipe.getGroup();
            if (s.isEmpty()) {
               map.computeIfAbsent(recipebookcategories, (recipebookcategories2) -> Lists.newArrayList()).add(ImmutableList.of(recipe));
            } else {
               List<Recipe<?>> list = table.get(recipebookcategories, s);
               if (list == null) {
                  list = Lists.newArrayList();
                  table.put(recipebookcategories, s, list);
                  map.computeIfAbsent(recipebookcategories, (recipebookcategories1) -> Lists.newArrayList()).add(list);
               }

               list.add(recipe);
            }
         }
      }

      return map;
   }

   private static RecipeBookCategories getCategory(Recipe<?> recipe) {
      if (recipe instanceof CraftingRecipe) {
         CraftingRecipe craftingrecipe = (CraftingRecipe)recipe;
         RecipeBookCategories var5;
         switch (craftingrecipe.category()) {
            case BUILDING:
               var5 = RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
               break;
            case EQUIPMENT:
               var5 = RecipeBookCategories.CRAFTING_EQUIPMENT;
               break;
            case REDSTONE:
               var5 = RecipeBookCategories.CRAFTING_REDSTONE;
               break;
            case MISC:
               var5 = RecipeBookCategories.CRAFTING_MISC;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var5;
      } else {
         RecipeType<?> recipetype = recipe.getType();
         if (recipe instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe abstractcookingrecipe = (AbstractCookingRecipe)recipe;
            CookingBookCategory cookingbookcategory = abstractcookingrecipe.category();
            if (recipetype == RecipeType.SMELTING) {
               RecipeBookCategories var10000;
               switch (cookingbookcategory) {
                  case BLOCKS:
                     var10000 = RecipeBookCategories.FURNACE_BLOCKS;
                     break;
                  case FOOD:
                     var10000 = RecipeBookCategories.FURNACE_FOOD;
                     break;
                  case MISC:
                     var10000 = RecipeBookCategories.FURNACE_MISC;
                     break;
                  default:
                     throw new IncompatibleClassChangeError();
               }

               return var10000;
            }

            if (recipetype == RecipeType.BLASTING) {
               return cookingbookcategory == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
            }

            if (recipetype == RecipeType.SMOKING) {
               return RecipeBookCategories.SMOKER_FOOD;
            }

            if (recipetype == RecipeType.CAMPFIRE_COOKING) {
               return RecipeBookCategories.CAMPFIRE;
            }
         }

         if (recipetype == RecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
         } else if (recipetype == RecipeType.SMITHING) {
            return RecipeBookCategories.SMITHING;
         } else {
            LOGGER.warn("Unknown recipe category: {}/{}", LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType())), LogUtils.defer(recipe::getId));
            return RecipeBookCategories.UNKNOWN;
         }
      }
   }

   public List<RecipeCollection> getCollections() {
      return this.allCollections;
   }

   public List<RecipeCollection> getCollection(RecipeBookCategories recipebookcategories) {
      return this.collectionsByTab.getOrDefault(recipebookcategories, Collections.emptyList());
   }
}
