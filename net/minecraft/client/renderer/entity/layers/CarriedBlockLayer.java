package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
   private final BlockRenderDispatcher blockRenderer;

   public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderlayerparent, BlockRenderDispatcher blockrenderdispatcher) {
      super(renderlayerparent);
      this.blockRenderer = blockrenderdispatcher;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, EnderMan enderman, float f, float f1, float f2, float f3, float f4, float f5) {
      BlockState blockstate = enderman.getCarriedBlock();
      if (blockstate != null) {
         posestack.pushPose();
         posestack.translate(0.0F, 0.6875F, -0.75F);
         posestack.mulPose(Axis.XP.rotationDegrees(20.0F));
         posestack.mulPose(Axis.YP.rotationDegrees(45.0F));
         posestack.translate(0.25F, 0.1875F, 0.25F);
         float f6 = 0.5F;
         posestack.scale(-0.5F, -0.5F, 0.5F);
         posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.blockRenderer.renderSingleBlock(blockstate, posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY);
         posestack.popPose();
      }
   }
}
