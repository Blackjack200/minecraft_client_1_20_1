package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;

public class MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
   private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

   public MagmaCubeRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new LavaSlimeModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
   }

   protected int getBlockLightLevel(MagmaCube magmacube, BlockPos blockpos) {
      return 15;
   }

   public ResourceLocation getTextureLocation(MagmaCube magmacube) {
      return MAGMACUBE_LOCATION;
   }

   public void render(MagmaCube magmacube, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      this.shadowRadius = 0.25F * (float)magmacube.getSize();
      super.render(magmacube, f, f1, posestack, multibuffersource, i);
   }

   protected void scale(MagmaCube magmacube, PoseStack posestack, float f) {
      int i = magmacube.getSize();
      float f1 = Mth.lerp(f, magmacube.oSquish, magmacube.squish) / ((float)i * 0.5F + 1.0F);
      float f2 = 1.0F / (f1 + 1.0F);
      posestack.scale(f2 * (float)i, 1.0F / f2 * (float)i, f2 * (float)i);
   }
}
