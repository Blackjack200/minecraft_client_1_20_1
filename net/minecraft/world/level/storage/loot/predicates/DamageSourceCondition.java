package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class DamageSourceCondition implements LootItemCondition {
   final DamageSourcePredicate predicate;

   DamageSourceCondition(DamageSourcePredicate damagesourcepredicate) {
      this.predicate = damagesourcepredicate;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN, LootContextParams.DAMAGE_SOURCE);
   }

   public boolean test(LootContext lootcontext) {
      DamageSource damagesource = lootcontext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
      Vec3 vec3 = lootcontext.getParamOrNull(LootContextParams.ORIGIN);
      return vec3 != null && damagesource != null && this.predicate.matches(lootcontext.getLevel(), vec3, damagesource);
   }

   public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder damagesourcepredicate_builder) {
      return () -> new DamageSourceCondition(damagesourcepredicate_builder.build());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageSourceCondition> {
      public void serialize(JsonObject jsonobject, DamageSourceCondition damagesourcecondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("predicate", damagesourcecondition.predicate.serializeToJson());
      }

      public DamageSourceCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(jsonobject.get("predicate"));
         return new DamageSourceCondition(damagesourcepredicate);
      }
   }
}
