package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Matrix4f;

public interface ClientTooltipComponent {
   static ClientTooltipComponent create(FormattedCharSequence formattedcharsequence) {
      return new ClientTextTooltip(formattedcharsequence);
   }

   static ClientTooltipComponent create(TooltipComponent tooltipcomponent) {
      if (tooltipcomponent instanceof BundleTooltip) {
         return new ClientBundleTooltip((BundleTooltip)tooltipcomponent);
      } else {
         throw new IllegalArgumentException("Unknown TooltipComponent");
      }
   }

   int getHeight();

   int getWidth(Font font);

   default void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource multibuffersource_buffersource) {
   }

   default void renderImage(Font font, int i, int j, GuiGraphics guigraphics) {
   }
}
