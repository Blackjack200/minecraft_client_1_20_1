package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GenericWaitingScreen extends Screen {
   private static final int TITLE_Y = 80;
   private static final int MESSAGE_Y = 120;
   private static final int MESSAGE_MAX_WIDTH = 360;
   @Nullable
   private final Component messageText;
   private final Component buttonLabel;
   private final Runnable buttonCallback;
   @Nullable
   private MultiLineLabel message;
   private Button button;
   private int disableButtonTicks;

   public static GenericWaitingScreen createWaiting(Component component, Component component1, Runnable runnable) {
      return new GenericWaitingScreen(component, (Component)null, component1, runnable, 0);
   }

   public static GenericWaitingScreen createCompleted(Component component, Component component1, Component component2, Runnable runnable) {
      return new GenericWaitingScreen(component, component1, component2, runnable, 20);
   }

   protected GenericWaitingScreen(Component component, @Nullable Component component1, Component component2, Runnable runnable, int i) {
      super(component);
      this.messageText = component1;
      this.buttonLabel = component2;
      this.buttonCallback = runnable;
      this.disableButtonTicks = i;
   }

   protected void init() {
      super.init();
      if (this.messageText != null) {
         this.message = MultiLineLabel.create(this.font, this.messageText, 360);
      }

      int i = 150;
      int j = 20;
      int k = this.message != null ? this.message.getLineCount() : 1;
      int l = Math.max(k, 5) * 9;
      int i1 = Math.min(120 + l, this.height - 40);
      this.button = this.addRenderableWidget(Button.builder(this.buttonLabel, (button) -> this.onClose()).bounds((this.width - 150) / 2, i1, 150, 20).build());
   }

   public void tick() {
      if (this.disableButtonTicks > 0) {
         --this.disableButtonTicks;
      }

      this.button.active = this.disableButtonTicks == 0;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 80, 16777215);
      if (this.message == null) {
         String s = LoadingDotsText.get(Util.getMillis());
         guigraphics.drawCenteredString(this.font, s, this.width / 2, 120, 10526880);
      } else {
         this.message.renderCentered(guigraphics, this.width / 2, 120);
      }

      super.render(guigraphics, i, j, f);
   }

   public boolean shouldCloseOnEsc() {
      return this.message != null && this.button.active;
   }

   public void onClose() {
      this.buttonCallback.run();
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.title, this.messageText != null ? this.messageText : CommonComponents.EMPTY);
   }
}
