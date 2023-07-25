package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
   private final String name;

   public ServerboundRenameItemPacket(String s) {
      this.name = s;
   }

   public ServerboundRenameItemPacket(FriendlyByteBuf friendlybytebuf) {
      this.name = friendlybytebuf.readUtf();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.name);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleRenameItem(this);
   }

   public String getName() {
      return this.name;
   }
}
