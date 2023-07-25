package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelTicks<T> implements LevelTickAccess<T> {
   private static final Comparator<LevelChunkTicks<?>> CONTAINER_DRAIN_ORDER = (levelchunkticks, levelchunkticks1) -> ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(levelchunkticks.peek(), levelchunkticks1.peek());
   private final LongPredicate tickCheck;
   private final Supplier<ProfilerFiller> profiler;
   private final Long2ObjectMap<LevelChunkTicks<T>> allContainers = new Long2ObjectOpenHashMap<>();
   private final Long2LongMap nextTickForContainer = Util.make(new Long2LongOpenHashMap(), (long2longopenhashmap) -> long2longopenhashmap.defaultReturnValue(Long.MAX_VALUE));
   private final Queue<LevelChunkTicks<T>> containersToTick = new PriorityQueue<>(CONTAINER_DRAIN_ORDER);
   private final Queue<ScheduledTick<T>> toRunThisTick = new ArrayDeque<>();
   private final List<ScheduledTick<T>> alreadyRunThisTick = new ArrayList<>();
   private final Set<ScheduledTick<?>> toRunThisTickSet = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
   private final BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> chunkScheduleUpdater = (levelchunkticks, scheduledtick) -> {
      if (scheduledtick.equals(levelchunkticks.peek())) {
         this.updateContainerScheduling(scheduledtick);
      }

   };

   public LevelTicks(LongPredicate longpredicate, Supplier<ProfilerFiller> supplier) {
      this.tickCheck = longpredicate;
      this.profiler = supplier;
   }

   public void addContainer(ChunkPos chunkpos, LevelChunkTicks<T> levelchunkticks) {
      long i = chunkpos.toLong();
      this.allContainers.put(i, levelchunkticks);
      ScheduledTick<T> scheduledtick = levelchunkticks.peek();
      if (scheduledtick != null) {
         this.nextTickForContainer.put(i, scheduledtick.triggerTick());
      }

      levelchunkticks.setOnTickAdded(this.chunkScheduleUpdater);
   }

   public void removeContainer(ChunkPos chunkpos) {
      long i = chunkpos.toLong();
      LevelChunkTicks<T> levelchunkticks = this.allContainers.remove(i);
      this.nextTickForContainer.remove(i);
      if (levelchunkticks != null) {
         levelchunkticks.setOnTickAdded((BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>>)null);
      }

   }

   public void schedule(ScheduledTick<T> scheduledtick) {
      long i = ChunkPos.asLong(scheduledtick.pos());
      LevelChunkTicks<T> levelchunkticks = this.allContainers.get(i);
      if (levelchunkticks == null) {
         Util.pauseInIde(new IllegalStateException("Trying to schedule tick in not loaded position " + scheduledtick.pos()));
      } else {
         levelchunkticks.schedule(scheduledtick);
      }
   }

   public void tick(long i, int j, BiConsumer<BlockPos, T> biconsumer) {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("collect");
      this.collectTicks(i, j, profilerfiller);
      profilerfiller.popPush("run");
      profilerfiller.incrementCounter("ticksToRun", this.toRunThisTick.size());
      this.runCollectedTicks(biconsumer);
      profilerfiller.popPush("cleanup");
      this.cleanupAfterTick();
      profilerfiller.pop();
   }

   private void collectTicks(long i, int j, ProfilerFiller profilerfiller) {
      this.sortContainersToTick(i);
      profilerfiller.incrementCounter("containersToTick", this.containersToTick.size());
      this.drainContainers(i, j);
      this.rescheduleLeftoverContainers();
   }

   private void sortContainersToTick(long i) {
      ObjectIterator<Long2LongMap.Entry> objectiterator = Long2LongMaps.fastIterator(this.nextTickForContainer);

      while(objectiterator.hasNext()) {
         Long2LongMap.Entry long2longmap_entry = objectiterator.next();
         long j = long2longmap_entry.getLongKey();
         long k = long2longmap_entry.getLongValue();
         if (k <= i) {
            LevelChunkTicks<T> levelchunkticks = this.allContainers.get(j);
            if (levelchunkticks == null) {
               objectiterator.remove();
            } else {
               ScheduledTick<T> scheduledtick = levelchunkticks.peek();
               if (scheduledtick == null) {
                  objectiterator.remove();
               } else if (scheduledtick.triggerTick() > i) {
                  long2longmap_entry.setValue(scheduledtick.triggerTick());
               } else if (this.tickCheck.test(j)) {
                  objectiterator.remove();
                  this.containersToTick.add(levelchunkticks);
               }
            }
         }
      }

   }

   private void drainContainers(long i, int j) {
      LevelChunkTicks<T> levelchunkticks;
      while(this.canScheduleMoreTicks(j) && (levelchunkticks = this.containersToTick.poll()) != null) {
         ScheduledTick<T> scheduledtick = levelchunkticks.poll();
         this.scheduleForThisTick(scheduledtick);
         this.drainFromCurrentContainer(this.containersToTick, levelchunkticks, i, j);
         ScheduledTick<T> scheduledtick1 = levelchunkticks.peek();
         if (scheduledtick1 != null) {
            if (scheduledtick1.triggerTick() <= i && this.canScheduleMoreTicks(j)) {
               this.containersToTick.add(levelchunkticks);
            } else {
               this.updateContainerScheduling(scheduledtick1);
            }
         }
      }

   }

   private void rescheduleLeftoverContainers() {
      for(LevelChunkTicks<T> levelchunkticks : this.containersToTick) {
         this.updateContainerScheduling(levelchunkticks.peek());
      }

   }

   private void updateContainerScheduling(ScheduledTick<T> scheduledtick) {
      this.nextTickForContainer.put(ChunkPos.asLong(scheduledtick.pos()), scheduledtick.triggerTick());
   }

   private void drainFromCurrentContainer(Queue<LevelChunkTicks<T>> queue, LevelChunkTicks<T> levelchunkticks, long i, int j) {
      if (this.canScheduleMoreTicks(j)) {
         LevelChunkTicks<T> levelchunkticks1 = queue.peek();
         ScheduledTick<T> scheduledtick = levelchunkticks1 != null ? levelchunkticks1.peek() : null;

         while(this.canScheduleMoreTicks(j)) {
            ScheduledTick<T> scheduledtick1 = levelchunkticks.peek();
            if (scheduledtick1 == null || scheduledtick1.triggerTick() > i || scheduledtick != null && ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(scheduledtick1, scheduledtick) > 0) {
               break;
            }

            levelchunkticks.poll();
            this.scheduleForThisTick(scheduledtick1);
         }

      }
   }

   private void scheduleForThisTick(ScheduledTick<T> scheduledtick) {
      this.toRunThisTick.add(scheduledtick);
   }

   private boolean canScheduleMoreTicks(int i) {
      return this.toRunThisTick.size() < i;
   }

   private void runCollectedTicks(BiConsumer<BlockPos, T> biconsumer) {
      while(!this.toRunThisTick.isEmpty()) {
         ScheduledTick<T> scheduledtick = this.toRunThisTick.poll();
         if (!this.toRunThisTickSet.isEmpty()) {
            this.toRunThisTickSet.remove(scheduledtick);
         }

         this.alreadyRunThisTick.add(scheduledtick);
         biconsumer.accept(scheduledtick.pos(), scheduledtick.type());
      }

   }

   private void cleanupAfterTick() {
      this.toRunThisTick.clear();
      this.containersToTick.clear();
      this.alreadyRunThisTick.clear();
      this.toRunThisTickSet.clear();
   }

   public boolean hasScheduledTick(BlockPos blockpos, T object) {
      LevelChunkTicks<T> levelchunkticks = this.allContainers.get(ChunkPos.asLong(blockpos));
      return levelchunkticks != null && levelchunkticks.hasScheduledTick(blockpos, object);
   }

   public boolean willTickThisTick(BlockPos blockpos, T object) {
      this.calculateTickSetIfNeeded();
      return this.toRunThisTickSet.contains(ScheduledTick.probe(object, blockpos));
   }

   private void calculateTickSetIfNeeded() {
      if (this.toRunThisTickSet.isEmpty() && !this.toRunThisTick.isEmpty()) {
         this.toRunThisTickSet.addAll(this.toRunThisTick);
      }

   }

   private void forContainersInArea(BoundingBox boundingbox, LevelTicks.PosAndContainerConsumer<T> levelticks_posandcontainerconsumer) {
      int i = SectionPos.posToSectionCoord((double)boundingbox.minX());
      int j = SectionPos.posToSectionCoord((double)boundingbox.minZ());
      int k = SectionPos.posToSectionCoord((double)boundingbox.maxX());
      int l = SectionPos.posToSectionCoord((double)boundingbox.maxZ());

      for(int i1 = i; i1 <= k; ++i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            long k1 = ChunkPos.asLong(i1, j1);
            LevelChunkTicks<T> levelchunkticks = this.allContainers.get(k1);
            if (levelchunkticks != null) {
               levelticks_posandcontainerconsumer.accept(k1, levelchunkticks);
            }
         }
      }

   }

   public void clearArea(BoundingBox boundingbox) {
      Predicate<ScheduledTick<T>> predicate = (scheduledtick2) -> boundingbox.isInside(scheduledtick2.pos());
      this.forContainersInArea(boundingbox, (i, levelchunkticks) -> {
         ScheduledTick<T> scheduledtick = levelchunkticks.peek();
         levelchunkticks.removeIf(predicate);
         ScheduledTick<T> scheduledtick1 = levelchunkticks.peek();
         if (scheduledtick1 != scheduledtick) {
            if (scheduledtick1 != null) {
               this.updateContainerScheduling(scheduledtick1);
            } else {
               this.nextTickForContainer.remove(i);
            }
         }

      });
      this.alreadyRunThisTick.removeIf(predicate);
      this.toRunThisTick.removeIf(predicate);
   }

   public void copyArea(BoundingBox boundingbox, Vec3i vec3i) {
      this.copyAreaFrom(this, boundingbox, vec3i);
   }

   public void copyAreaFrom(LevelTicks<T> levelticks, BoundingBox boundingbox, Vec3i vec3i) {
      List<ScheduledTick<T>> list = new ArrayList<>();
      Predicate<ScheduledTick<T>> predicate = (scheduledtick1) -> boundingbox.isInside(scheduledtick1.pos());
      levelticks.alreadyRunThisTick.stream().filter(predicate).forEach(list::add);
      levelticks.toRunThisTick.stream().filter(predicate).forEach(list::add);
      levelticks.forContainersInArea(boundingbox, (i1, levelchunkticks) -> levelchunkticks.getAll().filter(predicate).forEach(list::add));
      LongSummaryStatistics longsummarystatistics = list.stream().mapToLong(ScheduledTick::subTickOrder).summaryStatistics();
      long i = longsummarystatistics.getMin();
      long j = longsummarystatistics.getMax();
      list.forEach((scheduledtick) -> this.schedule(new ScheduledTick<>(scheduledtick.type(), scheduledtick.pos().offset(vec3i), scheduledtick.triggerTick(), scheduledtick.priority(), scheduledtick.subTickOrder() - i + j + 1L)));
   }

   public int count() {
      return this.allContainers.values().stream().mapToInt(TickAccess::count).sum();
   }

   @FunctionalInterface
   interface PosAndContainerConsumer<T> {
      void accept(long i, LevelChunkTicks<T> levelchunkticks);
   }
}
