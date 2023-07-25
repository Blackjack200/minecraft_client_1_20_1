package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
   private final float experienceProgress;
   private final int totalExperience;
   private final int experienceLevel;

   public ClientboundSetExperiencePacket(float f, int i, int j) {
      this.experienceProgress = f;
      this.totalExperience = i;
      this.experienceLevel = j;
   }

   public ClientboundSetExperiencePacket(FriendlyByteBuf friendlybytebuf) {
      this.experienceProgress = friendlybytebuf.readFloat();
      this.experienceLevel = friendlybytebuf.readVarInt();
      this.totalExperience = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeFloat(this.experienceProgress);
      friendlybytebuf.writeVarInt(this.experienceLevel);
      friendlybytebuf.writeVarInt(this.totalExperience);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetExperience(this);
   }

   public float getExperienceProgress() {
      return this.experienceProgress;
   }

   public int getTotalExperience() {
      return this.totalExperience;
   }

   public int getExperienceLevel() {
      return this.experienceLevel;
   }
}
