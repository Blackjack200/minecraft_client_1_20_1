package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagLoader<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Function<ResourceLocation, Optional<? extends T>> idToValue;
   private final String directory;

   public TagLoader(Function<ResourceLocation, Optional<? extends T>> function, String s) {
      this.idToValue = function;
      this.directory = s;
   }

   public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager resourcemanager) {
      Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = Maps.newHashMap();
      FileToIdConverter filetoidconverter = FileToIdConverter.json(this.directory);

      for(Map.Entry<ResourceLocation, List<Resource>> map_entry : filetoidconverter.listMatchingResourceStacks(resourcemanager).entrySet()) {
         ResourceLocation resourcelocation = map_entry.getKey();
         ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

         for(Resource resource : map_entry.getValue()) {
            try {
               Reader reader = resource.openAsReader();

               try {
                  JsonElement jsonelement = JsonParser.parseReader(reader);
                  List<TagLoader.EntryWithSource> list = map.computeIfAbsent(resourcelocation1, (resourcelocation2) -> new ArrayList());
                  TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonelement)).getOrThrow(false, LOGGER::error);
                  if (tagfile.replace()) {
                     list.clear();
                  }

                  String s = resource.sourcePackId();
                  tagfile.entries().forEach((tagentry) -> list.add(new TagLoader.EntryWithSource(tagentry, s)));
               } catch (Throwable var16) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (reader != null) {
                  reader.close();
               }
            } catch (Exception var17) {
               LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourcelocation1, resourcelocation, resource.sourcePackId(), var17);
            }
         }
      }

      return map;
   }

   private Either<Collection<TagLoader.EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> tagentry_lookup, List<TagLoader.EntryWithSource> list) {
      ImmutableSet.Builder<T> immutableset_builder = ImmutableSet.builder();
      List<TagLoader.EntryWithSource> list1 = new ArrayList<>();

      for(TagLoader.EntryWithSource tagloader_entrywithsource : list) {
         if (!tagloader_entrywithsource.entry().build(tagentry_lookup, immutableset_builder::add)) {
            list1.add(tagloader_entrywithsource);
         }
      }

      return list1.isEmpty() ? Either.right(immutableset_builder.build()) : Either.left(list1);
   }

   public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> map) {
      final Map<ResourceLocation, Collection<T>> map1 = Maps.newHashMap();
      TagEntry.Lookup<T> tagentry_lookup = new TagEntry.Lookup<T>() {
         @Nullable
         public T element(ResourceLocation resourcelocation) {
            return TagLoader.this.idToValue.apply(resourcelocation).orElse((T)null);
         }

         @Nullable
         public Collection<T> tag(ResourceLocation resourcelocation) {
            return map1.get(resourcelocation);
         }
      };
      DependencySorter<ResourceLocation, TagLoader.SortingEntry> dependencysorter = new DependencySorter<>();
      map.forEach((resourcelocation3, list) -> dependencysorter.addEntry(resourcelocation3, new TagLoader.SortingEntry(list)));
      dependencysorter.orderByDependencies((resourcelocation, tagloader_sortingentry) -> this.build(tagentry_lookup, tagloader_sortingentry.entries).ifLeft((collection1) -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourcelocation, collection1.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight((collection) -> map1.put(resourcelocation, collection)));
      return map1;
   }

   public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager resourcemanager) {
      return this.build(this.load(resourcemanager));
   }

   public static record EntryWithSource(TagEntry entry, String source) {
      final TagEntry entry;

      public String toString() {
         return this.entry + " (from " + this.source + ")";
      }
   }

   static record SortingEntry(List<TagLoader.EntryWithSource> entries) implements DependencySorter.Entry<ResourceLocation> {
      final List<TagLoader.EntryWithSource> entries;

      public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
         this.entries.forEach((tagloader_entrywithsource) -> tagloader_entrywithsource.entry.visitRequiredDependencies(consumer));
      }

      public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
         this.entries.forEach((tagloader_entrywithsource) -> tagloader_entrywithsource.entry.visitOptionalDependencies(consumer));
      }
   }
}
