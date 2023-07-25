package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      return CompletableFuture.supplyAsync(() -> this.prepare(resourcemanager, profilerfiller), executor).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((object) -> this.apply(object, resourcemanager, profilerfiller1), executor1);
   }

   protected abstract T prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller);

   protected abstract void apply(T object, ResourceManager resourcemanager, ProfilerFiller profilerfiller);
}
