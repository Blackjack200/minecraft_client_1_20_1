package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ServerboundTeleportToEntityPacket implements Packet<ServerGamePacketListener> {
   private final UUID uuid;

   public ServerboundTeleportToEntityPacket(UUID uuid) {
      this.uuid = uuid;
   }

   public ServerboundTeleportToEntityPacket(FriendlyByteBuf friendlybytebuf) {
      this.uuid = friendlybytebuf.readUUID();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUUID(this.uuid);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleTeleportToEntityPacket(this);
   }

   @Nullable
   public Entity getEntity(ServerLevel serverlevel) {
      return serverlevel.getEntity(this.uuid);
   }
}
