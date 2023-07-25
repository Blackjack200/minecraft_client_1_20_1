package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class AccessibilityOnboardingTextWidget extends MultiLineTextWidget {
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = 1426063360;
   private static final int PADDING = 3;
   private static final int BORDER = 1;

   public AccessibilityOnboardingTextWidget(Font font, Component component, int i) {
      super(component, font);
      this.setMaxWidth(i);
      this.setCentered(true);
      this.active = true;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, this.getMessage());
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      int k = this.getX() - 3;
      int l = this.getY() - 3;
      int i1 = this.getX() + this.getWidth() + 3;
      int j1 = this.getY() + this.getHeight() + 3;
      int k1 = this.isFocused() ? -1 : -6250336;
      guigraphics.fill(k - 1, l - 1, k, j1 + 1, k1);
      guigraphics.fill(i1, l - 1, i1 + 1, j1 + 1, k1);
      guigraphics.fill(k, l, i1, l - 1, k1);
      guigraphics.fill(k, j1, i1, j1 + 1, k1);
      guigraphics.fill(k, l, i1, j1, 1426063360);
      super.renderWidget(guigraphics, i, j, f);
   }

   public void playDownSound(SoundManager soundmanager) {
   }
}
