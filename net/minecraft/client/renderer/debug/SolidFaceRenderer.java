package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class SolidFaceRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;

   public SolidFaceRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Matrix4f matrix4f = posestack.last().pose();
      BlockGetter blockgetter = this.minecraft.player.level();
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-6, -6, -6), blockpos.offset(6, 6, 6))) {
         BlockState blockstate = blockgetter.getBlockState(blockpos1);
         if (!blockstate.is(Blocks.AIR)) {
            VoxelShape voxelshape = blockstate.getShape(blockgetter, blockpos1);

            for(AABB aabb : voxelshape.toAabbs()) {
               AABB aabb1 = aabb.move(blockpos1).inflate(0.002D);
               float f = (float)(aabb1.minX - d0);
               float f1 = (float)(aabb1.minY - d1);
               float f2 = (float)(aabb1.minZ - d2);
               float f3 = (float)(aabb1.maxX - d0);
               float f4 = (float)(aabb1.maxY - d1);
               float f5 = (float)(aabb1.maxZ - d2);
               float f6 = 1.0F;
               float f7 = 0.0F;
               float f8 = 0.0F;
               float f9 = 0.5F;
               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.WEST)) {
                  VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer.vertex(matrix4f, f, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer.vertex(matrix4f, f, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer.vertex(matrix4f, f, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer.vertex(matrix4f, f, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }

               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH)) {
                  VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer1.vertex(matrix4f, f, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer1.vertex(matrix4f, f, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer1.vertex(matrix4f, f3, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer1.vertex(matrix4f, f3, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }

               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.EAST)) {
                  VertexConsumer vertexconsumer2 = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer2.vertex(matrix4f, f3, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer2.vertex(matrix4f, f3, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer2.vertex(matrix4f, f3, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer2.vertex(matrix4f, f3, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }

               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.NORTH)) {
                  VertexConsumer vertexconsumer3 = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer3.vertex(matrix4f, f3, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer3.vertex(matrix4f, f3, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer3.vertex(matrix4f, f, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer3.vertex(matrix4f, f, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }

               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.DOWN)) {
                  VertexConsumer vertexconsumer4 = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer4.vertex(matrix4f, f, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer4.vertex(matrix4f, f3, f1, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer4.vertex(matrix4f, f, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer4.vertex(matrix4f, f3, f1, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }

               if (blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.UP)) {
                  VertexConsumer vertexconsumer5 = multibuffersource.getBuffer(RenderType.debugFilledBox());
                  vertexconsumer5.vertex(matrix4f, f, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer5.vertex(matrix4f, f, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer5.vertex(matrix4f, f3, f4, f2).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                  vertexconsumer5.vertex(matrix4f, f3, f4, f5).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
               }
            }
         }
      }

   }
}
