package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class AdvancementRewards {
   public static final AdvancementRewards EMPTY = new AdvancementRewards(0, new ResourceLocation[0], new ResourceLocation[0], CommandFunction.CacheableFunction.NONE);
   private final int experience;
   private final ResourceLocation[] loot;
   private final ResourceLocation[] recipes;
   private final CommandFunction.CacheableFunction function;

   public AdvancementRewards(int i, ResourceLocation[] aresourcelocation, ResourceLocation[] aresourcelocation1, CommandFunction.CacheableFunction commandfunction_cacheablefunction) {
      this.experience = i;
      this.loot = aresourcelocation;
      this.recipes = aresourcelocation1;
      this.function = commandfunction_cacheablefunction;
   }

   public ResourceLocation[] getRecipes() {
      return this.recipes;
   }

   public void grant(ServerPlayer serverplayer) {
      serverplayer.giveExperiencePoints(this.experience);
      LootParams lootparams = (new LootParams.Builder(serverplayer.serverLevel())).withParameter(LootContextParams.THIS_ENTITY, serverplayer).withParameter(LootContextParams.ORIGIN, serverplayer.position()).create(LootContextParamSets.ADVANCEMENT_REWARD);
      boolean flag = false;

      for(ResourceLocation resourcelocation : this.loot) {
         for(ItemStack itemstack : serverplayer.server.getLootData().getLootTable(resourcelocation).getRandomItems(lootparams)) {
            if (serverplayer.addItem(itemstack)) {
               serverplayer.level().playSound((Player)null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverplayer.getRandom().nextFloat() - serverplayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
               flag = true;
            } else {
               ItemEntity itementity = serverplayer.drop(itemstack, false);
               if (itementity != null) {
                  itementity.setNoPickUpDelay();
                  itementity.setTarget(serverplayer.getUUID());
               }
            }
         }
      }

      if (flag) {
         serverplayer.containerMenu.broadcastChanges();
      }

      if (this.recipes.length > 0) {
         serverplayer.awardRecipesByKey(this.recipes);
      }

      MinecraftServer minecraftserver = serverplayer.server;
      this.function.get(minecraftserver.getFunctions()).ifPresent((commandfunction) -> minecraftserver.getFunctions().execute(commandfunction, serverplayer.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
   }

   public String toString() {
      return "AdvancementRewards{experience=" + this.experience + ", loot=" + Arrays.toString((Object[])this.loot) + ", recipes=" + Arrays.toString((Object[])this.recipes) + ", function=" + this.function + "}";
   }

   public JsonElement serializeToJson() {
      if (this == EMPTY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.experience != 0) {
            jsonobject.addProperty("experience", this.experience);
         }

         if (this.loot.length > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ResourceLocation resourcelocation : this.loot) {
               jsonarray.add(resourcelocation.toString());
            }

            jsonobject.add("loot", jsonarray);
         }

         if (this.recipes.length > 0) {
            JsonArray jsonarray1 = new JsonArray();

            for(ResourceLocation resourcelocation1 : this.recipes) {
               jsonarray1.add(resourcelocation1.toString());
            }

            jsonobject.add("recipes", jsonarray1);
         }

         if (this.function.getId() != null) {
            jsonobject.addProperty("function", this.function.getId().toString());
         }

         return jsonobject;
      }
   }

   public static AdvancementRewards deserialize(JsonObject jsonobject) throws JsonParseException {
      int i = GsonHelper.getAsInt(jsonobject, "experience", 0);
      JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "loot", new JsonArray());
      ResourceLocation[] aresourcelocation = new ResourceLocation[jsonarray.size()];

      for(int j = 0; j < aresourcelocation.length; ++j) {
         aresourcelocation[j] = new ResourceLocation(GsonHelper.convertToString(jsonarray.get(j), "loot[" + j + "]"));
      }

      JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "recipes", new JsonArray());
      ResourceLocation[] aresourcelocation1 = new ResourceLocation[jsonarray1.size()];

      for(int k = 0; k < aresourcelocation1.length; ++k) {
         aresourcelocation1[k] = new ResourceLocation(GsonHelper.convertToString(jsonarray1.get(k), "recipes[" + k + "]"));
      }

      CommandFunction.CacheableFunction commandfunction_cacheablefunction;
      if (jsonobject.has("function")) {
         commandfunction_cacheablefunction = new CommandFunction.CacheableFunction(new ResourceLocation(GsonHelper.getAsString(jsonobject, "function")));
      } else {
         commandfunction_cacheablefunction = CommandFunction.CacheableFunction.NONE;
      }

      return new AdvancementRewards(i, aresourcelocation, aresourcelocation1, commandfunction_cacheablefunction);
   }

   public static class Builder {
      private int experience;
      private final List<ResourceLocation> loot = Lists.newArrayList();
      private final List<ResourceLocation> recipes = Lists.newArrayList();
      @Nullable
      private ResourceLocation function;

      public static AdvancementRewards.Builder experience(int i) {
         return (new AdvancementRewards.Builder()).addExperience(i);
      }

      public AdvancementRewards.Builder addExperience(int i) {
         this.experience += i;
         return this;
      }

      public static AdvancementRewards.Builder loot(ResourceLocation resourcelocation) {
         return (new AdvancementRewards.Builder()).addLootTable(resourcelocation);
      }

      public AdvancementRewards.Builder addLootTable(ResourceLocation resourcelocation) {
         this.loot.add(resourcelocation);
         return this;
      }

      public static AdvancementRewards.Builder recipe(ResourceLocation resourcelocation) {
         return (new AdvancementRewards.Builder()).addRecipe(resourcelocation);
      }

      public AdvancementRewards.Builder addRecipe(ResourceLocation resourcelocation) {
         this.recipes.add(resourcelocation);
         return this;
      }

      public static AdvancementRewards.Builder function(ResourceLocation resourcelocation) {
         return (new AdvancementRewards.Builder()).runs(resourcelocation);
      }

      public AdvancementRewards.Builder runs(ResourceLocation resourcelocation) {
         this.function = resourcelocation;
         return this;
      }

      public AdvancementRewards build() {
         return new AdvancementRewards(this.experience, this.loot.toArray(new ResourceLocation[0]), this.recipes.toArray(new ResourceLocation[0]), this.function == null ? CommandFunction.CacheableFunction.NONE : new CommandFunction.CacheableFunction(this.function));
      }
   }
}
