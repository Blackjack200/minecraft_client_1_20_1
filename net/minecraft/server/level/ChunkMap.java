package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
   private static final byte CHUNK_TYPE_REPLACEABLE = -1;
   private static final byte CHUNK_TYPE_UNKNOWN = 0;
   private static final byte CHUNK_TYPE_FULL = 1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CHUNK_SAVED_PER_TICK = 200;
   private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
   private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
   private static final int MIN_VIEW_DISTANCE = 2;
   public static final int MAX_VIEW_DISTANCE = 32;
   public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
   private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
   private final LongSet entitiesInLevel = new LongOpenHashSet();
   final ServerLevel level;
   private final ThreadedLevelLightEngine lightEngine;
   private final BlockableEventLoop<Runnable> mainThreadExecutor;
   private ChunkGenerator generator;
   private final RandomState randomState;
   private final ChunkGeneratorStructureState chunkGeneratorState;
   private final Supplier<DimensionDataStorage> overworldDataStorage;
   private final PoiManager poiManager;
   final LongSet toDrop = new LongOpenHashSet();
   private boolean modified;
   private final ChunkTaskPriorityQueueSorter queueSorter;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
   private final ChunkProgressListener progressListener;
   private final ChunkStatusUpdateListener chunkStatusListener;
   private final ChunkMap.DistanceManager distanceManager;
   private final AtomicInteger tickingGenerated = new AtomicInteger();
   private final StructureTemplateManager structureTemplateManager;
   private final String storageName;
   private final PlayerMap playerMap = new PlayerMap();
   private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
   private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
   private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
   private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
   int viewDistance;

   public ChunkMap(ServerLevel serverlevel, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer, StructureTemplateManager structuretemplatemanager, Executor executor, BlockableEventLoop<Runnable> blockableeventloop, LightChunkGetter lightchunkgetter, ChunkGenerator chunkgenerator, ChunkProgressListener chunkprogresslistener, ChunkStatusUpdateListener chunkstatusupdatelistener, Supplier<DimensionDataStorage> supplier, int i, boolean flag) {
      super(levelstoragesource_levelstorageaccess.getDimensionPath(serverlevel.dimension()).resolve("region"), datafixer, flag);
      this.structureTemplateManager = structuretemplatemanager;
      Path path = levelstoragesource_levelstorageaccess.getDimensionPath(serverlevel.dimension());
      this.storageName = path.getFileName().toString();
      this.level = serverlevel;
      this.generator = chunkgenerator;
      RegistryAccess registryaccess = serverlevel.registryAccess();
      long j = serverlevel.getSeed();
      if (chunkgenerator instanceof NoiseBasedChunkGenerator noisebasedchunkgenerator) {
         this.randomState = RandomState.create(noisebasedchunkgenerator.generatorSettings().value(), registryaccess.lookupOrThrow(Registries.NOISE), j);
      } else {
         this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), registryaccess.lookupOrThrow(Registries.NOISE), j);
      }

      this.chunkGeneratorState = chunkgenerator.createState(registryaccess.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, j);
      this.mainThreadExecutor = blockableeventloop;
      ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(executor, "worldgen");
      ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("main", blockableeventloop::tell);
      this.progressListener = chunkprogresslistener;
      this.chunkStatusListener = chunkstatusupdatelistener;
      ProcessorMailbox<Runnable> processormailbox1 = ProcessorMailbox.create(executor, "light");
      this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processormailbox, processorhandle, processormailbox1), executor, Integer.MAX_VALUE);
      this.worldgenMailbox = this.queueSorter.getProcessor(processormailbox, false);
      this.mainThreadMailbox = this.queueSorter.getProcessor(processorhandle, false);
      this.lightEngine = new ThreadedLevelLightEngine(lightchunkgetter, this, this.level.dimensionType().hasSkyLight(), processormailbox1, this.queueSorter.getProcessor(processormailbox1, false));
      this.distanceManager = new ChunkMap.DistanceManager(executor, blockableeventloop);
      this.overworldDataStorage = supplier;
      this.poiManager = new PoiManager(path.resolve("poi"), datafixer, flag, registryaccess, serverlevel);
      this.setViewDistance(i);
   }

   protected ChunkGenerator generator() {
      return this.generator;
   }

   protected ChunkGeneratorStructureState generatorState() {
      return this.chunkGeneratorState;
   }

   protected RandomState randomState() {
      return this.randomState;
   }

   public void debugReloadGenerator() {
      DataResult<JsonElement> dataresult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.generator);
      DataResult<ChunkGenerator> dataresult1 = dataresult.flatMap((jsonelement) -> ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, jsonelement));
      dataresult1.result().ifPresent((chunkgenerator) -> this.generator = chunkgenerator);
   }

   private static double euclideanDistanceSquared(ChunkPos chunkpos, Entity entity) {
      double d0 = (double)SectionPos.sectionToBlockCoord(chunkpos.x, 8);
      double d1 = (double)SectionPos.sectionToBlockCoord(chunkpos.z, 8);
      double d2 = d0 - entity.getX();
      double d3 = d1 - entity.getZ();
      return d2 * d2 + d3 * d3;
   }

   public static boolean isChunkInRange(int i, int j, int k, int l, int i1) {
      int j1 = Math.max(0, Math.abs(i - k) - 1);
      int k1 = Math.max(0, Math.abs(j - l) - 1);
      long l1 = (long)Math.max(0, Math.max(j1, k1) - 1);
      long i2 = (long)Math.min(j1, k1);
      long j2 = i2 * i2 + l1 * l1;
      int k2 = i1 * i1;
      return j2 < (long)k2;
   }

   private static boolean isChunkOnRangeBorder(int i, int j, int k, int l, int i1) {
      if (!isChunkInRange(i, j, k, l, i1)) {
         return false;
      } else {
         return !isChunkInRange(i + 1, j + 1, k, l, i1) || !isChunkInRange(i - 1, j + 1, k, l, i1) || !isChunkInRange(i + 1, j - 1, k, l, i1) || !isChunkInRange(i - 1, j - 1, k, l, i1);
      }
   }

   protected ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   protected ChunkHolder getUpdatingChunkIfPresent(long i) {
      return this.updatingChunkMap.get(i);
   }

   @Nullable
   protected ChunkHolder getVisibleChunkIfPresent(long i) {
      return this.visibleChunkMap.get(i);
   }

   protected IntSupplier getChunkQueueLevel(long i) {
      return () -> {
         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
         return chunkholder == null ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1 : Math.min(chunkholder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
      };
   }

   public String getChunkDebugData(ChunkPos chunkpos) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(chunkpos.toLong());
      if (chunkholder == null) {
         return "null";
      } else {
         String s = chunkholder.getTicketLevel() + "\n";
         ChunkStatus chunkstatus = chunkholder.getLastAvailableStatus();
         ChunkAccess chunkaccess = chunkholder.getLastAvailable();
         if (chunkstatus != null) {
            s = s + "St: \u00a7" + chunkstatus.getIndex() + chunkstatus + "\u00a7r\n";
         }

         if (chunkaccess != null) {
            s = s + "Ch: \u00a7" + chunkaccess.getStatus().getIndex() + chunkaccess.getStatus() + "\u00a7r\n";
         }

         FullChunkStatus fullchunkstatus = chunkholder.getFullStatus();
         s = s + "\u00a7" + fullchunkstatus.ordinal() + fullchunkstatus;
         return s + "\u00a7r";
      }
   }

   private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkHolder chunkholder, int i, IntFunction<ChunkStatus> intfunction) {
      if (i == 0) {
         ChunkStatus chunkstatus = intfunction.apply(0);
         return chunkholder.getOrScheduleFuture(chunkstatus, this).thenApply((either1) -> either1.mapLeft(List::of));
      } else {
         List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> list = new ArrayList<>();
         List<ChunkHolder> list1 = new ArrayList<>();
         ChunkPos chunkpos = chunkholder.getPos();
         int j = chunkpos.x;
         int k = chunkpos.z;

         for(int l = -i; l <= i; ++l) {
            for(int i1 = -i; i1 <= i; ++i1) {
               int j1 = Math.max(Math.abs(i1), Math.abs(l));
               final ChunkPos chunkpos1 = new ChunkPos(j + i1, k + l);
               long k1 = chunkpos1.toLong();
               ChunkHolder chunkholder1 = this.getUpdatingChunkIfPresent(k1);
               if (chunkholder1 == null) {
                  return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                     public String toString() {
                        return "Unloaded " + chunkpos1;
                     }
                  }));
               }

               ChunkStatus chunkstatus1 = intfunction.apply(j1);
               CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder1.getOrScheduleFuture(chunkstatus1, this);
               list1.add(chunkholder1);
               list.add(completablefuture);
            }
         }

         CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completablefuture1 = Util.sequence(list);
         CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture2 = completablefuture1.thenApply((list2) -> {
            List<ChunkAccess> list3 = Lists.newArrayList();
            int k2 = 0;

            for(final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either : list2) {
               if (either == null) {
                  throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
               }

               Optional<ChunkAccess> optional = either.left();
               if (!optional.isPresent()) {
                  final int l2 = k2;
                  return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                     public String toString() {
                        return "Unloaded " + new ChunkPos(j + l2 % (i * 2 + 1), k + l2 / (i * 2 + 1)) + " " + either.right().get();
                     }
                  });
               }

               list3.add(optional.get());
               ++k2;
            }

            return Either.left(list3);
         });

         for(ChunkHolder chunkholder2 : list1) {
            chunkholder2.addSaveDependency("getChunkRangeFuture " + chunkpos + " " + i, completablefuture2);
         }

         return completablefuture2;
      }
   }

   public ReportedException debugFuturesAndCreateReportedException(IllegalStateException illegalstateexception, String s) {
      StringBuilder stringbuilder = new StringBuilder();
      Consumer<ChunkHolder> consumer = (chunkholder) -> chunkholder.getAllFutures().forEach((pair) -> {
            ChunkStatus chunkstatus = pair.getFirst();
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = pair.getSecond();
            if (completablefuture != null && completablefuture.isDone() && completablefuture.join() == null) {
               stringbuilder.append((Object)chunkholder.getPos()).append(" - status: ").append((Object)chunkstatus).append(" future: ").append((Object)completablefuture).append(System.lineSeparator());
            }

         });
      stringbuilder.append("Updating:").append(System.lineSeparator());
      this.updatingChunkMap.values().forEach(consumer);
      stringbuilder.append("Visible:").append(System.lineSeparator());
      this.visibleChunkMap.values().forEach(consumer);
      CrashReport crashreport = CrashReport.forThrowable(illegalstateexception, "Chunk loading");
      CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk loading");
      crashreportcategory.setDetail("Details", s);
      crashreportcategory.setDetail("Futures", stringbuilder);
      return new ReportedException(crashreport);
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkHolder chunkholder) {
      return this.getChunkRangeFuture(chunkholder, 2, (i) -> ChunkStatus.FULL).thenApplyAsync((either) -> either.mapLeft((list) -> (LevelChunk)list.get(list.size() / 2)), this.mainThreadExecutor);
   }

   @Nullable
   ChunkHolder updateChunkScheduling(long i, int j, @Nullable ChunkHolder chunkholder, int k) {
      if (!ChunkLevel.isLoaded(k) && !ChunkLevel.isLoaded(j)) {
         return chunkholder;
      } else {
         if (chunkholder != null) {
            chunkholder.setTicketLevel(j);
         }

         if (chunkholder != null) {
            if (!ChunkLevel.isLoaded(j)) {
               this.toDrop.add(i);
            } else {
               this.toDrop.remove(i);
            }
         }

         if (ChunkLevel.isLoaded(j) && chunkholder == null) {
            chunkholder = this.pendingUnloads.remove(i);
            if (chunkholder != null) {
               chunkholder.setTicketLevel(j);
            } else {
               chunkholder = new ChunkHolder(new ChunkPos(i), j, this.level, this.lightEngine, this.queueSorter, this);
            }

            this.updatingChunkMap.put(i, chunkholder);
            this.modified = true;
         }

         return chunkholder;
      }
   }

   public void close() throws IOException {
      try {
         this.queueSorter.close();
         this.poiManager.close();
      } finally {
         super.close();
      }

   }

   protected void saveAllChunks(boolean flag) {
      if (flag) {
         List<ChunkHolder> list = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).collect(Collectors.toList());
         MutableBoolean mutableboolean = new MutableBoolean();

         do {
            mutableboolean.setFalse();
            list.stream().map((chunkholder1) -> {
               CompletableFuture<ChunkAccess> completablefuture;
               do {
                  completablefuture = chunkholder1.getChunkToSave();
                  this.mainThreadExecutor.managedBlock(completablefuture::isDone);
               } while(completablefuture != chunkholder1.getChunkToSave());

               return completablefuture.join();
            }).filter((chunkaccess3) -> chunkaccess3 instanceof ImposterProtoChunk || chunkaccess3 instanceof LevelChunk).filter(this::save).forEach((chunkaccess2) -> mutableboolean.setTrue());
         } while(mutableboolean.isTrue());

         this.processUnloads(() -> true);
         this.flushWorker();
      } else {
         this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
      }

   }

   protected void tick(BooleanSupplier booleansupplier) {
      ProfilerFiller profilerfiller = this.level.getProfiler();
      profilerfiller.push("poi");
      this.poiManager.tick(booleansupplier);
      profilerfiller.popPush("chunk_unload");
      if (!this.level.noSave()) {
         this.processUnloads(booleansupplier);
      }

      profilerfiller.pop();
   }

   public boolean hasWork() {
      return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
   }

   private void processUnloads(BooleanSupplier booleansupplier) {
      LongIterator longiterator = this.toDrop.iterator();

      for(int i = 0; longiterator.hasNext() && (booleansupplier.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longiterator.remove()) {
         long j = longiterator.nextLong();
         ChunkHolder chunkholder = this.updatingChunkMap.remove(j);
         if (chunkholder != null) {
            this.pendingUnloads.put(j, chunkholder);
            this.modified = true;
            ++i;
            this.scheduleUnload(j, chunkholder);
         }
      }

      int k = Math.max(0, this.unloadQueue.size() - 2000);

      Runnable runnable;
      while((booleansupplier.getAsBoolean() || k > 0) && (runnable = this.unloadQueue.poll()) != null) {
         --k;
         runnable.run();
      }

      int l = 0;
      ObjectIterator<ChunkHolder> objectiterator = this.visibleChunkMap.values().iterator();

      while(l < 20 && booleansupplier.getAsBoolean() && objectiterator.hasNext()) {
         if (this.saveChunkIfNeeded(objectiterator.next())) {
            ++l;
         }
      }

   }

   private void scheduleUnload(long i, ChunkHolder chunkholder) {
      CompletableFuture<ChunkAccess> completablefuture = chunkholder.getChunkToSave();
      completablefuture.thenAcceptAsync((chunkaccess) -> {
         CompletableFuture<ChunkAccess> completablefuture2 = chunkholder.getChunkToSave();
         if (completablefuture2 != completablefuture) {
            this.scheduleUnload(i, chunkholder);
         } else {
            if (this.pendingUnloads.remove(i, chunkholder) && chunkaccess != null) {
               if (chunkaccess instanceof LevelChunk) {
                  ((LevelChunk)chunkaccess).setLoaded(false);
               }

               this.save(chunkaccess);
               if (this.entitiesInLevel.remove(i) && chunkaccess instanceof LevelChunk) {
                  LevelChunk levelchunk = (LevelChunk)chunkaccess;
                  this.level.unload(levelchunk);
               }

               this.lightEngine.updateChunkStatus(chunkaccess.getPos());
               this.lightEngine.tryScheduleUpdate();
               this.progressListener.onStatusChange(chunkaccess.getPos(), (ChunkStatus)null);
               this.chunkSaveCooldowns.remove(chunkaccess.getPos().toLong());
            }

         }
      }, this.unloadQueue::add).whenComplete((ovoid, throwable) -> {
         if (throwable != null) {
            LOGGER.error("Failed to save chunk {}", chunkholder.getPos(), throwable);
         }

      });
   }

   protected boolean promoteChunkMap() {
      if (!this.modified) {
         return false;
      } else {
         this.visibleChunkMap = this.updatingChunkMap.clone();
         this.modified = false;
         return true;
      }
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder chunkholder, ChunkStatus chunkstatus) {
      ChunkPos chunkpos = chunkholder.getPos();
      if (chunkstatus == ChunkStatus.EMPTY) {
         return this.scheduleChunkLoad(chunkpos);
      } else {
         if (chunkstatus == ChunkStatus.LIGHT) {
            this.distanceManager.addTicket(TicketType.LIGHT, chunkpos, ChunkLevel.byStatus(ChunkStatus.LIGHT), chunkpos);
         }

         if (!chunkstatus.hasLoadDependencies()) {
            Optional<ChunkAccess> optional = chunkholder.getOrScheduleFuture(chunkstatus.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left();
            if (optional.isPresent() && optional.get().getStatus().isOrAfter(chunkstatus)) {
               CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkstatus.load(this.level, this.structureTemplateManager, this.lightEngine, (chunkaccess) -> this.protoChunkToFullChunk(chunkholder), optional.get());
               this.progressListener.onStatusChange(chunkpos, chunkstatus);
               return completablefuture;
            }
         }

         return this.scheduleChunkGeneration(chunkholder, chunkstatus);
      }
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos chunkpos) {
      return this.readChunk(chunkpos).thenApply((optional1) -> optional1.filter((compoundtag) -> {
            boolean flag = isChunkDataValid(compoundtag);
            if (!flag) {
               LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)chunkpos);
            }

            return flag;
         })).thenApplyAsync((optional) -> {
         this.level.getProfiler().incrementCounter("chunkLoad");
         if (optional.isPresent()) {
            ChunkAccess chunkaccess = ChunkSerializer.read(this.level, this.poiManager, chunkpos, optional.get());
            this.markPosition(chunkpos, chunkaccess.getStatus().getChunkType());
            return Either.left(chunkaccess);
         } else {
            return Either.left(this.createEmptyChunk(chunkpos));
         }
      }, this.mainThreadExecutor).exceptionallyAsync((throwable) -> this.handleChunkLoadFailure(throwable, chunkpos), this.mainThreadExecutor);
   }

   private static boolean isChunkDataValid(CompoundTag compoundtag) {
      return compoundtag.contains("Status", 8);
   }

   private Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> handleChunkLoadFailure(Throwable throwable, ChunkPos chunkpos) {
      if (throwable instanceof ReportedException reportedexception) {
         Throwable throwable1 = reportedexception.getCause();
         if (!(throwable1 instanceof IOException)) {
            this.markPositionReplaceable(chunkpos);
            throw reportedexception;
         }

         LOGGER.error("Couldn't load chunk {}", chunkpos, throwable1);
      } else if (throwable instanceof IOException) {
         LOGGER.error("Couldn't load chunk {}", chunkpos, throwable);
      }

      return Either.left(this.createEmptyChunk(chunkpos));
   }

   private ChunkAccess createEmptyChunk(ChunkPos chunkpos) {
      this.markPositionReplaceable(chunkpos);
      return new ProtoChunk(chunkpos, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), (BlendingData)null);
   }

   private void markPositionReplaceable(ChunkPos chunkpos) {
      this.chunkTypeCache.put(chunkpos.toLong(), (byte)-1);
   }

   private byte markPosition(ChunkPos chunkpos, ChunkStatus.ChunkType chunkstatus_chunktype) {
      return this.chunkTypeCache.put(chunkpos.toLong(), (byte)(chunkstatus_chunktype == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder chunkholder, ChunkStatus chunkstatus) {
      ChunkPos chunkpos = chunkholder.getPos();
      CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkholder, chunkstatus.getRange(), (i) -> this.getDependencyStatus(chunkstatus, i));
      this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + chunkstatus);
      Executor executor = (runnable) -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkholder, runnable));
      return completablefuture.thenComposeAsync((either) -> either.map((list) -> {
            try {
               ChunkAccess chunkaccess = list.get(list.size() / 2);
               CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture1;
               if (chunkaccess.getStatus().isOrAfter(chunkstatus)) {
                  completablefuture1 = chunkstatus.load(this.level, this.structureTemplateManager, this.lightEngine, (chunkaccess2) -> this.protoChunkToFullChunk(chunkholder), chunkaccess);
               } else {
                  completablefuture1 = chunkstatus.generate(executor, this.level, this.generator, this.structureTemplateManager, this.lightEngine, (chunkaccess1) -> this.protoChunkToFullChunk(chunkholder), list);
               }

               this.progressListener.onStatusChange(chunkpos, chunkstatus);
               return completablefuture1;
            } catch (Exception var9) {
               var9.getStackTrace();
               CrashReport crashreport = CrashReport.forThrowable(var9, "Exception generating new chunk");
               CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk to be generated");
               crashreportcategory.setDetail("Location", String.format(Locale.ROOT, "%d,%d", chunkpos.x, chunkpos.z));
               crashreportcategory.setDetail("Position hash", ChunkPos.asLong(chunkpos.x, chunkpos.z));
               crashreportcategory.setDetail("Generator", this.generator);
               this.mainThreadExecutor.execute(() -> {
                  throw new ReportedException(crashreport);
               });
               throw new ReportedException(crashreport);
            }
         }, (chunkholder_chunkloadingfailure) -> {
            this.releaseLightTicket(chunkpos);
            return CompletableFuture.completedFuture(Either.right(chunkholder_chunkloadingfailure));
         }), executor);
   }

   protected void releaseLightTicket(ChunkPos chunkpos) {
      this.mainThreadExecutor.tell(Util.name(() -> this.distanceManager.removeTicket(TicketType.LIGHT, chunkpos, ChunkLevel.byStatus(ChunkStatus.LIGHT), chunkpos), () -> "release light ticket " + chunkpos));
   }

   private ChunkStatus getDependencyStatus(ChunkStatus chunkstatus, int i) {
      ChunkStatus chunkstatus1;
      if (i == 0) {
         chunkstatus1 = chunkstatus.getParent();
      } else {
         chunkstatus1 = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(chunkstatus) + i);
      }

      return chunkstatus1;
   }

   private static void postLoadProtoChunk(ServerLevel serverlevel, List<CompoundTag> list) {
      if (!list.isEmpty()) {
         serverlevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(list, serverlevel));
      }

   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder chunkholder) {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
      return completablefuture.thenApplyAsync((either) -> {
         ChunkStatus chunkstatus = ChunkLevel.generationStatus(chunkholder.getTicketLevel());
         return !chunkstatus.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft((chunkaccess) -> {
            ChunkPos chunkpos = chunkholder.getPos();
            ProtoChunk protochunk = (ProtoChunk)chunkaccess;
            LevelChunk levelchunk;
            if (protochunk instanceof ImposterProtoChunk) {
               levelchunk = ((ImposterProtoChunk)protochunk).getWrapped();
            } else {
               levelchunk = new LevelChunk(this.level, protochunk, (levelchunk2) -> postLoadProtoChunk(this.level, protochunk.getEntities()));
               chunkholder.replaceProtoChunk(new ImposterProtoChunk(levelchunk, false));
            }

            levelchunk.setFullStatus(() -> ChunkLevel.fullStatus(chunkholder.getTicketLevel()));
            levelchunk.runPostLoad();
            if (this.entitiesInLevel.add(chunkpos.toLong())) {
               levelchunk.setLoaded(true);
               levelchunk.registerAllBlockEntitiesAfterLevelLoad();
               levelchunk.registerTickContainerInLevel(this.level);
            }

            return levelchunk;
         });
      }, (runnable) -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(runnable, chunkholder.getPos().toLong(), chunkholder::getTicketLevel)));
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder chunkholder) {
      CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkholder, 1, (i) -> ChunkStatus.FULL);
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = completablefuture.thenApplyAsync((either3) -> either3.mapLeft((list) -> (LevelChunk)list.get(list.size() / 2)), (runnable1) -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkholder, runnable1))).thenApplyAsync((either2) -> either2.ifLeft((levelchunk2) -> {
            levelchunk2.postProcessGeneration();
            this.level.startTickingChunk(levelchunk2);
         }), this.mainThreadExecutor);
      completablefuture1.handle((either1, throwable) -> {
         this.tickingGenerated.getAndIncrement();
         return null;
      });
      completablefuture1.thenAcceptAsync((either) -> either.ifLeft((levelchunk) -> {
            MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject = new MutableObject<>();
            this.getPlayers(chunkholder.getPos(), false).forEach((serverplayer) -> this.playerLoadedChunk(serverplayer, mutableobject, levelchunk));
         }), (runnable) -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkholder, runnable)));
      return completablefuture1;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder chunkholder) {
      return this.getChunkRangeFuture(chunkholder, 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync((either) -> either.mapLeft((list) -> (LevelChunk)list.get(list.size() / 2)), (runnable) -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkholder, runnable)));
   }

   public int getTickingGenerated() {
      return this.tickingGenerated.get();
   }

   private boolean saveChunkIfNeeded(ChunkHolder chunkholder) {
      if (!chunkholder.wasAccessibleSinceLastSave()) {
         return false;
      } else {
         ChunkAccess chunkaccess = chunkholder.getChunkToSave().getNow((ChunkAccess)null);
         if (!(chunkaccess instanceof ImposterProtoChunk) && !(chunkaccess instanceof LevelChunk)) {
            return false;
         } else {
            long i = chunkaccess.getPos().toLong();
            long j = this.chunkSaveCooldowns.getOrDefault(i, -1L);
            long k = System.currentTimeMillis();
            if (k < j) {
               return false;
            } else {
               boolean flag1 = this.save(chunkaccess);
               chunkholder.refreshAccessibility();
               if (flag1) {
                  this.chunkSaveCooldowns.put(i, k + 10000L);
               }

               return flag1;
            }
         }
      }
   }

   private boolean save(ChunkAccess chunkaccess1) {
      this.poiManager.flush(chunkaccess1.getPos());
      if (!chunkaccess1.isUnsaved()) {
         return false;
      } else {
         chunkaccess1.setUnsaved(false);
         ChunkPos chunkpos = chunkaccess1.getPos();

         try {
            ChunkStatus chunkstatus = chunkaccess1.getStatus();
            if (chunkstatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
               if (this.isExistingChunkFull(chunkpos)) {
                  return false;
               }

               if (chunkstatus == ChunkStatus.EMPTY && chunkaccess1.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                  return false;
               }
            }

            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundtag = ChunkSerializer.write(this.level, chunkaccess1);
            this.write(chunkpos, compoundtag);
            this.markPosition(chunkpos, chunkstatus.getChunkType());
            return true;
         } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", chunkpos.x, chunkpos.z, var5);
            return false;
         }
      }
   }

   private boolean isExistingChunkFull(ChunkPos chunkpos) {
      byte b0 = this.chunkTypeCache.get(chunkpos.toLong());
      if (b0 != 0) {
         return b0 == 1;
      } else {
         CompoundTag compoundtag;
         try {
            compoundtag = this.readChunk(chunkpos).join().orElse((CompoundTag)null);
            if (compoundtag == null) {
               this.markPositionReplaceable(chunkpos);
               return false;
            }
         } catch (Exception var5) {
            LOGGER.error("Failed to read chunk {}", chunkpos, var5);
            this.markPositionReplaceable(chunkpos);
            return false;
         }

         ChunkStatus.ChunkType chunkstatus_chunktype = ChunkSerializer.getChunkTypeFromTag(compoundtag);
         return this.markPosition(chunkpos, chunkstatus_chunktype) == 1;
      }
   }

   protected void setViewDistance(int i) {
      int j = Mth.clamp(i, 2, 32);
      if (j != this.viewDistance) {
         int k = this.viewDistance;
         this.viewDistance = j;
         this.distanceManager.updatePlayerTickets(this.viewDistance);

         for(ChunkHolder chunkholder : this.updatingChunkMap.values()) {
            ChunkPos chunkpos = chunkholder.getPos();
            MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject = new MutableObject<>();
            this.getPlayers(chunkpos, false).forEach((serverplayer) -> {
               SectionPos sectionpos = serverplayer.getLastSectionPos();
               boolean flag = isChunkInRange(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), k);
               boolean flag1 = isChunkInRange(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), this.viewDistance);
               this.updateChunkTracking(serverplayer, chunkpos, mutableobject, flag, flag1);
            });
         }
      }

   }

   protected void updateChunkTracking(ServerPlayer serverplayer, ChunkPos chunkpos, MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject, boolean flag, boolean flag1) {
      if (serverplayer.level() == this.level) {
         if (flag1 && !flag) {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(chunkpos.toLong());
            if (chunkholder != null) {
               LevelChunk levelchunk = chunkholder.getTickingChunk();
               if (levelchunk != null) {
                  this.playerLoadedChunk(serverplayer, mutableobject, levelchunk);
               }

               DebugPackets.sendPoiPacketsForChunk(this.level, chunkpos);
            }
         }

         if (!flag1 && flag) {
            serverplayer.untrackChunk(chunkpos);
         }

      }
   }

   public int size() {
      return this.visibleChunkMap.size();
   }

   public net.minecraft.server.level.DistanceManager getDistanceManager() {
      return this.distanceManager;
   }

   protected Iterable<ChunkHolder> getChunks() {
      return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
   }

   void dumpChunks(Writer writer) throws IOException {
      CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build(writer);
      TickingTracker tickingtracker = this.distanceManager.tickingTracker();

      for(Long2ObjectMap.Entry<ChunkHolder> long2objectmap_entry : this.visibleChunkMap.long2ObjectEntrySet()) {
         long i = long2objectmap_entry.getLongKey();
         ChunkPos chunkpos = new ChunkPos(i);
         ChunkHolder chunkholder = long2objectmap_entry.getValue();
         Optional<ChunkAccess> optional = Optional.ofNullable(chunkholder.getLastAvailable());
         Optional<LevelChunk> optional1 = optional.flatMap((chunkaccess) -> chunkaccess instanceof LevelChunk ? Optional.of((LevelChunk)chunkaccess) : Optional.empty());
         csvoutput.writeRow(chunkpos.x, chunkpos.z, chunkholder.getTicketLevel(), optional.isPresent(), optional.map(ChunkAccess::getStatus).orElse((ChunkStatus)null), optional1.map(LevelChunk::getFullStatus).orElse((FullChunkStatus)null), printFuture(chunkholder.getFullChunkFuture()), printFuture(chunkholder.getTickingChunkFuture()), printFuture(chunkholder.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(i), this.anyPlayerCloseEnoughForSpawning(chunkpos), optional1.map((levelchunk2) -> levelchunk2.getBlockEntities().size()).orElse(0), tickingtracker.getTicketDebugString(i), tickingtracker.getLevel(i), optional1.map((levelchunk1) -> levelchunk1.getBlockTicks().count()).orElse(0), optional1.map((levelchunk) -> levelchunk.getFluidTicks().count()).orElse(0));
      }

   }

   private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture) {
      try {
         Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
         return either != null ? either.map((levelchunk) -> "done", (chunkholder_chunkloadingfailure) -> "unloaded") : "not completed";
      } catch (CompletionException var2) {
         return "failed " + var2.getCause().getMessage();
      } catch (CancellationException var3) {
         return "cancelled";
      }
   }

   private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos chunkpos) {
      return this.read(chunkpos).thenApplyAsync((optional) -> optional.map(this::upgradeChunkTag), Util.backgroundExecutor());
   }

   private CompoundTag upgradeChunkTag(CompoundTag compoundtag) {
      return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundtag, this.generator.getTypeNameForDataFixer());
   }

   boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkpos) {
      long i = chunkpos.toLong();
      if (!this.distanceManager.hasPlayersNearby(i)) {
         return false;
      } else {
         for(ServerPlayer serverplayer : this.playerMap.getPlayers(i)) {
            if (this.playerIsCloseEnoughForSpawning(serverplayer, chunkpos)) {
               return true;
            }
         }

         return false;
      }
   }

   public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos chunkpos) {
      long i = chunkpos.toLong();
      if (!this.distanceManager.hasPlayersNearby(i)) {
         return List.of();
      } else {
         ImmutableList.Builder<ServerPlayer> immutablelist_builder = ImmutableList.builder();

         for(ServerPlayer serverplayer : this.playerMap.getPlayers(i)) {
            if (this.playerIsCloseEnoughForSpawning(serverplayer, chunkpos)) {
               immutablelist_builder.add(serverplayer);
            }
         }

         return immutablelist_builder.build();
      }
   }

   private boolean playerIsCloseEnoughForSpawning(ServerPlayer serverplayer, ChunkPos chunkpos) {
      if (serverplayer.isSpectator()) {
         return false;
      } else {
         double d0 = euclideanDistanceSquared(chunkpos, serverplayer);
         return d0 < 16384.0D;
      }
   }

   private boolean skipPlayer(ServerPlayer serverplayer) {
      return serverplayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
   }

   void updatePlayerStatus(ServerPlayer serverplayer, boolean flag) {
      boolean flag1 = this.skipPlayer(serverplayer);
      boolean flag2 = this.playerMap.ignoredOrUnknown(serverplayer);
      int i = SectionPos.blockToSectionCoord(serverplayer.getBlockX());
      int j = SectionPos.blockToSectionCoord(serverplayer.getBlockZ());
      if (flag) {
         this.playerMap.addPlayer(ChunkPos.asLong(i, j), serverplayer, flag1);
         this.updatePlayerPos(serverplayer);
         if (!flag1) {
            this.distanceManager.addPlayer(SectionPos.of(serverplayer), serverplayer);
         }
      } else {
         SectionPos sectionpos = serverplayer.getLastSectionPos();
         this.playerMap.removePlayer(sectionpos.chunk().toLong(), serverplayer);
         if (!flag2) {
            this.distanceManager.removePlayer(sectionpos, serverplayer);
         }
      }

      for(int k = i - this.viewDistance - 1; k <= i + this.viewDistance + 1; ++k) {
         for(int l = j - this.viewDistance - 1; l <= j + this.viewDistance + 1; ++l) {
            if (isChunkInRange(k, l, i, j, this.viewDistance)) {
               ChunkPos chunkpos = new ChunkPos(k, l);
               this.updateChunkTracking(serverplayer, chunkpos, new MutableObject<>(), !flag, flag);
            }
         }
      }

   }

   private SectionPos updatePlayerPos(ServerPlayer serverplayer) {
      SectionPos sectionpos = SectionPos.of(serverplayer);
      serverplayer.setLastSectionPos(sectionpos);
      serverplayer.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionpos.x(), sectionpos.z()));
      return sectionpos;
   }

   public void move(ServerPlayer serverplayer) {
      for(ChunkMap.TrackedEntity chunkmap_trackedentity : this.entityMap.values()) {
         if (chunkmap_trackedentity.entity == serverplayer) {
            chunkmap_trackedentity.updatePlayers(this.level.players());
         } else {
            chunkmap_trackedentity.updatePlayer(serverplayer);
         }
      }

      int i = SectionPos.blockToSectionCoord(serverplayer.getBlockX());
      int j = SectionPos.blockToSectionCoord(serverplayer.getBlockZ());
      SectionPos sectionpos = serverplayer.getLastSectionPos();
      SectionPos sectionpos1 = SectionPos.of(serverplayer);
      long k = sectionpos.chunk().toLong();
      long l = sectionpos1.chunk().toLong();
      boolean flag = this.playerMap.ignored(serverplayer);
      boolean flag1 = this.skipPlayer(serverplayer);
      boolean flag2 = sectionpos.asLong() != sectionpos1.asLong();
      if (flag2 || flag != flag1) {
         this.updatePlayerPos(serverplayer);
         if (!flag) {
            this.distanceManager.removePlayer(sectionpos, serverplayer);
         }

         if (!flag1) {
            this.distanceManager.addPlayer(sectionpos1, serverplayer);
         }

         if (!flag && flag1) {
            this.playerMap.ignorePlayer(serverplayer);
         }

         if (flag && !flag1) {
            this.playerMap.unIgnorePlayer(serverplayer);
         }

         if (k != l) {
            this.playerMap.updatePlayer(k, l, serverplayer);
         }
      }

      int i1 = sectionpos.x();
      int j1 = sectionpos.z();
      int k1 = this.viewDistance + 1;
      if (Math.abs(i1 - i) <= k1 * 2 && Math.abs(j1 - j) <= k1 * 2) {
         int l1 = Math.min(i, i1) - k1;
         int i2 = Math.min(j, j1) - k1;
         int j2 = Math.max(i, i1) + k1;
         int k2 = Math.max(j, j1) + k1;

         for(int l2 = l1; l2 <= j2; ++l2) {
            for(int i3 = i2; i3 <= k2; ++i3) {
               boolean flag3 = isChunkInRange(l2, i3, i1, j1, this.viewDistance);
               boolean flag4 = isChunkInRange(l2, i3, i, j, this.viewDistance);
               this.updateChunkTracking(serverplayer, new ChunkPos(l2, i3), new MutableObject<>(), flag3, flag4);
            }
         }
      } else {
         for(int j3 = i1 - k1; j3 <= i1 + k1; ++j3) {
            for(int k3 = j1 - k1; k3 <= j1 + k1; ++k3) {
               if (isChunkInRange(j3, k3, i1, j1, this.viewDistance)) {
                  boolean flag5 = true;
                  boolean flag6 = false;
                  this.updateChunkTracking(serverplayer, new ChunkPos(j3, k3), new MutableObject<>(), true, false);
               }
            }
         }

         for(int l3 = i - k1; l3 <= i + k1; ++l3) {
            for(int i4 = j - k1; i4 <= j + k1; ++i4) {
               if (isChunkInRange(l3, i4, i, j, this.viewDistance)) {
                  boolean flag7 = false;
                  boolean flag8 = true;
                  this.updateChunkTracking(serverplayer, new ChunkPos(l3, i4), new MutableObject<>(), false, true);
               }
            }
         }
      }

   }

   public List<ServerPlayer> getPlayers(ChunkPos chunkpos, boolean flag) {
      Set<ServerPlayer> set = this.playerMap.getPlayers(chunkpos.toLong());
      ImmutableList.Builder<ServerPlayer> immutablelist_builder = ImmutableList.builder();

      for(ServerPlayer serverplayer : set) {
         SectionPos sectionpos = serverplayer.getLastSectionPos();
         if (flag && isChunkOnRangeBorder(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), this.viewDistance) || !flag && isChunkInRange(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), this.viewDistance)) {
            immutablelist_builder.add(serverplayer);
         }
      }

      return immutablelist_builder.build();
   }

   protected void addEntity(Entity entity) {
      if (!(entity instanceof EnderDragonPart)) {
         EntityType<?> entitytype = entity.getType();
         int i = entitytype.clientTrackingRange() * 16;
         if (i != 0) {
            int j = entitytype.updateInterval();
            if (this.entityMap.containsKey(entity.getId())) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
            } else {
               ChunkMap.TrackedEntity chunkmap_trackedentity = new ChunkMap.TrackedEntity(entity, i, j, entitytype.trackDeltas());
               this.entityMap.put(entity.getId(), chunkmap_trackedentity);
               chunkmap_trackedentity.updatePlayers(this.level.players());
               if (entity instanceof ServerPlayer) {
                  ServerPlayer serverplayer = (ServerPlayer)entity;
                  this.updatePlayerStatus(serverplayer, true);

                  for(ChunkMap.TrackedEntity chunkmap_trackedentity1 : this.entityMap.values()) {
                     if (chunkmap_trackedentity1.entity != serverplayer) {
                        chunkmap_trackedentity1.updatePlayer(serverplayer);
                     }
                  }
               }

            }
         }
      }
   }

   protected void removeEntity(Entity entity) {
      if (entity instanceof ServerPlayer serverplayer) {
         this.updatePlayerStatus(serverplayer, false);

         for(ChunkMap.TrackedEntity chunkmap_trackedentity : this.entityMap.values()) {
            chunkmap_trackedentity.removePlayer(serverplayer);
         }
      }

      ChunkMap.TrackedEntity chunkmap_trackedentity1 = this.entityMap.remove(entity.getId());
      if (chunkmap_trackedentity1 != null) {
         chunkmap_trackedentity1.broadcastRemoved();
      }

   }

   protected void tick() {
      List<ServerPlayer> list = Lists.newArrayList();
      List<ServerPlayer> list1 = this.level.players();

      for(ChunkMap.TrackedEntity chunkmap_trackedentity : this.entityMap.values()) {
         SectionPos sectionpos = chunkmap_trackedentity.lastSectionPos;
         SectionPos sectionpos1 = SectionPos.of(chunkmap_trackedentity.entity);
         boolean flag = !Objects.equals(sectionpos, sectionpos1);
         if (flag) {
            chunkmap_trackedentity.updatePlayers(list1);
            Entity entity = chunkmap_trackedentity.entity;
            if (entity instanceof ServerPlayer) {
               list.add((ServerPlayer)entity);
            }

            chunkmap_trackedentity.lastSectionPos = sectionpos1;
         }

         if (flag || this.distanceManager.inEntityTickingRange(sectionpos1.chunk().toLong())) {
            chunkmap_trackedentity.serverEntity.sendChanges();
         }
      }

      if (!list.isEmpty()) {
         for(ChunkMap.TrackedEntity chunkmap_trackedentity1 : this.entityMap.values()) {
            chunkmap_trackedentity1.updatePlayers(list);
         }
      }

   }

   public void broadcast(Entity entity, Packet<?> packet) {
      ChunkMap.TrackedEntity chunkmap_trackedentity = this.entityMap.get(entity.getId());
      if (chunkmap_trackedentity != null) {
         chunkmap_trackedentity.broadcast(packet);
      }

   }

   protected void broadcastAndSend(Entity entity, Packet<?> packet) {
      ChunkMap.TrackedEntity chunkmap_trackedentity = this.entityMap.get(entity.getId());
      if (chunkmap_trackedentity != null) {
         chunkmap_trackedentity.broadcastAndSend(packet);
      }

   }

   public void resendBiomesForChunks(List<ChunkAccess> list) {
      Map<ServerPlayer, List<LevelChunk>> map = new HashMap<>();

      for(ChunkAccess chunkaccess : list) {
         ChunkPos chunkpos = chunkaccess.getPos();
         LevelChunk levelchunk1;
         if (chunkaccess instanceof LevelChunk levelchunk) {
            levelchunk1 = levelchunk;
         } else {
            levelchunk1 = this.level.getChunk(chunkpos.x, chunkpos.z);
         }

         for(ServerPlayer serverplayer : this.getPlayers(chunkpos, false)) {
            map.computeIfAbsent(serverplayer, (serverplayer2) -> new ArrayList()).add(levelchunk1);
         }
      }

      map.forEach((serverplayer1, list1) -> serverplayer1.connection.send(ClientboundChunksBiomesPacket.forChunks(list1)));
   }

   private void playerLoadedChunk(ServerPlayer serverplayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject, LevelChunk levelchunk) {
      if (mutableobject.getValue() == null) {
         mutableobject.setValue(new ClientboundLevelChunkWithLightPacket(levelchunk, this.lightEngine, (BitSet)null, (BitSet)null));
      }

      serverplayer.trackChunk(levelchunk.getPos(), mutableobject.getValue());
      DebugPackets.sendPoiPacketsForChunk(this.level, levelchunk.getPos());
      List<Entity> list = Lists.newArrayList();
      List<Entity> list1 = Lists.newArrayList();

      for(ChunkMap.TrackedEntity chunkmap_trackedentity : this.entityMap.values()) {
         Entity entity = chunkmap_trackedentity.entity;
         if (entity != serverplayer && entity.chunkPosition().equals(levelchunk.getPos())) {
            chunkmap_trackedentity.updatePlayer(serverplayer);
            if (entity instanceof Mob && ((Mob)entity).getLeashHolder() != null) {
               list.add(entity);
            }

            if (!entity.getPassengers().isEmpty()) {
               list1.add(entity);
            }
         }
      }

      if (!list.isEmpty()) {
         for(Entity entity1 : list) {
            serverplayer.connection.send(new ClientboundSetEntityLinkPacket(entity1, ((Mob)entity1).getLeashHolder()));
         }
      }

      if (!list1.isEmpty()) {
         for(Entity entity2 : list1) {
            serverplayer.connection.send(new ClientboundSetPassengersPacket(entity2));
         }
      }

   }

   protected PoiManager getPoiManager() {
      return this.poiManager;
   }

   public String getStorageName() {
      return this.storageName;
   }

   void onFullChunkStatusChange(ChunkPos chunkpos, FullChunkStatus fullchunkstatus) {
      this.chunkStatusListener.onChunkStatusChange(chunkpos, fullchunkstatus);
   }

   class DistanceManager extends net.minecraft.server.level.DistanceManager {
      protected DistanceManager(Executor executor, Executor executor1) {
         super(executor, executor1);
      }

      protected boolean isChunkToRemove(long i) {
         return ChunkMap.this.toDrop.contains(i);
      }

      @Nullable
      protected ChunkHolder getChunk(long i) {
         return ChunkMap.this.getUpdatingChunkIfPresent(i);
      }

      @Nullable
      protected ChunkHolder updateChunkScheduling(long i, int j, @Nullable ChunkHolder chunkholder, int k) {
         return ChunkMap.this.updateChunkScheduling(i, j, chunkholder, k);
      }
   }

   class TrackedEntity {
      final ServerEntity serverEntity;
      final Entity entity;
      private final int range;
      SectionPos lastSectionPos;
      private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

      public TrackedEntity(Entity entity, int i, int j, boolean flag) {
         this.serverEntity = new ServerEntity(ChunkMap.this.level, entity, j, flag, this::broadcast);
         this.entity = entity;
         this.range = i;
         this.lastSectionPos = SectionPos.of(entity);
      }

      public boolean equals(Object object) {
         if (object instanceof ChunkMap.TrackedEntity) {
            return ((ChunkMap.TrackedEntity)object).entity.getId() == this.entity.getId();
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.entity.getId();
      }

      public void broadcast(Packet<?> packet) {
         for(ServerPlayerConnection serverplayerconnection : this.seenBy) {
            serverplayerconnection.send(packet);
         }

      }

      public void broadcastAndSend(Packet<?> packet) {
         this.broadcast(packet);
         if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(packet);
         }

      }

      public void broadcastRemoved() {
         for(ServerPlayerConnection serverplayerconnection : this.seenBy) {
            this.serverEntity.removePairing(serverplayerconnection.getPlayer());
         }

      }

      public void removePlayer(ServerPlayer serverplayer) {
         if (this.seenBy.remove(serverplayer.connection)) {
            this.serverEntity.removePairing(serverplayer);
         }

      }

      public void updatePlayer(ServerPlayer serverplayer) {
         if (serverplayer != this.entity) {
            Vec3 vec3 = serverplayer.position().subtract(this.entity.position());
            double d0 = (double)Math.min(this.getEffectiveRange(), ChunkMap.this.viewDistance * 16);
            double d1 = vec3.x * vec3.x + vec3.z * vec3.z;
            double d2 = d0 * d0;
            boolean flag = d1 <= d2 && this.entity.broadcastToPlayer(serverplayer);
            if (flag) {
               if (this.seenBy.add(serverplayer.connection)) {
                  this.serverEntity.addPairing(serverplayer);
               }
            } else if (this.seenBy.remove(serverplayer.connection)) {
               this.serverEntity.removePairing(serverplayer);
            }

         }
      }

      private int scaledRange(int i) {
         return ChunkMap.this.level.getServer().getScaledTrackingDistance(i);
      }

      private int getEffectiveRange() {
         int i = this.range;

         for(Entity entity : this.entity.getIndirectPassengers()) {
            int j = entity.getType().clientTrackingRange() * 16;
            if (j > i) {
               i = j;
            }
         }

         return this.scaledRange(i);
      }

      public void updatePlayers(List<ServerPlayer> list) {
         for(ServerPlayer serverplayer : list) {
            this.updatePlayer(serverplayer);
         }

      }
   }
}
