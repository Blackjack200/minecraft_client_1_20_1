package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EuclideanGameEventListenerRegistry;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class LevelChunk extends ChunkAccess {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
      public void tick() {
      }

      public boolean isRemoved() {
         return true;
      }

      public BlockPos getPos() {
         return BlockPos.ZERO;
      }

      public String getType() {
         return "<null>";
      }
   };
   private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
   private boolean loaded;
   final Level level;
   @Nullable
   private Supplier<FullChunkStatus> fullStatus;
   @Nullable
   private LevelChunk.PostLoadProcessor postLoad;
   private final Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;
   private final LevelChunkTicks<Block> blockTicks;
   private final LevelChunkTicks<Fluid> fluidTicks;

   public LevelChunk(Level level, ChunkPos chunkpos) {
      this(level, chunkpos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, (LevelChunkSection[])null, (LevelChunk.PostLoadProcessor)null, (BlendingData)null);
   }

   public LevelChunk(Level level, ChunkPos chunkpos, UpgradeData upgradedata, LevelChunkTicks<Block> levelchunkticks, LevelChunkTicks<Fluid> levelchunkticks1, long i, @Nullable LevelChunkSection[] alevelchunksection, @Nullable LevelChunk.PostLoadProcessor levelchunk_postloadprocessor, @Nullable BlendingData blendingdata) {
      super(chunkpos, upgradedata, level, level.registryAccess().registryOrThrow(Registries.BIOME), i, alevelchunksection, blendingdata);
      this.level = level;
      this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap<>();

      for(Heightmap.Types heightmap_types : Heightmap.Types.values()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(heightmap_types)) {
            this.heightmaps.put(heightmap_types, new Heightmap(this, heightmap_types));
         }
      }

      this.postLoad = levelchunk_postloadprocessor;
      this.blockTicks = levelchunkticks;
      this.fluidTicks = levelchunkticks1;
   }

   public LevelChunk(ServerLevel serverlevel, ProtoChunk protochunk, @Nullable LevelChunk.PostLoadProcessor levelchunk_postloadprocessor) {
      this(serverlevel, protochunk.getPos(), protochunk.getUpgradeData(), protochunk.unpackBlockTicks(), protochunk.unpackFluidTicks(), protochunk.getInhabitedTime(), protochunk.getSections(), levelchunk_postloadprocessor, protochunk.getBlendingData());

      for(BlockEntity blockentity : protochunk.getBlockEntities().values()) {
         this.setBlockEntity(blockentity);
      }

      this.pendingBlockEntities.putAll(protochunk.getBlockEntityNbts());

      for(int i = 0; i < protochunk.getPostProcessing().length; ++i) {
         this.postProcessing[i] = protochunk.getPostProcessing()[i];
      }

      this.setAllStarts(protochunk.getAllStarts());
      this.setAllReferences(protochunk.getAllReferences());

      for(Map.Entry<Heightmap.Types, Heightmap> map_entry : protochunk.getHeightmaps()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(map_entry.getKey())) {
            this.setHeightmap(map_entry.getKey(), map_entry.getValue().getRawData());
         }
      }

      this.skyLightSources = protochunk.skyLightSources;
      this.setLightCorrect(protochunk.isLightCorrect());
      this.unsaved = true;
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

   public GameEventListenerRegistry getListenerRegistry(int i) {
      Level var3 = this.level;
      if (var3 instanceof ServerLevel serverlevel) {
         return this.gameEventListenerRegistrySections.computeIfAbsent(i, (k) -> new EuclideanGameEventListenerRegistry(serverlevel, i, this::removeGameEventListenerRegistry));
      } else {
         return super.getListenerRegistry(i);
      }
   }

   public BlockState getBlockState(BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      if (this.level.isDebug()) {
         BlockState blockstate = null;
         if (j == 60) {
            blockstate = Blocks.BARRIER.defaultBlockState();
         }

         if (j == 70) {
            blockstate = DebugLevelSource.getBlockStateFor(i, k);
         }

         return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
      } else {
         try {
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length) {
               LevelChunkSection levelchunksection = this.sections[l];
               if (!levelchunksection.hasOnlyAir()) {
                  return levelchunksection.getBlockState(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.defaultBlockState();
         } catch (Throwable var8) {
            CrashReport crashreport = CrashReport.forThrowable(var8, "Getting block state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this, i, j, k));
            throw new ReportedException(crashreport);
         }
      }
   }

   public FluidState getFluidState(BlockPos blockpos) {
      return this.getFluidState(blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   public FluidState getFluidState(int i, int j, int k) {
      try {
         int l = this.getSectionIndex(j);
         if (l >= 0 && l < this.sections.length) {
            LevelChunkSection levelchunksection = this.sections[l];
            if (!levelchunksection.hasOnlyAir()) {
               return levelchunksection.getFluidState(i & 15, j & 15, k & 15);
            }
         }

         return Fluids.EMPTY.defaultFluidState();
      } catch (Throwable var7) {
         CrashReport crashreport = CrashReport.forThrowable(var7, "Getting fluid state");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
         crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this, i, j, k));
         throw new ReportedException(crashreport);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos blockpos, BlockState blockstate, boolean flag) {
      int i = blockpos.getY();
      LevelChunkSection levelchunksection = this.getSection(this.getSectionIndex(i));
      boolean flag1 = levelchunksection.hasOnlyAir();
      if (flag1 && blockstate.isAir()) {
         return null;
      } else {
         int j = blockpos.getX() & 15;
         int k = i & 15;
         int l = blockpos.getZ() & 15;
         BlockState blockstate1 = levelchunksection.setBlockState(j, k, l, blockstate);
         if (blockstate1 == blockstate) {
            return null;
         } else {
            Block block = blockstate.getBlock();
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(j, i, l, blockstate);
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(j, i, l, blockstate);
            this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(j, i, l, blockstate);
            this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(j, i, l, blockstate);
            boolean flag2 = levelchunksection.hasOnlyAir();
            if (flag1 != flag2) {
               this.level.getChunkSource().getLightEngine().updateSectionStatus(blockpos, flag2);
            }

            if (LightEngine.hasDifferentLightProperties(this, blockpos, blockstate1, blockstate)) {
               ProfilerFiller profilerfiller = this.level.getProfiler();
               profilerfiller.push("updateSkyLightSources");
               this.skyLightSources.update(this, j, i, l);
               profilerfiller.popPush("queueCheckLight");
               this.level.getChunkSource().getLightEngine().checkBlock(blockpos);
               profilerfiller.pop();
            }

            boolean flag3 = blockstate1.hasBlockEntity();
            if (!this.level.isClientSide) {
               blockstate1.onRemove(this.level, blockpos, blockstate, flag);
            } else if (!blockstate1.is(block) && flag3) {
               this.removeBlockEntity(blockpos);
            }

            if (!levelchunksection.getBlockState(j, k, l).is(block)) {
               return null;
            } else {
               if (!this.level.isClientSide) {
                  blockstate.onPlace(this.level, blockpos, blockstate1, flag);
               }

               if (blockstate.hasBlockEntity()) {
                  BlockEntity blockentity = this.getBlockEntity(blockpos, LevelChunk.EntityCreationType.CHECK);
                  if (blockentity == null) {
                     blockentity = ((EntityBlock)block).newBlockEntity(blockpos, blockstate);
                     if (blockentity != null) {
                        this.addAndRegisterBlockEntity(blockentity);
                     }
                  } else {
                     blockentity.setBlockState(blockstate);
                     this.updateBlockEntityTicker(blockentity);
                  }
               }

               this.unsaved = true;
               return blockstate1;
            }
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void addEntity(Entity entity) {
   }

   @Nullable
   private BlockEntity createBlockEntity(BlockPos blockpos) {
      BlockState blockstate = this.getBlockState(blockpos);
      return !blockstate.hasBlockEntity() ? null : ((EntityBlock)blockstate.getBlock()).newBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      return this.getBlockEntity(blockpos, LevelChunk.EntityCreationType.CHECK);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos, LevelChunk.EntityCreationType levelchunk_entitycreationtype) {
      BlockEntity blockentity = this.blockEntities.get(blockpos);
      if (blockentity == null) {
         CompoundTag compoundtag = this.pendingBlockEntities.remove(blockpos);
         if (compoundtag != null) {
            BlockEntity blockentity1 = this.promotePendingBlockEntity(blockpos, compoundtag);
            if (blockentity1 != null) {
               return blockentity1;
            }
         }
      }

      if (blockentity == null) {
         if (levelchunk_entitycreationtype == LevelChunk.EntityCreationType.IMMEDIATE) {
            blockentity = this.createBlockEntity(blockpos);
            if (blockentity != null) {
               this.addAndRegisterBlockEntity(blockentity);
            }
         }
      } else if (blockentity.isRemoved()) {
         this.blockEntities.remove(blockpos);
         return null;
      }

      return blockentity;
   }

   public void addAndRegisterBlockEntity(BlockEntity blockentity) {
      this.setBlockEntity(blockentity);
      if (this.isInLevel()) {
         Level var3 = this.level;
         if (var3 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)var3;
            this.addGameEventListener(blockentity, serverlevel);
         }

         this.updateBlockEntityTicker(blockentity);
      }

   }

   private boolean isInLevel() {
      return this.loaded || this.level.isClientSide();
   }

   boolean isTicking(BlockPos blockpos) {
      if (!this.level.getWorldBorder().isWithinBounds(blockpos)) {
         return false;
      } else {
         Level var3 = this.level;
         if (!(var3 instanceof ServerLevel)) {
            return true;
         } else {
            ServerLevel serverlevel = (ServerLevel)var3;
            return this.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING) && serverlevel.areEntitiesLoaded(ChunkPos.asLong(blockpos));
         }
      }
   }

   public void setBlockEntity(BlockEntity blockentity) {
      BlockPos blockpos = blockentity.getBlockPos();
      if (this.getBlockState(blockpos).hasBlockEntity()) {
         blockentity.setLevel(this.level);
         blockentity.clearRemoved();
         BlockEntity blockentity1 = this.blockEntities.put(blockpos.immutable(), blockentity);
         if (blockentity1 != null && blockentity1 != blockentity) {
            blockentity1.setRemoved();
         }

      }
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos blockpos) {
      BlockEntity blockentity = this.getBlockEntity(blockpos);
      if (blockentity != null && !blockentity.isRemoved()) {
         CompoundTag compoundtag = blockentity.saveWithFullMetadata();
         compoundtag.putBoolean("keepPacked", false);
         return compoundtag;
      } else {
         CompoundTag compoundtag1 = this.pendingBlockEntities.get(blockpos);
         if (compoundtag1 != null) {
            compoundtag1 = compoundtag1.copy();
            compoundtag1.putBoolean("keepPacked", true);
         }

         return compoundtag1;
      }
   }

   public void removeBlockEntity(BlockPos blockpos) {
      if (this.isInLevel()) {
         BlockEntity blockentity = this.blockEntities.remove(blockpos);
         if (blockentity != null) {
            Level var4 = this.level;
            if (var4 instanceof ServerLevel) {
               ServerLevel serverlevel = (ServerLevel)var4;
               this.removeGameEventListener(blockentity, serverlevel);
            }

            blockentity.setRemoved();
         }
      }

      this.removeBlockEntityTicker(blockpos);
   }

   private <T extends BlockEntity> void removeGameEventListener(T blockentity, ServerLevel serverlevel) {
      Block block = blockentity.getBlockState().getBlock();
      if (block instanceof EntityBlock) {
         GameEventListener gameeventlistener = ((EntityBlock)block).getListener(serverlevel, blockentity);
         if (gameeventlistener != null) {
            int i = SectionPos.blockToSectionCoord(blockentity.getBlockPos().getY());
            GameEventListenerRegistry gameeventlistenerregistry = this.getListenerRegistry(i);
            gameeventlistenerregistry.unregister(gameeventlistener);
         }
      }

   }

   private void removeGameEventListenerRegistry(int i) {
      this.gameEventListenerRegistrySections.remove(i);
   }

   private void removeBlockEntityTicker(BlockPos blockpos) {
      LevelChunk.RebindableTickingBlockEntityWrapper levelchunk_rebindabletickingblockentitywrapper = this.tickersInLevel.remove(blockpos);
      if (levelchunk_rebindabletickingblockentitywrapper != null) {
         levelchunk_rebindabletickingblockentitywrapper.rebind(NULL_TICKER);
      }

   }

   public void runPostLoad() {
      if (this.postLoad != null) {
         this.postLoad.run(this);
         this.postLoad = null;
      }

   }

   public boolean isEmpty() {
      return false;
   }

   public void replaceWithPacketData(FriendlyByteBuf friendlybytebuf, CompoundTag compoundtag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
      this.clearAllBlockEntities();

      for(LevelChunkSection levelchunksection : this.sections) {
         levelchunksection.read(friendlybytebuf);
      }

      for(Heightmap.Types heightmap_types : Heightmap.Types.values()) {
         String s = heightmap_types.getSerializationKey();
         if (compoundtag.contains(s, 12)) {
            this.setHeightmap(heightmap_types, compoundtag.getLongArray(s));
         }
      }

      this.initializeLightSources();
      consumer.accept((blockpos, blockentitytype, compoundtag1) -> {
         BlockEntity blockentity = this.getBlockEntity(blockpos, LevelChunk.EntityCreationType.IMMEDIATE);
         if (blockentity != null && compoundtag1 != null && blockentity.getType() == blockentitytype) {
            blockentity.load(compoundtag1);
         }

      });
   }

   public void replaceBiomes(FriendlyByteBuf friendlybytebuf) {
      for(LevelChunkSection levelchunksection : this.sections) {
         levelchunksection.readBiomes(friendlybytebuf);
      }

   }

   public void setLoaded(boolean flag) {
      this.loaded = flag;
   }

   public Level getLevel() {
      return this.level;
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void postProcessGeneration() {
      ChunkPos chunkpos = this.getPos();

      for(int i = 0; i < this.postProcessing.length; ++i) {
         if (this.postProcessing[i] != null) {
            for(Short oshort : this.postProcessing[i]) {
               BlockPos blockpos = ProtoChunk.unpackOffsetCoordinates(oshort, this.getSectionYFromSectionIndex(i), chunkpos);
               BlockState blockstate = this.getBlockState(blockpos);
               FluidState fluidstate = blockstate.getFluidState();
               if (!fluidstate.isEmpty()) {
                  fluidstate.tick(this.level, blockpos);
               }

               if (!(blockstate.getBlock() instanceof LiquidBlock)) {
                  BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, this.level, blockpos);
                  this.level.setBlock(blockpos, blockstate1, 20);
               }
            }

            this.postProcessing[i].clear();
         }
      }

      for(BlockPos blockpos1 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
         this.getBlockEntity(blockpos1);
      }

      this.pendingBlockEntities.clear();
      this.upgradeData.upgrade(this);
   }

   @Nullable
   private BlockEntity promotePendingBlockEntity(BlockPos blockpos, CompoundTag compoundtag) {
      BlockState blockstate = this.getBlockState(blockpos);
      BlockEntity blockentity;
      if ("DUMMY".equals(compoundtag.getString("id"))) {
         if (blockstate.hasBlockEntity()) {
            blockentity = ((EntityBlock)blockstate.getBlock()).newBlockEntity(blockpos, blockstate);
         } else {
            blockentity = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", blockpos, blockstate);
         }
      } else {
         blockentity = BlockEntity.loadStatic(blockpos, blockstate, compoundtag);
      }

      if (blockentity != null) {
         blockentity.setLevel(this.level);
         this.addAndRegisterBlockEntity(blockentity);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", blockstate, blockpos);
      }

      return blockentity;
   }

   public void unpackTicks(long i) {
      this.blockTicks.unpack(i);
      this.fluidTicks.unpack(i);
   }

   public void registerTickContainerInLevel(ServerLevel serverlevel) {
      serverlevel.getBlockTicks().addContainer(this.chunkPos, this.blockTicks);
      serverlevel.getFluidTicks().addContainer(this.chunkPos, this.fluidTicks);
   }

   public void unregisterTickContainerFromLevel(ServerLevel serverlevel) {
      serverlevel.getBlockTicks().removeContainer(this.chunkPos);
      serverlevel.getFluidTicks().removeContainer(this.chunkPos);
   }

   public ChunkStatus getStatus() {
      return ChunkStatus.FULL;
   }

   public FullChunkStatus getFullStatus() {
      return this.fullStatus == null ? FullChunkStatus.FULL : this.fullStatus.get();
   }

   public void setFullStatus(Supplier<FullChunkStatus> supplier) {
      this.fullStatus = supplier;
   }

   public void clearAllBlockEntities() {
      this.blockEntities.values().forEach(BlockEntity::setRemoved);
      this.blockEntities.clear();
      this.tickersInLevel.values().forEach((levelchunk_rebindabletickingblockentitywrapper) -> levelchunk_rebindabletickingblockentitywrapper.rebind(NULL_TICKER));
      this.tickersInLevel.clear();
   }

   public void registerAllBlockEntitiesAfterLevelLoad() {
      this.blockEntities.values().forEach((blockentity) -> {
         Level level = this.level;
         if (level instanceof ServerLevel serverlevel) {
            this.addGameEventListener(blockentity, serverlevel);
         }

         this.updateBlockEntityTicker(blockentity);
      });
   }

   private <T extends BlockEntity> void addGameEventListener(T blockentity, ServerLevel serverlevel) {
      Block block = blockentity.getBlockState().getBlock();
      if (block instanceof EntityBlock) {
         GameEventListener gameeventlistener = ((EntityBlock)block).getListener(serverlevel, blockentity);
         if (gameeventlistener != null) {
            this.getListenerRegistry(SectionPos.blockToSectionCoord(blockentity.getBlockPos().getY())).register(gameeventlistener);
         }
      }

   }

   private <T extends BlockEntity> void updateBlockEntityTicker(T blockentity) {
      BlockState blockstate = blockentity.getBlockState();
      BlockEntityTicker<T> blockentityticker = blockstate.getTicker(this.level, blockentity.getType());
      if (blockentityticker == null) {
         this.removeBlockEntityTicker(blockentity.getBlockPos());
      } else {
         this.tickersInLevel.compute(blockentity.getBlockPos(), (blockpos, levelchunk_rebindabletickingblockentitywrapper) -> {
            TickingBlockEntity tickingblockentity = this.createTicker(blockentity, blockentityticker);
            if (levelchunk_rebindabletickingblockentitywrapper != null) {
               levelchunk_rebindabletickingblockentitywrapper.rebind(tickingblockentity);
               return levelchunk_rebindabletickingblockentitywrapper;
            } else if (this.isInLevel()) {
               LevelChunk.RebindableTickingBlockEntityWrapper levelchunk_rebindabletickingblockentitywrapper1 = new LevelChunk.RebindableTickingBlockEntityWrapper(tickingblockentity);
               this.level.addBlockEntityTicker(levelchunk_rebindabletickingblockentitywrapper1);
               return levelchunk_rebindabletickingblockentitywrapper1;
            } else {
               return null;
            }
         });
      }

   }

   private <T extends BlockEntity> TickingBlockEntity createTicker(T blockentity, BlockEntityTicker<T> blockentityticker) {
      return new LevelChunk.BoundTickingBlockEntity<>(blockentity, blockentityticker);
   }

   class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
      private final T blockEntity;
      private final BlockEntityTicker<T> ticker;
      private boolean loggedInvalidBlockState;

      BoundTickingBlockEntity(T blockentity, BlockEntityTicker<T> blockentityticker) {
         this.blockEntity = blockentity;
         this.ticker = blockentityticker;
      }

      public void tick() {
         if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
            BlockPos blockpos = this.blockEntity.getBlockPos();
            if (LevelChunk.this.isTicking(blockpos)) {
               try {
                  ProfilerFiller profilerfiller = LevelChunk.this.level.getProfiler();
                  profilerfiller.push(this::getType);
                  BlockState blockstate = LevelChunk.this.getBlockState(blockpos);
                  if (this.blockEntity.getType().isValid(blockstate)) {
                     this.ticker.tick(LevelChunk.this.level, this.blockEntity.getBlockPos(), blockstate, this.blockEntity);
                     this.loggedInvalidBlockState = false;
                  } else if (!this.loggedInvalidBlockState) {
                     this.loggedInvalidBlockState = true;
                     LevelChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", LogUtils.defer(this::getType), LogUtils.defer(this::getPos), blockstate);
                  }

                  profilerfiller.pop();
               } catch (Throwable var5) {
                  CrashReport crashreport = CrashReport.forThrowable(var5, "Ticking block entity");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Block entity being ticked");
                  this.blockEntity.fillCrashReportCategory(crashreportcategory);
                  throw new ReportedException(crashreport);
               }
            }
         }

      }

      public boolean isRemoved() {
         return this.blockEntity.isRemoved();
      }

      public BlockPos getPos() {
         return this.blockEntity.getBlockPos();
      }

      public String getType() {
         return BlockEntityType.getKey(this.blockEntity.getType()).toString();
      }

      public String toString() {
         return "Level ticker for " + this.getType() + "@" + this.getPos();
      }
   }

   public static enum EntityCreationType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }

   @FunctionalInterface
   public interface PostLoadProcessor {
      void run(LevelChunk levelchunk);
   }

   class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
      private TickingBlockEntity ticker;

      RebindableTickingBlockEntityWrapper(TickingBlockEntity tickingblockentity) {
         this.ticker = tickingblockentity;
      }

      void rebind(TickingBlockEntity tickingblockentity) {
         this.ticker = tickingblockentity;
      }

      public void tick() {
         this.ticker.tick();
      }

      public boolean isRemoved() {
         return this.ticker.isRemoved();
      }

      public BlockPos getPos() {
         return this.ticker.getPos();
      }

      public String getType() {
         return this.ticker.getType();
      }

      public String toString() {
         return this.ticker.toString() + " <wrapped>";
      }
   }
}
