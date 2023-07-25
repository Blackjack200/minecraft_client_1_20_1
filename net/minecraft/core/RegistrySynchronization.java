package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class RegistrySynchronization {
   private static final Map<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> NETWORKABLE_REGISTRIES = Util.make(() -> {
      ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> immutablemap_builder = ImmutableMap.builder();
      put(immutablemap_builder, Registries.BIOME, Biome.NETWORK_CODEC);
      put(immutablemap_builder, Registries.CHAT_TYPE, ChatType.CODEC);
      put(immutablemap_builder, Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC);
      put(immutablemap_builder, Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC);
      put(immutablemap_builder, Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC);
      put(immutablemap_builder, Registries.DAMAGE_TYPE, DamageType.CODEC);
      return immutablemap_builder.build();
   });
   public static final Codec<RegistryAccess> NETWORK_CODEC = makeNetworkCodec();

   private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> immutablemap_builder, ResourceKey<? extends Registry<E>> resourcekey, Codec<E> codec) {
      immutablemap_builder.put(resourcekey, new RegistrySynchronization.NetworkedRegistryData<>(resourcekey, codec));
   }

   private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryaccess) {
      return registryaccess.registries().filter((registryaccess_registryentry) -> NETWORKABLE_REGISTRIES.containsKey(registryaccess_registryentry.key()));
   }

   private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourcekey) {
      return Optional.ofNullable(NETWORKABLE_REGISTRIES.get(resourcekey)).map((registrysynchronization_networkedregistrydata) -> registrysynchronization_networkedregistrydata.networkCodec()).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown or not serializable registry: " + resourcekey));
   }

   private static <E> Codec<RegistryAccess> makeNetworkCodec() {
      Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
      Codec<Registry<E>> codec1 = codec.partialDispatch("type", (registry) -> DataResult.success(registry.key()), (resourcekey) -> getNetworkCodec(resourcekey).map((codec2) -> RegistryCodecs.networkCodec(resourcekey, Lifecycle.experimental(), codec2)));
      UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> unboundedmapcodec = Codec.unboundedMap(codec, codec1);
      return captureMap(unboundedmapcodec);
   }

   private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(UnboundedMapCodec<K, V> unboundedmapcodec) {
      return unboundedmapcodec.xmap(RegistryAccess.ImmutableRegistryAccess::new, (registryaccess) -> ownedNetworkableRegistries(registryaccess).collect(ImmutableMap.toImmutableMap((registryaccess_registryentry1) -> registryaccess_registryentry1.key(), (registryaccess_registryentry) -> registryaccess_registryentry.value())));
   }

   public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
      return ownedNetworkableRegistries(layeredregistryaccess.getAccessFrom(RegistryLayer.WORLDGEN));
   }

   public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
      Stream<RegistryAccess.RegistryEntry<?>> stream = layeredregistryaccess.getLayer(RegistryLayer.STATIC).registries();
      Stream<RegistryAccess.RegistryEntry<?>> stream1 = networkedRegistries(layeredregistryaccess);
      return Stream.concat(stream1, stream);
   }

   static record NetworkedRegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec) {
   }
}
