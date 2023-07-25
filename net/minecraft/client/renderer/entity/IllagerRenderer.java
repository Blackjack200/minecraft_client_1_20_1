package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;

public abstract class IllagerRenderer<T extends AbstractIllager> extends MobRenderer<T, IllagerModel<T>> {
   protected IllagerRenderer(EntityRendererProvider.Context entityrendererprovider_context, IllagerModel<T> illagermodel, float f) {
      super(entityrendererprovider_context, illagermodel, f);
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), entityrendererprovider_context.getItemInHandRenderer()));
   }

   protected void scale(T abstractillager, PoseStack posestack, float f) {
      float f1 = 0.9375F;
      posestack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}
