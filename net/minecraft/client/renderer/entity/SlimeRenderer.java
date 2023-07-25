package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

public class SlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
   private static final ResourceLocation SLIME_LOCATION = new ResourceLocation("textures/entity/slime/slime.png");

   public SlimeRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new SlimeModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.SLIME)), 0.25F);
      this.addLayer(new SlimeOuterLayer<>(this, entityrendererprovider_context.getModelSet()));
   }

   public void render(Slime slime, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      this.shadowRadius = 0.25F * (float)slime.getSize();
      super.render(slime, f, f1, posestack, multibuffersource, i);
   }

   protected void scale(Slime slime, PoseStack posestack, float f) {
      float f1 = 0.999F;
      posestack.scale(0.999F, 0.999F, 0.999F);
      posestack.translate(0.0F, 0.001F, 0.0F);
      float f2 = (float)slime.getSize();
      float f3 = Mth.lerp(f, slime.oSquish, slime.squish) / (f2 * 0.5F + 1.0F);
      float f4 = 1.0F / (f3 + 1.0F);
      posestack.scale(f4 * f2, 1.0F / f4 * f2, f4 * f2);
   }

   public ResourceLocation getTextureLocation(Slime slime) {
      return SLIME_LOCATION;
   }
}
