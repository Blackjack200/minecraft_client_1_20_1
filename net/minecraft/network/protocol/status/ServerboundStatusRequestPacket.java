package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
   public ServerboundStatusRequestPacket() {
   }

   public ServerboundStatusRequestPacket(FriendlyByteBuf friendlybytebuf) {
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
   }

   public void handle(ServerStatusPacketListener serverstatuspacketlistener) {
      serverstatuspacketlistener.handleStatusRequest(this);
   }
}
