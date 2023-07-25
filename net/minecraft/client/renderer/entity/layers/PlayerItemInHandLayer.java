package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;
   private static final float X_ROT_MIN = (-(float)Math.PI / 6F);
   private static final float X_ROT_MAX = ((float)Math.PI / 2F);

   public PlayerItemInHandLayer(RenderLayerParent<T, M> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent, iteminhandrenderer);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   protected void renderArmWithItem(LivingEntity livingentity, ItemStack itemstack, ItemDisplayContext itemdisplaycontext, HumanoidArm humanoidarm, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (itemstack.is(Items.SPYGLASS) && livingentity.getUseItem() == itemstack && livingentity.swingTime == 0) {
         this.renderArmWithSpyglass(livingentity, itemstack, humanoidarm, posestack, multibuffersource, i);
      } else {
         super.renderArmWithItem(livingentity, itemstack, itemdisplaycontext, humanoidarm, posestack, multibuffersource, i);
      }

   }

   private void renderArmWithSpyglass(LivingEntity livingentity, ItemStack itemstack, HumanoidArm humanoidarm, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      ModelPart modelpart = this.getParentModel().getHead();
      float f = modelpart.xRot;
      modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float)Math.PI / 6F), ((float)Math.PI / 2F));
      modelpart.translateAndRotate(posestack);
      modelpart.xRot = f;
      CustomHeadLayer.translateToHead(posestack, false);
      boolean flag = humanoidarm == HumanoidArm.LEFT;
      posestack.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
      this.itemInHandRenderer.renderItem(livingentity, itemstack, ItemDisplayContext.HEAD, false, posestack, multibuffersource, i);
      posestack.popPose();
   }
}
