package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class StateSwitchingButton extends AbstractWidget {
   protected ResourceLocation resourceLocation;
   protected boolean isStateTriggered;
   protected int xTexStart;
   protected int yTexStart;
   protected int xDiffTex;
   protected int yDiffTex;

   public StateSwitchingButton(int i, int j, int k, int l, boolean flag) {
      super(i, j, k, l, CommonComponents.EMPTY);
      this.isStateTriggered = flag;
   }

   public void initTextureValues(int i, int j, int k, int l, ResourceLocation resourcelocation) {
      this.xTexStart = i;
      this.yTexStart = j;
      this.xDiffTex = k;
      this.yDiffTex = l;
      this.resourceLocation = resourcelocation;
   }

   public void setStateTriggered(boolean flag) {
      this.isStateTriggered = flag;
   }

   public boolean isStateTriggered() {
      return this.isStateTriggered;
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      this.defaultButtonNarrationText(narrationelementoutput);
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      RenderSystem.disableDepthTest();
      int k = this.xTexStart;
      int l = this.yTexStart;
      if (this.isStateTriggered) {
         k += this.xDiffTex;
      }

      if (this.isHoveredOrFocused()) {
         l += this.yDiffTex;
      }

      guigraphics.blit(this.resourceLocation, this.getX(), this.getY(), k, l, this.width, this.height);
      RenderSystem.enableDepthTest();
   }
}
