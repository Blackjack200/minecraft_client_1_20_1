package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;
   private final ClientboundLightUpdatePacketData lightData;

   public ClientboundLightUpdatePacket(ChunkPos chunkpos, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
      this.x = chunkpos.x;
      this.z = chunkpos.z;
      this.lightData = new ClientboundLightUpdatePacketData(chunkpos, levellightengine, bitset, bitset1);
   }

   public ClientboundLightUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this.x = friendlybytebuf.readVarInt();
      this.z = friendlybytebuf.readVarInt();
      this.lightData = new ClientboundLightUpdatePacketData(friendlybytebuf, this.x, this.z);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.x);
      friendlybytebuf.writeVarInt(this.z);
      this.lightData.write(friendlybytebuf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleLightUpdatePacket(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public ClientboundLightUpdatePacketData getLightData() {
      return this.lightData;
   }
}
