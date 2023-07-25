package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

public class PistonHeadRenderer implements BlockEntityRenderer<PistonMovingBlockEntity> {
   private final BlockRenderDispatcher blockRenderer;

   public PistonHeadRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.blockRenderer = blockentityrendererprovider_context.getBlockRenderDispatcher();
   }

   public void render(PistonMovingBlockEntity pistonmovingblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Level level = pistonmovingblockentity.getLevel();
      if (level != null) {
         BlockPos blockpos = pistonmovingblockentity.getBlockPos().relative(pistonmovingblockentity.getMovementDirection().getOpposite());
         BlockState blockstate = pistonmovingblockentity.getMovedState();
         if (!blockstate.isAir()) {
            ModelBlockRenderer.enableCaching();
            posestack.pushPose();
            posestack.translate(pistonmovingblockentity.getXOff(f), pistonmovingblockentity.getYOff(f), pistonmovingblockentity.getZOff(f));
            if (blockstate.is(Blocks.PISTON_HEAD) && pistonmovingblockentity.getProgress(f) <= 4.0F) {
               blockstate = blockstate.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pistonmovingblockentity.getProgress(f) <= 0.5F));
               this.renderBlock(blockpos, blockstate, posestack, multibuffersource, level, false, j);
            } else if (pistonmovingblockentity.isSourcePiston() && !pistonmovingblockentity.isExtending()) {
               PistonType pistontype = blockstate.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
               BlockState blockstate1 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistontype).setValue(PistonHeadBlock.FACING, blockstate.getValue(PistonBaseBlock.FACING));
               blockstate1 = blockstate1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pistonmovingblockentity.getProgress(f) >= 0.5F));
               this.renderBlock(blockpos, blockstate1, posestack, multibuffersource, level, false, j);
               BlockPos blockpos1 = blockpos.relative(pistonmovingblockentity.getMovementDirection());
               posestack.popPose();
               posestack.pushPose();
               blockstate = blockstate.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
               this.renderBlock(blockpos1, blockstate, posestack, multibuffersource, level, true, j);
            } else {
               this.renderBlock(blockpos, blockstate, posestack, multibuffersource, level, false, j);
            }

            posestack.popPose();
            ModelBlockRenderer.clearCache();
         }
      }
   }

   private void renderBlock(BlockPos blockpos, BlockState blockstate, PoseStack posestack, MultiBufferSource multibuffersource, Level level, boolean flag, int i) {
      RenderType rendertype = ItemBlockRenderTypes.getMovingBlockRenderType(blockstate);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(rendertype);
      this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockstate), blockstate, blockpos, posestack, vertexconsumer, flag, RandomSource.create(), blockstate.getSeed(blockpos), i);
   }

   public int getViewDistance() {
      return 68;
   }
}
