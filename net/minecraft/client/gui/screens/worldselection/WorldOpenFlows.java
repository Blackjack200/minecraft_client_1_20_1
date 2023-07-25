package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SymlinkWarningScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.slf4j.Logger;

public class WorldOpenFlows {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final LevelStorageSource levelSource;

   public WorldOpenFlows(Minecraft minecraft, LevelStorageSource levelstoragesource) {
      this.minecraft = minecraft;
      this.levelSource = levelstoragesource;
   }

   public void loadLevel(Screen screen, String s) {
      this.doLoadLevel(screen, s, false, true);
   }

   public void createFreshLevel(String s, LevelSettings levelsettings, WorldOptions worldoptions, Function<RegistryAccess, WorldDimensions> function) {
      LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.createWorldAccess(s);
      if (levelstoragesource_levelstorageaccess != null) {
         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess);
         WorldDataConfiguration worlddataconfiguration = levelsettings.getDataConfiguration();

         try {
            WorldLoader.PackConfig worldloader_packconfig = new WorldLoader.PackConfig(packrepository, worlddataconfiguration, false, false);
            WorldStem worldstem = this.loadWorldDataBlocking(worldloader_packconfig, (worldloader_dataloadcontext) -> {
               WorldDimensions.Complete worlddimensions_complete = function.apply(worldloader_dataloadcontext.datapackWorldgen()).bake(worldloader_dataloadcontext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
               return new WorldLoader.DataLoadOutput<>(new PrimaryLevelData(levelsettings, worldoptions, worlddimensions_complete.specialWorldProperty(), worlddimensions_complete.lifecycle()), worlddimensions_complete.dimensionsRegistryAccess());
            }, WorldStem::new);
            this.minecraft.doWorldLoad(s, levelstoragesource_levelstorageaccess, packrepository, worldstem, true);
         } catch (Exception var10) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var10);
            safeCloseAccess(levelstoragesource_levelstorageaccess, s);
         }

      }
   }

   @Nullable
   private LevelStorageSource.LevelStorageAccess createWorldAccess(String s) {
      try {
         return this.levelSource.validateAndCreateAccess(s);
      } catch (IOException var3) {
         LOGGER.warn("Failed to read level {} data", s, var3);
         SystemToast.onWorldAccessFailure(this.minecraft, s);
         this.minecraft.setScreen((Screen)null);
         return null;
      } catch (ContentValidationException var4) {
         LOGGER.warn("{}", (Object)var4.getMessage());
         this.minecraft.setScreen(new SymlinkWarningScreen((Screen)null));
         return null;
      }
   }

   public void createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, ReloadableServerResources reloadableserverresources, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, WorldData worlddata) {
      PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess);
      CloseableResourceManager closeableresourcemanager = (new WorldLoader.PackConfig(packrepository, worlddata.getDataConfiguration(), false, false)).createResourceManager().getSecond();
      this.minecraft.doWorldLoad(levelstoragesource_levelstorageaccess.getLevelId(), levelstoragesource_levelstorageaccess, packrepository, new WorldStem(closeableresourcemanager, reloadableserverresources, layeredregistryaccess, worlddata), true);
   }

   private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, boolean flag, PackRepository packrepository) throws Exception {
      WorldLoader.PackConfig worldloader_packconfig = this.getPackConfigFromLevelData(levelstoragesource_levelstorageaccess, flag, packrepository);
      return this.loadWorldDataBlocking(worldloader_packconfig, (worldloader_dataloadcontext) -> {
         DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_dataloadcontext.datapackWorldgen());
         Registry<LevelStem> registry = worldloader_dataloadcontext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
         Pair<WorldData, WorldDimensions.Complete> pair = levelstoragesource_levelstorageaccess.getDataTag(dynamicops, worldloader_dataloadcontext.dataConfiguration(), registry, worldloader_dataloadcontext.datapackWorldgen().allRegistriesLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            return new WorldLoader.DataLoadOutput<>(pair.getFirst(), pair.getSecond().dimensionsRegistryAccess());
         }
      }, WorldStem::new);
   }

   public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess) throws Exception {
      PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess);
      WorldLoader.PackConfig worldloader_packconfig = this.getPackConfigFromLevelData(levelstoragesource_levelstorageaccess, false, packrepository);
      return this.loadWorldDataBlocking(worldloader_packconfig, (worldloader_dataloadcontext) -> {
         DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_dataloadcontext.datapackWorldgen());
         Registry<LevelStem> registry = (new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable())).freeze();
         Pair<WorldData, WorldDimensions.Complete> pair = levelstoragesource_levelstorageaccess.getDataTag(dynamicops, worldloader_dataloadcontext.dataConfiguration(), registry, worldloader_dataloadcontext.datapackWorldgen().allRegistriesLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
               final LevelSettings levelSettings;
               final WorldOptions options;
               final Registry<LevelStem> existingDimensions;
            }

            return new WorldLoader.DataLoadOutput<>(new Data(pair.getFirst().getLevelSettings(), pair.getFirst().worldGenOptions(), pair.getSecond().dimensions()), worldloader_dataloadcontext.datapackDimensions());
         }
      }, (closeableresourcemanager, reloadableserverresources, layeredregistryaccess, worldopenflows_1data) -> {
         closeableresourcemanager.close();
         return Pair.of(worldopenflows_1data.levelSettings, new WorldCreationContext(worldopenflows_1data.options, new WorldDimensions(worldopenflows_1data.existingDimensions), layeredregistryaccess, reloadableserverresources, worldopenflows_1data.levelSettings.getDataConfiguration()));
      });
   }

   private WorldLoader.PackConfig getPackConfigFromLevelData(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, boolean flag, PackRepository packrepository) {
      WorldDataConfiguration worlddataconfiguration = levelstoragesource_levelstorageaccess.getDataConfiguration();
      if (worlddataconfiguration == null) {
         throw new IllegalStateException("Failed to load data pack config");
      } else {
         return new WorldLoader.PackConfig(packrepository, worlddataconfiguration, flag, false);
      }
   }

   public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, boolean flag) throws Exception {
      PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess);
      return this.loadWorldStem(levelstoragesource_levelstorageaccess, flag, packrepository);
   }

   private <D, R> R loadWorldDataBlocking(WorldLoader.PackConfig worldloader_packconfig, WorldLoader.WorldDataSupplier<D> worldloader_worlddatasupplier, WorldLoader.ResultFactory<D, R> worldloader_resultfactory) throws Exception {
      WorldLoader.InitConfig worldloader_initconfig = new WorldLoader.InitConfig(worldloader_packconfig, Commands.CommandSelection.INTEGRATED, 2);
      CompletableFuture<R> completablefuture = WorldLoader.load(worldloader_initconfig, worldloader_worlddatasupplier, worldloader_resultfactory, Util.backgroundExecutor(), this.minecraft);
      this.minecraft.managedBlock(completablefuture::isDone);
      return completablefuture.get();
   }

   private void doLoadLevel(Screen screen, String s, boolean flag, boolean flag1) {
      LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.createWorldAccess(s);
      if (levelstoragesource_levelstorageaccess != null) {
         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess);

         WorldStem worldstem;
         try {
            worldstem = this.loadWorldStem(levelstoragesource_levelstorageaccess, flag, packrepository);
         } catch (Exception var11) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)var11);
            if (!flag) {
               this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(screen, s, true, flag1)));
            } else {
               this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen((Screen)null), Component.translatable("datapackFailure.safeMode.failed.title"), Component.translatable("datapackFailure.safeMode.failed.description"), CommonComponents.GUI_TO_TITLE, true));
            }

            safeCloseAccess(levelstoragesource_levelstorageaccess, s);
            return;
         }

         WorldData worlddata = worldstem.worldData();
         boolean flag2 = worlddata.worldGenOptions().isOldCustomizedWorld();
         boolean flag3 = worlddata.worldGenSettingsLifecycle() != Lifecycle.stable();
         if (!flag1 || !flag2 && !flag3) {
            this.minecraft.getDownloadedPackSource().loadBundledResourcePack(levelstoragesource_levelstorageaccess).thenApply((ovoid) -> true).exceptionallyComposeAsync((throwable1) -> {
               LOGGER.warn("Failed to load pack: ", throwable1);
               return this.promptBundledPackLoadFailure();
            }, this.minecraft).thenAcceptAsync((obool) -> {
               if (obool) {
                  this.minecraft.doWorldLoad(s, levelstoragesource_levelstorageaccess, packrepository, worldstem, false);
               } else {
                  worldstem.close();
                  safeCloseAccess(levelstoragesource_levelstorageaccess, s);
                  this.minecraft.getDownloadedPackSource().clearServerPack().thenRunAsync(() -> this.minecraft.setScreen(screen), this.minecraft);
               }

            }, this.minecraft).exceptionally((throwable) -> {
               this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Load world"));
               return null;
            });
         } else {
            this.askForBackup(screen, s, flag2, () -> this.doLoadLevel(screen, s, flag, false));
            worldstem.close();
            safeCloseAccess(levelstoragesource_levelstorageaccess, s);
         }
      }
   }

   private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
      CompletableFuture<Boolean> completablefuture = new CompletableFuture<>();
      this.minecraft.setScreen(new ConfirmScreen(completablefuture::complete, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
      return completablefuture;
   }

   private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, String s) {
      try {
         levelstoragesource_levelstorageaccess.close();
      } catch (IOException var3) {
         LOGGER.warn("Failed to unlock access to level {}", s, var3);
      }

   }

   private void askForBackup(Screen screen, String s, boolean flag, Runnable runnable) {
      Component component;
      Component component1;
      if (flag) {
         component = Component.translatable("selectWorld.backupQuestion.customized");
         component1 = Component.translatable("selectWorld.backupWarning.customized");
      } else {
         component = Component.translatable("selectWorld.backupQuestion.experimental");
         component1 = Component.translatable("selectWorld.backupWarning.experimental");
      }

      this.minecraft.setScreen(new BackupConfirmScreen(screen, (flag1, flag2) -> {
         if (flag1) {
            EditWorldScreen.makeBackupAndShowToast(this.levelSource, s);
         }

         runnable.run();
      }, component, component1, false));
   }

   public static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createworldscreen, Lifecycle lifecycle, Runnable runnable, boolean flag) {
      BooleanConsumer booleanconsumer = (flag1) -> {
         if (flag1) {
            runnable.run();
         } else {
            minecraft.setScreen(createworldscreen);
         }

      };
      if (!flag && lifecycle != Lifecycle.stable()) {
         if (lifecycle == Lifecycle.experimental()) {
            minecraft.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")));
         } else {
            minecraft.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")));
         }
      } else {
         runnable.run();
      }

   }
}
