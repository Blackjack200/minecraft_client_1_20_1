package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
   private static final int POS_IN_SECTION_BITS = 12;
   private final SectionPos sectionPos;
   private final short[] positions;
   private final BlockState[] states;

   public ClientboundSectionBlocksUpdatePacket(SectionPos sectionpos, ShortSet shortset, LevelChunkSection levelchunksection) {
      this.sectionPos = sectionpos;
      int i = shortset.size();
      this.positions = new short[i];
      this.states = new BlockState[i];
      int j = 0;

      for(short short0 : shortset) {
         this.positions[j] = short0;
         this.states[j] = levelchunksection.getBlockState(SectionPos.sectionRelativeX(short0), SectionPos.sectionRelativeY(short0), SectionPos.sectionRelativeZ(short0));
         ++j;
      }

   }

   public ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this.sectionPos = SectionPos.of(friendlybytebuf.readLong());
      int i = friendlybytebuf.readVarInt();
      this.positions = new short[i];
      this.states = new BlockState[i];

      for(int j = 0; j < i; ++j) {
         long k = friendlybytebuf.readVarLong();
         this.positions[j] = (short)((int)(k & 4095L));
         this.states[j] = Block.BLOCK_STATE_REGISTRY.byId((int)(k >>> 12));
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeLong(this.sectionPos.asLong());
      friendlybytebuf.writeVarInt(this.positions.length);

      for(int i = 0; i < this.positions.length; ++i) {
         friendlybytebuf.writeVarLong((long)Block.getId(this.states[i]) << 12 | (long)this.positions[i]);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleChunkBlocksUpdate(this);
   }

   public void runUpdates(BiConsumer<BlockPos, BlockState> biconsumer) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < this.positions.length; ++i) {
         short short0 = this.positions[i];
         blockpos_mutableblockpos.set(this.sectionPos.relativeToBlockX(short0), this.sectionPos.relativeToBlockY(short0), this.sectionPos.relativeToBlockZ(short0));
         biconsumer.accept(blockpos_mutableblockpos, this.states[i]);
      }

   }
}
