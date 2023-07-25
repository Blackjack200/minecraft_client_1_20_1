package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ResourceKey<T> {
   private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = (new MapMaker()).weakValues().makeMap();
   private final ResourceLocation registryName;
   private final ResourceLocation location;

   public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> resourcekey) {
      return ResourceLocation.CODEC.xmap((resourcelocation) -> create(resourcekey, resourcelocation), ResourceKey::location);
   }

   public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> resourcekey, ResourceLocation resourcelocation) {
      return create(resourcekey.location, resourcelocation);
   }

   public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation resourcelocation) {
      return create(BuiltInRegistries.ROOT_REGISTRY_NAME, resourcelocation);
   }

   private static <T> ResourceKey<T> create(ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      return VALUES.computeIfAbsent(new ResourceKey.InternKey(resourcelocation, resourcelocation1), (resourcekey_internkey) -> new ResourceKey(resourcekey_internkey.registry, resourcekey_internkey.location));
   }

   private ResourceKey(ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      this.registryName = resourcelocation;
      this.location = resourcelocation1;
   }

   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.location + "]";
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> resourcekey) {
      return this.registryName.equals(resourcekey.location());
   }

   public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
      return this.isFor(resourcekey) ? Optional.of(this) : Optional.empty();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public ResourceLocation registry() {
      return this.registryName;
   }

   static record InternKey(ResourceLocation registry, ResourceLocation location) {
      final ResourceLocation registry;
      final ResourceLocation location;
   }
}
