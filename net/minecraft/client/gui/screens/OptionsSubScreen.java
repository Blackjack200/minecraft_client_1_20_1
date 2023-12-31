package net.minecraft.client.gui.screens;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;

public class OptionsSubScreen extends Screen {
   protected final Screen lastScreen;
   protected final Options options;

   public OptionsSubScreen(Screen screen, Options options, Component component) {
      super(component);
      this.lastScreen = screen;
      this.options = options;
   }

   public void removed() {
      this.minecraft.options.save();
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   protected void basicListRender(GuiGraphics guigraphics, OptionsList optionslist, int i, int j, float f) {
      this.renderBackground(guigraphics);
      optionslist.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(guigraphics, i, j, f);
   }
}
