package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
   private final BlockHitResult blockHit;
   private final InteractionHand hand;
   private final int sequence;

   public ServerboundUseItemOnPacket(InteractionHand interactionhand, BlockHitResult blockhitresult, int i) {
      this.hand = interactionhand;
      this.blockHit = blockhitresult;
      this.sequence = i;
   }

   public ServerboundUseItemOnPacket(FriendlyByteBuf friendlybytebuf) {
      this.hand = friendlybytebuf.readEnum(InteractionHand.class);
      this.blockHit = friendlybytebuf.readBlockHitResult();
      this.sequence = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.hand);
      friendlybytebuf.writeBlockHitResult(this.blockHit);
      friendlybytebuf.writeVarInt(this.sequence);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleUseItemOn(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public BlockHitResult getHitResult() {
      return this.blockHit;
   }

   public int getSequence() {
      return this.sequence;
   }
}
