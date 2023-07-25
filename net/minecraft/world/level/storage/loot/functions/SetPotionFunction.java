package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
   final Potion potion;

   SetPotionFunction(LootItemCondition[] alootitemcondition, Potion potion) {
      super(alootitemcondition);
      this.potion = potion;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_POTION;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      PotionUtils.setPotion(itemstack, this.potion);
      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> setPotion(Potion potion) {
      return simpleBuilder((alootitemcondition) -> new SetPotionFunction(alootitemcondition, potion));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetPotionFunction> {
      public void serialize(JsonObject jsonobject, SetPotionFunction setpotionfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setpotionfunction, jsonserializationcontext);
         jsonobject.addProperty("id", BuiltInRegistries.POTION.getKey(setpotionfunction.potion).toString());
      }

      public SetPotionFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         String s = GsonHelper.getAsString(jsonobject, "id");
         Potion potion = BuiltInRegistries.POTION.getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + s + "'"));
         return new SetPotionFunction(alootitemcondition, potion);
      }
   }
}
