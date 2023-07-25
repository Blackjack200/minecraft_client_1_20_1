package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DisconnectedScreen extends Screen {
   private static final Component TO_SERVER_LIST = Component.translatable("gui.toMenu");
   private static final Component TO_TITLE = Component.translatable("gui.toTitle");
   private final Screen parent;
   private final Component reason;
   private final Component buttonText;
   private final GridLayout layout = new GridLayout();

   public DisconnectedScreen(Screen screen, Component component, Component component1) {
      this(screen, component, component1, TO_SERVER_LIST);
   }

   public DisconnectedScreen(Screen screen, Component component, Component component1, Component component2) {
      super(component);
      this.parent = screen;
      this.reason = component1;
      this.buttonText = component2;
   }

   protected void init() {
      this.layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
      GridLayout.RowHelper gridlayout_rowhelper = this.layout.createRowHelper(1);
      gridlayout_rowhelper.addChild(new StringWidget(this.title, this.font));
      gridlayout_rowhelper.addChild((new MultiLineTextWidget(this.reason, this.font)).setMaxWidth(this.width - 50).setCentered(true));
      Button button;
      if (this.minecraft.allowsMultiplayer()) {
         button = Button.builder(this.buttonText, (button3) -> this.minecraft.setScreen(this.parent)).build();
      } else {
         button = Button.builder(TO_TITLE, (button2) -> this.minecraft.setScreen(new TitleScreen())).build();
      }

      gridlayout_rowhelper.addChild(button);
      this.layout.arrangeElements();
      this.layout.visitWidgets(this::addRenderableWidget);
      this.repositionElements();
   }

   protected void repositionElements() {
      FrameLayout.centerInRectangle(this.layout, this.getRectangle());
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.title, this.reason);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
   }
}
