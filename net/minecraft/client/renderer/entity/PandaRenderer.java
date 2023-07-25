package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;

public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
   private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), (enummap) -> {
      enummap.put(Panda.Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
      enummap.put(Panda.Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
      enummap.put(Panda.Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
      enummap.put(Panda.Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
      enummap.put(Panda.Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
      enummap.put(Panda.Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
      enummap.put(Panda.Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new PandaModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.PANDA)), 0.9F);
      this.addLayer(new PandaHoldsItemLayer(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Panda panda) {
      return TEXTURES.getOrDefault(panda.getVariant(), TEXTURES.get(Panda.Gene.NORMAL));
   }

   protected void setupRotations(Panda panda, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(panda, posestack, f, f1, f2);
      if (panda.rollCounter > 0) {
         int i = panda.rollCounter;
         int j = i + 1;
         float f3 = 7.0F;
         float f4 = panda.isBaby() ? 0.3F : 0.8F;
         if (i < 8) {
            float f5 = (float)(90 * i) / 7.0F;
            float f6 = (float)(90 * j) / 7.0F;
            float f7 = this.getAngle(f5, f6, j, f2, 8.0F);
            posestack.translate(0.0F, (f4 + 0.2F) * (f7 / 90.0F), 0.0F);
            posestack.mulPose(Axis.XP.rotationDegrees(-f7));
         } else if (i < 16) {
            float f8 = ((float)i - 8.0F) / 7.0F;
            float f9 = 90.0F + 90.0F * f8;
            float f10 = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
            float f11 = this.getAngle(f9, f10, j, f2, 16.0F);
            posestack.translate(0.0F, f4 + 0.2F + (f4 - 0.2F) * (f11 - 90.0F) / 90.0F, 0.0F);
            posestack.mulPose(Axis.XP.rotationDegrees(-f11));
         } else if ((float)i < 24.0F) {
            float f12 = ((float)i - 16.0F) / 7.0F;
            float f13 = 180.0F + 90.0F * f12;
            float f14 = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
            float f15 = this.getAngle(f13, f14, j, f2, 24.0F);
            posestack.translate(0.0F, f4 + f4 * (270.0F - f15) / 90.0F, 0.0F);
            posestack.mulPose(Axis.XP.rotationDegrees(-f15));
         } else if (i < 32) {
            float f16 = ((float)i - 24.0F) / 7.0F;
            float f17 = 270.0F + 90.0F * f16;
            float f18 = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
            float f19 = this.getAngle(f17, f18, j, f2, 32.0F);
            posestack.translate(0.0F, f4 * ((360.0F - f19) / 90.0F), 0.0F);
            posestack.mulPose(Axis.XP.rotationDegrees(-f19));
         }
      }

      float f20 = panda.getSitAmount(f2);
      if (f20 > 0.0F) {
         posestack.translate(0.0F, 0.8F * f20, 0.0F);
         posestack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f20, panda.getXRot(), panda.getXRot() + 90.0F)));
         posestack.translate(0.0F, -1.0F * f20, 0.0F);
         if (panda.isScared()) {
            float f21 = (float)(Math.cos((double)panda.tickCount * 1.25D) * Math.PI * (double)0.05F);
            posestack.mulPose(Axis.YP.rotationDegrees(f21));
            if (panda.isBaby()) {
               posestack.translate(0.0F, 0.8F, 0.55F);
            }
         }
      }

      float f22 = panda.getLieOnBackAmount(f2);
      if (f22 > 0.0F) {
         float f23 = panda.isBaby() ? 0.5F : 1.3F;
         posestack.translate(0.0F, f23 * f22, 0.0F);
         posestack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f22, panda.getXRot(), panda.getXRot() + 180.0F)));
      }

   }

   private float getAngle(float f, float f1, int i, float f2, float f3) {
      return (float)i < f3 ? Mth.lerp(f2, f, f1) : f;
   }
}
