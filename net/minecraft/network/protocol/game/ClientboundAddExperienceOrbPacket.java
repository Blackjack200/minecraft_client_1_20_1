package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final double x;
   private final double y;
   private final double z;
   private final int value;

   public ClientboundAddExperienceOrbPacket(ExperienceOrb experienceorb) {
      this.id = experienceorb.getId();
      this.x = experienceorb.getX();
      this.y = experienceorb.getY();
      this.z = experienceorb.getZ();
      this.value = experienceorb.getValue();
   }

   public ClientboundAddExperienceOrbPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.value = friendlybytebuf.readShort();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeShort(this.value);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAddExperienceOrb(this);
   }

   public int getId() {
      return this.id;
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

   public int getValue() {
      return this.value;
   }
}
