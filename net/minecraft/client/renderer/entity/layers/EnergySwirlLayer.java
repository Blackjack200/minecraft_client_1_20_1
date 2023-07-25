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
import net.minecraft.world.entity.PowerableMob;

public abstract class EnergySwirlLayer<T extends Entity & PowerableMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public EnergySwirlLayer(RenderLayerParent<T, M> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T entity, float f, float f1, float f2, float f3, float f4, float f5) {
      if (entity.isPowered()) {
         float f6 = (float)entity.tickCount + f2;
         EntityModel<T> entitymodel = this.model();
         entitymodel.prepareMobModel(entity, f, f1, f2);
         this.getParentModel().copyPropertiesTo(entitymodel);
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f6) % 1.0F, f6 * 0.01F % 1.0F));
         entitymodel.setupAnim(entity, f, f1, f3, f4, f5);
         entitymodel.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
      }
   }

   protected abstract float xOffset(float f);

   protected abstract ResourceLocation getTextureLocation();

   protected abstract EntityModel<T> model();
}
