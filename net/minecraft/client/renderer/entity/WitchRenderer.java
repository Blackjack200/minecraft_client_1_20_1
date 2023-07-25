package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

public class WitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
   private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

   public WitchRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new WitchModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.WITCH)), 0.5F);
      this.addLayer(new WitchItemLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public void render(Witch witch, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      this.model.setHoldingItem(!witch.getMainHandItem().isEmpty());
      super.render(witch, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(Witch witch) {
      return WITCH_LOCATION;
   }

   protected void scale(Witch witch, PoseStack posestack, float f) {
      float f1 = 0.9375F;
      posestack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}
