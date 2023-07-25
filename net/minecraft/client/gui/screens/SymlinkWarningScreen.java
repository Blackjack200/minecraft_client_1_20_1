package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SymlinkWarningScreen extends Screen {
   private static final Component TITLE = Component.translatable("symlink_warning.title").withStyle(ChatFormatting.BOLD);
   private static final Component MESSAGE_TEXT = Component.translatable("symlink_warning.message", "https://aka.ms/MinecraftSymLinks");
   @Nullable
   private final Screen callbackScreen;
   private final GridLayout layout = (new GridLayout()).rowSpacing(10);

   public SymlinkWarningScreen(@Nullable Screen screen) {
      super(TITLE);
      this.callbackScreen = screen;
   }

   protected void init() {
      super.init();
      this.layout.defaultCellSetting().alignHorizontallyCenter();
      GridLayout.RowHelper gridlayout_rowhelper = this.layout.createRowHelper(1);
      gridlayout_rowhelper.addChild(new StringWidget(this.title, this.font));
      gridlayout_rowhelper.addChild((new MultiLineTextWidget(MESSAGE_TEXT, this.font)).setMaxWidth(this.width - 50).setCentered(true));
      int i = 120;
      GridLayout gridlayout = (new GridLayout()).columnSpacing(5);
      GridLayout.RowHelper gridlayout_rowhelper1 = gridlayout.createRowHelper(3);
      gridlayout_rowhelper1.addChild(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, (button2) -> Util.getPlatform().openUri("https://aka.ms/MinecraftSymLinks")).size(120, 20).build());
      gridlayout_rowhelper1.addChild(Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, (button1) -> this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftSymLinks")).size(120, 20).build());
      gridlayout_rowhelper1.addChild(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose()).size(120, 20).build());
      gridlayout_rowhelper.addChild(gridlayout);
      this.repositionElements();
      this.layout.visitWidgets(this::addRenderableWidget);
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
      FrameLayout.centerInRectangle(this.layout, this.getRectangle());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE_TEXT);
   }

   public void onClose() {
      this.minecraft.setScreen(this.callbackScreen);
   }
}
