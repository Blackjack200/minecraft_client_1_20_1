package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
   private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
   private final LeashKnotModel<LeashFenceKnotEntity> model;

   public LeashKnotRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new LeashKnotModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.LEASH_KNOT));
   }

   public void render(LeashFenceKnotEntity leashfenceknotentity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(leashfenceknotentity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(KNOT_LOCATION));
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
      super.render(leashfenceknotentity, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(LeashFenceKnotEntity leashfenceknotentity) {
      return KNOT_LOCATION;
   }
}
