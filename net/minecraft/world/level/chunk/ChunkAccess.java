package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess implements BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess {
   public static final int NO_FILLED_SECTION = -1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LongSet EMPTY_REFERENCE_SET = new LongOpenHashSet();
   protected final ShortList[] postProcessing;
   protected volatile boolean unsaved;
   private volatile boolean isLightCorrect;
   protected final ChunkPos chunkPos;
   private long inhabitedTime;
   /** @deprecated */
   @Nullable
   @Deprecated
   private BiomeGenerationSettings carverBiomeSettings;
   @Nullable
   protected NoiseChunk noiseChunk;
   protected final UpgradeData upgradeData;
   @Nullable
   protected BlendingData blendingData;
   protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
   protected ChunkSkyLightSources skyLightSources;
   private final Map<Structure, StructureStart> structureStarts = Maps.newHashMap();
   private final Map<Structure, LongSet> structuresRefences = Maps.newHashMap();
   protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
   protected final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
   protected final LevelHeightAccessor levelHeightAccessor;
   protected final LevelChunkSection[] sections;

   public ChunkAccess(ChunkPos chunkpos, UpgradeData upgradedata, LevelHeightAccessor levelheightaccessor, Registry<Biome> registry, long i, @Nullable LevelChunkSection[] alevelchunksection, @Nullable BlendingData blendingdata) {
      this.chunkPos = chunkpos;
      this.upgradeData = upgradedata;
      this.levelHeightAccessor = levelheightaccessor;
      this.sections = new LevelChunkSection[levelheightaccessor.getSectionsCount()];
      this.inhabitedTime = i;
      this.postProcessing = new ShortList[levelheightaccessor.getSectionsCount()];
      this.blendingData = blendingdata;
      this.skyLightSources = new ChunkSkyLightSources(levelheightaccessor);
      if (alevelchunksection != null) {
         if (this.sections.length == alevelchunksection.length) {
            System.arraycopy(alevelchunksection, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", alevelchunksection.length, this.sections.length);
         }
      }

      replaceMissingSections(registry, this.sections);
   }

   private static void replaceMissingSections(Registry<Biome> registry, LevelChunkSection[] alevelchunksection) {
      for(int i = 0; i < alevelchunksection.length; ++i) {
         if (alevelchunksection[i] == null) {
            alevelchunksection[i] = new LevelChunkSection(registry);
         }
      }

   }

   public GameEventListenerRegistry getListenerRegistry(int i) {
      return GameEventListenerRegistry.NOOP;
   }

   @Nullable
   public abstract BlockState setBlockState(BlockPos blockpos, BlockState blockstate, boolean flag);

   public abstract void setBlockEntity(BlockEntity blockentity);

   public abstract void addEntity(Entity entity);

   public int getHighestFilledSectionIndex() {
      LevelChunkSection[] alevelchunksection = this.getSections();

      for(int i = alevelchunksection.length - 1; i >= 0; --i) {
         LevelChunkSection levelchunksection = alevelchunksection[i];
         if (!levelchunksection.hasOnlyAir()) {
            return i;
         }
      }

      return -1;
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public int getHighestSectionPosition() {
      int i = this.getHighestFilledSectionIndex();
      return i == -1 ? this.getMinBuildHeight() : SectionPos.sectionToBlockCoord(this.getSectionYFromSectionIndex(i));
   }

   public Set<BlockPos> getBlockEntitiesPos() {
      Set<BlockPos> set = Sets.newHashSet(this.pendingBlockEntities.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   public LevelChunkSection[] getSections() {
      return this.sections;
   }

   public LevelChunkSection getSection(int i) {
      return this.getSections()[i];
   }

   public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public void setHeightmap(Heightmap.Types heightmap_types, long[] along) {
      this.getOrCreateHeightmapUnprimed(heightmap_types).setRawData(this, heightmap_types, along);
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types heightmap_types) {
      return this.heightmaps.computeIfAbsent(heightmap_types, (heightmap_types1) -> new Heightmap(this, heightmap_types1));
   }

   public boolean hasPrimedHeightmap(Heightmap.Types heightmap_types) {
      return this.heightmaps.get(heightmap_types) != null;
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      Heightmap heightmap = this.heightmaps.get(heightmap_types);
      if (heightmap == null) {
         if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
            LOGGER.error("Unprimed heightmap: " + heightmap_types + " " + i + " " + j);
         }

         Heightmap.primeHeightmaps(this, EnumSet.of(heightmap_types));
         heightmap = this.heightmaps.get(heightmap_types);
      }

      return heightmap.getFirstAvailable(i & 15, j & 15) - 1;
   }

   public ChunkPos getPos() {
      return this.chunkPos;
   }

   @Nullable
   public StructureStart getStartForStructure(Structure structure) {
      return this.structureStarts.get(structure);
   }

   public void setStartForStructure(Structure structure, StructureStart structurestart) {
      this.structureStarts.put(structure, structurestart);
      this.unsaved = true;
   }

   public Map<Structure, StructureStart> getAllStarts() {
      return Collections.unmodifiableMap(this.structureStarts);
   }

   public void setAllStarts(Map<Structure, StructureStart> map) {
      this.structureStarts.clear();
      this.structureStarts.putAll(map);
      this.unsaved = true;
   }

   public LongSet getReferencesForStructure(Structure structure) {
      return this.structuresRefences.getOrDefault(structure, EMPTY_REFERENCE_SET);
   }

   public void addReferenceForStructure(Structure structure, long i) {
      this.structuresRefences.computeIfAbsent(structure, (structure1) -> new LongOpenHashSet()).add(i);
      this.unsaved = true;
   }

   public Map<Structure, LongSet> getAllReferences() {
      return Collections.unmodifiableMap(this.structuresRefences);
   }

   public void setAllReferences(Map<Structure, LongSet> map) {
      this.structuresRefences.clear();
      this.structuresRefences.putAll(map);
      this.unsaved = true;
   }

   public boolean isYSpaceEmpty(int i, int j) {
      if (i < this.getMinBuildHeight()) {
         i = this.getMinBuildHeight();
      }

      if (j >= this.getMaxBuildHeight()) {
         j = this.getMaxBuildHeight() - 1;
      }

      for(int k = i; k <= j; k += 16) {
         if (!this.getSection(this.getSectionIndex(k)).hasOnlyAir()) {
            return false;
         }
      }

      return true;
   }

   public void setUnsaved(boolean flag) {
      this.unsaved = flag;
   }

   public boolean isUnsaved() {
      return this.unsaved;
   }

   public abstract ChunkStatus getStatus();

   public ChunkStatus getHighestGeneratedStatus() {
      ChunkStatus chunkstatus = this.getStatus();
      BelowZeroRetrogen belowzeroretrogen = this.getBelowZeroRetrogen();
      if (belowzeroretrogen != null) {
         ChunkStatus chunkstatus1 = belowzeroretrogen.targetStatus();
         return chunkstatus1.isOrAfter(chunkstatus) ? chunkstatus1 : chunkstatus;
      } else {
         return chunkstatus;
      }
   }

   public abstract void removeBlockEntity(BlockPos blockpos);

   public void markPosForPostprocessing(BlockPos blockpos) {
      LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)blockpos);
   }

   public ShortList[] getPostProcessing() {
      return this.postProcessing;
   }

   public void addPackedPostProcess(short short0, int i) {
      getOrCreateOffsetList(this.getPostProcessing(), i).add(short0);
   }

   public void setBlockEntityNbt(CompoundTag compoundtag) {
      this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundtag), compoundtag);
   }

   @Nullable
   public CompoundTag getBlockEntityNbt(BlockPos blockpos) {
      return this.pendingBlockEntities.get(blockpos);
   }

   @Nullable
   public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos blockpos);

   public final void findBlockLightSources(BiConsumer<BlockPos, BlockState> biconsumer) {
      this.findBlocks((blockstate) -> blockstate.getLightEmission() != 0, biconsumer);
   }

   public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biconsumer) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = this.getMinSection(); i < this.getMaxSection(); ++i) {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndexFromSectionY(i));
         if (levelchunksection.maybeHas(predicate)) {
            BlockPos blockpos = SectionPos.of(this.chunkPos, i).origin();

            for(int j = 0; j < 16; ++j) {
               for(int k = 0; k < 16; ++k) {
                  for(int l = 0; l < 16; ++l) {
                     BlockState blockstate = levelchunksection.getBlockState(l, j, k);
                     if (predicate.test(blockstate)) {
                        biconsumer.accept(blockpos_mutableblockpos.setWithOffset(blockpos, l, j, k), blockstate);
                     }
                  }
               }
            }
         }
      }

   }

   public abstract TickContainerAccess<Block> getBlockTicks();

   public abstract TickContainerAccess<Fluid> getFluidTicks();

   public abstract ChunkAccess.TicksToSave getTicksForSerialization();

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public boolean isOldNoiseGeneration() {
      return this.blendingData != null;
   }

   @Nullable
   public BlendingData getBlendingData() {
      return this.blendingData;
   }

   public void setBlendingData(BlendingData blendingdata) {
      this.blendingData = blendingdata;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void incrementInhabitedTime(long i) {
      this.inhabitedTime += i;
   }

   public void setInhabitedTime(long i) {
      this.inhabitedTime = i;
   }

   public static ShortList getOrCreateOffsetList(ShortList[] ashortlist, int i) {
      if (ashortlist[i] == null) {
         ashortlist[i] = new ShortArrayList();
      }

      return ashortlist[i];
   }

   public boolean isLightCorrect() {
      return this.isLightCorrect;
   }

   public void setLightCorrect(boolean flag) {
      this.isLightCorrect = flag;
      this.setUnsaved(true);
   }

   public int getMinBuildHeight() {
      return this.levelHeightAccessor.getMinBuildHeight();
   }

   public int getHeight() {
      return this.levelHeightAccessor.getHeight();
   }

   public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> function) {
      if (this.noiseChunk == null) {
         this.noiseChunk = function.apply(this);
      }

      return this.noiseChunk;
   }

   /** @deprecated */
   @Deprecated
   public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> supplier) {
      if (this.carverBiomeSettings == null) {
         this.carverBiomeSettings = supplier.get();
      }

      return this.carverBiomeSettings;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      try {
         int l = QuartPos.fromBlock(this.getMinBuildHeight());
         int i1 = l + QuartPos.fromBlock(this.getHeight()) - 1;
         int j1 = Mth.clamp(j, l, i1);
         int k1 = this.getSectionIndex(QuartPos.toBlock(j1));
         return this.sections[k1].getNoiseBiome(i & 3, j1 & 3, k & 3);
      } catch (Throwable var8) {
         CrashReport crashreport = CrashReport.forThrowable(var8, "Getting biome");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Biome being got");
         crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this, i, j, k));
         throw new ReportedException(crashreport);
      }
   }

   public void fillBiomesFromNoise(BiomeResolver biomeresolver, Climate.Sampler climate_sampler) {
      ChunkPos chunkpos = this.getPos();
      int i = QuartPos.fromBlock(chunkpos.getMinBlockX());
      int j = QuartPos.fromBlock(chunkpos.getMinBlockZ());
      LevelHeightAccessor levelheightaccessor = this.getHeightAccessorForGeneration();

      for(int k = levelheightaccessor.getMinSection(); k < levelheightaccessor.getMaxSection(); ++k) {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndexFromSectionY(k));
         int l = QuartPos.fromSection(k);
         levelchunksection.fillBiomesFromNoise(biomeresolver, climate_sampler, i, l, j);
      }

   }

   public boolean hasAnyStructureReferences() {
      return !this.getAllReferences().isEmpty();
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return null;
   }

   public boolean isUpgrading() {
      return this.getBelowZeroRetrogen() != null;
   }

   public LevelHeightAccessor getHeightAccessorForGeneration() {
      return this;
   }

   public void initializeLightSources() {
      this.skyLightSources.fillFrom(this);
   }

   public ChunkSkyLightSources getSkyLightSources() {
      return this.skyLightSources;
   }

   public static record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
   }
}
