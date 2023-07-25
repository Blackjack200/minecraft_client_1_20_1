package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class FeatureFlags {
   public static final FeatureFlag VANILLA;
   public static final FeatureFlag BUNDLE;
   public static final FeatureFlagRegistry REGISTRY;
   public static final Codec<FeatureFlagSet> CODEC;
   public static final FeatureFlagSet VANILLA_SET;
   public static final FeatureFlagSet DEFAULT_FLAGS;

   public static String printMissingFlags(FeatureFlagSet featureflagset, FeatureFlagSet featureflagset1) {
      return printMissingFlags(REGISTRY, featureflagset, featureflagset1);
   }

   public static String printMissingFlags(FeatureFlagRegistry featureflagregistry, FeatureFlagSet featureflagset, FeatureFlagSet featureflagset1) {
      Set<ResourceLocation> set = featureflagregistry.toNames(featureflagset1);
      Set<ResourceLocation> set1 = featureflagregistry.toNames(featureflagset);
      return set.stream().filter((resourcelocation) -> !set1.contains(resourcelocation)).map(ResourceLocation::toString).collect(Collectors.joining(", "));
   }

   public static boolean isExperimental(FeatureFlagSet featureflagset) {
      return !featureflagset.isSubsetOf(VANILLA_SET);
   }

   static {
      FeatureFlagRegistry.Builder featureflagregistry_builder = new FeatureFlagRegistry.Builder("main");
      VANILLA = featureflagregistry_builder.createVanilla("vanilla");
      BUNDLE = featureflagregistry_builder.createVanilla("bundle");
      REGISTRY = featureflagregistry_builder.build();
      CODEC = REGISTRY.codec();
      VANILLA_SET = FeatureFlagSet.of(VANILLA);
      DEFAULT_FLAGS = VANILLA_SET;
   }
}
