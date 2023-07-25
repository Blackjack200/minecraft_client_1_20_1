package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsLongConfirmationScreen extends RealmsScreen {
   static final Component WARNING = Component.translatable("mco.warning");
   static final Component INFO = Component.translatable("mco.info");
   private final RealmsLongConfirmationScreen.Type type;
   private final Component line2;
   private final Component line3;
   protected final BooleanConsumer callback;
   private final boolean yesNoQuestion;

   public RealmsLongConfirmationScreen(BooleanConsumer booleanconsumer, RealmsLongConfirmationScreen.Type realmslongconfirmationscreen_type, Component component, Component component1, boolean flag) {
      super(GameNarrator.NO_TITLE);
      this.callback = booleanconsumer;
      this.type = realmslongconfirmationscreen_type;
      this.line2 = component;
      this.line3 = component1;
      this.yesNoQuestion = flag;
   }

   public void init() {
      if (this.yesNoQuestion) {
         this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, (button2) -> this.callback.accept(true)).bounds(this.width / 2 - 105, row(8), 100, 20).build());
         this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, (button1) -> this.callback.accept(false)).bounds(this.width / 2 + 5, row(8), 100, 20).build());
      } else {
         this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, (button) -> this.callback.accept(true)).bounds(this.width / 2 - 50, row(8), 100, 20).build());
      }

   }

   public Component getNarrationMessage() {
      return CommonComponents.joinLines(this.type.text, this.line2, this.line3);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.callback.accept(false);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
      guigraphics.drawCenteredString(this.font, this.line2, this.width / 2, row(4), 16777215);
      guigraphics.drawCenteredString(this.font, this.line3, this.width / 2, row(6), 16777215);
      super.render(guigraphics, i, j, f);
   }

   public static enum Type {
      WARNING(RealmsLongConfirmationScreen.WARNING, 16711680),
      INFO(RealmsLongConfirmationScreen.INFO, 8226750);

      public final int colorCode;
      public final Component text;

      private Type(Component component, int i) {
         this.text = component;
         this.colorCode = i;
      }
   }
}
