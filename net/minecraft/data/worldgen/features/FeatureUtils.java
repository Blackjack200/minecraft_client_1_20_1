package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
   public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> bootstapcontext) {
      AquaticFeatures.bootstrap(bootstapcontext);
      CaveFeatures.bootstrap(bootstapcontext);
      EndFeatures.bootstrap(bootstapcontext);
      MiscOverworldFeatures.bootstrap(bootstapcontext);
      NetherFeatures.bootstrap(bootstapcontext);
      OreFeatures.bootstrap(bootstapcontext);
      PileFeatures.bootstrap(bootstapcontext);
      TreeFeatures.bootstrap(bootstapcontext);
      VegetationFeatures.bootstrap(bootstapcontext);
   }

   private static BlockPredicate simplePatchPredicate(List<Block> list) {
      BlockPredicate blockpredicate;
      if (!list.isEmpty()) {
         blockpredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), list));
      } else {
         blockpredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;
      }

      return blockpredicate;
   }

   public static RandomPatchConfiguration simpleRandomPatchConfiguration(int i, Holder<PlacedFeature> holder) {
      return new RandomPatchConfiguration(i, 7, 3, holder);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F feature, FC featureconfiguration, List<Block> list, int i) {
      return simpleRandomPatchConfiguration(i, PlacementUtils.filtered(feature, featureconfiguration, simplePatchPredicate(list)));
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F feature, FC featureconfiguration, List<Block> list) {
      return simplePatchConfiguration(feature, featureconfiguration, list, 96);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F feature, FC featureconfiguration) {
      return simplePatchConfiguration(feature, featureconfiguration, List.of(), 96);
   }

   public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String s) {
      return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(s));
   }

   public static void register(BootstapContext<ConfiguredFeature<?, ?>> bootstapcontext, ResourceKey<ConfiguredFeature<?, ?>> resourcekey, Feature<NoneFeatureConfiguration> feature) {
      register(bootstapcontext, resourcekey, feature, FeatureConfiguration.NONE);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> bootstapcontext, ResourceKey<ConfiguredFeature<?, ?>> resourcekey, F feature, FC featureconfiguration) {
      bootstapcontext.register(resourcekey, new ConfiguredFeature(feature, featureconfiguration));
   }
}
