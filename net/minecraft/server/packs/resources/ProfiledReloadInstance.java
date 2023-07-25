package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import org.slf4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Stopwatch total = Stopwatch.createUnstarted();

   public ProfiledReloadInstance(ResourceManager resourcemanager, List<PreparableReloadListener> list, Executor executor, Executor executor1, CompletableFuture<Unit> completablefuture) {
      super(executor, executor1, resourcemanager, list, (preparablereloadlistener_preparationbarrier, resourcemanager1, preparablereloadlistener, executor3, executor4) -> {
         AtomicLong atomiclong = new AtomicLong();
         AtomicLong atomiclong1 = new AtomicLong();
         ActiveProfiler activeprofiler = new ActiveProfiler(Util.timeSource, () -> 0, false);
         ActiveProfiler activeprofiler1 = new ActiveProfiler(Util.timeSource, () -> 0, false);
         CompletableFuture<Void> completablefuture1 = preparablereloadlistener.reload(preparablereloadlistener_preparationbarrier, resourcemanager1, activeprofiler, activeprofiler1, (runnable2) -> executor3.execute(() -> {
               long j1 = Util.getNanos();
               runnable2.run();
               atomiclong.addAndGet(Util.getNanos() - j1);
            }), (runnable) -> executor4.execute(() -> {
               long i1 = Util.getNanos();
               runnable.run();
               atomiclong1.addAndGet(Util.getNanos() - i1);
            }));
         return completablefuture1.thenApplyAsync((ovoid) -> {
            LOGGER.debug("Finished reloading " + preparablereloadlistener.getName());
            return new ProfiledReloadInstance.State(preparablereloadlistener.getName(), activeprofiler.getResults(), activeprofiler1.getResults(), atomiclong, atomiclong1);
         }, executor1);
      }, completablefuture);
      this.total.start();
      this.allDone = this.allDone.thenApplyAsync(this::finish, executor1);
   }

   private List<ProfiledReloadInstance.State> finish(List<ProfiledReloadInstance.State> list1) {
      this.total.stop();
      long i = 0L;
      LOGGER.info("Resource reload finished after {} ms", (long)this.total.elapsed(TimeUnit.MILLISECONDS));

      for(ProfiledReloadInstance.State profiledreloadinstance_state : list1) {
         ProfileResults profileresults = profiledreloadinstance_state.preparationResult;
         ProfileResults profileresults1 = profiledreloadinstance_state.reloadResult;
         long j = TimeUnit.NANOSECONDS.toMillis(profiledreloadinstance_state.preparationNanos.get());
         long k = TimeUnit.NANOSECONDS.toMillis(profiledreloadinstance_state.reloadNanos.get());
         long l = j + k;
         String s = profiledreloadinstance_state.name;
         LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", s, l, j, k);
         i += k;
      }

      LOGGER.info("Total blocking time: {} ms", (long)i);
      return list1;
   }

   public static class State {
      final String name;
      final ProfileResults preparationResult;
      final ProfileResults reloadResult;
      final AtomicLong preparationNanos;
      final AtomicLong reloadNanos;

      State(String s, ProfileResults profileresults, ProfileResults profileresults1, AtomicLong atomiclong, AtomicLong atomiclong1) {
         this.name = s;
         this.preparationResult = profileresults;
         this.reloadResult = profileresults1;
         this.preparationNanos = atomiclong;
         this.reloadNanos = atomiclong1;
      }
   }
}
