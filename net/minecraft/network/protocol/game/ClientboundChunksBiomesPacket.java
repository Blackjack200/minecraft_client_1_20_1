package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public record ClientboundChunksBiomesPacket(List<ClientboundChunksBiomesPacket.ChunkBiomeData> chunkBiomeData) implements Packet<ClientGamePacketListener> {
   private static final int TWO_MEGABYTES = 2097152;

   public ClientboundChunksBiomesPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readList(ClientboundChunksBiomesPacket.ChunkBiomeData::new));
   }

   public static ClientboundChunksBiomesPacket forChunks(List<LevelChunk> list) {
      return new ClientboundChunksBiomesPacket(list.stream().map(ClientboundChunksBiomesPacket.ChunkBiomeData::new).toList());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this.chunkBiomeData, (friendlybytebuf1, clientboundchunksbiomespacket_chunkbiomedata) -> clientboundchunksbiomespacket_chunkbiomedata.write(friendlybytebuf1));
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleChunksBiomes(this);
   }

   public static record ChunkBiomeData(ChunkPos pos, byte[] buffer) {
      public ChunkBiomeData(LevelChunk levelchunk) {
         this(levelchunk.getPos(), new byte[calculateChunkSize(levelchunk)]);
         extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelchunk);
      }

      public ChunkBiomeData(FriendlyByteBuf friendlybytebuf) {
         this(friendlybytebuf.readChunkPos(), friendlybytebuf.readByteArray(2097152));
      }

      private static int calculateChunkSize(LevelChunk levelchunk) {
         int i = 0;

         for(LevelChunkSection levelchunksection : levelchunk.getSections()) {
            i += levelchunksection.getBiomes().getSerializedSize();
         }

         return i;
      }

      public FriendlyByteBuf getReadBuffer() {
         return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
      }

      private ByteBuf getWriteBuffer() {
         ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
         bytebuf.writerIndex(0);
         return bytebuf;
      }

      public static void extractChunkData(FriendlyByteBuf friendlybytebuf, LevelChunk levelchunk) {
         for(LevelChunkSection levelchunksection : levelchunk.getSections()) {
            levelchunksection.getBiomes().write(friendlybytebuf);
         }

      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeChunkPos(this.pos);
         friendlybytebuf.writeByteArray(this.buffer);
      }
   }
}
