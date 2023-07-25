package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public class RaidDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private static final float TEXT_SCALE = 0.04F;
   private final Minecraft minecraft;
   private Collection<BlockPos> raidCenters = Lists.newArrayList();

   public RaidDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void setRaidCenters(Collection<BlockPos> collection) {
      this.raidCenters = collection;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      BlockPos blockpos = this.getCamera().getBlockPosition();

      for(BlockPos blockpos1 : this.raidCenters) {
         if (blockpos.closerThan(blockpos1, 160.0D)) {
            highlightRaidCenter(posestack, multibuffersource, blockpos1);
         }
      }

   }

   private static void highlightRaidCenter(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos) {
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos.offset(-1, -1, -1), blockpos.offset(1, 1, 1), 1.0F, 0.0F, 0.0F, 0.15F);
      int i = -65536;
      renderTextOverBlock(posestack, multibuffersource, "Raid center", blockpos, -65536);
   }

   private static void renderTextOverBlock(PoseStack posestack, MultiBufferSource multibuffersource, String s, BlockPos blockpos, int i) {
      double d0 = (double)blockpos.getX() + 0.5D;
      double d1 = (double)blockpos.getY() + 1.3D;
      double d2 = (double)blockpos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(posestack, multibuffersource, s, d0, d1, d2, i, 0.04F, true, 0.0F, true);
   }

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }
}
