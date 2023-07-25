package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsSettingsScreen extends RealmsScreen {
   private static final int COMPONENT_WIDTH = 212;
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
   private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
   private final RealmsConfigureWorldScreen configureWorldScreen;
   private final RealmsServer serverData;
   private Button doneButton;
   private EditBox descEdit;
   private EditBox nameEdit;

   public RealmsSettingsScreen(RealmsConfigureWorldScreen realmsconfigureworldscreen, RealmsServer realmsserver) {
      super(Component.translatable("mco.configure.world.settings.title"));
      this.configureWorldScreen = realmsconfigureworldscreen;
      this.serverData = realmsserver;
   }

   public void tick() {
      this.nameEdit.tick();
      this.descEdit.tick();
      this.doneButton.active = !this.nameEdit.getValue().trim().isEmpty();
   }

   public void init() {
      int i = this.width / 2 - 106;
      this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.done"), (button3) -> this.save()).bounds(i - 2, row(12), 106, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button2) -> this.minecraft.setScreen(this.configureWorldScreen)).bounds(this.width / 2 + 2, row(12), 106, 20).build());
      String s = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
      Button button = Button.builder(Component.translatable(s), (button1) -> {
         if (this.serverData.state == RealmsServer.State.OPEN) {
            Component component = Component.translatable("mco.configure.world.close.question.line1");
            Component component1 = Component.translatable("mco.configure.world.close.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen((flag) -> {
               if (flag) {
                  this.configureWorldScreen.closeTheWorld(this);
               } else {
                  this.minecraft.setScreen(this);
               }

            }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
         } else {
            this.configureWorldScreen.openTheWorld(false, this);
         }

      }).bounds(this.width / 2 - 53, row(0), 106, 20).build();
      this.addRenderableWidget(button);
      this.nameEdit = new EditBox(this.minecraft.font, i, row(4), 212, 20, (EditBox)null, Component.translatable("mco.configure.world.name"));
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setValue(this.serverData.getName());
      this.addWidget(this.nameEdit);
      this.magicalSpecialHackyFocus(this.nameEdit);
      this.descEdit = new EditBox(this.minecraft.font, i, row(8), 212, 20, (EditBox)null, Component.translatable("mco.configure.world.description"));
      this.descEdit.setMaxLength(32);
      this.descEdit.setValue(this.serverData.getDescription());
      this.addWidget(this.descEdit);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.configureWorldScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 106, row(3), 10526880, false);
      guigraphics.drawString(this.font, DESCRIPTION_LABEL, this.width / 2 - 106, row(7), 10526880, false);
      this.nameEdit.render(guigraphics, i, j, f);
      this.descEdit.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }

   public void save() {
      this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
   }
}
