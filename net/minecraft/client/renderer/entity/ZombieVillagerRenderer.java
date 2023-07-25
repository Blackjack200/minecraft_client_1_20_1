package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerRenderer extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> {
   private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_villager.png");

   public ZombieVillagerRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new ZombieVillagerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, new ZombieVillagerModel(entityrendererprovider_context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)), new ZombieVillagerModel(entityrendererprovider_context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)), entityrendererprovider_context.getModelManager()));
      this.addLayer(new VillagerProfessionLayer<>(this, entityrendererprovider_context.getResourceManager(), "zombie_villager"));
   }

   public ResourceLocation getTextureLocation(ZombieVillager zombievillager) {
      return ZOMBIE_VILLAGER_LOCATION;
   }

   protected boolean isShaking(ZombieVillager zombievillager) {
      return super.isShaking(zombievillager) || zombievillager.isConverting();
   }
}
