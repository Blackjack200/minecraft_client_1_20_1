package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundHelloPacket(String name, Optional<UUID> profileId) implements Packet<ServerLoginPacketListener> {
   public ServerboundHelloPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readUtf(16), friendlybytebuf.readOptional(FriendlyByteBuf::readUUID));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.name, 16);
      friendlybytebuf.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
   }

   public void handle(ServerLoginPacketListener serverloginpacketlistener) {
      serverloginpacketlistener.handleHello(this);
   }
}
