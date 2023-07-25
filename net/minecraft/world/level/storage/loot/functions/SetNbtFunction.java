package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetNbtFunction extends LootItemConditionalFunction {
   final CompoundTag tag;

   SetNbtFunction(LootItemCondition[] alootitemcondition, CompoundTag compoundtag) {
      super(alootitemcondition);
      this.tag = compoundtag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_NBT;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      itemstack.getOrCreateTag().merge(this.tag);
      return itemstack;
   }

   /** @deprecated */
   @Deprecated
   public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag compoundtag) {
      return simpleBuilder((alootitemcondition) -> new SetNbtFunction(alootitemcondition, compoundtag));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
      public void serialize(JsonObject jsonobject, SetNbtFunction setnbtfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setnbtfunction, jsonserializationcontext);
         jsonobject.addProperty("tag", setnbtfunction.tag.toString());
      }

      public SetNbtFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(GsonHelper.getAsString(jsonobject, "tag"));
            return new SetNbtFunction(alootitemcondition, compoundtag);
         } catch (CommandSyntaxException var5) {
            throw new JsonSyntaxException(var5.getMessage());
         }
      }
   }
}
