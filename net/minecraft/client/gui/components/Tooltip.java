package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class Tooltip implements NarrationSupplier {
   private static final int MAX_WIDTH = 170;
   private final Component message;
   @Nullable
   private List<FormattedCharSequence> cachedTooltip;
   @Nullable
   private final Component narration;

   private Tooltip(Component component, @Nullable Component component1) {
      this.message = component;
      this.narration = component1;
   }

   public static Tooltip create(Component component, @Nullable Component component1) {
      return new Tooltip(component, component1);
   }

   public static Tooltip create(Component component) {
      return new Tooltip(component, component);
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      if (this.narration != null) {
         narrationelementoutput.add(NarratedElementType.HINT, this.narration);
      }

   }

   public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
      if (this.cachedTooltip == null) {
         this.cachedTooltip = splitTooltip(minecraft, this.message);
      }

      return this.cachedTooltip;
   }

   public static List<FormattedCharSequence> splitTooltip(Minecraft minecraft, Component component) {
      return minecraft.font.split(component, 170);
   }
}
