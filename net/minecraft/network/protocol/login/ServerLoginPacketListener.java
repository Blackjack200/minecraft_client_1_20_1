package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerLoginPacketListener extends ServerPacketListener {
   void handleHello(ServerboundHelloPacket serverboundhellopacket);

   void handleKey(ServerboundKeyPacket serverboundkeypacket);

   void handleCustomQueryPacket(ServerboundCustomQueryPacket serverboundcustomquerypacket);
}
