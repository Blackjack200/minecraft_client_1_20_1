package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>> extends MobRenderer<T, M> {
   private final float scale;

   public AbstractHorseRenderer(EntityRendererProvider.Context entityrendererprovider_context, M horsemodel, float f) {
      super(entityrendererprovider_context, horsemodel, 0.75F);
      this.scale = f;
   }

   protected void scale(T abstracthorse, PoseStack posestack, float f) {
      posestack.scale(this.scale, this.scale, this.scale);
      super.scale(abstracthorse, posestack, f);
   }
}
