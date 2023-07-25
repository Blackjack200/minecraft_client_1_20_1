package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FishingHookRenderer extends EntityRenderer<FishingHook> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
   private static final double VIEW_BOBBING_SCALE = 960.0D;

   public FishingHookRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   public void render(FishingHook fishinghook, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      Player player = fishinghook.getPlayerOwner();
      if (player != null) {
         posestack.pushPose();
         posestack.pushPose();
         posestack.scale(0.5F, 0.5F, 0.5F);
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
         int j = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
         ItemStack itemstack = player.getMainHandItem();
         if (!itemstack.is(Items.FISHING_ROD)) {
            j = -j;
         }

         float f2 = player.getAttackAnim(f1);
         float f3 = Mth.sin(Mth.sqrt(f2) * (float)Math.PI);
         float f4 = Mth.lerp(f1, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180F);
         double d0 = (double)Mth.sin(f4);
         double d1 = (double)Mth.cos(f4);
         double d2 = (double)j * 0.35D;
         double d3 = 0.8D;
         double d8;
         double d9;
         double d10;
         float f6;
         if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && player == Minecraft.getInstance().player) {
            double d7 = 960.0D / (double)this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)j * 0.525F, -0.1F);
            vec3 = vec3.scale(d7);
            vec3 = vec3.yRot(f3 * 0.5F);
            vec3 = vec3.xRot(-f3 * 0.7F);
            d8 = Mth.lerp((double)f1, player.xo, player.getX()) + vec3.x;
            d9 = Mth.lerp((double)f1, player.yo, player.getY()) + vec3.y;
            d10 = Mth.lerp((double)f1, player.zo, player.getZ()) + vec3.z;
            f6 = player.getEyeHeight();
         } else {
            d8 = Mth.lerp((double)f1, player.xo, player.getX()) - d1 * d2 - d0 * 0.8D;
            d9 = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)f1 - 0.45D;
            d10 = Mth.lerp((double)f1, player.zo, player.getZ()) - d0 * d2 + d1 * 0.8D;
            f6 = player.isCrouching() ? -0.1875F : 0.0F;
         }

         double d11 = Mth.lerp((double)f1, fishinghook.xo, fishinghook.getX());
         double d12 = Mth.lerp((double)f1, fishinghook.yo, fishinghook.getY()) + 0.25D;
         double d13 = Mth.lerp((double)f1, fishinghook.zo, fishinghook.getZ());
         float f7 = (float)(d8 - d11);
         float f8 = (float)(d9 - d12) + f6;
         float f9 = (float)(d10 - d13);
         VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RenderType.lineStrip());
         PoseStack.Pose posestack_pose1 = posestack.last();
         int k = 16;

         for(int l = 0; l <= 16; ++l) {
            stringVertex(f7, f8, f9, vertexconsumer1, posestack_pose1, fraction(l, 16), fraction(l + 1, 16));
         }

         posestack.popPose();
         super.render(fishinghook, f, f1, posestack, multibuffersource, i);
      }
   }

   private static float fraction(int i, int j) {
      return (float)i / (float)j;
   }

   private static void vertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
      vertexconsumer.vertex(matrix4f, f - 0.5F, (float)j - 0.5F, 0.0F).color(255, 255, 255, 255).uv((float)k, (float)l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }

   private static void stringVertex(float f, float f1, float f2, VertexConsumer vertexconsumer, PoseStack.Pose posestack_pose, float f3, float f4) {
      float f5 = f * f3;
      float f6 = f1 * (f3 * f3 + f3) * 0.5F + 0.25F;
      float f7 = f2 * f3;
      float f8 = f * f4 - f5;
      float f9 = f1 * (f4 * f4 + f4) * 0.5F + 0.25F - f6;
      float f10 = f2 * f4 - f7;
      float f11 = Mth.sqrt(f8 * f8 + f9 * f9 + f10 * f10);
      f8 /= f11;
      f9 /= f11;
      f10 /= f11;
      vertexconsumer.vertex(posestack_pose.pose(), f5, f6, f7).color(0, 0, 0, 255).normal(posestack_pose.normal(), f8, f9, f10).endVertex();
   }

   public ResourceLocation getTextureLocation(FishingHook fishinghook) {
      return TEXTURE_LOCATION;
   }
}
