package net.minecraft.client.gui.screens.multiplayer;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class WarningScreen extends Screen {
   private final Component content;
   @Nullable
   private final Component check;
   private final Component narration;
   @Nullable
   protected Checkbox stopShowing;
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   protected WarningScreen(Component component, Component component1, Component component2) {
      this(component, component1, (Component)null, component2);
   }

   protected WarningScreen(Component component, Component component1, @Nullable Component component2, Component component3) {
      super(component);
      this.content = component1;
      this.check = component2;
      this.narration = component3;
   }

   protected abstract void initButtons(int i);

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.content, this.width - 100);
      int i = (this.message.getLineCount() + 1) * this.getLineHeight();
      if (this.check != null) {
         int j = this.font.width(this.check);
         this.stopShowing = new Checkbox(this.width / 2 - j / 2 - 8, 76 + i, j + 24, 20, this.check, false);
         this.addRenderableWidget(this.stopShowing);
      }

      this.initButtons(i);
   }

   public Component getNarrationMessage() {
      return this.narration;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.renderTitle(guigraphics);
      int k = this.width / 2 - this.message.getWidth() / 2;
      this.message.renderLeftAligned(guigraphics, k, 70, this.getLineHeight(), 16777215);
      super.render(guigraphics, i, j, f);
   }

   protected void renderTitle(GuiGraphics guigraphics) {
      guigraphics.drawString(this.font, this.title, 25, 30, 16777215);
   }

   protected int getLineHeight() {
      return 9 * 2;
   }
}
