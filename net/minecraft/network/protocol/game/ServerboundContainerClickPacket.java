package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.IntFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener> {
   private static final int MAX_SLOT_COUNT = 128;
   private final int containerId;
   private final int stateId;
   private final int slotNum;
   private final int buttonNum;
   private final ClickType clickType;
   private final ItemStack carriedItem;
   private final Int2ObjectMap<ItemStack> changedSlots;

   public ServerboundContainerClickPacket(int i, int j, int k, int l, ClickType clicktype, ItemStack itemstack, Int2ObjectMap<ItemStack> int2objectmap) {
      this.containerId = i;
      this.stateId = j;
      this.slotNum = k;
      this.buttonNum = l;
      this.clickType = clicktype;
      this.carriedItem = itemstack;
      this.changedSlots = Int2ObjectMaps.unmodifiable(int2objectmap);
   }

   public ServerboundContainerClickPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
      this.stateId = friendlybytebuf.readVarInt();
      this.slotNum = friendlybytebuf.readShort();
      this.buttonNum = friendlybytebuf.readByte();
      this.clickType = friendlybytebuf.readEnum(ClickType.class);
      IntFunction<Int2ObjectOpenHashMap<ItemStack>> intfunction = FriendlyByteBuf.limitValue(Int2ObjectOpenHashMap::new, 128);
      this.changedSlots = Int2ObjectMaps.unmodifiable(friendlybytebuf.readMap(intfunction, (friendlybytebuf1) -> Integer.valueOf(friendlybytebuf1.readShort()), FriendlyByteBuf::readItem));
      this.carriedItem = friendlybytebuf.readItem();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeVarInt(this.stateId);
      friendlybytebuf.writeShort(this.slotNum);
      friendlybytebuf.writeByte(this.buttonNum);
      friendlybytebuf.writeEnum(this.clickType);
      friendlybytebuf.writeMap(this.changedSlots, FriendlyByteBuf::writeShort, FriendlyByteBuf::writeItem);
      friendlybytebuf.writeItem(this.carriedItem);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleContainerClick(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSlotNum() {
      return this.slotNum;
   }

   public int getButtonNum() {
      return this.buttonNum;
   }

   public ItemStack getCarriedItem() {
      return this.carriedItem;
   }

   public Int2ObjectMap<ItemStack> getChangedSlots() {
      return this.changedSlots;
   }

   public ClickType getClickType() {
      return this.clickType;
   }

   public int getStateId() {
      return this.stateId;
   }
}
