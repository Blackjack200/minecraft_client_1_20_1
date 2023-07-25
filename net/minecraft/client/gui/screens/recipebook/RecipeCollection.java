package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCollection {
   private final RegistryAccess registryAccess;
   private final List<Recipe<?>> recipes;
   private final boolean singleResultItem;
   private final Set<Recipe<?>> craftable = Sets.newHashSet();
   private final Set<Recipe<?>> fitsDimensions = Sets.newHashSet();
   private final Set<Recipe<?>> known = Sets.newHashSet();

   public RecipeCollection(RegistryAccess registryaccess, List<Recipe<?>> list) {
      this.registryAccess = registryaccess;
      this.recipes = ImmutableList.copyOf(list);
      if (list.size() <= 1) {
         this.singleResultItem = true;
      } else {
         this.singleResultItem = allRecipesHaveSameResult(registryaccess, list);
      }

   }

   private static boolean allRecipesHaveSameResult(RegistryAccess registryaccess, List<Recipe<?>> list) {
      int i = list.size();
      ItemStack itemstack = list.get(0).getResultItem(registryaccess);

      for(int j = 1; j < i; ++j) {
         ItemStack itemstack1 = list.get(j).getResultItem(registryaccess);
         if (!ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
            return false;
         }
      }

      return true;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public boolean hasKnownRecipes() {
      return !this.known.isEmpty();
   }

   public void updateKnownRecipes(RecipeBook recipebook) {
      for(Recipe<?> recipe : this.recipes) {
         if (recipebook.contains(recipe)) {
            this.known.add(recipe);
         }
      }

   }

   public void canCraft(StackedContents stackedcontents, int i, int j, RecipeBook recipebook) {
      for(Recipe<?> recipe : this.recipes) {
         boolean flag = recipe.canCraftInDimensions(i, j) && recipebook.contains(recipe);
         if (flag) {
            this.fitsDimensions.add(recipe);
         } else {
            this.fitsDimensions.remove(recipe);
         }

         if (flag && stackedcontents.canCraft(recipe, (IntList)null)) {
            this.craftable.add(recipe);
         } else {
            this.craftable.remove(recipe);
         }
      }

   }

   public boolean isCraftable(Recipe<?> recipe) {
      return this.craftable.contains(recipe);
   }

   public boolean hasCraftable() {
      return !this.craftable.isEmpty();
   }

   public boolean hasFitting() {
      return !this.fitsDimensions.isEmpty();
   }

   public List<Recipe<?>> getRecipes() {
      return this.recipes;
   }

   public List<Recipe<?>> getRecipes(boolean flag) {
      List<Recipe<?>> list = Lists.newArrayList();
      Set<Recipe<?>> set = flag ? this.craftable : this.fitsDimensions;

      for(Recipe<?> recipe : this.recipes) {
         if (set.contains(recipe)) {
            list.add(recipe);
         }
      }

      return list;
   }

   public List<Recipe<?>> getDisplayRecipes(boolean flag) {
      List<Recipe<?>> list = Lists.newArrayList();

      for(Recipe<?> recipe : this.recipes) {
         if (this.fitsDimensions.contains(recipe) && this.craftable.contains(recipe) == flag) {
            list.add(recipe);
         }
      }

      return list;
   }

   public boolean hasSingleResultItem() {
      return this.singleResultItem;
   }
}
