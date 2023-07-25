package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
   protected final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositeCondition;

   protected LootPoolEntryContainer(LootItemCondition[] alootitemcondition) {
      this.conditions = alootitemcondition;
      this.compositeCondition = LootItemConditions.andConditions(alootitemcondition);
   }

   public void validate(ValidationContext validationcontext) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(validationcontext.forChild(".condition[" + i + "]"));
      }

   }

   protected final boolean canRun(LootContext lootcontext) {
      return this.compositeCondition.test(lootcontext);
   }

   public abstract LootPoolEntryType getType();

   public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
      private final List<LootItemCondition> conditions = Lists.newArrayList();

      protected abstract T getThis();

      public T when(LootItemCondition.Builder lootitemcondition_builder) {
         this.conditions.add(lootitemcondition_builder.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected LootItemCondition[] getConditions() {
         return this.conditions.toArray(new LootItemCondition[0]);
      }

      public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         return new AlternativesEntry.Builder(this, lootpoolentrycontainer_builder);
      }

      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         return new EntryGroup.Builder(this, lootpoolentrycontainer_builder);
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         return new SequentialEntry.Builder(this, lootpoolentrycontainer_builder);
      }

      public abstract LootPoolEntryContainer build();
   }

   public abstract static class Serializer<T extends LootPoolEntryContainer> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      public final void serialize(JsonObject jsonobject, T lootpoolentrycontainer, JsonSerializationContext jsonserializationcontext) {
         if (!ArrayUtils.isEmpty((Object[])lootpoolentrycontainer.conditions)) {
            jsonobject.add("conditions", jsonserializationcontext.serialize(lootpoolentrycontainer.conditions));
         }

         this.serializeCustom(jsonobject, lootpoolentrycontainer, jsonserializationcontext);
      }

      public final T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(jsonobject, "conditions", new LootItemCondition[0], jsondeserializationcontext, LootItemCondition[].class);
         return this.deserializeCustom(jsonobject, jsondeserializationcontext, alootitemcondition);
      }

      public abstract void serializeCustom(JsonObject jsonobject, T lootpoolentrycontainer, JsonSerializationContext jsonserializationcontext);

      public abstract T deserializeCustom(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition);
   }
}
