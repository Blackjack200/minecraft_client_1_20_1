package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
   final LootContext.EntityTarget entityTarget;

   public FillPlayerHead(LootItemCondition[] alootitemcondition, LootContext.EntityTarget lootcontext_entitytarget) {
      super(alootitemcondition);
      this.entityTarget = lootcontext_entitytarget;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.FILL_PLAYER_HEAD;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.entityTarget.getParam());
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.is(Items.PLAYER_HEAD)) {
         Entity entity = lootcontext.getParamOrNull(this.entityTarget.getParam());
         if (entity instanceof Player) {
            GameProfile gameprofile = ((Player)entity).getGameProfile();
            itemstack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameprofile));
         }
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget lootcontext_entitytarget) {
      return simpleBuilder((alootitemcondition) -> new FillPlayerHead(alootitemcondition, lootcontext_entitytarget));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<FillPlayerHead> {
      public void serialize(JsonObject jsonobject, FillPlayerHead fillplayerhead, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, fillplayerhead, jsonserializationcontext);
         jsonobject.add("entity", jsonserializationcontext.serialize(fillplayerhead.entityTarget));
      }

      public FillPlayerHead deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         LootContext.EntityTarget lootcontext_entitytarget = GsonHelper.getAsObject(jsonobject, "entity", jsondeserializationcontext, LootContext.EntityTarget.class);
         return new FillPlayerHead(alootitemcondition, lootcontext_entitytarget);
      }
   }
}
