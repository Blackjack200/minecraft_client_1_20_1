package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.RecipeBookType;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener> {
   private final RecipeBookType bookType;
   private final boolean isOpen;
   private final boolean isFiltering;

   public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType recipebooktype, boolean flag, boolean flag1) {
      this.bookType = recipebooktype;
      this.isOpen = flag;
      this.isFiltering = flag1;
   }

   public ServerboundRecipeBookChangeSettingsPacket(FriendlyByteBuf friendlybytebuf) {
      this.bookType = friendlybytebuf.readEnum(RecipeBookType.class);
      this.isOpen = friendlybytebuf.readBoolean();
      this.isFiltering = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.bookType);
      friendlybytebuf.writeBoolean(this.isOpen);
      friendlybytebuf.writeBoolean(this.isFiltering);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleRecipeBookChangeSettingsPacket(this);
   }

   public RecipeBookType getBookType() {
      return this.bookType;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public boolean isFiltering() {
      return this.isFiltering;
   }
}
