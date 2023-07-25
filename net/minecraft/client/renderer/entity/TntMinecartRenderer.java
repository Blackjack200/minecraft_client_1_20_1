package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;

public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
   private final BlockRenderDispatcher blockRenderer;

   public TntMinecartRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, ModelLayers.TNT_MINECART);
      this.blockRenderer = entityrendererprovider_context.getBlockRenderDispatcher();
   }

   protected void renderMinecartContents(MinecartTNT minecarttnt, float f, BlockState blockstate, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      int j = minecarttnt.getFuse();
      if (j > -1 && (float)j - f + 1.0F < 10.0F) {
         float f1 = 1.0F - ((float)j - f + 1.0F) / 10.0F;
         f1 = Mth.clamp(f1, 0.0F, 1.0F);
         f1 *= f1;
         f1 *= f1;
         float f2 = 1.0F + f1 * 0.3F;
         posestack.scale(f2, f2, f2);
      }

      renderWhiteSolidBlock(this.blockRenderer, blockstate, posestack, multibuffersource, i, j > -1 && j / 5 % 2 == 0);
   }

   public static void renderWhiteSolidBlock(BlockRenderDispatcher blockrenderdispatcher, BlockState blockstate, PoseStack posestack, MultiBufferSource multibuffersource, int i, boolean flag) {
      int j;
      if (flag) {
         j = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
      } else {
         j = OverlayTexture.NO_OVERLAY;
      }

      blockrenderdispatcher.renderSingleBlock(blockstate, posestack, multibuffersource, i, j);
   }
}
