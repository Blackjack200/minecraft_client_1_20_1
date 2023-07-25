package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ValueCheckCondition implements LootItemCondition {
   final NumberProvider provider;
   final IntRange range;

   ValueCheckCondition(NumberProvider numberprovider, IntRange intrange) {
      this.provider = numberprovider;
      this.range = intrange;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.VALUE_CHECK;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
   }

   public boolean test(LootContext lootcontext) {
      return this.range.test(lootcontext, this.provider.getInt(lootcontext));
   }

   public static LootItemCondition.Builder hasValue(NumberProvider numberprovider, IntRange intrange) {
      return () -> new ValueCheckCondition(numberprovider, intrange);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValueCheckCondition> {
      public void serialize(JsonObject jsonobject, ValueCheckCondition valuecheckcondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("value", jsonserializationcontext.serialize(valuecheckcondition.provider));
         jsonobject.add("range", jsonserializationcontext.serialize(valuecheckcondition.range));
      }

      public ValueCheckCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "value", jsondeserializationcontext, NumberProvider.class);
         IntRange intrange = GsonHelper.getAsObject(jsonobject, "range", jsondeserializationcontext, IntRange.class);
         return new ValueCheckCondition(numberprovider, intrange);
      }
   }
}
