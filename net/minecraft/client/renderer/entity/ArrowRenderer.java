package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class ArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {
   public ArrowRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   public void render(T abstractarrow, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(f1, abstractarrow.yRotO, abstractarrow.getYRot()) - 90.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(f1, abstractarrow.xRotO, abstractarrow.getXRot())));
      int j = 0;
      float f2 = 0.0F;
      float f3 = 0.5F;
      float f4 = 0.0F;
      float f5 = 0.15625F;
      float f6 = 0.0F;
      float f7 = 0.15625F;
      float f8 = 0.15625F;
      float f9 = 0.3125F;
      float f10 = 0.05625F;
      float f11 = (float)abstractarrow.shakeTime - f1;
      if (f11 > 0.0F) {
         float f12 = -Mth.sin(f11 * 3.0F) * f11;
         posestack.mulPose(Axis.ZP.rotationDegrees(f12));
      }

      posestack.mulPose(Axis.XP.rotationDegrees(45.0F));
      posestack.scale(0.05625F, 0.05625F, 0.05625F);
      posestack.translate(-4.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutout(this.getTextureLocation(abstractarrow)));
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, i);

      for(int k = 0; k < 4; ++k) {
         posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
         this.vertex(matrix4f, matrix3f, vertexconsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, vertexconsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, vertexconsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, vertexconsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, i);
      }

      posestack.popPose();
      super.render(abstractarrow, f, f1, posestack, multibuffersource, i);
   }

   public void vertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexconsumer, int i, int j, int k, float f, float f1, int l, int i1, int j1, int k1) {
      vertexconsumer.vertex(matrix4f, (float)i, (float)j, (float)k).color(255, 255, 255, 255).uv(f, f1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k1).normal(matrix3f, (float)l, (float)j1, (float)i1).endVertex();
   }
}
