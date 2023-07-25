package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
   public static final String RECIPE_BOOK_TAG = "recipeBook";
   private static final Logger LOGGER = LogUtils.getLogger();

   public int addRecipes(Collection<Recipe<?>> collection, ServerPlayer serverplayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(Recipe<?> recipe : collection) {
         ResourceLocation resourcelocation = recipe.getId();
         if (!this.known.contains(resourcelocation) && !recipe.isSpecial()) {
            this.add(resourcelocation);
            this.addHighlight(resourcelocation);
            list.add(resourcelocation);
            CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverplayer, recipe);
            ++i;
         }
      }

      if (list.size() > 0) {
         this.sendRecipes(ClientboundRecipePacket.State.ADD, serverplayer, list);
      }

      return i;
   }

   public int removeRecipes(Collection<Recipe<?>> collection, ServerPlayer serverplayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(Recipe<?> recipe : collection) {
         ResourceLocation resourcelocation = recipe.getId();
         if (this.known.contains(resourcelocation)) {
            this.remove(resourcelocation);
            list.add(resourcelocation);
            ++i;
         }
      }

      this.sendRecipes(ClientboundRecipePacket.State.REMOVE, serverplayer, list);
      return i;
   }

   private void sendRecipes(ClientboundRecipePacket.State clientboundrecipepacket_state, ServerPlayer serverplayer, List<ResourceLocation> list) {
      serverplayer.connection.send(new ClientboundRecipePacket(clientboundrecipepacket_state, list, Collections.emptyList(), this.getBookSettings()));
   }

   public CompoundTag toNbt() {
      CompoundTag compoundtag = new CompoundTag();
      this.getBookSettings().write(compoundtag);
      ListTag listtag = new ListTag();

      for(ResourceLocation resourcelocation : this.known) {
         listtag.add(StringTag.valueOf(resourcelocation.toString()));
      }

      compoundtag.put("recipes", listtag);
      ListTag listtag1 = new ListTag();

      for(ResourceLocation resourcelocation1 : this.highlight) {
         listtag1.add(StringTag.valueOf(resourcelocation1.toString()));
      }

      compoundtag.put("toBeDisplayed", listtag1);
      return compoundtag;
   }

   public void fromNbt(CompoundTag compoundtag, RecipeManager recipemanager) {
      this.setBookSettings(RecipeBookSettings.read(compoundtag));
      ListTag listtag = compoundtag.getList("recipes", 8);
      this.loadRecipes(listtag, this::add, recipemanager);
      ListTag listtag1 = compoundtag.getList("toBeDisplayed", 8);
      this.loadRecipes(listtag1, this::addHighlight, recipemanager);
   }

   private void loadRecipes(ListTag listtag, Consumer<Recipe<?>> consumer, RecipeManager recipemanager) {
      for(int i = 0; i < listtag.size(); ++i) {
         String s = listtag.getString(i);

         try {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            Optional<? extends Recipe<?>> optional = recipemanager.byKey(resourcelocation);
            if (!optional.isPresent()) {
               LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)resourcelocation);
            } else {
               consumer.accept(optional.get());
            }
         } catch (ResourceLocationException var8) {
            LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", (Object)s);
         }
      }

   }

   public void sendInitialRecipeBook(ServerPlayer serverplayer) {
      serverplayer.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
   }
}
