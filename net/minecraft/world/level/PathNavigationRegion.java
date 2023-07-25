package net.minecraft.world.level;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements BlockGetter, CollisionGetter {
   protected final int centerX;
   protected final int centerZ;
   protected final ChunkAccess[][] chunks;
   protected boolean allEmpty;
   protected final Level level;
   private final Supplier<Holder<Biome>> plains;

   public PathNavigationRegion(Level level, BlockPos blockpos, BlockPos blockpos1) {
      this.level = level;
      this.plains = Suppliers.memoize(() -> level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
      this.centerX = SectionPos.blockToSectionCoord(blockpos.getX());
      this.centerZ = SectionPos.blockToSectionCoord(blockpos.getZ());
      int i = SectionPos.blockToSectionCoord(blockpos1.getX());
      int j = SectionPos.blockToSectionCoord(blockpos1.getZ());
      this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
      ChunkSource chunksource = level.getChunkSource();
      this.allEmpty = true;

      for(int k = this.centerX; k <= i; ++k) {
         for(int l = this.centerZ; l <= j; ++l) {
            this.chunks[k - this.centerX][l - this.centerZ] = chunksource.getChunkNow(k, l);
         }
      }

      for(int i1 = SectionPos.blockToSectionCoord(blockpos.getX()); i1 <= SectionPos.blockToSectionCoord(blockpos1.getX()); ++i1) {
         for(int j1 = SectionPos.blockToSectionCoord(blockpos.getZ()); j1 <= SectionPos.blockToSectionCoord(blockpos1.getZ()); ++j1) {
            ChunkAccess chunkaccess = this.chunks[i1 - this.centerX][j1 - this.centerZ];
            if (chunkaccess != null && !chunkaccess.isYSpaceEmpty(blockpos.getY(), blockpos1.getY())) {
               this.allEmpty = false;
               return;
            }
         }
      }

   }

   private ChunkAccess getChunk(BlockPos blockpos) {
      return this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
   }

   private ChunkAccess getChunk(int i, int j) {
      int k = i - this.centerX;
      int l = j - this.centerZ;
      if (k >= 0 && k < this.chunks.length && l >= 0 && l < this.chunks[k].length) {
         ChunkAccess chunkaccess = this.chunks[k][l];
         return (ChunkAccess)(chunkaccess != null ? chunkaccess : new EmptyLevelChunk(this.level, new ChunkPos(i, j), this.plains.get()));
      } else {
         return new EmptyLevelChunk(this.level, new ChunkPos(i, j), this.plains.get());
      }
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public BlockGetter getChunkForCollisions(int i, int j) {
      return this.getChunk(i, j);
   }

   public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb) {
      return List.of();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      ChunkAccess chunkaccess = this.getChunk(blockpos);
      return chunkaccess.getBlockEntity(blockpos);
   }

   public BlockState getBlockState(BlockPos blockpos) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         ChunkAccess chunkaccess = this.getChunk(blockpos);
         return chunkaccess.getBlockState(blockpos);
      }
   }

   public FluidState getFluidState(BlockPos blockpos) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         ChunkAccess chunkaccess = this.getChunk(blockpos);
         return chunkaccess.getFluidState(blockpos);
      }
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public ProfilerFiller getProfiler() {
      return this.level.getProfiler();
   }
}
