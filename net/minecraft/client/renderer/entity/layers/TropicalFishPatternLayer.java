package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.TropicalFish;

public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
   private static final ResourceLocation KOB_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png");
   private static final ResourceLocation SUNSTREAK_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png");
   private static final ResourceLocation SNOOPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png");
   private static final ResourceLocation DASHER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png");
   private static final ResourceLocation BRINELY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png");
   private static final ResourceLocation SPOTTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png");
   private static final ResourceLocation FLOPPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png");
   private static final ResourceLocation STRIPEY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png");
   private static final ResourceLocation GLITTER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png");
   private static final ResourceLocation BLOCKFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png");
   private static final ResourceLocation BETTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png");
   private static final ResourceLocation CLAYFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png");
   private final TropicalFishModelA<TropicalFish> modelA;
   private final TropicalFishModelB<TropicalFish> modelB;

   public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.modelA = new TropicalFishModelA<>(entitymodelset.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
      this.modelB = new TropicalFishModelB<>(entitymodelset.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, TropicalFish tropicalfish, float f, float f1, float f2, float f3, float f4, float f5) {
      TropicalFish.Pattern tropicalfish_pattern = tropicalfish.getVariant();
      Object var10000;
      switch (tropicalfish_pattern.base()) {
         case SMALL:
            var10000 = this.modelA;
            break;
         case LARGE:
            var10000 = this.modelB;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      EntityModel<TropicalFish> entitymodel = (EntityModel<TropicalFish>)var10000;
      ResourceLocation var15;
      switch (tropicalfish_pattern) {
         case KOB:
            var15 = KOB_TEXTURE;
            break;
         case SUNSTREAK:
            var15 = SUNSTREAK_TEXTURE;
            break;
         case SNOOPER:
            var15 = SNOOPER_TEXTURE;
            break;
         case DASHER:
            var15 = DASHER_TEXTURE;
            break;
         case BRINELY:
            var15 = BRINELY_TEXTURE;
            break;
         case SPOTTY:
            var15 = SPOTTY_TEXTURE;
            break;
         case FLOPPER:
            var15 = FLOPPER_TEXTURE;
            break;
         case STRIPEY:
            var15 = STRIPEY_TEXTURE;
            break;
         case GLITTER:
            var15 = GLITTER_TEXTURE;
            break;
         case BLOCKFISH:
            var15 = BLOCKFISH_TEXTURE;
            break;
         case BETTY:
            var15 = BETTY_TEXTURE;
            break;
         case CLAYFISH:
            var15 = CLAYFISH_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      ResourceLocation resourcelocation = var15;
      float[] afloat = tropicalfish.getPatternColor().getTextureDiffuseColors();
      coloredCutoutModelCopyLayerRender(this.getParentModel(), entitymodel, resourcelocation, posestack, multibuffersource, i, tropicalfish, f, f1, f3, f4, f5, f2, afloat[0], afloat[1], afloat[2]);
   }
}
