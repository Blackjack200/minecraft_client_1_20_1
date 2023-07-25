package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PandaHoldsItemLayer extends RenderLayer<Panda, PandaModel<Panda>> {
   private final ItemInHandRenderer itemInHandRenderer;

   public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> renderlayerparent, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Panda panda, float f, float f1, float f2, float f3, float f4, float f5) {
      ItemStack itemstack = panda.getItemBySlot(EquipmentSlot.MAINHAND);
      if (panda.isSitting() && !panda.isScared()) {
         float f6 = -0.6F;
         float f7 = 1.4F;
         if (panda.isEating()) {
            f6 -= 0.2F * Mth.sin(f3 * 0.6F) + 0.2F;
            f7 -= 0.09F * Mth.sin(f3 * 0.6F);
         }

         posestack.pushPose();
         posestack.translate(0.1F, f7, f6);
         this.itemInHandRenderer.renderItem(panda, itemstack, ItemDisplayContext.GROUND, false, posestack, multibuffersource, i);
         posestack.popPose();
      }
   }
}
