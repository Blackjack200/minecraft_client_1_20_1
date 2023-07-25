package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
   private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

   public ChunkBorderRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
      float f = (float)((double)this.minecraft.level.getMinBuildHeight() - d1);
      float f1 = (float)((double)this.minecraft.level.getMaxBuildHeight() - d1);
      ChunkPos chunkpos = entity.chunkPosition();
      float f2 = (float)((double)chunkpos.getMinBlockX() - d0);
      float f3 = (float)((double)chunkpos.getMinBlockZ() - d2);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugLineStrip(1.0D));
      Matrix4f matrix4f = posestack.last().pose();

      for(int i = -16; i <= 32; i += 16) {
         for(int j = -16; j <= 32; j += 16) {
            vertexconsumer.vertex(matrix4f, f2 + (float)i, f, f3 + (float)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i, f, f3 + (float)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i, f1, f3 + (float)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i, f1, f3 + (float)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
         }
      }

      for(int k = 2; k < 16; k += 2) {
         int l = k % 4 == 0 ? CELL_BORDER : YELLOW;
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f, f3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f, f3).color(l).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f1, f3).color(l).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f1, f3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f, f3 + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f, f3 + 16.0F).color(l).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f1, f3 + 16.0F).color(l).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + (float)k, f1, f3 + 16.0F).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int i1 = 2; i1 < 16; i1 += 2) {
         int j1 = i1 % 4 == 0 ? CELL_BORDER : YELLOW;
         vertexconsumer.vertex(matrix4f, f2, f, f3 + (float)i1).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f, f3 + (float)i1).color(j1).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f1, f3 + (float)i1).color(j1).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f1, f3 + (float)i1).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f, f3 + (float)i1).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f, f3 + (float)i1).color(j1).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f1, f3 + (float)i1).color(j1).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f1, f3 + (float)i1).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int k1 = this.minecraft.level.getMinBuildHeight(); k1 <= this.minecraft.level.getMaxBuildHeight(); k1 += 2) {
         float f4 = (float)((double)k1 - d1);
         int l1 = k1 % 8 == 0 ? CELL_BORDER : YELLOW;
         vertexconsumer.vertex(matrix4f, f2, f4, f3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f4, f3).color(l1).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f4, f3 + 16.0F).color(l1).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f4, f3 + 16.0F).color(l1).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f4, f3).color(l1).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f4, f3).color(l1).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f4, f3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      vertexconsumer = multibuffersource.getBuffer(RenderType.debugLineStrip(2.0D));

      for(int i2 = 0; i2 <= 16; i2 += 16) {
         for(int j2 = 0; j2 <= 16; j2 += 16) {
            vertexconsumer.vertex(matrix4f, f2 + (float)i2, f, f3 + (float)j2).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i2, f, f3 + (float)j2).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i2, f1, f3 + (float)j2).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            vertexconsumer.vertex(matrix4f, f2 + (float)i2, f1, f3 + (float)j2).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         }
      }

      for(int k2 = this.minecraft.level.getMinBuildHeight(); k2 <= this.minecraft.level.getMaxBuildHeight(); k2 += 16) {
         float f5 = (float)((double)k2 - d1);
         vertexconsumer.vertex(matrix4f, f2, f5, f3).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f5, f3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f5, f3 + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f5, f3 + 16.0F).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2 + 16.0F, f5, f3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f5, f3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f2, f5, f3).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
      }

   }
}
