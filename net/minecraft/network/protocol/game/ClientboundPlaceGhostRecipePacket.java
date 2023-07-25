package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final ResourceLocation recipe;

   public ClientboundPlaceGhostRecipePacket(int i, Recipe<?> recipe) {
      this.containerId = i;
      this.recipe = recipe.getId();
   }

   public ClientboundPlaceGhostRecipePacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readByte();
      this.recipe = friendlybytebuf.readResourceLocation();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByte(this.containerId);
      friendlybytebuf.writeResourceLocation(this.recipe);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlaceRecipe(this);
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   public int getContainerId() {
      return this.containerId;
   }
}
