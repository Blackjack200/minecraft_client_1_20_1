package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Saddleable;

public class SaddleLayer<T extends Entity & Saddleable, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private final ResourceLocation textureLocation;
   private final M model;

   public SaddleLayer(RenderLayerParent<T, M> renderlayerparent, M entitymodel, ResourceLocation resourcelocation) {
      super(renderlayerparent);
      this.model = entitymodel;
      this.textureLocation = resourcelocation;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T entity, float f, float f1, float f2, float f3, float f4, float f5) {
      if (entity.isSaddled()) {
         this.getParentModel().copyPropertiesTo(this.model);
         this.model.prepareMobModel(entity, f, f1, f2);
         this.model.setupAnim(entity, f, f1, f3, f4, f5);
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
         this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
