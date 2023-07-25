package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
   private final RegistryOps.RegistryInfoLookup lookupProvider;

   private static RegistryOps.RegistryInfoLookup memoizeLookup(final RegistryOps.RegistryInfoLookup registryops_registryinfolookup) {
      return new RegistryOps.RegistryInfoLookup() {
         private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new HashMap<>();

         public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
            return this.lookups.computeIfAbsent(resourcekey, registryops_registryinfolookup::lookup);
         }
      };
   }

   public static <T> RegistryOps<T> create(DynamicOps<T> dynamicops, final HolderLookup.Provider holderlookup_provider) {
      return create(dynamicops, memoizeLookup(new RegistryOps.RegistryInfoLookup() {
         public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourcekey) {
            return holderlookup_provider.lookup(resourcekey).map((holderlookup_registrylookup) -> new RegistryOps.RegistryInfo<>(holderlookup_registrylookup, holderlookup_registrylookup, holderlookup_registrylookup.registryLifecycle()));
         }
      }));
   }

   public static <T> RegistryOps<T> create(DynamicOps<T> dynamicops, RegistryOps.RegistryInfoLookup registryops_registryinfolookup) {
      return new RegistryOps<>(dynamicops, registryops_registryinfolookup);
   }

   private RegistryOps(DynamicOps<T> dynamicops, RegistryOps.RegistryInfoLookup registryops_registryinfolookup) {
      super(dynamicops);
      this.lookupProvider = registryops_registryinfolookup;
   }

   public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> resourcekey) {
      return this.lookupProvider.lookup(resourcekey).map(RegistryOps.RegistryInfo::owner);
   }

   public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> resourcekey) {
      return this.lookupProvider.lookup(resourcekey).map(RegistryOps.RegistryInfo::getter);
   }

   public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> resourcekey) {
      return ExtraCodecs.retrieveContext((dynamicops) -> {
         if (dynamicops instanceof RegistryOps<?> registryops) {
            return registryops.lookupProvider.lookup(resourcekey).map((registryops_registryinfo) -> DataResult.success(registryops_registryinfo.getter(), registryops_registryinfo.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + resourcekey));
         } else {
            return DataResult.error(() -> "Not a registry ops");
         }
      }).forGetter((object) -> null);
   }

   public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> resourcekey) {
      ResourceKey<? extends Registry<E>> resourcekey1 = ResourceKey.createRegistryKey(resourcekey.registry());
      return ExtraCodecs.retrieveContext((dynamicops) -> {
         if (dynamicops instanceof RegistryOps<?> registryops) {
            return registryops.lookupProvider.lookup(resourcekey1).flatMap((registryops_registryinfo) -> registryops_registryinfo.getter().get(resourcekey)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + resourcekey));
         } else {
            return DataResult.error(() -> "Not a registry ops");
         }
      }).forGetter((object) -> null);
   }

   public static record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
   }

   public interface RegistryInfoLookup {
      <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey);
   }
}
