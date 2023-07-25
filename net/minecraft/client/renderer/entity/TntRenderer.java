package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;

public class TntRenderer extends EntityRenderer<PrimedTnt> {
   private final BlockRenderDispatcher blockRenderer;

   public TntRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.5F;
      this.blockRenderer = entityrendererprovider_context.getBlockRenderDispatcher();
   }

   public void render(PrimedTnt primedtnt, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.translate(0.0F, 0.5F, 0.0F);
      int j = primedtnt.getFuse();
      if ((float)j - f1 + 1.0F < 10.0F) {
         float f2 = 1.0F - ((float)j - f1 + 1.0F) / 10.0F;
         f2 = Mth.clamp(f2, 0.0F, 1.0F);
         f2 *= f2;
         f2 *= f2;
         float f3 = 1.0F + f2 * 0.3F;
         posestack.scale(f3, f3, f3);
      }

      posestack.mulPose(Axis.YP.rotationDegrees(-90.0F));
      posestack.translate(-0.5F, -0.5F, 0.5F);
      posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
      TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, Blocks.TNT.defaultBlockState(), posestack, multibuffersource, i, j / 5 % 2 == 0);
      posestack.popPose();
      super.render(primedtnt, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(PrimedTnt primedtnt) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
