package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageButton extends Button {
   protected final ResourceLocation resourceLocation;
   protected final int xTexStart;
   protected final int yTexStart;
   protected final int yDiffTex;
   protected final int textureWidth;
   protected final int textureHeight;

   public ImageButton(int i, int j, int k, int l, int i1, int j1, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
      this(i, j, k, l, i1, j1, l, resourcelocation, 256, 256, button_onpress);
   }

   public ImageButton(int i, int j, int k, int l, int i1, int j1, int k1, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
      this(i, j, k, l, i1, j1, k1, resourcelocation, 256, 256, button_onpress);
   }

   public ImageButton(int i, int j, int k, int l, int i1, int j1, int k1, ResourceLocation resourcelocation, int l1, int i2, Button.OnPress button_onpress) {
      this(i, j, k, l, i1, j1, k1, resourcelocation, l1, i2, button_onpress, CommonComponents.EMPTY);
   }

   public ImageButton(int i, int j, int k, int l, int i1, int j1, int k1, ResourceLocation resourcelocation, int l1, int i2, Button.OnPress button_onpress, Component component) {
      super(i, j, k, l, component, button_onpress, DEFAULT_NARRATION);
      this.textureWidth = l1;
      this.textureHeight = i2;
      this.xTexStart = i1;
      this.yTexStart = j1;
      this.yDiffTex = k1;
      this.resourceLocation = resourcelocation;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderTexture(guigraphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
   }
}
