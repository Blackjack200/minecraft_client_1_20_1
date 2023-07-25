package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OutOfMemoryScreen extends Screen {
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   public OutOfMemoryScreen() {
      super(Component.translatable("outOfMemory.title"));
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_TO_TITLE, (button1) -> this.minecraft.setScreen(new TitleScreen())).bounds(this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button) -> this.minecraft.stop()).bounds(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20).build());
      this.message = MultiLineLabel.create(this.font, Component.translatable("outOfMemory.message"), 295);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
      this.message.renderLeftAligned(guigraphics, this.width / 2 - 145, this.height / 4, 9, 10526880);
      super.render(guigraphics, i, j, f);
   }
}
