package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
   private static final int TWO_MEGABYTES = 2097152;
   private final CompoundTag heightmaps;
   private final byte[] buffer;
   private final List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;

   public ClientboundLevelChunkPacketData(LevelChunk levelchunk) {
      this.heightmaps = new CompoundTag();

      for(Map.Entry<Heightmap.Types, Heightmap> map_entry : levelchunk.getHeightmaps()) {
         if (map_entry.getKey().sendToClient()) {
            this.heightmaps.put(map_entry.getKey().getSerializationKey(), new LongArrayTag(map_entry.getValue().getRawData()));
         }
      }

      this.buffer = new byte[calculateChunkSize(levelchunk)];
      extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelchunk);
      this.blockEntitiesData = Lists.newArrayList();

      for(Map.Entry<BlockPos, BlockEntity> map_entry1 : levelchunk.getBlockEntities().entrySet()) {
         this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create(map_entry1.getValue()));
      }

   }

   public ClientboundLevelChunkPacketData(FriendlyByteBuf friendlybytebuf, int i, int j) {
      this.heightmaps = friendlybytebuf.readNbt();
      if (this.heightmaps == null) {
         throw new RuntimeException("Can't read heightmap in packet for [" + i + ", " + j + "]");
      } else {
         int k = friendlybytebuf.readVarInt();
         if (k > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
         } else {
            this.buffer = new byte[k];
            friendlybytebuf.readBytes(this.buffer);
            this.blockEntitiesData = friendlybytebuf.readList(ClientboundLevelChunkPacketData.BlockEntityInfo::new);
         }
      }
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeNbt(this.heightmaps);
      friendlybytebuf.writeVarInt(this.buffer.length);
      friendlybytebuf.writeBytes(this.buffer);
      friendlybytebuf.writeCollection(this.blockEntitiesData, (friendlybytebuf1, clientboundlevelchunkpacketdata_blockentityinfo) -> clientboundlevelchunkpacketdata_blockentityinfo.write(friendlybytebuf1));
   }

   private static int calculateChunkSize(LevelChunk levelchunk) {
      int i = 0;

      for(LevelChunkSection levelchunksection : levelchunk.getSections()) {
         i += levelchunksection.getSerializedSize();
      }

      return i;
   }

   private ByteBuf getWriteBuffer() {
      ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
      bytebuf.writerIndex(0);
      return bytebuf;
   }

   public static void extractChunkData(FriendlyByteBuf friendlybytebuf, LevelChunk levelchunk) {
      for(LevelChunkSection levelchunksection : levelchunk.getSections()) {
         levelchunksection.write(friendlybytebuf);
      }

   }

   public Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int i, int j) {
      return (clientboundlevelchunkpacketdata_blockentitytagoutput) -> this.getBlockEntitiesTags(clientboundlevelchunkpacketdata_blockentitytagoutput, i, j);
   }

   private void getBlockEntitiesTags(ClientboundLevelChunkPacketData.BlockEntityTagOutput clientboundlevelchunkpacketdata_blockentitytagoutput, int i, int j) {
      int k = 16 * i;
      int l = 16 * j;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(ClientboundLevelChunkPacketData.BlockEntityInfo clientboundlevelchunkpacketdata_blockentityinfo : this.blockEntitiesData) {
         int i1 = k + SectionPos.sectionRelative(clientboundlevelchunkpacketdata_blockentityinfo.packedXZ >> 4);
         int j1 = l + SectionPos.sectionRelative(clientboundlevelchunkpacketdata_blockentityinfo.packedXZ);
         blockpos_mutableblockpos.set(i1, clientboundlevelchunkpacketdata_blockentityinfo.y, j1);
         clientboundlevelchunkpacketdata_blockentitytagoutput.accept(blockpos_mutableblockpos, clientboundlevelchunkpacketdata_blockentityinfo.type, clientboundlevelchunkpacketdata_blockentityinfo.tag);
      }

   }

   public FriendlyByteBuf getReadBuffer() {
      return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
   }

   public CompoundTag getHeightmaps() {
      return this.heightmaps;
   }

   static class BlockEntityInfo {
      final int packedXZ;
      final int y;
      final BlockEntityType<?> type;
      @Nullable
      final CompoundTag tag;

      private BlockEntityInfo(int i, int j, BlockEntityType<?> blockentitytype, @Nullable CompoundTag compoundtag) {
         this.packedXZ = i;
         this.y = j;
         this.type = blockentitytype;
         this.tag = compoundtag;
      }

      private BlockEntityInfo(FriendlyByteBuf friendlybytebuf) {
         this.packedXZ = friendlybytebuf.readByte();
         this.y = friendlybytebuf.readShort();
         this.type = friendlybytebuf.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
         this.tag = friendlybytebuf.readNbt();
      }

      void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeByte(this.packedXZ);
         friendlybytebuf.writeShort(this.y);
         friendlybytebuf.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
         friendlybytebuf.writeNbt(this.tag);
      }

      static ClientboundLevelChunkPacketData.BlockEntityInfo create(BlockEntity blockentity) {
         CompoundTag compoundtag = blockentity.getUpdateTag();
         BlockPos blockpos = blockentity.getBlockPos();
         int i = SectionPos.sectionRelative(blockpos.getX()) << 4 | SectionPos.sectionRelative(blockpos.getZ());
         return new ClientboundLevelChunkPacketData.BlockEntityInfo(i, blockpos.getY(), blockentity.getType(), compoundtag.isEmpty() ? null : compoundtag);
      }
   }

   @FunctionalInterface
   public interface BlockEntityTagOutput {
      void accept(BlockPos blockpos, BlockEntityType<?> blockentitytype, @Nullable CompoundTag compoundtag);
   }
}
