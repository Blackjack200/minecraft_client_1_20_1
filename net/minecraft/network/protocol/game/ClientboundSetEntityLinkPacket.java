package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
   private final int sourceId;
   private final int destId;

   public ClientboundSetEntityLinkPacket(Entity entity, @Nullable Entity entity1) {
      this.sourceId = entity.getId();
      this.destId = entity1 != null ? entity1.getId() : 0;
   }

   public ClientboundSetEntityLinkPacket(FriendlyByteBuf friendlybytebuf) {
      this.sourceId = friendlybytebuf.readInt();
      this.destId = friendlybytebuf.readInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.sourceId);
      friendlybytebuf.writeInt(this.destId);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleEntityLinkPacket(this);
   }

   public int getSourceId() {
      return this.sourceId;
   }

   public int getDestId() {
      return this.destId;
   }
}
