package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
   final LootItemCondition[] terms;
   private final Predicate<LootContext> composedPredicate;

   protected CompositeLootItemCondition(LootItemCondition[] alootitemcondition, Predicate<LootContext> predicate) {
      this.terms = alootitemcondition;
      this.composedPredicate = predicate;
   }

   public final boolean test(LootContext lootcontext) {
      return this.composedPredicate.test(lootcontext);
   }

   public void validate(ValidationContext validationcontext) {
      LootItemCondition.super.validate(validationcontext);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(validationcontext.forChild(".term[" + i + "]"));
      }

   }

   public abstract static class Builder implements LootItemCondition.Builder {
      private final List<LootItemCondition> terms = new ArrayList<>();

      public Builder(LootItemCondition.Builder... alootitemcondition_builder) {
         for(LootItemCondition.Builder lootitemcondition_builder : alootitemcondition_builder) {
            this.terms.add(lootitemcondition_builder.build());
         }

      }

      public void addTerm(LootItemCondition.Builder lootitemcondition_builder) {
         this.terms.add(lootitemcondition_builder.build());
      }

      public LootItemCondition build() {
         LootItemCondition[] alootitemcondition = this.terms.toArray((i) -> new LootItemCondition[i]);
         return this.create(alootitemcondition);
      }

      protected abstract LootItemCondition create(LootItemCondition[] alootitemcondition);
   }

   public abstract static class Serializer<T extends CompositeLootItemCondition> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      public void serialize(JsonObject jsonobject, CompositeLootItemCondition compositelootitemcondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("terms", jsonserializationcontext.serialize(compositelootitemcondition.terms));
      }

      public T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(jsonobject, "terms", jsondeserializationcontext, LootItemCondition[].class);
         return this.create(alootitemcondition);
      }

      protected abstract T create(LootItemCondition[] alootitemcondition);
   }
}
