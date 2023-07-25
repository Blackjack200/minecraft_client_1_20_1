package net.minecraft.client.gui.screens.telemetry;

import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class TelemetryInfoScreen extends Screen {
   private static final int PADDING = 8;
   private static final Component TITLE = Component.translatable("telemetry_info.screen.title");
   private static final Component DESCRIPTION = Component.translatable("telemetry_info.screen.description").withStyle(ChatFormatting.GRAY);
   private static final Component BUTTON_GIVE_FEEDBACK = Component.translatable("telemetry_info.button.give_feedback");
   private static final Component BUTTON_SHOW_DATA = Component.translatable("telemetry_info.button.show_data");
   private final Screen lastScreen;
   private final Options options;
   private TelemetryEventWidget telemetryEventWidget;
   private double savedScroll;

   public TelemetryInfoScreen(Screen screen, Options options) {
      super(TITLE);
      this.lastScreen = screen;
      this.options = options;
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), DESCRIPTION);
   }

   protected void init() {
      FrameLayout framelayout = new FrameLayout();
      framelayout.defaultChildLayoutSetting().padding(8);
      framelayout.setMinHeight(this.height);
      GridLayout gridlayout = framelayout.addChild(new GridLayout(), framelayout.newChildLayoutSettings().align(0.5F, 0.0F));
      gridlayout.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
      GridLayout.RowHelper gridlayout_rowhelper = gridlayout.createRowHelper(1);
      gridlayout_rowhelper.addChild(new StringWidget(this.getTitle(), this.font));
      gridlayout_rowhelper.addChild((new MultiLineTextWidget(DESCRIPTION, this.font)).setMaxWidth(this.width - 16).setCentered(true));
      GridLayout gridlayout1 = this.twoButtonContainer(Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build(), Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build());
      gridlayout_rowhelper.addChild(gridlayout1);
      GridLayout gridlayout2 = this.twoButtonContainer(this.createTelemetryButton(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build());
      framelayout.addChild(gridlayout2, framelayout.newChildLayoutSettings().align(0.5F, 1.0F));
      framelayout.arrangeElements();
      this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, gridlayout2.getY() - (gridlayout1.getY() + gridlayout1.getHeight()) - 16, this.minecraft.font);
      this.telemetryEventWidget.setScrollAmount(this.savedScroll);
      this.telemetryEventWidget.setOnScrolledListener((d0) -> this.savedScroll = d0);
      this.setInitialFocus(this.telemetryEventWidget);
      gridlayout_rowhelper.addChild(this.telemetryEventWidget);
      framelayout.arrangeElements();
      FrameLayout.alignInRectangle(framelayout, 0, 0, this.width, this.height, 0.5F, 0.0F);
      framelayout.visitWidgets((guieventlistener) -> {
         AbstractWidget var10000 = this.addRenderableWidget(guieventlistener);
      });
   }

   private AbstractWidget createTelemetryButton() {
      AbstractWidget abstractwidget = this.options.telemetryOptInExtra().createButton(this.options, 0, 0, 150, (obool) -> this.telemetryEventWidget.onOptInChanged(obool));
      abstractwidget.active = this.minecraft.extraTelemetryAvailable();
      return abstractwidget;
   }

   private void openLastScreen(Button button) {
      this.minecraft.setScreen(this.lastScreen);
   }

   private void openFeedbackLink(Button button1) {
      this.minecraft.setScreen(new ConfirmLinkScreen((flag) -> {
         if (flag) {
            Util.getPlatform().openUri("https://aka.ms/javafeedback?ref=game");
         }

         this.minecraft.setScreen(this);
      }, "https://aka.ms/javafeedback?ref=game", true));
   }

   private void openDataFolder(Button button2) {
      Path path = this.minecraft.getTelemetryManager().getLogDirectory();
      Util.getPlatform().openUri(path.toUri());
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderDirtBackground(guigraphics);
      super.render(guigraphics, i, j, f);
   }

   private GridLayout twoButtonContainer(AbstractWidget abstractwidget, AbstractWidget abstractwidget1) {
      GridLayout gridlayout = new GridLayout();
      gridlayout.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
      gridlayout.addChild(abstractwidget, 0, 0);
      gridlayout.addChild(abstractwidget1, 0, 1);
      return gridlayout;
   }
}
