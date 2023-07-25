package net.minecraft.world.ticks;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
   private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue<>(ScheduledTick.DRAIN_ORDER);
   @Nullable
   private List<SavedTick<T>> pendingTicks;
   private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
   @Nullable
   private BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

   public LevelChunkTicks() {
   }

   public LevelChunkTicks(List<SavedTick<T>> list) {
      this.pendingTicks = list;

      for(SavedTick<T> savedtick : list) {
         this.ticksPerPosition.add(ScheduledTick.probe(savedtick.type(), savedtick.pos()));
      }

   }

   public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> biconsumer) {
      this.onTickAdded = biconsumer;
   }

   @Nullable
   public ScheduledTick<T> peek() {
      return this.tickQueue.peek();
   }

   @Nullable
   public ScheduledTick<T> poll() {
      ScheduledTick<T> scheduledtick = this.tickQueue.poll();
      if (scheduledtick != null) {
         this.ticksPerPosition.remove(scheduledtick);
      }

      return scheduledtick;
   }

   public void schedule(ScheduledTick<T> scheduledtick) {
      if (this.ticksPerPosition.add(scheduledtick)) {
         this.scheduleUnchecked(scheduledtick);
      }

   }

   private void scheduleUnchecked(ScheduledTick<T> scheduledtick) {
      this.tickQueue.add(scheduledtick);
      if (this.onTickAdded != null) {
         this.onTickAdded.accept(this, scheduledtick);
      }

   }

   public boolean hasScheduledTick(BlockPos blockpos, T object) {
      return this.ticksPerPosition.contains(ScheduledTick.probe(object, blockpos));
   }

   public void removeIf(Predicate<ScheduledTick<T>> predicate) {
      Iterator<ScheduledTick<T>> iterator = this.tickQueue.iterator();

      while(iterator.hasNext()) {
         ScheduledTick<T> scheduledtick = iterator.next();
         if (predicate.test(scheduledtick)) {
            iterator.remove();
            this.ticksPerPosition.remove(scheduledtick);
         }
      }

   }

   public Stream<ScheduledTick<T>> getAll() {
      return this.tickQueue.stream();
   }

   public int count() {
      return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
   }

   public ListTag save(long i, Function<T, String> function) {
      ListTag listtag = new ListTag();
      if (this.pendingTicks != null) {
         for(SavedTick<T> savedtick : this.pendingTicks) {
            listtag.add(savedtick.save(function));
         }
      }

      for(ScheduledTick<T> scheduledtick : this.tickQueue) {
         listtag.add(SavedTick.saveTick(scheduledtick, function, i));
      }

      return listtag;
   }

   public void unpack(long i) {
      if (this.pendingTicks != null) {
         int j = -this.pendingTicks.size();

         for(SavedTick<T> savedtick : this.pendingTicks) {
            this.scheduleUnchecked(savedtick.unpack(i, (long)(j++)));
         }
      }

      this.pendingTicks = null;
   }

   public static <T> LevelChunkTicks<T> load(ListTag listtag, Function<String, Optional<T>> function, ChunkPos chunkpos) {
      ImmutableList.Builder<SavedTick<T>> immutablelist_builder = ImmutableList.builder();
      SavedTick.loadTickList(listtag, function, chunkpos, immutablelist_builder::add);
      return new LevelChunkTicks<>(immutablelist_builder.build());
   }
}
