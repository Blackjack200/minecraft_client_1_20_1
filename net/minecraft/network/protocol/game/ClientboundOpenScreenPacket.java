package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.MenuType;

public class ClientboundOpenScreenPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final MenuType<?> type;
   private final Component title;

   public ClientboundOpenScreenPacket(int i, MenuType<?> menutype, Component component) {
      this.containerId = i;
      this.type = menutype;
      this.title = component;
   }

   public ClientboundOpenScreenPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readVarInt();
      this.type = friendlybytebuf.readById(BuiltInRegistries.MENU);
      this.title = friendlybytebuf.readComponent();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.containerId);
      friendlybytebuf.writeId(BuiltInRegistries.MENU, this.type);
      friendlybytebuf.writeComponent(this.title);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleOpenScreen(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   @Nullable
   public MenuType<?> getType() {
      return this.type;
   }

   public Component getTitle() {
      return this.title;
   }
}
