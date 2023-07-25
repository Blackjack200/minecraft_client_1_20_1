package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class EntityHasScoreCondition implements LootItemCondition {
   final Map<String, IntRange> scores;
   final LootContext.EntityTarget entityTarget;

   EntityHasScoreCondition(Map<String, IntRange> map, LootContext.EntityTarget lootcontext_entitytarget) {
      this.scores = ImmutableMap.copyOf(map);
      this.entityTarget = lootcontext_entitytarget;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ENTITY_SCORES;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Stream.concat(Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap((intrange) -> intrange.getReferencedContextParams().stream())).collect(ImmutableSet.toImmutableSet());
   }

   public boolean test(LootContext lootcontext) {
      Entity entity = lootcontext.getParamOrNull(this.entityTarget.getParam());
      if (entity == null) {
         return false;
      } else {
         Scoreboard scoreboard = entity.level().getScoreboard();

         for(Map.Entry<String, IntRange> map_entry : this.scores.entrySet()) {
            if (!this.hasScore(lootcontext, entity, scoreboard, map_entry.getKey(), map_entry.getValue())) {
               return false;
            }
         }

         return true;
      }
   }

   protected boolean hasScore(LootContext lootcontext, Entity entity, Scoreboard scoreboard, String s, IntRange intrange) {
      Objective objective = scoreboard.getObjective(s);
      if (objective == null) {
         return false;
      } else {
         String s1 = entity.getScoreboardName();
         return !scoreboard.hasPlayerScore(s1, objective) ? false : intrange.test(lootcontext, scoreboard.getOrCreatePlayerScore(s1, objective).getScore());
      }
   }

   public static EntityHasScoreCondition.Builder hasScores(LootContext.EntityTarget lootcontext_entitytarget) {
      return new EntityHasScoreCondition.Builder(lootcontext_entitytarget);
   }

   public static class Builder implements LootItemCondition.Builder {
      private final Map<String, IntRange> scores = Maps.newHashMap();
      private final LootContext.EntityTarget entityTarget;

      public Builder(LootContext.EntityTarget lootcontext_entitytarget) {
         this.entityTarget = lootcontext_entitytarget;
      }

      public EntityHasScoreCondition.Builder withScore(String s, IntRange intrange) {
         this.scores.put(s, intrange);
         return this;
      }

      public LootItemCondition build() {
         return new EntityHasScoreCondition(this.scores, this.entityTarget);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
      public void serialize(JsonObject jsonobject, EntityHasScoreCondition entityhasscorecondition, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<String, IntRange> map_entry : entityhasscorecondition.scores.entrySet()) {
            jsonobject1.add(map_entry.getKey(), jsonserializationcontext.serialize(map_entry.getValue()));
         }

         jsonobject.add("scores", jsonobject1);
         jsonobject.add("entity", jsonserializationcontext.serialize(entityhasscorecondition.entityTarget));
      }

      public EntityHasScoreCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         Set<Map.Entry<String, JsonElement>> set = GsonHelper.getAsJsonObject(jsonobject, "scores").entrySet();
         Map<String, IntRange> map = Maps.newLinkedHashMap();

         for(Map.Entry<String, JsonElement> map_entry : set) {
            map.put(map_entry.getKey(), GsonHelper.convertToObject(map_entry.getValue(), "score", jsondeserializationcontext, IntRange.class));
         }

         return new EntityHasScoreCondition(map, GsonHelper.getAsObject(jsonobject, "entity", jsondeserializationcontext, LootContext.EntityTarget.class));
      }
   }
}
