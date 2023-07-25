package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserFlag;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.Optionull;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BanNoticeScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.profiling.ClientMetricsSamplersProvider;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindResolver;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FileZipper;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
   static Minecraft instance;
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
   private static final int MAX_TICKS_PER_UPDATE = 10;
   public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
   public static final ResourceLocation UNIFORM_FONT = new ResourceLocation("uniform");
   public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
   private static final ResourceLocation REGIONAL_COMPLIANCIES = new ResourceLocation("regional_compliancies.json");
   private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
   private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE = Component.translatable("multiplayer.socialInteractions.not_available");
   public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
   private final Path resourcePackDirectory;
   private final PropertyMap profileProperties;
   private final TextureManager textureManager;
   private final DataFixer fixerUpper;
   private final VirtualScreen virtualScreen;
   private final Window window;
   private final Timer timer = new Timer(20.0F, 0L);
   private final RenderBuffers renderBuffers;
   public final LevelRenderer levelRenderer;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;
   public final ParticleEngine particleEngine;
   private final SearchRegistry searchRegistry = new SearchRegistry();
   private final User user;
   public final Font font;
   public final Font fontFilterFishy;
   public final GameRenderer gameRenderer;
   public final DebugRenderer debugRenderer;
   private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference<>();
   public final Gui gui;
   public final Options options;
   private final HotbarManager hotbarManager;
   public final MouseHandler mouseHandler;
   public final KeyboardHandler keyboardHandler;
   private InputType lastInputType = InputType.NONE;
   public final File gameDirectory;
   private final String launchedVersion;
   private final String versionType;
   private final Proxy proxy;
   private final LevelStorageSource levelSource;
   public final FrameTimer frameTimer = new FrameTimer();
   private final boolean is64bit;
   private final boolean demo;
   private final boolean allowsMultiplayer;
   private final boolean allowsChat;
   private final ReloadableResourceManager resourceManager;
   private final VanillaPackResources vanillaPackResources;
   private final DownloadedPackSource downloadedPackSource;
   private final PackRepository resourcePackRepository;
   private final LanguageManager languageManager;
   private final BlockColors blockColors;
   private final ItemColors itemColors;
   private final RenderTarget mainRenderTarget;
   private final SoundManager soundManager;
   private final MusicManager musicManager;
   private final FontManager fontManager;
   private final SplashManager splashManager;
   private final GpuWarnlistManager gpuWarnlistManager;
   private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, Minecraft::countryEqualsISO3);
   private final YggdrasilAuthenticationService authenticationService;
   private final MinecraftSessionService minecraftSessionService;
   private final UserApiService userApiService;
   private final SkinManager skinManager;
   private final ModelManager modelManager;
   private final BlockRenderDispatcher blockRenderer;
   private final PaintingTextureManager paintingTextures;
   private final MobEffectTextureManager mobEffectTextures;
   private final ToastComponent toast;
   private final Tutorial tutorial;
   private final PlayerSocialManager playerSocialManager;
   private final EntityModelSet entityModels;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final ClientTelemetryManager telemetryManager;
   private final ProfileKeyPairManager profileKeyPairManager;
   private final RealmsDataFetcher realmsDataFetcher;
   private final QuickPlayLog quickPlayLog;
   @Nullable
   public MultiPlayerGameMode gameMode;
   @Nullable
   public ClientLevel level;
   @Nullable
   public LocalPlayer player;
   @Nullable
   private IntegratedServer singleplayerServer;
   @Nullable
   private Connection pendingConnection;
   private boolean isLocalServer;
   @Nullable
   public Entity cameraEntity;
   @Nullable
   public Entity crosshairPickEntity;
   @Nullable
   public HitResult hitResult;
   private int rightClickDelay;
   protected int missTime;
   private volatile boolean pause;
   private float pausePartialTick;
   private long lastNanoTime = Util.getNanos();
   private long lastTime;
   private int frames;
   public boolean noRender;
   @Nullable
   public Screen screen;
   @Nullable
   private Overlay overlay;
   private boolean connectedToRealms;
   private Thread gameThread;
   private volatile boolean running;
   @Nullable
   private Supplier<CrashReport> delayedCrash;
   private static int fps;
   public String fpsString = "";
   private long frameTimeNs;
   public boolean wireframe;
   public boolean chunkPath;
   public boolean chunkVisibility;
   public boolean smartCull = true;
   private boolean windowActive;
   private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
   @Nullable
   private CompletableFuture<Void> pendingReload;
   @Nullable
   private TutorialToast socialInteractionsToast;
   private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
   private int fpsPieRenderTicks;
   private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
   @Nullable
   private ProfileResults fpsPieResults;
   private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
   private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
   private long savedCpuDuration;
   private double gpuUtilization;
   @Nullable
   private TimerQuery.FrameProfile currentFrameProfile;
   private final Realms32BitWarningStatus realms32BitWarningStatus;
   private final GameNarrator narrator;
   private final ChatListener chatListener;
   private ReportingContext reportingContext;
   private String debugPath = "root";

   public Minecraft(GameConfig gameconfig) {
      super("Client");
      instance = this;
      this.gameDirectory = gameconfig.location.gameDirectory;
      File file = gameconfig.location.assetDirectory;
      this.resourcePackDirectory = gameconfig.location.resourcePackDirectory.toPath();
      this.launchedVersion = gameconfig.game.launchVersion;
      this.versionType = gameconfig.game.versionType;
      this.profileProperties = gameconfig.user.profileProperties;
      ClientPackSource clientpacksource = new ClientPackSource(gameconfig.location.getExternalAssetSource());
      this.downloadedPackSource = new DownloadedPackSource(new File(this.gameDirectory, "server-resource-packs"));
      RepositorySource repositorysource = new FolderRepositorySource(this.resourcePackDirectory, PackType.CLIENT_RESOURCES, PackSource.DEFAULT);
      this.resourcePackRepository = new PackRepository(clientpacksource, this.downloadedPackSource, repositorysource);
      this.vanillaPackResources = clientpacksource.getVanillaPack();
      this.proxy = gameconfig.user.proxy;
      this.authenticationService = new YggdrasilAuthenticationService(this.proxy);
      this.minecraftSessionService = this.authenticationService.createMinecraftSessionService();
      this.userApiService = this.createUserApiService(this.authenticationService, gameconfig);
      this.user = gameconfig.user.user;
      LOGGER.info("Setting user: {}", (Object)this.user.getName());
      LOGGER.debug("(Session ID is {})", (Object)this.user.getSessionId());
      this.demo = gameconfig.game.demo;
      this.allowsMultiplayer = !gameconfig.game.disableMultiplayer;
      this.allowsChat = !gameconfig.game.disableChat;
      this.is64bit = checkIs64Bit();
      this.singleplayerServer = null;
      KeybindResolver.setKeyResolver(KeyMapping::createNameSupplier);
      this.fixerUpper = DataFixers.getDataFixer();
      this.toast = new ToastComponent(this);
      this.gameThread = Thread.currentThread();
      this.options = new Options(this, this.gameDirectory);
      RenderSystem.setShaderGlintAlpha(this.options.glintStrength().get());
      this.running = true;
      this.tutorial = new Tutorial(this, this.options);
      this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
      LOGGER.info("Backend library: {}", (Object)RenderSystem.getBackendDescription());
      DisplayData displaydata;
      if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
         displaydata = new DisplayData(this.options.overrideWidth, this.options.overrideHeight, gameconfig.display.fullscreenWidth, gameconfig.display.fullscreenHeight, gameconfig.display.isFullscreen);
      } else {
         displaydata = gameconfig.display;
      }

      Util.timeSource = RenderSystem.initBackendSystem();
      this.virtualScreen = new VirtualScreen(this);
      this.window = this.virtualScreen.newWindow(displaydata, this.options.fullscreenVideoModeString, this.createTitle());
      this.setWindowActive(true);
      GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS);

      try {
         this.window.setIcon(this.vanillaPackResources, SharedConstants.getCurrentVersion().isStable() ? IconSet.RELEASE : IconSet.SNAPSHOT);
      } catch (IOException var12) {
         LOGGER.error("Couldn't set icon", (Throwable)var12);
      }

      this.window.setFramerateLimit(this.options.framerateLimit().get());
      this.mouseHandler = new MouseHandler(this);
      this.mouseHandler.setup(this.window.getWindow());
      this.keyboardHandler = new KeyboardHandler(this);
      this.keyboardHandler.setup(this.window.getWindow());
      RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
      this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
      this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.mainRenderTarget.clear(ON_OSX);
      this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
      this.resourcePackRepository.reload();
      this.options.loadSelectedResourcePacks(this.resourcePackRepository);
      this.languageManager = new LanguageManager(this.options.languageCode);
      this.resourceManager.registerReloadListener(this.languageManager);
      this.textureManager = new TextureManager(this.resourceManager);
      this.resourceManager.registerReloadListener(this.textureManager);
      this.skinManager = new SkinManager(this.textureManager, new File(file, "skins"), this.minecraftSessionService);
      Path path = this.gameDirectory.toPath();
      DirectoryValidator directoryvalidator = LevelStorageSource.parseValidator(path.resolve("allowed_symlinks.txt"));
      this.levelSource = new LevelStorageSource(path.resolve("saves"), path.resolve("backups"), directoryvalidator, this.fixerUpper);
      this.soundManager = new SoundManager(this.options);
      this.resourceManager.registerReloadListener(this.soundManager);
      this.splashManager = new SplashManager(this.user);
      this.resourceManager.registerReloadListener(this.splashManager);
      this.musicManager = new MusicManager(this);
      this.fontManager = new FontManager(this.textureManager);
      this.font = this.fontManager.createFont();
      this.fontFilterFishy = this.fontManager.createFontFilterFishy();
      this.resourceManager.registerReloadListener(this.fontManager);
      this.selectMainFont(this.isEnforceUnicode());
      this.resourceManager.registerReloadListener(new GrassColorReloadListener());
      this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
      this.window.setErrorSection("Startup");
      RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
      this.window.setErrorSection("Post startup");
      this.blockColors = BlockColors.createDefault();
      this.itemColors = ItemColors.createDefault(this.blockColors);
      this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels().get());
      this.resourceManager.registerReloadListener(this.modelManager);
      this.entityModels = new EntityModelSet();
      this.resourceManager.registerReloadListener(this.entityModels);
      this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.font, this.entityModels, this::getBlockRenderer, this::getItemRenderer, this::getEntityRenderDispatcher);
      this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
      BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
      this.resourceManager.registerReloadListener(blockentitywithoutlevelrenderer);
      this.itemRenderer = new ItemRenderer(this, this.textureManager, this.modelManager, this.itemColors, blockentitywithoutlevelrenderer);
      this.resourceManager.registerReloadListener(this.itemRenderer);
      this.renderBuffers = new RenderBuffers();
      this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
      this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), blockentitywithoutlevelrenderer, this.blockColors);
      this.resourceManager.registerReloadListener(this.blockRenderer);
      this.entityRenderDispatcher = new EntityRenderDispatcher(this, this.textureManager, this.itemRenderer, this.blockRenderer, this.font, this.options, this.entityModels);
      this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
      this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getItemInHandRenderer(), this.resourceManager, this.renderBuffers);
      this.resourceManager.registerReloadListener(this.gameRenderer.createReloadListener());
      this.levelRenderer = new LevelRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.renderBuffers);
      this.resourceManager.registerReloadListener(this.levelRenderer);
      this.createSearchTrees();
      this.resourceManager.registerReloadListener(this.searchRegistry);
      this.particleEngine = new ParticleEngine(this.level, this.textureManager);
      this.resourceManager.registerReloadListener(this.particleEngine);
      this.paintingTextures = new PaintingTextureManager(this.textureManager);
      this.resourceManager.registerReloadListener(this.paintingTextures);
      this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
      this.resourceManager.registerReloadListener(this.mobEffectTextures);
      this.gpuWarnlistManager = new GpuWarnlistManager();
      this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
      this.resourceManager.registerReloadListener(this.regionalCompliancies);
      this.gui = new Gui(this, this.itemRenderer);
      this.debugRenderer = new DebugRenderer(this);
      RealmsClient realmsclient = RealmsClient.create(this);
      this.realmsDataFetcher = new RealmsDataFetcher(realmsclient);
      RenderSystem.setErrorCallback(this::onFullscreenError);
      if (this.mainRenderTarget.width == this.window.getWidth() && this.mainRenderTarget.height == this.window.getHeight()) {
         if (this.options.fullscreen().get() && !this.window.isFullscreen()) {
            this.window.toggleFullScreen();
            this.options.fullscreen().set(this.window.isFullscreen());
         }
      } else {
         StringBuilder stringbuilder = new StringBuilder("Recovering from unsupported resolution (" + this.window.getWidth() + "x" + this.window.getHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
         if (GlDebug.isDebugEnabled()) {
            stringbuilder.append("\n\nReported GL debug messages:\n").append(String.join("\n", GlDebug.getLastOpenGlDebugMessages()));
         }

         this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
         TinyFileDialogs.tinyfd_messageBox("Minecraft", stringbuilder.toString(), "ok", "error", false);
      }

      this.window.updateVsync(this.options.enableVsync().get());
      this.window.updateRawMouseInput(this.options.rawMouseInput().get());
      this.window.setDefaultErrorCallback();
      this.resizeDisplay();
      this.gameRenderer.preloadUiShader(this.vanillaPackResources.asProvider());
      this.telemetryManager = new ClientTelemetryManager(this, this.userApiService, this.user);
      this.profileKeyPairManager = ProfileKeyPairManager.create(this.userApiService, this.user, path);
      this.realms32BitWarningStatus = new Realms32BitWarningStatus(this);
      this.narrator = new GameNarrator(this);
      this.narrator.checkStatus(this.options.narrator().get() != NarratorStatus.OFF);
      this.chatListener = new ChatListener(this);
      this.chatListener.setMessageDelay(this.options.chatDelay().get());
      this.reportingContext = ReportingContext.create(ReportEnvironment.local(), this.userApiService);
      LoadingOverlay.registerTextures(this);
      List<PackResources> list = this.resourcePackRepository.openAllSelected();
      this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, list);
      ReloadInstance reloadinstance = this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list);
      GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
      this.setOverlay(new LoadingOverlay(this, reloadinstance, (optional) -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
               this.selfTest();
            }

            this.reloadStateTracker.finishReload();
            this.onGameLoadFinished();
         }), false));
      this.quickPlayLog = QuickPlayLog.of(gameconfig.quickPlay.path());
      if (this.shouldShowBanNotice()) {
         this.setScreen(BanNoticeScreen.create((flag) -> {
            if (flag) {
               Util.getPlatform().openUri("https://aka.ms/mcjavamoderation");
            }

            this.setInitialScreen(realmsclient, reloadinstance, gameconfig.quickPlay);
         }, this.multiplayerBan()));
      } else {
         this.setInitialScreen(realmsclient, reloadinstance, gameconfig.quickPlay);
      }

   }

   private void onGameLoadFinished() {
      GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
      GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS);
      GameLoadTimesEvent.INSTANCE.send(this.telemetryManager.getOutsideSessionSender());
   }

   private void setInitialScreen(RealmsClient realmsclient, ReloadInstance reloadinstance, GameConfig.QuickPlayData gameconfig_quickplaydata) {
      if (gameconfig_quickplaydata.isEnabled()) {
         QuickPlay.connect(this, gameconfig_quickplaydata, reloadinstance, realmsclient);
      } else if (this.options.onboardAccessibility) {
         this.setScreen(new AccessibilityOnboardingScreen(this.options));
      } else {
         this.setScreen(new TitleScreen(true));
      }

   }

   private static boolean countryEqualsISO3(Object object) {
      try {
         return Locale.getDefault().getISO3Country().equals(object);
      } catch (MissingResourceException var2) {
         return false;
      }
   }

   public void updateTitle() {
      this.window.setTitle(this.createTitle());
   }

   private String createTitle() {
      StringBuilder stringbuilder = new StringBuilder("Minecraft");
      if (checkModStatus().shouldReportAsModified()) {
         stringbuilder.append("*");
      }

      stringbuilder.append(" ");
      stringbuilder.append(SharedConstants.getCurrentVersion().getName());
      ClientPacketListener clientpacketlistener = this.getConnection();
      if (clientpacketlistener != null && clientpacketlistener.getConnection().isConnected()) {
         stringbuilder.append(" - ");
         if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
            stringbuilder.append(I18n.get("title.singleplayer"));
         } else if (this.isConnectedToRealms()) {
            stringbuilder.append(I18n.get("title.multiplayer.realms"));
         } else if (this.singleplayerServer == null && (this.getCurrentServer() == null || !this.getCurrentServer().isLan())) {
            stringbuilder.append(I18n.get("title.multiplayer.other"));
         } else {
            stringbuilder.append(I18n.get("title.multiplayer.lan"));
         }
      }

      return stringbuilder.toString();
   }

   private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilauthenticationservice, GameConfig gameconfig) {
      try {
         return yggdrasilauthenticationservice.createUserApiService(gameconfig.user.user.getAccessToken());
      } catch (AuthenticationException var4) {
         LOGGER.error("Failed to verify authentication", (Throwable)var4);
         return UserApiService.OFFLINE;
      }
   }

   public static ModCheck checkModStatus() {
      return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
   }

   private void rollbackResourcePacks(Throwable throwable) {
      if (this.resourcePackRepository.getSelectedIds().size() > 1) {
         this.clearResourcePacksOnError(throwable, (Component)null);
      } else {
         Util.throwAsRuntime(throwable);
      }

   }

   public void clearResourcePacksOnError(Throwable throwable, @Nullable Component component) {
      LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
      this.reloadStateTracker.startRecovery(throwable);
      this.resourcePackRepository.setSelected(Collections.emptyList());
      this.options.resourcePacks.clear();
      this.options.incompatibleResourcePacks.clear();
      this.options.save();
      this.reloadResourcePacks(true).thenRun(() -> this.addResourcePackLoadFailToast(component));
   }

   private void abortResourcePackRecovery() {
      this.setOverlay((Overlay)null);
      if (this.level != null) {
         this.level.disconnect();
         this.clearLevel();
      }

      this.setScreen(new TitleScreen());
      this.addResourcePackLoadFailToast((Component)null);
   }

   private void addResourcePackLoadFailToast(@Nullable Component component) {
      ToastComponent toastcomponent = this.getToasts();
      SystemToast.addOrUpdate(toastcomponent, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), component);
   }

   public void run() {
      this.gameThread = Thread.currentThread();
      if (Runtime.getRuntime().availableProcessors() > 4) {
         this.gameThread.setPriority(10);
      }

      try {
         boolean flag = false;

         while(this.running) {
            if (this.delayedCrash != null) {
               crash(this.delayedCrash.get());
               return;
            }

            try {
               SingleTickProfiler singletickprofiler = SingleTickProfiler.createTickProfiler("Renderer");
               boolean flag1 = this.shouldRenderFpsPie();
               this.profiler = this.constructProfiler(flag1, singletickprofiler);
               this.profiler.startTick();
               this.metricsRecorder.startTick();
               this.runTick(!flag);
               this.metricsRecorder.endTick();
               this.profiler.endTick();
               this.finishProfilers(flag1, singletickprofiler);
            } catch (OutOfMemoryError var4) {
               if (flag) {
                  throw var4;
               }

               this.emergencySave();
               this.setScreen(new OutOfMemoryScreen());
               System.gc();
               LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)var4);
               flag = true;
            }
         }
      } catch (ReportedException var5) {
         this.fillReport(var5.getReport());
         this.emergencySave();
         LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)var5);
         crash(var5.getReport());
      } catch (Throwable var6) {
         CrashReport crashreport = this.fillReport(new CrashReport("Unexpected error", var6));
         LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", var6);
         this.emergencySave();
         crash(crashreport);
      }

   }

   void selectMainFont(boolean flag) {
      this.fontManager.setRenames(flag ? ImmutableMap.of(DEFAULT_FONT, UNIFORM_FONT) : ImmutableMap.of());
   }

   private void createSearchTrees() {
      this.searchRegistry.register(SearchRegistry.CREATIVE_NAMES, (list3) -> new FullTextSearchTree<>((itemstack2) -> itemstack2.getTooltipLines((Player)null, TooltipFlag.Default.NORMAL.asCreative()).stream().map((component1) -> ChatFormatting.stripFormatting(component1.getString()).trim()).filter((s1) -> !s1.isEmpty()), (itemstack1) -> Stream.of(BuiltInRegistries.ITEM.getKey(itemstack1.getItem())), list3));
      this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, (list2) -> new IdSearchTree<>((itemstack) -> itemstack.getTags().map(TagKey::location), list2));
      this.searchRegistry.register(SearchRegistry.RECIPE_COLLECTIONS, (list1) -> new FullTextSearchTree<>((recipecollection2) -> recipecollection2.getRecipes().stream().flatMap((recipe1) -> recipe1.getResultItem(recipecollection2.registryAccess()).getTooltipLines((Player)null, TooltipFlag.Default.NORMAL).stream()).map((component) -> ChatFormatting.stripFormatting(component.getString()).trim()).filter((s) -> !s.isEmpty()), (recipecollection) -> recipecollection.getRecipes().stream().map((recipe) -> BuiltInRegistries.ITEM.getKey(recipe.getResultItem(recipecollection.registryAccess()).getItem())), list1));
      CreativeModeTabs.searchTab().setSearchTreeBuilder((list) -> {
         this.populateSearchTree(SearchRegistry.CREATIVE_NAMES, list);
         this.populateSearchTree(SearchRegistry.CREATIVE_TAGS, list);
      });
   }

   private void onFullscreenError(int i, long j) {
      this.options.enableVsync().set(false);
      this.options.save();
   }

   private static boolean checkIs64Bit() {
      String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

      for(String s : astring) {
         String s1 = System.getProperty(s);
         if (s1 != null && s1.contains("64")) {
            return true;
         }
      }

      return false;
   }

   public RenderTarget getMainRenderTarget() {
      return this.mainRenderTarget;
   }

   public String getLaunchedVersion() {
      return this.launchedVersion;
   }

   public String getVersionType() {
      return this.versionType;
   }

   public void delayCrash(CrashReport crashreport) {
      this.delayedCrash = () -> this.fillReport(crashreport);
   }

   public void delayCrashRaw(CrashReport crashreport) {
      this.delayedCrash = () -> crashreport;
   }

   public static void crash(CrashReport crashreport) {
      File file = new File(getInstance().gameDirectory, "crash-reports");
      File file1 = new File(file, "crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
      Bootstrap.realStdoutPrintln(crashreport.getFriendlyReport());
      if (crashreport.getSaveFile() != null) {
         Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashreport.getSaveFile());
         System.exit(-1);
      } else if (crashreport.saveToFile(file1)) {
         Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + file1.getAbsolutePath());
         System.exit(-1);
      } else {
         Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
         System.exit(-2);
      }

   }

   public boolean isEnforceUnicode() {
      return this.options.forceUnicodeFont().get();
   }

   public CompletableFuture<Void> reloadResourcePacks() {
      return this.reloadResourcePacks(false);
   }

   private CompletableFuture<Void> reloadResourcePacks(boolean flag) {
      if (this.pendingReload != null) {
         return this.pendingReload;
      } else {
         CompletableFuture<Void> completablefuture = new CompletableFuture<>();
         if (!flag && this.overlay instanceof LoadingOverlay) {
            this.pendingReload = completablefuture;
            return completablefuture;
         } else {
            this.resourcePackRepository.reload();
            List<PackResources> list = this.resourcePackRepository.openAllSelected();
            if (!flag) {
               this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, list);
            }

            this.setOverlay(new LoadingOverlay(this, this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list), (optional) -> Util.ifElse(optional, (throwable) -> {
                  if (flag) {
                     this.abortResourcePackRecovery();
                  } else {
                     this.rollbackResourcePacks(throwable);
                  }

               }, () -> {
                  this.levelRenderer.allChanged();
                  this.reloadStateTracker.finishReload();
                  completablefuture.complete((Void)null);
               }), true));
            return completablefuture;
         }
      }
   }

   private void selfTest() {
      boolean flag = false;
      BlockModelShaper blockmodelshaper = this.getBlockRenderer().getBlockModelShaper();
      BakedModel bakedmodel = blockmodelshaper.getModelManager().getMissingModel();

      for(Block block : BuiltInRegistries.BLOCK) {
         for(BlockState blockstate : block.getStateDefinition().getPossibleStates()) {
            if (blockstate.getRenderShape() == RenderShape.MODEL) {
               BakedModel bakedmodel1 = blockmodelshaper.getBlockModel(blockstate);
               if (bakedmodel1 == bakedmodel) {
                  LOGGER.debug("Missing model for: {}", (Object)blockstate);
                  flag = true;
               }
            }
         }
      }

      TextureAtlasSprite textureatlassprite = bakedmodel.getParticleIcon();

      for(Block block1 : BuiltInRegistries.BLOCK) {
         for(BlockState blockstate1 : block1.getStateDefinition().getPossibleStates()) {
            TextureAtlasSprite textureatlassprite1 = blockmodelshaper.getParticleIcon(blockstate1);
            if (!blockstate1.isAir() && textureatlassprite1 == textureatlassprite) {
               LOGGER.debug("Missing particle icon for: {}", (Object)blockstate1);
            }
         }
      }

      for(Item item : BuiltInRegistries.ITEM) {
         ItemStack itemstack = item.getDefaultInstance();
         String s = itemstack.getDescriptionId();
         String s1 = Component.translatable(s).getString();
         if (s1.toLowerCase(Locale.ROOT).equals(item.getDescriptionId())) {
            LOGGER.debug("Missing translation for: {} {} {}", itemstack, s, item);
         }
      }

      flag |= MenuScreens.selfTest();
      flag |= EntityRenderers.validateRegistrations();
      if (flag) {
         throw new IllegalStateException("Your game data is foobar, fix the errors above!");
      }
   }

   public LevelStorageSource getLevelSource() {
      return this.levelSource;
   }

   private void openChatScreen(String s) {
      Minecraft.ChatStatus minecraft_chatstatus = this.getChatStatus();
      if (!minecraft_chatstatus.isChatAllowed(this.isLocalServer())) {
         if (this.gui.isShowingChatDisabledByPlayer()) {
            this.gui.setChatDisabledByPlayerShown(false);
            this.setScreen(new ConfirmLinkScreen((flag) -> {
               if (flag) {
                  Util.getPlatform().openUri("https://aka.ms/JavaAccountSettings");
               }

               this.setScreen((Screen)null);
            }, Minecraft.ChatStatus.INFO_DISABLED_BY_PROFILE, "https://aka.ms/JavaAccountSettings", true));
         } else {
            Component component = minecraft_chatstatus.getMessage();
            this.gui.setOverlayMessage(component, false);
            this.narrator.sayNow(component);
            this.gui.setChatDisabledByPlayerShown(minecraft_chatstatus == Minecraft.ChatStatus.DISABLED_BY_PROFILE);
         }
      } else {
         this.setScreen(new ChatScreen(s));
      }

   }

   public void setScreen(@Nullable Screen screen) {
      if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
         LOGGER.error("setScreen called from non-game thread");
      }

      if (this.screen != null) {
         this.screen.removed();
      }

      if (screen == null && this.level == null) {
         screen = new TitleScreen();
      } else if (screen == null && this.player.isDeadOrDying()) {
         if (this.player.shouldShowDeathScreen()) {
            screen = new DeathScreen((Component)null, this.level.getLevelData().isHardcore());
         } else {
            this.player.respawn();
         }
      }

      this.screen = screen;
      if (this.screen != null) {
         this.screen.added();
      }

      BufferUploader.reset();
      if (screen != null) {
         this.mouseHandler.releaseMouse();
         KeyMapping.releaseAll();
         screen.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
         this.noRender = false;
      } else {
         this.soundManager.resume();
         this.mouseHandler.grabMouse();
      }

      this.updateTitle();
   }

   public void setOverlay(@Nullable Overlay overlay) {
      this.overlay = overlay;
   }

   public void destroy() {
      try {
         LOGGER.info("Stopping!");

         try {
            this.narrator.destroy();
         } catch (Throwable var7) {
         }

         try {
            if (this.level != null) {
               this.level.disconnect();
            }

            this.clearLevel();
         } catch (Throwable var6) {
         }

         if (this.screen != null) {
            this.screen.removed();
         }

         this.close();
      } finally {
         Util.timeSource = System::nanoTime;
         if (this.delayedCrash == null) {
            System.exit(0);
         }

      }

   }

   public void close() {
      if (this.currentFrameProfile != null) {
         this.currentFrameProfile.cancel();
      }

      try {
         this.telemetryManager.close();
         this.regionalCompliancies.close();
         this.modelManager.close();
         this.fontManager.close();
         this.gameRenderer.close();
         this.levelRenderer.close();
         this.soundManager.destroy();
         this.particleEngine.close();
         this.mobEffectTextures.close();
         this.paintingTextures.close();
         this.textureManager.close();
         this.resourceManager.close();
         Util.shutdownExecutors();
      } catch (Throwable var5) {
         LOGGER.error("Shutdown failure!", var5);
         throw var5;
      } finally {
         this.virtualScreen.close();
         this.window.close();
      }

   }

   private void runTick(boolean flag) {
      this.window.setErrorSection("Pre render");
      long i = Util.getNanos();
      if (this.window.shouldClose()) {
         this.stop();
      }

      if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
         CompletableFuture<Void> completablefuture = this.pendingReload;
         this.pendingReload = null;
         this.reloadResourcePacks().thenRun(() -> completablefuture.complete((Void)null));
      }

      Runnable runnable;
      while((runnable = this.progressTasks.poll()) != null) {
         runnable.run();
      }

      if (flag) {
         int j = this.timer.advanceTime(Util.getMillis());
         this.profiler.push("scheduledExecutables");
         this.runAllTasks();
         this.profiler.pop();
         this.profiler.push("tick");

         for(int k = 0; k < Math.min(10, j); ++k) {
            this.profiler.incrementCounter("clientTick");
            this.tick();
         }

         this.profiler.pop();
      }

      this.mouseHandler.turnPlayer();
      this.window.setErrorSection("Render");
      this.profiler.push("sound");
      this.soundManager.updateSource(this.gameRenderer.getMainCamera());
      this.profiler.pop();
      this.profiler.push("render");
      long l = Util.getNanos();
      boolean flag2;
      if (!this.options.renderDebug && !this.metricsRecorder.isRecording()) {
         flag2 = false;
         this.gpuUtilization = 0.0D;
      } else {
         flag2 = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
         if (flag2) {
            TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
         }
      }

      RenderSystem.clear(16640, ON_OSX);
      this.mainRenderTarget.bindWrite(true);
      FogRenderer.setupNoFog();
      this.profiler.push("display");
      RenderSystem.enableCull();
      this.profiler.pop();
      if (!this.noRender) {
         this.profiler.popPush("gameRenderer");
         this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, i, flag);
         this.profiler.pop();
      }

      if (this.fpsPieResults != null) {
         this.profiler.push("fpsPie");
         GuiGraphics guigraphics = new GuiGraphics(this, this.renderBuffers.bufferSource());
         this.renderFpsMeter(guigraphics, this.fpsPieResults);
         guigraphics.flush();
         this.profiler.pop();
      }

      this.profiler.push("blit");
      this.mainRenderTarget.unbindWrite();
      this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
      this.frameTimeNs = Util.getNanos() - l;
      if (flag2) {
         TimerQuery.getInstance().ifPresent((timerquery) -> this.currentFrameProfile = timerquery.endProfile());
      }

      this.profiler.popPush("updateDisplay");
      this.window.updateDisplay();
      int i1 = this.getFramerateLimit();
      if (i1 < 260) {
         RenderSystem.limitDisplayFPS(i1);
      }

      this.profiler.popPush("yield");
      Thread.yield();
      this.profiler.pop();
      this.window.setErrorSection("Post render");
      ++this.frames;
      boolean flag3 = this.hasSingleplayerServer() && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen()) && !this.singleplayerServer.isPublished();
      if (this.pause != flag3) {
         if (this.pause) {
            this.pausePartialTick = this.timer.partialTick;
         } else {
            this.timer.partialTick = this.pausePartialTick;
         }

         this.pause = flag3;
      }

      long j1 = Util.getNanos();
      long k1 = j1 - this.lastNanoTime;
      if (flag2) {
         this.savedCpuDuration = k1;
      }

      this.frameTimer.logFrameDuration(k1);
      this.lastNanoTime = j1;
      this.profiler.push("fpsUpdate");
      if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
         this.gpuUtilization = (double)this.currentFrameProfile.get() * 100.0D / (double)this.savedCpuDuration;
      }

      while(Util.getMillis() >= this.lastTime + 1000L) {
         String s;
         if (this.gpuUtilization > 0.0D) {
            s = " GPU: " + (this.gpuUtilization > 100.0D ? ChatFormatting.RED + "100%" : Math.round(this.gpuUtilization) + "%");
         } else {
            s = "";
         }

         fps = this.frames;
         this.fpsString = String.format(Locale.ROOT, "%d fps T: %s%s%s%s B: %d%s", fps, i1 == 260 ? "inf" : i1, this.options.enableVsync().get() ? " vsync" : "", this.options.graphicsMode().get(), this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"), this.options.biomeBlendRadius().get(), s);
         this.lastTime += 1000L;
         this.frames = 0;
      }

      this.profiler.pop();
   }

   private boolean shouldRenderFpsPie() {
      return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
   }

   private ProfilerFiller constructProfiler(boolean flag, @Nullable SingleTickProfiler singletickprofiler) {
      if (!flag) {
         this.fpsPieProfiler.disable();
         if (!this.metricsRecorder.isRecording() && singletickprofiler == null) {
            return InactiveProfiler.INSTANCE;
         }
      }

      ProfilerFiller profilerfiller;
      if (flag) {
         if (!this.fpsPieProfiler.isEnabled()) {
            this.fpsPieRenderTicks = 0;
            this.fpsPieProfiler.enable();
         }

         ++this.fpsPieRenderTicks;
         profilerfiller = this.fpsPieProfiler.getFiller();
      } else {
         profilerfiller = InactiveProfiler.INSTANCE;
      }

      if (this.metricsRecorder.isRecording()) {
         profilerfiller = ProfilerFiller.tee(profilerfiller, this.metricsRecorder.getProfiler());
      }

      return SingleTickProfiler.decorateFiller(profilerfiller, singletickprofiler);
   }

   private void finishProfilers(boolean flag, @Nullable SingleTickProfiler singletickprofiler) {
      if (singletickprofiler != null) {
         singletickprofiler.endTick();
      }

      if (flag) {
         this.fpsPieResults = this.fpsPieProfiler.getResults();
      } else {
         this.fpsPieResults = null;
      }

      this.profiler = this.fpsPieProfiler.getFiller();
   }

   public void resizeDisplay() {
      int i = this.window.calculateScale(this.options.guiScale().get(), this.isEnforceUnicode());
      this.window.setGuiScale((double)i);
      if (this.screen != null) {
         this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
      }

      RenderTarget rendertarget = this.getMainRenderTarget();
      rendertarget.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
      this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
      this.mouseHandler.setIgnoreFirstMove();
   }

   public void cursorEntered() {
      this.mouseHandler.cursorEntered();
   }

   public int getFps() {
      return fps;
   }

   public long getFrameTimeNs() {
      return this.frameTimeNs;
   }

   private int getFramerateLimit() {
      return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
   }

   public void emergencySave() {
      try {
         MemoryReserve.release();
         this.levelRenderer.clear();
      } catch (Throwable var3) {
      }

      try {
         System.gc();
         if (this.isLocalServer && this.singleplayerServer != null) {
            this.singleplayerServer.halt(true);
         }

         this.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
      } catch (Throwable var2) {
      }

      System.gc();
   }

   public boolean debugClientMetricsStart(Consumer<Component> consumer) {
      if (this.metricsRecorder.isRecording()) {
         this.debugClientMetricsStop();
         return false;
      } else {
         Consumer<ProfileResults> consumer1 = (profileresults2) -> {
            if (profileresults2 != EmptyProfileResults.EMPTY) {
               int i = profileresults2.getTickDuration();
               double d0 = (double)profileresults2.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
               this.execute(() -> consumer.accept(Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d0), i, String.format(Locale.ROOT, "%.2f", (double)i / d0))));
            }
         };
         Consumer<Path> consumer2 = (path2) -> {
            Component component = Component.literal(path2.toString()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path2.toFile().getParent())));
            this.execute(() -> consumer.accept(Component.translatable("debug.profiling.stop", component)));
         };
         SystemReport systemreport = fillSystemReport(new SystemReport(), this, this.languageManager, this.launchedVersion, this.options);
         Consumer<List<Path>> consumer3 = (list) -> {
            Path path1 = this.archiveProfilingReport(systemreport, list);
            consumer2.accept(path1);
         };
         Consumer<Path> consumer4;
         if (this.singleplayerServer == null) {
            consumer4 = (path) -> consumer3.accept(ImmutableList.of(path));
         } else {
            this.singleplayerServer.fillSystemReport(systemreport);
            CompletableFuture<Path> completablefuture = new CompletableFuture<>();
            CompletableFuture<Path> completablefuture1 = new CompletableFuture<>();
            CompletableFuture.allOf(completablefuture, completablefuture1).thenRunAsync(() -> consumer3.accept(ImmutableList.of(completablefuture.join(), completablefuture1.join())), Util.ioPool());
            this.singleplayerServer.startRecordingMetrics((profileresults1) -> {
            }, completablefuture1::complete);
            consumer4 = completablefuture::complete;
         }

         this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ClientMetricsSamplersProvider(Util.timeSource, this.levelRenderer), Util.timeSource, Util.ioPool(), new MetricsPersister("client"), (profileresults) -> {
            this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
            consumer1.accept(profileresults);
         }, consumer4);
         return true;
      }
   }

   private void debugClientMetricsStop() {
      this.metricsRecorder.end();
      if (this.singleplayerServer != null) {
         this.singleplayerServer.finishRecordingMetrics();
      }

   }

   private void debugClientMetricsCancel() {
      this.metricsRecorder.cancel();
      if (this.singleplayerServer != null) {
         this.singleplayerServer.cancelRecordingMetrics();
      }

   }

   private Path archiveProfilingReport(SystemReport systemreport, List<Path> list) {
      String s;
      if (this.isLocalServer()) {
         s = this.getSingleplayerServer().getWorldData().getLevelName();
      } else {
         ServerData serverdata = this.getCurrentServer();
         s = serverdata != null ? serverdata.name : "unknown";
      }

      Path path;
      try {
         String s2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), s, SharedConstants.getCurrentVersion().getId());
         String s3 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, s2, ".zip");
         path = MetricsPersister.PROFILING_RESULTS_DIR.resolve(s3);
      } catch (IOException var21) {
         throw new UncheckedIOException(var21);
      }

      try {
         FileZipper filezipper = new FileZipper(path);

         try {
            filezipper.add(Paths.get("system.txt"), systemreport.toLineSeparatedString());
            filezipper.add(Paths.get("client").resolve(this.options.getFile().getName()), this.options.dumpOptionsForReport());
            list.forEach(filezipper::add);
         } catch (Throwable var20) {
            try {
               filezipper.close();
            } catch (Throwable var19) {
               var20.addSuppressed(var19);
            }

            throw var20;
         }

         filezipper.close();
      } finally {
         for(Path path3 : list) {
            try {
               FileUtils.forceDelete(path3.toFile());
            } catch (IOException var18) {
               LOGGER.warn("Failed to delete temporary profiling result {}", path3, var18);
            }
         }

      }

      return path;
   }

   public void debugFpsMeterKeyPress(int i) {
      if (this.fpsPieResults != null) {
         List<ResultField> list = this.fpsPieResults.getTimes(this.debugPath);
         if (!list.isEmpty()) {
            ResultField resultfield = list.remove(0);
            if (i == 0) {
               if (!resultfield.name.isEmpty()) {
                  int j = this.debugPath.lastIndexOf(30);
                  if (j >= 0) {
                     this.debugPath = this.debugPath.substring(0, j);
                  }
               }
            } else {
               --i;
               if (i < list.size() && !"unspecified".equals((list.get(i)).name)) {
                  if (!this.debugPath.isEmpty()) {
                     this.debugPath = this.debugPath + "\u001e";
                  }

                  this.debugPath = this.debugPath + (list.get(i)).name;
               }
            }

         }
      }
   }

   private void renderFpsMeter(GuiGraphics guigraphics, ProfileResults profileresults) {
      List<ResultField> list = profileresults.getTimes(this.debugPath);
      ResultField resultfield = list.remove(0);
      RenderSystem.clear(256, ON_OSX);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)this.window.getWidth(), (float)this.window.getHeight(), 0.0F, 1000.0F, 3000.0F);
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
      PoseStack posestack = RenderSystem.getModelViewStack();
      posestack.pushPose();
      posestack.setIdentity();
      posestack.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
      RenderSystem.lineWidth(1.0F);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      int i = 160;
      int j = this.window.getWidth() - 160 - 10;
      int k = this.window.getHeight() - 320;
      RenderSystem.enableBlend();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex((double)((float)j - 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
      bufferbuilder.vertex((double)((float)j - 176.0F), (double)(k + 320), 0.0D).color(200, 0, 0, 0).endVertex();
      bufferbuilder.vertex((double)((float)j + 176.0F), (double)(k + 320), 0.0D).color(200, 0, 0, 0).endVertex();
      bufferbuilder.vertex((double)((float)j + 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
      tesselator.end();
      RenderSystem.disableBlend();
      double d0 = 0.0D;

      for(ResultField resultfield1 : list) {
         int l = Mth.floor(resultfield1.percentage / 4.0D) + 1;
         bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
         int i1 = resultfield1.getColor();
         int j1 = i1 >> 16 & 255;
         int k1 = i1 >> 8 & 255;
         int l1 = i1 & 255;
         bufferbuilder.vertex((double)j, (double)k, 0.0D).color(j1, k1, l1, 255).endVertex();

         for(int i2 = l; i2 >= 0; --i2) {
            float f = (float)((d0 + resultfield1.percentage * (double)i2 / (double)l) * (double)((float)Math.PI * 2F) / 100.0D);
            float f1 = Mth.sin(f) * 160.0F;
            float f2 = Mth.cos(f) * 160.0F * 0.5F;
            bufferbuilder.vertex((double)((float)j + f1), (double)((float)k - f2), 0.0D).color(j1, k1, l1, 255).endVertex();
         }

         tesselator.end();
         bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

         for(int j2 = l; j2 >= 0; --j2) {
            float f3 = (float)((d0 + resultfield1.percentage * (double)j2 / (double)l) * (double)((float)Math.PI * 2F) / 100.0D);
            float f4 = Mth.sin(f3) * 160.0F;
            float f5 = Mth.cos(f3) * 160.0F * 0.5F;
            if (!(f5 > 0.0F)) {
               bufferbuilder.vertex((double)((float)j + f4), (double)((float)k - f5), 0.0D).color(j1 >> 1, k1 >> 1, l1 >> 1, 255).endVertex();
               bufferbuilder.vertex((double)((float)j + f4), (double)((float)k - f5 + 10.0F), 0.0D).color(j1 >> 1, k1 >> 1, l1 >> 1, 255).endVertex();
            }
         }

         tesselator.end();
         d0 += resultfield1.percentage;
      }

      DecimalFormat decimalformat = new DecimalFormat("##0.00");
      decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
      String s = ProfileResults.demanglePath(resultfield.name);
      String s1 = "";
      if (!"unspecified".equals(s)) {
         s1 = s1 + "[0] ";
      }

      if (s.isEmpty()) {
         s1 = s1 + "ROOT ";
      } else {
         s1 = s1 + s + " ";
      }

      int k2 = 16777215;
      guigraphics.drawString(this.font, s1, j - 160, k - 80 - 16, 16777215);
      s1 = decimalformat.format(resultfield.globalPercentage) + "%";
      guigraphics.drawString(this.font, s1, j + 160 - this.font.width(s1), k - 80 - 16, 16777215);

      for(int l2 = 0; l2 < list.size(); ++l2) {
         ResultField resultfield2 = list.get(l2);
         StringBuilder stringbuilder = new StringBuilder();
         if ("unspecified".equals(resultfield2.name)) {
            stringbuilder.append("[?] ");
         } else {
            stringbuilder.append("[").append(l2 + 1).append("] ");
         }

         String s2 = stringbuilder.append(resultfield2.name).toString();
         guigraphics.drawString(this.font, s2, j - 160, k + 80 + l2 * 8 + 20, resultfield2.getColor());
         s2 = decimalformat.format(resultfield2.percentage) + "%";
         guigraphics.drawString(this.font, s2, j + 160 - 50 - this.font.width(s2), k + 80 + l2 * 8 + 20, resultfield2.getColor());
         s2 = decimalformat.format(resultfield2.globalPercentage) + "%";
         guigraphics.drawString(this.font, s2, j + 160 - this.font.width(s2), k + 80 + l2 * 8 + 20, resultfield2.getColor());
      }

      posestack.popPose();
      RenderSystem.applyModelViewMatrix();
   }

   public void stop() {
      this.running = false;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void pauseGame(boolean flag) {
      if (this.screen == null) {
         boolean flag1 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
         if (flag1) {
            this.setScreen(new PauseScreen(!flag));
            this.soundManager.pause();
         } else {
            this.setScreen(new PauseScreen(true));
         }

      }
   }

   private void continueAttack(boolean flag) {
      if (!flag) {
         this.missTime = 0;
      }

      if (this.missTime <= 0 && !this.player.isUsingItem()) {
         if (flag && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult)this.hitResult;
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!this.level.getBlockState(blockpos).isAir()) {
               Direction direction = blockhitresult.getDirection();
               if (this.gameMode.continueDestroyBlock(blockpos, direction)) {
                  this.particleEngine.crack(blockpos, direction);
                  this.player.swing(InteractionHand.MAIN_HAND);
               }
            }

         } else {
            this.gameMode.stopDestroyBlock();
         }
      }
   }

   private boolean startAttack() {
      if (this.missTime > 0) {
         return false;
      } else if (this.hitResult == null) {
         LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
         if (this.gameMode.hasMissTime()) {
            this.missTime = 10;
         }

         return false;
      } else if (this.player.isHandsBusy()) {
         return false;
      } else {
         ItemStack itemstack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
         if (!itemstack.isItemEnabled(this.level.enabledFeatures())) {
            return false;
         } else {
            boolean flag = false;
            switch (this.hitResult.getType()) {
               case ENTITY:
                  this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                  break;
               case BLOCK:
                  BlockHitResult blockhitresult = (BlockHitResult)this.hitResult;
                  BlockPos blockpos = blockhitresult.getBlockPos();
                  if (!this.level.getBlockState(blockpos).isAir()) {
                     this.gameMode.startDestroyBlock(blockpos, blockhitresult.getDirection());
                     if (this.level.getBlockState(blockpos).isAir()) {
                        flag = true;
                     }
                     break;
                  }
               case MISS:
                  if (this.gameMode.hasMissTime()) {
                     this.missTime = 10;
                  }

                  this.player.resetAttackStrengthTicker();
            }

            this.player.swing(InteractionHand.MAIN_HAND);
            return flag;
         }
      }
   }

   private void startUseItem() {
      if (!this.gameMode.isDestroying()) {
         this.rightClickDelay = 4;
         if (!this.player.isHandsBusy()) {
            if (this.hitResult == null) {
               LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
            }

            for(InteractionHand interactionhand : InteractionHand.values()) {
               ItemStack itemstack = this.player.getItemInHand(interactionhand);
               if (!itemstack.isItemEnabled(this.level.enabledFeatures())) {
                  return;
               }

               if (this.hitResult != null) {
                  switch (this.hitResult.getType()) {
                     case ENTITY:
                        EntityHitResult entityhitresult = (EntityHitResult)this.hitResult;
                        Entity entity = entityhitresult.getEntity();
                        if (!this.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                           return;
                        }

                        InteractionResult interactionresult = this.gameMode.interactAt(this.player, entity, entityhitresult, interactionhand);
                        if (!interactionresult.consumesAction()) {
                           interactionresult = this.gameMode.interact(this.player, entity, interactionhand);
                        }

                        if (interactionresult.consumesAction()) {
                           if (interactionresult.shouldSwing()) {
                              this.player.swing(interactionhand);
                           }

                           return;
                        }
                        break;
                     case BLOCK:
                        BlockHitResult blockhitresult = (BlockHitResult)this.hitResult;
                        int i = itemstack.getCount();
                        InteractionResult interactionresult1 = this.gameMode.useItemOn(this.player, interactionhand, blockhitresult);
                        if (interactionresult1.consumesAction()) {
                           if (interactionresult1.shouldSwing()) {
                              this.player.swing(interactionhand);
                              if (!itemstack.isEmpty() && (itemstack.getCount() != i || this.gameMode.hasInfiniteItems())) {
                                 this.gameRenderer.itemInHandRenderer.itemUsed(interactionhand);
                              }
                           }

                           return;
                        }

                        if (interactionresult1 == InteractionResult.FAIL) {
                           return;
                        }
                  }
               }

               if (!itemstack.isEmpty()) {
                  InteractionResult interactionresult2 = this.gameMode.useItem(this.player, interactionhand);
                  if (interactionresult2.consumesAction()) {
                     if (interactionresult2.shouldSwing()) {
                        this.player.swing(interactionhand);
                     }

                     this.gameRenderer.itemInHandRenderer.itemUsed(interactionhand);
                     return;
                  }
               }
            }

         }
      }
   }

   public MusicManager getMusicManager() {
      return this.musicManager;
   }

   public void tick() {
      if (this.rightClickDelay > 0) {
         --this.rightClickDelay;
      }

      this.profiler.push("gui");
      this.chatListener.tick();
      this.gui.tick(this.pause);
      this.profiler.pop();
      this.gameRenderer.pick(1.0F);
      this.tutorial.onLookAt(this.level, this.hitResult);
      this.profiler.push("gameMode");
      if (!this.pause && this.level != null) {
         this.gameMode.tick();
      }

      this.profiler.popPush("textures");
      if (this.level != null) {
         this.textureManager.tick();
      }

      if (this.screen == null && this.player != null) {
         if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
            this.setScreen((Screen)null);
         } else if (this.player.isSleeping() && this.level != null) {
            this.setScreen(new InBedChatScreen());
         }
      } else {
         Screen crashreport = this.screen;
         if (crashreport instanceof InBedChatScreen) {
            InBedChatScreen inbedchatscreen = (InBedChatScreen)crashreport;
            if (!this.player.isSleeping()) {
               inbedchatscreen.onPlayerWokeUp();
            }
         }
      }

      if (this.screen != null) {
         this.missTime = 10000;
      }

      if (this.screen != null) {
         Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
      }

      if (!this.options.renderDebug) {
         this.gui.clearCache();
      }

      if (this.overlay == null && this.screen == null) {
         this.profiler.popPush("Keybindings");
         this.handleKeybinds();
         if (this.missTime > 0) {
            --this.missTime;
         }
      }

      if (this.level != null) {
         this.profiler.popPush("gameRenderer");
         if (!this.pause) {
            this.gameRenderer.tick();
         }

         this.profiler.popPush("levelRenderer");
         if (!this.pause) {
            this.levelRenderer.tick();
         }

         this.profiler.popPush("level");
         if (!this.pause) {
            this.level.tickEntities();
         }
      } else if (this.gameRenderer.currentEffect() != null) {
         this.gameRenderer.shutdownEffect();
      }

      if (!this.pause) {
         this.musicManager.tick();
      }

      this.soundManager.tick(this.pause);
      if (this.level != null) {
         if (!this.pause) {
            if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
               Component component = Component.translatable("tutorial.socialInteractions.title");
               Component component1 = Component.translatable("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
               this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, component, component1, true);
               this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
               this.options.joinedFirstServer = true;
               this.options.save();
            }

            this.tutorial.tick();

            try {
               this.level.tick(() -> true);
            } catch (Throwable var4) {
               CrashReport crashreport = CrashReport.forThrowable(var4, "Exception in world tick");
               if (this.level == null) {
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Affected level");
                  crashreportcategory.setDetail("Problem", "Level is null!");
               } else {
                  this.level.fillReportDetails(crashreport);
               }

               throw new ReportedException(crashreport);
            }
         }

         this.profiler.popPush("animateTick");
         if (!this.pause && this.level != null) {
            this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
         }

         this.profiler.popPush("particles");
         if (!this.pause) {
            this.particleEngine.tick();
         }
      } else if (this.pendingConnection != null) {
         this.profiler.popPush("pendingConnection");
         this.pendingConnection.tick();
      }

      this.profiler.popPush("keyboard");
      this.keyboardHandler.tick();
      this.profiler.pop();
   }

   private boolean isMultiplayerServer() {
      return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
   }

   private void handleKeybinds() {
      for(; this.options.keyTogglePerspective.consumeClick(); this.levelRenderer.needsUpdate()) {
         CameraType cameratype = this.options.getCameraType();
         this.options.setCameraType(this.options.getCameraType().cycle());
         if (cameratype.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
            this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
         }
      }

      while(this.options.keySmoothCamera.consumeClick()) {
         this.options.smoothCamera = !this.options.smoothCamera;
      }

      for(int i = 0; i < 9; ++i) {
         boolean flag = this.options.keySaveHotbarActivator.isDown();
         boolean flag1 = this.options.keyLoadHotbarActivator.isDown();
         if (this.options.keyHotbarSlots[i].consumeClick()) {
            if (this.player.isSpectator()) {
               this.gui.getSpectatorGui().onHotbarSelected(i);
            } else if (!this.player.isCreative() || this.screen != null || !flag1 && !flag) {
               this.player.getInventory().selected = i;
            } else {
               CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, i, flag1, flag);
            }
         }
      }

      while(this.options.keySocialInteractions.consumeClick()) {
         if (!this.isMultiplayerServer()) {
            this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
            this.narrator.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
         } else {
            if (this.socialInteractionsToast != null) {
               this.tutorial.removeTimedToast(this.socialInteractionsToast);
               this.socialInteractionsToast = null;
            }

            this.setScreen(new SocialInteractionsScreen());
         }
      }

      while(this.options.keyInventory.consumeClick()) {
         if (this.gameMode.isServerControlledInventory()) {
            this.player.sendOpenInventory();
         } else {
            this.tutorial.onOpenInventory();
            this.setScreen(new InventoryScreen(this.player));
         }
      }

      while(this.options.keyAdvancements.consumeClick()) {
         this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
      }

      while(this.options.keySwapOffhand.consumeClick()) {
         if (!this.player.isSpectator()) {
            this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
         }
      }

      while(this.options.keyDrop.consumeClick()) {
         if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
            this.player.swing(InteractionHand.MAIN_HAND);
         }
      }

      while(this.options.keyChat.consumeClick()) {
         this.openChatScreen("");
      }

      if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
         this.openChatScreen("/");
      }

      boolean flag2 = false;
      if (this.player.isUsingItem()) {
         if (!this.options.keyUse.isDown()) {
            this.gameMode.releaseUsingItem(this.player);
         }

         while(this.options.keyAttack.consumeClick()) {
         }

         while(this.options.keyUse.consumeClick()) {
         }

         while(this.options.keyPickItem.consumeClick()) {
         }
      } else {
         while(this.options.keyAttack.consumeClick()) {
            flag2 |= this.startAttack();
         }

         while(this.options.keyUse.consumeClick()) {
            this.startUseItem();
         }

         while(this.options.keyPickItem.consumeClick()) {
            this.pickBlock();
         }
      }

      if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
         this.startUseItem();
      }

      this.continueAttack(this.screen == null && !flag2 && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
   }

   public ClientTelemetryManager getTelemetryManager() {
      return this.telemetryManager;
   }

   public double getGpuUtilization() {
      return this.gpuUtilization;
   }

   public ProfileKeyPairManager getProfileKeyPairManager() {
      return this.profileKeyPairManager;
   }

   public WorldOpenFlows createWorldOpenFlows() {
      return new WorldOpenFlows(this, this.levelSource);
   }

   public void doWorldLoad(String s, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, PackRepository packrepository, WorldStem worldstem, boolean flag) {
      this.clearLevel();
      this.progressListener.set((StoringChunkProgressListener)null);
      Instant instant = Instant.now();

      try {
         levelstoragesource_levelstorageaccess.saveDataTag(worldstem.registries().compositeAccess(), worldstem.worldData());
         Services services = Services.create(this.authenticationService, this.gameDirectory);
         services.profileCache().setExecutor(this);
         SkullBlockEntity.setup(services, this);
         GameProfileCache.setUsesAuthentication(false);
         this.singleplayerServer = MinecraftServer.spin((thread) -> new IntegratedServer(thread, this, levelstoragesource_levelstorageaccess, packrepository, worldstem, services, (i) -> {
               StoringChunkProgressListener storingchunkprogresslistener = new StoringChunkProgressListener(i + 0);
               this.progressListener.set(storingchunkprogresslistener);
               return ProcessorChunkProgressListener.createStarted(storingchunkprogresslistener, this.progressTasks::add);
            }));
         this.isLocalServer = true;
         this.updateReportEnvironment(ReportEnvironment.local());
         this.quickPlayLog.setWorldData(QuickPlayLog.Type.SINGLEPLAYER, s, worldstem.worldData().getLevelName());
      } catch (Throwable var12) {
         CrashReport crashreport = CrashReport.forThrowable(var12, "Starting integrated server");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Starting integrated server");
         crashreportcategory.setDetail("Level ID", s);
         crashreportcategory.setDetail("Level Name", () -> worldstem.worldData().getLevelName());
         throw new ReportedException(crashreport);
      }

      while(this.progressListener.get() == null) {
         Thread.yield();
      }

      LevelLoadingScreen levelloadingscreen = new LevelLoadingScreen(this.progressListener.get());
      this.setScreen(levelloadingscreen);
      this.profiler.push("waitForServer");

      while(!this.singleplayerServer.isReady()) {
         levelloadingscreen.tick();
         this.runTick(false);

         try {
            Thread.sleep(16L);
         } catch (InterruptedException var11) {
         }

         if (this.delayedCrash != null) {
            crash(this.delayedCrash.get());
            return;
         }
      }

      this.profiler.pop();
      Duration duration = Duration.between(instant, Instant.now());
      SocketAddress socketaddress = this.singleplayerServer.getConnection().startMemoryChannel();
      Connection connection = Connection.connectToLocalServer(socketaddress);
      connection.setListener(new ClientHandshakePacketListenerImpl(connection, this, (ServerData)null, (Screen)null, flag, duration, (component) -> {
      }));
      connection.send(new ClientIntentionPacket(socketaddress.toString(), 0, ConnectionProtocol.LOGIN));
      connection.send(new ServerboundHelloPacket(this.getUser().getName(), Optional.ofNullable(this.getUser().getProfileId())));
      this.pendingConnection = connection;
   }

   public void setLevel(ClientLevel clientlevel) {
      ProgressScreen progressscreen = new ProgressScreen(true);
      progressscreen.progressStartNoAbort(Component.translatable("connect.joining"));
      this.updateScreenAndTick(progressscreen);
      this.level = clientlevel;
      this.updateLevelInEngines(clientlevel);
      if (!this.isLocalServer) {
         Services services = Services.create(this.authenticationService, this.gameDirectory);
         services.profileCache().setExecutor(this);
         SkullBlockEntity.setup(services, this);
         GameProfileCache.setUsesAuthentication(false);
      }

   }

   public void clearLevel() {
      this.clearLevel(new ProgressScreen(true));
   }

   public void clearLevel(Screen screen) {
      ClientPacketListener clientpacketlistener = this.getConnection();
      if (clientpacketlistener != null) {
         this.dropAllTasks();
         clientpacketlistener.close();
      }

      this.playerSocialManager.stopOnlineMode();
      if (this.metricsRecorder.isRecording()) {
         this.debugClientMetricsCancel();
      }

      IntegratedServer integratedserver = this.singleplayerServer;
      this.singleplayerServer = null;
      this.gameRenderer.resetData();
      this.gameMode = null;
      this.narrator.clear();
      this.updateScreenAndTick(screen);
      if (this.level != null) {
         if (integratedserver != null) {
            this.profiler.push("waitForServer");

            while(!integratedserver.isShutdown()) {
               this.runTick(false);
            }

            this.profiler.pop();
         }

         this.downloadedPackSource.clearServerPack();
         this.gui.onDisconnected();
         this.isLocalServer = false;
      }

      this.level = null;
      this.updateLevelInEngines((ClientLevel)null);
      this.player = null;
      SkullBlockEntity.clear();
   }

   private void updateScreenAndTick(Screen screen) {
      this.profiler.push("forcedTick");
      this.soundManager.stop();
      this.cameraEntity = null;
      this.pendingConnection = null;
      this.setScreen(screen);
      this.runTick(false);
      this.profiler.pop();
   }

   public void forceSetScreen(Screen screen) {
      this.profiler.push("forcedTick");
      this.setScreen(screen);
      this.runTick(false);
      this.profiler.pop();
   }

   private void updateLevelInEngines(@Nullable ClientLevel clientlevel) {
      this.levelRenderer.setLevel(clientlevel);
      this.particleEngine.setLevel(clientlevel);
      this.blockEntityRenderDispatcher.setLevel(clientlevel);
      this.updateTitle();
   }

   public boolean telemetryOptInExtra() {
      return this.extraTelemetryAvailable() && this.options.telemetryOptInExtra().get();
   }

   public boolean extraTelemetryAvailable() {
      return this.allowsTelemetry() && this.userApiService.properties().flag(UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
   }

   public boolean allowsTelemetry() {
      return this.userApiService.properties().flag(UserFlag.TELEMETRY_ENABLED);
   }

   public boolean allowsMultiplayer() {
      return this.allowsMultiplayer && this.userApiService.properties().flag(UserFlag.SERVERS_ALLOWED) && this.multiplayerBan() == null;
   }

   public boolean allowsRealms() {
      return this.userApiService.properties().flag(UserFlag.REALMS_ALLOWED) && this.multiplayerBan() == null;
   }

   public boolean shouldShowBanNotice() {
      return this.multiplayerBan() != null;
   }

   @Nullable
   public BanDetails multiplayerBan() {
      return this.userApiService.properties().bannedScopes().get("MULTIPLAYER");
   }

   public boolean isBlocked(UUID uuid) {
      if (this.getChatStatus().isChatAllowed(false)) {
         return this.playerSocialManager.shouldHideMessageFrom(uuid);
      } else {
         return (this.player == null || !uuid.equals(this.player.getUUID())) && !uuid.equals(Util.NIL_UUID);
      }
   }

   public Minecraft.ChatStatus getChatStatus() {
      if (this.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
         return Minecraft.ChatStatus.DISABLED_BY_OPTIONS;
      } else if (!this.allowsChat) {
         return Minecraft.ChatStatus.DISABLED_BY_LAUNCHER;
      } else {
         return !this.userApiService.properties().flag(UserFlag.CHAT_ALLOWED) ? Minecraft.ChatStatus.DISABLED_BY_PROFILE : Minecraft.ChatStatus.ENABLED;
      }
   }

   public final boolean isDemo() {
      return this.demo;
   }

   @Nullable
   public ClientPacketListener getConnection() {
      return this.player == null ? null : this.player.connection;
   }

   public static boolean renderNames() {
      return !instance.options.hideGui;
   }

   public static boolean useFancyGraphics() {
      return instance.options.graphicsMode().get().getId() >= GraphicsStatus.FANCY.getId();
   }

   public static boolean useShaderTransparency() {
      return !instance.gameRenderer.isPanoramicMode() && instance.options.graphicsMode().get().getId() >= GraphicsStatus.FABULOUS.getId();
   }

   public static boolean useAmbientOcclusion() {
      return instance.options.ambientOcclusion().get();
   }

   private void pickBlock() {
      if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
         boolean flag = this.player.getAbilities().instabuild;
         BlockEntity blockentity = null;
         HitResult.Type hitresult_type = this.hitResult.getType();
         ItemStack itemstack;
         if (hitresult_type == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)this.hitResult).getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.isAir()) {
               return;
            }

            Block block = blockstate.getBlock();
            itemstack = block.getCloneItemStack(this.level, blockpos, blockstate);
            if (itemstack.isEmpty()) {
               return;
            }

            if (flag && Screen.hasControlDown() && blockstate.hasBlockEntity()) {
               blockentity = this.level.getBlockEntity(blockpos);
            }
         } else {
            if (hitresult_type != HitResult.Type.ENTITY || !flag) {
               return;
            }

            Entity entity = ((EntityHitResult)this.hitResult).getEntity();
            itemstack = entity.getPickResult();
            if (itemstack == null) {
               return;
            }
         }

         if (itemstack.isEmpty()) {
            String s = "";
            if (hitresult_type == HitResult.Type.BLOCK) {
               s = BuiltInRegistries.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
            } else if (hitresult_type == HitResult.Type.ENTITY) {
               s = BuiltInRegistries.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
            }

            LOGGER.warn("Picking on: [{}] {} gave null item", hitresult_type, s);
         } else {
            Inventory inventory = this.player.getInventory();
            if (blockentity != null) {
               this.addCustomNbtData(itemstack, blockentity);
            }

            int i = inventory.findSlotMatchingItem(itemstack);
            if (flag) {
               inventory.setPickedItem(itemstack);
               this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
            } else if (i != -1) {
               if (Inventory.isHotbarSlot(i)) {
                  inventory.selected = i;
               } else {
                  this.gameMode.handlePickItem(i);
               }
            }

         }
      }
   }

   private void addCustomNbtData(ItemStack itemstack, BlockEntity blockentity) {
      CompoundTag compoundtag = blockentity.saveWithFullMetadata();
      BlockItem.setBlockEntityData(itemstack, blockentity.getType(), compoundtag);
      if (itemstack.getItem() instanceof PlayerHeadItem && compoundtag.contains("SkullOwner")) {
         CompoundTag compoundtag1 = compoundtag.getCompound("SkullOwner");
         CompoundTag compoundtag2 = itemstack.getOrCreateTag();
         compoundtag2.put("SkullOwner", compoundtag1);
         CompoundTag compoundtag3 = compoundtag2.getCompound("BlockEntityTag");
         compoundtag3.remove("SkullOwner");
         compoundtag3.remove("x");
         compoundtag3.remove("y");
         compoundtag3.remove("z");
      } else {
         CompoundTag compoundtag4 = new CompoundTag();
         ListTag listtag = new ListTag();
         listtag.add(StringTag.valueOf("\"(+NBT)\""));
         compoundtag4.put("Lore", listtag);
         itemstack.addTagElement("display", compoundtag4);
      }
   }

   public CrashReport fillReport(CrashReport crashreport) {
      SystemReport systemreport = crashreport.getSystemReport();
      fillSystemReport(systemreport, this, this.languageManager, this.launchedVersion, this.options);
      if (this.level != null) {
         this.level.fillReportDetails(crashreport);
      }

      if (this.singleplayerServer != null) {
         this.singleplayerServer.fillSystemReport(systemreport);
      }

      this.reloadStateTracker.fillCrashReport(crashreport);
      return crashreport;
   }

   public static void fillReport(@Nullable Minecraft minecraft, @Nullable LanguageManager languagemanager, String s, @Nullable Options options, CrashReport crashreport) {
      SystemReport systemreport = crashreport.getSystemReport();
      fillSystemReport(systemreport, minecraft, languagemanager, s, options);
   }

   private static SystemReport fillSystemReport(SystemReport systemreport, @Nullable Minecraft minecraft, @Nullable LanguageManager languagemanager, String s, Options options) {
      systemreport.setDetail("Launched Version", () -> s);
      systemreport.setDetail("Backend library", RenderSystem::getBackendDescription);
      systemreport.setDetail("Backend API", RenderSystem::getApiDescription);
      systemreport.setDetail("Window size", () -> minecraft != null ? minecraft.window.getWidth() + "x" + minecraft.window.getHeight() : "<not initialized>");
      systemreport.setDetail("GL Caps", RenderSystem::getCapsString);
      systemreport.setDetail("GL debug messages", () -> GlDebug.isDebugEnabled() ? String.join("\n", GlDebug.getLastOpenGlDebugMessages()) : "<disabled>");
      systemreport.setDetail("Using VBOs", () -> "Yes");
      systemreport.setDetail("Is Modded", () -> checkModStatus().fullDescription());
      systemreport.setDetail("Type", "Client (map_client.txt)");
      if (options != null) {
         if (instance != null) {
            String s1 = instance.getGpuWarnlistManager().getAllWarnings();
            if (s1 != null) {
               systemreport.setDetail("GPU Warnings", s1);
            }
         }

         systemreport.setDetail("Graphics mode", options.graphicsMode().get().toString());
         systemreport.setDetail("Resource Packs", () -> {
            StringBuilder stringbuilder = new StringBuilder();

            for(String s2 : options.resourcePacks) {
               if (stringbuilder.length() > 0) {
                  stringbuilder.append(", ");
               }

               stringbuilder.append(s2);
               if (options.incompatibleResourcePacks.contains(s2)) {
                  stringbuilder.append(" (incompatible)");
               }
            }

            return stringbuilder.toString();
         });
      }

      if (languagemanager != null) {
         systemreport.setDetail("Current Language", () -> languagemanager.getSelected());
      }

      systemreport.setDetail("CPU", GlUtil::getCpuInfo);
      return systemreport;
   }

   public static Minecraft getInstance() {
      return instance;
   }

   public CompletableFuture<Void> delayTextureReload() {
      return this.submit(this::reloadResourcePacks).thenCompose((completablefuture) -> completablefuture);
   }

   public void updateReportEnvironment(ReportEnvironment reportenvironment) {
      if (!this.reportingContext.matches(reportenvironment)) {
         this.reportingContext = ReportingContext.create(reportenvironment, this.userApiService);
      }

   }

   @Nullable
   public ServerData getCurrentServer() {
      return Optionull.map(this.getConnection(), ClientPacketListener::getServerData);
   }

   public boolean isLocalServer() {
      return this.isLocalServer;
   }

   public boolean hasSingleplayerServer() {
      return this.isLocalServer && this.singleplayerServer != null;
   }

   @Nullable
   public IntegratedServer getSingleplayerServer() {
      return this.singleplayerServer;
   }

   public boolean isSingleplayer() {
      IntegratedServer integratedserver = this.getSingleplayerServer();
      return integratedserver != null && !integratedserver.isPublished();
   }

   public User getUser() {
      return this.user;
   }

   public PropertyMap getProfileProperties() {
      if (this.profileProperties.isEmpty()) {
         GameProfile gameprofile = this.getMinecraftSessionService().fillProfileProperties(this.user.getGameProfile(), false);
         this.profileProperties.putAll(gameprofile.getProperties());
      }

      return this.profileProperties;
   }

   public Proxy getProxy() {
      return this.proxy;
   }

   public TextureManager getTextureManager() {
      return this.textureManager;
   }

   public ResourceManager getResourceManager() {
      return this.resourceManager;
   }

   public PackRepository getResourcePackRepository() {
      return this.resourcePackRepository;
   }

   public VanillaPackResources getVanillaPackResources() {
      return this.vanillaPackResources;
   }

   public DownloadedPackSource getDownloadedPackSource() {
      return this.downloadedPackSource;
   }

   public Path getResourcePackDirectory() {
      return this.resourcePackDirectory;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation resourcelocation) {
      return this.modelManager.getAtlas(resourcelocation)::getSprite;
   }

   public boolean is64Bit() {
      return this.is64bit;
   }

   public boolean isPaused() {
      return this.pause;
   }

   public GpuWarnlistManager getGpuWarnlistManager() {
      return this.gpuWarnlistManager;
   }

   public SoundManager getSoundManager() {
      return this.soundManager;
   }

   public Music getSituationalMusic() {
      Music music = Optionull.map(this.screen, Screen::getBackgroundMusic);
      if (music != null) {
         return music;
      } else if (this.player != null) {
         if (this.player.level().dimension() == Level.END) {
            return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
         } else {
            Holder<Biome> holder = this.player.level().getBiome(this.player.blockPosition());
            if (!this.musicManager.isPlayingMusic(Musics.UNDER_WATER) && (!this.player.isUnderWater() || !holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC))) {
               return this.player.level().dimension() != Level.NETHER && this.player.getAbilities().instabuild && this.player.getAbilities().mayfly ? Musics.CREATIVE : holder.value().getBackgroundMusic().orElse(Musics.GAME);
            } else {
               return Musics.UNDER_WATER;
            }
         }
      } else {
         return Musics.MENU;
      }
   }

   public MinecraftSessionService getMinecraftSessionService() {
      return this.minecraftSessionService;
   }

   public SkinManager getSkinManager() {
      return this.skinManager;
   }

   @Nullable
   public Entity getCameraEntity() {
      return this.cameraEntity;
   }

   public void setCameraEntity(Entity entity) {
      this.cameraEntity = entity;
      this.gameRenderer.checkEntityPostEffect(entity);
   }

   public boolean shouldEntityAppearGlowing(Entity entity) {
      return entity.isCurrentlyGlowing() || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && entity.getType() == EntityType.PLAYER;
   }

   protected Thread getRunningThread() {
      return this.gameThread;
   }

   protected Runnable wrapRunnable(Runnable runnable) {
      return runnable;
   }

   protected boolean shouldRun(Runnable runnable) {
      return true;
   }

   public BlockRenderDispatcher getBlockRenderer() {
      return this.blockRenderer;
   }

   public EntityRenderDispatcher getEntityRenderDispatcher() {
      return this.entityRenderDispatcher;
   }

   public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
      return this.blockEntityRenderDispatcher;
   }

   public ItemRenderer getItemRenderer() {
      return this.itemRenderer;
   }

   public <T> SearchTree<T> getSearchTree(SearchRegistry.Key<T> searchregistry_key) {
      return this.searchRegistry.getTree(searchregistry_key);
   }

   public <T> void populateSearchTree(SearchRegistry.Key<T> searchregistry_key, List<T> list) {
      this.searchRegistry.populate(searchregistry_key, list);
   }

   public FrameTimer getFrameTimer() {
      return this.frameTimer;
   }

   public boolean isConnectedToRealms() {
      return this.connectedToRealms;
   }

   public void setConnectedToRealms(boolean flag) {
      this.connectedToRealms = flag;
   }

   public DataFixer getFixerUpper() {
      return this.fixerUpper;
   }

   public float getFrameTime() {
      return this.timer.partialTick;
   }

   public float getDeltaFrameTime() {
      return this.timer.tickDelta;
   }

   public BlockColors getBlockColors() {
      return this.blockColors;
   }

   public boolean showOnlyReducedInfo() {
      return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo().get();
   }

   public ToastComponent getToasts() {
      return this.toast;
   }

   public Tutorial getTutorial() {
      return this.tutorial;
   }

   public boolean isWindowActive() {
      return this.windowActive;
   }

   public HotbarManager getHotbarManager() {
      return this.hotbarManager;
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public PaintingTextureManager getPaintingTextures() {
      return this.paintingTextures;
   }

   public MobEffectTextureManager getMobEffectTextures() {
      return this.mobEffectTextures;
   }

   public void setWindowActive(boolean flag) {
      this.windowActive = flag;
   }

   public Component grabPanoramixScreenshot(File file, int i, int j) {
      int k = this.window.getWidth();
      int l = this.window.getHeight();
      RenderTarget rendertarget = new TextureTarget(i, j, true, ON_OSX);
      float f = this.player.getXRot();
      float f1 = this.player.getYRot();
      float f2 = this.player.xRotO;
      float f3 = this.player.yRotO;
      this.gameRenderer.setRenderBlockOutline(false);

      MutableComponent var12;
      try {
         this.gameRenderer.setPanoramicMode(true);
         this.levelRenderer.graphicsChanged();
         this.window.setWidth(i);
         this.window.setHeight(j);

         for(int i1 = 0; i1 < 6; ++i1) {
            switch (i1) {
               case 0:
                  this.player.setYRot(f1);
                  this.player.setXRot(0.0F);
                  break;
               case 1:
                  this.player.setYRot((f1 + 90.0F) % 360.0F);
                  this.player.setXRot(0.0F);
                  break;
               case 2:
                  this.player.setYRot((f1 + 180.0F) % 360.0F);
                  this.player.setXRot(0.0F);
                  break;
               case 3:
                  this.player.setYRot((f1 - 90.0F) % 360.0F);
                  this.player.setXRot(0.0F);
                  break;
               case 4:
                  this.player.setYRot(f1);
                  this.player.setXRot(-90.0F);
                  break;
               case 5:
               default:
                  this.player.setYRot(f1);
                  this.player.setXRot(90.0F);
            }

            this.player.yRotO = this.player.getYRot();
            this.player.xRotO = this.player.getXRot();
            rendertarget.bindWrite(true);
            this.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());

            try {
               Thread.sleep(10L);
            } catch (InterruptedException var17) {
            }

            Screenshot.grab(file, "panorama_" + i1 + ".png", rendertarget, (component1) -> {
            });
         }

         Component component = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
         return Component.translatable("screenshot.success", component);
      } catch (Exception var18) {
         LOGGER.error("Couldn't save image", (Throwable)var18);
         var12 = Component.translatable("screenshot.failure", var18.getMessage());
      } finally {
         this.player.setXRot(f);
         this.player.setYRot(f1);
         this.player.xRotO = f2;
         this.player.yRotO = f3;
         this.gameRenderer.setRenderBlockOutline(true);
         this.window.setWidth(k);
         this.window.setHeight(l);
         rendertarget.destroyBuffers();
         this.gameRenderer.setPanoramicMode(false);
         this.levelRenderer.graphicsChanged();
         this.getMainRenderTarget().bindWrite(true);
      }

      return var12;
   }

   private Component grabHugeScreenshot(File file, int i, int j, int k, int l) {
      try {
         ByteBuffer bytebuffer = GlUtil.allocateMemory(i * j * 3);
         Screenshot screenshot = new Screenshot(file, k, l, j);
         float f = (float)k / (float)i;
         float f1 = (float)l / (float)j;
         float f2 = f > f1 ? f : f1;

         for(int i1 = (l - 1) / j * j; i1 >= 0; i1 -= j) {
            for(int j1 = 0; j1 < k; j1 += i) {
               RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
               float f3 = (float)(k - i) / 2.0F * 2.0F - (float)(j1 * 2);
               float f4 = (float)(l - j) / 2.0F * 2.0F - (float)(i1 * 2);
               f3 /= (float)i;
               f4 /= (float)j;
               this.gameRenderer.renderZoomed(f2, f3, f4);
               bytebuffer.clear();
               RenderSystem.pixelStore(3333, 1);
               RenderSystem.pixelStore(3317, 1);
               RenderSystem.readPixels(0, 0, i, j, 32992, 5121, bytebuffer);
               screenshot.addRegion(bytebuffer, j1, i1, i, j);
            }

            screenshot.saveRow();
         }

         File file1 = screenshot.close();
         GlUtil.freeMemory(bytebuffer);
         Component component = Component.literal(file1.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file1.getAbsolutePath())));
         return Component.translatable("screenshot.success", component);
      } catch (Exception var15) {
         LOGGER.warn("Couldn't save screenshot", (Throwable)var15);
         return Component.translatable("screenshot.failure", var15.getMessage());
      }
   }

   public ProfilerFiller getProfiler() {
      return this.profiler;
   }

   @Nullable
   public StoringChunkProgressListener getProgressListener() {
      return this.progressListener.get();
   }

   public SplashManager getSplashManager() {
      return this.splashManager;
   }

   @Nullable
   public Overlay getOverlay() {
      return this.overlay;
   }

   public PlayerSocialManager getPlayerSocialManager() {
      return this.playerSocialManager;
   }

   public boolean renderOnThread() {
      return false;
   }

   public Window getWindow() {
      return this.window;
   }

   public RenderBuffers renderBuffers() {
      return this.renderBuffers;
   }

   public void updateMaxMipLevel(int i) {
      this.modelManager.updateMaxMipLevel(i);
   }

   public EntityModelSet getEntityModels() {
      return this.entityModels;
   }

   public boolean isTextFilteringEnabled() {
      return this.userApiService.properties().flag(UserFlag.PROFANITY_FILTER_ENABLED);
   }

   public void prepareForMultiplayer() {
      this.playerSocialManager.startOnlineMode();
      this.getProfileKeyPairManager().prepareKeyPair();
   }

   public Realms32BitWarningStatus getRealms32BitWarningStatus() {
      return this.realms32BitWarningStatus;
   }

   @Nullable
   public SignatureValidator getProfileKeySignatureValidator() {
      return SignatureValidator.from(this.authenticationService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
   }

   public InputType getLastInputType() {
      return this.lastInputType;
   }

   public void setLastInputType(InputType inputtype) {
      this.lastInputType = inputtype;
   }

   public GameNarrator getNarrator() {
      return this.narrator;
   }

   public ChatListener getChatListener() {
      return this.chatListener;
   }

   public ReportingContext getReportingContext() {
      return this.reportingContext;
   }

   public RealmsDataFetcher realmsDataFetcher() {
      return this.realmsDataFetcher;
   }

   public QuickPlayLog quickPlayLog() {
      return this.quickPlayLog;
   }

   public static enum ChatStatus {
      ENABLED(CommonComponents.EMPTY) {
         public boolean isChatAllowed(boolean flag) {
            return true;
         }
      },
      DISABLED_BY_OPTIONS(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED)) {
         public boolean isChatAllowed(boolean flag) {
            return false;
         }
      },
      DISABLED_BY_LAUNCHER(Component.translatable("chat.disabled.launcher").withStyle(ChatFormatting.RED)) {
         public boolean isChatAllowed(boolean flag) {
            return flag;
         }
      },
      DISABLED_BY_PROFILE(Component.translatable("chat.disabled.profile", Component.keybind(Minecraft.instance.options.keyChat.getName())).withStyle(ChatFormatting.RED)) {
         public boolean isChatAllowed(boolean flag) {
            return flag;
         }
      };

      static final Component INFO_DISABLED_BY_PROFILE = Component.translatable("chat.disabled.profile.moreInfo");
      private final Component message;

      ChatStatus(Component component) {
         this.message = component;
      }

      public Component getMessage() {
         return this.message;
      }

      public abstract boolean isChatAllowed(boolean flag);
   }
}
