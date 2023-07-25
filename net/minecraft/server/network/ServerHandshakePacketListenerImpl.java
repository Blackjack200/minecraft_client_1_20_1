package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
   private final MinecraftServer server;
   private final Connection connection;

   public ServerHandshakePacketListenerImpl(MinecraftServer minecraftserver, Connection connection) {
      this.server = minecraftserver;
      this.connection = connection;
   }

   public void handleIntention(ClientIntentionPacket clientintentionpacket) {
      switch (clientintentionpacket.getIntention()) {
         case LOGIN:
            this.connection.setProtocol(ConnectionProtocol.LOGIN);
            if (clientintentionpacket.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
               Component component;
               if (clientintentionpacket.getProtocolVersion() < 754) {
                  component = Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
               } else {
                  component = Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
               }

               this.connection.send(new ClientboundLoginDisconnectPacket(component));
               this.connection.disconnect(component);
            } else {
               this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
            }
            break;
         case STATUS:
            ServerStatus serverstatus = this.server.getStatus();
            if (this.server.repliesToStatus() && serverstatus != null) {
               this.connection.setProtocol(ConnectionProtocol.STATUS);
               this.connection.setListener(new ServerStatusPacketListenerImpl(serverstatus, this.connection));
            } else {
               this.connection.disconnect(IGNORE_STATUS_REASON);
            }
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + clientintentionpacket.getIntention());
      }

   }

   public void onDisconnect(Component component) {
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }
}
