package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final Direction direction;
   private final ServerboundPlayerActionPacket.Action action;
   private final int sequence;

   public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action serverboundplayeractionpacket_action, BlockPos blockpos, Direction direction, int i) {
      this.action = serverboundplayeractionpacket_action;
      this.pos = blockpos.immutable();
      this.direction = direction;
      this.sequence = i;
   }

   public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action serverboundplayeractionpacket_action, BlockPos blockpos, Direction direction) {
      this(serverboundplayeractionpacket_action, blockpos, direction, 0);
   }

   public ServerboundPlayerActionPacket(FriendlyByteBuf friendlybytebuf) {
      this.action = friendlybytebuf.readEnum(ServerboundPlayerActionPacket.Action.class);
      this.pos = friendlybytebuf.readBlockPos();
      this.direction = Direction.from3DDataValue(friendlybytebuf.readUnsignedByte());
      this.sequence = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.action);
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeByte(this.direction.get3DDataValue());
      friendlybytebuf.writeVarInt(this.sequence);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePlayerAction(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public ServerboundPlayerActionPacket.Action getAction() {
      return this.action;
   }

   public int getSequence() {
      return this.sequence;
   }

   public static enum Action {
      START_DESTROY_BLOCK,
      ABORT_DESTROY_BLOCK,
      STOP_DESTROY_BLOCK,
      DROP_ALL_ITEMS,
      DROP_ITEM,
      RELEASE_USE_ITEM,
      SWAP_ITEM_WITH_OFFHAND;
   }
}
