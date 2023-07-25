package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final PackOutput.PathProvider pathProvider;
   private final CompletableFuture<HolderLookup.Provider> lookupProvider;
   private final CompletableFuture<Void> contentsDone = new CompletableFuture<>();
   private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
   protected final ResourceKey<? extends Registry<T>> registryKey;
   private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

   protected TagsProvider(PackOutput packoutput, ResourceKey<? extends Registry<T>> resourcekey, CompletableFuture<HolderLookup.Provider> completablefuture) {
      this(packoutput, resourcekey, completablefuture, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()));
   }

   protected TagsProvider(PackOutput packoutput, ResourceKey<? extends Registry<T>> resourcekey, CompletableFuture<HolderLookup.Provider> completablefuture, CompletableFuture<TagsProvider.TagLookup<T>> completablefuture1) {
      this.pathProvider = packoutput.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(resourcekey));
      this.registryKey = resourcekey;
      this.parentProvider = completablefuture1;
      this.lookupProvider = completablefuture;
   }

   public final String getName() {
      return "Tags for " + this.registryKey.location();
   }

   protected abstract void addTags(HolderLookup.Provider holderlookup_provider);

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      return this.createContentsProvider().thenApply((holderlookup_provider1) -> {
         this.contentsDone.complete((Void)null);
         return holderlookup_provider1;
      }).thenCombineAsync(this.parentProvider, (holderlookup_provider, tagsprovider_taglookup) -> {
         record CombinedData<T>(HolderLookup.Provider contents, TagsProvider.TagLookup<T> parent) {
            final HolderLookup.Provider contents;
            final TagsProvider.TagLookup<T> parent;
         }

         return new CombinedData<>(holderlookup_provider, tagsprovider_taglookup);
      }).thenCompose((tagsprovider_1combineddata) -> {
         HolderLookup.RegistryLookup<T> holderlookup_registrylookup = tagsprovider_1combineddata.contents.lookupOrThrow(this.registryKey);
         Predicate<ResourceLocation> predicate = (resourcelocation2) -> holderlookup_registrylookup.get(ResourceKey.create(this.registryKey, resourcelocation2)).isPresent();
         Predicate<ResourceLocation> predicate1 = (resourcelocation1) -> this.builders.containsKey(resourcelocation1) || tagsprovider_1combineddata.parent.contains(TagKey.create(this.registryKey, resourcelocation1));
         return CompletableFuture.allOf(this.builders.entrySet().stream().map((map_entry) -> {
            ResourceLocation resourcelocation = map_entry.getKey();
            TagBuilder tagbuilder = map_entry.getValue();
            List<TagEntry> list = tagbuilder.build();
            List<TagEntry> list1 = list.stream().filter((tagentry) -> !tagentry.verifyIfPresent(predicate, predicate1)).toList();
            if (!list1.isEmpty()) {
               throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", resourcelocation, list1.stream().map(Objects::toString).collect(Collectors.joining(","))));
            } else {
               JsonElement jsonelement = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false)).getOrThrow(false, LOGGER::error);
               Path path = this.pathProvider.json(resourcelocation);
               return DataProvider.saveStable(cachedoutput, jsonelement, path);
            }
         }).toArray((i) -> new CompletableFuture[i]));
      });
   }

   protected TagsProvider.TagAppender<T> tag(TagKey<T> tagkey) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(tagkey);
      return new TagsProvider.TagAppender<>(tagbuilder);
   }

   protected TagBuilder getOrCreateRawBuilder(TagKey<T> tagkey) {
      return this.builders.computeIfAbsent(tagkey.location(), (resourcelocation) -> TagBuilder.create());
   }

   public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
      return this.contentsDone.thenApply((ovoid) -> (tagkey) -> Optional.ofNullable(this.builders.get(tagkey.location())));
   }

   protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
      return this.lookupProvider.thenApply((holderlookup_provider) -> {
         this.builders.clear();
         this.addTags(holderlookup_provider);
         return holderlookup_provider;
      });
   }

   protected static class TagAppender<T> {
      private final TagBuilder builder;

      protected TagAppender(TagBuilder tagbuilder) {
         this.builder = tagbuilder;
      }

      public final TagsProvider.TagAppender<T> add(ResourceKey<T> resourcekey) {
         this.builder.addElement(resourcekey.location());
         return this;
      }

      @SafeVarargs
      public final TagsProvider.TagAppender<T> add(ResourceKey<T>... aresourcekey) {
         for(ResourceKey<T> resourcekey : aresourcekey) {
            this.builder.addElement(resourcekey.location());
         }

         return this;
      }

      public TagsProvider.TagAppender<T> addOptional(ResourceLocation resourcelocation) {
         this.builder.addOptionalElement(resourcelocation);
         return this;
      }

      public TagsProvider.TagAppender<T> addTag(TagKey<T> tagkey) {
         this.builder.addTag(tagkey.location());
         return this;
      }

      public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation resourcelocation) {
         this.builder.addOptionalTag(resourcelocation);
         return this;
      }
   }

   @FunctionalInterface
   public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
      static <T> TagsProvider.TagLookup<T> empty() {
         return (tagkey) -> Optional.empty();
      }

      default boolean contains(TagKey<T> tagkey) {
         return this.apply((T)tagkey).isPresent();
      }
   }
}
