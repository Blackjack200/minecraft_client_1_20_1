package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

   public DragonFireballRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   protected int getBlockLightLevel(DragonFireball dragonfireball, BlockPos blockpos) {
      return 15;
   }

   public void render(DragonFireball dragonfireball, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.scale(2.0F, 2.0F, 2.0F);
      posestack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RENDER_TYPE);
      vertex(vertexconsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
      vertex(vertexconsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
      posestack.popPose();
      super.render(dragonfireball, f, f1, posestack, multibuffersource, i);
   }

   private static void vertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
      vertexconsumer.vertex(matrix4f, f - 0.5F, (float)j - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)k, (float)l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }

   public ResourceLocation getTextureLocation(DragonFireball dragonfireball) {
      return TEXTURE_LOCATION;
   }
}
