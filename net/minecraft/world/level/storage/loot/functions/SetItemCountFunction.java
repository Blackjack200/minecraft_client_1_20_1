package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetItemCountFunction extends LootItemConditionalFunction {
   final NumberProvider value;
   final boolean add;

   SetItemCountFunction(LootItemCondition[] alootitemcondition, NumberProvider numberprovider, boolean flag) {
      super(alootitemcondition);
      this.value = numberprovider;
      this.add = flag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_COUNT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      int i = this.add ? itemstack.getCount() : 0;
      itemstack.setCount(Mth.clamp(i + this.value.getInt(lootcontext), 0, itemstack.getMaxStackSize()));
      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberprovider) {
      return simpleBuilder((alootitemcondition) -> new SetItemCountFunction(alootitemcondition, numberprovider, false));
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberprovider, boolean flag) {
      return simpleBuilder((alootitemcondition) -> new SetItemCountFunction(alootitemcondition, numberprovider, flag));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
      public void serialize(JsonObject jsonobject, SetItemCountFunction setitemcountfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setitemcountfunction, jsonserializationcontext);
         jsonobject.add("count", jsonserializationcontext.serialize(setitemcountfunction.value));
         jsonobject.addProperty("add", setitemcountfunction.add);
      }

      public SetItemCountFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "count", jsondeserializationcontext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "add", false);
         return new SetItemCountFunction(alootitemcondition, numberprovider, flag);
      }
   }
}
