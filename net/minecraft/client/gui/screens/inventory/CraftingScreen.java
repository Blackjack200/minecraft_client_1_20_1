package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;

public class CraftingScreen extends AbstractContainerScreen<CraftingMenu> implements RecipeUpdateListener {
   private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
   private boolean widthTooNarrow;

   public CraftingScreen(CraftingMenu craftingmenu, Inventory inventory, Component component) {
      super(craftingmenu, inventory, component);
   }

   protected void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
      this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
      this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
         this.recipeBookComponent.toggleVisibility();
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
         button.setPosition(this.leftPos + 5, this.height / 2 - 49);
      }));
      this.addWidget(this.recipeBookComponent);
      this.setInitialFocus(this.recipeBookComponent);
      this.titleLabelX = 29;
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
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(CRAFTING_TABLE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
   }

   protected boolean isHovering(int i, int j, int k, int l, double d0, double d1) {
      return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(i, j, k, l, d0, d1);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.recipeBookComponent.mouseClicked(d0, d1, i)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(d0, d1, i);
      }
   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      boolean flag = d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(d0, d1, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && flag;
   }

   protected void slotClicked(Slot slot, int i, int j, ClickType clicktype) {
      super.slotClicked(slot, i, j, clicktype);
      this.recipeBookComponent.slotClicked(slot);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public RecipeBookComponent getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}
