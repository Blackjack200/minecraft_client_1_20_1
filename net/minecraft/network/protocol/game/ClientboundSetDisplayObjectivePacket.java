package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
   private final int slot;
   private final String objectiveName;

   public ClientboundSetDisplayObjectivePacket(int i, @Nullable Objective objective) {
      this.slot = i;
      if (objective == null) {
         this.objectiveName = "";
      } else {
         this.objectiveName = objective.getName();
      }

   }

   public ClientboundSetDisplayObjectivePacket(FriendlyByteBuf friendlybytebuf) {
      this.slot = friendlybytebuf.readByte();
      this.objectiveName = friendlybytebuf.readUtf();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.slot);
      friendlybytebuf.writeUtf(this.objectiveName);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetDisplayObjective(this);
   }

   public int getSlot() {
      return this.slot;
   }

   @Nullable
   public String getObjectiveName() {
      return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
   }
}
