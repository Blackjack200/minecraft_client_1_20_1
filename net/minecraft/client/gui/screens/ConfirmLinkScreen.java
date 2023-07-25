package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfirmLinkScreen extends ConfirmScreen {
   private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
   private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
   private final String url;
   private final boolean showWarning;

   public ConfirmLinkScreen(BooleanConsumer booleanconsumer, String s, boolean flag) {
      this(booleanconsumer, confirmMessage(flag), Component.literal(s), s, flag ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, flag);
   }

   public ConfirmLinkScreen(BooleanConsumer booleanconsumer, Component component, String s, boolean flag) {
      this(booleanconsumer, component, s, flag ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, flag);
   }

   public ConfirmLinkScreen(BooleanConsumer booleanconsumer, Component component, String s, Component component1, boolean flag) {
      this(booleanconsumer, component, confirmMessage(flag, s), s, component1, flag);
   }

   public ConfirmLinkScreen(BooleanConsumer booleanconsumer, Component component, Component component1, String s, Component component2, boolean flag) {
      super(booleanconsumer, component, component1);
      this.yesButton = (Component)(flag ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
      this.noButton = component2;
      this.showWarning = !flag;
      this.url = s;
   }

   protected static MutableComponent confirmMessage(boolean flag, String s) {
      return confirmMessage(flag).append(CommonComponents.SPACE).append(Component.literal(s));
   }

   protected static MutableComponent confirmMessage(boolean flag) {
      return Component.translatable(flag ? "chat.link.confirmTrusted" : "chat.link.confirm");
   }

   protected void addButtons(int i) {
      this.addRenderableWidget(Button.builder(this.yesButton, (button2) -> this.callback.accept(true)).bounds(this.width / 2 - 50 - 105, i, 100, 20).build());
      this.addRenderableWidget(Button.builder(COPY_BUTTON_TEXT, (button1) -> {
         this.copyToClipboard();
         this.callback.accept(false);
      }).bounds(this.width / 2 - 50, i, 100, 20).build());
      this.addRenderableWidget(Button.builder(this.noButton, (button) -> this.callback.accept(false)).bounds(this.width / 2 - 50 + 105, i, 100, 20).build());
   }

   public void copyToClipboard() {
      this.minecraft.keyboardHandler.setClipboard(this.url);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      super.render(guigraphics, i, j, f);
      if (this.showWarning) {
         guigraphics.drawCenteredString(this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
      }

   }

   public static void confirmLinkNow(String s, Screen screen, boolean flag) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.setScreen(new ConfirmLinkScreen((flag1) -> {
         if (flag1) {
            Util.getPlatform().openUri(s);
         }

         minecraft.setScreen(screen);
      }, s, flag));
   }

   public static Button.OnPress confirmLink(String s, Screen screen, boolean flag) {
      return (button) -> confirmLinkNow(s, screen, flag);
   }
}
