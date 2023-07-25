package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class HoglinRenderer extends MobRenderer<Hoglin, HoglinModel<Hoglin>> {
   private static final ResourceLocation HOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

   public HoglinRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new HoglinModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.HOGLIN)), 0.7F);
   }

   public ResourceLocation getTextureLocation(Hoglin hoglin) {
      return HOGLIN_LOCATION;
   }

   protected boolean isShaking(Hoglin hoglin) {
      return super.isShaking(hoglin) || hoglin.isConverting();
   }
}
