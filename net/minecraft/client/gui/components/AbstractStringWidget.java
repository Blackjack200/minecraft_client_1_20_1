package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class AbstractStringWidget extends AbstractWidget {
   private final Font font;
   private int color = 16777215;

   public AbstractStringWidget(int i, int j, int k, int l, Component component, Font font) {
      super(i, j, k, l, component);
      this.font = font;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
   }

   public AbstractStringWidget setColor(int i) {
      this.color = i;
      return this;
   }

   protected final Font getFont() {
      return this.font;
   }

   protected final int getColor() {
      return this.color;
   }
}
