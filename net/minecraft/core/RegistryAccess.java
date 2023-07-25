package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess extends HolderLookup.Provider {
   Logger LOGGER = LogUtils.getLogger();
   RegistryAccess.Frozen EMPTY = (new RegistryAccess.ImmutableRegistryAccess(Map.of())).freeze();

   <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourcekey);

   default <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
      return this.registry(resourcekey).map(Registry::asLookup);
   }

   default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourcekey) {
      return this.registry(resourcekey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourcekey));
   }

   Stream<RegistryAccess.RegistryEntry<?>> registries();

   static RegistryAccess.Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> registry) {
      return new RegistryAccess.Frozen() {
         public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> resourcekey) {
            Registry<Registry<T>> registry = registry;
            return registry.getOptional(resourcekey);
         }

         public Stream<RegistryAccess.RegistryEntry<?>> registries() {
            return registry.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
         }

         public RegistryAccess.Frozen freeze() {
            return this;
         }
      };
   }

   default RegistryAccess.Frozen freeze() {
      class FrozenAccess extends RegistryAccess.ImmutableRegistryAccess implements RegistryAccess.Frozen {
         protected FrozenAccess(Stream<RegistryAccess.RegistryEntry<?>> stream) {
            super(stream);
         }
      }

      return new FrozenAccess(this.registries().map(RegistryAccess.RegistryEntry::freeze));
   }

   default Lifecycle allRegistriesLifecycle() {
      return this.registries().map((registryaccess_registryentry) -> registryaccess_registryentry.value.registryLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
   }

   public interface Frozen extends RegistryAccess {
   }

   public static class ImmutableRegistryAccess implements RegistryAccess {
      private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

      public ImmutableRegistryAccess(List<? extends Registry<?>> list) {
         this.registries = list.stream().collect(Collectors.toUnmodifiableMap(Registry::key, (registry) -> registry));
      }

      public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
         this.registries = Map.copyOf(map);
      }

      public ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> stream) {
         this.registries = stream.collect(ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value));
      }

      public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourcekey) {
         return Optional.ofNullable(this.registries.get(resourcekey)).map((registry) -> registry);
      }

      public Stream<RegistryAccess.RegistryEntry<?>> registries() {
         return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
      }
   }

   public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
      final Registry<T> value;

      private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> map_entry) {
         return fromUntyped(map_entry.getKey(), map_entry.getValue());
      }

      private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourcekey, Registry<?> registry) {
         return new RegistryAccess.RegistryEntry<>(resourcekey, registry);
      }

      private RegistryAccess.RegistryEntry<T> freeze() {
         return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
      }
   }
}
