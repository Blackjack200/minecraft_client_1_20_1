package net.minecraft.client.gui.screens.worldselection;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;

public record WorldCreationContext(WorldOptions options, Registry<LevelStem> datapackDimensions, WorldDimensions selectedDimensions, LayeredRegistryAccess<RegistryLayer> worldgenRegistries, ReloadableServerResources dataPackResources, WorldDataConfiguration dataConfiguration) {
   public WorldCreationContext(WorldGenSettings worldgensettings, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, ReloadableServerResources reloadableserverresources, WorldDataConfiguration worlddataconfiguration) {
      this(worldgensettings.options(), worldgensettings.dimensions(), layeredregistryaccess, reloadableserverresources, worlddataconfiguration);
   }

   public WorldCreationContext(WorldOptions worldoptions, WorldDimensions worlddimensions, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, ReloadableServerResources reloadableserverresources, WorldDataConfiguration worlddataconfiguration) {
      this(worldoptions, layeredregistryaccess.getLayer(RegistryLayer.DIMENSIONS).registryOrThrow(Registries.LEVEL_STEM), worlddimensions, layeredregistryaccess.replaceFrom(RegistryLayer.DIMENSIONS), reloadableserverresources, worlddataconfiguration);
   }

   public WorldCreationContext withSettings(WorldOptions worldoptions, WorldDimensions worlddimensions) {
      return new WorldCreationContext(worldoptions, this.datapackDimensions, worlddimensions, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration);
   }

   public WorldCreationContext withOptions(WorldCreationContext.OptionsModifier worldcreationcontext_optionsmodifier) {
      return new WorldCreationContext(worldcreationcontext_optionsmodifier.apply(this.options), this.datapackDimensions, this.selectedDimensions, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration);
   }

   public WorldCreationContext withDimensions(WorldCreationContext.DimensionsUpdater worldcreationcontext_dimensionsupdater) {
      return new WorldCreationContext(this.options, this.datapackDimensions, worldcreationcontext_dimensionsupdater.apply(this.worldgenLoadContext(), this.selectedDimensions), this.worldgenRegistries, this.dataPackResources, this.dataConfiguration);
   }

   public RegistryAccess.Frozen worldgenLoadContext() {
      return this.worldgenRegistries.compositeAccess();
   }

   @FunctionalInterface
   public interface DimensionsUpdater extends BiFunction<RegistryAccess.Frozen, WorldDimensions, WorldDimensions> {
   }

   public interface OptionsModifier extends UnaryOperator<WorldOptions> {
   }
}
