package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class WolfRenderer extends MobRenderer<Wolf, WolfModel<Wolf>> {
   private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public WolfRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new WolfModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.WOLF)), 0.5F);
      this.addLayer(new WolfCollarLayer(this));
   }

   protected float getBob(Wolf wolf, float f) {
      return wolf.getTailAngle();
   }

   public void render(Wolf wolf, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (wolf.isWet()) {
         float f2 = wolf.getWetShade(f1);
         this.model.setColor(f2, f2, f2);
      }

      super.render(wolf, f, f1, posestack, multibuffersource, i);
      if (wolf.isWet()) {
         this.model.setColor(1.0F, 1.0F, 1.0F);
      }

   }

   public ResourceLocation getTextureLocation(Wolf wolf) {
      if (wolf.isTame()) {
         return WOLF_TAME_LOCATION;
      } else {
         return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
      }
   }
}
