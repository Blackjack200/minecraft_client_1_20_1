package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class StonecutterScreen extends AbstractContainerScreen<StonecutterMenu> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int RECIPES_COLUMNS = 4;
   private static final int RECIPES_ROWS = 3;
   private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
   private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
   private static final int SCROLLER_FULL_HEIGHT = 54;
   private static final int RECIPES_X = 52;
   private static final int RECIPES_Y = 14;
   private float scrollOffs;
   private boolean scrolling;
   private int startIndex;
   private boolean displayRecipes;

   public StonecutterScreen(StonecutterMenu stonecuttermenu, Inventory inventory, Component component) {
      super(stonecuttermenu, inventory, component);
      stonecuttermenu.registerUpdateListener(this::containerChanged);
      --this.titleLabelY;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      this.renderBackground(guigraphics);
      int k = this.leftPos;
      int l = this.topPos;
      guigraphics.blit(BG_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      int i1 = (int)(41.0F * this.scrollOffs);
      guigraphics.blit(BG_LOCATION, k + 119, l + 15 + i1, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
      int j1 = this.leftPos + 52;
      int k1 = this.topPos + 14;
      int l1 = this.startIndex + 12;
      this.renderButtons(guigraphics, i, j, j1, k1, l1);
      this.renderRecipes(guigraphics, j1, k1, l1);
   }

   protected void renderTooltip(GuiGraphics guigraphics, int i, int j) {
      super.renderTooltip(guigraphics, i, j);
      if (this.displayRecipes) {
         int k = this.leftPos + 52;
         int l = this.topPos + 14;
         int i1 = this.startIndex + 12;
         List<StonecutterRecipe> list = this.menu.getRecipes();

         for(int j1 = this.startIndex; j1 < i1 && j1 < this.menu.getNumRecipes(); ++j1) {
            int k1 = j1 - this.startIndex;
            int l1 = k + k1 % 4 * 16;
            int i2 = l + k1 / 4 * 18 + 2;
            if (i >= l1 && i < l1 + 16 && j >= i2 && j < i2 + 18) {
               guigraphics.renderTooltip(this.font, list.get(j1).getResultItem(this.minecraft.level.registryAccess()), i, j);
            }
         }
      }

   }

   private void renderButtons(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      for(int j1 = this.startIndex; j1 < i1 && j1 < this.menu.getNumRecipes(); ++j1) {
         int k1 = j1 - this.startIndex;
         int l1 = k + k1 % 4 * 16;
         int i2 = k1 / 4;
         int j2 = l + i2 * 18 + 2;
         int k2 = this.imageHeight;
         if (j1 == this.menu.getSelectedRecipeIndex()) {
            k2 += 18;
         } else if (i >= l1 && j >= j2 && i < l1 + 16 && j < j2 + 18) {
            k2 += 36;
         }

         guigraphics.blit(BG_LOCATION, l1, j2 - 1, 0, k2, 16, 18);
      }

   }

   private void renderRecipes(GuiGraphics guigraphics, int i, int j, int k) {
      List<StonecutterRecipe> list = this.menu.getRecipes();

      for(int l = this.startIndex; l < k && l < this.menu.getNumRecipes(); ++l) {
         int i1 = l - this.startIndex;
         int j1 = i + i1 % 4 * 16;
         int k1 = i1 / 4;
         int l1 = j + k1 * 18 + 2;
         guigraphics.renderItem(list.get(l).getResultItem(this.minecraft.level.registryAccess()), j1, l1);
      }

   }

   public boolean mouseClicked(double d0, double d1, int i) {
      this.scrolling = false;
      if (this.displayRecipes) {
         int j = this.leftPos + 52;
         int k = this.topPos + 14;
         int l = this.startIndex + 12;

         for(int i1 = this.startIndex; i1 < l; ++i1) {
            int j1 = i1 - this.startIndex;
            double d2 = d0 - (double)(j + j1 % 4 * 16);
            double d3 = d1 - (double)(k + j1 / 4 * 18);
            if (d2 >= 0.0D && d3 >= 0.0D && d2 < 16.0D && d3 < 18.0D && this.menu.clickMenuButton(this.minecraft.player, i1)) {
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
               this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, i1);
               return true;
            }
         }

         j = this.leftPos + 119;
         k = this.topPos + 9;
         if (d0 >= (double)j && d0 < (double)(j + 12) && d1 >= (double)k && d1 < (double)(k + 54)) {
            this.scrolling = true;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (this.scrolling && this.isScrollBarActive()) {
         int j = this.topPos + 14;
         int k = j + 54;
         this.scrollOffs = ((float)d1 - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5D) * 4;
         return true;
      } else {
         return super.mouseDragged(d0, d1, i, d2, d3);
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      if (this.isScrollBarActive()) {
         int i = this.getOffscreenRows();
         float f = (float)d2 / (float)i;
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.startIndex = (int)((double)(this.scrollOffs * (float)i) + 0.5D) * 4;
      }

      return true;
   }

   private boolean isScrollBarActive() {
      return this.displayRecipes && this.menu.getNumRecipes() > 12;
   }

   protected int getOffscreenRows() {
      return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
   }

   private void containerChanged() {
      this.displayRecipes = this.menu.hasInputItem();
      if (!this.displayRecipes) {
         this.scrollOffs = 0.0F;
         this.startIndex = 0;
      }

   }
}
