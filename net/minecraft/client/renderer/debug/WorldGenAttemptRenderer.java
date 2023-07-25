package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

public class WorldGenAttemptRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final List<BlockPos> toRender = Lists.newArrayList();
   private final List<Float> scales = Lists.newArrayList();
   private final List<Float> alphas = Lists.newArrayList();
   private final List<Float> reds = Lists.newArrayList();
   private final List<Float> greens = Lists.newArrayList();
   private final List<Float> blues = Lists.newArrayList();

   public void addPos(BlockPos blockpos, float f, float f1, float f2, float f3, float f4) {
      this.toRender.add(blockpos);
      this.scales.add(f);
      this.alphas.add(f4);
      this.reds.add(f1);
      this.greens.add(f2);
      this.blues.add(f3);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugFilledBox());

      for(int i = 0; i < this.toRender.size(); ++i) {
         BlockPos blockpos = this.toRender.get(i);
         Float ofloat = this.scales.get(i);
         float f = ofloat / 2.0F;
         LevelRenderer.addChainedFilledBoxVertices(posestack, vertexconsumer, (double)((float)blockpos.getX() + 0.5F - f) - d0, (double)((float)blockpos.getY() + 0.5F - f) - d1, (double)((float)blockpos.getZ() + 0.5F - f) - d2, (double)((float)blockpos.getX() + 0.5F + f) - d0, (double)((float)blockpos.getY() + 0.5F + f) - d1, (double)((float)blockpos.getZ() + 0.5F + f) - d2, this.reds.get(i), this.greens.get(i), this.blues.get(i), this.alphas.get(i));
      }

   }
}
