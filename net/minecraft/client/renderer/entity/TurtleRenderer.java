package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

public class TurtleRenderer extends MobRenderer<Turtle, TurtleModel<Turtle>> {
   private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");

   public TurtleRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new TurtleModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.TURTLE)), 0.7F);
   }

   public void render(Turtle turtle, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (turtle.isBaby()) {
         this.shadowRadius *= 0.5F;
      }

      super.render(turtle, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(Turtle turtle) {
      return TURTLE_LOCATION;
   }
}
