package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;

public class EndermiteRenderer extends MobRenderer<Endermite, EndermiteModel<Endermite>> {
   private static final ResourceLocation ENDERMITE_LOCATION = new ResourceLocation("textures/entity/endermite.png");

   public EndermiteRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new EndermiteModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
   }

   protected float getFlipDegrees(Endermite endermite) {
      return 180.0F;
   }

   public ResourceLocation getTextureLocation(Endermite endermite) {
      return ENDERMITE_LOCATION;
   }
}
