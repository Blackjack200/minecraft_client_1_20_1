package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
   final IntRange limiter;

   LimitCount(LootItemCondition[] alootitemcondition, IntRange intrange) {
      super(alootitemcondition);
      this.limiter = intrange;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.LIMIT_COUNT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.limiter.getReferencedContextParams();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      int i = this.limiter.clamp(lootcontext, itemstack.getCount());
      itemstack.setCount(i);
      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> limitCount(IntRange intrange) {
      return simpleBuilder((alootitemcondition) -> new LimitCount(alootitemcondition, intrange));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
      public void serialize(JsonObject jsonobject, LimitCount limitcount, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, limitcount, jsonserializationcontext);
         jsonobject.add("limit", jsonserializationcontext.serialize(limitcount.limiter));
      }

      public LimitCount deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         IntRange intrange = GsonHelper.getAsObject(jsonobject, "limit", jsondeserializationcontext, IntRange.class);
         return new LimitCount(alootitemcondition, intrange);
      }
   }
}
