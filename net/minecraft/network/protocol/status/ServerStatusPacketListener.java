package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerStatusPacketListener extends ServerPacketListener {
   void handlePingRequest(ServerboundPingRequestPacket serverboundpingrequestpacket);

   void handleStatusRequest(ServerboundStatusRequestPacket serverboundstatusrequestpacket);
}
