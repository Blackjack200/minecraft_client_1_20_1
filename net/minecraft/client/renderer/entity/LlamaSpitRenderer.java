package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;

public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
   private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
   private final LlamaSpitModel<LlamaSpit> model;

   public LlamaSpitRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new LlamaSpitModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.LLAMA_SPIT));
   }

   public void render(LlamaSpit llamaspit, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.translate(0.0F, 0.15F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(f1, llamaspit.yRotO, llamaspit.getYRot()) - 90.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(f1, llamaspit.xRotO, llamaspit.getXRot())));
      this.model.setupAnim(llamaspit, f1, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
      super.render(llamaspit, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(LlamaSpit llamaspit) {
      return LLAMA_SPIT_LOCATION;
   }
}
