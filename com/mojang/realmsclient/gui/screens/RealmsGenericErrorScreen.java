package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsGenericErrorScreen extends RealmsScreen {
   private final Screen nextScreen;
   private final RealmsGenericErrorScreen.ErrorMessage lines;
   private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

   public RealmsGenericErrorScreen(RealmsServiceException realmsserviceexception, Screen screen) {
      super(GameNarrator.NO_TITLE);
      this.nextScreen = screen;
      this.lines = errorMessage(realmsserviceexception);
   }

   public RealmsGenericErrorScreen(Component component, Screen screen) {
      super(GameNarrator.NO_TITLE);
      this.nextScreen = screen;
      this.lines = errorMessage(component);
   }

   public RealmsGenericErrorScreen(Component component, Component component1, Screen screen) {
      super(GameNarrator.NO_TITLE);
      this.nextScreen = screen;
      this.lines = errorMessage(component, component1);
   }

   private static RealmsGenericErrorScreen.ErrorMessage errorMessage(RealmsServiceException realmsserviceexception) {
      RealmsError realmserror = realmsserviceexception.realmsError;
      if (realmserror == null) {
         return errorMessage(Component.translatable("mco.errorMessage.realmsService", realmsserviceexception.httpResultCode), Component.literal(realmsserviceexception.rawResponse));
      } else {
         int i = realmserror.getErrorCode();
         String s = "mco.errorMessage." + i;
         return errorMessage(Component.translatable("mco.errorMessage.realmsService.realmsError", i), (Component)(I18n.exists(s) ? Component.translatable(s) : Component.nullToEmpty(realmserror.getErrorMessage())));
      }
   }

   private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component component) {
      return errorMessage(Component.translatable("mco.errorMessage.generic"), component);
   }

   private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component component, Component component1) {
      return new RealmsGenericErrorScreen.ErrorMessage(component, component1);
   }

   public void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, (button) -> this.minecraft.setScreen(this.nextScreen)).bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());
      this.line2Split = MultiLineLabel.create(this.font, this.lines.detail, this.width * 3 / 4);
   }

   public Component getNarrationMessage() {
      return Component.empty().append(this.lines.title).append(": ").append(this.lines.detail);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.lines.title, this.width / 2, 80, 16777215);
      this.line2Split.renderCentered(guigraphics, this.width / 2, 100, 9, 16711680);
      super.render(guigraphics, i, j, f);
   }

   static record ErrorMessage(Component title, Component detail) {
      final Component title;
      final Component detail;
   }
}
