package net.minecraft.tags;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class TagManager implements PreparableReloadListener {
   private static final Map<ResourceKey<? extends Registry<?>>, String> CUSTOM_REGISTRY_DIRECTORIES = Map.of(Registries.BLOCK, "tags/blocks", Registries.ENTITY_TYPE, "tags/entity_types", Registries.FLUID, "tags/fluids", Registries.GAME_EVENT, "tags/game_events", Registries.ITEM, "tags/items");
   private final RegistryAccess registryAccess;
   private List<TagManager.LoadResult<?>> results = List.of();

   public TagManager(RegistryAccess registryaccess) {
      this.registryAccess = registryaccess;
   }

   public List<TagManager.LoadResult<?>> getResult() {
      return this.results;
   }

   public static String getTagDir(ResourceKey<? extends Registry<?>> resourcekey) {
      String s = CUSTOM_REGISTRY_DIRECTORIES.get(resourcekey);
      return s != null ? s : "tags/" + resourcekey.location().getPath();
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      List<? extends CompletableFuture<? extends TagManager.LoadResult<?>>> list = this.registryAccess.registries().map((registryaccess_registryentry) -> this.createLoader(resourcemanager, executor, registryaccess_registryentry)).toList();
      return CompletableFuture.allOf(list.toArray((i) -> new CompletableFuture[i])).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((ovoid) -> this.results = list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList()), executor1);
   }

   private <T> CompletableFuture<TagManager.LoadResult<T>> createLoader(ResourceManager resourcemanager, Executor executor, RegistryAccess.RegistryEntry<T> registryaccess_registryentry) {
      ResourceKey<? extends Registry<T>> resourcekey = registryaccess_registryentry.key();
      Registry<T> registry = registryaccess_registryentry.value();
      TagLoader<Holder<T>> tagloader = new TagLoader<>((resourcelocation) -> registry.getHolder(ResourceKey.create(resourcekey, resourcelocation)), getTagDir(resourcekey));
      return CompletableFuture.supplyAsync(() -> new TagManager.LoadResult<>(resourcekey, tagloader.loadAndBuild(resourcemanager)), executor);
   }

   public static record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<ResourceLocation, Collection<Holder<T>>> tags) {
   }
}
