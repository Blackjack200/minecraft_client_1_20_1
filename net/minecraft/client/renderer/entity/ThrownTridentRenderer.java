package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;

public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
   public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
   private final TridentModel model;

   public ThrownTridentRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new TridentModel(entityrendererprovider_context.bakeLayer(ModelLayers.TRIDENT));
   }

   public void render(ThrownTrident throwntrident, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(f1, throwntrident.yRotO, throwntrident.getYRot()) - 90.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(f1, throwntrident.xRotO, throwntrident.getXRot()) + 90.0F));
      VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(multibuffersource, this.model.renderType(this.getTextureLocation(throwntrident)), false, throwntrident.isFoil());
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
      super.render(throwntrident, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(ThrownTrident throwntrident) {
      return TRIDENT_LOCATION;
   }
}
