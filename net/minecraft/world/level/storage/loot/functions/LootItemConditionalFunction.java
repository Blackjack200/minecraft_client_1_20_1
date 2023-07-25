package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootItemConditionalFunction implements LootItemFunction {
   protected final LootItemCondition[] predicates;
   private final Predicate<LootContext> compositePredicates;

   protected LootItemConditionalFunction(LootItemCondition[] alootitemcondition) {
      this.predicates = alootitemcondition;
      this.compositePredicates = LootItemConditions.andConditions(alootitemcondition);
   }

   public final ItemStack apply(ItemStack itemstack, LootContext lootcontext) {
      return this.compositePredicates.test(lootcontext) ? this.run(itemstack, lootcontext) : itemstack;
   }

   protected abstract ItemStack run(ItemStack itemstack, LootContext lootcontext);

   public void validate(ValidationContext validationcontext) {
      LootItemFunction.super.validate(validationcontext);

      for(int i = 0; i < this.predicates.length; ++i) {
         this.predicates[i].validate(validationcontext.forChild(".conditions[" + i + "]"));
      }

   }

   protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> function) {
      return new LootItemConditionalFunction.DummyBuilder(function);
   }

   public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
      private final List<LootItemCondition> conditions = Lists.newArrayList();

      public T when(LootItemCondition.Builder lootitemcondition_builder) {
         this.conditions.add(lootitemcondition_builder.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected abstract T getThis();

      protected LootItemCondition[] getConditions() {
         return this.conditions.toArray(new LootItemCondition[0]);
      }
   }

   static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
      private final Function<LootItemCondition[], LootItemFunction> constructor;

      public DummyBuilder(Function<LootItemCondition[], LootItemFunction> function) {
         this.constructor = function;
      }

      protected LootItemConditionalFunction.DummyBuilder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return this.constructor.apply(this.getConditions());
      }
   }

   public abstract static class Serializer<T extends LootItemConditionalFunction> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      public void serialize(JsonObject jsonobject, T lootitemconditionalfunction, JsonSerializationContext jsonserializationcontext) {
         if (!ArrayUtils.isEmpty((Object[])lootitemconditionalfunction.predicates)) {
            jsonobject.add("conditions", jsonserializationcontext.serialize(lootitemconditionalfunction.predicates));
         }

      }

      public final T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(jsonobject, "conditions", new LootItemCondition[0], jsondeserializationcontext, LootItemCondition[].class);
         return this.deserialize(jsonobject, jsondeserializationcontext, alootitemcondition);
      }

      public abstract T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition);
   }
}
