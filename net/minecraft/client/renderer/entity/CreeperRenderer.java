package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperRenderer extends MobRenderer<Creeper, CreeperModel<Creeper>> {
   private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

   public CreeperRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new CreeperModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
      this.addLayer(new CreeperPowerLayer(this, entityrendererprovider_context.getModelSet()));
   }

   protected void scale(Creeper creeper, PoseStack posestack, float f) {
      float f1 = creeper.getSwelling(f);
      float f2 = 1.0F + Mth.sin(f1 * 100.0F) * f1 * 0.01F;
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      f1 *= f1;
      f1 *= f1;
      float f3 = (1.0F + f1 * 0.4F) * f2;
      float f4 = (1.0F + f1 * 0.1F) / f2;
      posestack.scale(f3, f4, f3);
   }

   protected float getWhiteOverlayProgress(Creeper creeper, float f) {
      float f1 = creeper.getSwelling(f);
      return (int)(f1 * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f1, 0.5F, 1.0F);
   }

   public ResourceLocation getTextureLocation(Creeper creeper) {
      return CREEPER_LOCATION;
   }
}
