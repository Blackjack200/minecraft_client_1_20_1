package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
   private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

   public BeeStingerLayer(LivingEntityRenderer<T, M> livingentityrenderer) {
      super(livingentityrenderer);
   }

   protected int numStuck(T livingentity) {
      return livingentity.getStingerCount();
   }

   protected void renderStuckItem(PoseStack posestack, MultiBufferSource multibuffersource, int i, Entity entity, float f, float f1, float f2, float f3) {
      float f4 = Mth.sqrt(f * f + f2 * f2);
      float f5 = (float)(Math.atan2((double)f, (double)f2) * (double)(180F / (float)Math.PI));
      float f6 = (float)(Math.atan2((double)f1, (double)f4) * (double)(180F / (float)Math.PI));
      posestack.translate(0.0F, 0.0F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(f5 - 90.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(f6));
      float f7 = 0.0F;
      float f8 = 0.125F;
      float f9 = 0.0F;
      float f10 = 0.0625F;
      float f11 = 0.03125F;
      posestack.mulPose(Axis.XP.rotationDegrees(45.0F));
      posestack.scale(0.03125F, 0.03125F, 0.03125F);
      posestack.translate(2.5F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

      for(int j = 0; j < 4; ++j) {
         posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
         PoseStack.Pose posestack_pose = posestack.last();
         Matrix4f matrix4f = posestack_pose.pose();
         Matrix3f matrix3f = posestack_pose.normal();
         vertex(vertexconsumer, matrix4f, matrix3f, -4.5F, -1, 0.0F, 0.0F, i);
         vertex(vertexconsumer, matrix4f, matrix3f, 4.5F, -1, 0.125F, 0.0F, i);
         vertex(vertexconsumer, matrix4f, matrix3f, 4.5F, 1, 0.125F, 0.0625F, i);
         vertex(vertexconsumer, matrix4f, matrix3f, -4.5F, 1, 0.0F, 0.0625F, i);
      }

   }

   private static void vertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, int i, float f1, float f2, int j) {
      vertexconsumer.vertex(matrix4f, f, (float)i, 0.0F).color(255, 255, 255, 255).uv(f1, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }
}
