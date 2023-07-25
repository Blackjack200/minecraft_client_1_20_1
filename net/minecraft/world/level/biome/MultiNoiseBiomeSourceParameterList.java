package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MultiNoiseBiomeSourceParameterList {
   public static final Codec<MultiNoiseBiomeSourceParameterList> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(MultiNoiseBiomeSourceParameterList.Preset.CODEC.fieldOf("preset").forGetter((multinoisebiomesourceparameterlist) -> multinoisebiomesourceparameterlist.preset), RegistryOps.retrieveGetter(Registries.BIOME)).apply(recordcodecbuilder_instance, MultiNoiseBiomeSourceParameterList::new));
   public static final Codec<Holder<MultiNoiseBiomeSourceParameterList>> CODEC = RegistryFileCodec.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, DIRECT_CODEC);
   private final MultiNoiseBiomeSourceParameterList.Preset preset;
   private final Climate.ParameterList<Holder<Biome>> parameters;

   public MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset multinoisebiomesourceparameterlist_preset, HolderGetter<Biome> holdergetter) {
      this.preset = multinoisebiomesourceparameterlist_preset;
      this.parameters = multinoisebiomesourceparameterlist_preset.provider.apply(holdergetter::getOrThrow);
   }

   public Climate.ParameterList<Holder<Biome>> parameters() {
      return this.parameters;
   }

   public static Map<MultiNoiseBiomeSourceParameterList.Preset, Climate.ParameterList<ResourceKey<Biome>>> knownPresets() {
      return MultiNoiseBiomeSourceParameterList.Preset.BY_NAME.values().stream().collect(Collectors.toMap((multinoisebiomesourceparameterlist_preset1) -> multinoisebiomesourceparameterlist_preset1, (multinoisebiomesourceparameterlist_preset) -> multinoisebiomesourceparameterlist_preset.provider().apply((resourcekey) -> resourcekey)));
   }

   public static record Preset(ResourceLocation id, MultiNoiseBiomeSourceParameterList.Preset.SourceProvider provider) {
      final MultiNoiseBiomeSourceParameterList.Preset.SourceProvider provider;
      public static final MultiNoiseBiomeSourceParameterList.Preset NETHER = new MultiNoiseBiomeSourceParameterList.Preset(new ResourceLocation("nether"), new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
         public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
            return new Climate.ParameterList<>(List.of(Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.NETHER_WASTES)), Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.SOUL_SAND_VALLEY)), Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.CRIMSON_FOREST)), Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), function.apply(Biomes.WARPED_FOREST)), Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), function.apply(Biomes.BASALT_DELTAS))));
         }
      });
      public static final MultiNoiseBiomeSourceParameterList.Preset OVERWORLD = new MultiNoiseBiomeSourceParameterList.Preset(new ResourceLocation("overworld"), new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
         public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
            return MultiNoiseBiomeSourceParameterList.Preset.generateOverworldBiomes(function);
         }
      });
      static final Map<ResourceLocation, MultiNoiseBiomeSourceParameterList.Preset> BY_NAME = Stream.of(NETHER, OVERWORLD).collect(Collectors.toMap(MultiNoiseBiomeSourceParameterList.Preset::id, (multinoisebiomesourceparameterlist_preset) -> multinoisebiomesourceparameterlist_preset));
      public static final Codec<MultiNoiseBiomeSourceParameterList.Preset> CODEC = ResourceLocation.CODEC.flatXmap((resourcelocation) -> Optional.ofNullable(BY_NAME.get(resourcelocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown preset: " + resourcelocation)), (multinoisebiomesourceparameterlist_preset) -> DataResult.success(multinoisebiomesourceparameterlist_preset.id));

      static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> function) {
         ImmutableList.Builder<Pair<Climate.ParameterPoint, T>> immutablelist_builder = ImmutableList.builder();
         (new OverworldBiomeBuilder()).addBiomes((pair) -> immutablelist_builder.add(pair.mapSecond(function)));
         return new Climate.ParameterList<>(immutablelist_builder.build());
      }

      public Stream<ResourceKey<Biome>> usedBiomes() {
         return this.provider.apply((resourcekey) -> resourcekey).values().stream().map(Pair::getSecond).distinct();
      }

      @FunctionalInterface
      interface SourceProvider {
         <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function);
      }
   }
}
