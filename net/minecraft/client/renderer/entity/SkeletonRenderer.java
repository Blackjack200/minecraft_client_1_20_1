package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class SkeletonRenderer extends HumanoidMobRenderer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> {
   private static final ResourceLocation SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/skeleton.png");

   public SkeletonRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this(entityrendererprovider_context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
   }

   public SkeletonRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation, ModelLayerLocation modellayerlocation1, ModelLayerLocation modellayerlocation2) {
      super(entityrendererprovider_context, new SkeletonModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation)), 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonModel(entityrendererprovider_context.bakeLayer(modellayerlocation1)), new SkeletonModel(entityrendererprovider_context.bakeLayer(modellayerlocation2)), entityrendererprovider_context.getModelManager()));
   }

   public ResourceLocation getTextureLocation(AbstractSkeleton abstractskeleton) {
      return SKELETON_LOCATION;
   }

   protected boolean isShaking(AbstractSkeleton abstractskeleton) {
      return abstractskeleton.isShaking();
   }
}
