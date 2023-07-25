package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class OverlayRecipeComponent implements Renderable, GuiEventListener {
   static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
   private static final int MAX_ROW = 4;
   private static final int MAX_ROW_LARGE = 5;
   private static final float ITEM_RENDER_SCALE = 0.375F;
   public static final int BUTTON_SIZE = 25;
   private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.newArrayList();
   private boolean isVisible;
   private int x;
   private int y;
   private Minecraft minecraft;
   private RecipeCollection collection;
   @Nullable
   private Recipe<?> lastRecipeClicked;
   float time;
   boolean isFurnaceMenu;

   public void init(Minecraft minecraft, RecipeCollection recipecollection, int i, int j, int k, int l, float f) {
      this.minecraft = minecraft;
      this.collection = recipecollection;
      if (minecraft.player.containerMenu instanceof AbstractFurnaceMenu) {
         this.isFurnaceMenu = true;
      }

      boolean flag = minecraft.player.getRecipeBook().isFiltering((RecipeBookMenu)minecraft.player.containerMenu);
      List<Recipe<?>> list = recipecollection.getDisplayRecipes(true);
      List<Recipe<?>> list1 = flag ? Collections.emptyList() : recipecollection.getDisplayRecipes(false);
      int i1 = list.size();
      int j1 = i1 + list1.size();
      int k1 = j1 <= 16 ? 4 : 5;
      int l1 = (int)Math.ceil((double)((float)j1 / (float)k1));
      this.x = i;
      this.y = j;
      float f1 = (float)(this.x + Math.min(j1, k1) * 25);
      float f2 = (float)(k + 50);
      if (f1 > f2) {
         this.x = (int)((float)this.x - f * (float)((int)((f1 - f2) / f)));
      }

      float f3 = (float)(this.y + l1 * 25);
      float f4 = (float)(l + 50);
      if (f3 > f4) {
         this.y = (int)((float)this.y - f * (float)Mth.ceil((f3 - f4) / f));
      }

      float f5 = (float)this.y;
      float f6 = (float)(l - 100);
      if (f5 < f6) {
         this.y = (int)((float)this.y - f * (float)Mth.ceil((f5 - f6) / f));
      }

      this.isVisible = true;
      this.recipeButtons.clear();

      for(int i2 = 0; i2 < j1; ++i2) {
         boolean flag1 = i2 < i1;
         Recipe<?> recipe = flag1 ? list.get(i2) : list1.get(i2 - i1);
         int j2 = this.x + 4 + 25 * (i2 % k1);
         int k2 = this.y + 5 + 25 * (i2 / k1);
         if (this.isFurnaceMenu) {
            this.recipeButtons.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(j2, k2, recipe, flag1));
         } else {
            this.recipeButtons.add(new OverlayRecipeComponent.OverlayRecipeButton(j2, k2, recipe, flag1));
         }
      }

      this.lastRecipeClicked = null;
   }

   public RecipeCollection getRecipeCollection() {
      return this.collection;
   }

   @Nullable
   public Recipe<?> getLastRecipeClicked() {
      return this.lastRecipeClicked;
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (i != 0) {
         return false;
      } else {
         for(OverlayRecipeComponent.OverlayRecipeButton overlayrecipecomponent_overlayrecipebutton : this.recipeButtons) {
            if (overlayrecipecomponent_overlayrecipebutton.mouseClicked(d0, d1, i)) {
               this.lastRecipeClicked = overlayrecipecomponent_overlayrecipebutton.recipe;
               return true;
            }
         }

         return false;
      }
   }

   public boolean isMouseOver(double d0, double d1) {
      return false;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.isVisible) {
         this.time += f;
         RenderSystem.enableBlend();
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, 1000.0F);
         int k = this.recipeButtons.size() <= 16 ? 4 : 5;
         int l = Math.min(this.recipeButtons.size(), k);
         int i1 = Mth.ceil((float)this.recipeButtons.size() / (float)k);
         int j1 = 4;
         guigraphics.blitNineSliced(RECIPE_BOOK_LOCATION, this.x, this.y, l * 25 + 8, i1 * 25 + 8, 4, 32, 32, 82, 208);
         RenderSystem.disableBlend();

         for(OverlayRecipeComponent.OverlayRecipeButton overlayrecipecomponent_overlayrecipebutton : this.recipeButtons) {
            overlayrecipecomponent_overlayrecipebutton.render(guigraphics, i, j, f);
         }

         guigraphics.pose().popPose();
      }
   }

   public void setVisible(boolean flag) {
      this.isVisible = flag;
   }

   public boolean isVisible() {
      return this.isVisible;
   }

   public void setFocused(boolean flag) {
   }

   public boolean isFocused() {
      return false;
   }

   class OverlayRecipeButton extends AbstractWidget implements PlaceRecipe<Ingredient> {
      final Recipe<?> recipe;
      private final boolean isCraftable;
      protected final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> ingredientPos = Lists.newArrayList();

      public OverlayRecipeButton(int i, int j, Recipe<?> recipe, boolean flag) {
         super(i, j, 200, 20, CommonComponents.EMPTY);
         this.width = 24;
         this.height = 24;
         this.recipe = recipe;
         this.isCraftable = flag;
         this.calculateIngredientsPositions(recipe);
      }

      protected void calculateIngredientsPositions(Recipe<?> recipe) {
         this.placeRecipe(3, 3, -1, recipe, recipe.getIngredients().iterator(), 0);
      }

      public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
         this.defaultButtonNarrationText(narrationelementoutput);
      }

      public void addItemToSlot(Iterator<Ingredient> iterator, int i, int j, int k, int l) {
         ItemStack[] aitemstack = iterator.next().getItems();
         if (aitemstack.length != 0) {
            this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(3 + l * 7, 3 + k * 7, aitemstack));
         }

      }

      public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
         int k = 152;
         if (!this.isCraftable) {
            k += 26;
         }

         int l = OverlayRecipeComponent.this.isFurnaceMenu ? 130 : 78;
         if (this.isHoveredOrFocused()) {
            l += 26;
         }

         guigraphics.blit(OverlayRecipeComponent.RECIPE_BOOK_LOCATION, this.getX(), this.getY(), k, l, this.width, this.height);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0D);

         for(OverlayRecipeComponent.OverlayRecipeButton.Pos overlayrecipecomponent_overlayrecipebutton_pos : this.ingredientPos) {
            guigraphics.pose().pushPose();
            guigraphics.pose().translate((double)overlayrecipecomponent_overlayrecipebutton_pos.x, (double)overlayrecipecomponent_overlayrecipebutton_pos.y, 0.0D);
            guigraphics.pose().scale(0.375F, 0.375F, 1.0F);
            guigraphics.pose().translate(-8.0D, -8.0D, 0.0D);
            if (overlayrecipecomponent_overlayrecipebutton_pos.ingredients.length > 0) {
               guigraphics.renderItem(overlayrecipecomponent_overlayrecipebutton_pos.ingredients[Mth.floor(OverlayRecipeComponent.this.time / 30.0F) % overlayrecipecomponent_overlayrecipebutton_pos.ingredients.length], 0, 0);
            }

            guigraphics.pose().popPose();
         }

         guigraphics.pose().popPose();
      }

      protected class Pos {
         public final ItemStack[] ingredients;
         public final int x;
         public final int y;

         public Pos(int i, int j, ItemStack[] aitemstack) {
            this.x = i;
            this.y = j;
            this.ingredients = aitemstack;
         }
      }
   }

   class OverlaySmeltingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
      public OverlaySmeltingRecipeButton(int i, int j, Recipe<?> recipe, boolean flag) {
         super(i, j, recipe, flag);
      }

      protected void calculateIngredientsPositions(Recipe<?> recipe) {
         ItemStack[] aitemstack = recipe.getIngredients().get(0).getItems();
         this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(10, 10, aitemstack));
      }
   }
}
