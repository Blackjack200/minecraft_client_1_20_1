package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

public class FoxRenderer extends MobRenderer<Fox, FoxModel<Fox>> {
   private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
   private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
   private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
   private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

   public FoxRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new FoxModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.FOX)), 0.4F);
      this.addLayer(new FoxHeldItemLayer(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   protected void setupRotations(Fox fox, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(fox, posestack, f, f1, f2);
      if (fox.isPouncing() || fox.isFaceplanted()) {
         float f3 = -Mth.lerp(f2, fox.xRotO, fox.getXRot());
         posestack.mulPose(Axis.XP.rotationDegrees(f3));
      }

   }

   public ResourceLocation getTextureLocation(Fox fox) {
      if (fox.getVariant() == Fox.Type.RED) {
         return fox.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
      } else {
         return fox.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
      }
   }
}
