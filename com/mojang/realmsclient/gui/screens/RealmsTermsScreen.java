package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.realms.RealmsScreen;
import org.slf4j.Logger;

public class RealmsTermsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component TITLE = Component.translatable("mco.terms.title");
   private static final Component TERMS_STATIC_TEXT = Component.translatable("mco.terms.sentence.1");
   private static final Component TERMS_LINK_TEXT = CommonComponents.space().append(Component.translatable("mco.terms.sentence.2").withStyle(Style.EMPTY.withUnderlined(true)));
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   private final RealmsServer realmsServer;
   private boolean onLink;

   public RealmsTermsScreen(Screen screen, RealmsMainScreen realmsmainscreen, RealmsServer realmsserver) {
      super(TITLE);
      this.lastScreen = screen;
      this.mainScreen = realmsmainscreen;
      this.realmsServer = realmsserver;
   }

   public void init() {
      int i = this.width / 4 - 2;
      this.addRenderableWidget(Button.builder(Component.translatable("mco.terms.buttons.agree"), (button1) -> this.agreedToTos()).bounds(this.width / 4, row(12), i, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("mco.terms.buttons.disagree"), (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 4, row(12), i, 20).build());
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   private void agreedToTos() {
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.agreeToTos();
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new GetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())));
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't agree to TOS");
      }

   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.onLink) {
         this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftRealmsTerms");
         Util.getPlatform().openUri("https://aka.ms/MinecraftRealmsTerms");
         return true;
      } else {
         return super.mouseClicked(d0, d1, i);
      }
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), TERMS_STATIC_TEXT).append(CommonComponents.SPACE).append(TERMS_LINK_TEXT);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      guigraphics.drawString(this.font, TERMS_STATIC_TEXT, this.width / 2 - 120, row(5), 16777215, false);
      int k = this.font.width(TERMS_STATIC_TEXT);
      int l = this.width / 2 - 121 + k;
      int i1 = row(5);
      int j1 = l + this.font.width(TERMS_LINK_TEXT) + 1;
      int k1 = i1 + 1 + 9;
      this.onLink = l <= i && i <= j1 && i1 <= j && j <= k1;
      guigraphics.drawString(this.font, TERMS_LINK_TEXT, this.width / 2 - 120 + k, row(5), this.onLink ? 7107012 : 3368635, false);
      super.render(guigraphics, i, j, f);
   }
}
