package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public class BundleDelimiterPacket<T extends PacketListener> implements Packet<T> {
   public final void write(FriendlyByteBuf friendlybytebuf) {
   }

   public final void handle(T packetlistener) {
      throw new AssertionError("This packet should be handled by pipeline");
   }
}
