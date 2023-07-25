package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;

public class CodRenderer extends MobRenderer<Cod, CodModel<Cod>> {
   private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

   public CodRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new CodModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.COD)), 0.3F);
   }

   public ResourceLocation getTextureLocation(Cod cod) {
      return COD_LOCATION;
   }

   protected void setupRotations(Cod cod, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(cod, posestack, f, f1, f2);
      float f3 = 4.3F * Mth.sin(0.6F * f);
      posestack.mulPose(Axis.YP.rotationDegrees(f3));
      if (!cod.isInWater()) {
         posestack.translate(0.1F, 0.1F, -0.1F);
         posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}
