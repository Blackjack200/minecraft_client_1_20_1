package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
   private final RenderLayerParent<T, M> renderer;

   public RenderLayer(RenderLayerParent<T, M> renderlayerparent) {
      this.renderer = renderlayerparent;
   }

   protected static <T extends LivingEntity> void coloredCutoutModelCopyLayerRender(EntityModel<T> entitymodel, EntityModel<T> entitymodel1, ResourceLocation resourcelocation, PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
      if (!livingentity.isInvisible()) {
         entitymodel.copyPropertiesTo(entitymodel1);
         entitymodel1.prepareMobModel(livingentity, f, f1, f5);
         entitymodel1.setupAnim(livingentity, f, f1, f2, f3, f4);
         renderColoredCutoutModel(entitymodel1, resourcelocation, posestack, multibuffersource, i, livingentity, f6, f7, f8);
      }

   }

   protected static <T extends LivingEntity> void renderColoredCutoutModel(EntityModel<T> entitymodel, ResourceLocation resourcelocation, PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2) {
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(resourcelocation));
      entitymodel.renderToBuffer(posestack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(livingentity, 0.0F), f, f1, f2, 1.0F);
   }

   public M getParentModel() {
      return this.renderer.getModel();
   }

   protected ResourceLocation getTextureLocation(T entity) {
      return this.renderer.getTextureLocation(entity);
   }

   public abstract void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T entity, float f, float f1, float f2, float f3, float f4, float f5);
}
