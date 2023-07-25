package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

public class RealmsLabel implements Renderable {
   private final Component text;
   private final int x;
   private final int y;
   private final int color;

   public RealmsLabel(Component component, int i, int j, int k) {
      this.text = component;
      this.x = i;
      this.y = j;
      this.color = k;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.drawCenteredString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
   }

   public Component getText() {
      return this.text;
   }
}
