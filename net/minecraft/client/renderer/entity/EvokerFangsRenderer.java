package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
   private final EvokerFangsModel<EvokerFangs> model;

   public EvokerFangsRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new EvokerFangsModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.EVOKER_FANGS));
   }

   public void render(EvokerFangs evokerfangs, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      float f2 = evokerfangs.getAnimationProgress(f1);
      if (f2 != 0.0F) {
         float f3 = 2.0F;
         if (f2 > 0.9F) {
            f3 *= (1.0F - f2) / 0.1F;
         }

         posestack.pushPose();
         posestack.mulPose(Axis.YP.rotationDegrees(90.0F - evokerfangs.getYRot()));
         posestack.scale(-f3, -f3, f3);
         float f4 = 0.03125F;
         posestack.translate(0.0D, -0.626D, 0.0D);
         posestack.scale(0.5F, 0.5F, 0.5F);
         this.model.setupAnim(evokerfangs, f2, 0.0F, 0.0F, evokerfangs.getYRot(), evokerfangs.getXRot());
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
         this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         posestack.popPose();
         super.render(evokerfangs, f, f1, posestack, multibuffersource, i);
      }
   }

   public ResourceLocation getTextureLocation(EvokerFangs evokerfangs) {
      return TEXTURE_LOCATION;
   }
}
