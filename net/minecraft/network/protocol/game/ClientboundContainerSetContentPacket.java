package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int stateId;
   private final List<ItemStack> items;
   private final ItemStack carriedItem;

   public ClientboundContainerSetContentPacket(int i, int j, NonNullList<ItemStack> nonnulllist, ItemStack itemstack) {
      this.containerId = i;
      this.stateId = j;
      this.items = NonNullList.withSize(nonnulllist.size(), ItemStack.EMPTY);

      for(int k = 0; k < nonnulllist.size(); ++k) {
         this.items.set(k, nonnulllist.get(k).copy());
      }

      this.carriedItem = itemstack.copy();
   }

   public ClientboundContainerSetContentPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readUnsignedByte();
      this.stateId = friendlybytebuf.readVarInt();
      this.items = friendlybytebuf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readItem);
      this.carriedItem = friendlybytebuf.readItem();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeVarInt(this.stateId);
      friendlybytebuf.writeCollection(this.items, FriendlyByteBuf::writeItem);
      friendlybytebuf.writeItem(this.carriedItem);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleContainerContent(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public List<ItemStack> getItems() {
      return this.items;
   }

   public ItemStack getCarriedItem() {
      return this.carriedItem;
   }

   public int getStateId() {
      return this.stateId;
   }
}
