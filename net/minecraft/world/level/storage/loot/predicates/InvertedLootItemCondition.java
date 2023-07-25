package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class InvertedLootItemCondition implements LootItemCondition {
   final LootItemCondition term;

   InvertedLootItemCondition(LootItemCondition lootitemcondition) {
      this.term = lootitemcondition;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.INVERTED;
   }

   public final boolean test(LootContext lootcontext) {
      return !this.term.test(lootcontext);
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.term.getReferencedContextParams();
   }

   public void validate(ValidationContext validationcontext) {
      LootItemCondition.super.validate(validationcontext);
      this.term.validate(validationcontext);
   }

   public static LootItemCondition.Builder invert(LootItemCondition.Builder lootitemcondition_builder) {
      InvertedLootItemCondition invertedlootitemcondition = new InvertedLootItemCondition(lootitemcondition_builder.build());
      return () -> invertedlootitemcondition;
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<InvertedLootItemCondition> {
      public void serialize(JsonObject jsonobject, InvertedLootItemCondition invertedlootitemcondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("term", jsonserializationcontext.serialize(invertedlootitemcondition.term));
      }

      public InvertedLootItemCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LootItemCondition lootitemcondition = GsonHelper.getAsObject(jsonobject, "term", jsondeserializationcontext, LootItemCondition.class);
         return new InvertedLootItemCondition(lootitemcondition);
      }
   }
}
