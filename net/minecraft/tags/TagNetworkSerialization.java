package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;

public class TagNetworkSerialization {
   public static Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> serializeTagsToNetwork(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
      return RegistrySynchronization.networkSafeRegistries(layeredregistryaccess).map((registryaccess_registryentry) -> Pair.of(registryaccess_registryentry.key(), serializeToNetwork(registryaccess_registryentry.value()))).filter((pair) -> !pair.getSecond().isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   private static <T> TagNetworkSerialization.NetworkPayload serializeToNetwork(Registry<T> registry) {
      Map<ResourceLocation, IntList> map = new HashMap<>();
      registry.getTags().forEach((pair) -> {
         HolderSet<T> holderset = pair.getSecond();
         IntList intlist = new IntArrayList(holderset.size());

         for(Holder<T> holder : holderset) {
            if (holder.kind() != Holder.Kind.REFERENCE) {
               throw new IllegalStateException("Can't serialize unregistered value " + holder);
            }

            intlist.add(registry.getId(holder.value()));
         }

         map.put(pair.getFirst().location(), intlist);
      });
      return new TagNetworkSerialization.NetworkPayload(map);
   }

   public static <T> void deserializeTagsFromNetwork(ResourceKey<? extends Registry<T>> resourcekey, Registry<T> registry, TagNetworkSerialization.NetworkPayload tagnetworkserialization_networkpayload, TagNetworkSerialization.TagOutput<T> tagnetworkserialization_tagoutput) {
      tagnetworkserialization_networkpayload.tags.forEach((resourcelocation, intlist) -> {
         TagKey<T> tagkey = TagKey.create(resourcekey, resourcelocation);
         List<Holder<T>> list = intlist.intStream().mapToObj(registry::getHolder).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
         tagnetworkserialization_tagoutput.accept(tagkey, list);
      });
   }

   public static final class NetworkPayload {
      final Map<ResourceLocation, IntList> tags;

      NetworkPayload(Map<ResourceLocation, IntList> map) {
         this.tags = map;
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
      }

      public static TagNetworkSerialization.NetworkPayload read(FriendlyByteBuf friendlybytebuf) {
         return new TagNetworkSerialization.NetworkPayload(friendlybytebuf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
      }

      public boolean isEmpty() {
         return this.tags.isEmpty();
      }
   }

   @FunctionalInterface
   public interface TagOutput<T> {
      void accept(TagKey<T> tagkey, List<Holder<T>> list);
   }
}
