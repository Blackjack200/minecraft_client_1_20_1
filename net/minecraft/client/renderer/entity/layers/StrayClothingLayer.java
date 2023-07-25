package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class StrayClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
   private final SkeletonModel<T> layerModel;

   public StrayClothingLayer(RenderLayerParent<T, M> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.layerModel = new SkeletonModel<>(entitymodelset.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T mob, float f, float f1, float f2, float f3, float f4, float f5) {
      coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, STRAY_CLOTHES_LOCATION, posestack, multibuffersource, i, mob, f, f1, f3, f4, f5, f2, 1.0F, 1.0F, 1.0F);
   }
}
