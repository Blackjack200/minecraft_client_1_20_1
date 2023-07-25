package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public class WaterDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;

   public WaterDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      BlockPos blockpos = this.minecraft.player.blockPosition();
      LevelReader levelreader = this.minecraft.player.level();

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-10, -10, -10), blockpos.offset(10, 10, 10))) {
         FluidState fluidstate = levelreader.getFluidState(blockpos1);
         if (fluidstate.is(FluidTags.WATER)) {
            double d3 = (double)((float)blockpos1.getY() + fluidstate.getHeight(levelreader, blockpos1));
            DebugRenderer.renderFilledBox(posestack, multibuffersource, (new AABB((double)((float)blockpos1.getX() + 0.01F), (double)((float)blockpos1.getY() + 0.01F), (double)((float)blockpos1.getZ() + 0.01F), (double)((float)blockpos1.getX() + 0.99F), d3, (double)((float)blockpos1.getZ() + 0.99F))).move(-d0, -d1, -d2), 0.0F, 1.0F, 0.0F, 0.15F);
         }
      }

      for(BlockPos blockpos2 : BlockPos.betweenClosed(blockpos.offset(-10, -10, -10), blockpos.offset(10, 10, 10))) {
         FluidState fluidstate1 = levelreader.getFluidState(blockpos2);
         if (fluidstate1.is(FluidTags.WATER)) {
            DebugRenderer.renderFloatingText(posestack, multibuffersource, String.valueOf(fluidstate1.getAmount()), (double)blockpos2.getX() + 0.5D, (double)((float)blockpos2.getY() + fluidstate1.getHeight(levelreader, blockpos2)), (double)blockpos2.getZ() + 0.5D, -16777216);
         }
      }

   }
}
