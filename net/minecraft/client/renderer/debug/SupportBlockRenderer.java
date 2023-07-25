package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SupportBlockRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private double lastUpdateTime = Double.MIN_VALUE;
   private List<Entity> surroundEntities = Collections.emptyList();

   public SupportBlockRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      double d3 = (double)Util.getNanos();
      if (d3 - this.lastUpdateTime > 1.0E8D) {
         this.lastUpdateTime = d3;
         Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
         this.surroundEntities = ImmutableList.copyOf(entity.level().getEntities(entity, entity.getBoundingBox().inflate(16.0D)));
      }

      Player player = this.minecraft.player;
      if (player != null && player.mainSupportingBlockPos.isPresent()) {
         this.drawHighlights(posestack, multibuffersource, d0, d1, d2, player, () -> 0.0D, 1.0F, 0.0F, 0.0F);
      }

      for(Entity entity1 : this.surroundEntities) {
         if (entity1 != player) {
            this.drawHighlights(posestack, multibuffersource, d0, d1, d2, entity1, () -> this.getBias(entity1), 0.0F, 1.0F, 0.0F);
         }
      }

   }

   private void drawHighlights(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2, Entity entity, DoubleSupplier doublesupplier, float f, float f1, float f2) {
      entity.mainSupportingBlockPos.ifPresent((blockpos) -> {
         double d6 = doublesupplier.getAsDouble();
         BlockPos blockpos1 = entity.getOnPos();
         this.highlightPosition(blockpos1, posestack, d0, d1, d2, multibuffersource, 0.02D + d6, f, f1, f2);
         BlockPos blockpos2 = entity.getOnPosLegacy();
         if (!blockpos2.equals(blockpos1)) {
            this.highlightPosition(blockpos2, posestack, d0, d1, d2, multibuffersource, 0.04D + d6, 0.0F, 1.0F, 1.0F);
         }

      });
   }

   private double getBias(Entity entity) {
      return 0.02D * (double)(String.valueOf((double)entity.getId() + 0.132453657D).hashCode() % 1000) / 1000.0D;
   }

   private void highlightPosition(BlockPos blockpos, PoseStack posestack, double d0, double d1, double d2, MultiBufferSource multibuffersource, double d3, float f, float f1, float f2) {
      double d4 = (double)blockpos.getX() - d0 - 2.0D * d3;
      double d5 = (double)blockpos.getY() - d1 - 2.0D * d3;
      double d6 = (double)blockpos.getZ() - d2 - 2.0D * d3;
      double d7 = d4 + 1.0D + 4.0D * d3;
      double d8 = d5 + 1.0D + 4.0D * d3;
      double d9 = d6 + 1.0D + 4.0D * d3;
      LevelRenderer.renderLineBox(posestack, multibuffersource.getBuffer(RenderType.lines()), d4, d5, d6, d7, d8, d9, f, f1, f2, 0.4F);
      LevelRenderer.renderVoxelShape(posestack, multibuffersource.getBuffer(RenderType.lines()), this.minecraft.level.getBlockState(blockpos).getCollisionShape(this.minecraft.level, blockpos, CollisionContext.empty()).move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()), -d0, -d1, -d2, f, f1, f2, 1.0F, false);
   }
}
