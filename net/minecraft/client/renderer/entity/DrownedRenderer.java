package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;

public class DrownedRenderer extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
   private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

   public DrownedRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new DrownedModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.DROWNED)), new DrownedModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)), new DrownedModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)));
      this.addLayer(new DrownedOuterLayer<>(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Zombie zombie) {
      return DROWNED_LOCATION;
   }

   protected void setupRotations(Drowned drowned, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(drowned, posestack, f, f1, f2);
      float f3 = drowned.getSwimAmount(f2);
      if (f3 > 0.0F) {
         float f4 = -10.0F - drowned.getXRot();
         float f5 = Mth.lerp(f3, 0.0F, f4);
         posestack.rotateAround(Axis.XP.rotationDegrees(f5), 0.0F, drowned.getBbHeight() / 2.0F, 0.0F);
      }

   }
}
