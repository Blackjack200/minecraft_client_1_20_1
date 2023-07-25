package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S> implements ReloadInstance {
   private static final int PREPARATION_PROGRESS_WEIGHT = 2;
   private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
   private static final int LISTENER_PROGRESS_WEIGHT = 1;
   protected final CompletableFuture<Unit> allPreparations = new CompletableFuture<>();
   protected CompletableFuture<List<S>> allDone;
   final Set<PreparableReloadListener> preparingListeners;
   private final int listenerCount;
   private int startedReloads;
   private int finishedReloads;
   private final AtomicInteger startedTaskCounter = new AtomicInteger();
   private final AtomicInteger doneTaskCounter = new AtomicInteger();

   public static SimpleReloadInstance<Void> of(ResourceManager resourcemanager, List<PreparableReloadListener> list, Executor executor, Executor executor1, CompletableFuture<Unit> completablefuture) {
      return new SimpleReloadInstance<>(executor, executor1, resourcemanager, list, (preparablereloadlistener_preparationbarrier, resourcemanager1, preparablereloadlistener, executor3, executor4) -> preparablereloadlistener.reload(preparablereloadlistener_preparationbarrier, resourcemanager1, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, executor, executor4), completablefuture);
   }

   protected SimpleReloadInstance(Executor executor, final Executor executor1, ResourceManager resourcemanager, List<PreparableReloadListener> list, SimpleReloadInstance.StateFactory<S> simplereloadinstance_statefactory, CompletableFuture<Unit> completablefuture) {
      this.listenerCount = list.size();
      this.startedTaskCounter.incrementAndGet();
      completablefuture.thenRun(this.doneTaskCounter::incrementAndGet);
      List<CompletableFuture<S>> list1 = Lists.newArrayList();
      CompletableFuture<?> completablefuture1 = completablefuture;
      this.preparingListeners = Sets.newHashSet(list);

      for(final PreparableReloadListener preparablereloadlistener : list) {
         final CompletableFuture<?> completablefuture2 = completablefuture1;
         CompletableFuture<S> completablefuture3 = simplereloadinstance_statefactory.create(new PreparableReloadListener.PreparationBarrier() {
            public <T> CompletableFuture<T> wait(T object) {
               executor1.execute(() -> {
                  SimpleReloadInstance.this.preparingListeners.remove(preparablereloadlistener);
                  if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                     SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                  }

               });
               return SimpleReloadInstance.this.allPreparations.thenCombine(completablefuture2, (unit, object2) -> object);
            }
         }, resourcemanager, preparablereloadlistener, (runnable2) -> {
            this.startedTaskCounter.incrementAndGet();
            executor.execute(() -> {
               runnable2.run();
               this.doneTaskCounter.incrementAndGet();
            });
         }, (runnable) -> {
            ++this.startedReloads;
            executor1.execute(() -> {
               runnable.run();
               ++this.finishedReloads;
            });
         });
         list1.add(completablefuture3);
         completablefuture1 = completablefuture3;
      }

      this.allDone = Util.sequenceFailFast(list1);
   }

   public CompletableFuture<?> done() {
      return this.allDone;
   }

   public float getActualProgress() {
      int i = this.listenerCount - this.preparingListeners.size();
      float f = (float)(this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + i * 1);
      float f1 = (float)(this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1);
      return f / f1;
   }

   public static ReloadInstance create(ResourceManager resourcemanager, List<PreparableReloadListener> list, Executor executor, Executor executor1, CompletableFuture<Unit> completablefuture, boolean flag) {
      return (ReloadInstance)(flag ? new ProfiledReloadInstance(resourcemanager, list, executor, executor1, completablefuture) : of(resourcemanager, list, executor, executor1, completablefuture));
   }

   protected interface StateFactory<S> {
      CompletableFuture<S> create(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, PreparableReloadListener preparablereloadlistener, Executor executor, Executor executor1);
   }
}
