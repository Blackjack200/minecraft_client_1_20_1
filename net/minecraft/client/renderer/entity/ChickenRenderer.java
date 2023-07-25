package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

public class ChickenRenderer extends MobRenderer<Chicken, ChickenModel<Chicken>> {
   private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

   public ChickenRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new ChickenModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
   }

   public ResourceLocation getTextureLocation(Chicken chicken) {
      return CHICKEN_LOCATION;
   }

   protected float getBob(Chicken chicken, float f) {
      float f1 = Mth.lerp(f, chicken.oFlap, chicken.flap);
      float f2 = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
      return (Mth.sin(f1) + 1.0F) * f2;
   }
}
