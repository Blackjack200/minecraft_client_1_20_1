package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
   public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)), StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter((biomegenerationsettings1) -> biomegenerationsettings1.carvers), PlacedFeature.LIST_OF_LISTS_CODEC.promotePartial(Util.prefix("Features: ", LOGGER::error)).fieldOf("features").forGetter((biomegenerationsettings) -> biomegenerationsettings.features)).apply(recordcodecbuilder_instance, BiomeGenerationSettings::new));
   private final Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers;
   private final List<HolderSet<PlacedFeature>> features;
   private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
   private final Supplier<Set<PlacedFeature>> featureSet;

   BiomeGenerationSettings(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> map, List<HolderSet<PlacedFeature>> list) {
      this.carvers = map;
      this.features = list;
      this.flowerFeatures = Suppliers.memoize(() -> list.stream().flatMap(HolderSet::stream).map(Holder::value).flatMap(PlacedFeature::getFeatures).filter((configuredfeature) -> configuredfeature.feature() == Feature.FLOWER).collect(ImmutableList.toImmutableList()));
      this.featureSet = Suppliers.memoize(() -> list.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
   }

   public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving generationstep_carving) {
      return Objects.requireNonNullElseGet(this.carvers.get(generationstep_carving), List::of);
   }

   public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
      return this.flowerFeatures.get();
   }

   public List<HolderSet<PlacedFeature>> features() {
      return this.features;
   }

   public boolean hasFeature(PlacedFeature placedfeature) {
      return this.featureSet.get().contains(placedfeature);
   }

   public static class Builder extends BiomeGenerationSettings.PlainBuilder {
      private final HolderGetter<PlacedFeature> placedFeatures;
      private final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers;

      public Builder(HolderGetter<PlacedFeature> holdergetter, HolderGetter<ConfiguredWorldCarver<?>> holdergetter1) {
         this.placedFeatures = holdergetter;
         this.worldCarvers = holdergetter1;
      }

      public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration generationstep_decoration, ResourceKey<PlacedFeature> resourcekey) {
         this.addFeature(generationstep_decoration.ordinal(), this.placedFeatures.getOrThrow(resourcekey));
         return this;
      }

      public BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving generationstep_carving, ResourceKey<ConfiguredWorldCarver<?>> resourcekey) {
         this.addCarver(generationstep_carving, this.worldCarvers.getOrThrow(resourcekey));
         return this;
      }
   }

   public static class PlainBuilder {
      private final Map<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
      private final List<List<Holder<PlacedFeature>>> features = Lists.newArrayList();

      public BiomeGenerationSettings.PlainBuilder addFeature(GenerationStep.Decoration generationstep_decoration, Holder<PlacedFeature> holder) {
         return this.addFeature(generationstep_decoration.ordinal(), holder);
      }

      public BiomeGenerationSettings.PlainBuilder addFeature(int i, Holder<PlacedFeature> holder) {
         this.addFeatureStepsUpTo(i);
         this.features.get(i).add(holder);
         return this;
      }

      public BiomeGenerationSettings.PlainBuilder addCarver(GenerationStep.Carving generationstep_carving, Holder<ConfiguredWorldCarver<?>> holder) {
         this.carvers.computeIfAbsent(generationstep_carving, (generationstep_carving1) -> Lists.newArrayList()).add(holder);
         return this;
      }

      private void addFeatureStepsUpTo(int i) {
         while(this.features.size() <= i) {
            this.features.add(Lists.newArrayList());
         }

      }

      public BiomeGenerationSettings build() {
         return new BiomeGenerationSettings(this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (map_entry) -> HolderSet.direct(map_entry.getValue()))), this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList()));
      }
   }
}
