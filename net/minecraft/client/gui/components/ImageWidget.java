package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageWidget extends AbstractWidget {
   private final ResourceLocation imageLocation;

   public ImageWidget(int i, int j, ResourceLocation resourcelocation) {
      this(0, 0, i, j, resourcelocation);
   }

   public ImageWidget(int i, int j, int k, int l, ResourceLocation resourcelocation) {
      super(i, j, k, l, Component.empty());
      this.imageLocation = resourcelocation;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      int k = this.getWidth();
      int l = this.getHeight();
      guigraphics.blit(this.imageLocation, this.getX(), this.getY(), 0.0F, 0.0F, k, l, k, l);
   }
}
