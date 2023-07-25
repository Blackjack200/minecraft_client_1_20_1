package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
   private final ClientboundRecipePacket.State state;
   private final List<ResourceLocation> recipes;
   private final List<ResourceLocation> toHighlight;
   private final RecipeBookSettings bookSettings;

   public ClientboundRecipePacket(ClientboundRecipePacket.State clientboundrecipepacket_state, Collection<ResourceLocation> collection, Collection<ResourceLocation> collection1, RecipeBookSettings recipebooksettings) {
      this.state = clientboundrecipepacket_state;
      this.recipes = ImmutableList.copyOf(collection);
      this.toHighlight = ImmutableList.copyOf(collection1);
      this.bookSettings = recipebooksettings;
   }

   public ClientboundRecipePacket(FriendlyByteBuf friendlybytebuf) {
      this.state = friendlybytebuf.readEnum(ClientboundRecipePacket.State.class);
      this.bookSettings = RecipeBookSettings.read(friendlybytebuf);
      this.recipes = friendlybytebuf.readList(FriendlyByteBuf::readResourceLocation);
      if (this.state == ClientboundRecipePacket.State.INIT) {
         this.toHighlight = friendlybytebuf.readList(FriendlyByteBuf::readResourceLocation);
      } else {
         this.toHighlight = ImmutableList.of();
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.state);
      this.bookSettings.write(friendlybytebuf);
      friendlybytebuf.writeCollection(this.recipes, FriendlyByteBuf::writeResourceLocation);
      if (this.state == ClientboundRecipePacket.State.INIT) {
         friendlybytebuf.writeCollection(this.toHighlight, FriendlyByteBuf::writeResourceLocation);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAddOrRemoveRecipes(this);
   }

   public List<ResourceLocation> getRecipes() {
      return this.recipes;
   }

   public List<ResourceLocation> getHighlights() {
      return this.toHighlight;
   }

   public RecipeBookSettings getBookSettings() {
      return this.bookSettings;
   }

   public ClientboundRecipePacket.State getState() {
      return this.state;
   }

   public static enum State {
      INIT,
      ADD,
      REMOVE;
   }
}
