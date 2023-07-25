package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.Mob;

public abstract class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
   public HumanoidMobRenderer(EntityRendererProvider.Context entityrendererprovider_context, M humanoidmodel, float f) {
      this(entityrendererprovider_context, humanoidmodel, f, 1.0F, 1.0F, 1.0F);
   }

   public HumanoidMobRenderer(EntityRendererProvider.Context entityrendererprovider_context, M humanoidmodel, float f, float f1, float f2, float f3) {
      super(entityrendererprovider_context, humanoidmodel, f);
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), f1, f2, f3, entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new ElytraLayer<>(this, entityrendererprovider_context.getModelSet()));
      this.addLayer(new ItemInHandLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }
}
