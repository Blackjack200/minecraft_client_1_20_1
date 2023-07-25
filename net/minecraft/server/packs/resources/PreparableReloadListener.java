package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public interface PreparableReloadListener {
   CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1);

   default String getName() {
      return this.getClass().getSimpleName();
   }

   public interface PreparationBarrier {
      <T> CompletableFuture<T> wait(T object);
   }
}
