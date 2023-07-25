package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener> {
   public ClientboundStatusResponsePacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readJsonWithCodec(ServerStatus.CODEC));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeJsonWithCodec(ServerStatus.CODEC, this.status);
   }

   public void handle(ClientStatusPacketListener clientstatuspacketlistener) {
      clientstatuspacketlistener.handleStatusResponse(this);
   }
}
