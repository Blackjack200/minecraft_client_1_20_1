package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;

public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
   public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Shulker shulker, float f, float f1, float f2, float f3, float f4, float f5) {
      ResourceLocation resourcelocation = ShulkerRenderer.getTextureLocation(shulker.getColor());
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entitySolid(resourcelocation));
      this.getParentModel().getHead().render(posestack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(shulker, 0.0F));
   }
}
