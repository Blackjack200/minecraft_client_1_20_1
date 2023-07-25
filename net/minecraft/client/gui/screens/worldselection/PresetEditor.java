package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public interface PresetEditor {
   Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(Optional.of(WorldPresets.FLAT), (createworldscreen, worldcreationcontext) -> {
      ChunkGenerator chunkgenerator = worldcreationcontext.selectedDimensions().overworld();
      RegistryAccess registryaccess = worldcreationcontext.worldgenLoadContext();
      HolderGetter<Biome> holdergetter = registryaccess.lookupOrThrow(Registries.BIOME);
      HolderGetter<StructureSet> holdergetter1 = registryaccess.lookupOrThrow(Registries.STRUCTURE_SET);
      HolderGetter<PlacedFeature> holdergetter2 = registryaccess.lookupOrThrow(Registries.PLACED_FEATURE);
      return new CreateFlatWorldScreen(createworldscreen, (flatlevelgeneratorsettings) -> createworldscreen.getUiState().updateDimensions(flatWorldConfigurator(flatlevelgeneratorsettings)), chunkgenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkgenerator).settings() : FlatLevelGeneratorSettings.getDefault(holdergetter, holdergetter1, holdergetter2));
   }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (createworldscreen, worldcreationcontext) -> new CreateBuffetWorldScreen(createworldscreen, worldcreationcontext, (holder) -> createworldscreen.getUiState().updateDimensions(fixedBiomeConfigurator(holder))));

   Screen createEditScreen(CreateWorldScreen createworldscreen, WorldCreationContext worldcreationcontext);

   private static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      return (registryaccess_frozen, worlddimensions) -> {
         ChunkGenerator chunkgenerator = new FlatLevelSource(flatlevelgeneratorsettings);
         return worlddimensions.replaceOverworldGenerator(registryaccess_frozen, chunkgenerator);
      };
   }

   private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> holder) {
      return (registryaccess_frozen, worlddimensions) -> {
         Registry<NoiseGeneratorSettings> registry = registryaccess_frozen.registryOrThrow(Registries.NOISE_SETTINGS);
         Holder<NoiseGeneratorSettings> holder2 = registry.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
         BiomeSource biomesource = new FixedBiomeSource(holder);
         ChunkGenerator chunkgenerator = new NoiseBasedChunkGenerator(biomesource, holder2);
         return worlddimensions.replaceOverworldGenerator(registryaccess_frozen, chunkgenerator);
      };
   }
}
