package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Holder<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;
   private final boolean allowInline;

   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec) {
      return create(resourcekey, codec, true);
   }

   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec, boolean flag) {
      return new RegistryFileCodec<>(resourcekey, codec, flag);
   }

   private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec, boolean flag) {
      this.registryKey = resourcekey;
      this.elementCodec = codec;
      this.allowInline = flag;
   }

   public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<?> registryops) {
         Optional<HolderOwner<E>> optional = registryops.owner(this.registryKey);
         if (optional.isPresent()) {
            if (!holder.canSerializeIn(optional.get())) {
               return DataResult.error(() -> "Element " + holder + " is not valid in current registry set");
            }

            return holder.unwrap().map((resourcekey) -> ResourceLocation.CODEC.encode(resourcekey.location(), dynamicops, object), (object2) -> this.elementCodec.encode(object2, dynamicops, object));
         }
      }

      return this.elementCodec.encode(holder.value(), dynamicops, object);
   }

   public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<?> registryops) {
         Optional<HolderGetter<E>> optional = registryops.getter(this.registryKey);
         if (optional.isEmpty()) {
            return DataResult.error(() -> "Registry does not exist: " + this.registryKey);
         } else {
            HolderGetter<E> holdergetter = optional.get();
            DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(dynamicops, object);
            if (dataresult.result().isEmpty()) {
               return !this.allowInline ? DataResult.error(() -> "Inline definitions not allowed here") : this.elementCodec.decode(dynamicops, object).map((pair3) -> pair3.mapFirst(Holder::direct));
            } else {
               Pair<ResourceLocation, T> pair = dataresult.result().get();
               ResourceKey<E> resourcekey = ResourceKey.create(this.registryKey, pair.getFirst());
               return holdergetter.get(resourcekey).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + resourcekey)).map((holder_reference) -> Pair.of(holder_reference, pair.getSecond())).setLifecycle(Lifecycle.stable());
            }
         }
      } else {
         return this.elementCodec.decode(dynamicops, object).map((pair1) -> pair1.mapFirst(Holder::direct));
      }
   }

   public String toString() {
      return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
   }
}
