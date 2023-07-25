package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

public class CowRenderer extends MobRenderer<Cow, CowModel<Cow>> {
   private static final ResourceLocation COW_LOCATION = new ResourceLocation("textures/entity/cow/cow.png");

   public CowRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new CowModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.COW)), 0.7F);
   }

   public ResourceLocation getTextureLocation(Cow cow) {
      return COW_LOCATION;
   }
}
