package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsConfirmScreen extends RealmsScreen {
   protected BooleanConsumer callback;
   private final Component title1;
   private final Component title2;

   public RealmsConfirmScreen(BooleanConsumer booleanconsumer, Component component, Component component1) {
      super(GameNarrator.NO_TITLE);
      this.callback = booleanconsumer;
      this.title1 = component;
      this.title2 = component1;
   }

   public void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, (button1) -> this.callback.accept(true)).bounds(this.width / 2 - 105, row(9), 100, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, (button) -> this.callback.accept(false)).bounds(this.width / 2 + 5, row(9), 100, 20).build());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title1, this.width / 2, row(3), 16777215);
      guigraphics.drawCenteredString(this.font, this.title2, this.width / 2, row(5), 16777215);
      super.render(guigraphics, i, j, f);
   }
}
