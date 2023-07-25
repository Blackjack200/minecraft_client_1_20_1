package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class FeatureSorter {
   public static <T> List<FeatureSorter.StepFeatureData> buildFeaturesPerStep(List<T> list, Function<T, List<HolderSet<PlacedFeature>>> function, boolean flag) {
      Object2IntMap<PlacedFeature> object2intmap = new Object2IntOpenHashMap<>();
      MutableInt mutableint = new MutableInt(0);

      record FeatureData(int featureIndex, int step, PlacedFeature feature) {
      }

      Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
      Map<FeatureData, Set<FeatureData>> map = new TreeMap<>(comparator);
      int i = 0;

      for(T object : list) {
         List<FeatureData> list1 = Lists.newArrayList();
         List<HolderSet<PlacedFeature>> list2 = function.apply(object);
         i = Math.max(i, list2.size());

         for(int j = 0; j < list2.size(); ++j) {
            for(Holder<PlacedFeature> holder : list2.get(j)) {
               PlacedFeature placedfeature = holder.value();
               list1.add(new FeatureData(object2intmap.computeIfAbsent(placedfeature, (object2) -> mutableint.getAndIncrement()), j, placedfeature));
            }
         }

         for(int k = 0; k < list1.size(); ++k) {
            Set<FeatureData> set = map.computeIfAbsent(list1.get(k), (featuresorter_1featuredata2) -> new TreeSet<>(comparator));
            if (k < list1.size() - 1) {
               set.add(list1.get(k + 1));
            }
         }
      }

      Set<FeatureData> set1 = new TreeSet<>(comparator);
      Set<FeatureData> set2 = new TreeSet<>(comparator);
      List<FeatureData> list3 = Lists.newArrayList();

      for(FeatureData featuresorter_1featuredata : map.keySet()) {
         if (!set2.isEmpty()) {
            throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
         }

         if (!set1.contains(featuresorter_1featuredata) && Graph.depthFirstSearch(map, set1, set2, list3::add, featuresorter_1featuredata)) {
            if (!flag) {
               throw new IllegalStateException("Feature order cycle found");
            }

            List<T> list4 = new ArrayList<>(list);

            int l;
            do {
               l = list4.size();
               ListIterator<T> listiterator = list4.listIterator();

               while(listiterator.hasNext()) {
                  T object1 = listiterator.next();
                  listiterator.remove();

                  try {
                     buildFeaturesPerStep(list4, function, false);
                  } catch (IllegalStateException var18) {
                     continue;
                  }

                  listiterator.add(object1);
               }
            } while(l != list4.size());

            throw new IllegalStateException("Feature order cycle found, involved sources: " + list4);
         }
      }

      Collections.reverse(list3);
      ImmutableList.Builder<FeatureSorter.StepFeatureData> immutablelist_builder = ImmutableList.builder();

      for(int i1 = 0; i1 < i; ++i1) {
         int j1 = i1;
         List<PlacedFeature> list5 = list3.stream().filter((featuresorter_1featuredata1) -> featuresorter_1featuredata1.step() == j1).map(FeatureData::feature).collect(Collectors.toList());
         immutablelist_builder.add(new FeatureSorter.StepFeatureData(list5));
      }

      return immutablelist_builder.build();
   }

   public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
      StepFeatureData(List<PlacedFeature> list) {
         this(list, Util.createIndexLookup(list, (i) -> new Object2IntOpenCustomHashMap<>(i, Util.identityStrategy())));
      }
   }
}
