package net.minecraft.client.renderer.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;

public class SpectralArrowRenderer extends ArrowRenderer<SpectralArrow> {
   public static final ResourceLocation SPECTRAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/spectral_arrow.png");

   public SpectralArrowRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   public ResourceLocation getTextureLocation(SpectralArrow spectralarrow) {
      return SPECTRAL_ARROW_LOCATION;
   }
}
