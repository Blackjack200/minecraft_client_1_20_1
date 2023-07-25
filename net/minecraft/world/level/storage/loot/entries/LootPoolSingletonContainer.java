package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
   public static final int DEFAULT_WEIGHT = 1;
   public static final int DEFAULT_QUALITY = 0;
   protected final int weight;
   protected final int quality;
   protected final LootItemFunction[] functions;
   final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
      public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
         LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootcontext), lootcontext);
      }
   };

   protected LootPoolSingletonContainer(int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(alootitemcondition);
      this.weight = i;
      this.quality = j;
      this.functions = alootitemfunction;
      this.compositeFunction = LootItemFunctions.compose(alootitemfunction);
   }

   public void validate(ValidationContext validationcontext) {
      super.validate(validationcontext);

      for(int i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(validationcontext.forChild(".functions[" + i + "]"));
      }

   }

   protected abstract void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext);

   public boolean expand(LootContext lootcontext, Consumer<LootPoolEntry> consumer) {
      if (this.canRun(lootcontext)) {
         consumer.accept(this.entry);
         return true;
      } else {
         return false;
      }
   }

   public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor lootpoolsingletoncontainer_entryconstructor) {
      return new LootPoolSingletonContainer.DummyBuilder(lootpoolsingletoncontainer_entryconstructor);
   }

   public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>> extends LootPoolEntryContainer.Builder<T> implements FunctionUserBuilder<T> {
      protected int weight = 1;
      protected int quality = 0;
      private final List<LootItemFunction> functions = Lists.newArrayList();

      public T apply(LootItemFunction.Builder lootitemfunction_builder) {
         this.functions.add(lootitemfunction_builder.build());
         return this.getThis();
      }

      protected LootItemFunction[] getFunctions() {
         return this.functions.toArray(new LootItemFunction[0]);
      }

      public T setWeight(int i) {
         this.weight = i;
         return this.getThis();
      }

      public T setQuality(int i) {
         this.quality = i;
         return this.getThis();
      }
   }

   static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
      private final LootPoolSingletonContainer.EntryConstructor constructor;

      public DummyBuilder(LootPoolSingletonContainer.EntryConstructor lootpoolsingletoncontainer_entryconstructor) {
         this.constructor = lootpoolsingletoncontainer_entryconstructor;
      }

      protected LootPoolSingletonContainer.DummyBuilder getThis() {
         return this;
      }

      public LootPoolEntryContainer build() {
         return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }
   }

   protected abstract class EntryBase implements LootPoolEntry {
      public int getWeight(float f) {
         return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * f), 0);
      }
   }

   @FunctionalInterface
   protected interface EntryConstructor {
      LootPoolSingletonContainer build(int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction);
   }

   public abstract static class Serializer<T extends LootPoolSingletonContainer> extends LootPoolEntryContainer.Serializer<T> {
      public void serializeCustom(JsonObject jsonobject, T lootpoolsingletoncontainer, JsonSerializationContext jsonserializationcontext) {
         if (lootpoolsingletoncontainer.weight != 1) {
            jsonobject.addProperty("weight", lootpoolsingletoncontainer.weight);
         }

         if (lootpoolsingletoncontainer.quality != 0) {
            jsonobject.addProperty("quality", lootpoolsingletoncontainer.quality);
         }

         if (!ArrayUtils.isEmpty((Object[])lootpoolsingletoncontainer.functions)) {
            jsonobject.add("functions", jsonserializationcontext.serialize(lootpoolsingletoncontainer.functions));
         }

      }

      public final T deserializeCustom(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         int i = GsonHelper.getAsInt(jsonobject, "weight", 1);
         int j = GsonHelper.getAsInt(jsonobject, "quality", 0);
         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(jsonobject, "functions", new LootItemFunction[0], jsondeserializationcontext, LootItemFunction[].class);
         return this.deserialize(jsonobject, jsondeserializationcontext, i, j, alootitemcondition, alootitemfunction);
      }

      protected abstract T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction);
   }
}
