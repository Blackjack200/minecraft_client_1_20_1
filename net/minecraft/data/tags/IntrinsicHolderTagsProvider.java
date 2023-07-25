package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T> extends TagsProvider<T> {
   private final Function<T, ResourceKey<T>> keyExtractor;

   public IntrinsicHolderTagsProvider(PackOutput packoutput, ResourceKey<? extends Registry<T>> resourcekey, CompletableFuture<HolderLookup.Provider> completablefuture, Function<T, ResourceKey<T>> function) {
      super(packoutput, resourcekey, completablefuture);
      this.keyExtractor = function;
   }

   public IntrinsicHolderTagsProvider(PackOutput packoutput, ResourceKey<? extends Registry<T>> resourcekey, CompletableFuture<HolderLookup.Provider> completablefuture, CompletableFuture<TagsProvider.TagLookup<T>> completablefuture1, Function<T, ResourceKey<T>> function) {
      super(packoutput, resourcekey, completablefuture, completablefuture1);
      this.keyExtractor = function;
   }

   protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tag(TagKey<T> tagkey) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(tagkey);
      return new IntrinsicHolderTagsProvider.IntrinsicTagAppender<>(tagbuilder, this.keyExtractor);
   }

   protected static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> {
      private final Function<T, ResourceKey<T>> keyExtractor;

      IntrinsicTagAppender(TagBuilder tagbuilder, Function<T, ResourceKey<T>> function) {
         super(tagbuilder);
         this.keyExtractor = function;
      }

      public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> tagkey) {
         super.addTag(tagkey);
         return this;
      }

      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T object) {
         this.add(this.keyExtractor.apply(object));
         return this;
      }

      @SafeVarargs
      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T... aobject) {
         Stream.<T>of(aobject).map(this.keyExtractor).forEach(this::add);
         return this;
      }
   }
}
