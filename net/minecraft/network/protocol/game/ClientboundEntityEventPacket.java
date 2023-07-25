package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final byte eventId;

   public ClientboundEntityEventPacket(Entity entity, byte b0) {
      this.entityId = entity.getId();
      this.eventId = b0;
   }

   public ClientboundEntityEventPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readInt();
      this.eventId = friendlybytebuf.readByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.entityId);
      friendlybytebuf.writeByte(this.eventId);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleEntityEvent(this);
   }

   @Nullable
   public Entity getEntity(Level level) {
      return level.getEntity(this.entityId);
   }

   public byte getEventId() {
      return this.eventId;
   }
}
