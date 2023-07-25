package net.minecraft.world.flag;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

public final class FeatureFlagSet {
   private static final FeatureFlagSet EMPTY = new FeatureFlagSet((FeatureFlagUniverse)null, 0L);
   public static final int MAX_CONTAINER_SIZE = 64;
   @Nullable
   private final FeatureFlagUniverse universe;
   private final long mask;

   private FeatureFlagSet(@Nullable FeatureFlagUniverse featureflaguniverse, long i) {
      this.universe = featureflaguniverse;
      this.mask = i;
   }

   static FeatureFlagSet create(FeatureFlagUniverse featureflaguniverse, Collection<FeatureFlag> collection) {
      if (collection.isEmpty()) {
         return EMPTY;
      } else {
         long i = computeMask(featureflaguniverse, 0L, collection);
         return new FeatureFlagSet(featureflaguniverse, i);
      }
   }

   public static FeatureFlagSet of() {
      return EMPTY;
   }

   public static FeatureFlagSet of(FeatureFlag featureflag) {
      return new FeatureFlagSet(featureflag.universe, featureflag.mask);
   }

   public static FeatureFlagSet of(FeatureFlag featureflag, FeatureFlag... afeatureflag) {
      long i = afeatureflag.length == 0 ? featureflag.mask : computeMask(featureflag.universe, featureflag.mask, Arrays.asList(afeatureflag));
      return new FeatureFlagSet(featureflag.universe, i);
   }

   private static long computeMask(FeatureFlagUniverse featureflaguniverse, long i, Iterable<FeatureFlag> iterable) {
      for(FeatureFlag featureflag : iterable) {
         if (featureflaguniverse != featureflag.universe) {
            throw new IllegalStateException("Mismatched feature universe, expected '" + featureflaguniverse + "', but got '" + featureflag.universe + "'");
         }

         i |= featureflag.mask;
      }

      return i;
   }

   public boolean contains(FeatureFlag featureflag) {
      if (this.universe != featureflag.universe) {
         return false;
      } else {
         return (this.mask & featureflag.mask) != 0L;
      }
   }

   public boolean isSubsetOf(FeatureFlagSet featureflagset) {
      if (this.universe == null) {
         return true;
      } else if (this.universe != featureflagset.universe) {
         return false;
      } else {
         return (this.mask & ~featureflagset.mask) == 0L;
      }
   }

   public FeatureFlagSet join(FeatureFlagSet featureflagset) {
      if (this.universe == null) {
         return featureflagset;
      } else if (featureflagset.universe == null) {
         return this;
      } else if (this.universe != featureflagset.universe) {
         throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + featureflagset.universe + "'");
      } else {
         return new FeatureFlagSet(this.universe, this.mask | featureflagset.mask);
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof FeatureFlagSet) {
            FeatureFlagSet featureflagset = (FeatureFlagSet)object;
            if (this.universe == featureflagset.universe && this.mask == featureflagset.mask) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return (int)HashCommon.mix(this.mask);
   }
}
