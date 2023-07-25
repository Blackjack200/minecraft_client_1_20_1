package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;

public class GoatRenderer extends MobRenderer<Goat, GoatModel<Goat>> {
   private static final ResourceLocation GOAT_LOCATION = new ResourceLocation("textures/entity/goat/goat.png");

   public GoatRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new GoatModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.GOAT)), 0.7F);
   }

   public ResourceLocation getTextureLocation(Goat goat) {
      return GOAT_LOCATION;
   }
}
