package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = List.of(new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.CODEC), new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE, Structure.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryDataLoader.RegistryData<>(Registries.DAMAGE_TYPE, DamageType.CODEC), new RegistryDataLoader.RegistryData<>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC));
   public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(new RegistryDataLoader.RegistryData<>(Registries.LEVEL_STEM, LevelStem.CODEC));

   public static RegistryAccess.Frozen load(ResourceManager resourcemanager, RegistryAccess registryaccess, List<RegistryDataLoader.RegistryData<?>> list) {
      Map<ResourceKey<?>, Exception> map = new HashMap<>();
      List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> list1 = list.stream().map((registrydataloader_registrydata) -> registrydataloader_registrydata.create(Lifecycle.stable(), map)).toList();
      RegistryOps.RegistryInfoLookup registryops_registryinfolookup = createContext(registryaccess, list1);
      list1.forEach((pair1) -> pair1.getSecond().load(resourcemanager, registryops_registryinfolookup));
      list1.forEach((pair) -> {
         Registry<?> registry = pair.getFirst();

         try {
            registry.freeze();
         } catch (Exception var4) {
            map.put(registry.key(), var4);
         }

      });
      if (!map.isEmpty()) {
         logErrors(map);
         throw new IllegalStateException("Failed to load registries due to above errors");
      } else {
         return (new RegistryAccess.ImmutableRegistryAccess(list1.stream().map(Pair::getFirst).toList())).freeze();
      }
   }

   private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryaccess, List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> list) {
      final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
      registryaccess.registries().forEach((registryaccess_registryentry) -> map.put(registryaccess_registryentry.key(), createInfoForContextRegistry(registryaccess_registryentry.value())));
      list.forEach((pair) -> map.put(pair.getFirst().key(), createInfoForNewRegistry(pair.getFirst())));
      return new RegistryOps.RegistryInfoLookup() {
         public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
            return Optional.ofNullable(map.get(resourcekey));
         }
      };
   }

   private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableregistry) {
      return new RegistryOps.RegistryInfo<>(writableregistry.asLookup(), writableregistry.createRegistrationLookup(), writableregistry.registryLifecycle());
   }

   private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
      return new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
   }

   private static void logErrors(Map<ResourceKey<?>, Exception> map) {
      StringWriter stringwriter = new StringWriter();
      PrintWriter printwriter = new PrintWriter(stringwriter);
      Map<ResourceLocation, Map<ResourceLocation, Exception>> map1 = map.entrySet().stream().collect(Collectors.groupingBy((map_entry3) -> map_entry3.getKey().registry(), Collectors.toMap((map_entry2) -> map_entry2.getKey().location(), Map.Entry::getValue)));
      map1.entrySet().stream().sorted(Entry.comparingByKey()).forEach((map_entry) -> {
         printwriter.printf("> Errors in registry %s:%n", map_entry.getKey());
         map_entry.getValue().entrySet().stream().sorted(Entry.comparingByKey()).forEach((map_entry1) -> {
            printwriter.printf(">> Errors in element %s:%n", map_entry1.getKey());
            map_entry1.getValue().printStackTrace(printwriter);
         });
      });
      printwriter.flush();
      LOGGER.error("Registry loading errors:\n{}", (Object)stringwriter);
   }

   private static String registryDirPath(ResourceLocation resourcelocation) {
      return resourcelocation.getPath();
   }

   static <E> void loadRegistryContents(RegistryOps.RegistryInfoLookup registryops_registryinfolookup, ResourceManager resourcemanager, ResourceKey<? extends Registry<E>> resourcekey, WritableRegistry<E> writableregistry, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map) {
      String s = registryDirPath(resourcekey.location());
      FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
      RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, registryops_registryinfolookup);

      for(Map.Entry<ResourceLocation, Resource> map_entry : filetoidconverter.listMatchingResources(resourcemanager).entrySet()) {
         ResourceLocation resourcelocation = map_entry.getKey();
         ResourceKey<E> resourcekey1 = ResourceKey.create(resourcekey, filetoidconverter.fileToId(resourcelocation));
         Resource resource = map_entry.getValue();

         try {
            Reader reader = resource.openAsReader();

            try {
               JsonElement jsonelement = JsonParser.parseReader(reader);
               DataResult<E> dataresult = decoder.parse(registryops, jsonelement);
               E object = dataresult.getOrThrow(false, (s1) -> {
               });
               writableregistry.register(resourcekey1, object, resource.isBuiltin() ? Lifecycle.stable() : dataresult.lifecycle());
            } catch (Throwable var19) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (Exception var20) {
            map.put(resourcekey1, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourcelocation, resource.sourcePackId()), var20));
         }
      }

   }

   interface Loader {
      void load(ResourceManager resourcemanager, RegistryOps.RegistryInfoLookup registryops_registryinfolookup);
   }

   public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
      Pair<WritableRegistry<?>, RegistryDataLoader.Loader> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
         WritableRegistry<T> writableregistry = new MappedRegistry<>(this.key, lifecycle);
         RegistryDataLoader.Loader registrydataloader_loader = (resourcemanager, registryops_registryinfolookup) -> RegistryDataLoader.loadRegistryContents(registryops_registryinfolookup, resourcemanager, this.key, writableregistry, this.elementCodec, map);
         return Pair.of(writableregistry, registrydataloader_loader);
      }
   }
}
