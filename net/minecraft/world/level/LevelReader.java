package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader extends BlockAndTintGetter, CollisionGetter, SignalGetter, BiomeManager.NoiseBiomeSource {
   @Nullable
   ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag);

   /** @deprecated */
   @Deprecated
   boolean hasChunk(int i, int j);

   int getHeight(Heightmap.Types heightmap_types, int i, int j);

   int getSkyDarken();

   BiomeManager getBiomeManager();

   default Holder<Biome> getBiome(BlockPos blockpos) {
      return this.getBiomeManager().getBiome(blockpos);
   }

   default Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
      int i = Mth.floor(aabb.minX);
      int j = Mth.floor(aabb.maxX);
      int k = Mth.floor(aabb.minY);
      int l = Mth.floor(aabb.maxY);
      int i1 = Mth.floor(aabb.minZ);
      int j1 = Mth.floor(aabb.maxZ);
      return this.hasChunksAt(i, k, i1, j, l, j1) ? this.getBlockStates(aabb) : Stream.empty();
   }

   default int getBlockTint(BlockPos blockpos, ColorResolver colorresolver) {
      return colorresolver.getColor(this.getBiome(blockpos).value(), (double)blockpos.getX(), (double)blockpos.getZ());
   }

   default Holder<Biome> getNoiseBiome(int i, int j, int k) {
      ChunkAccess chunkaccess = this.getChunk(QuartPos.toSection(i), QuartPos.toSection(k), ChunkStatus.BIOMES, false);
      return chunkaccess != null ? chunkaccess.getNoiseBiome(i, j, k) : this.getUncachedNoiseBiome(i, j, k);
   }

   Holder<Biome> getUncachedNoiseBiome(int i, int j, int k);

   boolean isClientSide();

   /** @deprecated */
   @Deprecated
   int getSeaLevel();

   DimensionType dimensionType();

   default int getMinBuildHeight() {
      return this.dimensionType().minY();
   }

   default int getHeight() {
      return this.dimensionType().height();
   }

   default BlockPos getHeightmapPos(Heightmap.Types heightmap_types, BlockPos blockpos) {
      return new BlockPos(blockpos.getX(), this.getHeight(heightmap_types, blockpos.getX(), blockpos.getZ()), blockpos.getZ());
   }

   default boolean isEmptyBlock(BlockPos blockpos) {
      return this.getBlockState(blockpos).isAir();
   }

   default boolean canSeeSkyFromBelowWater(BlockPos blockpos) {
      if (blockpos.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(blockpos);
      } else {
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), this.getSeaLevel(), blockpos.getZ());
         if (!this.canSeeSky(blockpos1)) {
            return false;
         } else {
            for(BlockPos var4 = blockpos1.below(); var4.getY() > blockpos.getY(); var4 = var4.below()) {
               BlockState blockstate = this.getBlockState(var4);
               if (blockstate.getLightBlock(this, var4) > 0 && !blockstate.liquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   default float getPathfindingCostFromLightLevels(BlockPos blockpos) {
      return this.getLightLevelDependentMagicValue(blockpos) - 0.5F;
   }

   /** @deprecated */
   @Deprecated
   default float getLightLevelDependentMagicValue(BlockPos blockpos) {
      float f = (float)this.getMaxLocalRawBrightness(blockpos) / 15.0F;
      float f1 = f / (4.0F - 3.0F * f);
      return Mth.lerp(this.dimensionType().ambientLight(), f1, 1.0F);
   }

   default ChunkAccess getChunk(BlockPos blockpos) {
      return this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
   }

   default ChunkAccess getChunk(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.FULL, true);
   }

   default ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus) {
      return this.getChunk(i, j, chunkstatus, true);
   }

   @Nullable
   default BlockGetter getChunkForCollisions(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.EMPTY, false);
   }

   default boolean isWaterAt(BlockPos blockpos) {
      return this.getFluidState(blockpos).is(FluidTags.WATER);
   }

   default boolean containsAnyLiquid(AABB aabb) {
      int i = Mth.floor(aabb.minX);
      int j = Mth.ceil(aabb.maxX);
      int k = Mth.floor(aabb.minY);
      int l = Mth.ceil(aabb.maxY);
      int i1 = Mth.floor(aabb.minZ);
      int j1 = Mth.ceil(aabb.maxZ);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               BlockState blockstate = this.getBlockState(blockpos_mutableblockpos.set(k1, l1, i2));
               if (!blockstate.getFluidState().isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   default int getMaxLocalRawBrightness(BlockPos blockpos) {
      return this.getMaxLocalRawBrightness(blockpos, this.getSkyDarken());
   }

   default int getMaxLocalRawBrightness(BlockPos blockpos, int i) {
      return blockpos.getX() >= -30000000 && blockpos.getZ() >= -30000000 && blockpos.getX() < 30000000 && blockpos.getZ() < 30000000 ? this.getRawBrightness(blockpos, i) : 15;
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunkAt(int i, int j) {
      return this.hasChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunkAt(BlockPos blockpos) {
      return this.hasChunkAt(blockpos.getX(), blockpos.getZ());
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunksAt(BlockPos blockpos, BlockPos blockpos1) {
      return this.hasChunksAt(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunksAt(int i, int j, int k, int l, int i1, int j1) {
      return i1 >= this.getMinBuildHeight() && j < this.getMaxBuildHeight() ? this.hasChunksAt(i, k, l, j1) : false;
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunksAt(int i, int j, int k, int l) {
      int i1 = SectionPos.blockToSectionCoord(i);
      int j1 = SectionPos.blockToSectionCoord(k);
      int k1 = SectionPos.blockToSectionCoord(j);
      int l1 = SectionPos.blockToSectionCoord(l);

      for(int i2 = i1; i2 <= j1; ++i2) {
         for(int j2 = k1; j2 <= l1; ++j2) {
            if (!this.hasChunk(i2, j2)) {
               return false;
            }
         }
      }

      return true;
   }

   RegistryAccess registryAccess();

   FeatureFlagSet enabledFeatures();

   default <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
      Registry<T> registry = this.registryAccess().registryOrThrow(resourcekey);
      return registry.asLookup().filterFeatures(this.enabledFeatures());
   }
}
