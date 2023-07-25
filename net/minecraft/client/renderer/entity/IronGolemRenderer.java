package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
   private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem/iron_golem.png");

   public IronGolemRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new IronGolemModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
      this.addLayer(new IronGolemCrackinessLayer(this));
      this.addLayer(new IronGolemFlowerLayer(this, entityrendererprovider_context.getBlockRenderDispatcher()));
   }

   public ResourceLocation getTextureLocation(IronGolem irongolem) {
      return GOLEM_LOCATION;
   }

   protected void setupRotations(IronGolem irongolem, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(irongolem, posestack, f, f1, f2);
      if (!((double)irongolem.walkAnimation.speed() < 0.01D)) {
         float f3 = 13.0F;
         float f4 = irongolem.walkAnimation.position(f2) + 6.0F;
         float f5 = (Math.abs(f4 % 13.0F - 6.5F) - 3.25F) / 3.25F;
         posestack.mulPose(Axis.ZP.rotationDegrees(6.5F * f5));
      }
   }
}
