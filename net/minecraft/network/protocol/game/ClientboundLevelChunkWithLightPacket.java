package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;
   private final ClientboundLevelChunkPacketData chunkData;
   private final ClientboundLightUpdatePacketData lightData;

   public ClientboundLevelChunkWithLightPacket(LevelChunk levelchunk, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
      ChunkPos chunkpos = levelchunk.getPos();
      this.x = chunkpos.x;
      this.z = chunkpos.z;
      this.chunkData = new ClientboundLevelChunkPacketData(levelchunk);
      this.lightData = new ClientboundLightUpdatePacketData(chunkpos, levellightengine, bitset, bitset1);
   }

   public ClientboundLevelChunkWithLightPacket(FriendlyByteBuf friendlybytebuf) {
      this.x = friendlybytebuf.readInt();
      this.z = friendlybytebuf.readInt();
      this.chunkData = new ClientboundLevelChunkPacketData(friendlybytebuf, this.x, this.z);
      this.lightData = new ClientboundLightUpdatePacketData(friendlybytebuf, this.x, this.z);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.x);
      friendlybytebuf.writeInt(this.z);
      this.chunkData.write(friendlybytebuf);
      this.lightData.write(friendlybytebuf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleLevelChunkWithLight(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public ClientboundLevelChunkPacketData getChunkData() {
      return this.chunkData;
   }

   public ClientboundLightUpdatePacketData getLightData() {
      return this.lightData;
   }
}
