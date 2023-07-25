package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
   private final BlockRenderDispatcher blockRenderer;

   public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderlayerparent, BlockRenderDispatcher blockrenderdispatcher) {
      super(renderlayerparent);
      this.blockRenderer = blockrenderdispatcher;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, IronGolem irongolem, float f, float f1, float f2, float f3, float f4, float f5) {
      if (irongolem.getOfferFlowerTick() != 0) {
         posestack.pushPose();
         ModelPart modelpart = this.getParentModel().getFlowerHoldingArm();
         modelpart.translateAndRotate(posestack);
         posestack.translate(-1.1875F, 1.0625F, -0.9375F);
         posestack.translate(0.5F, 0.5F, 0.5F);
         float f6 = 0.5F;
         posestack.scale(0.5F, 0.5F, 0.5F);
         posestack.mulPose(Axis.XP.rotationDegrees(-90.0F));
         posestack.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY);
         posestack.popPose();
      }
   }
}
