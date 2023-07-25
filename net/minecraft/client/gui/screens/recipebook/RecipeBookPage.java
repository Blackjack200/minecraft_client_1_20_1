package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBookPage {
   public static final int ITEMS_PER_PAGE = 20;
   private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity(20);
   @Nullable
   private RecipeButton hoveredButton;
   private final OverlayRecipeComponent overlay = new OverlayRecipeComponent();
   private Minecraft minecraft;
   private final List<RecipeShownListener> showListeners = Lists.newArrayList();
   private List<RecipeCollection> recipeCollections = ImmutableList.of();
   private StateSwitchingButton forwardButton;
   private StateSwitchingButton backButton;
   private int totalPages;
   private int currentPage;
   private RecipeBook recipeBook;
   @Nullable
   private Recipe<?> lastClickedRecipe;
   @Nullable
   private RecipeCollection lastClickedRecipeCollection;

   public RecipeBookPage() {
      for(int i = 0; i < 20; ++i) {
         this.buttons.add(new RecipeButton());
      }

   }

   public void init(Minecraft minecraft, int i, int j) {
      this.minecraft = minecraft;
      this.recipeBook = minecraft.player.getRecipeBook();

      for(int k = 0; k < this.buttons.size(); ++k) {
         this.buttons.get(k).setPosition(i + 11 + 25 * (k % 5), j + 31 + 25 * (k / 5));
      }

      this.forwardButton = new StateSwitchingButton(i + 93, j + 137, 12, 17, false);
      this.forwardButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
      this.backButton = new StateSwitchingButton(i + 38, j + 137, 12, 17, true);
      this.backButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
   }

   public void addListener(RecipeBookComponent recipebookcomponent) {
      this.showListeners.remove(recipebookcomponent);
      this.showListeners.add(recipebookcomponent);
   }

   public void updateCollections(List<RecipeCollection> list, boolean flag) {
      this.recipeCollections = list;
      this.totalPages = (int)Math.ceil((double)list.size() / 20.0D);
      if (this.totalPages <= this.currentPage || flag) {
         this.currentPage = 0;
      }

      this.updateButtonsForPage();
   }

   private void updateButtonsForPage() {
      int i = 20 * this.currentPage;

      for(int j = 0; j < this.buttons.size(); ++j) {
         RecipeButton recipebutton = this.buttons.get(j);
         if (i + j < this.recipeCollections.size()) {
            RecipeCollection recipecollection = this.recipeCollections.get(i + j);
            recipebutton.init(recipecollection, this);
            recipebutton.visible = true;
         } else {
            recipebutton.visible = false;
         }
      }

      this.updateArrowButtons();
   }

   private void updateArrowButtons() {
      this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
      this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
   }

   public void render(GuiGraphics guigraphics, int i, int j, int k, int l, float f) {
      if (this.totalPages > 1) {
         String s = this.currentPage + 1 + "/" + this.totalPages;
         int i1 = this.minecraft.font.width(s);
         guigraphics.drawString(this.minecraft.font, s, i - i1 / 2 + 73, j + 141, -1, false);
      }

      this.hoveredButton = null;

      for(RecipeButton recipebutton : this.buttons) {
         recipebutton.render(guigraphics, k, l, f);
         if (recipebutton.visible && recipebutton.isHoveredOrFocused()) {
            this.hoveredButton = recipebutton;
         }
      }

      this.backButton.render(guigraphics, k, l, f);
      this.forwardButton.render(guigraphics, k, l, f);
      this.overlay.render(guigraphics, k, l, f);
   }

   public void renderTooltip(GuiGraphics guigraphics, int i, int j) {
      if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
         guigraphics.renderComponentTooltip(this.minecraft.font, this.hoveredButton.getTooltipText(), i, j);
      }

   }

   @Nullable
   public Recipe<?> getLastClickedRecipe() {
      return this.lastClickedRecipe;
   }

   @Nullable
   public RecipeCollection getLastClickedRecipeCollection() {
      return this.lastClickedRecipeCollection;
   }

   public void setInvisible() {
      this.overlay.setVisible(false);
   }

   public boolean mouseClicked(double d0, double d1, int i, int j, int k, int l, int i1) {
      this.lastClickedRecipe = null;
      this.lastClickedRecipeCollection = null;
      if (this.overlay.isVisible()) {
         if (this.overlay.mouseClicked(d0, d1, i)) {
            this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
            this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
         } else {
            this.overlay.setVisible(false);
         }

         return true;
      } else if (this.forwardButton.mouseClicked(d0, d1, i)) {
         ++this.currentPage;
         this.updateButtonsForPage();
         return true;
      } else if (this.backButton.mouseClicked(d0, d1, i)) {
         --this.currentPage;
         this.updateButtonsForPage();
         return true;
      } else {
         for(RecipeButton recipebutton : this.buttons) {
            if (recipebutton.mouseClicked(d0, d1, i)) {
               if (i == 0) {
                  this.lastClickedRecipe = recipebutton.getRecipe();
                  this.lastClickedRecipeCollection = recipebutton.getCollection();
               } else if (i == 1 && !this.overlay.isVisible() && !recipebutton.isOnlyOption()) {
                  this.overlay.init(this.minecraft, recipebutton.getCollection(), recipebutton.getX(), recipebutton.getY(), j + l / 2, k + 13 + i1 / 2, (float)recipebutton.getWidth());
               }

               return true;
            }
         }

         return false;
      }
   }

   public void recipesShown(List<Recipe<?>> list) {
      for(RecipeShownListener recipeshownlistener : this.showListeners) {
         recipeshownlistener.recipesShown(list);
      }

   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public RecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   protected void listButtons(Consumer<AbstractWidget> consumer) {
      consumer.accept(this.forwardButton);
      consumer.accept(this.backButton);
      this.buttons.forEach(consumer);
   }
}
