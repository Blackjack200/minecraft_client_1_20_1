package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk {
   private final Holder<Biome> biome;

   public EmptyLevelChunk(Level level, ChunkPos chunkpos, Holder<Biome> holder) {
      super(level, chunkpos);
      this.biome = holder;
   }

   public BlockState getBlockState(BlockPos blockpos) {
      return Blocks.VOID_AIR.defaultBlockState();
   }

   @Nullable
   public BlockState setBlockState(BlockPos blockpos, BlockState blockstate, boolean flag) {
      return null;
   }

   public FluidState getFluidState(BlockPos blockpos) {
      return Fluids.EMPTY.defaultFluidState();
   }

   public int getLightEmission(BlockPos blockpos) {
      return 0;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos, LevelChunk.EntityCreationType levelchunk_entitycreationtype) {
      return null;
   }

   public void addAndRegisterBlockEntity(BlockEntity blockentity) {
   }

   public void setBlockEntity(BlockEntity blockentity) {
   }

   public void removeBlockEntity(BlockPos blockpos) {
   }

   public boolean isEmpty() {
      return true;
   }

   public boolean isYSpaceEmpty(int i, int j) {
      return true;
   }

   public FullChunkStatus getFullStatus() {
      return FullChunkStatus.FULL;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      return this.biome;
   }
}
