package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class HangingSignEditScreen extends AbstractSignEditScreen {
   public static final float MAGIC_BACKGROUND_SCALE = 4.5F;
   private static final Vector3f TEXT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
   private static final int TEXTURE_WIDTH = 16;
   private static final int TEXTURE_HEIGHT = 16;
   private final ResourceLocation texture = new ResourceLocation("textures/gui/hanging_signs/" + this.woodType.name() + ".png");

   public HangingSignEditScreen(SignBlockEntity signblockentity, boolean flag, boolean flag1) {
      super(signblockentity, flag, flag1, Component.translatable("hanging_sign.edit"));
   }

   protected void offsetSign(GuiGraphics guigraphics, BlockState blockstate) {
      guigraphics.pose().translate((float)this.width / 2.0F, 125.0F, 50.0F);
   }

   protected void renderSignBackground(GuiGraphics guigraphics, BlockState blockstate) {
      guigraphics.pose().translate(0.0F, -13.0F, 0.0F);
      guigraphics.pose().scale(4.5F, 4.5F, 1.0F);
      guigraphics.blit(this.texture, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
   }

   protected Vector3f getSignTextScale() {
      return TEXT_SCALE;
   }
}
