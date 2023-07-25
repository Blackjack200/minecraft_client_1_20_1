package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
   private final IntList entityIds;

   public ClientboundRemoveEntitiesPacket(IntList intlist) {
      this.entityIds = new IntArrayList(intlist);
   }

   public ClientboundRemoveEntitiesPacket(int... aint) {
      this.entityIds = new IntArrayList(aint);
   }

   public ClientboundRemoveEntitiesPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityIds = friendlybytebuf.readIntIdList();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeIntIdList(this.entityIds);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleRemoveEntities(this);
   }

   public IntList getEntityIds() {
      return this.entityIds;
   }
}
