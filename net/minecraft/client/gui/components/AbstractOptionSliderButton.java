package net.minecraft.client.gui.components;

import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;

public abstract class AbstractOptionSliderButton extends AbstractSliderButton {
   protected final Options options;

   protected AbstractOptionSliderButton(Options options, int i, int j, int k, int l, double d0) {
      super(i, j, k, l, CommonComponents.EMPTY, d0);
      this.options = options;
   }
}
