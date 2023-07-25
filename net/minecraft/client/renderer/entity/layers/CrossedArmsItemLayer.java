package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;

   public CrossedArmsItemLayer(RenderLayerParent<T, M> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      posestack.pushPose();
      posestack.translate(0.0F, 0.4F, -0.4F);
      posestack.mulPose(Axis.XP.rotationDegrees(180.0F));
      ItemStack itemstack = livingentity.getItemBySlot(EquipmentSlot.MAINHAND);
      this.itemInHandRenderer.renderItem(livingentity, itemstack, ItemDisplayContext.GROUND, false, posestack, multibuffersource, i);
      posestack.popPose();
   }
}
