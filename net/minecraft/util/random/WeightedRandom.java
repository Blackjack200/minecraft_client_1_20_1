package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public class WeightedRandom {
   private WeightedRandom() {
   }

   public static int getTotalWeight(List<? extends WeightedEntry> list) {
      long i = 0L;

      for(WeightedEntry weightedentry : list) {
         i += (long)weightedentry.getWeight().asInt();
      }

      if (i > 2147483647L) {
         throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
      } else {
         return (int)i;
      }
   }

   public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomsource, List<T> list, int i) {
      if (i < 0) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
      } else if (i == 0) {
         return Optional.empty();
      } else {
         int j = randomsource.nextInt(i);
         return getWeightedItem(list, j);
      }
   }

   public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> list, int i) {
      for(T weightedentry : list) {
         i -= weightedentry.getWeight().asInt();
         if (i < 0) {
            return Optional.of(weightedentry);
         }
      }

      return Optional.empty();
   }

   public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource randomsource, List<T> list) {
      return getRandomItem(randomsource, list, getTotalWeight(list));
   }
}
