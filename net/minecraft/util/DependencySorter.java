package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends DependencySorter.Entry<K>> {
   private final Map<K, V> contents = new HashMap<>();

   public DependencySorter<K, V> addEntry(K object, V dependencysorter_entry) {
      this.contents.put(object, dependencysorter_entry);
      return this;
   }

   private void visitDependenciesAndElement(Multimap<K, K> multimap, Set<K> set, K object, BiConsumer<K, V> biconsumer) {
      if (set.add(object)) {
         multimap.get(object).forEach((object1) -> this.visitDependenciesAndElement(multimap, set, object1, biconsumer));
         V dependencysorter_entry = this.contents.get(object);
         if (dependencysorter_entry != null) {
            biconsumer.accept(object, dependencysorter_entry);
         }

      }
   }

   private static <K> boolean isCyclic(Multimap<K, K> multimap, K object, K object1) {
      Collection<K> collection = multimap.get(object1);
      return collection.contains(object) ? true : collection.stream().anyMatch((object3) -> isCyclic(multimap, object, object3));
   }

   private static <K> void addDependencyIfNotCyclic(Multimap<K, K> multimap, K object, K object1) {
      if (!isCyclic(multimap, object, object1)) {
         multimap.put(object, object1);
      }

   }

   public void orderByDependencies(BiConsumer<K, V> biconsumer) {
      Multimap<K, K> multimap = HashMultimap.create();
      this.contents.forEach((object4, dependencysorter_entry1) -> dependencysorter_entry1.visitRequiredDependencies((object6) -> addDependencyIfNotCyclic(multimap, object4, object6)));
      this.contents.forEach((object1, dependencysorter_entry) -> dependencysorter_entry.visitOptionalDependencies((object3) -> addDependencyIfNotCyclic(multimap, object1, object3)));
      Set<K> set = new HashSet<>();
      this.contents.keySet().forEach((object) -> this.visitDependenciesAndElement(multimap, set, object, biconsumer));
   }

   public interface Entry<K> {
      void visitRequiredDependencies(Consumer<K> consumer);

      void visitOptionalDependencies(Consumer<K> consumer);
   }
}
