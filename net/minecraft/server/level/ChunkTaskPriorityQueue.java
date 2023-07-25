package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue<T> {
   public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
   private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT).mapToObj((j) -> new Long2ObjectLinkedOpenHashMap()).collect(Collectors.toList());
   private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
   private final String name;
   private final LongSet acquired = new LongOpenHashSet();
   private final int maxTasks;

   public ChunkTaskPriorityQueue(String s, int i) {
      this.name = s;
      this.maxTasks = i;
   }

   protected void resortChunkTasks(int i, ChunkPos chunkpos, int j) {
      if (i < PRIORITY_LEVEL_COUNT) {
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap = this.taskQueue.get(i);
         List<Optional<T>> list = long2objectlinkedopenhashmap.remove(chunkpos.toLong());
         if (i == this.firstQueue) {
            while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
               ++this.firstQueue;
            }
         }

         if (list != null && !list.isEmpty()) {
            this.taskQueue.get(j).computeIfAbsent(chunkpos.toLong(), (k) -> Lists.newArrayList()).addAll(list);
            this.firstQueue = Math.min(this.firstQueue, j);
         }

      }
   }

   protected void submit(Optional<T> optional, long i, int j) {
      this.taskQueue.get(j).computeIfAbsent(i, (k) -> Lists.newArrayList()).add(optional);
      this.firstQueue = Math.min(this.firstQueue, j);
   }

   protected void release(long i, boolean flag) {
      for(Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap : this.taskQueue) {
         List<Optional<T>> list = long2objectlinkedopenhashmap.get(i);
         if (list != null) {
            if (flag) {
               list.clear();
            } else {
               list.removeIf((optional) -> !optional.isPresent());
            }

            if (list.isEmpty()) {
               long2objectlinkedopenhashmap.remove(i);
            }
         }
      }

      while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
         ++this.firstQueue;
      }

      this.acquired.remove(i);
   }

   private Runnable acquire(long i) {
      return () -> this.acquired.add(i);
   }

   @Nullable
   public Stream<Either<T, Runnable>> pop() {
      if (this.acquired.size() >= this.maxTasks) {
         return null;
      } else if (!this.hasWork()) {
         return null;
      } else {
         int i = this.firstQueue;
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap = this.taskQueue.get(i);
         long j = long2objectlinkedopenhashmap.firstLongKey();

         List<Optional<T>> list;
         for(list = long2objectlinkedopenhashmap.removeFirst(); this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty(); ++this.firstQueue) {
         }

         return list.stream().map((optional) -> optional.map(Either::left).orElseGet(() -> Either.right(this.acquire(j))));
      }
   }

   public boolean hasWork() {
      return this.firstQueue < PRIORITY_LEVEL_COUNT;
   }

   public String toString() {
      return this.name + " " + this.firstQueue + "...";
   }

   @VisibleForTesting
   LongSet getAcquired() {
      return new LongOpenHashSet(this.acquired);
   }
}
