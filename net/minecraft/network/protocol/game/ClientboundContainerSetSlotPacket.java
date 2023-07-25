package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
   public static final int CARRIED_ITEM = -1;
   public static final int PLAYER_INVENTORY = -2;
   private final int containerId;
   private final int stateId;
   private final int slot;
   private final ItemStack itemStack;

   public ClientboundContainerSetSlotPacket(int i, int j, int k, ItemStack itemstack) {
      this.containerId = i;
      this.stateId = j;
      this.slot = k;
      this.itemStack = itemstack.copy();
   }

   public ClientboundContainerSetSlotPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
      this.stateId = friendlybytebuf.readVarInt();
      this.slot = friendlybytebuf.readShort();
      this.itemStack = friendlybytebuf.readItem();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeVarInt(this.stateId);
      friendlybytebuf.writeShort(this.slot);
      friendlybytebuf.writeItem(this.itemStack);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleContainerSetSlot(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }

   public int getStateId() {
      return this.stateId;
   }
}
