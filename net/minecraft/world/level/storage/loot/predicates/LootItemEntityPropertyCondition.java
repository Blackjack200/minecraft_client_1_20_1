package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootItemEntityPropertyCondition implements LootItemCondition {
   final EntityPredicate predicate;
   final LootContext.EntityTarget entityTarget;

   LootItemEntityPropertyCondition(EntityPredicate entitypredicate, LootContext.EntityTarget lootcontext_entitytarget) {
      this.predicate = entitypredicate;
      this.entityTarget = lootcontext_entitytarget;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ENTITY_PROPERTIES;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
   }

   public boolean test(LootContext lootcontext) {
      Entity entity = lootcontext.getParamOrNull(this.entityTarget.getParam());
      Vec3 vec3 = lootcontext.getParamOrNull(LootContextParams.ORIGIN);
      return this.predicate.matches(lootcontext.getLevel(), vec3, entity);
   }

   public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget lootcontext_entitytarget) {
      return hasProperties(lootcontext_entitytarget, EntityPredicate.Builder.entity());
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget lootcontext_entitytarget, EntityPredicate.Builder entitypredicate_builder) {
      return () -> new LootItemEntityPropertyCondition(entitypredicate_builder.build(), lootcontext_entitytarget);
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget lootcontext_entitytarget, EntityPredicate entitypredicate) {
      return () -> new LootItemEntityPropertyCondition(entitypredicate, lootcontext_entitytarget);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemEntityPropertyCondition> {
      public void serialize(JsonObject jsonobject, LootItemEntityPropertyCondition lootitementitypropertycondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("predicate", lootitementitypropertycondition.predicate.serializeToJson());
         jsonobject.add("entity", jsonserializationcontext.serialize(lootitementitypropertycondition.entityTarget));
      }

      public LootItemEntityPropertyCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("predicate"));
         return new LootItemEntityPropertyCondition(entitypredicate, GsonHelper.getAsObject(jsonobject, "entity", jsondeserializationcontext, LootContext.EntityTarget.class));
      }
   }
}
