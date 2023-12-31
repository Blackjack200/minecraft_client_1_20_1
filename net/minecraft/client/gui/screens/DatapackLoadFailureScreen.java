package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DatapackLoadFailureScreen extends Screen {
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   private final Runnable callback;

   public DatapackLoadFailureScreen(Runnable runnable) {
      super(Component.translatable("datapackFailure.title"));
      this.callback = runnable;
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
      this.addRenderableWidget(Button.builder(Component.translatable("datapackFailure.safeMode"), (button1) -> this.callback.run()).bounds(this.width / 2 - 155, this.height / 6 + 96, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_TO_TITLE, (button) -> this.minecraft.setScreen((Screen)null)).bounds(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20).build());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.message.renderCentered(guigraphics, this.width / 2, 70);
      super.render(guigraphics, i, j, f);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
