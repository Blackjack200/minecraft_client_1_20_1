package net.minecraft.world.flag;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final FeatureFlagUniverse universe;
   private final Map<ResourceLocation, FeatureFlag> names;
   private final FeatureFlagSet allFlags;

   FeatureFlagRegistry(FeatureFlagUniverse featureflaguniverse, FeatureFlagSet featureflagset, Map<ResourceLocation, FeatureFlag> map) {
      this.universe = featureflaguniverse;
      this.names = map;
      this.allFlags = featureflagset;
   }

   public boolean isSubset(FeatureFlagSet featureflagset) {
      return featureflagset.isSubsetOf(this.allFlags);
   }

   public FeatureFlagSet allFlags() {
      return this.allFlags;
   }

   public FeatureFlagSet fromNames(Iterable<ResourceLocation> iterable) {
      return this.fromNames(iterable, (resourcelocation) -> LOGGER.warn("Unknown feature flag: {}", (Object)resourcelocation));
   }

   public FeatureFlagSet subset(FeatureFlag... afeatureflag) {
      return FeatureFlagSet.create(this.universe, Arrays.asList(afeatureflag));
   }

   public FeatureFlagSet fromNames(Iterable<ResourceLocation> iterable, Consumer<ResourceLocation> consumer) {
      Set<FeatureFlag> set = Sets.newIdentityHashSet();

      for(ResourceLocation resourcelocation : iterable) {
         FeatureFlag featureflag = this.names.get(resourcelocation);
         if (featureflag == null) {
            consumer.accept(resourcelocation);
         } else {
            set.add(featureflag);
         }
      }

      return FeatureFlagSet.create(this.universe, set);
   }

   public Set<ResourceLocation> toNames(FeatureFlagSet featureflagset) {
      Set<ResourceLocation> set = new HashSet<>();
      this.names.forEach((resourcelocation, featureflag) -> {
         if (featureflagset.contains(featureflag)) {
            set.add(resourcelocation);
         }

      });
      return set;
   }

   public Codec<FeatureFlagSet> codec() {
      return ResourceLocation.CODEC.listOf().comapFlatMap((list) -> {
         Set<ResourceLocation> set = new HashSet<>();
         FeatureFlagSet featureflagset1 = this.fromNames(list, set::add);
         return !set.isEmpty() ? DataResult.error(() -> "Unknown feature ids: " + set, featureflagset1) : DataResult.success(featureflagset1);
      }, (featureflagset) -> List.copyOf(this.toNames(featureflagset)));
   }

   public static class Builder {
      private final FeatureFlagUniverse universe;
      private int id;
      private final Map<ResourceLocation, FeatureFlag> flags = new LinkedHashMap<>();

      public Builder(String s) {
         this.universe = new FeatureFlagUniverse(s);
      }

      public FeatureFlag createVanilla(String s) {
         return this.create(new ResourceLocation("minecraft", s));
      }

      public FeatureFlag create(ResourceLocation resourcelocation) {
         if (this.id >= 64) {
            throw new IllegalStateException("Too many feature flags");
         } else {
            FeatureFlag featureflag = new FeatureFlag(this.universe, this.id++);
            FeatureFlag featureflag1 = this.flags.put(resourcelocation, featureflag);
            if (featureflag1 != null) {
               throw new IllegalStateException("Duplicate feature flag " + resourcelocation);
            } else {
               return featureflag;
            }
         }
      }

      public FeatureFlagRegistry build() {
         FeatureFlagSet featureflagset = FeatureFlagSet.create(this.universe, this.flags.values());
         return new FeatureFlagRegistry(this.universe, featureflagset, Map.copyOf(this.flags));
      }
   }
}
