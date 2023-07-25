package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
   private final ItemInHandRenderer itemInHandRenderer;

   public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Dolphin dolphin, float f, float f1, float f2, float f3, float f4, float f5) {
      boolean flag = dolphin.getMainArm() == HumanoidArm.RIGHT;
      posestack.pushPose();
      float f6 = 1.0F;
      float f7 = -1.0F;
      float f8 = Mth.abs(dolphin.getXRot()) / 60.0F;
      if (dolphin.getXRot() < 0.0F) {
         posestack.translate(0.0F, 1.0F - f8 * 0.5F, -1.0F + f8 * 0.5F);
      } else {
         posestack.translate(0.0F, 1.0F + f8 * 0.8F, -1.0F + f8 * 0.2F);
      }

      ItemStack itemstack = flag ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
      this.itemInHandRenderer.renderItem(dolphin, itemstack, ItemDisplayContext.GROUND, false, posestack, multibuffersource, i);
      posestack.popPose();
   }
}
