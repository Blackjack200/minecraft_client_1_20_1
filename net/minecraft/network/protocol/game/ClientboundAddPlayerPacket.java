package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final UUID playerId;
   private final double x;
   private final double y;
   private final double z;
   private final byte yRot;
   private final byte xRot;

   public ClientboundAddPlayerPacket(Player player) {
      this.entityId = player.getId();
      this.playerId = player.getGameProfile().getId();
      this.x = player.getX();
      this.y = player.getY();
      this.z = player.getZ();
      this.yRot = (byte)((int)(player.getYRot() * 256.0F / 360.0F));
      this.xRot = (byte)((int)(player.getXRot() * 256.0F / 360.0F));
   }

   public ClientboundAddPlayerPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readVarInt();
      this.playerId = friendlybytebuf.readUUID();
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.yRot = friendlybytebuf.readByte();
      this.xRot = friendlybytebuf.readByte();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeUUID(this.playerId);
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeByte(this.yRot);
      friendlybytebuf.writeByte(this.xRot);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAddPlayer(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public byte getyRot() {
      return this.yRot;
   }

   public byte getxRot() {
      return this.xRot;
   }
}
