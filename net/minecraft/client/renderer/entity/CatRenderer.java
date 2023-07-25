package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class CatRenderer extends MobRenderer<Cat, CatModel<Cat>> {
   public CatRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new CatModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.CAT)), 0.4F);
      this.addLayer(new CatCollarLayer(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Cat cat) {
      return cat.getResourceLocation();
   }

   protected void scale(Cat cat, PoseStack posestack, float f) {
      super.scale(cat, posestack, f);
      posestack.scale(0.8F, 0.8F, 0.8F);
   }

   protected void setupRotations(Cat cat, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(cat, posestack, f, f1, f2);
      float f3 = cat.getLieDownAmount(f2);
      if (f3 > 0.0F) {
         posestack.translate(0.4F * f3, 0.15F * f3, 0.1F * f3);
         posestack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(f3, 0.0F, 90.0F)));
         BlockPos blockpos = cat.blockPosition();

         for(Player player : cat.level().getEntitiesOfClass(Player.class, (new AABB(blockpos)).inflate(2.0D, 2.0D, 2.0D))) {
            if (player.isSleeping()) {
               posestack.translate(0.15F * f3, 0.0F, 0.0F);
               break;
            }
         }
      }

   }
}
