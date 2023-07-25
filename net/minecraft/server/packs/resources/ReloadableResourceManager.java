package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class ReloadableResourceManager implements ResourceManager, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private CloseableResourceManager resources;
   private final List<PreparableReloadListener> listeners = Lists.newArrayList();
   private final PackType type;

   public ReloadableResourceManager(PackType packtype) {
      this.type = packtype;
      this.resources = new MultiPackResourceManager(packtype, List.of());
   }

   public void close() {
      this.resources.close();
   }

   public void registerReloadListener(PreparableReloadListener preparablereloadlistener) {
      this.listeners.add(preparablereloadlistener);
   }

   public ReloadInstance createReload(Executor executor, Executor executor1, CompletableFuture<Unit> completablefuture, List<PackResources> list) {
      LOGGER.info("Reloading ResourceManager: {}", LogUtils.defer(() -> list.stream().map(PackResources::packId).collect(Collectors.joining(", "))));
      this.resources.close();
      this.resources = new MultiPackResourceManager(this.type, list);
      return SimpleReloadInstance.create(this.resources, this.listeners, executor, executor1, completablefuture, LOGGER.isDebugEnabled());
   }

   public Optional<Resource> getResource(ResourceLocation resourcelocation) {
      return this.resources.getResource(resourcelocation);
   }

   public Set<String> getNamespaces() {
      return this.resources.getNamespaces();
   }

   public List<Resource> getResourceStack(ResourceLocation resourcelocation) {
      return this.resources.getResourceStack(resourcelocation);
   }

   public Map<ResourceLocation, Resource> listResources(String s, Predicate<ResourceLocation> predicate) {
      return this.resources.listResources(s, predicate);
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String s, Predicate<ResourceLocation> predicate) {
      return this.resources.listResourceStacks(s, predicate);
   }

   public Stream<PackResources> listPacks() {
      return this.resources.listPacks();
   }
}
