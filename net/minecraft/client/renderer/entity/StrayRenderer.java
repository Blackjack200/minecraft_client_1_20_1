package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class StrayRenderer extends SkeletonRenderer {
   private static final ResourceLocation STRAY_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/stray.png");

   public StrayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, ModelLayers.STRAY, ModelLayers.STRAY_INNER_ARMOR, ModelLayers.STRAY_OUTER_ARMOR);
      this.addLayer(new StrayClothingLayer<>(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(AbstractSkeleton abstractskeleton) {
      return STRAY_SKELETON_LOCATION;
   }
}
