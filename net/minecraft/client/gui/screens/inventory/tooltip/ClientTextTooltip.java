package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public class ClientTextTooltip implements ClientTooltipComponent {
   private final FormattedCharSequence text;

   public ClientTextTooltip(FormattedCharSequence formattedcharsequence) {
      this.text = formattedcharsequence;
   }

   public int getWidth(Font font) {
      return font.width(this.text);
   }

   public int getHeight() {
      return 10;
   }

   public void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource multibuffersource_buffersource) {
      font.drawInBatch(this.text, (float)i, (float)j, -1, true, matrix4f, multibuffersource_buffersource, Font.DisplayMode.NORMAL, 0, 15728880);
   }
}
