package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ErrorScreen extends Screen {
   private final Component message;

   public ErrorScreen(Component component, Component component1) {
      super(component);
      this.message = component1;
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen((Screen)null)).bounds(this.width / 2 - 100, 140, 200, 20).build());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
      guigraphics.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
      super.render(guigraphics, i, j, f);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
