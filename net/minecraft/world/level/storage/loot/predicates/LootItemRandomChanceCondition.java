package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemRandomChanceCondition implements LootItemCondition {
   final float probability;

   LootItemRandomChanceCondition(float f) {
      this.probability = f;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE;
   }

   public boolean test(LootContext lootcontext) {
      return lootcontext.getRandom().nextFloat() < this.probability;
   }

   public static LootItemCondition.Builder randomChance(float f) {
      return () -> new LootItemRandomChanceCondition(f);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceCondition> {
      public void serialize(JsonObject jsonobject, LootItemRandomChanceCondition lootitemrandomchancecondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("chance", lootitemrandomchancecondition.probability);
      }

      public LootItemRandomChanceCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(jsonobject, "chance"));
      }
   }
}
