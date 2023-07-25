package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
   public StructureBlockRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
   }

   public void render(StructureBlockEntity structureblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
         BlockPos blockpos = structureblockentity.getStructurePos();
         Vec3i vec3i = structureblockentity.getStructureSize();
         if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
            if (structureblockentity.getMode() == StructureMode.SAVE || structureblockentity.getMode() == StructureMode.LOAD) {
               double d0 = (double)blockpos.getX();
               double d1 = (double)blockpos.getZ();
               double d2 = (double)blockpos.getY();
               double d3 = d2 + (double)vec3i.getY();
               double d4;
               double d5;
               switch (structureblockentity.getMirror()) {
                  case LEFT_RIGHT:
                     d4 = (double)vec3i.getX();
                     d5 = (double)(-vec3i.getZ());
                     break;
                  case FRONT_BACK:
                     d4 = (double)(-vec3i.getX());
                     d5 = (double)vec3i.getZ();
                     break;
                  default:
                     d4 = (double)vec3i.getX();
                     d5 = (double)vec3i.getZ();
               }

               double d22;
               double d23;
               double d24;
               double d25;
               switch (structureblockentity.getRotation()) {
                  case CLOCKWISE_90:
                     d22 = d5 < 0.0D ? d0 : d0 + 1.0D;
                     d23 = d4 < 0.0D ? d1 + 1.0D : d1;
                     d24 = d22 - d5;
                     d25 = d23 + d4;
                     break;
                  case CLOCKWISE_180:
                     d22 = d4 < 0.0D ? d0 : d0 + 1.0D;
                     d23 = d5 < 0.0D ? d1 : d1 + 1.0D;
                     d24 = d22 - d4;
                     d25 = d23 - d5;
                     break;
                  case COUNTERCLOCKWISE_90:
                     d22 = d5 < 0.0D ? d0 + 1.0D : d0;
                     d23 = d4 < 0.0D ? d1 : d1 + 1.0D;
                     d24 = d22 + d5;
                     d25 = d23 - d4;
                     break;
                  default:
                     d22 = d4 < 0.0D ? d0 + 1.0D : d0;
                     d23 = d5 < 0.0D ? d1 + 1.0D : d1;
                     d24 = d22 + d4;
                     d25 = d23 + d5;
               }

               float f1 = 1.0F;
               float f2 = 0.9F;
               float f3 = 0.5F;
               VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());
               if (structureblockentity.getMode() == StructureMode.SAVE || structureblockentity.getShowBoundingBox()) {
                  LevelRenderer.renderLineBox(posestack, vertexconsumer, d22, d2, d23, d24, d3, d25, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
               }

               if (structureblockentity.getMode() == StructureMode.SAVE && structureblockentity.getShowAir()) {
                  this.renderInvisibleBlocks(structureblockentity, vertexconsumer, blockpos, posestack);
               }

            }
         }
      }
   }

   private void renderInvisibleBlocks(StructureBlockEntity structureblockentity, VertexConsumer vertexconsumer, BlockPos blockpos, PoseStack posestack) {
      BlockGetter blockgetter = structureblockentity.getLevel();
      BlockPos blockpos1 = structureblockentity.getBlockPos();
      BlockPos blockpos2 = blockpos1.offset(blockpos);

      for(BlockPos blockpos3 : BlockPos.betweenClosed(blockpos2, blockpos2.offset(structureblockentity.getStructureSize()).offset(-1, -1, -1))) {
         BlockState blockstate = blockgetter.getBlockState(blockpos3);
         boolean flag = blockstate.isAir();
         boolean flag1 = blockstate.is(Blocks.STRUCTURE_VOID);
         boolean flag2 = blockstate.is(Blocks.BARRIER);
         boolean flag3 = blockstate.is(Blocks.LIGHT);
         boolean flag4 = flag1 || flag2 || flag3;
         if (flag || flag4) {
            float f = flag ? 0.05F : 0.0F;
            double d0 = (double)((float)(blockpos3.getX() - blockpos1.getX()) + 0.45F - f);
            double d1 = (double)((float)(blockpos3.getY() - blockpos1.getY()) + 0.45F - f);
            double d2 = (double)((float)(blockpos3.getZ() - blockpos1.getZ()) + 0.45F - f);
            double d3 = (double)((float)(blockpos3.getX() - blockpos1.getX()) + 0.55F + f);
            double d4 = (double)((float)(blockpos3.getY() - blockpos1.getY()) + 0.55F + f);
            double d5 = (double)((float)(blockpos3.getZ() - blockpos1.getZ()) + 0.55F + f);
            if (flag) {
               LevelRenderer.renderLineBox(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
            } else if (flag1) {
               LevelRenderer.renderLineBox(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
            } else if (flag2) {
               LevelRenderer.renderLineBox(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
            } else if (flag3) {
               LevelRenderer.renderLineBox(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
            }
         }
      }

   }

   public boolean shouldRenderOffScreen(StructureBlockEntity structureblockentity) {
      return true;
   }

   public int getViewDistance() {
      return 96;
   }
}
