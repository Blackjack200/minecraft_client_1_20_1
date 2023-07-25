package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
   private boolean widthTooNarrow;
   private final ResourceLocation texture;

   public AbstractFurnaceScreen(T abstractfurnacemenu, AbstractFurnaceRecipeBookComponent abstractfurnacerecipebookcomponent, Inventory inventory, Component component, ResourceLocation resourcelocation) {
      super(abstractfurnacemenu, inventory, component);
      this.recipeBookComponent = abstractfurnacerecipebookcomponent;
      this.texture = resourcelocation;
   }

   public void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
      this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
      this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
         this.recipeBookComponent.toggleVisibility();
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
         button.setPosition(this.leftPos + 20, this.height / 2 - 49);
      }));
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }

   public void containerTick() {
      super.containerTick();
      this.recipeBookComponent.tick();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(guigraphics, f, i, j);
         this.recipeBookComponent.render(guigraphics, i, j, f);
      } else {
         this.recipeBookComponent.render(guigraphics, i, j, f);
         super.render(guigraphics, i, j, f);
         this.recipeBookComponent.renderGhostRecipe(guigraphics, this.leftPos, this.topPos, true, f);
      }

      this.renderTooltip(guigraphics, i, j);
      this.recipeBookComponent.renderTooltip(guigraphics, this.leftPos, this.topPos, i, j);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = this.leftPos;
      int l = this.topPos;
      guigraphics.blit(this.texture, k, l, 0, 0, this.imageWidth, this.imageHeight);
      if (this.menu.isLit()) {
         int i1 = this.menu.getLitProgress();
         guigraphics.blit(this.texture, k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 1);
      }

      int j1 = this.menu.getBurnProgress();
      guigraphics.blit(this.texture, k + 79, l + 34, 176, 14, j1 + 1, 16);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.recipeBookComponent.mouseClicked(d0, d1, i)) {
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(d0, d1, i);
      }
   }

   protected void slotClicked(Slot slot, int i, int j, ClickType clicktype) {
      super.slotClicked(slot, i, j, clicktype);
      this.recipeBookComponent.slotClicked(slot);
   }

   public boolean keyPressed(int i, int j, int k) {
      return this.recipeBookComponent.keyPressed(i, j, k) ? false : super.keyPressed(i, j, k);
   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      boolean flag = d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(d0, d1, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && flag;
   }

   public boolean charTyped(char c0, int i) {
      return this.recipeBookComponent.charTyped(c0, i) ? true : super.charTyped(c0, i);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public RecipeBookComponent getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}
