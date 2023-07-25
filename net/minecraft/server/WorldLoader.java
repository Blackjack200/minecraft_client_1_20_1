package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static <D, R> CompletableFuture<R> load(WorldLoader.InitConfig worldloader_initconfig, WorldLoader.WorldDataSupplier<D> worldloader_worlddatasupplier, WorldLoader.ResultFactory<D, R> worldloader_resultfactory, Executor executor, Executor executor1) {
      try {
         Pair<WorldDataConfiguration, CloseableResourceManager> pair = worldloader_initconfig.packConfig.createResourceManager();
         CloseableResourceManager closeableresourcemanager = pair.getSecond();
         LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = RegistryLayer.createRegistryAccess();
         LayeredRegistryAccess<RegistryLayer> layeredregistryaccess1 = loadAndReplaceLayer(closeableresourcemanager, layeredregistryaccess, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES);
         RegistryAccess.Frozen registryaccess_frozen = layeredregistryaccess1.getAccessForLoading(RegistryLayer.DIMENSIONS);
         RegistryAccess.Frozen registryaccess_frozen1 = RegistryDataLoader.load(closeableresourcemanager, registryaccess_frozen, RegistryDataLoader.DIMENSION_REGISTRIES);
         WorldDataConfiguration worlddataconfiguration = pair.getFirst();
         WorldLoader.DataLoadOutput<D> worldloader_dataloadoutput = worldloader_worlddatasupplier.get(new WorldLoader.DataLoadContext(closeableresourcemanager, worlddataconfiguration, registryaccess_frozen, registryaccess_frozen1));
         LayeredRegistryAccess<RegistryLayer> layeredregistryaccess2 = layeredregistryaccess1.replaceFrom(RegistryLayer.DIMENSIONS, worldloader_dataloadoutput.finalDimensions);
         RegistryAccess.Frozen registryaccess_frozen2 = layeredregistryaccess2.getAccessForLoading(RegistryLayer.RELOADABLE);
         return ReloadableServerResources.loadResources(closeableresourcemanager, registryaccess_frozen2, worlddataconfiguration.enabledFeatures(), worldloader_initconfig.commandSelection(), worldloader_initconfig.functionCompilationLevel(), executor, executor1).whenComplete((reloadableserverresources1, throwable) -> {
            if (throwable != null) {
               closeableresourcemanager.close();
            }

         }).thenApplyAsync((reloadableserverresources) -> {
            reloadableserverresources.updateRegistryTags(registryaccess_frozen2);
            return worldloader_resultfactory.create(closeableresourcemanager, reloadableserverresources, layeredregistryaccess2, worldloader_dataloadoutput.cookie);
         }, executor1);
      } catch (Exception var15) {
         return CompletableFuture.failedFuture(var15);
      }
   }

   private static RegistryAccess.Frozen loadLayer(ResourceManager resourcemanager, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, RegistryLayer registrylayer, List<RegistryDataLoader.RegistryData<?>> list) {
      RegistryAccess.Frozen registryaccess_frozen = layeredregistryaccess.getAccessForLoading(registrylayer);
      return RegistryDataLoader.load(resourcemanager, registryaccess_frozen, list);
   }

   private static LayeredRegistryAccess<RegistryLayer> loadAndReplaceLayer(ResourceManager resourcemanager, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, RegistryLayer registrylayer, List<RegistryDataLoader.RegistryData<?>> list) {
      RegistryAccess.Frozen registryaccess_frozen = loadLayer(resourcemanager, layeredregistryaccess, registrylayer, list);
      return layeredregistryaccess.replaceFrom(registrylayer, registryaccess_frozen);
   }

   public static record DataLoadContext(ResourceManager resources, WorldDataConfiguration dataConfiguration, RegistryAccess.Frozen datapackWorldgen, RegistryAccess.Frozen datapackDimensions) {
   }

   public static record DataLoadOutput<D>(D cookie, RegistryAccess.Frozen finalDimensions) {
      final D cookie;
      final RegistryAccess.Frozen finalDimensions;
   }

   public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
      final WorldLoader.PackConfig packConfig;
   }

   public static record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
      public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
         FeatureFlagSet featureflagset = this.initMode ? FeatureFlags.REGISTRY.allFlags() : this.initialDataConfig.enabledFeatures();
         WorldDataConfiguration worlddataconfiguration = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig.dataPacks(), this.safeMode, featureflagset);
         if (!this.initMode) {
            worlddataconfiguration = worlddataconfiguration.expandFeatures(this.initialDataConfig.enabledFeatures());
         }

         List<PackResources> list = this.packRepository.openAllSelected();
         CloseableResourceManager closeableresourcemanager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
         return Pair.of(worlddataconfiguration, closeableresourcemanager);
      }
   }

   @FunctionalInterface
   public interface ResultFactory<D, R> {
      R create(CloseableResourceManager closeableresourcemanager, ReloadableServerResources reloadableserverresources, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, D object);
   }

   @FunctionalInterface
   public interface WorldDataSupplier<D> {
      WorldLoader.DataLoadOutput<D> get(WorldLoader.DataLoadContext worldloader_dataloadcontext);
   }
}
