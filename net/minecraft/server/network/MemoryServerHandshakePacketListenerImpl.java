package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private final MinecraftServer server;
   private final Connection connection;

   public MemoryServerHandshakePacketListenerImpl(MinecraftServer minecraftserver, Connection connection) {
      this.server = minecraftserver;
      this.connection = connection;
   }

   public void handleIntention(ClientIntentionPacket clientintentionpacket) {
      this.connection.setProtocol(clientintentionpacket.getIntention());
      this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
   }

   public void onDisconnect(Component component) {
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }
}
