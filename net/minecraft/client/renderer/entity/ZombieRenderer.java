package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieModel<Zombie>> {
   public ZombieRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this(entityrendererprovider_context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
   }

   public ZombieRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation, ModelLayerLocation modellayerlocation1, ModelLayerLocation modellayerlocation2) {
      super(entityrendererprovider_context, new ZombieModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation)), new ZombieModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation1)), new ZombieModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation2)));
   }
}
