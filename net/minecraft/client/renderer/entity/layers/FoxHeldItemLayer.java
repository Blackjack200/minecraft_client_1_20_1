package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
   private final ItemInHandRenderer itemInHandRenderer;

   public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Fox fox, float f, float f1, float f2, float f3, float f4, float f5) {
      boolean flag = fox.isSleeping();
      boolean flag1 = fox.isBaby();
      posestack.pushPose();
      if (flag1) {
         float f6 = 0.75F;
         posestack.scale(0.75F, 0.75F, 0.75F);
         posestack.translate(0.0F, 0.5F, 0.209375F);
      }

      posestack.translate((this.getParentModel()).head.x / 16.0F, (this.getParentModel()).head.y / 16.0F, (this.getParentModel()).head.z / 16.0F);
      float f7 = fox.getHeadRollAngle(f2);
      posestack.mulPose(Axis.ZP.rotation(f7));
      posestack.mulPose(Axis.YP.rotationDegrees(f4));
      posestack.mulPose(Axis.XP.rotationDegrees(f5));
      if (fox.isBaby()) {
         if (flag) {
            posestack.translate(0.4F, 0.26F, 0.15F);
         } else {
            posestack.translate(0.06F, 0.26F, -0.5F);
         }
      } else if (flag) {
         posestack.translate(0.46F, 0.26F, 0.22F);
      } else {
         posestack.translate(0.06F, 0.27F, -0.5F);
      }

      posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
      if (flag) {
         posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

      ItemStack itemstack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
      this.itemInHandRenderer.renderItem(fox, itemstack, ItemDisplayContext.GROUND, false, posestack, multibuffersource, i);
      posestack.popPose();
   }
}
