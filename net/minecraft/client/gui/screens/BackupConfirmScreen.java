package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BackupConfirmScreen extends Screen {
   private final Screen lastScreen;
   protected final BackupConfirmScreen.Listener listener;
   private final Component description;
   private final boolean promptForCacheErase;
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   protected int id;
   private Checkbox eraseCache;

   public BackupConfirmScreen(Screen screen, BackupConfirmScreen.Listener backupconfirmscreen_listener, Component component, Component component1, boolean flag) {
      super(component);
      this.lastScreen = screen;
      this.listener = backupconfirmscreen_listener;
      this.description = component1;
      this.promptForCacheErase = flag;
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
      int i = (this.message.getLineCount() + 1) * 9;
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinConfirmButton"), (button2) -> this.listener.proceed(true, this.eraseCache.selected())).bounds(this.width / 2 - 155, 100 + i, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinSkipButton"), (button1) -> this.listener.proceed(false, this.eraseCache.selected())).bounds(this.width / 2 - 155 + 160, 100 + i, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 155 + 80, 124 + i, 150, 20).build());
      this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, Component.translatable("selectWorld.backupEraseCache"), false);
      if (this.promptForCacheErase) {
         this.addRenderableWidget(this.eraseCache);
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
      this.message.renderCentered(guigraphics, this.width / 2, 70);
      super.render(guigraphics, i, j, f);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public interface Listener {
      void proceed(boolean flag, boolean flag1);
   }
}
