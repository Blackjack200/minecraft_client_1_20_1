package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabButton extends AbstractWidget {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/tab_button.png");
   private static final int TEXTURE_WIDTH = 130;
   private static final int TEXTURE_HEIGHT = 24;
   private static final int TEXTURE_BORDER = 2;
   private static final int TEXTURE_BORDER_BOTTOM = 0;
   private static final int SELECTED_OFFSET = 3;
   private static final int TEXT_MARGIN = 1;
   private static final int UNDERLINE_HEIGHT = 1;
   private static final int UNDERLINE_MARGIN_X = 4;
   private static final int UNDERLINE_MARGIN_BOTTOM = 2;
   private final TabManager tabManager;
   private final Tab tab;

   public TabButton(TabManager tabmanager, Tab tab, int i, int j) {
      super(0, 0, i, j, tab.getTabTitle());
      this.tabManager = tabmanager;
      this.tab = tab;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.blitNineSliced(TEXTURE_LOCATION, this.getX(), this.getY(), this.width, this.height, 2, 2, 2, 0, 130, 24, 0, this.getTextureY());
      Font font = Minecraft.getInstance().font;
      int k = this.active ? -1 : -6250336;
      this.renderString(guigraphics, font, k);
      if (this.isSelected()) {
         this.renderFocusUnderline(guigraphics, font, k);
      }

   }

   public void renderString(GuiGraphics guigraphics, Font font, int i) {
      int j = this.getX() + 1;
      int k = this.getY() + (this.isSelected() ? 0 : 3);
      int l = this.getX() + this.getWidth() - 1;
      int i1 = this.getY() + this.getHeight();
      renderScrollingString(guigraphics, font, this.getMessage(), j, k, l, i1, i);
   }

   private void renderFocusUnderline(GuiGraphics guigraphics, Font font, int i) {
      int j = Math.min(font.width(this.getMessage()), this.getWidth() - 4);
      int k = this.getX() + (this.getWidth() - j) / 2;
      int l = this.getY() + this.getHeight() - 2;
      guigraphics.fill(k, l, k + j, l + 1, i);
   }

   protected int getTextureY() {
      int i = 2;
      if (this.isSelected() && this.isHoveredOrFocused()) {
         i = 1;
      } else if (this.isSelected()) {
         i = 0;
      } else if (this.isHoveredOrFocused()) {
         i = 3;
      }

      return i * 24;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.tab", this.tab.getTabTitle()));
   }

   public void playDownSound(SoundManager soundmanager) {
   }

   public Tab tab() {
      return this.tab;
   }

   public boolean isSelected() {
      return this.tabManager.getCurrentTab() == this.tab;
   }
}
