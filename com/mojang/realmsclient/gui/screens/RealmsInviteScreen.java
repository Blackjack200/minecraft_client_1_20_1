package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import org.slf4j.Logger;

public class RealmsInviteScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name").withStyle((style) -> style.withColor(-6250336));
   private static final Component INVITING_PLAYER_TEXT = Component.translatable("mco.configure.world.players.inviting").withStyle((style) -> style.withColor(-6250336));
   private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error").withStyle((style) -> style.withColor(-65536));
   private EditBox profileName;
   private Button inviteButton;
   private final RealmsServer serverData;
   private final RealmsConfigureWorldScreen configureScreen;
   private final Screen lastScreen;
   @Nullable
   private Component message;

   public RealmsInviteScreen(RealmsConfigureWorldScreen realmsconfigureworldscreen, Screen screen, RealmsServer realmsserver) {
      super(GameNarrator.NO_TITLE);
      this.configureScreen = realmsconfigureworldscreen;
      this.lastScreen = screen;
      this.serverData = realmsserver;
   }

   public void tick() {
      this.profileName.tick();
   }

   public void init() {
      this.profileName = new EditBox(this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, (EditBox)null, Component.translatable("mco.configure.world.invite.profile.name"));
      this.addWidget(this.profileName);
      this.setInitialFocus(this.profileName);
      this.inviteButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.invite"), (button1) -> this.onInvite()).bounds(this.width / 2 - 100, row(10), 200, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, row(12), 200, 20).build());
   }

   private void onInvite() {
      if (Util.isBlank(this.profileName.getValue())) {
         this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
      } else {
         long i = this.serverData.id;
         String s = this.profileName.getValue().trim();
         this.inviteButton.active = false;
         this.profileName.setEditable(false);
         this.showMessage(INVITING_PLAYER_TEXT);
         CompletableFuture.supplyAsync(() -> {
            try {
               return RealmsClient.create().invite(i, s);
            } catch (Exception var4) {
               LOGGER.error("Couldn't invite user");
               return null;
            }
         }, Util.ioPool()).thenAcceptAsync((realmsserver) -> {
            if (realmsserver != null) {
               this.serverData.players = realmsserver.players;
               this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
            } else {
               this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
            }

            this.profileName.setEditable(true);
            this.inviteButton.active = true;
         }, this.screenExecutor);
      }
   }

   private void showMessage(Component component) {
      this.message = component;
      this.minecraft.getNarrator().sayNow(component);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, row(1), -1, false);
      if (this.message != null) {
         guigraphics.drawCenteredString(this.font, this.message, this.width / 2, row(5), -1);
      }

      this.profileName.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }
}
