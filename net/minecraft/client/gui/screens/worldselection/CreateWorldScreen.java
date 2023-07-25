package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class CreateWorldScreen extends Screen {
   private static final int GROUP_BOTTOM = 1;
   private static final int TAB_COLUMN_WIDTH = 210;
   private static final int FOOTER_HEIGHT = 36;
   private static final int TEXT_INDENT = 1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TEMP_WORLD_PREFIX = "mcworld-";
   static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
   static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
   static final Component EXPERIMENTS_LABEL = Component.translatable("selectWorld.experiments");
   static final Component ALLOW_CHEATS_INFO = Component.translatable("selectWorld.allowCommands.info");
   private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
   private static final int HORIZONTAL_BUTTON_SPACING = 10;
   private static final int VERTICAL_BUTTON_SPACING = 8;
   public static final ResourceLocation HEADER_SEPERATOR = new ResourceLocation("textures/gui/header_separator.png");
   public static final ResourceLocation FOOTER_SEPERATOR = new ResourceLocation("textures/gui/footer_separator.png");
   public static final ResourceLocation LIGHT_DIRT_BACKGROUND = new ResourceLocation("textures/gui/light_dirt_background.png");
   final WorldCreationUiState uiState;
   private final TabManager tabManager = new TabManager(this::addRenderableWidget, (guieventlistener) -> this.removeWidget(guieventlistener));
   private boolean recreated;
   @Nullable
   private final Screen lastScreen;
   @Nullable
   private Path tempDataPackDir;
   @Nullable
   private PackRepository tempDataPackRepository;
   @Nullable
   private GridLayout bottomButtons;
   @Nullable
   private TabNavigationBar tabNavigationBar;

   public static void openFresh(Minecraft minecraft, @Nullable Screen screen) {
      queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
      PackRepository packrepository = new PackRepository(new ServerPacksSource());
      WorldLoader.InitConfig worldloader_initconfig = createDefaultLoadConfig(packrepository, WorldDataConfiguration.DEFAULT);
      CompletableFuture<WorldCreationContext> completablefuture = WorldLoader.load(worldloader_initconfig, (worldloader_dataloadcontext) -> new WorldLoader.DataLoadOutput<>(new CreateWorldScreen.DataPackReloadCookie(new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(worldloader_dataloadcontext.datapackWorldgen())), worldloader_dataloadcontext.dataConfiguration()), worldloader_dataloadcontext.datapackDimensions()), (closeableresourcemanager, reloadableserverresources, layeredregistryaccess, createworldscreen_datapackreloadcookie) -> {
         closeableresourcemanager.close();
         return new WorldCreationContext(createworldscreen_datapackreloadcookie.worldGenSettings(), layeredregistryaccess, reloadableserverresources, createworldscreen_datapackreloadcookie.dataConfiguration());
      }, Util.backgroundExecutor(), minecraft);
      minecraft.managedBlock(completablefuture::isDone);
      minecraft.setScreen(new CreateWorldScreen(minecraft, screen, completablefuture.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty()));
   }

   public static CreateWorldScreen createFromExisting(Minecraft minecraft, @Nullable Screen screen, LevelSettings levelsettings, WorldCreationContext worldcreationcontext, @Nullable Path path) {
      CreateWorldScreen createworldscreen = new CreateWorldScreen(minecraft, screen, worldcreationcontext, WorldPresets.fromSettings(worldcreationcontext.selectedDimensions().dimensions()), OptionalLong.of(worldcreationcontext.options().seed()));
      createworldscreen.recreated = true;
      createworldscreen.uiState.setName(levelsettings.levelName());
      createworldscreen.uiState.setAllowCheats(levelsettings.allowCommands());
      createworldscreen.uiState.setDifficulty(levelsettings.difficulty());
      createworldscreen.uiState.getGameRules().assignFrom(levelsettings.gameRules(), (MinecraftServer)null);
      if (levelsettings.hardcore()) {
         createworldscreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
      } else if (levelsettings.gameType().isSurvival()) {
         createworldscreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
      } else if (levelsettings.gameType().isCreative()) {
         createworldscreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
      }

      createworldscreen.tempDataPackDir = path;
      return createworldscreen;
   }

   private CreateWorldScreen(Minecraft minecraft, @Nullable Screen screen, WorldCreationContext worldcreationcontext, Optional<ResourceKey<WorldPreset>> optional, OptionalLong optionallong) {
      super(Component.translatable("selectWorld.create"));
      this.lastScreen = screen;
      this.uiState = new WorldCreationUiState(minecraft.getLevelSource().getBaseDir(), worldcreationcontext, optional, optionallong);
   }

   public WorldCreationUiState getUiState() {
      return this.uiState;
   }

   public void tick() {
      this.tabManager.tickCurrent();
   }

   protected void init() {
      this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new CreateWorldScreen.GameTab(), new CreateWorldScreen.WorldTab(), new CreateWorldScreen.MoreTab()).build();
      this.addRenderableWidget(this.tabNavigationBar);
      this.bottomButtons = (new GridLayout()).columnSpacing(10);
      GridLayout.RowHelper gridlayout_rowhelper = this.bottomButtons.createRowHelper(2);
      gridlayout_rowhelper.addChild(Button.builder(Component.translatable("selectWorld.create"), (button1) -> this.onCreate()).build());
      gridlayout_rowhelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.popScreen()).build());
      this.bottomButtons.visitWidgets((abstractwidget) -> {
         abstractwidget.setTabOrderGroup(1);
         this.addRenderableWidget(abstractwidget);
      });
      this.tabNavigationBar.selectTab(0, false);
      this.uiState.onChanged();
      this.repositionElements();
   }

   public void repositionElements() {
      if (this.tabNavigationBar != null && this.bottomButtons != null) {
         this.tabNavigationBar.setWidth(this.width);
         this.tabNavigationBar.arrangeElements();
         this.bottomButtons.arrangeElements();
         FrameLayout.centerInRectangle(this.bottomButtons, 0, this.height - 36, this.width, 36);
         int i = this.tabNavigationBar.getRectangle().bottom();
         ScreenRectangle screenrectangle = new ScreenRectangle(0, i, this.width, this.bottomButtons.getY() - i);
         this.tabManager.setTabArea(screenrectangle);
      }
   }

   private static void queueLoadScreen(Minecraft minecraft, Component component) {
      minecraft.forceSetScreen(new GenericDirtMessageScreen(component));
   }

   private void onCreate() {
      WorldCreationContext worldcreationcontext = this.uiState.getSettings();
      WorldDimensions.Complete worlddimensions_complete = worldcreationcontext.selectedDimensions().bake(worldcreationcontext.datapackDimensions());
      LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = worldcreationcontext.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, worlddimensions_complete.dimensionsRegistryAccess());
      Lifecycle lifecycle = FeatureFlags.isExperimental(worldcreationcontext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
      Lifecycle lifecycle1 = layeredregistryaccess.compositeAccess().allRegistriesLifecycle();
      Lifecycle lifecycle2 = lifecycle1.add(lifecycle);
      boolean flag = !this.recreated && lifecycle1 == Lifecycle.stable();
      WorldOpenFlows.confirmWorldCreation(this.minecraft, this, lifecycle2, () -> this.createNewWorld(worlddimensions_complete.specialWorldProperty(), layeredregistryaccess, lifecycle2), flag);
   }

   private void createNewWorld(PrimaryLevelData.SpecialWorldProperty primaryleveldata_specialworldproperty, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, Lifecycle lifecycle) {
      queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
      Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
      if (!optional.isEmpty()) {
         this.removeTempDataPackDir();
         boolean flag = primaryleveldata_specialworldproperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
         WorldCreationContext worldcreationcontext = this.uiState.getSettings();
         LevelSettings levelsettings = this.createLevelSettings(flag);
         WorldData worlddata = new PrimaryLevelData(levelsettings, worldcreationcontext.options(), primaryleveldata_specialworldproperty, lifecycle);
         this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), worldcreationcontext.dataPackResources(), layeredregistryaccess, worlddata);
      }
   }

   private LevelSettings createLevelSettings(boolean flag) {
      String s = this.uiState.getName().trim();
      if (flag) {
         GameRules gamerules = new GameRules();
         gamerules.getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
         return new LevelSettings(s, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gamerules, WorldDataConfiguration.DEFAULT);
      } else {
         return new LevelSettings(s, this.uiState.getGameMode().gameType, this.uiState.isHardcore(), this.uiState.getDifficulty(), this.uiState.isAllowCheats(), this.uiState.getGameRules(), this.uiState.getSettings().dataConfiguration());
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.tabNavigationBar.keyPressed(i)) {
         return true;
      } else if (super.keyPressed(i, j, k)) {
         return true;
      } else if (i != 257 && i != 335) {
         return false;
      } else {
         this.onCreate();
         return true;
      }
   }

   public void onClose() {
      this.popScreen();
   }

   public void popScreen() {
      this.minecraft.setScreen(this.lastScreen);
      this.removeTempDataPackDir();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.blit(FOOTER_SEPERATOR, 0, Mth.roundToward(this.height - 36 - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
      super.render(guigraphics, i, j, f);
   }

   public void renderDirtBackground(GuiGraphics guigraphics) {
      int i = 32;
      guigraphics.blit(LIGHT_DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
   }

   protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guieventlistener) {
      return super.addWidget(guieventlistener);
   }

   protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guieventlistener) {
      return super.addRenderableWidget(guieventlistener);
   }

   @Nullable
   private Path getTempDataPackDir() {
      if (this.tempDataPackDir == null) {
         try {
            this.tempDataPackDir = Files.createTempDirectory("mcworld-");
         } catch (IOException var2) {
            LOGGER.warn("Failed to create temporary dir", (Throwable)var2);
            SystemToast.onPackCopyFailure(this.minecraft, this.uiState.getTargetFolder());
            this.popScreen();
         }
      }

      return this.tempDataPackDir;
   }

   void openExperimentsScreen(WorldDataConfiguration worlddataconfiguration) {
      Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worlddataconfiguration);
      if (pair != null) {
         this.minecraft.setScreen(new ExperimentsScreen(this, pair.getSecond(), (packrepository) -> this.tryApplyNewDataPacks(packrepository, false, this::openExperimentsScreen)));
      }

   }

   void openDataPackSelectionScreen(WorldDataConfiguration worlddataconfiguration) {
      Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worlddataconfiguration);
      if (pair != null) {
         this.minecraft.setScreen(new PackSelectionScreen(pair.getSecond(), (packrepository) -> this.tryApplyNewDataPacks(packrepository, true, this::openDataPackSelectionScreen), pair.getFirst(), Component.translatable("dataPack.title")));
      }

   }

   private void tryApplyNewDataPacks(PackRepository packrepository, boolean flag, Consumer<WorldDataConfiguration> consumer) {
      List<String> list = ImmutableList.copyOf(packrepository.getSelectedIds());
      List<String> list1 = packrepository.getAvailableIds().stream().filter((s) -> !list.contains(s)).collect(ImmutableList.toImmutableList());
      WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(new DataPackConfig(list, list1), this.uiState.getSettings().dataConfiguration().enabledFeatures());
      if (this.uiState.tryUpdateDataConfiguration(worlddataconfiguration)) {
         this.minecraft.setScreen(this);
      } else {
         FeatureFlagSet featureflagset = packrepository.getRequestedFeatureFlags();
         if (FeatureFlags.isExperimental(featureflagset) && flag) {
            this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(packrepository.getSelectedPacks(), (flag1) -> {
               if (flag1) {
                  this.applyNewPackConfig(packrepository, worlddataconfiguration, consumer);
               } else {
                  consumer.accept(this.uiState.getSettings().dataConfiguration());
               }

            }));
         } else {
            this.applyNewPackConfig(packrepository, worlddataconfiguration, consumer);
         }

      }
   }

   private void applyNewPackConfig(PackRepository packrepository, WorldDataConfiguration worlddataconfiguration, Consumer<WorldDataConfiguration> consumer) {
      this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working")));
      WorldLoader.InitConfig worldloader_initconfig = createDefaultLoadConfig(packrepository, worlddataconfiguration);
      WorldLoader.load(worldloader_initconfig, (worldloader_dataloadcontext) -> {
         if (worldloader_dataloadcontext.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).size() == 0) {
            throw new IllegalStateException("Needs at least one world preset to continue");
         } else if (worldloader_dataloadcontext.datapackWorldgen().registryOrThrow(Registries.BIOME).size() == 0) {
            throw new IllegalStateException("Needs at least one biome continue");
         } else {
            WorldCreationContext worldcreationcontext = this.uiState.getSettings();
            DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, worldcreationcontext.worldgenLoadContext());
            DataResult<JsonElement> dataresult = WorldGenSettings.encode(dynamicops, worldcreationcontext.options(), worldcreationcontext.selectedDimensions()).setLifecycle(Lifecycle.stable());
            DynamicOps<JsonElement> dynamicops1 = RegistryOps.create(JsonOps.INSTANCE, worldloader_dataloadcontext.datapackWorldgen());
            WorldGenSettings worldgensettings = dataresult.flatMap((jsonelement) -> WorldGenSettings.CODEC.parse(dynamicops1, jsonelement)).getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
            return new WorldLoader.DataLoadOutput<>(new CreateWorldScreen.DataPackReloadCookie(worldgensettings, worldloader_dataloadcontext.dataConfiguration()), worldloader_dataloadcontext.datapackDimensions());
         }
      }, (closeableresourcemanager, reloadableserverresources, layeredregistryaccess, createworldscreen_datapackreloadcookie) -> {
         closeableresourcemanager.close();
         return new WorldCreationContext(createworldscreen_datapackreloadcookie.worldGenSettings(), layeredregistryaccess, reloadableserverresources, createworldscreen_datapackreloadcookie.dataConfiguration());
      }, Util.backgroundExecutor(), this.minecraft).thenAcceptAsync(this.uiState::setSettings, this.minecraft).handle((ovoid, throwable) -> {
         if (throwable != null) {
            LOGGER.warn("Failed to validate datapack", throwable);
            this.minecraft.setScreen(new ConfirmScreen((flag) -> {
               if (flag) {
                  consumer.accept(this.uiState.getSettings().dataConfiguration());
               } else {
                  consumer.accept(WorldDataConfiguration.DEFAULT);
               }

            }, Component.translatable("dataPack.validation.failed"), CommonComponents.EMPTY, Component.translatable("dataPack.validation.back"), Component.translatable("dataPack.validation.reset")));
         } else {
            this.minecraft.setScreen(this);
         }

         return null;
      });
   }

   private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packrepository, WorldDataConfiguration worlddataconfiguration) {
      WorldLoader.PackConfig worldloader_packconfig = new WorldLoader.PackConfig(packrepository, worlddataconfiguration, false, true);
      return new WorldLoader.InitConfig(worldloader_packconfig, Commands.CommandSelection.INTEGRATED, 2);
   }

   private void removeTempDataPackDir() {
      if (this.tempDataPackDir != null) {
         try {
            Stream<Path> stream = Files.walk(this.tempDataPackDir);

            try {
               stream.sorted(Comparator.reverseOrder()).forEach((path) -> {
                  try {
                     Files.delete(path);
                  } catch (IOException var2) {
                     LOGGER.warn("Failed to remove temporary file {}", path, var2);
                  }

               });
            } catch (Throwable var5) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var6) {
            LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
         }

         this.tempDataPackDir = null;
      }

   }

   private static void copyBetweenDirs(Path path, Path path1, Path path2) {
      try {
         Util.copyBetweenDirs(path, path1, path2);
      } catch (IOException var4) {
         LOGGER.warn("Failed to copy datapack file from {} to {}", path2, path1);
         throw new UncheckedIOException(var4);
      }
   }

   private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
      String s = this.uiState.getTargetFolder();

      try {
         LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().createAccess(s);
         if (this.tempDataPackDir == null) {
            return Optional.of(levelstoragesource_levelstorageaccess);
         }

         try {
            Stream<Path> stream = Files.walk(this.tempDataPackDir);

            Optional var5;
            try {
               Path path = levelstoragesource_levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR);
               FileUtil.createDirectoriesSafe(path);
               stream.filter((path3) -> !path3.equals(this.tempDataPackDir)).forEach((path2) -> copyBetweenDirs(this.tempDataPackDir, path, path2));
               var5 = Optional.of(levelstoragesource_levelstorageaccess);
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }

            return var5;
         } catch (UncheckedIOException | IOException var8) {
            LOGGER.warn("Failed to copy datapacks to world {}", s, var8);
            levelstoragesource_levelstorageaccess.close();
         }
      } catch (UncheckedIOException | IOException var9) {
         LOGGER.warn("Failed to create access for {}", s, var9);
      }

      SystemToast.onPackCopyFailure(this.minecraft, s);
      this.popScreen();
      return Optional.empty();
   }

   @Nullable
   public static Path createTempDataPackDirFromExistingWorld(Path path, Minecraft minecraft) {
      MutableObject<Path> mutableobject = new MutableObject<>();

      try {
         Stream<Path> stream = Files.walk(path);

         try {
            stream.filter((path5) -> !path5.equals(path)).forEach((path2) -> {
               Path path3 = mutableobject.getValue();
               if (path3 == null) {
                  try {
                     path3 = Files.createTempDirectory("mcworld-");
                  } catch (IOException var5) {
                     LOGGER.warn("Failed to create temporary dir");
                     throw new UncheckedIOException(var5);
                  }

                  mutableobject.setValue(path3);
               }

               copyBetweenDirs(path, path3, path2);
            });
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (UncheckedIOException | IOException var8) {
         LOGGER.warn("Failed to copy datapacks from world {}", path, var8);
         SystemToast.onPackCopyFailure(minecraft, path.toString());
         return null;
      }

      return mutableobject.getValue();
   }

   @Nullable
   private Pair<Path, PackRepository> getDataPackSelectionSettings(WorldDataConfiguration worlddataconfiguration) {
      Path path = this.getTempDataPackDir();
      if (path != null) {
         if (this.tempDataPackRepository == null) {
            this.tempDataPackRepository = ServerPacksSource.createPackRepository(path);
            this.tempDataPackRepository.reload();
         }

         this.tempDataPackRepository.setSelected(worlddataconfiguration.dataPacks().getEnabled());
         return Pair.of(path, this.tempDataPackRepository);
      } else {
         return null;
      }
   }

   static record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
   }

   class GameTab extends GridLayoutTab {
      private static final Component TITLE = Component.translatable("createWorld.tab.game.title");
      private static final Component ALLOW_CHEATS = Component.translatable("selectWorld.allowCommands");
      private final EditBox nameEdit;

      GameTab() {
         super(TITLE);
         GridLayout.RowHelper gridlayout_rowhelper = this.layout.rowSpacing(8).createRowHelper(1);
         LayoutSettings layoutsettings = gridlayout_rowhelper.newCellSettings();
         GridLayout.RowHelper gridlayout_rowhelper1 = (new GridLayout()).rowSpacing(4).createRowHelper(1);
         gridlayout_rowhelper1.addChild(new StringWidget(CreateWorldScreen.NAME_LABEL, CreateWorldScreen.this.minecraft.font), gridlayout_rowhelper1.newCellSettings().paddingLeft(1));
         this.nameEdit = gridlayout_rowhelper1.addChild(new EditBox(CreateWorldScreen.this.font, 0, 0, 208, 20, Component.translatable("selectWorld.enterName")), gridlayout_rowhelper1.newCellSettings().padding(1));
         this.nameEdit.setValue(CreateWorldScreen.this.uiState.getName());
         this.nameEdit.setResponder(CreateWorldScreen.this.uiState::setName);
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate3) -> this.nameEdit.setTooltip(Tooltip.create(Component.translatable("selectWorld.targetFolder", Component.literal(worldcreationuistate3.getTargetFolder()).withStyle(ChatFormatting.ITALIC)))));
         CreateWorldScreen.this.setInitialFocus(this.nameEdit);
         gridlayout_rowhelper.addChild(gridlayout_rowhelper1.getGrid(), gridlayout_rowhelper.newCellSettings().alignHorizontallyCenter());
         CycleButton<WorldCreationUiState.SelectedGameMode> cyclebutton = gridlayout_rowhelper.addChild(CycleButton.builder((worldcreationuistate_selectedgamemode1) -> worldcreationuistate_selectedgamemode1.displayName).withValues(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE).create(0, 0, 210, 20, CreateWorldScreen.GAME_MODEL_LABEL, (cyclebutton8, worldcreationuistate_selectedgamemode) -> CreateWorldScreen.this.uiState.setGameMode(worldcreationuistate_selectedgamemode)), layoutsettings);
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate2) -> {
            cyclebutton.setValue(worldcreationuistate2.getGameMode());
            cyclebutton.active = !worldcreationuistate2.isDebug();
            cyclebutton.setTooltip(Tooltip.create(worldcreationuistate2.getGameMode().getInfo()));
         });
         CycleButton<Difficulty> cyclebutton1 = gridlayout_rowhelper.addChild(CycleButton.builder(Difficulty::getDisplayName).withValues(Difficulty.values()).create(0, 0, 210, 20, Component.translatable("options.difficulty"), (cyclebutton6, difficulty) -> CreateWorldScreen.this.uiState.setDifficulty(difficulty)), layoutsettings);
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate1) -> {
            cyclebutton1.setValue(CreateWorldScreen.this.uiState.getDifficulty());
            cyclebutton1.active = !CreateWorldScreen.this.uiState.isHardcore();
            cyclebutton1.setTooltip(Tooltip.create(CreateWorldScreen.this.uiState.getDifficulty().getInfo()));
         });
         CycleButton<Boolean> cyclebutton2 = gridlayout_rowhelper.addChild(CycleButton.onOffBuilder().withTooltip((obool1) -> Tooltip.create(CreateWorldScreen.ALLOW_CHEATS_INFO)).create(0, 0, 210, 20, ALLOW_CHEATS, (cyclebutton4, obool) -> CreateWorldScreen.this.uiState.setAllowCheats(obool)));
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate) -> {
            cyclebutton2.setValue(CreateWorldScreen.this.uiState.isAllowCheats());
            cyclebutton2.active = !CreateWorldScreen.this.uiState.isDebug() && !CreateWorldScreen.this.uiState.isHardcore();
         });
         if (!SharedConstants.getCurrentVersion().isStable()) {
            gridlayout_rowhelper.addChild(Button.builder(CreateWorldScreen.EXPERIMENTS_LABEL, (button) -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
         }

      }

      public void tick() {
         this.nameEdit.tick();
      }
   }

   class MoreTab extends GridLayoutTab {
      private static final Component TITLE = Component.translatable("createWorld.tab.more.title");
      private static final Component GAME_RULES_LABEL = Component.translatable("selectWorld.gameRules");
      private static final Component DATA_PACKS_LABEL = Component.translatable("selectWorld.dataPacks");

      MoreTab() {
         super(TITLE);
         GridLayout.RowHelper gridlayout_rowhelper = this.layout.rowSpacing(8).createRowHelper(1);
         gridlayout_rowhelper.addChild(Button.builder(GAME_RULES_LABEL, (button2) -> this.openGameRulesScreen()).width(210).build());
         gridlayout_rowhelper.addChild(Button.builder(CreateWorldScreen.EXPERIMENTS_LABEL, (button1) -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
         gridlayout_rowhelper.addChild(Button.builder(DATA_PACKS_LABEL, (button) -> CreateWorldScreen.this.openDataPackSelectionScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
      }

      private void openGameRulesScreen() {
         CreateWorldScreen.this.minecraft.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.uiState.getGameRules().copy(), (optional) -> {
            CreateWorldScreen.this.minecraft.setScreen(CreateWorldScreen.this);
            optional.ifPresent(CreateWorldScreen.this.uiState::setGameRules);
         }));
      }
   }

   class WorldTab extends GridLayoutTab {
      private static final Component TITLE = Component.translatable("createWorld.tab.world.title");
      private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
      private static final Component GENERATE_STRUCTURES = Component.translatable("selectWorld.mapFeatures");
      private static final Component GENERATE_STRUCTURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
      private static final Component BONUS_CHEST = Component.translatable("selectWorld.bonusItems");
      private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
      static final Component SEED_EMPTY_HINT = Component.translatable("selectWorld.seedInfo").withStyle(ChatFormatting.DARK_GRAY);
      private static final int WORLD_TAB_WIDTH = 310;
      private final EditBox seedEdit;
      private final Button customizeTypeButton;

      WorldTab() {
         super(TITLE);
         GridLayout.RowHelper gridlayout_rowhelper = this.layout.columnSpacing(10).rowSpacing(8).createRowHelper(2);
         CycleButton<WorldCreationUiState.WorldTypeEntry> cyclebutton = gridlayout_rowhelper.addChild(CycleButton.builder(WorldCreationUiState.WorldTypeEntry::describePreset).withValues(this.createWorldTypeValueSupplier()).withCustomNarration(CreateWorldScreen.WorldTab::createTypeButtonNarration).create(0, 0, 150, 20, Component.translatable("selectWorld.mapType"), (cyclebutton3, worldcreationuistate_worldtypeentry1) -> CreateWorldScreen.this.uiState.setWorldType(worldcreationuistate_worldtypeentry1)));
         cyclebutton.setValue(CreateWorldScreen.this.uiState.getWorldType());
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate2) -> {
            WorldCreationUiState.WorldTypeEntry worldcreationuistate_worldtypeentry = worldcreationuistate2.getWorldType();
            cyclebutton.setValue(worldcreationuistate_worldtypeentry);
            if (worldcreationuistate_worldtypeentry.isAmplified()) {
               cyclebutton.setTooltip(Tooltip.create(AMPLIFIED_HELP_TEXT));
            } else {
               cyclebutton.setTooltip((Tooltip)null);
            }

            cyclebutton.active = CreateWorldScreen.this.uiState.getWorldType().preset() != null;
         });
         this.customizeTypeButton = gridlayout_rowhelper.addChild(Button.builder(Component.translatable("selectWorld.customizeType"), (button) -> this.openPresetEditor()).build());
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate1) -> this.customizeTypeButton.active = !worldcreationuistate1.isDebug() && worldcreationuistate1.getPresetEditor() != null);
         GridLayout.RowHelper gridlayout_rowhelper1 = (new GridLayout()).rowSpacing(4).createRowHelper(1);
         gridlayout_rowhelper1.addChild((new StringWidget(SEED_LABEL, CreateWorldScreen.this.font)).alignLeft());
         this.seedEdit = gridlayout_rowhelper1.addChild(new EditBox(CreateWorldScreen.this.font, 0, 0, 308, 20, Component.translatable("selectWorld.enterSeed")) {
            protected MutableComponent createNarrationMessage() {
               return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(CreateWorldScreen.WorldTab.SEED_EMPTY_HINT);
            }
         }, gridlayout_rowhelper.newCellSettings().padding(1));
         this.seedEdit.setHint(SEED_EMPTY_HINT);
         this.seedEdit.setValue(CreateWorldScreen.this.uiState.getSeed());
         this.seedEdit.setResponder((s) -> CreateWorldScreen.this.uiState.setSeed(this.seedEdit.getValue()));
         gridlayout_rowhelper.addChild(gridlayout_rowhelper1.getGrid(), 2);
         SwitchGrid.Builder switchgrid_builder = SwitchGrid.builder(310).withPaddingLeft(1);
         switchgrid_builder.addSwitch(GENERATE_STRUCTURES, CreateWorldScreen.this.uiState::isGenerateStructures, CreateWorldScreen.this.uiState::setGenerateStructures).withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isDebug()).withInfo(GENERATE_STRUCTURES_INFO);
         switchgrid_builder.addSwitch(BONUS_CHEST, CreateWorldScreen.this.uiState::isBonusChest, CreateWorldScreen.this.uiState::setBonusChest).withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isHardcore() && !CreateWorldScreen.this.uiState.isDebug());
         SwitchGrid switchgrid = switchgrid_builder.build((layoutelement) -> gridlayout_rowhelper.addChild(layoutelement, 2));
         CreateWorldScreen.this.uiState.addListener((worldcreationuistate) -> switchgrid.refreshStates());
      }

      private void openPresetEditor() {
         PresetEditor preseteditor = CreateWorldScreen.this.uiState.getPresetEditor();
         if (preseteditor != null) {
            CreateWorldScreen.this.minecraft.setScreen(preseteditor.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.uiState.getSettings()));
         }

      }

      private CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry> createWorldTypeValueSupplier() {
         return new CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry>() {
            public List<WorldCreationUiState.WorldTypeEntry> getSelectedList() {
               return CycleButton.DEFAULT_ALT_LIST_SELECTOR.getAsBoolean() ? CreateWorldScreen.this.uiState.getAltPresetList() : CreateWorldScreen.this.uiState.getNormalPresetList();
            }

            public List<WorldCreationUiState.WorldTypeEntry> getDefaultList() {
               return CreateWorldScreen.this.uiState.getNormalPresetList();
            }
         };
      }

      private static MutableComponent createTypeButtonNarration(CycleButton<WorldCreationUiState.WorldTypeEntry> cyclebutton1) {
         return cyclebutton1.getValue().isAmplified() ? CommonComponents.joinForNarration(cyclebutton1.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT) : cyclebutton1.createDefaultNarrationMessage();
      }

      public void tick() {
         this.seedEdit.tick();
      }
   }
}
