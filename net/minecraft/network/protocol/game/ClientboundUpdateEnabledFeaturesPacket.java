package net.minecraft.network.protocol.game;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateEnabledFeaturesPacket(Set<ResourceLocation> features) implements Packet<ClientGamePacketListener> {
   public ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this.features, FriendlyByteBuf::writeResourceLocation);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleEnabledFeatures(this);
   }
}
