package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public class CatCollarLayer extends RenderLayer<Cat, CatModel<Cat>> {
   private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
   private final CatModel<Cat> catModel;

   public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.catModel = new CatModel<>(entitymodelset.bakeLayer(ModelLayers.CAT_COLLAR));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Cat cat, float f, float f1, float f2, float f3, float f4, float f5) {
      if (cat.isTame()) {
         float[] afloat = cat.getCollarColor().getTextureDiffuseColors();
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.catModel, CAT_COLLAR_LOCATION, posestack, multibuffersource, i, cat, f, f1, f3, f4, f5, f2, afloat[0], afloat[1], afloat[2]);
      }
   }
}
