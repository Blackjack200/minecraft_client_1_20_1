package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener> {
   public ClientboundHurtAnimationPacket(LivingEntity livingentity) {
      this(livingentity.getId(), livingentity.getHurtDir());
   }

   public ClientboundHurtAnimationPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readVarInt(), friendlybytebuf.readFloat());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeFloat(this.yaw);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleHurtAnimation(this);
   }
}
