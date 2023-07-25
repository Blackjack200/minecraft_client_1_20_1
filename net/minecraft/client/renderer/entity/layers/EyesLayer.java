package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

public abstract class EyesLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public EyesLayer(RenderLayerParent<T, M> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T entity, float f, float f1, float f2, float f3, float f4, float f5) {
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.renderType());
      this.getParentModel().renderToBuffer(posestack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public abstract RenderType renderType();
}
