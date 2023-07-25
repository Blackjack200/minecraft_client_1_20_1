package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Registry<LevelStem> dimensions) {
   public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryCodecs.fullCodec(Registries.LEVEL_STEM, Lifecycle.stable(), LevelStem.CODEC).fieldOf("dimensions").forGetter(WorldDimensions::dimensions)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(WorldDimensions::new)));
   private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
   private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

   public WorldDimensions {
      LevelStem levelstem = registry.get(LevelStem.OVERWORLD);
      if (levelstem == null) {
         throw new IllegalStateException("Overworld settings missing");
      }
   }

   public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> stream) {
      return Stream.concat(BUILTIN_ORDER.stream(), stream.filter((resourcekey) -> !BUILTIN_ORDER.contains(resourcekey)));
   }

   public WorldDimensions replaceOverworldGenerator(RegistryAccess registryaccess, ChunkGenerator chunkgenerator) {
      Registry<DimensionType> registry = registryaccess.registryOrThrow(Registries.DIMENSION_TYPE);
      Registry<LevelStem> registry1 = withOverworld(registry, this.dimensions, chunkgenerator);
      return new WorldDimensions(registry1);
   }

   public static Registry<LevelStem> withOverworld(Registry<DimensionType> registry, Registry<LevelStem> registry1, ChunkGenerator chunkgenerator) {
      LevelStem levelstem = registry1.get(LevelStem.OVERWORLD);
      Holder<DimensionType> holder = (Holder<DimensionType>)(levelstem == null ? registry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelstem.type());
      return withOverworld(registry1, holder, chunkgenerator);
   }

   public static Registry<LevelStem> withOverworld(Registry<LevelStem> registry, Holder<DimensionType> holder, ChunkGenerator chunkgenerator) {
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
      writableregistry.register(LevelStem.OVERWORLD, new LevelStem(holder, chunkgenerator), Lifecycle.stable());

      for(Map.Entry<ResourceKey<LevelStem>, LevelStem> map_entry : registry.entrySet()) {
         ResourceKey<LevelStem> resourcekey = map_entry.getKey();
         if (resourcekey != LevelStem.OVERWORLD) {
            writableregistry.register(resourcekey, map_entry.getValue(), registry.lifecycle(map_entry.getValue()));
         }
      }

      return writableregistry.freeze();
   }

   public ChunkGenerator overworld() {
      LevelStem levelstem = this.dimensions.get(LevelStem.OVERWORLD);
      if (levelstem == null) {
         throw new IllegalStateException("Overworld settings missing");
      } else {
         return levelstem.generator();
      }
   }

   public Optional<LevelStem> get(ResourceKey<LevelStem> resourcekey) {
      return this.dimensions.getOptional(resourcekey);
   }

   public ImmutableSet<ResourceKey<Level>> levels() {
      return this.dimensions().entrySet().stream().map(Map.Entry::getKey).map(Registries::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
   }

   public boolean isDebug() {
      return this.overworld() instanceof DebugLevelSource;
   }

   private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> registry) {
      return registry.getOptional(LevelStem.OVERWORLD).map((levelstem) -> {
         ChunkGenerator chunkgenerator = levelstem.generator();
         if (chunkgenerator instanceof DebugLevelSource) {
            return PrimaryLevelData.SpecialWorldProperty.DEBUG;
         } else {
            return chunkgenerator instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
         }
      }).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
   }

   static Lifecycle checkStability(ResourceKey<LevelStem> resourcekey, LevelStem levelstem) {
      return isVanillaLike(resourcekey, levelstem) ? Lifecycle.stable() : Lifecycle.experimental();
   }

   private static boolean isVanillaLike(ResourceKey<LevelStem> resourcekey, LevelStem levelstem) {
      if (resourcekey == LevelStem.OVERWORLD) {
         return isStableOverworld(levelstem);
      } else if (resourcekey == LevelStem.NETHER) {
         return isStableNether(levelstem);
      } else {
         return resourcekey == LevelStem.END ? isStableEnd(levelstem) : false;
      }
   }

   private static boolean isStableOverworld(LevelStem levelstem) {
      Holder<DimensionType> holder = levelstem.type();
      if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
         return false;
      } else {
         BiomeSource var3 = levelstem.generator().getBiomeSource();
         if (var3 instanceof MultiNoiseBiomeSource) {
            MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)var3;
            if (!multinoisebiomesource.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isStableNether(LevelStem levelstem) {
      if (levelstem.type().is(BuiltinDimensionTypes.NETHER)) {
         ChunkGenerator var3 = levelstem.generator();
         if (var3 instanceof NoiseBasedChunkGenerator) {
            NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)var3;
            if (noisebasedchunkgenerator.stable(NoiseGeneratorSettings.NETHER)) {
               BiomeSource var4 = noisebasedchunkgenerator.getBiomeSource();
               if (var4 instanceof MultiNoiseBiomeSource) {
                  MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)var4;
                  if (multinoisebiomesource.stable(MultiNoiseBiomeSourceParameterLists.NETHER)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isStableEnd(LevelStem levelstem) {
      if (levelstem.type().is(BuiltinDimensionTypes.END)) {
         ChunkGenerator var2 = levelstem.generator();
         if (var2 instanceof NoiseBasedChunkGenerator) {
            NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)var2;
            if (noisebasedchunkgenerator.stable(NoiseGeneratorSettings.END) && noisebasedchunkgenerator.getBiomeSource() instanceof TheEndBiomeSource) {
               return true;
            }
         }
      }

      return false;
   }

   public WorldDimensions.Complete bake(Registry<LevelStem> registry) {
      Stream<ResourceKey<LevelStem>> stream = Stream.concat(registry.registryKeySet().stream(), this.dimensions.registryKeySet().stream()).distinct();
      List<Entry> list = new ArrayList<>();
      keysInOrder(stream).forEach((resourcekey) -> registry.getOptional(resourcekey).or(() -> this.dimensions.getOptional(resourcekey)).ifPresent((levelstem) -> {
            record Entry(ResourceKey<LevelStem> key, LevelStem value) {
               final ResourceKey<LevelStem> key;
               final LevelStem value;

               Lifecycle lifecycle() {
                  return WorldDimensions.checkStability(this.key, this.value);
               }
            }

            list.add(new Entry(resourcekey, levelstem));
         }));
      Lifecycle lifecycle = list.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registries.LEVEL_STEM, lifecycle);
      list.forEach((worlddimensions_1entry) -> writableregistry.register(worlddimensions_1entry.key, worlddimensions_1entry.value, worlddimensions_1entry.lifecycle()));
      Registry<LevelStem> registry1 = writableregistry.freeze();
      PrimaryLevelData.SpecialWorldProperty primaryleveldata_specialworldproperty = specialWorldProperty(registry1);
      return new WorldDimensions.Complete(registry1.freeze(), primaryleveldata_specialworldproperty);
   }

   public static record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
      public Lifecycle lifecycle() {
         return this.dimensions.registryLifecycle();
      }

      public RegistryAccess.Frozen dimensionsRegistryAccess() {
         return (new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions))).freeze();
      }
   }
}
