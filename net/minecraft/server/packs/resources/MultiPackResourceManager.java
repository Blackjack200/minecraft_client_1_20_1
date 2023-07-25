package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class MultiPackResourceManager implements CloseableResourceManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, FallbackResourceManager> namespacedManagers;
   private final List<PackResources> packs;

   public MultiPackResourceManager(PackType packtype, List<PackResources> list) {
      this.packs = List.copyOf(list);
      Map<String, FallbackResourceManager> map = new HashMap<>();
      List<String> list1 = list.stream().flatMap((packresources1) -> packresources1.getNamespaces(packtype).stream()).distinct().toList();

      for(PackResources packresources : list) {
         ResourceFilterSection resourcefiltersection = this.getPackFilterSection(packresources);
         Set<String> set = packresources.getNamespaces(packtype);
         Predicate<ResourceLocation> predicate = resourcefiltersection != null ? (resourcelocation) -> resourcefiltersection.isPathFiltered(resourcelocation.getPath()) : null;

         for(String s : list1) {
            boolean flag = set.contains(s);
            boolean flag1 = resourcefiltersection != null && resourcefiltersection.isNamespaceFiltered(s);
            if (flag || flag1) {
               FallbackResourceManager fallbackresourcemanager = map.get(s);
               if (fallbackresourcemanager == null) {
                  fallbackresourcemanager = new FallbackResourceManager(packtype, s);
                  map.put(s, fallbackresourcemanager);
               }

               if (flag && flag1) {
                  fallbackresourcemanager.push(packresources, predicate);
               } else if (flag) {
                  fallbackresourcemanager.push(packresources);
               } else {
                  fallbackresourcemanager.pushFilterOnly(packresources.packId(), predicate);
               }
            }
         }
      }

      this.namespacedManagers = map;
   }

   @Nullable
   private ResourceFilterSection getPackFilterSection(PackResources packresources) {
      try {
         return packresources.getMetadataSection(ResourceFilterSection.TYPE);
      } catch (IOException var3) {
         LOGGER.error("Failed to get filter section from pack {}", (Object)packresources.packId());
         return null;
      }
   }

   public Set<String> getNamespaces() {
      return this.namespacedManagers.keySet();
   }

   public Optional<Resource> getResource(ResourceLocation resourcelocation) {
      ResourceManager resourcemanager = this.namespacedManagers.get(resourcelocation.getNamespace());
      return resourcemanager != null ? resourcemanager.getResource(resourcelocation) : Optional.empty();
   }

   public List<Resource> getResourceStack(ResourceLocation resourcelocation) {
      ResourceManager resourcemanager = this.namespacedManagers.get(resourcelocation.getNamespace());
      return resourcemanager != null ? resourcemanager.getResourceStack(resourcelocation) : List.of();
   }

   public Map<ResourceLocation, Resource> listResources(String s, Predicate<ResourceLocation> predicate) {
      checkTrailingDirectoryPath(s);
      Map<ResourceLocation, Resource> map = new TreeMap<>();

      for(FallbackResourceManager fallbackresourcemanager : this.namespacedManagers.values()) {
         map.putAll(fallbackresourcemanager.listResources(s, predicate));
      }

      return map;
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String s, Predicate<ResourceLocation> predicate) {
      checkTrailingDirectoryPath(s);
      Map<ResourceLocation, List<Resource>> map = new TreeMap<>();

      for(FallbackResourceManager fallbackresourcemanager : this.namespacedManagers.values()) {
         map.putAll(fallbackresourcemanager.listResourceStacks(s, predicate));
      }

      return map;
   }

   private static void checkTrailingDirectoryPath(String s) {
      if (s.endsWith("/")) {
         throw new IllegalArgumentException("Trailing slash in path " + s);
      }
   }

   public Stream<PackResources> listPacks() {
      return this.packs.stream();
   }

   public void close() {
      this.packs.forEach(PackResources::close);
   }
}
