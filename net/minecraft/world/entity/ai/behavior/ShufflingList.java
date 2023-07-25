package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U> implements Iterable<U> {
   protected final List<ShufflingList.WeightedEntry<U>> entries;
   private final RandomSource random = RandomSource.create();

   public ShufflingList() {
      this.entries = Lists.newArrayList();
   }

   private ShufflingList(List<ShufflingList.WeightedEntry<U>> list) {
      this.entries = Lists.newArrayList(list);
   }

   public static <U> Codec<ShufflingList<U>> codec(Codec<U> codec) {
      return ShufflingList.WeightedEntry.<U>codec(codec).listOf().xmap(ShufflingList::new, (shufflinglist) -> shufflinglist.entries);
   }

   public ShufflingList<U> add(U object, int i) {
      this.entries.add(new ShufflingList.WeightedEntry<>(object, i));
      return this;
   }

   public ShufflingList<U> shuffle() {
      this.entries.forEach((shufflinglist_weightedentry) -> shufflinglist_weightedentry.setRandom(this.random.nextFloat()));
      this.entries.sort(Comparator.comparingDouble(ShufflingList.WeightedEntry::getRandWeight));
      return this;
   }

   public Stream<U> stream() {
      return this.entries.stream().map(ShufflingList.WeightedEntry::getData);
   }

   public Iterator<U> iterator() {
      return Iterators.transform(this.entries.iterator(), ShufflingList.WeightedEntry::getData);
   }

   public String toString() {
      return "ShufflingList[" + this.entries + "]";
   }

   public static class WeightedEntry<T> {
      final T data;
      final int weight;
      private double randWeight;

      WeightedEntry(T object, int i) {
         this.weight = i;
         this.data = object;
      }

      private double getRandWeight() {
         return this.randWeight;
      }

      void setRandom(float f) {
         this.randWeight = -Math.pow((double)f, (double)(1.0F / (float)this.weight));
      }

      public T getData() {
         return this.data;
      }

      public int getWeight() {
         return this.weight;
      }

      public String toString() {
         return this.weight + ":" + this.data;
      }

      public static <E> Codec<ShufflingList.WeightedEntry<E>> codec(final Codec<E> codec) {
         return new Codec<ShufflingList.WeightedEntry<E>>() {
            public <T> DataResult<Pair<ShufflingList.WeightedEntry<E>, T>> decode(DynamicOps<T> dynamicops, T object) {
               Dynamic<T> dynamic = new Dynamic<>(dynamicops, object);
               return dynamic.get("data").flatMap(codec::parse).map((object1) -> new ShufflingList.WeightedEntry<>(object1, dynamic.get("weight").asInt(1))).map((shufflinglist_weightedentry) -> Pair.of(shufflinglist_weightedentry, dynamicops.empty()));
            }

            public <T> DataResult<T> encode(ShufflingList.WeightedEntry<E> shufflinglist_weightedentry, DynamicOps<T> dynamicops, T object) {
               return dynamicops.mapBuilder().add("weight", dynamicops.createInt(shufflinglist_weightedentry.weight)).add("data", codec.encodeStart(dynamicops, shufflinglist_weightedentry.data)).build(object);
            }
         };
      }
   }
}
