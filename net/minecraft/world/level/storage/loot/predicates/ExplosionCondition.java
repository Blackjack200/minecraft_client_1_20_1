package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
   static final ExplosionCondition INSTANCE = new ExplosionCondition();

   private ExplosionCondition() {
   }

   public LootItemConditionType getType() {
      return LootItemConditions.SURVIVES_EXPLOSION;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
   }

   public boolean test(LootContext lootcontext) {
      Float ofloat = lootcontext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
      if (ofloat != null) {
         RandomSource randomsource = lootcontext.getRandom();
         float f = 1.0F / ofloat;
         return randomsource.nextFloat() <= f;
      } else {
         return true;
      }
   }

   public static LootItemCondition.Builder survivesExplosion() {
      return () -> INSTANCE;
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ExplosionCondition> {
      public void serialize(JsonObject jsonobject, ExplosionCondition explosioncondition, JsonSerializationContext jsonserializationcontext) {
      }

      public ExplosionCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         return ExplosionCondition.INSTANCE;
      }
   }
}
