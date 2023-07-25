package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;

public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
   public RecipeBookMenu(MenuType<?> menutype, int i) {
      super(menutype, i);
   }

   public void handlePlacement(boolean flag, Recipe<?> recipe, ServerPlayer serverplayer) {
      (new ServerPlaceRecipe<>(this)).recipeClicked(serverplayer, recipe, flag);
   }

   public abstract void fillCraftSlotsStackedContents(StackedContents stackedcontents);

   public abstract void clearCraftingContent();

   public abstract boolean recipeMatches(Recipe<? super C> recipe);

   public abstract int getResultSlotIndex();

   public abstract int getGridWidth();

   public abstract int getGridHeight();

   public abstract int getSize();

   public abstract RecipeBookType getRecipeBookType();

   public abstract boolean shouldMoveToInventory(int i);
}
