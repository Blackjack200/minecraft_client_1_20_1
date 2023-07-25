package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogRenderer extends MobRenderer<Frog, FrogModel<Frog>> {
   public FrogRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new FrogModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.FROG)), 0.3F);
   }

   public ResourceLocation getTextureLocation(Frog frog) {
      return frog.getVariant().texture();
   }
}
