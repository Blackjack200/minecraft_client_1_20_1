package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
   private final int containerId;
   private final ResourceLocation recipe;
   private final boolean shiftDown;

   public ServerboundPlaceRecipePacket(int i, Recipe<?> recipe, boolean flag) {
      this.containerId = i;
      this.recipe = recipe.getId();
      this.shiftDown = flag;
   }

   public ServerboundPlaceRecipePacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
      this.recipe = friendlybytebuf.readResourceLocation();
      this.shiftDown = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeResourceLocation(this.recipe);
      friendlybytebuf.writeBoolean(this.shiftDown);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePlaceRecipe(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   public boolean isShiftDown() {
      return this.shiftDown;
   }
}
