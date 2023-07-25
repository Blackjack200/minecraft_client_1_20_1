package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
   private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
   private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

   public ExperienceOrbRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.15F;
      this.shadowStrength = 0.75F;
   }

   protected int getBlockLightLevel(ExperienceOrb experienceorb, BlockPos blockpos) {
      return Mth.clamp(super.getBlockLightLevel(experienceorb, blockpos) + 7, 0, 15);
   }

   public void render(ExperienceOrb experienceorb, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      int j = experienceorb.getIcon();
      float f2 = (float)(j % 4 * 16 + 0) / 64.0F;
      float f3 = (float)(j % 4 * 16 + 16) / 64.0F;
      float f4 = (float)(j / 4 * 16 + 0) / 64.0F;
      float f5 = (float)(j / 4 * 16 + 16) / 64.0F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.25F;
      float f9 = 255.0F;
      float f10 = ((float)experienceorb.tickCount + f1) / 2.0F;
      int k = (int)((Mth.sin(f10 + 0.0F) + 1.0F) * 0.5F * 255.0F);
      int l = 255;
      int i1 = (int)((Mth.sin(f10 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
      posestack.translate(0.0F, 0.1F, 0.0F);
      posestack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      float f11 = 0.3F;
      posestack.scale(0.3F, 0.3F, 0.3F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RENDER_TYPE);
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();
      vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, -0.25F, k, 255, i1, f2, f5, i);
      vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, -0.25F, k, 255, i1, f3, f5, i);
      vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, 0.75F, k, 255, i1, f3, f4, i);
      vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, 0.75F, k, 255, i1, f2, f4, i);
      posestack.popPose();
      super.render(experienceorb, f, f1, posestack, multibuffersource, i);
   }

   private static void vertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float f1, int i, int j, int k, float f2, float f3, int l) {
      vertexconsumer.vertex(matrix4f, f, f1, 0.0F).color(i, j, k, 128).uv(f2, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(l).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }

   public ResourceLocation getTextureLocation(ExperienceOrb experienceorb) {
      return EXPERIENCE_ORB_LOCATION;
   }
}
