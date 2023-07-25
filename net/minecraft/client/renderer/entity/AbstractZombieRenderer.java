package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>> extends HumanoidMobRenderer<T, M> {
   private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

   protected AbstractZombieRenderer(EntityRendererProvider.Context entityrendererprovider_context, M zombiemodel, M zombiemodel1, M zombiemodel2) {
      super(entityrendererprovider_context, zombiemodel, 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, zombiemodel1, zombiemodel2, entityrendererprovider_context.getModelManager()));
   }

   public ResourceLocation getTextureLocation(Zombie zombie) {
      return ZOMBIE_LOCATION;
   }

   protected boolean isShaking(T zombie) {
      return super.isShaking(zombie) || zombie.isUnderWaterConverting();
   }
}
