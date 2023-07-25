package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerChunkCache extends ChunkSource {
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private final DistanceManager distanceManager;
   final ServerLevel level;
   final Thread mainThread;
   final ThreadedLevelLightEngine lightEngine;
   private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
   public final ChunkMap chunkMap;
   private final DimensionDataStorage dataStorage;
   private long lastInhabitedUpdate;
   private boolean spawnEnemies = true;
   private boolean spawnFriendlies = true;
   private static final int CACHE_SIZE = 4;
   private final long[] lastChunkPos = new long[4];
   private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
   private final ChunkAccess[] lastChunk = new ChunkAccess[4];
   @Nullable
   @VisibleForDebug
   private NaturalSpawner.SpawnState lastSpawnState;

   public ServerChunkCache(ServerLevel serverlevel, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer, StructureTemplateManager structuretemplatemanager, Executor executor, ChunkGenerator chunkgenerator, int i, int j, boolean flag, ChunkProgressListener chunkprogresslistener, ChunkStatusUpdateListener chunkstatusupdatelistener, Supplier<DimensionDataStorage> supplier) {
      this.level = serverlevel;
      this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(serverlevel);
      this.mainThread = Thread.currentThread();
      File file = levelstoragesource_levelstorageaccess.getDimensionPath(serverlevel.dimension()).resolve("data").toFile();
      file.mkdirs();
      this.dataStorage = new DimensionDataStorage(file, datafixer);
      this.chunkMap = new ChunkMap(serverlevel, levelstoragesource_levelstorageaccess, datafixer, structuretemplatemanager, executor, this.mainThreadProcessor, this, chunkgenerator, chunkprogresslistener, chunkstatusupdatelistener, supplier, i, flag);
      this.lightEngine = this.chunkMap.getLightEngine();
      this.distanceManager = this.chunkMap.getDistanceManager();
      this.distanceManager.updateSimulationDistance(j);
      this.clearCache();
   }

   public ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   private ChunkHolder getVisibleChunkIfPresent(long i) {
      return this.chunkMap.getVisibleChunkIfPresent(i);
   }

   public int getTickingGenerated() {
      return this.chunkMap.getTickingGenerated();
   }

   private void storeInCache(long i, ChunkAccess chunkaccess, ChunkStatus chunkstatus) {
      for(int j = 3; j > 0; --j) {
         this.lastChunkPos[j] = this.lastChunkPos[j - 1];
         this.lastChunkStatus[j] = this.lastChunkStatus[j - 1];
         this.lastChunk[j] = this.lastChunk[j - 1];
      }

      this.lastChunkPos[0] = i;
      this.lastChunkStatus[0] = chunkstatus;
      this.lastChunk[0] = chunkaccess;
   }

   @Nullable
   public ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      if (Thread.currentThread() != this.mainThread) {
         return CompletableFuture.supplyAsync(() -> this.getChunk(i, j, chunkstatus, flag), this.mainThreadProcessor).join();
      } else {
         ProfilerFiller profilerfiller = this.level.getProfiler();
         profilerfiller.incrementCounter("getChunk");
         long k = ChunkPos.asLong(i, j);

         for(int l = 0; l < 4; ++l) {
            if (k == this.lastChunkPos[l] && chunkstatus == this.lastChunkStatus[l]) {
               ChunkAccess chunkaccess = this.lastChunk[l];
               if (chunkaccess != null || !flag) {
                  return chunkaccess;
               }
            }
         }

         profilerfiller.incrementCounter("getChunkCacheMiss");
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkFutureMainThread(i, j, chunkstatus, flag);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
         ChunkAccess chunkaccess1 = completablefuture.join().map((chunkaccess2) -> chunkaccess2, (chunkholder_chunkloadingfailure) -> {
            if (flag) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkholder_chunkloadingfailure));
            } else {
               return null;
            }
         });
         this.storeInCache(k, chunkaccess1, chunkstatus);
         return chunkaccess1;
      }
   }

   @Nullable
   public LevelChunk getChunkNow(int i, int j) {
      if (Thread.currentThread() != this.mainThread) {
         return null;
      } else {
         this.level.getProfiler().incrementCounter("getChunkNow");
         long k = ChunkPos.asLong(i, j);

         for(int l = 0; l < 4; ++l) {
            if (k == this.lastChunkPos[l] && this.lastChunkStatus[l] == ChunkStatus.FULL) {
               ChunkAccess chunkaccess = this.lastChunk[l];
               return chunkaccess instanceof LevelChunk ? (LevelChunk)chunkaccess : null;
            }
         }

         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(k);
         if (chunkholder == null) {
            return null;
         } else {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getFutureIfPresent(ChunkStatus.FULL).getNow((Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)null);
            if (either == null) {
               return null;
            } else {
               ChunkAccess chunkaccess1 = either.left().orElse((ChunkAccess)null);
               if (chunkaccess1 != null) {
                  this.storeInCache(k, chunkaccess1, ChunkStatus.FULL);
                  if (chunkaccess1 instanceof LevelChunk) {
                     return (LevelChunk)chunkaccess1;
                  }
               }

               return null;
            }
         }
      }
   }

   private void clearCache() {
      Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunkStatus, (Object)null);
      Arrays.fill(this.lastChunk, (Object)null);
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      boolean flag1 = Thread.currentThread() == this.mainThread;
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture;
      if (flag1) {
         completablefuture = this.getChunkFutureMainThread(i, j, chunkstatus, flag);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
      } else {
         completablefuture = CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(i, j, chunkstatus, flag), this.mainThreadProcessor).thenCompose((completablefuture2) -> completablefuture2);
      }

      return completablefuture;
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      ChunkPos chunkpos = new ChunkPos(i, j);
      long k = chunkpos.toLong();
      int l = ChunkLevel.byStatus(chunkstatus);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(k);
      if (flag) {
         this.distanceManager.addTicket(TicketType.UNKNOWN, chunkpos, l, chunkpos);
         if (this.chunkAbsent(chunkholder, l)) {
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.push("chunkLoad");
            this.runDistanceManagerUpdates();
            chunkholder = this.getVisibleChunkIfPresent(k);
            profilerfiller.pop();
            if (this.chunkAbsent(chunkholder, l)) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
            }
         }
      }

      return this.chunkAbsent(chunkholder, l) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(chunkstatus, this.chunkMap);
   }

   private boolean chunkAbsent(@Nullable ChunkHolder chunkholder, int i) {
      return chunkholder == null || chunkholder.getTicketLevel() > i;
   }

   public boolean hasChunk(int i, int j) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent((new ChunkPos(i, j)).toLong());
      int k = ChunkLevel.byStatus(ChunkStatus.FULL);
      return !this.chunkAbsent(chunkholder, k);
   }

   @Nullable
   public LightChunk getChunkForLighting(int i, int j) {
      long k = ChunkPos.asLong(i, j);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(k);
      if (chunkholder == null) {
         return null;
      } else {
         int l = CHUNK_STATUSES.size() - 1;

         while(true) {
            ChunkStatus chunkstatus = CHUNK_STATUSES.get(l);
            Optional<ChunkAccess> optional = chunkholder.getFutureIfPresentUnchecked(chunkstatus).getNow(ChunkHolder.UNLOADED_CHUNK).left();
            if (optional.isPresent()) {
               return optional.get();
            }

            if (chunkstatus == ChunkStatus.INITIALIZE_LIGHT.getParent()) {
               return null;
            }

            --l;
         }
      }
   }

   public Level getLevel() {
      return this.level;
   }

   public boolean pollTask() {
      return this.mainThreadProcessor.pollTask();
   }

   boolean runDistanceManagerUpdates() {
      boolean flag = this.distanceManager.runAllUpdates(this.chunkMap);
      boolean flag1 = this.chunkMap.promoteChunkMap();
      if (!flag && !flag1) {
         return false;
      } else {
         this.clearCache();
         return true;
      }
   }

   public boolean isPositionTicking(long i) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
      if (chunkholder == null) {
         return false;
      } else if (!this.level.shouldTickBlocksAt(i)) {
         return false;
      } else {
         Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getTickingChunkFuture().getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
         return either != null && either.left().isPresent();
      }
   }

   public void save(boolean flag) {
      this.runDistanceManagerUpdates();
      this.chunkMap.saveAllChunks(flag);
   }

   public void close() throws IOException {
      this.save(true);
      this.lightEngine.close();
      this.chunkMap.close();
   }

   public void tick(BooleanSupplier booleansupplier, boolean flag) {
      this.level.getProfiler().push("purge");
      this.distanceManager.purgeStaleTickets();
      this.runDistanceManagerUpdates();
      this.level.getProfiler().popPush("chunks");
      if (flag) {
         this.tickChunks();
      }

      this.level.getProfiler().popPush("unload");
      this.chunkMap.tick(booleansupplier);
      this.level.getProfiler().pop();
      this.clearCache();
   }

   private void tickChunks() {
      long i = this.level.getGameTime();
      long j = i - this.lastInhabitedUpdate;
      this.lastInhabitedUpdate = i;
      boolean flag = this.level.isDebug();
      if (flag) {
         this.chunkMap.tick();
      } else {
         LevelData leveldata = this.level.getLevelData();
         ProfilerFiller profilerfiller = this.level.getProfiler();
         profilerfiller.push("pollingChunks");
         int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
         boolean flag1 = leveldata.getGameTime() % 400L == 0L;
         profilerfiller.push("naturalSpawnCount");
         int l = this.distanceManager.getNaturalSpawnChunkCount();
         NaturalSpawner.SpawnState naturalspawner_spawnstate = NaturalSpawner.createState(l, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
         this.lastSpawnState = naturalspawner_spawnstate;
         profilerfiller.popPush("filteringLoadedChunks");
         List<ServerChunkCache.ChunkAndHolder> list = Lists.newArrayListWithCapacity(l);

         for(ChunkHolder chunkholder : this.chunkMap.getChunks()) {
            LevelChunk levelchunk = chunkholder.getTickingChunk();
            if (levelchunk != null) {
               list.add(new ServerChunkCache.ChunkAndHolder(levelchunk, chunkholder));
            }
         }

         profilerfiller.popPush("spawnAndTick");
         boolean flag2 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
         Collections.shuffle(list);

         for(ServerChunkCache.ChunkAndHolder serverchunkcache_chunkandholder : list) {
            LevelChunk levelchunk1 = serverchunkcache_chunkandholder.chunk;
            ChunkPos chunkpos = levelchunk1.getPos();
            if (this.level.isNaturalSpawningAllowed(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos)) {
               levelchunk1.incrementInhabitedTime(j);
               if (flag2 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos)) {
                  NaturalSpawner.spawnForChunk(this.level, levelchunk1, naturalspawner_spawnstate, this.spawnFriendlies, this.spawnEnemies, flag1);
               }

               if (this.level.shouldTickBlocksAt(chunkpos.toLong())) {
                  this.level.tickChunk(levelchunk1, k);
               }
            }
         }

         profilerfiller.popPush("customSpawners");
         if (flag2) {
            this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
         }

         profilerfiller.popPush("broadcast");
         list.forEach((serverchunkcache_chunkandholder1) -> serverchunkcache_chunkandholder1.holder.broadcastChanges(serverchunkcache_chunkandholder1.chunk));
         profilerfiller.pop();
         profilerfiller.pop();
         this.chunkMap.tick();
      }
   }

   private void getFullChunk(long i1, Consumer<LevelChunk> consumer) {
      ChunkHolder chunkholder1 = this.getVisibleChunkIfPresent(i1);
      if (chunkholder1 != null) {
         chunkholder1.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(consumer);
      }

   }

   public String gatherStats() {
      return Integer.toString(this.getLoadedChunksCount());
   }

   @VisibleForTesting
   public int getPendingTasksCount() {
      return this.mainThreadProcessor.getPendingTasksCount();
   }

   public ChunkGenerator getGenerator() {
      return this.chunkMap.generator();
   }

   public ChunkGeneratorStructureState getGeneratorState() {
      return this.chunkMap.generatorState();
   }

   public RandomState randomState() {
      return this.chunkMap.randomState();
   }

   public int getLoadedChunksCount() {
      return this.chunkMap.size();
   }

   public void blockChanged(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX());
      int j = SectionPos.blockToSectionCoord(blockpos.getZ());
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
      if (chunkholder != null) {
         chunkholder.blockChanged(blockpos);
      }

   }

   public void onLightUpdate(LightLayer lightlayer, SectionPos sectionpos) {
      this.mainThreadProcessor.execute(() -> {
         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(sectionpos.chunk().toLong());
         if (chunkholder != null) {
            chunkholder.sectionLightChanged(lightlayer, sectionpos.y());
         }

      });
   }

   public <T> void addRegionTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      this.distanceManager.addRegionTicket(tickettype, chunkpos, i, object);
   }

   public <T> void removeRegionTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      this.distanceManager.removeRegionTicket(tickettype, chunkpos, i, object);
   }

   public void updateChunkForced(ChunkPos chunkpos, boolean flag) {
      this.distanceManager.updateChunkForced(chunkpos, flag);
   }

   public void move(ServerPlayer serverplayer) {
      if (!serverplayer.isRemoved()) {
         this.chunkMap.move(serverplayer);
      }

   }

   public void removeEntity(Entity entity) {
      this.chunkMap.removeEntity(entity);
   }

   public void addEntity(Entity entity) {
      this.chunkMap.addEntity(entity);
   }

   public void broadcastAndSend(Entity entity, Packet<?> packet) {
      this.chunkMap.broadcastAndSend(entity, packet);
   }

   public void broadcast(Entity entity, Packet<?> packet) {
      this.chunkMap.broadcast(entity, packet);
   }

   public void setViewDistance(int i) {
      this.chunkMap.setViewDistance(i);
   }

   public void setSimulationDistance(int i) {
      this.distanceManager.updateSimulationDistance(i);
   }

   public void setSpawnSettings(boolean flag, boolean flag1) {
      this.spawnEnemies = flag;
      this.spawnFriendlies = flag1;
   }

   public String getChunkDebugData(ChunkPos chunkpos) {
      return this.chunkMap.getChunkDebugData(chunkpos);
   }

   public DimensionDataStorage getDataStorage() {
      return this.dataStorage;
   }

   public PoiManager getPoiManager() {
      return this.chunkMap.getPoiManager();
   }

   public ChunkScanAccess chunkScanner() {
      return this.chunkMap.chunkScanner();
   }

   @Nullable
   @VisibleForDebug
   public NaturalSpawner.SpawnState getLastSpawnState() {
      return this.lastSpawnState;
   }

   public void removeTicketsOnClosing() {
      this.distanceManager.removeTicketsOnClosing();
   }

   static record ChunkAndHolder(LevelChunk chunk, ChunkHolder holder) {
      final LevelChunk chunk;
      final ChunkHolder holder;
   }

   final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
      MainThreadExecutor(Level level) {
         super("Chunk source main thread executor for " + level.dimension().location());
      }

      protected Runnable wrapRunnable(Runnable runnable) {
         return runnable;
      }

      protected boolean shouldRun(Runnable runnable) {
         return true;
      }

      protected boolean scheduleExecutables() {
         return true;
      }

      protected Thread getRunningThread() {
         return ServerChunkCache.this.mainThread;
      }

      protected void doRunTask(Runnable runnable) {
         ServerChunkCache.this.level.getProfiler().incrementCounter("runTask");
         super.doRunTask(runnable);
      }

      protected boolean pollTask() {
         if (ServerChunkCache.this.runDistanceManagerUpdates()) {
            return true;
         } else {
            ServerChunkCache.this.lightEngine.tryScheduleUpdate();
            return super.pollTask();
         }
      }
   }
}
