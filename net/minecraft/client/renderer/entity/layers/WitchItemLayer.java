package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WitchItemLayer<T extends LivingEntity> extends CrossedArmsItemLayer<T, WitchModel<T>> {
   public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent, iteminhandrenderer);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      ItemStack itemstack = livingentity.getMainHandItem();
      posestack.pushPose();
      if (itemstack.is(Items.POTION)) {
         this.getParentModel().getHead().translateAndRotate(posestack);
         this.getParentModel().getNose().translateAndRotate(posestack);
         posestack.translate(0.0625F, 0.25F, 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
         posestack.mulPose(Axis.XP.rotationDegrees(140.0F));
         posestack.mulPose(Axis.ZP.rotationDegrees(10.0F));
         posestack.translate(0.0F, -0.4F, 0.4F);
      }

      super.render(posestack, multibuffersource, i, livingentity, f, f1, f2, f3, f4, f5);
      posestack.popPose();
   }
}
