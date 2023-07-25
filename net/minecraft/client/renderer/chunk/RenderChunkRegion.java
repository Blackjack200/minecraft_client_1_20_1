package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class RenderChunkRegion implements BlockAndTintGetter {
   private final int centerX;
   private final int centerZ;
   protected final RenderChunk[][] chunks;
   protected final Level level;

   RenderChunkRegion(Level level, int i, int j, RenderChunk[][] arenderchunk) {
      this.level = level;
      this.centerX = i;
      this.centerZ = j;
      this.chunks = arenderchunk;
   }

   public BlockState getBlockState(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX()) - this.centerX;
      int j = SectionPos.blockToSectionCoord(blockpos.getZ()) - this.centerZ;
      return this.chunks[i][j].getBlockState(blockpos);
   }

   public FluidState getFluidState(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX()) - this.centerX;
      int j = SectionPos.blockToSectionCoord(blockpos.getZ()) - this.centerZ;
      return this.chunks[i][j].getBlockState(blockpos).getFluidState();
   }

   public float getShade(Direction direction, boolean flag) {
      return this.level.getShade(direction, flag);
   }

   public LevelLightEngine getLightEngine() {
      return this.level.getLightEngine();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX()) - this.centerX;
      int j = SectionPos.blockToSectionCoord(blockpos.getZ()) - this.centerZ;
      return this.chunks[i][j].getBlockEntity(blockpos);
   }

   public int getBlockTint(BlockPos blockpos, ColorResolver colorresolver) {
      return this.level.getBlockTint(blockpos, colorresolver);
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }
}
