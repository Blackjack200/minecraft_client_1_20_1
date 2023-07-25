package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final BlockState blockState;

   public ClientboundBlockUpdatePacket(BlockPos blockpos, BlockState blockstate) {
      this.pos = blockpos;
      this.blockState = blockstate;
   }

   public ClientboundBlockUpdatePacket(BlockGetter blockgetter, BlockPos blockpos) {
      this(blockpos, blockgetter.getBlockState(blockpos));
   }

   public ClientboundBlockUpdatePacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.blockState = friendlybytebuf.readById(Block.BLOCK_STATE_REGISTRY);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeId(Block.BLOCK_STATE_REGISTRY, this.blockState);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleBlockUpdate(this);
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
