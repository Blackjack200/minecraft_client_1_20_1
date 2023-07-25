package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
   private final int vehicle;
   private final int[] passengers;

   public ClientboundSetPassengersPacket(Entity entity) {
      this.vehicle = entity.getId();
      List<Entity> list = entity.getPassengers();
      this.passengers = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         this.passengers[i] = list.get(i).getId();
      }

   }

   public ClientboundSetPassengersPacket(FriendlyByteBuf friendlybytebuf) {
      this.vehicle = friendlybytebuf.readVarInt();
      this.passengers = friendlybytebuf.readVarIntArray();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.vehicle);
      friendlybytebuf.writeVarIntArray(this.passengers);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSetEntityPassengersPacket(this);
   }

   public int[] getPassengers() {
      return this.passengers;
   }

   public int getVehicle() {
      return this.vehicle;
   }
}
