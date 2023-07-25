package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class GuardianRenderer extends MobRenderer<Guardian, GuardianModel> {
   private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
   private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
   private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

   public GuardianRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this(entityrendererprovider_context, 0.5F, ModelLayers.GUARDIAN);
   }

   protected GuardianRenderer(EntityRendererProvider.Context entityrendererprovider_context, float f, ModelLayerLocation modellayerlocation) {
      super(entityrendererprovider_context, new GuardianModel(entityrendererprovider_context.bakeLayer(modellayerlocation)), f);
   }

   public boolean shouldRender(Guardian guardian, Frustum frustum, double d0, double d1, double d2) {
      if (super.shouldRender(guardian, frustum, d0, d1, d2)) {
         return true;
      } else {
         if (guardian.hasActiveAttackTarget()) {
            LivingEntity livingentity = guardian.getActiveAttackTarget();
            if (livingentity != null) {
               Vec3 vec3 = this.getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5D, 1.0F);
               Vec3 vec31 = this.getPosition(guardian, (double)guardian.getEyeHeight(), 1.0F);
               return frustum.isVisible(new AABB(vec31.x, vec31.y, vec31.z, vec3.x, vec3.y, vec3.z));
            }
         }

         return false;
      }
   }

   private Vec3 getPosition(LivingEntity livingentity, double d0, float f) {
      double d1 = Mth.lerp((double)f, livingentity.xOld, livingentity.getX());
      double d2 = Mth.lerp((double)f, livingentity.yOld, livingentity.getY()) + d0;
      double d3 = Mth.lerp((double)f, livingentity.zOld, livingentity.getZ());
      return new Vec3(d1, d2, d3);
   }

   public void render(Guardian guardian, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      super.render(guardian, f, f1, posestack, multibuffersource, i);
      LivingEntity livingentity = guardian.getActiveAttackTarget();
      if (livingentity != null) {
         float f2 = guardian.getAttackAnimationScale(f1);
         float f3 = guardian.getClientSideAttackTime() + f1;
         float f4 = f3 * 0.5F % 1.0F;
         float f5 = guardian.getEyeHeight();
         posestack.pushPose();
         posestack.translate(0.0F, f5, 0.0F);
         Vec3 vec3 = this.getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5D, f1);
         Vec3 vec31 = this.getPosition(guardian, (double)f5, f1);
         Vec3 vec32 = vec3.subtract(vec31);
         float f6 = (float)(vec32.length() + 1.0D);
         vec32 = vec32.normalize();
         float f7 = (float)Math.acos(vec32.y);
         float f8 = (float)Math.atan2(vec32.z, vec32.x);
         posestack.mulPose(Axis.YP.rotationDegrees((((float)Math.PI / 2F) - f8) * (180F / (float)Math.PI)));
         posestack.mulPose(Axis.XP.rotationDegrees(f7 * (180F / (float)Math.PI)));
         int j = 1;
         float f9 = f3 * 0.05F * -1.5F;
         float f10 = f2 * f2;
         int k = 64 + (int)(f10 * 191.0F);
         int l = 32 + (int)(f10 * 191.0F);
         int i1 = 128 - (int)(f10 * 64.0F);
         float f11 = 0.2F;
         float f12 = 0.282F;
         float f13 = Mth.cos(f9 + 2.3561945F) * 0.282F;
         float f14 = Mth.sin(f9 + 2.3561945F) * 0.282F;
         float f15 = Mth.cos(f9 + ((float)Math.PI / 4F)) * 0.282F;
         float f16 = Mth.sin(f9 + ((float)Math.PI / 4F)) * 0.282F;
         float f17 = Mth.cos(f9 + 3.926991F) * 0.282F;
         float f18 = Mth.sin(f9 + 3.926991F) * 0.282F;
         float f19 = Mth.cos(f9 + 5.4977875F) * 0.282F;
         float f20 = Mth.sin(f9 + 5.4977875F) * 0.282F;
         float f21 = Mth.cos(f9 + (float)Math.PI) * 0.2F;
         float f22 = Mth.sin(f9 + (float)Math.PI) * 0.2F;
         float f23 = Mth.cos(f9 + 0.0F) * 0.2F;
         float f24 = Mth.sin(f9 + 0.0F) * 0.2F;
         float f25 = Mth.cos(f9 + ((float)Math.PI / 2F)) * 0.2F;
         float f26 = Mth.sin(f9 + ((float)Math.PI / 2F)) * 0.2F;
         float f27 = Mth.cos(f9 + ((float)Math.PI * 1.5F)) * 0.2F;
         float f28 = Mth.sin(f9 + ((float)Math.PI * 1.5F)) * 0.2F;
         float f30 = 0.0F;
         float f31 = 0.4999F;
         float f32 = -1.0F + f4;
         float f33 = f6 * 2.5F + f32;
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(BEAM_RENDER_TYPE);
         PoseStack.Pose posestack_pose = posestack.last();
         Matrix4f matrix4f = posestack_pose.pose();
         Matrix3f matrix3f = posestack_pose.normal();
         vertex(vertexconsumer, matrix4f, matrix3f, f21, f6, f22, k, l, i1, 0.4999F, f33);
         vertex(vertexconsumer, matrix4f, matrix3f, f21, 0.0F, f22, k, l, i1, 0.4999F, f32);
         vertex(vertexconsumer, matrix4f, matrix3f, f23, 0.0F, f24, k, l, i1, 0.0F, f32);
         vertex(vertexconsumer, matrix4f, matrix3f, f23, f6, f24, k, l, i1, 0.0F, f33);
         vertex(vertexconsumer, matrix4f, matrix3f, f25, f6, f26, k, l, i1, 0.4999F, f33);
         vertex(vertexconsumer, matrix4f, matrix3f, f25, 0.0F, f26, k, l, i1, 0.4999F, f32);
         vertex(vertexconsumer, matrix4f, matrix3f, f27, 0.0F, f28, k, l, i1, 0.0F, f32);
         vertex(vertexconsumer, matrix4f, matrix3f, f27, f6, f28, k, l, i1, 0.0F, f33);
         float f34 = 0.0F;
         if (guardian.tickCount % 2 == 0) {
            f34 = 0.5F;
         }

         vertex(vertexconsumer, matrix4f, matrix3f, f13, f6, f14, k, l, i1, 0.5F, f34 + 0.5F);
         vertex(vertexconsumer, matrix4f, matrix3f, f15, f6, f16, k, l, i1, 1.0F, f34 + 0.5F);
         vertex(vertexconsumer, matrix4f, matrix3f, f19, f6, f20, k, l, i1, 1.0F, f34);
         vertex(vertexconsumer, matrix4f, matrix3f, f17, f6, f18, k, l, i1, 0.5F, f34);
         posestack.popPose();
      }

   }

   private static void vertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float f1, float f2, int i, int j, int k, float f3, float f4) {
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(i, j, k, 255).uv(f3, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }

   public ResourceLocation getTextureLocation(Guardian guardian) {
      return GUARDIAN_LOCATION;
   }
}
