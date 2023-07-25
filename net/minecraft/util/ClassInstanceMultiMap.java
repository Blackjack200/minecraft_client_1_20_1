package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInstanceMultiMap<T> extends AbstractCollection<T> {
   private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
   private final Class<T> baseClass;
   private final List<T> allInstances = Lists.newArrayList();

   public ClassInstanceMultiMap(Class<T> oclass) {
      this.baseClass = oclass;
      this.byClass.put(oclass, this.allInstances);
   }

   public boolean add(T object) {
      boolean flag = false;

      for(Map.Entry<Class<?>, List<T>> map_entry : this.byClass.entrySet()) {
         if (map_entry.getKey().isInstance(object)) {
            flag |= map_entry.getValue().add(object);
         }
      }

      return flag;
   }

   public boolean remove(Object object) {
      boolean flag = false;

      for(Map.Entry<Class<?>, List<T>> map_entry : this.byClass.entrySet()) {
         if (map_entry.getKey().isInstance(object)) {
            List<T> list = map_entry.getValue();
            flag |= list.remove(object);
         }
      }

      return flag;
   }

   public boolean contains(Object object) {
      return this.find(object.getClass()).contains(object);
   }

   public <S> Collection<S> find(Class<S> oclass) {
      if (!this.baseClass.isAssignableFrom(oclass)) {
         throw new IllegalArgumentException("Don't know how to search for " + oclass);
      } else {
         List<? extends T> list = this.byClass.computeIfAbsent(oclass, (oclass1) -> this.allInstances.stream().filter(oclass1::isInstance).collect(Collectors.toList()));
         return Collections.unmodifiableCollection(list);
      }
   }

   public Iterator<T> iterator() {
      return (Iterator<T>)(this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator()));
   }

   public List<T> getAllInstances() {
      return ImmutableList.copyOf(this.allInstances);
   }

   public int size() {
      return this.allInstances.size();
   }
}
