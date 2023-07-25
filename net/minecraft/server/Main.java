package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main {
   private static final Logger LOGGER = LogUtils.getLogger();

   @DontObfuscate
   public static void main(String[] astring) {
      SharedConstants.tryDetectVersion();
      OptionParser optionparser = new OptionParser();
      OptionSpec<Void> optionspec = optionparser.accepts("nogui");
      OptionSpec<Void> optionspec1 = optionparser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
      OptionSpec<Void> optionspec2 = optionparser.accepts("demo");
      OptionSpec<Void> optionspec3 = optionparser.accepts("bonusChest");
      OptionSpec<Void> optionspec4 = optionparser.accepts("forceUpgrade");
      OptionSpec<Void> optionspec5 = optionparser.accepts("eraseCache");
      OptionSpec<Void> optionspec6 = optionparser.accepts("safeMode", "Loads level with vanilla datapack only");
      OptionSpec<Void> optionspec7 = optionparser.accepts("help").forHelp();
      OptionSpec<String> optionspec8 = optionparser.accepts("singleplayer").withRequiredArg();
      OptionSpec<String> optionspec9 = optionparser.accepts("universe").withRequiredArg().defaultsTo(".");
      OptionSpec<String> optionspec10 = optionparser.accepts("world").withRequiredArg();
      OptionSpec<Integer> optionspec11 = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
      OptionSpec<String> optionspec12 = optionparser.accepts("serverId").withRequiredArg();
      OptionSpec<Void> optionspec13 = optionparser.accepts("jfrProfile");
      OptionSpec<Path> optionspec14 = optionparser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter());
      OptionSpec<String> optionspec15 = optionparser.nonOptions();

      try {
         OptionSet optionset = optionparser.parse(astring);
         if (optionset.has(optionspec7)) {
            optionparser.printHelpOn(System.err);
            return;
         }

         Path path = optionset.valueOf(optionspec14);
         if (path != null) {
            writePidFile(path);
         }

         CrashReport.preload();
         if (optionset.has(optionspec13)) {
            JvmProfiler.INSTANCE.start(Environment.SERVER);
         }

         Bootstrap.bootStrap();
         Bootstrap.validate();
         Util.startTimerHackThread();
         Path path1 = Paths.get("server.properties");
         DedicatedServerSettings dedicatedserversettings = new DedicatedServerSettings(path1);
         dedicatedserversettings.forceSave();
         Path path2 = Paths.get("eula.txt");
         Eula eula = new Eula(path2);
         if (optionset.has(optionspec1)) {
            LOGGER.info("Initialized '{}' and '{}'", path1.toAbsolutePath(), path2.toAbsolutePath());
            return;
         }

         if (!eula.hasAgreedToEULA()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
         }

         File file = new File(optionset.valueOf(optionspec9));
         Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
         String s = Optional.ofNullable(optionset.valueOf(optionspec10)).orElse(dedicatedserversettings.getProperties().levelName);
         LevelStorageSource levelstoragesource = LevelStorageSource.createDefault(file.toPath());
         LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = levelstoragesource.validateAndCreateAccess(s);
         LevelSummary levelsummary = levelstoragesource_levelstorageaccess.getSummary();
         if (levelsummary != null) {
            if (levelsummary.requiresManualConversion()) {
               LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
               return;
            }

            if (!levelsummary.isCompatible()) {
               LOGGER.info("This world was created by an incompatible version.");
               return;
            }
         }

         boolean flag = optionset.has(optionspec6);
         if (flag) {
            LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
         }

         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource_levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR));

         WorldStem worldstem;
         try {
            WorldLoader.InitConfig worldloader_initconfig = loadOrCreateConfig(dedicatedserversettings.getProperties(), levelstoragesource_levelstorageaccess, flag, packrepository);
            worldstem = Util.blockUntilDone((executor) -> WorldLoader.load(worldloader_initconfig, (worldloader_dataloadcontext) -> {
                  Registry<LevelStem> registry = worldloader_dataloadcontext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
                  DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_dataloadcontext.datapackWorldgen());
                  Pair<WorldData, WorldDimensions.Complete> pair = levelstoragesource_levelstorageaccess.getDataTag(dynamicops, worldloader_dataloadcontext.dataConfiguration(), registry, worldloader_dataloadcontext.datapackWorldgen().allRegistriesLifecycle());
                  if (pair != null) {
                     return new WorldLoader.DataLoadOutput<>(pair.getFirst(), pair.getSecond().dimensionsRegistryAccess());
                  } else {
                     LevelSettings levelsettings;
                     WorldOptions worldoptions;
                     WorldDimensions worlddimensions;
                     if (optionset.has(optionspec2)) {
                        levelsettings = MinecraftServer.DEMO_SETTINGS;
                        worldoptions = WorldOptions.DEMO_OPTIONS;
                        worlddimensions = WorldPresets.createNormalWorldDimensions(worldloader_dataloadcontext.datapackWorldgen());
                     } else {
                        DedicatedServerProperties dedicatedserverproperties = dedicatedserversettings.getProperties();
                        levelsettings = new LevelSettings(dedicatedserverproperties.levelName, dedicatedserverproperties.gamemode, dedicatedserverproperties.hardcore, dedicatedserverproperties.difficulty, false, new GameRules(), worldloader_dataloadcontext.dataConfiguration());
                        worldoptions = optionset.has(optionspec3) ? dedicatedserverproperties.worldOptions.withBonusChest(true) : dedicatedserverproperties.worldOptions;
                        worlddimensions = dedicatedserverproperties.createDimensions(worldloader_dataloadcontext.datapackWorldgen());
                     }

                     WorldDimensions.Complete worlddimensions_complete = worlddimensions.bake(registry);
                     Lifecycle lifecycle = worlddimensions_complete.lifecycle().add(worldloader_dataloadcontext.datapackWorldgen().allRegistriesLifecycle());
                     return new WorldLoader.DataLoadOutput<>(new PrimaryLevelData(levelsettings, worldoptions, worlddimensions_complete.specialWorldProperty(), lifecycle), worlddimensions_complete.dimensionsRegistryAccess());
                  }
               }, WorldStem::new, Util.backgroundExecutor(), executor)).get();
         } catch (Exception var37) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)var37);
            return;
         }

         RegistryAccess.Frozen registryaccess_frozen = worldstem.registries().compositeAccess();
         if (optionset.has(optionspec4)) {
            forceUpgrade(levelstoragesource_levelstorageaccess, DataFixers.getDataFixer(), optionset.has(optionspec5), () -> true, registryaccess_frozen.registryOrThrow(Registries.LEVEL_STEM));
         }

         WorldData worlddata = worldstem.worldData();
         levelstoragesource_levelstorageaccess.saveDataTag(registryaccess_frozen, worlddata);
         final DedicatedServer dedicatedserver = MinecraftServer.spin((thread1) -> {
            DedicatedServer dedicatedserver1 = new DedicatedServer(thread1, levelstoragesource_levelstorageaccess, packrepository, worldstem, dedicatedserversettings, DataFixers.getDataFixer(), services, LoggerChunkProgressListener::new);
            dedicatedserver1.setSingleplayerProfile(optionset.has(optionspec8) ? new GameProfile((UUID)null, optionset.valueOf(optionspec8)) : null);
            dedicatedserver1.setPort(optionset.valueOf(optionspec11));
            dedicatedserver1.setDemo(optionset.has(optionspec2));
            dedicatedserver1.setId(optionset.valueOf(optionspec12));
            boolean flag1 = !optionset.has(optionspec) && !optionset.valuesOf(optionspec15).contains("nogui");
            if (flag1 && !GraphicsEnvironment.isHeadless()) {
               dedicatedserver1.showGui();
            }

            return dedicatedserver1;
         });
         Thread thread = new Thread("Server Shutdown Thread") {
            public void run() {
               dedicatedserver.halt(true);
            }
         };
         thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
         Runtime.getRuntime().addShutdownHook(thread);
      } catch (Exception var38) {
         LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var38);
      }

   }

   private static void writePidFile(Path path) {
      try {
         long i = ProcessHandle.current().pid();
         Files.writeString(path, Long.toString(i));
      } catch (IOException var3) {
         throw new UncheckedIOException(var3);
      }
   }

   private static WorldLoader.InitConfig loadOrCreateConfig(DedicatedServerProperties dedicatedserverproperties, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, boolean flag, PackRepository packrepository) {
      WorldDataConfiguration worlddataconfiguration = levelstoragesource_levelstorageaccess.getDataConfiguration();
      WorldDataConfiguration worlddataconfiguration1;
      boolean flag1;
      if (worlddataconfiguration != null) {
         flag1 = false;
         worlddataconfiguration1 = worlddataconfiguration;
      } else {
         flag1 = true;
         worlddataconfiguration1 = new WorldDataConfiguration(dedicatedserverproperties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
      }

      WorldLoader.PackConfig worldloader_packconfig = new WorldLoader.PackConfig(packrepository, worlddataconfiguration1, flag, flag1);
      return new WorldLoader.InitConfig(worldloader_packconfig, Commands.CommandSelection.DEDICATED, dedicatedserverproperties.functionPermissionLevel);
   }

   private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer, boolean flag, BooleanSupplier booleansupplier, Registry<LevelStem> registry) {
      LOGGER.info("Forcing world upgrade!");
      WorldUpgrader worldupgrader = new WorldUpgrader(levelstoragesource_levelstorageaccess, datafixer, registry, flag);
      Component component = null;

      while(!worldupgrader.isFinished()) {
         Component component1 = worldupgrader.getStatus();
         if (component != component1) {
            component = component1;
            LOGGER.info(worldupgrader.getStatus().getString());
         }

         int i = worldupgrader.getTotalChunks();
         if (i > 0) {
            int j = worldupgrader.getConverted() + worldupgrader.getSkipped();
            LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)j / (float)i * 100.0F), j, i);
         }

         if (!booleansupplier.getAsBoolean()) {
            worldupgrader.cancel();
         } else {
            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var10) {
            }
         }
      }

   }
}
