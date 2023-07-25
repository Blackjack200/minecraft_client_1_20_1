package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

public class BatRenderer extends MobRenderer<Bat, BatModel> {
   private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

   public BatRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new BatModel(entityrendererprovider_context.bakeLayer(ModelLayers.BAT)), 0.25F);
   }

   public ResourceLocation getTextureLocation(Bat bat) {
      return BAT_LOCATION;
   }

   protected void scale(Bat bat, PoseStack posestack, float f) {
      posestack.scale(0.35F, 0.35F, 0.35F);
   }

   protected void setupRotations(Bat bat, PoseStack posestack, float f, float f1, float f2) {
      if (bat.isResting()) {
         posestack.translate(0.0F, -0.1F, 0.0F);
      } else {
         posestack.translate(0.0F, Mth.cos(f * 0.3F) * 0.1F, 0.0F);
      }

      super.setupRotations(bat, posestack, f, f1, f2);
   }
}
