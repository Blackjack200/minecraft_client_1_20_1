package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
   private static <T> MapCodec<RegistryCodecs.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourcekey, MapCodec<T> mapcodec) {
      return RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceKey.codec(resourcekey).fieldOf("name").forGetter(RegistryCodecs.RegistryEntry::key), Codec.INT.fieldOf("id").forGetter(RegistryCodecs.RegistryEntry::id), mapcodec.forGetter(RegistryCodecs.RegistryEntry::value)).apply(recordcodecbuilder_instance, RegistryCodecs.RegistryEntry::new));
   }

   public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourcekey, Lifecycle lifecycle, Codec<T> codec) {
      return withNameAndId(resourcekey, codec.fieldOf("element")).codec().listOf().xmap((list) -> {
         WritableRegistry<T> writableregistry = new MappedRegistry<>(resourcekey, lifecycle);

         for(RegistryCodecs.RegistryEntry<T> registrycodecs_registryentry : list) {
            writableregistry.registerMapping(registrycodecs_registryentry.id(), registrycodecs_registryentry.key(), registrycodecs_registryentry.value(), lifecycle);
         }

         return writableregistry;
      }, (registry) -> {
         ImmutableList.Builder<RegistryCodecs.RegistryEntry<T>> immutablelist_builder = ImmutableList.builder();

         for(T object : registry) {
            immutablelist_builder.add(new RegistryCodecs.RegistryEntry<>(registry.getResourceKey(object).get(), registry.getId(object), object));
         }

         return immutablelist_builder.build();
      });
   }

   public static <E> Codec<Registry<E>> fullCodec(ResourceKey<? extends Registry<E>> resourcekey, Lifecycle lifecycle, Codec<E> codec) {
      Codec<Map<ResourceKey<E>, E>> codec1 = Codec.unboundedMap(ResourceKey.codec(resourcekey), codec);
      return codec1.xmap((map) -> {
         WritableRegistry<E> writableregistry = new MappedRegistry<>(resourcekey, lifecycle);
         map.forEach((resourcekey2, object) -> writableregistry.register(resourcekey2, object, lifecycle));
         return writableregistry.freeze();
      }, (registry) -> ImmutableMap.copyOf(registry.entrySet()));
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec) {
      return homogeneousList(resourcekey, codec, false);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec, boolean flag) {
      return HolderSetCodec.create(resourcekey, RegistryFileCodec.create(resourcekey, codec), flag);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourcekey) {
      return homogeneousList(resourcekey, false);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourcekey, boolean flag) {
      return HolderSetCodec.create(resourcekey, RegistryFixedCodec.create(resourcekey), flag);
   }

   static record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
   }
}
