package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;

public class PopupScreen extends Screen {
   private static final int BUTTON_PADDING = 20;
   private static final int BUTTON_MARGIN = 5;
   private static final int BUTTON_HEIGHT = 20;
   private final Component narrationMessage;
   private final FormattedText message;
   private final ImmutableList<PopupScreen.ButtonOption> buttonOptions;
   private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
   private int contentTop;
   private int buttonWidth;

   protected PopupScreen(Component component, List<Component> list, ImmutableList<PopupScreen.ButtonOption> immutablelist) {
      super(component);
      this.message = FormattedText.composite(list);
      this.narrationMessage = CommonComponents.joinForNarration(component, ComponentUtils.formatList(list, CommonComponents.EMPTY));
      this.buttonOptions = immutablelist;
   }

   public Component getNarrationMessage() {
      return this.narrationMessage;
   }

   public void init() {
      for(PopupScreen.ButtonOption popupscreen_buttonoption : this.buttonOptions) {
         this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(popupscreen_buttonoption.message) + 20);
      }

      int i = 5 + this.buttonWidth + 5;
      int j = i * this.buttonOptions.size();
      this.messageLines = MultiLineLabel.create(this.font, this.message, j);
      int k = this.messageLines.getLineCount() * 9;
      this.contentTop = (int)((double)this.height / 2.0D - (double)k / 2.0D);
      int l = this.contentTop + k + 9 * 2;
      int i1 = (int)((double)this.width / 2.0D - (double)j / 2.0D);

      for(PopupScreen.ButtonOption popupscreen_buttonoption1 : this.buttonOptions) {
         this.addRenderableWidget(Button.builder(popupscreen_buttonoption1.message, popupscreen_buttonoption1.onPress).bounds(i1, l, this.buttonWidth, 20).build());
         i1 += i;
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderDirtBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
      this.messageLines.renderCentered(guigraphics, this.width / 2, this.contentTop);
      super.render(guigraphics, i, j, f);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public static final class ButtonOption {
      final Component message;
      final Button.OnPress onPress;

      public ButtonOption(Component component, Button.OnPress button_onpress) {
         this.message = component;
         this.onPress = button_onpress;
      }
   }
}
