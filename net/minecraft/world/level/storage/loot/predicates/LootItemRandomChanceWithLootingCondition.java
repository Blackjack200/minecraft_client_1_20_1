package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemRandomChanceWithLootingCondition implements LootItemCondition {
   final float percent;
   final float lootingMultiplier;

   LootItemRandomChanceWithLootingCondition(float f, float f1) {
      this.percent = f;
      this.lootingMultiplier = f1;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
   }

   public boolean test(LootContext lootcontext) {
      Entity entity = lootcontext.getParamOrNull(LootContextParams.KILLER_ENTITY);
      int i = 0;
      if (entity instanceof LivingEntity) {
         i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
      }

      return lootcontext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
   }

   public static LootItemCondition.Builder randomChanceAndLootingBoost(float f, float f1) {
      return () -> new LootItemRandomChanceWithLootingCondition(f, f1);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceWithLootingCondition> {
      public void serialize(JsonObject jsonobject, LootItemRandomChanceWithLootingCondition lootitemrandomchancewithlootingcondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("chance", lootitemrandomchancewithlootingcondition.percent);
         jsonobject.addProperty("looting_multiplier", lootitemrandomchancewithlootingcondition.lootingMultiplier);
      }

      public LootItemRandomChanceWithLootingCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(jsonobject, "chance"), GsonHelper.getAsFloat(jsonobject, "looting_multiplier"));
      }
   }
}
