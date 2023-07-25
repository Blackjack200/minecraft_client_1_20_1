package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionBoxRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private double lastUpdateTime = Double.MIN_VALUE;
   private List<VoxelShape> shapes = Collections.emptyList();

   public CollisionBoxRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      double d3 = (double)Util.getNanos();
      if (d3 - this.lastUpdateTime > 1.0E8D) {
         this.lastUpdateTime = d3;
         Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
         this.shapes = ImmutableList.copyOf(entity.level().getCollisions(entity, entity.getBoundingBox().inflate(6.0D)));
      }

      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());

      for(VoxelShape voxelshape : this.shapes) {
         LevelRenderer.renderVoxelShape(posestack, vertexconsumer, voxelshape, -d0, -d1, -d2, 1.0F, 1.0F, 1.0F, 1.0F, true);
      }

   }
}
