package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
   private final BlockRenderDispatcher dispatcher;

   public FallingBlockRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.5F;
      this.dispatcher = entityrendererprovider_context.getBlockRenderDispatcher();
   }

   public void render(FallingBlockEntity fallingblockentity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      BlockState blockstate = fallingblockentity.getBlockState();
      if (blockstate.getRenderShape() == RenderShape.MODEL) {
         Level level = fallingblockentity.level();
         if (blockstate != level.getBlockState(fallingblockentity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            posestack.pushPose();
            BlockPos blockpos = BlockPos.containing(fallingblockentity.getX(), fallingblockentity.getBoundingBox().maxY, fallingblockentity.getZ());
            posestack.translate(-0.5D, 0.0D, -0.5D);
            this.dispatcher.getModelRenderer().tesselateBlock(level, this.dispatcher.getBlockModel(blockstate), blockstate, blockpos, posestack, multibuffersource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockstate)), false, RandomSource.create(), blockstate.getSeed(fallingblockentity.getStartPos()), OverlayTexture.NO_OVERLAY);
            posestack.popPose();
            super.render(fallingblockentity, f, f1, posestack, multibuffersource, i);
         }
      }
   }

   public ResourceLocation getTextureLocation(FallingBlockEntity fallingblockentity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
