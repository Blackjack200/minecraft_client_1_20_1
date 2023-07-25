package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final BlockEntityType<?> type;
   @Nullable
   private final CompoundTag tag;

   public static ClientboundBlockEntityDataPacket create(BlockEntity blockentity, Function<BlockEntity, CompoundTag> function) {
      return new ClientboundBlockEntityDataPacket(blockentity.getBlockPos(), blockentity.getType(), function.apply(blockentity));
   }

   public static ClientboundBlockEntityDataPacket create(BlockEntity blockentity) {
      return create(blockentity, BlockEntity::getUpdateTag);
   }

   private ClientboundBlockEntityDataPacket(BlockPos blockpos, BlockEntityType<?> blockentitytype, CompoundTag compoundtag) {
      this.pos = blockpos;
      this.type = blockentitytype;
      this.tag = compoundtag.isEmpty() ? null : compoundtag;
   }

   public ClientboundBlockEntityDataPacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.type = friendlybytebuf.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
      this.tag = friendlybytebuf.readNbt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
      friendlybytebuf.writeNbt(this.tag);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleBlockEntityData(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockEntityType<?> getType() {
      return this.type;
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }
}
