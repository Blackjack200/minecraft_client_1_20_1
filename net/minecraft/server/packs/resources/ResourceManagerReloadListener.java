package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ResourceManagerReloadListener extends PreparableReloadListener {
   default CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      return preparablereloadlistener_preparationbarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
         profilerfiller1.startTick();
         profilerfiller1.push("listener");
         this.onResourceManagerReload(resourcemanager);
         profilerfiller1.pop();
         profilerfiller1.endTick();
      }, executor1);
   }

   void onResourceManagerReload(ResourceManager resourcemanager);
}
