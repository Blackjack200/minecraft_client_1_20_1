package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;

public class DrownedOuterLayer<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {
   private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
   private final DrownedModel<T> model;

   public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new DrownedModel<>(entitymodelset.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T drowned, float f, float f1, float f2, float f3, float f4, float f5) {
      coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, DROWNED_OUTER_LAYER_LOCATION, posestack, multibuffersource, i, drowned, f, f1, f3, f4, f5, f2, 1.0F, 1.0F, 1.0F);
   }
}
