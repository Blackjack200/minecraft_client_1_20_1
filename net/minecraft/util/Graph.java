package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
   private Graph() {
   }

   public static <T> boolean depthFirstSearch(Map<T, Set<T>> map, Set<T> set, Set<T> set1, Consumer<T> consumer, T object) {
      if (set.contains(object)) {
         return false;
      } else if (set1.contains(object)) {
         return true;
      } else {
         set1.add(object);

         for(T object1 : map.getOrDefault(object, ImmutableSet.of())) {
            if (depthFirstSearch(map, set, set1, consumer, object1)) {
               return true;
            }
         }

         set1.remove(object);
         set.add(object);
         consumer.accept(object);
         return false;
      }
   }
}
