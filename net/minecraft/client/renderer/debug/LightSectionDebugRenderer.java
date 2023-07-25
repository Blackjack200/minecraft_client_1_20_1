package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class LightSectionDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final Duration REFRESH_INTERVAL = Duration.ofMillis(500L);
   private static final int RADIUS = 10;
   private static final Vector4f LIGHT_AND_BLOCKS_COLOR = new Vector4f(1.0F, 1.0F, 0.0F, 0.25F);
   private static final Vector4f LIGHT_ONLY_COLOR = new Vector4f(0.25F, 0.125F, 0.0F, 0.125F);
   private final Minecraft minecraft;
   private final LightLayer lightLayer;
   private Instant lastUpdateTime = Instant.now();
   @Nullable
   private LightSectionDebugRenderer.SectionData data;

   public LightSectionDebugRenderer(Minecraft minecraft, LightLayer lightlayer) {
      this.minecraft = minecraft;
      this.lightLayer = lightlayer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Instant instant = Instant.now();
      if (this.data == null || Duration.between(this.lastUpdateTime, instant).compareTo(REFRESH_INTERVAL) > 0) {
         this.lastUpdateTime = instant;
         this.data = new LightSectionDebugRenderer.SectionData(this.minecraft.level.getLightEngine(), SectionPos.of(this.minecraft.player.blockPosition()), 10, this.lightLayer);
      }

      renderEdges(posestack, this.data.lightAndBlocksShape, this.data.minPos, multibuffersource, d0, d1, d2, LIGHT_AND_BLOCKS_COLOR);
      renderEdges(posestack, this.data.lightShape, this.data.minPos, multibuffersource, d0, d1, d2, LIGHT_ONLY_COLOR);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugSectionQuads());
      renderFaces(posestack, this.data.lightAndBlocksShape, this.data.minPos, vertexconsumer, d0, d1, d2, LIGHT_AND_BLOCKS_COLOR);
      renderFaces(posestack, this.data.lightShape, this.data.minPos, vertexconsumer, d0, d1, d2, LIGHT_ONLY_COLOR);
   }

   private static void renderFaces(PoseStack posestack, DiscreteVoxelShape discretevoxelshape, SectionPos sectionpos, VertexConsumer vertexconsumer, double d0, double d1, double d2, Vector4f vector4f) {
      discretevoxelshape.forAllFaces((direction, i, j, k) -> {
         int l = i + sectionpos.getX();
         int i1 = j + sectionpos.getY();
         int j1 = k + sectionpos.getZ();
         renderFace(posestack, vertexconsumer, direction, d0, d1, d2, l, i1, j1, vector4f);
      });
   }

   private static void renderEdges(PoseStack posestack, DiscreteVoxelShape discretevoxelshape, SectionPos sectionpos, MultiBufferSource multibuffersource, double d0, double d1, double d2, Vector4f vector4f) {
      discretevoxelshape.forAllEdges((i, j, k, l, i1, j1) -> {
         int k1 = i + sectionpos.getX();
         int l1 = j + sectionpos.getY();
         int i2 = k + sectionpos.getZ();
         int j2 = l + sectionpos.getX();
         int k2 = i1 + sectionpos.getY();
         int l2 = j1 + sectionpos.getZ();
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugLineStrip(1.0D));
         renderEdge(posestack, vertexconsumer, d0, d1, d2, k1, l1, i2, j2, k2, l2, vector4f);
      }, true);
   }

   private static void renderFace(PoseStack posestack, VertexConsumer vertexconsumer, Direction direction, double d0, double d1, double d2, int i, int j, int k, Vector4f vector4f) {
      float f = (float)((double)SectionPos.sectionToBlockCoord(i) - d0);
      float f1 = (float)((double)SectionPos.sectionToBlockCoord(j) - d1);
      float f2 = (float)((double)SectionPos.sectionToBlockCoord(k) - d2);
      float f3 = f + 16.0F;
      float f4 = f1 + 16.0F;
      float f5 = f2 + 16.0F;
      float f6 = vector4f.x();
      float f7 = vector4f.y();
      float f8 = vector4f.z();
      float f9 = vector4f.w();
      Matrix4f matrix4f = posestack.last().pose();
      switch (direction) {
         case DOWN:
            vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
            break;
         case UP:
            vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
            break;
         case NORTH:
            vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
            break;
         case SOUTH:
            vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
            break;
         case WEST:
            vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
            break;
         case EAST:
            vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
            vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
      }

   }

   private static void renderEdge(PoseStack posestack, VertexConsumer vertexconsumer, double d0, double d1, double d2, int i, int j, int k, int l, int i1, int j1, Vector4f vector4f) {
      float f = (float)((double)SectionPos.sectionToBlockCoord(i) - d0);
      float f1 = (float)((double)SectionPos.sectionToBlockCoord(j) - d1);
      float f2 = (float)((double)SectionPos.sectionToBlockCoord(k) - d2);
      float f3 = (float)((double)SectionPos.sectionToBlockCoord(l) - d0);
      float f4 = (float)((double)SectionPos.sectionToBlockCoord(i1) - d1);
      float f5 = (float)((double)SectionPos.sectionToBlockCoord(j1) - d2);
      Matrix4f matrix4f = posestack.last().pose();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(vector4f.x(), vector4f.y(), vector4f.z(), 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(vector4f.x(), vector4f.y(), vector4f.z(), 1.0F).endVertex();
   }

   static final class SectionData {
      final DiscreteVoxelShape lightAndBlocksShape;
      final DiscreteVoxelShape lightShape;
      final SectionPos minPos;

      SectionData(LevelLightEngine levellightengine, SectionPos sectionpos, int i, LightLayer lightlayer) {
         int j = i * 2 + 1;
         this.lightAndBlocksShape = new BitSetDiscreteVoxelShape(j, j, j);
         this.lightShape = new BitSetDiscreteVoxelShape(j, j, j);

         for(int k = 0; k < j; ++k) {
            for(int l = 0; l < j; ++l) {
               for(int i1 = 0; i1 < j; ++i1) {
                  SectionPos sectionpos1 = SectionPos.of(sectionpos.x() + i1 - i, sectionpos.y() + l - i, sectionpos.z() + k - i);
                  LayerLightSectionStorage.SectionType layerlightsectionstorage_sectiontype = levellightengine.getDebugSectionType(lightlayer, sectionpos1);
                  if (layerlightsectionstorage_sectiontype == LayerLightSectionStorage.SectionType.LIGHT_AND_DATA) {
                     this.lightAndBlocksShape.fill(i1, l, k);
                     this.lightShape.fill(i1, l, k);
                  } else if (layerlightsectionstorage_sectiontype == LayerLightSectionStorage.SectionType.LIGHT_ONLY) {
                     this.lightShape.fill(i1, l, k);
                  }
               }
            }
         }

         this.minPos = SectionPos.of(sectionpos.x() - i, sectionpos.y() - i, sectionpos.z() - i);
      }
   }
}
