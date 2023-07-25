package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
   private final PackType type;
   private final String namespace;

   public FallbackResourceManager(PackType packtype, String s) {
      this.type = packtype;
      this.namespace = s;
   }

   public void push(PackResources packresources) {
      this.pushInternal(packresources.packId(), packresources, (Predicate<ResourceLocation>)null);
   }

   public void push(PackResources packresources, Predicate<ResourceLocation> predicate) {
      this.pushInternal(packresources.packId(), packresources, predicate);
   }

   public void pushFilterOnly(String s, Predicate<ResourceLocation> predicate) {
      this.pushInternal(s, (PackResources)null, predicate);
   }

   private void pushInternal(String s, @Nullable PackResources packresources, @Nullable Predicate<ResourceLocation> predicate) {
      this.fallbacks.add(new FallbackResourceManager.PackEntry(s, packresources, predicate));
   }

   public Set<String> getNamespaces() {
      return ImmutableSet.of(this.namespace);
   }

   public Optional<Resource> getResource(ResourceLocation resourcelocation) {
      for(int i = this.fallbacks.size() - 1; i >= 0; --i) {
         FallbackResourceManager.PackEntry fallbackresourcemanager_packentry = this.fallbacks.get(i);
         PackResources packresources = fallbackresourcemanager_packentry.resources;
         if (packresources != null) {
            IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, resourcelocation);
            if (iosupplier != null) {
               IoSupplier<ResourceMetadata> iosupplier1 = this.createStackMetadataFinder(resourcelocation, i);
               return Optional.of(createResource(packresources, resourcelocation, iosupplier, iosupplier1));
            }
         }

         if (fallbackresourcemanager_packentry.isFiltered(resourcelocation)) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", resourcelocation, fallbackresourcemanager_packentry.name);
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static Resource createResource(PackResources packresources, ResourceLocation resourcelocation, IoSupplier<InputStream> iosupplier, IoSupplier<ResourceMetadata> iosupplier1) {
      return new Resource(packresources, wrapForDebug(resourcelocation, packresources, iosupplier), iosupplier1);
   }

   private static IoSupplier<InputStream> wrapForDebug(ResourceLocation resourcelocation, PackResources packresources, IoSupplier<InputStream> iosupplier) {
      return LOGGER.isDebugEnabled() ? () -> new FallbackResourceManager.LeakedResourceWarningInputStream(iosupplier.get(), resourcelocation, packresources.packId()) : iosupplier;
   }

   public List<Resource> getResourceStack(ResourceLocation resourcelocation) {
      ResourceLocation resourcelocation1 = getMetadataLocation(resourcelocation);
      List<Resource> list = new ArrayList<>();
      boolean flag = false;
      String s = null;

      for(int i = this.fallbacks.size() - 1; i >= 0; --i) {
         FallbackResourceManager.PackEntry fallbackresourcemanager_packentry = this.fallbacks.get(i);
         PackResources packresources = fallbackresourcemanager_packentry.resources;
         if (packresources != null) {
            IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, resourcelocation);
            if (iosupplier != null) {
               IoSupplier<ResourceMetadata> iosupplier1;
               if (flag) {
                  iosupplier1 = ResourceMetadata.EMPTY_SUPPLIER;
               } else {
                  iosupplier1 = () -> {
                     IoSupplier<InputStream> iosupplier3 = packresources.getResource(this.type, resourcelocation1);
                     return iosupplier3 != null ? parseMetadata(iosupplier3) : ResourceMetadata.EMPTY;
                  };
               }

               list.add(new Resource(packresources, iosupplier, iosupplier1));
            }
         }

         if (fallbackresourcemanager_packentry.isFiltered(resourcelocation)) {
            s = fallbackresourcemanager_packentry.name;
            break;
         }

         if (fallbackresourcemanager_packentry.isFiltered(resourcelocation1)) {
            flag = true;
         }
      }

      if (list.isEmpty() && s != null) {
         LOGGER.warn("Resource {} not found, but was filtered by pack {}", resourcelocation, s);
      }

      return Lists.reverse(list);
   }

   private static boolean isMetadata(ResourceLocation resourcelocation) {
      return resourcelocation.getPath().endsWith(".mcmeta");
   }

   private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation resourcelocation) {
      String s = resourcelocation.getPath().substring(0, resourcelocation.getPath().length() - ".mcmeta".length());
      return resourcelocation.withPath(s);
   }

   static ResourceLocation getMetadataLocation(ResourceLocation resourcelocation) {
      return resourcelocation.withPath(resourcelocation.getPath() + ".mcmeta");
   }

   public Map<ResourceLocation, Resource> listResources(String s, Predicate<ResourceLocation> predicate) {
      Map<ResourceLocation, ResourceWithSourceAndIndex> map = new HashMap<>();
      Map<ResourceLocation, ResourceWithSourceAndIndex> map1 = new HashMap<>();
      int i = this.fallbacks.size();

      for(int j = 0; j < i; ++j) {
         FallbackResourceManager.PackEntry fallbackresourcemanager_packentry = this.fallbacks.get(j);
         fallbackresourcemanager_packentry.filterAll(map.keySet());
         fallbackresourcemanager_packentry.filterAll(map1.keySet());
         PackResources packresources = fallbackresourcemanager_packentry.resources;
         if (packresources != null) {
            int k = j;
            packresources.listResources(this.type, this.namespace, s, (resourcelocation2, iosupplier2) -> {
               record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
                  final PackResources packResources;
                  final IoSupplier<InputStream> resource;
                  final int packIndex;
               }

               if (isMetadata(resourcelocation2)) {
                  if (predicate.test(getResourceLocationFromMetadata(resourcelocation2))) {
                     map1.put(resourcelocation2, new ResourceWithSourceAndIndex(packresources, iosupplier2, k));
                  }
               } else if (predicate.test(resourcelocation2)) {
                  map.put(resourcelocation2, new ResourceWithSourceAndIndex(packresources, iosupplier2, k));
               }

            });
         }
      }

      Map<ResourceLocation, Resource> map2 = Maps.newTreeMap();
      map.forEach((resourcelocation, fallbackresourcemanager_1resourcewithsourceandindex) -> {
         ResourceLocation resourcelocation1 = getMetadataLocation(resourcelocation);
         ResourceWithSourceAndIndex fallbackresourcemanager_1resourcewithsourceandindex1 = map1.get(resourcelocation1);
         IoSupplier<ResourceMetadata> iosupplier;
         if (fallbackresourcemanager_1resourcewithsourceandindex1 != null && fallbackresourcemanager_1resourcewithsourceandindex1.packIndex >= fallbackresourcemanager_1resourcewithsourceandindex.packIndex) {
            iosupplier = convertToMetadata(fallbackresourcemanager_1resourcewithsourceandindex1.resource);
         } else {
            iosupplier = ResourceMetadata.EMPTY_SUPPLIER;
         }

         map2.put(resourcelocation, createResource(fallbackresourcemanager_1resourcewithsourceandindex.packResources, resourcelocation, fallbackresourcemanager_1resourcewithsourceandindex.resource, iosupplier));
      });
      return map2;
   }

   private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourcelocation, int i) {
      return () -> {
         ResourceLocation resourcelocation2 = getMetadataLocation(resourcelocation);

         for(int k = this.fallbacks.size() - 1; k >= i; --k) {
            FallbackResourceManager.PackEntry fallbackresourcemanager_packentry = this.fallbacks.get(k);
            PackResources packresources = fallbackresourcemanager_packentry.resources;
            if (packresources != null) {
               IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, resourcelocation2);
               if (iosupplier != null) {
                  return parseMetadata(iosupplier);
               }
            }

            if (fallbackresourcemanager_packentry.isFiltered(resourcelocation2)) {
               break;
            }
         }

         return ResourceMetadata.EMPTY;
      };
   }

   private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> iosupplier) {
      return () -> parseMetadata(iosupplier);
   }

   private static ResourceMetadata parseMetadata(IoSupplier<InputStream> iosupplier) throws IOException {
      InputStream inputstream = iosupplier.get();

      ResourceMetadata var2;
      try {
         var2 = ResourceMetadata.fromJsonStream(inputstream);
      } catch (Throwable var5) {
         if (inputstream != null) {
            try {
               inputstream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (inputstream != null) {
         inputstream.close();
      }

      return var2;
   }

   private static void applyPackFiltersToExistingResources(FallbackResourceManager.PackEntry fallbackresourcemanager_packentry, Map<ResourceLocation, FallbackResourceManager.EntryStack> map) {
      for(FallbackResourceManager.EntryStack fallbackresourcemanager_entrystack : map.values()) {
         if (fallbackresourcemanager_packentry.isFiltered(fallbackresourcemanager_entrystack.fileLocation)) {
            fallbackresourcemanager_entrystack.fileSources.clear();
         } else if (fallbackresourcemanager_packentry.isFiltered(fallbackresourcemanager_entrystack.metadataLocation())) {
            fallbackresourcemanager_entrystack.metaSources.clear();
         }
      }

   }

   private void listPackResources(FallbackResourceManager.PackEntry fallbackresourcemanager_packentry, String s, Predicate<ResourceLocation> predicate, Map<ResourceLocation, FallbackResourceManager.EntryStack> map) {
      PackResources packresources = fallbackresourcemanager_packentry.resources;
      if (packresources != null) {
         packresources.listResources(this.type, this.namespace, s, (resourcelocation, iosupplier) -> {
            if (isMetadata(resourcelocation)) {
               ResourceLocation resourcelocation1 = getResourceLocationFromMetadata(resourcelocation);
               if (!predicate.test(resourcelocation1)) {
                  return;
               }

               (map.computeIfAbsent(resourcelocation1, FallbackResourceManager.EntryStack::new)).metaSources.put(packresources, iosupplier);
            } else {
               if (!predicate.test(resourcelocation)) {
                  return;
               }

               (map.computeIfAbsent(resourcelocation, FallbackResourceManager.EntryStack::new)).fileSources.add(new FallbackResourceManager.ResourceWithSource(packresources, iosupplier));
            }

         });
      }
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String s, Predicate<ResourceLocation> predicate) {
      Map<ResourceLocation, FallbackResourceManager.EntryStack> map = Maps.newHashMap();

      for(FallbackResourceManager.PackEntry fallbackresourcemanager_packentry : this.fallbacks) {
         applyPackFiltersToExistingResources(fallbackresourcemanager_packentry, map);
         this.listPackResources(fallbackresourcemanager_packentry, s, predicate, map);
      }

      TreeMap<ResourceLocation, List<Resource>> treemap = Maps.newTreeMap();

      for(FallbackResourceManager.EntryStack fallbackresourcemanager_entrystack : map.values()) {
         if (!fallbackresourcemanager_entrystack.fileSources.isEmpty()) {
            List<Resource> list = new ArrayList<>();

            for(FallbackResourceManager.ResourceWithSource fallbackresourcemanager_resourcewithsource : fallbackresourcemanager_entrystack.fileSources) {
               PackResources packresources = fallbackresourcemanager_resourcewithsource.source;
               IoSupplier<InputStream> iosupplier = fallbackresourcemanager_entrystack.metaSources.get(packresources);
               IoSupplier<ResourceMetadata> iosupplier1 = iosupplier != null ? convertToMetadata(iosupplier) : ResourceMetadata.EMPTY_SUPPLIER;
               list.add(createResource(packresources, fallbackresourcemanager_entrystack.fileLocation, fallbackresourcemanager_resourcewithsource.resource, iosupplier1));
            }

            treemap.put(fallbackresourcemanager_entrystack.fileLocation, list);
         }
      }

      return treemap;
   }

   public Stream<PackResources> listPacks() {
      return this.fallbacks.stream().map((fallbackresourcemanager_packentry) -> fallbackresourcemanager_packentry.resources).filter(Objects::nonNull);
   }

   static record EntryStack(ResourceLocation fileLocation, ResourceLocation metadataLocation, List<FallbackResourceManager.ResourceWithSource> fileSources, Map<PackResources, IoSupplier<InputStream>> metaSources) {
      final ResourceLocation fileLocation;
      final List<FallbackResourceManager.ResourceWithSource> fileSources;
      final Map<PackResources, IoSupplier<InputStream>> metaSources;

      EntryStack(ResourceLocation resourcelocation) {
         this(resourcelocation, FallbackResourceManager.getMetadataLocation(resourcelocation), new ArrayList<>(), new Object2ObjectArrayMap<>());
      }
   }

   static class LeakedResourceWarningInputStream extends FilterInputStream {
      private final Supplier<String> message;
      private boolean closed;

      public LeakedResourceWarningInputStream(InputStream inputstream, ResourceLocation resourcelocation, String s) {
         super(inputstream);
         Exception exception = new Exception("Stacktrace");
         this.message = () -> {
            StringWriter stringwriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringwriter));
            return "Leaked resource: '" + resourcelocation + "' loaded from pack: '" + s + "'\n" + stringwriter;
         };
      }

      public void close() throws IOException {
         super.close();
         this.closed = true;
      }

      protected void finalize() throws Throwable {
         if (!this.closed) {
            FallbackResourceManager.LOGGER.warn("{}", this.message.get());
         }

         super.finalize();
      }
   }

   static record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {
      final String name;
      @Nullable
      final PackResources resources;

      public void filterAll(Collection<ResourceLocation> collection) {
         if (this.filter != null) {
            collection.removeIf(this.filter);
         }

      }

      public boolean isFiltered(ResourceLocation resourcelocation) {
         return this.filter != null && this.filter.test(resourcelocation);
      }
   }

   static record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
      final PackResources source;
      final IoSupplier<InputStream> resource;
   }
}
