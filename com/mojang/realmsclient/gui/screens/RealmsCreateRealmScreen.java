package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsCreateRealmScreen extends RealmsScreen {
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
   private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
   private final RealmsServer server;
   private final RealmsMainScreen lastScreen;
   private EditBox nameBox;
   private EditBox descriptionBox;
   private Button createButton;

   public RealmsCreateRealmScreen(RealmsServer realmsserver, RealmsMainScreen realmsmainscreen) {
      super(Component.translatable("mco.selectServer.create"));
      this.server = realmsserver;
      this.lastScreen = realmsmainscreen;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.tick();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.tick();
      }

   }

   public void init() {
      this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.create.world"), (button1) -> this.createWorld()).bounds(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20).build());
      this.createButton.active = false;
      this.nameBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, (EditBox)null, Component.translatable("mco.configure.world.name"));
      this.addWidget(this.nameBox);
      this.setInitialFocus(this.nameBox);
      this.descriptionBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 115, 200, 20, (EditBox)null, Component.translatable("mco.configure.world.description"));
      this.addWidget(this.descriptionBox);
   }

   public boolean charTyped(char c0, int i) {
      boolean flag = super.charTyped(c0, i);
      this.createButton.active = this.valid();
      return flag;
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         boolean flag = super.keyPressed(i, j, k);
         this.createButton.active = this.valid();
         return flag;
      }
   }

   private void createWorld() {
      if (this.valid()) {
         RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this.lastScreen, this.server, Component.translatable("mco.selectServer.create"), Component.translatable("mco.create.world.subtitle"), 10526880, Component.translatable("mco.create.world.skip"), () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen.newScreen())), () -> this.minecraft.setScreen(this.lastScreen.newScreen()));
         realmsresetworldscreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsresetworldscreen)));
      }

   }

   private boolean valid() {
      return !this.nameBox.getValue().trim().isEmpty();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 11, 16777215);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, 52, 10526880, false);
      guigraphics.drawString(this.font, DESCRIPTION_LABEL, this.width / 2 - 100, 102, 10526880, false);
      if (this.nameBox != null) {
         this.nameBox.render(guigraphics, i, j, f);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render(guigraphics, i, j, f);
      }

      super.render(guigraphics, i, j, f);
   }
}
