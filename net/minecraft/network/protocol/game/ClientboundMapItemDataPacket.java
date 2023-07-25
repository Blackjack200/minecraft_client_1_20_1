package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
   private final int mapId;
   private final byte scale;
   private final boolean locked;
   @Nullable
   private final List<MapDecoration> decorations;
   @Nullable
   private final MapItemSavedData.MapPatch colorPatch;

   public ClientboundMapItemDataPacket(int i, byte b0, boolean flag, @Nullable Collection<MapDecoration> collection, @Nullable MapItemSavedData.MapPatch mapitemsaveddata_mappatch) {
      this.mapId = i;
      this.scale = b0;
      this.locked = flag;
      this.decorations = collection != null ? Lists.newArrayList(collection) : null;
      this.colorPatch = mapitemsaveddata_mappatch;
   }

   public ClientboundMapItemDataPacket(FriendlyByteBuf friendlybytebuf) {
      this.mapId = friendlybytebuf.readVarInt();
      this.scale = friendlybytebuf.readByte();
      this.locked = friendlybytebuf.readBoolean();
      this.decorations = friendlybytebuf.readNullable((friendlybytebuf1) -> friendlybytebuf1.readList((friendlybytebuf2) -> {
            MapDecoration.Type mapdecoration_type = friendlybytebuf2.readEnum(MapDecoration.Type.class);
            byte b0 = friendlybytebuf2.readByte();
            byte b1 = friendlybytebuf2.readByte();
            byte b2 = (byte)(friendlybytebuf2.readByte() & 15);
            Component component = friendlybytebuf2.readNullable(FriendlyByteBuf::readComponent);
            return new MapDecoration(mapdecoration_type, b0, b1, b2, component);
         }));
      int i = friendlybytebuf.readUnsignedByte();
      if (i > 0) {
         int j = friendlybytebuf.readUnsignedByte();
         int k = friendlybytebuf.readUnsignedByte();
         int l = friendlybytebuf.readUnsignedByte();
         byte[] abyte = friendlybytebuf.readByteArray();
         this.colorPatch = new MapItemSavedData.MapPatch(k, l, i, j, abyte);
      } else {
         this.colorPatch = null;
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.mapId);
      friendlybytebuf.writeByte(this.scale);
      friendlybytebuf.writeBoolean(this.locked);
      friendlybytebuf.writeNullable(this.decorations, (friendlybytebuf1, list) -> friendlybytebuf1.writeCollection(list, (friendlybytebuf2, mapdecoration) -> {
            friendlybytebuf2.writeEnum(mapdecoration.getType());
            friendlybytebuf2.writeByte(mapdecoration.getX());
            friendlybytebuf2.writeByte(mapdecoration.getY());
            friendlybytebuf2.writeByte(mapdecoration.getRot() & 15);
            friendlybytebuf2.writeNullable(mapdecoration.getName(), FriendlyByteBuf::writeComponent);
         }));
      if (this.colorPatch != null) {
         friendlybytebuf.writeByte(this.colorPatch.width);
         friendlybytebuf.writeByte(this.colorPatch.height);
         friendlybytebuf.writeByte(this.colorPatch.startX);
         friendlybytebuf.writeByte(this.colorPatch.startY);
         friendlybytebuf.writeByteArray(this.colorPatch.mapColors);
      } else {
         friendlybytebuf.writeByte(0);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleMapItemData(this);
   }

   public int getMapId() {
      return this.mapId;
   }

   public void applyToMap(MapItemSavedData mapitemsaveddata) {
      if (this.decorations != null) {
         mapitemsaveddata.addClientSideDecorations(this.decorations);
      }

      if (this.colorPatch != null) {
         this.colorPatch.applyToMap(mapitemsaveddata);
      }

   }

   public byte getScale() {
      return this.scale;
   }

   public boolean isLocked() {
      return this.locked;
   }
}
