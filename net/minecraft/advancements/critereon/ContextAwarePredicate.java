package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
   public static final ContextAwarePredicate ANY = new ContextAwarePredicate(new LootItemCondition[0]);
   private final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositePredicates;

   ContextAwarePredicate(LootItemCondition[] alootitemcondition) {
      this.conditions = alootitemcondition;
      this.compositePredicates = LootItemConditions.andConditions(alootitemcondition);
   }

   public static ContextAwarePredicate create(LootItemCondition... alootitemcondition) {
      return new ContextAwarePredicate(alootitemcondition);
   }

   @Nullable
   public static ContextAwarePredicate fromElement(String s, DeserializationContext deserializationcontext, @Nullable JsonElement jsonelement, LootContextParamSet lootcontextparamset) {
      if (jsonelement != null && jsonelement.isJsonArray()) {
         LootItemCondition[] alootitemcondition = deserializationcontext.deserializeConditions(jsonelement.getAsJsonArray(), deserializationcontext.getAdvancementId() + "/" + s, lootcontextparamset);
         return new ContextAwarePredicate(alootitemcondition);
      } else {
         return null;
      }
   }

   public boolean matches(LootContext lootcontext) {
      return this.compositePredicates.test(lootcontext);
   }

   public JsonElement toJson(SerializationContext serializationcontext) {
      return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : serializationcontext.serializeConditions(this.conditions));
   }

   public static JsonElement toJson(ContextAwarePredicate[] acontextawarepredicate, SerializationContext serializationcontext) {
      if (acontextawarepredicate.length == 0) {
         return JsonNull.INSTANCE;
      } else {
         JsonArray jsonarray = new JsonArray();

         for(ContextAwarePredicate contextawarepredicate : acontextawarepredicate) {
            jsonarray.add(contextawarepredicate.toJson(serializationcontext));
         }

         return jsonarray;
      }
   }
}
