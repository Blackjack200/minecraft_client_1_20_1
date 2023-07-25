package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
   private final Item item;
   private final int duration;

   public ClientboundCooldownPacket(Item item, int i) {
      this.item = item;
      this.duration = i;
   }

   public ClientboundCooldownPacket(FriendlyByteBuf friendlybytebuf) {
      this.item = friendlybytebuf.readById(BuiltInRegistries.ITEM);
      this.duration = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeId(BuiltInRegistries.ITEM, this.item);
      friendlybytebuf.writeVarInt(this.duration);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleItemCooldown(this);
   }

   public Item getItem() {
      return this.item;
   }

   public int getDuration() {
      return this.duration;
   }
}
