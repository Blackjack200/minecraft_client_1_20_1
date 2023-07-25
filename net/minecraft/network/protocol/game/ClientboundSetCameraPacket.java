package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
   private final int cameraId;

   public ClientboundSetCameraPacket(Entity entity) {
      this.cameraId = entity.getId();
   }

   public ClientboundSetCameraPacket(FriendlyByteBuf friendlybytebuf) {
      this.cameraId = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.cameraId);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetCamera(this);
   }

   @Nullable
   public Entity getEntity(Level level) {
      return level.getEntity(this.cameraId);
   }
}
