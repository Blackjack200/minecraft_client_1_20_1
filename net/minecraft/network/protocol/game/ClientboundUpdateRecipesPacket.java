package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
   private final List<Recipe<?>> recipes;

   public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> collection) {
      this.recipes = Lists.newArrayList(collection);
   }

   public ClientboundUpdateRecipesPacket(FriendlyByteBuf friendlybytebuf) {
      this.recipes = friendlybytebuf.readList(ClientboundUpdateRecipesPacket::fromNetwork);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this.recipes, ClientboundUpdateRecipesPacket::toNetwork);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleUpdateRecipes(this);
   }

   public List<Recipe<?>> getRecipes() {
      return this.recipes;
   }

   public static Recipe<?> fromNetwork(FriendlyByteBuf friendlybytebuf) {
      ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
      ResourceLocation resourcelocation1 = friendlybytebuf.readResourceLocation();
      return BuiltInRegistries.RECIPE_SERIALIZER.getOptional(resourcelocation).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourcelocation)).fromNetwork(resourcelocation1, friendlybytebuf);
   }

   public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf friendlybytebuf, T recipe) {
      friendlybytebuf.writeResourceLocation(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer()));
      friendlybytebuf.writeResourceLocation(recipe.getId());
      recipe.getSerializer().toNetwork(friendlybytebuf, recipe);
   }
}
