package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

public class TropicalFishRenderer extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
   private final ColorableHierarchicalModel<TropicalFish> modelA = this.getModel();
   private final ColorableHierarchicalModel<TropicalFish> modelB;
   private static final ResourceLocation MODEL_A_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a.png");
   private static final ResourceLocation MODEL_B_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b.png");

   public TropicalFishRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new TropicalFishModelA<>(entityrendererprovider_context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
      this.modelB = new TropicalFishModelB<>(entityrendererprovider_context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
      this.addLayer(new TropicalFishPatternLayer(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(TropicalFish tropicalfish) {
      ResourceLocation var10000;
      switch (tropicalfish.getVariant().base()) {
         case SMALL:
            var10000 = MODEL_A_TEXTURE;
            break;
         case LARGE:
            var10000 = MODEL_B_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public void render(TropicalFish tropicalfish, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      ColorableHierarchicalModel var10000;
      switch (tropicalfish.getVariant().base()) {
         case SMALL:
            var10000 = this.modelA;
            break;
         case LARGE:
            var10000 = this.modelB;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      ColorableHierarchicalModel<TropicalFish> colorablehierarchicalmodel = var10000;
      this.model = colorablehierarchicalmodel;
      float[] afloat = tropicalfish.getBaseColor().getTextureDiffuseColors();
      colorablehierarchicalmodel.setColor(afloat[0], afloat[1], afloat[2]);
      super.render(tropicalfish, f, f1, posestack, multibuffersource, i);
      colorablehierarchicalmodel.setColor(1.0F, 1.0F, 1.0F);
   }

   protected void setupRotations(TropicalFish tropicalfish, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(tropicalfish, posestack, f, f1, f2);
      float f3 = 4.3F * Mth.sin(0.6F * f);
      posestack.mulPose(Axis.YP.rotationDegrees(f3));
      if (!tropicalfish.isInWater()) {
         posestack.translate(0.2F, 0.1F, 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}
