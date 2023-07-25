package net.minecraft.client.gui.components;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Button extends AbstractButton {
   public static final int SMALL_WIDTH = 120;
   public static final int DEFAULT_WIDTH = 150;
   public static final int DEFAULT_HEIGHT = 20;
   protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> supplier.get();
   protected final Button.OnPress onPress;
   protected final Button.CreateNarration createNarration;

   public static Button.Builder builder(Component component, Button.OnPress button_onpress) {
      return new Button.Builder(component, button_onpress);
   }

   protected Button(int i, int j, int k, int l, Component component, Button.OnPress button_onpress, Button.CreateNarration button_createnarration) {
      super(i, j, k, l, component);
      this.onPress = button_onpress;
      this.createNarration = button_createnarration;
   }

   public void onPress() {
      this.onPress.onPress(this);
   }

   protected MutableComponent createNarrationMessage() {
      return this.createNarration.createNarrationMessage(() -> super.createNarrationMessage());
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      this.defaultButtonNarrationText(narrationelementoutput);
   }

   public static class Builder {
      private final Component message;
      private final Button.OnPress onPress;
      @Nullable
      private Tooltip tooltip;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

      public Builder(Component component, Button.OnPress button_onpress) {
         this.message = component;
         this.onPress = button_onpress;
      }

      public Button.Builder pos(int i, int j) {
         this.x = i;
         this.y = j;
         return this;
      }

      public Button.Builder width(int i) {
         this.width = i;
         return this;
      }

      public Button.Builder size(int i, int j) {
         this.width = i;
         this.height = j;
         return this;
      }

      public Button.Builder bounds(int i, int j, int k, int l) {
         return this.pos(i, j).size(k, l);
      }

      public Button.Builder tooltip(@Nullable Tooltip tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public Button.Builder createNarration(Button.CreateNarration button_createnarration) {
         this.createNarration = button_createnarration;
         return this;
      }

      public Button build() {
         Button button = new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration);
         button.setTooltip(this.tooltip);
         return button;
      }
   }

   public interface CreateNarration {
      MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier);
   }

   public interface OnPress {
      void onPress(Button button);
   }
}
