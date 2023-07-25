package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

public class PhantomRenderer extends MobRenderer<Phantom, PhantomModel<Phantom>> {
   private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

   public PhantomRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new PhantomModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
      this.addLayer(new PhantomEyesLayer<>(this));
   }

   public ResourceLocation getTextureLocation(Phantom phantom) {
      return PHANTOM_LOCATION;
   }

   protected void scale(Phantom phantom, PoseStack posestack, float f) {
      int i = phantom.getPhantomSize();
      float f1 = 1.0F + 0.15F * (float)i;
      posestack.scale(f1, f1, f1);
      posestack.translate(0.0F, 1.3125F, 0.1875F);
   }

   protected void setupRotations(Phantom phantom, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(phantom, posestack, f, f1, f2);
      posestack.mulPose(Axis.XP.rotationDegrees(phantom.getXRot()));
   }
}
