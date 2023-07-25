package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class PlainTextButton extends Button {
   private final Font font;
   private final Component message;
   private final Component underlinedMessage;

   public PlainTextButton(int i, int j, int k, int l, Component component, Button.OnPress button_onpress, Font font) {
      super(i, j, k, l, component, button_onpress, DEFAULT_NARRATION);
      this.font = font;
      this.message = component;
      this.underlinedMessage = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withUnderlined(true));
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      Component component = this.isHoveredOrFocused() ? this.underlinedMessage : this.message;
      guigraphics.drawString(this.font, component, this.getX(), this.getY(), 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
   }
}
