package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

public class SquidRenderer<T extends Squid> extends MobRenderer<T, SquidModel<T>> {
   private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

   public SquidRenderer(EntityRendererProvider.Context entityrendererprovider_context, SquidModel<T> squidmodel) {
      super(entityrendererprovider_context, squidmodel, 0.7F);
   }

   public ResourceLocation getTextureLocation(T squid) {
      return SQUID_LOCATION;
   }

   protected void setupRotations(T squid, PoseStack posestack, float f, float f1, float f2) {
      float f3 = Mth.lerp(f2, squid.xBodyRotO, squid.xBodyRot);
      float f4 = Mth.lerp(f2, squid.zBodyRotO, squid.zBodyRot);
      posestack.translate(0.0F, 0.5F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f1));
      posestack.mulPose(Axis.XP.rotationDegrees(f3));
      posestack.mulPose(Axis.YP.rotationDegrees(f4));
      posestack.translate(0.0F, -1.2F, 0.0F);
   }

   protected float getBob(T squid, float f) {
      return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
   }
}
