package net.minecraft.client.gui.screens.controls;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ControlsScreen extends OptionsSubScreen {
   private static final int ROW_SPACING = 24;

   public ControlsScreen(Screen screen, Options options) {
      super(screen, options, Component.translatable("controls.title"));
   }

   protected void init() {
      super.init();
      int i = this.width / 2 - 155;
      int j = i + 160;
      int k = this.height / 6 - 12;
      this.addRenderableWidget(Button.builder(Component.translatable("options.mouse_settings"), (button2) -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))).bounds(i, k, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("controls.keybinds"), (button1) -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))).bounds(j, k, 150, 20).build());
      k += 24;
      this.addRenderableWidget(this.options.toggleCrouch().createButton(this.options, i, k, 150));
      this.addRenderableWidget(this.options.toggleSprint().createButton(this.options, j, k, 150));
      k += 24;
      this.addRenderableWidget(this.options.autoJump().createButton(this.options, i, k, 150));
      this.addRenderableWidget(this.options.operatorItemsTab().createButton(this.options, j, k, 150));
      k += 24;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, k, 200, 20).build());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
      super.render(guigraphics, i, j, f);
   }
}
