package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;

public class SalmonRenderer extends MobRenderer<Salmon, SalmonModel<Salmon>> {
   private static final ResourceLocation SALMON_LOCATION = new ResourceLocation("textures/entity/fish/salmon.png");

   public SalmonRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new SalmonModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.SALMON)), 0.4F);
   }

   public ResourceLocation getTextureLocation(Salmon salmon) {
      return SALMON_LOCATION;
   }

   protected void setupRotations(Salmon salmon, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(salmon, posestack, f, f1, f2);
      float f3 = 1.0F;
      float f4 = 1.0F;
      if (!salmon.isInWater()) {
         f3 = 1.3F;
         f4 = 1.7F;
      }

      float f5 = f3 * 4.3F * Mth.sin(f4 * 0.6F * f);
      posestack.mulPose(Axis.YP.rotationDegrees(f5));
      posestack.translate(0.0F, 0.0F, -0.4F);
      if (!salmon.isInWater()) {
         posestack.translate(0.2F, 0.1F, 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}
