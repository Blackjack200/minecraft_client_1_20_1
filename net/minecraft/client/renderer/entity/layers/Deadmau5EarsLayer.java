package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;

public class Deadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, AbstractClientPlayer abstractclientplayer, float f, float f1, float f2, float f3, float f4, float f5) {
      if ("deadmau5".equals(abstractclientplayer.getName().getString()) && abstractclientplayer.isSkinLoaded() && !abstractclientplayer.isInvisible()) {
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entitySolid(abstractclientplayer.getSkinTextureLocation()));
         int j = LivingEntityRenderer.getOverlayCoords(abstractclientplayer, 0.0F);

         for(int k = 0; k < 2; ++k) {
            float f6 = Mth.lerp(f2, abstractclientplayer.yRotO, abstractclientplayer.getYRot()) - Mth.lerp(f2, abstractclientplayer.yBodyRotO, abstractclientplayer.yBodyRot);
            float f7 = Mth.lerp(f2, abstractclientplayer.xRotO, abstractclientplayer.getXRot());
            posestack.pushPose();
            posestack.mulPose(Axis.YP.rotationDegrees(f6));
            posestack.mulPose(Axis.XP.rotationDegrees(f7));
            posestack.translate(0.375F * (float)(k * 2 - 1), 0.0F, 0.0F);
            posestack.translate(0.0F, -0.375F, 0.0F);
            posestack.mulPose(Axis.XP.rotationDegrees(-f7));
            posestack.mulPose(Axis.YP.rotationDegrees(-f6));
            float f8 = 1.3333334F;
            posestack.scale(1.3333334F, 1.3333334F, 1.3333334F);
            this.getParentModel().renderEars(posestack, vertexconsumer, i, j);
            posestack.popPose();
         }

      }
   }
}
