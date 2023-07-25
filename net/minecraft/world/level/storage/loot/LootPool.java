package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
   final LootPoolEntryContainer[] entries;
   final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositeCondition;
   final LootItemFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   final NumberProvider rolls;
   final NumberProvider bonusRolls;

   LootPool(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction, NumberProvider numberprovider, NumberProvider numberprovider1) {
      this.entries = alootpoolentrycontainer;
      this.conditions = alootitemcondition;
      this.compositeCondition = LootItemConditions.andConditions(alootitemcondition);
      this.functions = alootitemfunction;
      this.compositeFunction = LootItemFunctions.compose(alootitemfunction);
      this.rolls = numberprovider;
      this.bonusRolls = numberprovider1;
   }

   private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootcontext) {
      RandomSource randomsource = lootcontext.getRandom();
      List<LootPoolEntry> list = Lists.newArrayList();
      MutableInt mutableint = new MutableInt();

      for(LootPoolEntryContainer lootpoolentrycontainer : this.entries) {
         lootpoolentrycontainer.expand(lootcontext, (lootpoolentry1) -> {
            int k = lootpoolentry1.getWeight(lootcontext.getLuck());
            if (k > 0) {
               list.add(lootpoolentry1);
               mutableint.add(k);
            }

         });
      }

      int i = list.size();
      if (mutableint.intValue() != 0 && i != 0) {
         if (i == 1) {
            list.get(0).createItemStack(consumer, lootcontext);
         } else {
            int j = randomsource.nextInt(mutableint.intValue());

            for(LootPoolEntry lootpoolentry : list) {
               j -= lootpoolentry.getWeight(lootcontext.getLuck());
               if (j < 0) {
                  lootpoolentry.createItemStack(consumer, lootcontext);
                  return;
               }
            }

         }
      }
   }

   public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootcontext) {
      if (this.compositeCondition.test(lootcontext)) {
         Consumer<ItemStack> consumer1 = LootItemFunction.decorate(this.compositeFunction, consumer, lootcontext);
         int i = this.rolls.getInt(lootcontext) + Mth.floor(this.bonusRolls.getFloat(lootcontext) * lootcontext.getLuck());

         for(int j = 0; j < i; ++j) {
            this.addRandomItem(consumer1, lootcontext);
         }

      }
   }

   public void validate(ValidationContext validationcontext) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(validationcontext.forChild(".condition[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(validationcontext.forChild(".functions[" + j + "]"));
      }

      for(int k = 0; k < this.entries.length; ++k) {
         this.entries[k].validate(validationcontext.forChild(".entries[" + k + "]"));
      }

      this.rolls.validate(validationcontext.forChild(".rolls"));
      this.bonusRolls.validate(validationcontext.forChild(".bonusRolls"));
   }

   public static LootPool.Builder lootPool() {
      return new LootPool.Builder();
   }

   public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
      private final List<LootItemCondition> conditions = Lists.newArrayList();
      private final List<LootItemFunction> functions = Lists.newArrayList();
      private NumberProvider rolls = ConstantValue.exactly(1.0F);
      private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

      public LootPool.Builder setRolls(NumberProvider numberprovider) {
         this.rolls = numberprovider;
         return this;
      }

      public LootPool.Builder unwrap() {
         return this;
      }

      public LootPool.Builder setBonusRolls(NumberProvider numberprovider) {
         this.bonusRolls = numberprovider;
         return this;
      }

      public LootPool.Builder add(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         this.entries.add(lootpoolentrycontainer_builder.build());
         return this;
      }

      public LootPool.Builder when(LootItemCondition.Builder lootitemcondition_builder) {
         this.conditions.add(lootitemcondition_builder.build());
         return this;
      }

      public LootPool.Builder apply(LootItemFunction.Builder lootitemfunction_builder) {
         this.functions.add(lootitemfunction_builder.build());
         return this;
      }

      public LootPool build() {
         if (this.rolls == null) {
            throw new IllegalArgumentException("Rolls not set");
         } else {
            return new LootPool(this.entries.toArray(new LootPoolEntryContainer[0]), this.conditions.toArray(new LootItemCondition[0]), this.functions.toArray(new LootItemFunction[0]), this.rolls, this.bonusRolls);
         }
      }
   }

   public static class Serializer implements JsonDeserializer<LootPool>, JsonSerializer<LootPool> {
      public LootPool deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "loot pool");
         LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(jsonobject, "entries", jsondeserializationcontext, LootPoolEntryContainer[].class);
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(jsonobject, "conditions", new LootItemCondition[0], jsondeserializationcontext, LootItemCondition[].class);
         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(jsonobject, "functions", new LootItemFunction[0], jsondeserializationcontext, LootItemFunction[].class);
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "rolls", jsondeserializationcontext, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(jsonobject, "bonus_rolls", ConstantValue.exactly(0.0F), jsondeserializationcontext, NumberProvider.class);
         return new LootPool(alootpoolentrycontainer, alootitemcondition, alootitemfunction, numberprovider, numberprovider1);
      }

      public JsonElement serialize(LootPool lootpool, Type type, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("rolls", jsonserializationcontext.serialize(lootpool.rolls));
         jsonobject.add("bonus_rolls", jsonserializationcontext.serialize(lootpool.bonusRolls));
         jsonobject.add("entries", jsonserializationcontext.serialize(lootpool.entries));
         if (!ArrayUtils.isEmpty((Object[])lootpool.conditions)) {
            jsonobject.add("conditions", jsonserializationcontext.serialize(lootpool.conditions));
         }

         if (!ArrayUtils.isEmpty((Object[])lootpool.functions)) {
            jsonobject.add("functions", jsonserializationcontext.serialize(lootpool.functions));
         }

         return jsonobject;
      }
   }
}
