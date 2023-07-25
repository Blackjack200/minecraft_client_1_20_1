package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractButton extends AbstractWidget {
   protected static final int TEXTURE_Y_OFFSET = 46;
   protected static final int TEXTURE_WIDTH = 200;
   protected static final int TEXTURE_HEIGHT = 20;
   protected static final int TEXTURE_BORDER_X = 20;
   protected static final int TEXTURE_BORDER_Y = 4;
   protected static final int TEXT_MARGIN = 2;

   public AbstractButton(int i, int j, int k, int l, Component component) {
      super(i, j, k, l, component);
   }

   public abstract void onPress();

   protected void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      Minecraft minecraft = Minecraft.getInstance();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      guigraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.active ? 16777215 : 10526880;
      this.renderString(guigraphics, minecraft.font, k | Mth.ceil(this.alpha * 255.0F) << 24);
   }

   public void renderString(GuiGraphics guigraphics, Font font, int i) {
      this.renderScrollingString(guigraphics, font, 2, i);
   }

   private int getTextureY() {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (this.isHoveredOrFocused()) {
         i = 2;
      }

      return 46 + i * 20;
   }

   public void onClick(double d0, double d1) {
      this.onPress();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.active && this.visible) {
         if (CommonInputs.selected(i)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
