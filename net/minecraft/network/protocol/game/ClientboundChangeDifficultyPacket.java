package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
   private final Difficulty difficulty;
   private final boolean locked;

   public ClientboundChangeDifficultyPacket(Difficulty difficulty, boolean flag) {
      this.difficulty = difficulty;
      this.locked = flag;
   }

   public ClientboundChangeDifficultyPacket(FriendlyByteBuf friendlybytebuf) {
      this.difficulty = Difficulty.byId(friendlybytebuf.readUnsignedByte());
      this.locked = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.difficulty.getId());
      friendlybytebuf.writeBoolean(this.locked);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleChangeDifficulty(this);
   }

   public boolean isLocked() {
      return this.locked;
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}
