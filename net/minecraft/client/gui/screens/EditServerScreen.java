package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditServerScreen extends Screen {
   private static final Component NAME_LABEL = Component.translatable("addServer.enterName");
   private static final Component IP_LABEL = Component.translatable("addServer.enterIp");
   private Button addButton;
   private final BooleanConsumer callback;
   private final ServerData serverData;
   private EditBox ipEdit;
   private EditBox nameEdit;
   private final Screen lastScreen;

   public EditServerScreen(Screen screen, BooleanConsumer booleanconsumer, ServerData serverdata) {
      super(Component.translatable("addServer.title"));
      this.lastScreen = screen;
      this.callback = booleanconsumer;
      this.serverData = serverdata;
   }

   public void tick() {
      this.nameEdit.tick();
      this.ipEdit.tick();
   }

   protected void init() {
      this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, Component.translatable("addServer.enterName"));
      this.nameEdit.setValue(this.serverData.name);
      this.nameEdit.setResponder((s1) -> this.updateAddButtonStatus());
      this.addWidget(this.nameEdit);
      this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, Component.translatable("addServer.enterIp"));
      this.ipEdit.setMaxLength(128);
      this.ipEdit.setValue(this.serverData.ip);
      this.ipEdit.setResponder((s) -> this.updateAddButtonStatus());
      this.addWidget(this.ipEdit);
      this.addRenderableWidget(CycleButton.builder(ServerData.ServerPackStatus::getName).withValues(ServerData.ServerPackStatus.values()).withInitialValue(this.serverData.getResourcePackStatus()).create(this.width / 2 - 100, this.height / 4 + 72, 200, 20, Component.translatable("addServer.resourcePack"), (cyclebutton, serverdata_serverpackstatus) -> this.serverData.setResourcePackStatus(serverdata_serverpackstatus)));
      this.addButton = this.addRenderableWidget(Button.builder(Component.translatable("addServer.add"), (button1) -> this.onAdd()).bounds(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.callback.accept(false)).bounds(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20).build());
      this.setInitialFocus(this.nameEdit);
      this.updateAddButtonStatus();
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.ipEdit.getValue();
      String s1 = this.nameEdit.getValue();
      this.init(minecraft, i, j);
      this.ipEdit.setValue(s);
      this.nameEdit.setValue(s1);
   }

   private void onAdd() {
      this.serverData.name = this.nameEdit.getValue();
      this.serverData.ip = this.ipEdit.getValue();
      this.callback.accept(true);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   private void updateAddButtonStatus() {
      this.addButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue()) && !this.nameEdit.getValue().isEmpty();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, 53, 10526880);
      guigraphics.drawString(this.font, IP_LABEL, this.width / 2 - 100, 94, 10526880);
      this.nameEdit.render(guigraphics, i, j, f);
      this.ipEdit.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }
}
