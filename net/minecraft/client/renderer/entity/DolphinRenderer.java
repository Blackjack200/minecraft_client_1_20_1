package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
   private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

   public DolphinRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new DolphinModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.DOLPHIN)), 0.7F);
      this.addLayer(new DolphinCarryingItemLayer(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Dolphin dolphin) {
      return DOLPHIN_LOCATION;
   }
}
