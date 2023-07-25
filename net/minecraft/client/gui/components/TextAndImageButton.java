package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TextAndImageButton extends Button {
   protected final ResourceLocation resourceLocation;
   protected final int xTexStart;
   protected final int yTexStart;
   protected final int yDiffTex;
   protected final int textureWidth;
   protected final int textureHeight;
   private final int xOffset;
   private final int yOffset;
   private final int usedTextureWidth;
   private final int usedTextureHeight;

   TextAndImageButton(Component component, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
      super(0, 0, 150, 20, component, button_onpress, DEFAULT_NARRATION);
      this.textureWidth = l1;
      this.textureHeight = i2;
      this.xTexStart = i;
      this.yTexStart = j;
      this.yDiffTex = i1;
      this.resourceLocation = resourcelocation;
      this.xOffset = k;
      this.yOffset = l;
      this.usedTextureWidth = j1;
      this.usedTextureHeight = k1;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      super.renderWidget(guigraphics, i, j, f);
      this.renderTexture(guigraphics, this.resourceLocation, this.getXOffset(), this.getYOffset(), this.xTexStart, this.yTexStart, this.yDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
   }

   public void renderString(GuiGraphics guigraphics, Font font, int i) {
      int j = this.getX() + 2;
      int k = this.getX() + this.getWidth() - this.usedTextureWidth - 6;
      renderScrollingString(guigraphics, font, this.getMessage(), j, this.getY(), k, this.getY() + this.getHeight(), i);
   }

   private int getXOffset() {
      return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
   }

   private int getYOffset() {
      return this.getY() + this.yOffset;
   }

   public static TextAndImageButton.Builder builder(Component component, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
      return new TextAndImageButton.Builder(component, resourcelocation, button_onpress);
   }

   public static class Builder {
      private final Component message;
      private final ResourceLocation resourceLocation;
      private final Button.OnPress onPress;
      private int xTexStart;
      private int yTexStart;
      private int yDiffTex;
      private int usedTextureWidth;
      private int usedTextureHeight;
      private int textureWidth;
      private int textureHeight;
      private int xOffset;
      private int yOffset;

      public Builder(Component component, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
         this.message = component;
         this.resourceLocation = resourcelocation;
         this.onPress = button_onpress;
      }

      public TextAndImageButton.Builder texStart(int i, int j) {
         this.xTexStart = i;
         this.yTexStart = j;
         return this;
      }

      public TextAndImageButton.Builder offset(int i, int j) {
         this.xOffset = i;
         this.yOffset = j;
         return this;
      }

      public TextAndImageButton.Builder yDiffTex(int i) {
         this.yDiffTex = i;
         return this;
      }

      public TextAndImageButton.Builder usedTextureSize(int i, int j) {
         this.usedTextureWidth = i;
         this.usedTextureHeight = j;
         return this;
      }

      public TextAndImageButton.Builder textureSize(int i, int j) {
         this.textureWidth = i;
         this.textureHeight = j;
         return this;
      }

      public TextAndImageButton build() {
         return new TextAndImageButton(this.message, this.xTexStart, this.yTexStart, this.xOffset, this.yOffset, this.yDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight, this.resourceLocation, this.onPress);
      }
   }
}
