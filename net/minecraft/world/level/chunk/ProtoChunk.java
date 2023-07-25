package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk extends ChunkAccess {
   @Nullable
   private volatile LevelLightEngine lightEngine;
   private volatile ChunkStatus status = ChunkStatus.EMPTY;
   private final List<CompoundTag> entities = Lists.newArrayList();
   private final Map<GenerationStep.Carving, CarvingMask> carvingMasks = new Object2ObjectArrayMap<>();
   @Nullable
   private BelowZeroRetrogen belowZeroRetrogen;
   private final ProtoChunkTicks<Block> blockTicks;
   private final ProtoChunkTicks<Fluid> fluidTicks;

   public ProtoChunk(ChunkPos chunkpos, UpgradeData upgradedata, LevelHeightAccessor levelheightaccessor, Registry<Biome> registry, @Nullable BlendingData blendingdata) {
      this(chunkpos, upgradedata, (LevelChunkSection[])null, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), levelheightaccessor, registry, blendingdata);
   }

   public ProtoChunk(ChunkPos chunkpos, UpgradeData upgradedata, @Nullable LevelChunkSection[] alevelchunksection, ProtoChunkTicks<Block> protochunkticks, ProtoChunkTicks<Fluid> protochunkticks1, LevelHeightAccessor levelheightaccessor, Registry<Biome> registry, @Nullable BlendingData blendingdata) {
      super(chunkpos, upgradedata, levelheightaccessor, registry, 0L, alevelchunksection, blendingdata);
      this.blockTicks = protochunkticks;
      this.fluidTicks = protochunkticks1;
   }

   public TickContainerAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public TickContainerAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   public ChunkAccess.TicksToSave getTicksForSerialization() {
      return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
   }

   public BlockState getBlockState(BlockPos blockpos) {
      int i = blockpos.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndex(i));
         return levelchunksection.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : levelchunksection.getBlockState(blockpos.getX() & 15, i & 15, blockpos.getZ() & 15);
      }
   }

   public FluidState getFluidState(BlockPos blockpos) {
      int i = blockpos.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndex(i));
         return levelchunksection.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : levelchunksection.getFluidState(blockpos.getX() & 15, i & 15, blockpos.getZ() & 15);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos blockpos, BlockState blockstate, boolean flag) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      if (j >= this.getMinBuildHeight() && j < this.getMaxBuildHeight()) {
         int l = this.getSectionIndex(j);
         LevelChunkSection levelchunksection = this.getSection(l);
         boolean flag1 = levelchunksection.hasOnlyAir();
         if (flag1 && blockstate.is(Blocks.AIR)) {
            return blockstate;
         } else {
            int i1 = SectionPos.sectionRelative(i);
            int j1 = SectionPos.sectionRelative(j);
            int k1 = SectionPos.sectionRelative(k);
            BlockState blockstate1 = levelchunksection.setBlockState(i1, j1, k1, blockstate);
            if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
               boolean flag2 = levelchunksection.hasOnlyAir();
               if (flag2 != flag1) {
                  this.lightEngine.updateSectionStatus(blockpos, flag2);
               }

               if (LightEngine.hasDifferentLightProperties(this, blockpos, blockstate1, blockstate)) {
                  this.skyLightSources.update(this, i1, j, k1);
                  this.lightEngine.checkBlock(blockpos);
               }
            }

            EnumSet<Heightmap.Types> enumset = this.getStatus().heightmapsAfter();
            EnumSet<Heightmap.Types> enumset1 = null;

            for(Heightmap.Types heightmap_types : enumset) {
               Heightmap heightmap = this.heightmaps.get(heightmap_types);
               if (heightmap == null) {
                  if (enumset1 == null) {
                     enumset1 = EnumSet.noneOf(Heightmap.Types.class);
                  }

                  enumset1.add(heightmap_types);
               }
            }

            if (enumset1 != null) {
               Heightmap.primeHeightmaps(this, enumset1);
            }

            for(Heightmap.Types heightmap_types1 : enumset) {
               this.heightmaps.get(heightmap_types1).update(i1, j, k1, blockstate);
            }

            return blockstate1;
         }
      } else {
         return Blocks.VOID_AIR.defaultBlockState();
      }
   }

   public void setBlockEntity(BlockEntity blockentity) {
      this.blockEntities.put(blockentity.getBlockPos(), blockentity);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      return this.blockEntities.get(blockpos);
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(CompoundTag compoundtag) {
      this.entities.add(compoundtag);
   }

   public void addEntity(Entity entity) {
      if (!entity.isPassenger()) {
         CompoundTag compoundtag = new CompoundTag();
         entity.save(compoundtag);
         this.addEntity(compoundtag);
      }
   }

   public void setStartForStructure(Structure structure, StructureStart structurestart) {
      BelowZeroRetrogen belowzeroretrogen = this.getBelowZeroRetrogen();
      if (belowzeroretrogen != null && structurestart.isValid()) {
         BoundingBox boundingbox = structurestart.getBoundingBox();
         LevelHeightAccessor levelheightaccessor = this.getHeightAccessorForGeneration();
         if (boundingbox.minY() < levelheightaccessor.getMinBuildHeight() || boundingbox.maxY() >= levelheightaccessor.getMaxBuildHeight()) {
            return;
         }
      }

      super.setStartForStructure(structure, structurestart);
   }

   public List<CompoundTag> getEntities() {
      return this.entities;
   }

   public ChunkStatus getStatus() {
      return this.status;
   }

   public void setStatus(ChunkStatus chunkstatus) {
      this.status = chunkstatus;
      if (this.belowZeroRetrogen != null && chunkstatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
         this.setBelowZeroRetrogen((BelowZeroRetrogen)null);
      }

      this.setUnsaved(true);
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
         return super.getNoiseBiome(i, j, k);
      } else {
         throw new IllegalStateException("Asking for biomes before we have biomes");
      }
   }

   public static short packOffsetCoordinates(BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      int l = i & 15;
      int i1 = j & 15;
      int j1 = k & 15;
      return (short)(l | i1 << 4 | j1 << 8);
   }

   public static BlockPos unpackOffsetCoordinates(short short0, int i, ChunkPos chunkpos) {
      int j = SectionPos.sectionToBlockCoord(chunkpos.x, short0 & 15);
      int k = SectionPos.sectionToBlockCoord(i, short0 >>> 4 & 15);
      int l = SectionPos.sectionToBlockCoord(chunkpos.z, short0 >>> 8 & 15);
      return new BlockPos(j, k, l);
   }

   public void markPosForPostprocessing(BlockPos blockpos) {
      if (!this.isOutsideBuildHeight(blockpos)) {
         ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(blockpos.getY())).add(packOffsetCoordinates(blockpos));
      }

   }

   public void addPackedPostProcess(short short0, int i) {
      ChunkAccess.getOrCreateOffsetList(this.postProcessing, i).add(short0);
   }

   public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.pendingBlockEntities);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos blockpos) {
      BlockEntity blockentity = this.getBlockEntity(blockpos);
      return blockentity != null ? blockentity.saveWithFullMetadata() : this.pendingBlockEntities.get(blockpos);
   }

   public void removeBlockEntity(BlockPos blockpos) {
      this.blockEntities.remove(blockpos);
      this.pendingBlockEntities.remove(blockpos);
   }

   @Nullable
   public CarvingMask getCarvingMask(GenerationStep.Carving generationstep_carving) {
      return this.carvingMasks.get(generationstep_carving);
   }

   public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving generationstep_carving) {
      return this.carvingMasks.computeIfAbsent(generationstep_carving, (generationstep_carving1) -> new CarvingMask(this.getHeight(), this.getMinBuildHeight()));
   }

   public void setCarvingMask(GenerationStep.Carving generationstep_carving, CarvingMask carvingmask) {
      this.carvingMasks.put(generationstep_carving, carvingmask);
   }

   public void setLightEngine(LevelLightEngine levellightengine) {
      this.lightEngine = levellightengine;
   }

   public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowzeroretrogen) {
      this.belowZeroRetrogen = belowzeroretrogen;
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return this.belowZeroRetrogen;
   }

   private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> protochunkticks) {
      return new LevelChunkTicks<>(protochunkticks.scheduledTicks());
   }

   public LevelChunkTicks<Block> unpackBlockTicks() {
      return unpackTicks(this.blockTicks);
   }

   public LevelChunkTicks<Fluid> unpackFluidTicks() {
      return unpackTicks(this.fluidTicks);
   }

   public LevelHeightAccessor getHeightAccessorForGeneration() {
      return (LevelHeightAccessor)(this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
   }
}
