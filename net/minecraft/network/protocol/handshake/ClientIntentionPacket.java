package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
   private static final int MAX_HOST_LENGTH = 255;
   private final int protocolVersion;
   private final String hostName;
   private final int port;
   private final ConnectionProtocol intention;

   public ClientIntentionPacket(String s, int i, ConnectionProtocol connectionprotocol) {
      this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
      this.hostName = s;
      this.port = i;
      this.intention = connectionprotocol;
   }

   public ClientIntentionPacket(FriendlyByteBuf friendlybytebuf) {
      this.protocolVersion = friendlybytebuf.readVarInt();
      this.hostName = friendlybytebuf.readUtf(255);
      this.port = friendlybytebuf.readUnsignedShort();
      this.intention = ConnectionProtocol.getById(friendlybytebuf.readVarInt());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.protocolVersion);
      friendlybytebuf.writeUtf(this.hostName);
      friendlybytebuf.writeShort(this.port);
      friendlybytebuf.writeVarInt(this.intention.getId());
   }

   public void handle(ServerHandshakePacketListener serverhandshakepacketlistener) {
      serverhandshakepacketlistener.handleIntention(this);
   }

   public ConnectionProtocol getIntention() {
      return this.intention;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public String getHostName() {
      return this.hostName;
   }

   public int getPort() {
      return this.port;
   }
}
