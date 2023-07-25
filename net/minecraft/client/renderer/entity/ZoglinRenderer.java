package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zoglin;

public class ZoglinRenderer extends MobRenderer<Zoglin, HoglinModel<Zoglin>> {
   private static final ResourceLocation ZOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/zoglin.png");

   public ZoglinRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new HoglinModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.ZOGLIN)), 0.7F);
   }

   public ResourceLocation getTextureLocation(Zoglin zoglin) {
      return ZOGLIN_LOCATION;
   }
}
