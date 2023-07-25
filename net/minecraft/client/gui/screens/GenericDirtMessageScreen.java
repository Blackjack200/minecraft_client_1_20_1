package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GenericDirtMessageScreen extends Screen {
   public GenericDirtMessageScreen(Component component) {
      super(component);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderDirtBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
      super.render(guigraphics, i, j, f);
   }
}
