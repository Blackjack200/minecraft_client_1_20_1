package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, IdMap<T> {
   ResourceKey<? extends Registry<T>> key();

   default Codec<T> byNameCodec() {
      Codec<T> codec = ResourceLocation.CODEC.flatXmap((resourcelocation) -> Optional.ofNullable(this.get(resourcelocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + resourcelocation)), (object1) -> this.getResourceKey(object1).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.key() + ":" + object1)));
      Codec<T> codec1 = ExtraCodecs.idResolverCodec((object) -> this.getResourceKey(object).isPresent() ? this.getId(object) : -1, this::byId, -1);
      return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec1), this::lifecycle, this::lifecycle);
   }

   default Codec<Holder<T>> holderByNameCodec() {
      Codec<Holder<T>> codec = ResourceLocation.CODEC.flatXmap((resourcelocation) -> this.getHolder(ResourceKey.create(this.key(), resourcelocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + resourcelocation)), (holder2) -> holder2.unwrapKey().map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.key() + ":" + holder2)));
      return ExtraCodecs.overrideLifecycle(codec, (holder1) -> this.lifecycle(holder1.value()), (holder) -> this.lifecycle(holder.value()));
   }

   default <U> Stream<U> keys(DynamicOps<U> dynamicops) {
      return this.keySet().stream().map((resourcelocation) -> dynamicops.createString(resourcelocation.toString()));
   }

   @Nullable
   ResourceLocation getKey(T object);

   Optional<ResourceKey<T>> getResourceKey(T object);

   int getId(@Nullable T object);

   @Nullable
   T get(@Nullable ResourceKey<T> resourcekey);

   @Nullable
   T get(@Nullable ResourceLocation resourcelocation);

   Lifecycle lifecycle(T object);

   Lifecycle registryLifecycle();

   default Optional<T> getOptional(@Nullable ResourceLocation resourcelocation) {
      return Optional.ofNullable(this.get(resourcelocation));
   }

   default Optional<T> getOptional(@Nullable ResourceKey<T> resourcekey) {
      return Optional.ofNullable(this.get(resourcekey));
   }

   default T getOrThrow(ResourceKey<T> resourcekey) {
      T object = this.get(resourcekey);
      if (object == null) {
         throw new IllegalStateException("Missing key in " + this.key() + ": " + resourcekey);
      } else {
         return object;
      }
   }

   Set<ResourceLocation> keySet();

   Set<Map.Entry<ResourceKey<T>, T>> entrySet();

   Set<ResourceKey<T>> registryKeySet();

   Optional<Holder.Reference<T>> getRandom(RandomSource randomsource);

   default Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   boolean containsKey(ResourceLocation resourcelocation);

   boolean containsKey(ResourceKey<T> resourcekey);

   static <T> T register(Registry<? super T> registry, String s, T object) {
      return register(registry, new ResourceLocation(s), object);
   }

   static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourcelocation, T object) {
      return register(registry, ResourceKey.create(registry.key(), resourcelocation), object);
   }

   static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourcekey, T object) {
      ((WritableRegistry)registry).register(resourcekey, (V)object, Lifecycle.stable());
      return object;
   }

   static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceKey<T> resourcekey, T object) {
      return ((WritableRegistry)registry).register(resourcekey, object, Lifecycle.stable());
   }

   static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceLocation resourcelocation, T object) {
      return registerForHolder(registry, ResourceKey.create(registry.key(), resourcelocation), object);
   }

   static <V, T extends V> T registerMapping(Registry<V> registry, int i, String s, T object) {
      ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key(), new ResourceLocation(s)), (V)object, Lifecycle.stable());
      return object;
   }

   Registry<T> freeze();

   Holder.Reference<T> createIntrusiveHolder(T object);

   Optional<Holder.Reference<T>> getHolder(int i);

   Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourcekey);

   Holder<T> wrapAsHolder(T object);

   default Holder.Reference<T> getHolderOrThrow(ResourceKey<T> resourcekey) {
      return this.getHolder(resourcekey).orElseThrow(() -> new IllegalStateException("Missing key in " + this.key() + ": " + resourcekey));
   }

   Stream<Holder.Reference<T>> holders();

   Optional<HolderSet.Named<T>> getTag(TagKey<T> tagkey);

   default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagkey) {
      return DataFixUtils.orElse(this.getTag(tagkey), List.<T>of());
   }

   HolderSet.Named<T> getOrCreateTag(TagKey<T> tagkey);

   Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

   Stream<TagKey<T>> getTagNames();

   void resetTags();

   void bindTags(Map<TagKey<T>, List<Holder<T>>> map);

   default IdMap<Holder<T>> asHolderIdMap() {
      return new IdMap<Holder<T>>() {
         public int getId(Holder<T> holder) {
            return Registry.this.getId(holder.value());
         }

         @Nullable
         public Holder<T> byId(int i) {
            return (Holder)Registry.this.getHolder(i).orElse((T)null);
         }

         public int size() {
            return Registry.this.size();
         }

         public Iterator<Holder<T>> iterator() {
            return Registry.this.holders().map((holder_reference) -> holder_reference).iterator();
         }
      };
   }

   HolderOwner<T> holderOwner();

   HolderLookup.RegistryLookup<T> asLookup();

   default HolderLookup.RegistryLookup<T> asTagAddingLookup() {
      return new HolderLookup.RegistryLookup.Delegate<T>() {
         protected HolderLookup.RegistryLookup<T> parent() {
            return Registry.this.asLookup();
         }

         public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
            return Optional.of(this.getOrThrow(tagkey));
         }

         public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
            return Registry.this.getOrCreateTag(tagkey);
         }
      };
   }
}
