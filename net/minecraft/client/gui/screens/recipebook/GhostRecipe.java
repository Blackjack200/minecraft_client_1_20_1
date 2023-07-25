package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class GhostRecipe {
   @Nullable
   private Recipe<?> recipe;
   private final List<GhostRecipe.GhostIngredient> ingredients = Lists.newArrayList();
   float time;

   public void clear() {
      this.recipe = null;
      this.ingredients.clear();
      this.time = 0.0F;
   }

   public void addIngredient(Ingredient ingredient, int i, int j) {
      this.ingredients.add(new GhostRecipe.GhostIngredient(ingredient, i, j));
   }

   public GhostRecipe.GhostIngredient get(int i) {
      return this.ingredients.get(i);
   }

   public int size() {
      return this.ingredients.size();
   }

   @Nullable
   public Recipe<?> getRecipe() {
      return this.recipe;
   }

   public void setRecipe(Recipe<?> recipe) {
      this.recipe = recipe;
   }

   public void render(GuiGraphics guigraphics, Minecraft minecraft, int i, int j, boolean flag, float f) {
      if (!Screen.hasControlDown()) {
         this.time += f;
      }

      for(int k = 0; k < this.ingredients.size(); ++k) {
         GhostRecipe.GhostIngredient ghostrecipe_ghostingredient = this.ingredients.get(k);
         int l = ghostrecipe_ghostingredient.getX() + i;
         int i1 = ghostrecipe_ghostingredient.getY() + j;
         if (k == 0 && flag) {
            guigraphics.fill(l - 4, i1 - 4, l + 20, i1 + 20, 822018048);
         } else {
            guigraphics.fill(l, i1, l + 16, i1 + 16, 822018048);
         }

         ItemStack itemstack = ghostrecipe_ghostingredient.getItem();
         guigraphics.renderFakeItem(itemstack, l, i1);
         guigraphics.fill(RenderType.guiGhostRecipeOverlay(), l, i1, l + 16, i1 + 16, 822083583);
         if (k == 0) {
            guigraphics.renderItemDecorations(minecraft.font, itemstack, l, i1);
         }
      }

   }

   public class GhostIngredient {
      private final Ingredient ingredient;
      private final int x;
      private final int y;

      public GhostIngredient(Ingredient ingredient, int i, int j) {
         this.ingredient = ingredient;
         this.x = i;
         this.y = j;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public ItemStack getItem() {
         ItemStack[] aitemstack = this.ingredient.getItems();
         return aitemstack.length == 0 ? ItemStack.EMPTY : aitemstack[Mth.floor(GhostRecipe.this.time / 30.0F) % aitemstack.length];
      }
   }
}
