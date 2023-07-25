package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
   private static final Interner<TagKey<?>> VALUES = Interners.newWeakInterner();

   public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> resourcekey) {
      return ResourceLocation.CODEC.xmap((resourcelocation) -> create(resourcekey, resourcelocation), TagKey::location);
   }

   public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> resourcekey) {
      return Codec.STRING.comapFlatMap((s) -> s.startsWith("#") ? ResourceLocation.read(s.substring(1)).map((resourcelocation) -> create(resourcekey, resourcelocation)) : DataResult.error(() -> "Not a tag id"), (tagkey) -> "#" + tagkey.location);
   }

   public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> resourcekey, ResourceLocation resourcelocation) {
      return VALUES.intern(new TagKey<>(resourcekey, resourcelocation));
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> resourcekey) {
      return this.registry == resourcekey;
   }

   public <E> Optional<TagKey<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
      return this.isFor(resourcekey) ? Optional.of(this) : Optional.empty();
   }

   public String toString() {
      return "TagKey[" + this.registry.location() + " / " + this.location + "]";
   }
}
