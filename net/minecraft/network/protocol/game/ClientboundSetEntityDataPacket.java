package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener> {
   public static final int EOF_MARKER = 255;

   public ClientboundSetEntityDataPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readVarInt(), unpack(friendlybytebuf));
   }

   private static void pack(List<SynchedEntityData.DataValue<?>> list, FriendlyByteBuf friendlybytebuf) {
      for(SynchedEntityData.DataValue<?> synchedentitydata_datavalue : list) {
         synchedentitydata_datavalue.write(friendlybytebuf);
      }

      friendlybytebuf.writeByte(255);
   }

   private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf friendlybytebuf) {
      List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

      int i;
      while((i = friendlybytebuf.readUnsignedByte()) != 255) {
         list.add(SynchedEntityData.DataValue.read(friendlybytebuf, i));
      }

      return list;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      pack(this.packedItems, friendlybytebuf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetEntityData(this);
   }
}
