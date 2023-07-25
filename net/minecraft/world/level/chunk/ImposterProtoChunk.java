package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class ImposterProtoChunk extends ProtoChunk {
   private final LevelChunk wrapped;
   private final boolean allowWrites;

   public ImposterProtoChunk(LevelChunk levelchunk, boolean flag) {
      super(levelchunk.getPos(), UpgradeData.EMPTY, levelchunk.levelHeightAccessor, levelchunk.getLevel().registryAccess().registryOrThrow(Registries.BIOME), levelchunk.getBlendingData());
      this.wrapped = levelchunk;
      this.allowWrites = flag;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      return this.wrapped.getBlockEntity(blockpos);
   }

   public BlockState getBlockState(BlockPos blockpos) {
      return this.wrapped.getBlockState(blockpos);
   }

   public FluidState getFluidState(BlockPos blockpos) {
      return this.wrapped.getFluidState(blockpos);
   }

   public int getMaxLightLevel() {
      return this.wrapped.getMaxLightLevel();
   }

   public LevelChunkSection getSection(int i) {
      return this.allowWrites ? this.wrapped.getSection(i) : super.getSection(i);
   }

   @Nullable
   public BlockState setBlockState(BlockPos blockpos, BlockState blockstate, boolean flag) {
      return this.allowWrites ? this.wrapped.setBlockState(blockpos, blockstate, flag) : null;
   }

   public void setBlockEntity(BlockEntity blockentity) {
      if (this.allowWrites) {
         this.wrapped.setBlockEntity(blockentity);
      }

   }

   public void addEntity(Entity entity) {
      if (this.allowWrites) {
         this.wrapped.addEntity(entity);
      }

   }

   public void setStatus(ChunkStatus chunkstatus) {
      if (this.allowWrites) {
         super.setStatus(chunkstatus);
      }

   }

   public LevelChunkSection[] getSections() {
      return this.wrapped.getSections();
   }

   public void setHeightmap(Heightmap.Types heightmap_types, long[] along) {
   }

   private Heightmap.Types fixType(Heightmap.Types heightmap_types) {
      if (heightmap_types == Heightmap.Types.WORLD_SURFACE_WG) {
         return Heightmap.Types.WORLD_SURFACE;
      } else {
         return heightmap_types == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : heightmap_types;
      }
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types heightmap_types) {
      return this.wrapped.getOrCreateHeightmapUnprimed(heightmap_types);
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      return this.wrapped.getHeight(this.fixType(heightmap_types), i, j);
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      return this.wrapped.getNoiseBiome(i, j, k);
   }

   public ChunkPos getPos() {
      return this.wrapped.getPos();
   }

   @Nullable
   public StructureStart getStartForStructure(Structure structure) {
      return this.wrapped.getStartForStructure(structure);
   }

   public void setStartForStructure(Structure structure, StructureStart structurestart) {
   }

   public Map<Structure, StructureStart> getAllStarts() {
      return this.wrapped.getAllStarts();
   }

   public void setAllStarts(Map<Structure, StructureStart> map) {
   }

   public LongSet getReferencesForStructure(Structure structure) {
      return this.wrapped.getReferencesForStructure(structure);
   }

   public void addReferenceForStructure(Structure structure, long i) {
   }

   public Map<Structure, LongSet> getAllReferences() {
      return this.wrapped.getAllReferences();
   }

   public void setAllReferences(Map<Structure, LongSet> map) {
   }

   public void setUnsaved(boolean flag) {
      this.wrapped.setUnsaved(flag);
   }

   public boolean isUnsaved() {
      return false;
   }

   public ChunkStatus getStatus() {
      return this.wrapped.getStatus();
   }

   public void removeBlockEntity(BlockPos blockpos) {
   }

   public void markPosForPostprocessing(BlockPos blockpos) {
   }

   public void setBlockEntityNbt(CompoundTag compoundtag) {
   }

   @Nullable
   public CompoundTag getBlockEntityNbt(BlockPos blockpos) {
      return this.wrapped.getBlockEntityNbt(blockpos);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos blockpos) {
      return this.wrapped.getBlockEntityNbtForSaving(blockpos);
   }

   public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biconsumer) {
      this.wrapped.findBlocks(predicate, biconsumer);
   }

   public TickContainerAccess<Block> getBlockTicks() {
      return this.allowWrites ? this.wrapped.getBlockTicks() : BlackholeTickAccess.emptyContainer();
   }

   public TickContainerAccess<Fluid> getFluidTicks() {
      return this.allowWrites ? this.wrapped.getFluidTicks() : BlackholeTickAccess.emptyContainer();
   }

   public ChunkAccess.TicksToSave getTicksForSerialization() {
      return this.wrapped.getTicksForSerialization();
   }

   @Nullable
   public BlendingData getBlendingData() {
      return this.wrapped.getBlendingData();
   }

   public void setBlendingData(BlendingData blendingdata) {
      this.wrapped.setBlendingData(blendingdata);
   }

   public CarvingMask getCarvingMask(GenerationStep.Carving generationstep_carving) {
      if (this.allowWrites) {
         return super.getCarvingMask(generationstep_carving);
      } else {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
      }
   }

   public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving generationstep_carving) {
      if (this.allowWrites) {
         return super.getOrCreateCarvingMask(generationstep_carving);
      } else {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
      }
   }

   public LevelChunk getWrapped() {
      return this.wrapped;
   }

   public boolean isLightCorrect() {
      return this.wrapped.isLightCorrect();
   }

   public void setLightCorrect(boolean flag) {
      this.wrapped.setLightCorrect(flag);
   }

   public void fillBiomesFromNoise(BiomeResolver biomeresolver, Climate.Sampler climate_sampler) {
      if (this.allowWrites) {
         this.wrapped.fillBiomesFromNoise(biomeresolver, climate_sampler);
      }

   }

   public void initializeLightSources() {
      this.wrapped.initializeLightSources();
   }

   public ChunkSkyLightSources getSkyLightSources() {
      return this.wrapped.getSkyLightSources();
   }
}
