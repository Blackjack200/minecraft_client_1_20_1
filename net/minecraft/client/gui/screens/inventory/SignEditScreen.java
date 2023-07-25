package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class SignEditScreen extends AbstractSignEditScreen {
   public static final float MAGIC_SCALE_NUMBER = 62.500004F;
   public static final float MAGIC_TEXT_SCALE = 0.9765628F;
   private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
   @Nullable
   private SignRenderer.SignModel signModel;

   public SignEditScreen(SignBlockEntity signblockentity, boolean flag, boolean flag1) {
      super(signblockentity, flag, flag1);
   }

   protected void init() {
      super.init();
      this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
   }

   protected void offsetSign(GuiGraphics guigraphics, BlockState blockstate) {
      super.offsetSign(guigraphics, blockstate);
      boolean flag = blockstate.getBlock() instanceof StandingSignBlock;
      if (!flag) {
         guigraphics.pose().translate(0.0F, 35.0F, 0.0F);
      }

   }

   protected void renderSignBackground(GuiGraphics guigraphics, BlockState blockstate) {
      if (this.signModel != null) {
         boolean flag = blockstate.getBlock() instanceof StandingSignBlock;
         guigraphics.pose().translate(0.0F, 31.0F, 0.0F);
         guigraphics.pose().scale(62.500004F, 62.500004F, -62.500004F);
         Material material = Sheets.getSignMaterial(this.woodType);
         VertexConsumer vertexconsumer = material.buffer(guigraphics.bufferSource(), this.signModel::renderType);
         this.signModel.stick.visible = flag;
         this.signModel.root.render(guigraphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY);
      }
   }

   protected Vector3f getSignTextScale() {
      return TEXT_SCALE;
   }
}
