package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;

   public ItemInHandLayer(RenderLayerParent<T, M> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      boolean flag = livingentity.getMainArm() == HumanoidArm.RIGHT;
      ItemStack itemstack = flag ? livingentity.getOffhandItem() : livingentity.getMainHandItem();
      ItemStack itemstack1 = flag ? livingentity.getMainHandItem() : livingentity.getOffhandItem();
      if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
         posestack.pushPose();
         if (this.getParentModel().young) {
            float f6 = 0.5F;
            posestack.translate(0.0F, 0.75F, 0.0F);
            posestack.scale(0.5F, 0.5F, 0.5F);
         }

         this.renderArmWithItem(livingentity, itemstack1, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, posestack, multibuffersource, i);
         this.renderArmWithItem(livingentity, itemstack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, posestack, multibuffersource, i);
         posestack.popPose();
      }
   }

   protected void renderArmWithItem(LivingEntity livingentity, ItemStack itemstack, ItemDisplayContext itemdisplaycontext, HumanoidArm humanoidarm, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (!itemstack.isEmpty()) {
         posestack.pushPose();
         this.getParentModel().translateToHand(humanoidarm, posestack);
         posestack.mulPose(Axis.XP.rotationDegrees(-90.0F));
         posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
         boolean flag = humanoidarm == HumanoidArm.LEFT;
         posestack.translate((float)(flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
         this.itemInHandRenderer.renderItem(livingentity, itemstack, itemdisplaycontext, flag, posestack, multibuffersource, i);
         posestack.popPose();
      }
   }
}
