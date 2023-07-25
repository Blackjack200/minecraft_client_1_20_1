package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class ChunkTaskPriorityQueueSorter implements ChunkHolder.LevelChangeListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
   private final Set<ProcessorHandle<?>> sleeping;
   private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

   public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> list, Executor executor, int i) {
      this.queues = list.stream().collect(Collectors.toMap(Function.identity(), (processorhandle) -> new ChunkTaskPriorityQueue<>(processorhandle.name() + "_queue", i)));
      this.sleeping = Sets.newHashSet(list);
      this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(4), executor, "sorter");
   }

   public boolean hasWork() {
      return this.mailbox.hasWork() || this.queues.values().stream().anyMatch(ChunkTaskPriorityQueue::hasWork);
   }

   public static <T> ChunkTaskPriorityQueueSorter.Message<T> message(Function<ProcessorHandle<Unit>, T> function, long i, IntSupplier intsupplier) {
      return new ChunkTaskPriorityQueueSorter.Message<>(function, i, intsupplier);
   }

   public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(Runnable runnable, long i, IntSupplier intsupplier) {
      return new ChunkTaskPriorityQueueSorter.Message<>((processorhandle) -> () -> {
            runnable.run();
            processorhandle.tell(Unit.INSTANCE);
         }, i, intsupplier);
   }

   public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(ChunkHolder chunkholder, Runnable runnable) {
      return message(runnable, chunkholder.getPos().toLong(), chunkholder::getQueueLevel);
   }

   public static <T> ChunkTaskPriorityQueueSorter.Message<T> message(ChunkHolder chunkholder, Function<ProcessorHandle<Unit>, T> function) {
      return message(function, chunkholder.getPos().toLong(), chunkholder::getQueueLevel);
   }

   public static ChunkTaskPriorityQueueSorter.Release release(Runnable runnable, long i, boolean flag) {
      return new ChunkTaskPriorityQueueSorter.Release(runnable, i, flag);
   }

   public <T> ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>> getProcessor(ProcessorHandle<T> processorhandle, boolean flag) {
      return this.mailbox.ask((processorhandle2) -> new StrictQueue.IntRunnable(0, () -> {
            this.getQueue(processorhandle);
            processorhandle2.tell(ProcessorHandle.of("chunk priority sorter around " + processorhandle.name(), (chunktaskpriorityqueuesorter_message) -> this.submit(processorhandle, chunktaskpriorityqueuesorter_message.task, chunktaskpriorityqueuesorter_message.pos, chunktaskpriorityqueuesorter_message.level, flag)));
         })).join();
   }

   public ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> getReleaseProcessor(ProcessorHandle<Runnable> processorhandle) {
      return this.mailbox.ask((processorhandle2) -> new StrictQueue.IntRunnable(0, () -> processorhandle2.tell(ProcessorHandle.of("chunk priority sorter around " + processorhandle.name(), (chunktaskpriorityqueuesorter_release) -> this.release(processorhandle, chunktaskpriorityqueuesorter_release.pos, chunktaskpriorityqueuesorter_release.task, chunktaskpriorityqueuesorter_release.clearQueue))))).join();
   }

   public void onLevelChange(ChunkPos chunkpos, IntSupplier intsupplier, int i, IntConsumer intconsumer) {
      this.mailbox.tell(new StrictQueue.IntRunnable(0, () -> {
         int k = intsupplier.getAsInt();
         this.queues.values().forEach((chunktaskpriorityqueue) -> chunktaskpriorityqueue.resortChunkTasks(k, chunkpos, i));
         intconsumer.accept(i);
      }));
   }

   private <T> void release(ProcessorHandle<T> processorhandle, long i, Runnable runnable, boolean flag) {
      this.mailbox.tell(new StrictQueue.IntRunnable(1, () -> {
         ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunktaskpriorityqueue = this.getQueue(processorhandle);
         chunktaskpriorityqueue.release(i, flag);
         if (this.sleeping.remove(processorhandle)) {
            this.pollTask(chunktaskpriorityqueue, processorhandle);
         }

         runnable.run();
      }));
   }

   private <T> void submit(ProcessorHandle<T> processorhandle, Function<ProcessorHandle<Unit>, T> function, long i, IntSupplier intsupplier, boolean flag) {
      this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
         ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunktaskpriorityqueue = this.getQueue(processorhandle);
         int k = intsupplier.getAsInt();
         chunktaskpriorityqueue.submit(Optional.of(function), i, k);
         if (flag) {
            chunktaskpriorityqueue.submit(Optional.empty(), i, k);
         }

         if (this.sleeping.remove(processorhandle)) {
            this.pollTask(chunktaskpriorityqueue, processorhandle);
         }

      }));
   }

   private <T> void pollTask(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunktaskpriorityqueue, ProcessorHandle<T> processorhandle) {
      this.mailbox.tell(new StrictQueue.IntRunnable(3, () -> {
         Stream<Either<Function<ProcessorHandle<Unit>, T>, Runnable>> stream = chunktaskpriorityqueue.pop();
         if (stream == null) {
            this.sleeping.add(processorhandle);
         } else {
            CompletableFuture.allOf(stream.map((either) -> either.map(processorhandle::ask, (runnable) -> {
                  runnable.run();
                  return CompletableFuture.completedFuture(Unit.INSTANCE);
               })).toArray((i) -> new CompletableFuture[i])).thenAccept((ovoid) -> this.pollTask(chunktaskpriorityqueue, processorhandle));
         }

      }));
   }

   private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> processorhandle) {
      ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>> chunktaskpriorityqueue = this.queues.get(processorhandle);
      if (chunktaskpriorityqueue == null) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("No queue for: " + processorhandle));
      } else {
         return chunktaskpriorityqueue;
      }
   }

   @VisibleForTesting
   public String getDebugStatus() {
      return (String)this.queues.entrySet().stream().map((map_entry) -> map_entry.getKey().name() + "=[" + (String)map_entry.getValue().getAcquired().stream().map((olong) -> olong + ":" + new ChunkPos(olong)).collect(Collectors.joining(",")) + "]").collect(Collectors.joining(",")) + ", s=" + this.sleeping.size();
   }

   public void close() {
      this.queues.keySet().forEach(ProcessorHandle::close);
   }

   public static final class Message<T> {
      final Function<ProcessorHandle<Unit>, T> task;
      final long pos;
      final IntSupplier level;

      Message(Function<ProcessorHandle<Unit>, T> function, long i, IntSupplier intsupplier) {
         this.task = function;
         this.pos = i;
         this.level = intsupplier;
      }
   }

   public static final class Release {
      final Runnable task;
      final long pos;
      final boolean clearQueue;

      Release(Runnable runnable, long i, boolean flag) {
         this.task = runnable;
         this.pos = i;
         this.clearQueue = flag;
      }
   }
}
