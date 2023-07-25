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

public final class RegistryFixedCodec<E> implements Codec<Holder<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;

   public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> resourcekey) {
      return new RegistryFixedCodec<>(resourcekey);
   }

   private RegistryFixedCodec(ResourceKey<? extends Registry<E>> resourcekey) {
      this.registryKey = resourcekey;
   }

   public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<?> registryops) {
         Optional<HolderOwner<E>> optional = registryops.owner(this.registryKey);
         if (optional.isPresent()) {
            if (!holder.canSerializeIn(optional.get())) {
               return DataResult.error(() -> "Element " + holder + " is not valid in current registry set");
            }

            return holder.unwrap().map((resourcekey) -> ResourceLocation.CODEC.encode(resourcekey.location(), dynamicops, object), (object1) -> DataResult.error(() -> "Elements from registry " + this.registryKey + " can't be serialized to a value"));
         }
      }

      return DataResult.error(() -> "Can't access registry " + this.registryKey);
   }

   public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<?> registryops) {
         Optional<HolderGetter<E>> optional = registryops.getter(this.registryKey);
         if (optional.isPresent()) {
            return ResourceLocation.CODEC.decode(dynamicops, object).flatMap((pair) -> {
               ResourceLocation resourcelocation = pair.getFirst();
               return optional.get().get(ResourceKey.create(this.registryKey, resourcelocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + resourcelocation)).map((holder_reference) -> Pair.of(holder_reference, (T)pair.getSecond())).setLifecycle(Lifecycle.stable());
            });
         }
      }

      return DataResult.error(() -> "Can't access registry " + this.registryKey);
   }

   public String toString() {
      return "RegistryFixedCodec[" + this.registryKey + "]";
   }
}
