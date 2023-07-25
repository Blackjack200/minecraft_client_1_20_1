package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;

public class SpiderRenderer<T extends Spider> extends MobRenderer<T, SpiderModel<T>> {
   private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/spider.png");

   public SpiderRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this(entityrendererprovider_context, ModelLayers.SPIDER);
   }

   public SpiderRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation) {
      super(entityrendererprovider_context, new SpiderModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation)), 0.8F);
      this.addLayer(new SpiderEyesLayer<>(this));
   }

   protected float getFlipDegrees(T spider) {
      return 180.0F;
   }

   public ResourceLocation getTextureLocation(T spider) {
      return SPIDER_LOCATION;
   }
}
