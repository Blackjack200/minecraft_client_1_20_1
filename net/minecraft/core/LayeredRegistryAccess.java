package net.minecraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;

public class LayeredRegistryAccess<T> {
   private final List<T> keys;
   private final List<RegistryAccess.Frozen> values;
   private final RegistryAccess.Frozen composite;

   public LayeredRegistryAccess(List<T> list) {
      this(list, Util.make(() -> {
         RegistryAccess.Frozen[] aregistryaccess_frozen = new RegistryAccess.Frozen[list.size()];
         Arrays.fill(aregistryaccess_frozen, RegistryAccess.EMPTY);
         return Arrays.asList(aregistryaccess_frozen);
      }));
   }

   private LayeredRegistryAccess(List<T> list, List<RegistryAccess.Frozen> list1) {
      this.keys = List.copyOf(list);
      this.values = List.copyOf(list1);
      this.composite = (new RegistryAccess.ImmutableRegistryAccess(collectRegistries(list1.stream()))).freeze();
   }

   private int getLayerIndexOrThrow(T object) {
      int i = this.keys.indexOf(object);
      if (i == -1) {
         throw new IllegalStateException("Can't find " + object + " inside " + this.keys);
      } else {
         return i;
      }
   }

   public RegistryAccess.Frozen getLayer(T object) {
      int i = this.getLayerIndexOrThrow(object);
      return this.values.get(i);
   }

   public RegistryAccess.Frozen getAccessForLoading(T object) {
      int i = this.getLayerIndexOrThrow(object);
      return this.getCompositeAccessForLayers(0, i);
   }

   public RegistryAccess.Frozen getAccessFrom(T object) {
      int i = this.getLayerIndexOrThrow(object);
      return this.getCompositeAccessForLayers(i, this.values.size());
   }

   private RegistryAccess.Frozen getCompositeAccessForLayers(int i, int j) {
      return (new RegistryAccess.ImmutableRegistryAccess(collectRegistries(this.values.subList(i, j).stream()))).freeze();
   }

   public LayeredRegistryAccess<T> replaceFrom(T object, RegistryAccess.Frozen... aregistryaccess_frozen) {
      return this.replaceFrom(object, Arrays.asList(aregistryaccess_frozen));
   }

   public LayeredRegistryAccess<T> replaceFrom(T object, List<RegistryAccess.Frozen> list) {
      int i = this.getLayerIndexOrThrow(object);
      if (list.size() > this.values.size() - i) {
         throw new IllegalStateException("Too many values to replace");
      } else {
         List<RegistryAccess.Frozen> list1 = new ArrayList<>();

         for(int j = 0; j < i; ++j) {
            list1.add(this.values.get(j));
         }

         list1.addAll(list);

         while(list1.size() < this.values.size()) {
            list1.add(RegistryAccess.EMPTY);
         }

         return new LayeredRegistryAccess<>(this.keys, list1);
      }
   }

   public RegistryAccess.Frozen compositeAccess() {
      return this.composite;
   }

   private static Map<ResourceKey<? extends Registry<?>>, Registry<?>> collectRegistries(Stream<? extends RegistryAccess> stream) {
      Map<ResourceKey<? extends Registry<?>>, Registry<?>> map = new HashMap<>();
      stream.forEach((registryaccess) -> registryaccess.registries().forEach((registryaccess_registryentry) -> {
            if (map.put(registryaccess_registryentry.key(), registryaccess_registryentry.value()) != null) {
               throw new IllegalStateException("Duplicated registry " + registryaccess_registryentry.key());
            }
         }));
      return map;
   }
}
