package net.minecraft.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderGetter<T> {
   Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey);

   default Holder.Reference<T> getOrThrow(ResourceKey<T> resourcekey) {
      return this.get(resourcekey).orElseThrow(() -> new IllegalStateException("Missing element " + resourcekey));
   }

   Optional<HolderSet.Named<T>> get(TagKey<T> tagkey);

   default HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
      return this.get(tagkey).orElseThrow(() -> new IllegalStateException("Missing tag " + tagkey));
   }

   public interface Provider {
      <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey);

      default <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourcekey) {
         return this.<T>lookup(resourcekey).orElseThrow(() -> new IllegalStateException("Registry " + resourcekey.location() + " not found"));
      }
   }
}
