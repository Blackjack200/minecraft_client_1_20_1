package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

public interface ClientLoginPacketListener extends PacketListener {
   void handleHello(ClientboundHelloPacket clientboundhellopacket);

   void handleGameProfile(ClientboundGameProfilePacket clientboundgameprofilepacket);

   void handleDisconnect(ClientboundLoginDisconnectPacket clientboundlogindisconnectpacket);

   void handleCompression(ClientboundLoginCompressionPacket clientboundlogincompressionpacket);

   void handleCustomQuery(ClientboundCustomQueryPacket clientboundcustomquerypacket);
}
