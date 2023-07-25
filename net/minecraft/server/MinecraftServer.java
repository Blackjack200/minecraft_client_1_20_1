package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements CommandSource, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_BRAND = "vanilla";
   private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
   private static final int TICK_STATS_SPAN = 100;
   public static final int MS_PER_TICK = 50;
   private static final int OVERLOADED_THRESHOLD = 2000;
   private static final int OVERLOADED_WARNING_INTERVAL = 15000;
   private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
   private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
   public static final int START_CHUNK_RADIUS = 11;
   private static final int START_TICKING_CHUNK_COUNT = 441;
   private static final int AUTOSAVE_INTERVAL = 6000;
   private static final int MAX_TICK_LATENCY = 3;
   public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
   public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT);
   private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
   public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
   protected final LevelStorageSource.LevelStorageAccess storageSource;
   protected final PlayerDataStorage playerDataStorage;
   private final List<Runnable> tickables = Lists.newArrayList();
   private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
   private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
   private Consumer<ProfileResults> onMetricsRecordingStopped = (profileresults) -> this.stopRecordingMetrics();
   private Consumer<Path> onMetricsRecordingFinished = (path) -> {
   };
   private boolean willStartRecordingMetrics;
   @Nullable
   private MinecraftServer.TimeProfiler debugCommandProfiler;
   private boolean debugCommandProfilerDelayStart;
   private final ServerConnectionListener connection;
   private final ChunkProgressListenerFactory progressListenerFactory;
   @Nullable
   private ServerStatus status;
   @Nullable
   private ServerStatus.Favicon statusIcon;
   private final RandomSource random = RandomSource.create();
   private final DataFixer fixerUpper;
   private String localIp;
   private int port = -1;
   private final LayeredRegistryAccess<RegistryLayer> registries;
   private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
   private PlayerList playerList;
   private volatile boolean running = true;
   private boolean stopped;
   private int tickCount;
   protected final Proxy proxy;
   private boolean onlineMode;
   private boolean preventProxyConnections;
   private boolean pvp;
   private boolean allowFlight;
   @Nullable
   private String motd;
   private int playerIdleTimeout;
   public final long[] tickTimes = new long[100];
   @Nullable
   private KeyPair keyPair;
   @Nullable
   private GameProfile singleplayerProfile;
   private boolean isDemo;
   private volatile boolean isReady;
   private long lastOverloadWarning;
   protected final Services services;
   private long lastServerStatus;
   private final Thread serverThread;
   private long nextTickTime = Util.getMillis();
   private long delayedTasksMaxNextTickTime;
   private boolean mayHaveDelayedTasks;
   private final PackRepository packRepository;
   private final ServerScoreboard scoreboard = new ServerScoreboard(this);
   @Nullable
   private CommandStorage commandStorage;
   private final CustomBossEvents customBossEvents = new CustomBossEvents();
   private final ServerFunctionManager functionManager;
   private final FrameTimer frameTimer = new FrameTimer();
   private boolean enforceWhitelist;
   private float averageTickTime;
   private final Executor executor;
   @Nullable
   private String serverId;
   private MinecraftServer.ReloadableResources resources;
   private final StructureTemplateManager structureTemplateManager;
   protected final WorldData worldData;
   private volatile boolean isSaving;

   public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
      AtomicReference<S> atomicreference = new AtomicReference<>();
      Thread thread = new Thread(() -> atomicreference.get().runServer(), "Server thread");
      thread.setUncaughtExceptionHandler((thread1, throwable) -> LOGGER.error("Uncaught exception in server thread", throwable));
      if (Runtime.getRuntime().availableProcessors() > 4) {
         thread.setPriority(8);
      }

      S minecraftserver = function.apply(thread);
      atomicreference.set(minecraftserver);
      thread.start();
      return minecraftserver;
   }

   public MinecraftServer(Thread thread, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, PackRepository packrepository, WorldStem worldstem, Proxy proxy, DataFixer datafixer, Services services, ChunkProgressListenerFactory chunkprogresslistenerfactory) {
      super("Server");
      this.registries = worldstem.registries();
      this.worldData = worldstem.worldData();
      if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
         throw new IllegalStateException("Missing Overworld dimension data");
      } else {
         this.proxy = proxy;
         this.packRepository = packrepository;
         this.resources = new MinecraftServer.ReloadableResources(worldstem.resourceManager(), worldstem.dataPackResources());
         this.services = services;
         if (services.profileCache() != null) {
            services.profileCache().setExecutor(this);
         }

         this.connection = new ServerConnectionListener(this);
         this.progressListenerFactory = chunkprogresslistenerfactory;
         this.storageSource = levelstoragesource_levelstorageaccess;
         this.playerDataStorage = levelstoragesource_levelstorageaccess.createPlayerStorage();
         this.fixerUpper = datafixer;
         this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
         HolderGetter<Block> holdergetter = this.registries.compositeAccess().<Block>registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(this.worldData.enabledFeatures());
         this.structureTemplateManager = new StructureTemplateManager(worldstem.resourceManager(), levelstoragesource_levelstorageaccess, datafixer, holdergetter);
         this.serverThread = thread;
         this.executor = Util.backgroundExecutor();
      }
   }

   private void readScoreboard(DimensionDataStorage dimensiondatastorage) {
      dimensiondatastorage.computeIfAbsent(this.getScoreboard()::createData, this.getScoreboard()::createData, "scoreboard");
   }

   protected abstract boolean initServer() throws IOException;

   protected void loadLevel() {
      if (!JvmProfiler.INSTANCE.isRunning()) {
      }

      boolean flag = false;
      ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onWorldLoadedStarted();
      this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
      ChunkProgressListener chunkprogresslistener = this.progressListenerFactory.create(11);
      this.createLevels(chunkprogresslistener);
      this.forceDifficulty();
      this.prepareLevels(chunkprogresslistener);
      if (profiledduration != null) {
         profiledduration.finish();
      }

      if (flag) {
         try {
            JvmProfiler.INSTANCE.stop();
         } catch (Throwable var5) {
            LOGGER.warn("Failed to stop JFR profiling", var5);
         }
      }

   }

   protected void forceDifficulty() {
   }

   protected void createLevels(ChunkProgressListener chunkprogresslistener) {
      ServerLevelData serverleveldata = this.worldData.overworldData();
      boolean flag = this.worldData.isDebugWorld();
      Registry<LevelStem> registry = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
      WorldOptions worldoptions = this.worldData.worldGenOptions();
      long i = worldoptions.seed();
      long j = BiomeManager.obfuscateSeed(i);
      List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverleveldata));
      LevelStem levelstem = registry.get(LevelStem.OVERWORLD);
      ServerLevel serverlevel = new ServerLevel(this, this.executor, this.storageSource, serverleveldata, Level.OVERWORLD, levelstem, chunkprogresslistener, flag, j, list, true, (RandomSequences)null);
      this.levels.put(Level.OVERWORLD, serverlevel);
      DimensionDataStorage dimensiondatastorage = serverlevel.getDataStorage();
      this.readScoreboard(dimensiondatastorage);
      this.commandStorage = new CommandStorage(dimensiondatastorage);
      WorldBorder worldborder = serverlevel.getWorldBorder();
      if (!serverleveldata.isInitialized()) {
         try {
            setInitialSpawn(serverlevel, serverleveldata, worldoptions.generateBonusChest(), flag);
            serverleveldata.setInitialized(true);
            if (flag) {
               this.setupDebugLevel(this.worldData);
            }
         } catch (Throwable var23) {
            CrashReport crashreport = CrashReport.forThrowable(var23, "Exception initializing level");

            try {
               serverlevel.fillReportDetails(crashreport);
            } catch (Throwable var22) {
            }

            throw new ReportedException(crashreport);
         }

         serverleveldata.setInitialized(true);
      }

      this.getPlayerList().addWorldborderListener(serverlevel);
      if (this.worldData.getCustomBossEvents() != null) {
         this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
      }

      RandomSequences randomsequences = serverlevel.getRandomSequences();

      for(Map.Entry<ResourceKey<LevelStem>, LevelStem> map_entry : registry.entrySet()) {
         ResourceKey<LevelStem> resourcekey = map_entry.getKey();
         if (resourcekey != LevelStem.OVERWORLD) {
            ResourceKey<Level> resourcekey1 = ResourceKey.create(Registries.DIMENSION, resourcekey.location());
            DerivedLevelData derivedleveldata = new DerivedLevelData(this.worldData, serverleveldata);
            ServerLevel serverlevel1 = new ServerLevel(this, this.executor, this.storageSource, derivedleveldata, resourcekey1, map_entry.getValue(), chunkprogresslistener, flag, j, ImmutableList.of(), false, randomsequences);
            worldborder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverlevel1.getWorldBorder()));
            this.levels.put(resourcekey1, serverlevel1);
         }
      }

      worldborder.applySettings(serverleveldata.getWorldBorder());
   }

   private static void setInitialSpawn(ServerLevel serverlevel, ServerLevelData serverleveldata, boolean flag, boolean flag1) {
      if (flag1) {
         serverleveldata.setSpawn(BlockPos.ZERO.above(80), 0.0F);
      } else {
         ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
         ChunkPos chunkpos = new ChunkPos(serverchunkcache.randomState().sampler().findSpawnPosition());
         int i = serverchunkcache.getGenerator().getSpawnHeight(serverlevel);
         if (i < serverlevel.getMinBuildHeight()) {
            BlockPos blockpos = chunkpos.getWorldPosition();
            i = serverlevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos.getX() + 8, blockpos.getZ() + 8);
         }

         serverleveldata.setSpawn(chunkpos.getWorldPosition().offset(8, i, 8), 0.0F);
         int j = 0;
         int k = 0;
         int l = 0;
         int i1 = -1;
         int j1 = 5;

         for(int k1 = 0; k1 < Mth.square(11); ++k1) {
            if (j >= -5 && j <= 5 && k >= -5 && k <= 5) {
               BlockPos blockpos1 = PlayerRespawnLogic.getSpawnPosInChunk(serverlevel, new ChunkPos(chunkpos.x + j, chunkpos.z + k));
               if (blockpos1 != null) {
                  serverleveldata.setSpawn(blockpos1, 0.0F);
                  break;
               }
            }

            if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
               int l1 = l;
               l = -i1;
               i1 = l1;
            }

            j += l;
            k += i1;
         }

         if (flag) {
            serverlevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((registry) -> registry.getHolder(MiscOverworldFeatures.BONUS_CHEST)).ifPresent((holder_reference) -> holder_reference.value().place(serverlevel, serverchunkcache.getGenerator(), serverlevel.random, new BlockPos(serverleveldata.getXSpawn(), serverleveldata.getYSpawn(), serverleveldata.getZSpawn())));
         }

      }
   }

   private void setupDebugLevel(WorldData worlddata) {
      worlddata.setDifficulty(Difficulty.PEACEFUL);
      worlddata.setDifficultyLocked(true);
      ServerLevelData serverleveldata = worlddata.overworldData();
      serverleveldata.setRaining(false);
      serverleveldata.setThundering(false);
      serverleveldata.setClearWeatherTime(1000000000);
      serverleveldata.setDayTime(6000L);
      serverleveldata.setGameType(GameType.SPECTATOR);
   }

   private void prepareLevels(ChunkProgressListener chunkprogresslistener) {
      ServerLevel serverlevel = this.overworld();
      LOGGER.info("Preparing start region for dimension {}", (Object)serverlevel.dimension().location());
      BlockPos blockpos = serverlevel.getSharedSpawnPos();
      chunkprogresslistener.updateSpawnPos(new ChunkPos(blockpos));
      ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
      this.nextTickTime = Util.getMillis();
      serverchunkcache.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

      while(serverchunkcache.getTickingGenerated() != 441) {
         this.nextTickTime = Util.getMillis() + 10L;
         this.waitUntilNextTick();
      }

      this.nextTickTime = Util.getMillis() + 10L;
      this.waitUntilNextTick();

      for(ServerLevel serverlevel1 : this.levels.values()) {
         ForcedChunksSavedData forcedchunkssaveddata = serverlevel1.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
         if (forcedchunkssaveddata != null) {
            LongIterator longiterator = forcedchunkssaveddata.getChunks().iterator();

            while(longiterator.hasNext()) {
               long i = longiterator.nextLong();
               ChunkPos chunkpos = new ChunkPos(i);
               serverlevel1.getChunkSource().updateChunkForced(chunkpos, true);
            }
         }
      }

      this.nextTickTime = Util.getMillis() + 10L;
      this.waitUntilNextTick();
      chunkprogresslistener.stop();
      this.updateMobSpawningFlags();
   }

   public GameType getDefaultGameType() {
      return this.worldData.getGameType();
   }

   public boolean isHardcore() {
      return this.worldData.isHardcore();
   }

   public abstract int getOperatorUserPermissionLevel();

   public abstract int getFunctionCompilationLevel();

   public abstract boolean shouldRconBroadcast();

   public boolean saveAllChunks(boolean flag, boolean flag1, boolean flag2) {
      boolean flag3 = false;

      for(ServerLevel serverlevel : this.getAllLevels()) {
         if (!flag) {
            LOGGER.info("Saving chunks for level '{}'/{}", serverlevel, serverlevel.dimension().location());
         }

         serverlevel.save((ProgressListener)null, flag1, serverlevel.noSave && !flag2);
         flag3 = true;
      }

      ServerLevel serverlevel1 = this.overworld();
      ServerLevelData serverleveldata = this.worldData.overworldData();
      serverleveldata.setWorldBorder(serverlevel1.getWorldBorder().createSettings());
      this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
      this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
      if (flag1) {
         for(ServerLevel serverlevel2 : this.getAllLevels()) {
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)serverlevel2.getChunkSource().chunkMap.getStorageName());
         }

         LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
      }

      return flag3;
   }

   public boolean saveEverything(boolean flag, boolean flag1, boolean flag2) {
      boolean var4;
      try {
         this.isSaving = true;
         this.getPlayerList().saveAll();
         var4 = this.saveAllChunks(flag, flag1, flag2);
      } finally {
         this.isSaving = false;
      }

      return var4;
   }

   public void close() {
      this.stopServer();
   }

   public void stopServer() {
      if (this.metricsRecorder.isRecording()) {
         this.cancelRecordingMetrics();
      }

      LOGGER.info("Stopping server");
      if (this.getConnection() != null) {
         this.getConnection().stop();
      }

      this.isSaving = true;
      if (this.playerList != null) {
         LOGGER.info("Saving players");
         this.playerList.saveAll();
         this.playerList.removeAll();
      }

      LOGGER.info("Saving worlds");

      for(ServerLevel serverlevel : this.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.noSave = false;
         }
      }

      while(this.levels.values().stream().anyMatch((serverlevel3) -> serverlevel3.getChunkSource().chunkMap.hasWork())) {
         this.nextTickTime = Util.getMillis() + 1L;

         for(ServerLevel serverlevel1 : this.getAllLevels()) {
            serverlevel1.getChunkSource().removeTicketsOnClosing();
            serverlevel1.getChunkSource().tick(() -> true, false);
         }

         this.waitUntilNextTick();
      }

      this.saveAllChunks(false, true, false);

      for(ServerLevel serverlevel2 : this.getAllLevels()) {
         if (serverlevel2 != null) {
            try {
               serverlevel2.close();
            } catch (IOException var5) {
               LOGGER.error("Exception closing the level", (Throwable)var5);
            }
         }
      }

      this.isSaving = false;
      this.resources.close();

      try {
         this.storageSource.close();
      } catch (IOException var4) {
         LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), var4);
      }

   }

   public String getLocalIp() {
      return this.localIp;
   }

   public void setLocalIp(String s) {
      this.localIp = s;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void halt(boolean flag) {
      this.running = false;
      if (flag) {
         try {
            this.serverThread.join();
         } catch (InterruptedException var3) {
            LOGGER.error("Error while shutting down", (Throwable)var3);
         }
      }

   }

   protected void runServer() {
      try {
         if (!this.initServer()) {
            throw new IllegalStateException("Failed to initialize server");
         }

         this.nextTickTime = Util.getMillis();
         this.statusIcon = this.loadStatusIcon().orElse((ServerStatus.Favicon)null);
         this.status = this.buildServerStatus();

         while(this.running) {
            long i = Util.getMillis() - this.nextTickTime;
            if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
               long j = i / 50L;
               LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
               this.nextTickTime += j * 50L;
               this.lastOverloadWarning = this.nextTickTime;
            }

            if (this.debugCommandProfilerDelayStart) {
               this.debugCommandProfilerDelayStart = false;
               this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
            }

            this.nextTickTime += 50L;
            this.startMetricsRecordingTick();
            this.profiler.push("tick");
            this.tickServer(this::haveTime);
            this.profiler.popPush("nextTickWait");
            this.mayHaveDelayedTasks = true;
            this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
            this.waitUntilNextTick();
            this.profiler.pop();
            this.endMetricsRecordingTick();
            this.isReady = true;
            JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
         }
      } catch (Throwable var44) {
         LOGGER.error("Encountered an unexpected exception", var44);
         CrashReport crashreport = constructOrExtractCrashReport(var44);
         this.fillSystemReport(crashreport.getSystemReport());
         File file = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
         if (crashreport.saveToFile(file)) {
            LOGGER.error("This crash report has been saved to: {}", (Object)file.getAbsolutePath());
         } else {
            LOGGER.error("We were unable to save this crash report to disk.");
         }

         this.onServerCrash(crashreport);
      } finally {
         try {
            this.stopped = true;
            this.stopServer();
         } catch (Throwable var42) {
            LOGGER.error("Exception stopping the server", var42);
         } finally {
            if (this.services.profileCache() != null) {
               this.services.profileCache().clearExecutor();
            }

            this.onServerExit();
         }

      }

   }

   private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
      ReportedException reportedexception = null;

      for(Throwable throwable1 = throwable; throwable1 != null; throwable1 = throwable1.getCause()) {
         if (throwable1 instanceof ReportedException reportedexception1) {
            reportedexception = reportedexception1;
         }
      }

      CrashReport crashreport;
      if (reportedexception != null) {
         crashreport = reportedexception.getReport();
         if (reportedexception != throwable) {
            crashreport.addCategory("Wrapped in").setDetailError("Wrapping exception", throwable);
         }
      } else {
         crashreport = new CrashReport("Exception in server tick loop", throwable);
      }

      return crashreport;
   }

   private boolean haveTime() {
      return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
   }

   protected void waitUntilNextTick() {
      this.runAllTasks();
      this.managedBlock(() -> !this.haveTime());
   }

   protected TickTask wrapRunnable(Runnable runnable) {
      return new TickTask(this.tickCount, runnable);
   }

   protected boolean shouldRun(TickTask ticktask) {
      return ticktask.getTick() + 3 < this.tickCount || this.haveTime();
   }

   public boolean pollTask() {
      boolean flag = this.pollTaskInternal();
      this.mayHaveDelayedTasks = flag;
      return flag;
   }

   private boolean pollTaskInternal() {
      if (super.pollTask()) {
         return true;
      } else {
         if (this.haveTime()) {
            for(ServerLevel serverlevel : this.getAllLevels()) {
               if (serverlevel.getChunkSource().pollTask()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected void doRunTask(TickTask ticktask) {
      this.getProfiler().incrementCounter("runTask");
      super.doRunTask(ticktask);
   }

   private Optional<ServerStatus.Favicon> loadStatusIcon() {
      Optional<Path> optional = Optional.of(this.getFile("server-icon.png").toPath()).filter((path2) -> Files.isRegularFile(path2)).or(() -> this.storageSource.getIconFile().filter((path1) -> Files.isRegularFile(path1)));
      return optional.flatMap((path) -> {
         try {
            BufferedImage bufferedimage = ImageIO.read(path.toFile());
            Preconditions.checkState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
            Preconditions.checkState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            ImageIO.write(bufferedimage, "PNG", bytearrayoutputstream);
            return Optional.of(new ServerStatus.Favicon(bytearrayoutputstream.toByteArray()));
         } catch (Exception var3) {
            LOGGER.error("Couldn't load server icon", (Throwable)var3);
            return Optional.empty();
         }
      });
   }

   public Optional<Path> getWorldScreenshotFile() {
      return this.storageSource.getIconFile();
   }

   public File getServerDirectory() {
      return new File(".");
   }

   public void onServerCrash(CrashReport crashreport) {
   }

   public void onServerExit() {
   }

   public void tickServer(BooleanSupplier booleansupplier) {
      long i = Util.getNanos();
      ++this.tickCount;
      this.tickChildren(booleansupplier);
      if (i - this.lastServerStatus >= 5000000000L) {
         this.lastServerStatus = i;
         this.status = this.buildServerStatus();
      }

      if (this.tickCount % 6000 == 0) {
         LOGGER.debug("Autosave started");
         this.profiler.push("save");
         this.saveEverything(true, false, false);
         this.profiler.pop();
         LOGGER.debug("Autosave finished");
      }

      this.profiler.push("tallying");
      long j = this.tickTimes[this.tickCount % 100] = Util.getNanos() - i;
      this.averageTickTime = this.averageTickTime * 0.8F + (float)j / 1000000.0F * 0.19999999F;
      long k = Util.getNanos();
      this.frameTimer.logFrameDuration(k - i);
      this.profiler.pop();
   }

   private ServerStatus buildServerStatus() {
      ServerStatus.Players serverstatus_players = this.buildPlayerStatus();
      return new ServerStatus(Component.nullToEmpty(this.motd), Optional.of(serverstatus_players), Optional.of(ServerStatus.Version.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile());
   }

   private ServerStatus.Players buildPlayerStatus() {
      List<ServerPlayer> list = this.playerList.getPlayers();
      int i = this.getMaxPlayers();
      if (this.hidesOnlinePlayers()) {
         return new ServerStatus.Players(i, list.size(), List.of());
      } else {
         int j = Math.min(list.size(), 12);
         ObjectArrayList<GameProfile> objectarraylist = new ObjectArrayList<>(j);
         int k = Mth.nextInt(this.random, 0, list.size() - j);

         for(int l = 0; l < j; ++l) {
            ServerPlayer serverplayer = list.get(k + l);
            objectarraylist.add(serverplayer.allowsListing() ? serverplayer.getGameProfile() : ANONYMOUS_PLAYER_PROFILE);
         }

         Util.shuffle(objectarraylist, this.random);
         return new ServerStatus.Players(i, list.size(), objectarraylist);
      }
   }

   public void tickChildren(BooleanSupplier booleansupplier) {
      this.profiler.push("commandFunctions");
      this.getFunctions().tick();
      this.profiler.popPush("levels");

      for(ServerLevel serverlevel : this.getAllLevels()) {
         this.profiler.push(() -> serverlevel + " " + serverlevel.dimension().location());
         if (this.tickCount % 20 == 0) {
            this.profiler.push("timeSync");
            this.synchronizeTime(serverlevel);
            this.profiler.pop();
         }

         this.profiler.push("tick");

         try {
            serverlevel.tick(booleansupplier);
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.forThrowable(var6, "Exception ticking world");
            serverlevel.fillReportDetails(crashreport);
            throw new ReportedException(crashreport);
         }

         this.profiler.pop();
         this.profiler.pop();
      }

      this.profiler.popPush("connection");
      this.getConnection().tick();
      this.profiler.popPush("players");
      this.playerList.tick();
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         GameTestTicker.SINGLETON.tick();
      }

      this.profiler.popPush("server gui refresh");

      for(int i = 0; i < this.tickables.size(); ++i) {
         this.tickables.get(i).run();
      }

      this.profiler.pop();
   }

   private void synchronizeTime(ServerLevel serverlevel) {
      this.playerList.broadcastAll(new ClientboundSetTimePacket(serverlevel.getGameTime(), serverlevel.getDayTime(), serverlevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverlevel.dimension());
   }

   public void forceTimeSynchronization() {
      this.profiler.push("timeSync");

      for(ServerLevel serverlevel : this.getAllLevels()) {
         this.synchronizeTime(serverlevel);
      }

      this.profiler.pop();
   }

   public boolean isNetherEnabled() {
      return true;
   }

   public void addTickable(Runnable runnable) {
      this.tickables.add(runnable);
   }

   protected void setId(String s) {
      this.serverId = s;
   }

   public boolean isShutdown() {
      return !this.serverThread.isAlive();
   }

   public File getFile(String s) {
      return new File(this.getServerDirectory(), s);
   }

   public final ServerLevel overworld() {
      return this.levels.get(Level.OVERWORLD);
   }

   @Nullable
   public ServerLevel getLevel(ResourceKey<Level> resourcekey) {
      return this.levels.get(resourcekey);
   }

   public Set<ResourceKey<Level>> levelKeys() {
      return this.levels.keySet();
   }

   public Iterable<ServerLevel> getAllLevels() {
      return this.levels.values();
   }

   public String getServerVersion() {
      return SharedConstants.getCurrentVersion().getName();
   }

   public int getPlayerCount() {
      return this.playerList.getPlayerCount();
   }

   public int getMaxPlayers() {
      return this.playerList.getMaxPlayers();
   }

   public String[] getPlayerNames() {
      return this.playerList.getPlayerNamesArray();
   }

   @DontObfuscate
   public String getServerModName() {
      return "vanilla";
   }

   public SystemReport fillSystemReport(SystemReport systemreport) {
      systemreport.setDetail("Server Running", () -> Boolean.toString(this.running));
      if (this.playerList != null) {
         systemreport.setDetail("Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers());
      }

      systemreport.setDetail("Data Packs", () -> this.packRepository.getSelectedPacks().stream().map((pack) -> pack.getId() + (pack.getCompatibility().isCompatible() ? "" : " (incompatible)")).collect(Collectors.joining(", ")));
      systemreport.setDetail("Enabled Feature Flags", () -> FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
      systemreport.setDetail("World Generation", () -> this.worldData.worldGenSettingsLifecycle().toString());
      if (this.serverId != null) {
         systemreport.setDetail("Server Id", () -> this.serverId);
      }

      return this.fillServerSystemReport(systemreport);
   }

   public abstract SystemReport fillServerSystemReport(SystemReport systemreport);

   public ModCheck getModdedStatus() {
      return ModCheck.identify("vanilla", this::getServerModName, "Server", MinecraftServer.class);
   }

   public void sendSystemMessage(Component component) {
      LOGGER.info(component.getString());
   }

   public KeyPair getKeyPair() {
      return this.keyPair;
   }

   public int getPort() {
      return this.port;
   }

   public void setPort(int i) {
      this.port = i;
   }

   @Nullable
   public GameProfile getSingleplayerProfile() {
      return this.singleplayerProfile;
   }

   public void setSingleplayerProfile(@Nullable GameProfile gameprofile) {
      this.singleplayerProfile = gameprofile;
   }

   public boolean isSingleplayer() {
      return this.singleplayerProfile != null;
   }

   protected void initializeKeyPair() {
      LOGGER.info("Generating keypair");

      try {
         this.keyPair = Crypt.generateKeyPair();
      } catch (CryptException var2) {
         throw new IllegalStateException("Failed to generate key pair", var2);
      }
   }

   public void setDifficulty(Difficulty difficulty, boolean flag) {
      if (flag || !this.worldData.isDifficultyLocked()) {
         this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : difficulty);
         this.updateMobSpawningFlags();
         this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
      }
   }

   public int getScaledTrackingDistance(int i) {
      return i;
   }

   private void updateMobSpawningFlags() {
      for(ServerLevel serverlevel : this.getAllLevels()) {
         serverlevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
      }

   }

   public void setDifficultyLocked(boolean flag) {
      this.worldData.setDifficultyLocked(flag);
      this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
   }

   private void sendDifficultyUpdate(ServerPlayer serverplayer) {
      LevelData leveldata = serverplayer.level().getLevelData();
      serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
   }

   public boolean isSpawningMonsters() {
      return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
   }

   public boolean isDemo() {
      return this.isDemo;
   }

   public void setDemo(boolean flag) {
      this.isDemo = flag;
   }

   public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
      return Optional.empty();
   }

   public boolean isResourcePackRequired() {
      return this.getServerResourcePack().filter(MinecraftServer.ServerResourcePackInfo::isRequired).isPresent();
   }

   public abstract boolean isDedicatedServer();

   public abstract int getRateLimitPacketsPerSecond();

   public boolean usesAuthentication() {
      return this.onlineMode;
   }

   public void setUsesAuthentication(boolean flag) {
      this.onlineMode = flag;
   }

   public boolean getPreventProxyConnections() {
      return this.preventProxyConnections;
   }

   public void setPreventProxyConnections(boolean flag) {
      this.preventProxyConnections = flag;
   }

   public boolean isSpawningAnimals() {
      return true;
   }

   public boolean areNpcsEnabled() {
      return true;
   }

   public abstract boolean isEpollEnabled();

   public boolean isPvpAllowed() {
      return this.pvp;
   }

   public void setPvpAllowed(boolean flag) {
      this.pvp = flag;
   }

   public boolean isFlightAllowed() {
      return this.allowFlight;
   }

   public void setFlightAllowed(boolean flag) {
      this.allowFlight = flag;
   }

   public abstract boolean isCommandBlockEnabled();

   public String getMotd() {
      return this.motd;
   }

   public void setMotd(String s) {
      this.motd = s;
   }

   public boolean isStopped() {
      return this.stopped;
   }

   public PlayerList getPlayerList() {
      return this.playerList;
   }

   public void setPlayerList(PlayerList playerlist) {
      this.playerList = playerlist;
   }

   public abstract boolean isPublished();

   public void setDefaultGameType(GameType gametype) {
      this.worldData.setGameType(gametype);
   }

   @Nullable
   public ServerConnectionListener getConnection() {
      return this.connection;
   }

   public boolean isReady() {
      return this.isReady;
   }

   public boolean hasGui() {
      return false;
   }

   public boolean publishServer(@Nullable GameType gametype, boolean flag, int i) {
      return false;
   }

   public int getTickCount() {
      return this.tickCount;
   }

   public int getSpawnProtectionRadius() {
      return 16;
   }

   public boolean isUnderSpawnProtection(ServerLevel serverlevel, BlockPos blockpos, Player player) {
      return false;
   }

   public boolean repliesToStatus() {
      return true;
   }

   public boolean hidesOnlinePlayers() {
      return false;
   }

   public Proxy getProxy() {
      return this.proxy;
   }

   public int getPlayerIdleTimeout() {
      return this.playerIdleTimeout;
   }

   public void setPlayerIdleTimeout(int i) {
      this.playerIdleTimeout = i;
   }

   public MinecraftSessionService getSessionService() {
      return this.services.sessionService();
   }

   @Nullable
   public SignatureValidator getProfileKeySignatureValidator() {
      return this.services.profileKeySignatureValidator();
   }

   public GameProfileRepository getProfileRepository() {
      return this.services.profileRepository();
   }

   @Nullable
   public GameProfileCache getProfileCache() {
      return this.services.profileCache();
   }

   @Nullable
   public ServerStatus getStatus() {
      return this.status;
   }

   public void invalidateStatus() {
      this.lastServerStatus = 0L;
   }

   public int getAbsoluteMaxWorldSize() {
      return 29999984;
   }

   public boolean scheduleExecutables() {
      return super.scheduleExecutables() && !this.isStopped();
   }

   public void executeIfPossible(Runnable runnable) {
      if (this.isStopped()) {
         throw new RejectedExecutionException("Server already shutting down");
      } else {
         super.executeIfPossible(runnable);
      }
   }

   public Thread getRunningThread() {
      return this.serverThread;
   }

   public int getCompressionThreshold() {
      return 256;
   }

   public boolean enforceSecureProfile() {
      return false;
   }

   public long getNextTickTime() {
      return this.nextTickTime;
   }

   public DataFixer getFixerUpper() {
      return this.fixerUpper;
   }

   public int getSpawnRadius(@Nullable ServerLevel serverlevel) {
      return serverlevel != null ? serverlevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
   }

   public ServerAdvancementManager getAdvancements() {
      return this.resources.managers.getAdvancements();
   }

   public ServerFunctionManager getFunctions() {
      return this.functionManager;
   }

   public CompletableFuture<Void> reloadResources(Collection<String> collection) {
      RegistryAccess.Frozen registryaccess_frozen = this.registries.getAccessForLoading(RegistryLayer.RELOADABLE);
      CompletableFuture<Void> completablefuture = CompletableFuture.supplyAsync(() -> collection.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this).thenCompose((immutablelist) -> {
         CloseableResourceManager closeableresourcemanager = new MultiPackResourceManager(PackType.SERVER_DATA, immutablelist);
         return ReloadableServerResources.loadResources(closeableresourcemanager, registryaccess_frozen, this.worldData.enabledFeatures(), this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this).whenComplete((reloadableserverresources1, throwable) -> {
            if (throwable != null) {
               closeableresourcemanager.close();
            }

         }).thenApply((reloadableserverresources) -> new MinecraftServer.ReloadableResources(closeableresourcemanager, reloadableserverresources));
      }).thenAcceptAsync((minecraftserver_reloadableresources) -> {
         this.resources.close();
         this.resources = minecraftserver_reloadableresources;
         this.packRepository.setSelected(collection);
         WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(getSelectedPacks(this.packRepository), this.worldData.enabledFeatures());
         this.worldData.setDataConfiguration(worlddataconfiguration);
         this.resources.managers.updateRegistryTags(this.registryAccess());
         this.getPlayerList().saveAll();
         this.getPlayerList().reloadResources();
         this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
         this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
      }, this);
      if (this.isSameThread()) {
         this.managedBlock(completablefuture::isDone);
      }

      return completablefuture;
   }

   public static WorldDataConfiguration configurePackRepository(PackRepository packrepository, DataPackConfig datapackconfig, boolean flag, FeatureFlagSet featureflagset) {
      packrepository.reload();
      if (flag) {
         packrepository.setSelected(Collections.singleton("vanilla"));
         return WorldDataConfiguration.DEFAULT;
      } else {
         Set<String> set = Sets.newLinkedHashSet();

         for(String s : datapackconfig.getEnabled()) {
            if (packrepository.isAvailable(s)) {
               set.add(s);
            } else {
               LOGGER.warn("Missing data pack {}", (Object)s);
            }
         }

         for(Pack pack : packrepository.getAvailablePacks()) {
            String s1 = pack.getId();
            if (!datapackconfig.getDisabled().contains(s1)) {
               FeatureFlagSet featureflagset1 = pack.getRequestedFeatures();
               boolean flag1 = set.contains(s1);
               if (!flag1 && pack.getPackSource().shouldAddAutomatically()) {
                  if (featureflagset1.isSubsetOf(featureflagset)) {
                     LOGGER.info("Found new data pack {}, loading it automatically", (Object)s1);
                     set.add(s1);
                  } else {
                     LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", s1, FeatureFlags.printMissingFlags(featureflagset, featureflagset1));
                  }
               }

               if (flag1 && !featureflagset1.isSubsetOf(featureflagset)) {
                  LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", s1, FeatureFlags.printMissingFlags(featureflagset, featureflagset1));
                  set.remove(s1);
               }
            }
         }

         if (set.isEmpty()) {
            LOGGER.info("No datapacks selected, forcing vanilla");
            set.add("vanilla");
         }

         packrepository.setSelected(set);
         DataPackConfig datapackconfig1 = getSelectedPacks(packrepository);
         FeatureFlagSet featureflagset2 = packrepository.getRequestedFeatureFlags();
         return new WorldDataConfiguration(datapackconfig1, featureflagset2);
      }
   }

   private static DataPackConfig getSelectedPacks(PackRepository packrepository) {
      Collection<String> collection = packrepository.getSelectedIds();
      List<String> list = ImmutableList.copyOf(collection);
      List<String> list1 = packrepository.getAvailableIds().stream().filter((s) -> !collection.contains(s)).collect(ImmutableList.toImmutableList());
      return new DataPackConfig(list, list1);
   }

   public void kickUnlistedPlayers(CommandSourceStack commandsourcestack) {
      if (this.isEnforceWhitelist()) {
         PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
         UserWhiteList userwhitelist = playerlist.getWhiteList();

         for(ServerPlayer serverplayer : Lists.newArrayList(playerlist.getPlayers())) {
            if (!userwhitelist.isWhiteListed(serverplayer.getGameProfile())) {
               serverplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
            }
         }

      }
   }

   public PackRepository getPackRepository() {
      return this.packRepository;
   }

   public Commands getCommands() {
      return this.resources.managers.getCommands();
   }

   public CommandSourceStack createCommandSourceStack() {
      ServerLevel serverlevel = this.overworld();
      return new CommandSourceStack(this, serverlevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverlevel.getSharedSpawnPos()), Vec2.ZERO, serverlevel, 4, "Server", Component.literal("Server"), this, (Entity)null);
   }

   public boolean acceptsSuccess() {
      return true;
   }

   public boolean acceptsFailure() {
      return true;
   }

   public abstract boolean shouldInformAdmins();

   public RecipeManager getRecipeManager() {
      return this.resources.managers.getRecipeManager();
   }

   public ServerScoreboard getScoreboard() {
      return this.scoreboard;
   }

   public CommandStorage getCommandStorage() {
      if (this.commandStorage == null) {
         throw new NullPointerException("Called before server init");
      } else {
         return this.commandStorage;
      }
   }

   public LootDataManager getLootData() {
      return this.resources.managers.getLootData();
   }

   public GameRules getGameRules() {
      return this.overworld().getGameRules();
   }

   public CustomBossEvents getCustomBossEvents() {
      return this.customBossEvents;
   }

   public boolean isEnforceWhitelist() {
      return this.enforceWhitelist;
   }

   public void setEnforceWhitelist(boolean flag) {
      this.enforceWhitelist = flag;
   }

   public float getAverageTickTime() {
      return this.averageTickTime;
   }

   public int getProfilePermissions(GameProfile gameprofile) {
      if (this.getPlayerList().isOp(gameprofile)) {
         ServerOpListEntry serveroplistentry = this.getPlayerList().getOps().get(gameprofile);
         if (serveroplistentry != null) {
            return serveroplistentry.getLevel();
         } else if (this.isSingleplayerOwner(gameprofile)) {
            return 4;
         } else if (this.isSingleplayer()) {
            return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
         } else {
            return this.getOperatorUserPermissionLevel();
         }
      } else {
         return 0;
      }
   }

   public FrameTimer getFrameTimer() {
      return this.frameTimer;
   }

   public ProfilerFiller getProfiler() {
      return this.profiler;
   }

   public abstract boolean isSingleplayerOwner(GameProfile gameprofile);

   public void dumpServerProperties(Path path) throws IOException {
   }

   private void saveDebugReport(Path path) {
      Path path1 = path.resolve("levels");

      try {
         for(Map.Entry<ResourceKey<Level>, ServerLevel> map_entry : this.levels.entrySet()) {
            ResourceLocation resourcelocation = map_entry.getKey().location();
            Path path2 = path1.resolve(resourcelocation.getNamespace()).resolve(resourcelocation.getPath());
            Files.createDirectories(path2);
            map_entry.getValue().saveDebugReport(path2);
         }

         this.dumpGameRules(path.resolve("gamerules.txt"));
         this.dumpClasspath(path.resolve("classpath.txt"));
         this.dumpMiscStats(path.resolve("stats.txt"));
         this.dumpThreads(path.resolve("threads.txt"));
         this.dumpServerProperties(path.resolve("server.properties.txt"));
         this.dumpNativeModules(path.resolve("modules.txt"));
      } catch (IOException var7) {
         LOGGER.warn("Failed to save debug report", (Throwable)var7);
      }

   }

   private void dumpMiscStats(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);

      try {
         writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
         writer.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getAverageTickTime()));
         writer.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimes)));
         writer.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
      } catch (Throwable var6) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void dumpGameRules(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);

      try {
         final List<String> list = Lists.newArrayList();
         final GameRules gamerules = this.getGameRules();
         GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> gamerules_key, GameRules.Type<T> gamerules_type) {
               list.add(String.format(Locale.ROOT, "%s=%s\n", gamerules_key.getId(), gamerules.<T>getRule(gamerules_key)));
            }
         });

         for(String s : list) {
            writer.write(s);
         }
      } catch (Throwable var8) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void dumpClasspath(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);

      try {
         String s = System.getProperty("java.class.path");
         String s1 = System.getProperty("path.separator");

         for(String s2 : Splitter.on(s1).split(s)) {
            writer.write(s2);
            writer.write("\n");
         }
      } catch (Throwable var8) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void dumpThreads(Path path) throws IOException {
      ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
      ThreadInfo[] athreadinfo = threadmxbean.dumpAllThreads(true, true);
      Arrays.sort(athreadinfo, Comparator.comparing(ThreadInfo::getThreadName));
      Writer writer = Files.newBufferedWriter(path);

      try {
         for(ThreadInfo threadinfo : athreadinfo) {
            writer.write(threadinfo.toString());
            writer.write(10);
         }
      } catch (Throwable var10) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void dumpNativeModules(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);

      label49: {
         try {
            label50: {
               List<NativeModuleLister.NativeModuleInfo> list;
               try {
                  list = Lists.newArrayList(NativeModuleLister.listModules());
               } catch (Throwable var7) {
                  LOGGER.warn("Failed to list native modules", var7);
                  break label50;
               }

               list.sort(Comparator.comparing((nativemodulelister_nativemoduleinfo1) -> nativemodulelister_nativemoduleinfo1.name));
               Iterator throwable = list.iterator();

               while(true) {
                  if (!throwable.hasNext()) {
                     break label49;
                  }

                  NativeModuleLister.NativeModuleInfo nativemodulelister_nativemoduleinfo = (NativeModuleLister.NativeModuleInfo)throwable.next();
                  writer.write(nativemodulelister_nativemoduleinfo.toString());
                  writer.write(10);
               }
            }
         } catch (Throwable var8) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var8.addSuppressed(var6);
               }
            }

            throw var8;
         }

         if (writer != null) {
            writer.close();
         }

         return;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void startMetricsRecordingTick() {
      if (this.willStartRecordingMetrics) {
         this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, (path) -> {
            this.executeBlocking(() -> this.saveDebugReport(path.resolve("server")));
            this.onMetricsRecordingFinished.accept(path);
         });
         this.willStartRecordingMetrics = false;
      }

      this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
      this.metricsRecorder.startTick();
      this.profiler.startTick();
   }

   private void endMetricsRecordingTick() {
      this.profiler.endTick();
      this.metricsRecorder.endTick();
   }

   public boolean isRecordingMetrics() {
      return this.metricsRecorder.isRecording();
   }

   public void startRecordingMetrics(Consumer<ProfileResults> consumer, Consumer<Path> consumer1) {
      this.onMetricsRecordingStopped = (profileresults) -> {
         this.stopRecordingMetrics();
         consumer.accept(profileresults);
      };
      this.onMetricsRecordingFinished = consumer1;
      this.willStartRecordingMetrics = true;
   }

   public void stopRecordingMetrics() {
      this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
   }

   public void finishRecordingMetrics() {
      this.metricsRecorder.end();
   }

   public void cancelRecordingMetrics() {
      this.metricsRecorder.cancel();
      this.profiler = this.metricsRecorder.getProfiler();
   }

   public Path getWorldPath(LevelResource levelresource) {
      return this.storageSource.getLevelPath(levelresource);
   }

   public boolean forceSynchronousWrites() {
      return true;
   }

   public StructureTemplateManager getStructureManager() {
      return this.structureTemplateManager;
   }

   public WorldData getWorldData() {
      return this.worldData;
   }

   public RegistryAccess.Frozen registryAccess() {
      return this.registries.compositeAccess();
   }

   public LayeredRegistryAccess<RegistryLayer> registries() {
      return this.registries;
   }

   public TextFilter createTextFilterForPlayer(ServerPlayer serverplayer) {
      return TextFilter.DUMMY;
   }

   public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer serverplayer) {
      return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(serverplayer) : new ServerPlayerGameMode(serverplayer));
   }

   @Nullable
   public GameType getForcedGameType() {
      return null;
   }

   public ResourceManager getResourceManager() {
      return this.resources.resourceManager;
   }

   public boolean isCurrentlySaving() {
      return this.isSaving;
   }

   public boolean isTimeProfilerRunning() {
      return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
   }

   public void startTimeProfiler() {
      this.debugCommandProfilerDelayStart = true;
   }

   public ProfileResults stopTimeProfiler() {
      if (this.debugCommandProfiler == null) {
         return EmptyProfileResults.EMPTY;
      } else {
         ProfileResults profileresults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
         this.debugCommandProfiler = null;
         return profileresults;
      }
   }

   public int getMaxChainedNeighborUpdates() {
      return 1000000;
   }

   public void logChatMessage(Component component, ChatType.Bound chattype_bound, @Nullable String s) {
      String s1 = chattype_bound.decorate(component).getString();
      if (s != null) {
         LOGGER.info("[{}] {}", s, s1);
      } else {
         LOGGER.info("{}", (Object)s1);
      }

   }

   public ChatDecorator getChatDecorator() {
      return ChatDecorator.PLAIN;
   }

   static record ReloadableResources(CloseableResourceManager resourceManager, ReloadableServerResources managers) implements AutoCloseable {
      final CloseableResourceManager resourceManager;
      final ReloadableServerResources managers;

      public void close() {
         this.resourceManager.close();
      }
   }

   public static record ServerResourcePackInfo(String url, String hash, boolean isRequired, @Nullable Component prompt) {
   }

   static class TimeProfiler {
      final long startNanos;
      final int startTick;

      TimeProfiler(long i, int j) {
         this.startNanos = i;
         this.startTick = j;
      }

      ProfileResults stop(final long i, final int j) {
         return new ProfileResults() {
            public List<ResultField> getTimes(String s) {
               return Collections.emptyList();
            }

            public boolean saveResults(Path path) {
               return false;
            }

            public long getStartTimeNano() {
               return TimeProfiler.this.startNanos;
            }

            public int getStartTimeTicks() {
               return TimeProfiler.this.startTick;
            }

            public long getEndTimeNano() {
               return i;
            }

            public int getEndTimeTicks() {
               return j;
            }

            public String getProfilerResults() {
               return "";
            }
         };
      }
   }
}
