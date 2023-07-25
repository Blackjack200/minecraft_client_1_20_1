package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
   private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
   private final ShulkerBulletModel<ShulkerBullet> model;

   public ShulkerBulletRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new ShulkerBulletModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.SHULKER_BULLET));
   }

   protected int getBlockLightLevel(ShulkerBullet shulkerbullet, BlockPos blockpos) {
      return 15;
   }

   public void render(ShulkerBullet shulkerbullet, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      float f2 = Mth.rotLerp(f1, shulkerbullet.yRotO, shulkerbullet.getYRot());
      float f3 = Mth.lerp(f1, shulkerbullet.xRotO, shulkerbullet.getXRot());
      float f4 = (float)shulkerbullet.tickCount + f1;
      posestack.translate(0.0F, 0.15F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f4 * 0.1F) * 180.0F));
      posestack.mulPose(Axis.XP.rotationDegrees(Mth.cos(f4 * 0.1F) * 180.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f4 * 0.15F) * 360.0F));
      posestack.scale(-0.5F, -0.5F, 0.5F);
      this.model.setupAnim(shulkerbullet, 0.0F, 0.0F, 0.0F, f2, f3);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.scale(1.5F, 1.5F, 1.5F);
      VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RENDER_TYPE);
      this.model.renderToBuffer(posestack, vertexconsumer1, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
      posestack.popPose();
      super.render(shulkerbullet, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(ShulkerBullet shulkerbullet) {
      return TEXTURE_LOCATION;
   }
}
