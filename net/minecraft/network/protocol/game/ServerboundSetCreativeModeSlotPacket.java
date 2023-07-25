package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
   private final int slotNum;
   private final ItemStack itemStack;

   public ServerboundSetCreativeModeSlotPacket(int i, ItemStack itemstack) {
      this.slotNum = i;
      this.itemStack = itemstack.copy();
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetCreativeModeSlot(this);
   }

   public ServerboundSetCreativeModeSlotPacket(FriendlyByteBuf friendlybytebuf) {
      this.slotNum = friendlybytebuf.readShort();
      this.itemStack = friendlybytebuf.readItem();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeShort(this.slotNum);
      friendlybytebuf.writeItem(this.itemStack);
   }

   public int getSlotNum() {
      return this.slotNum;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }
}
