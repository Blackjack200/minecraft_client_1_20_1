package net.minecraft.network.protocol.game;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
   private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags;

   public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
      this.tags = map;
   }

   public ClientboundUpdateTagsPacket(FriendlyByteBuf friendlybytebuf) {
      this.tags = friendlybytebuf.readMap((friendlybytebuf1) -> ResourceKey.createRegistryKey(friendlybytebuf1.readResourceLocation()), TagNetworkSerialization.NetworkPayload::read);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeMap(this.tags, (friendlybytebuf2, resourcekey) -> friendlybytebuf2.writeResourceLocation(resourcekey.location()), (friendlybytebuf1, tagnetworkserialization_networkpayload) -> tagnetworkserialization_networkpayload.write(friendlybytebuf1));
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleUpdateTags(this);
   }

   public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> getTags() {
      return this.tags;
   }
}
