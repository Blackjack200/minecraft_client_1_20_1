package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class IntRange {
   @Nullable
   final NumberProvider min;
   @Nullable
   final NumberProvider max;
   private final IntRange.IntLimiter limiter;
   private final IntRange.IntChecker predicate;

   public Set<LootContextParam<?>> getReferencedContextParams() {
      ImmutableSet.Builder<LootContextParam<?>> immutableset_builder = ImmutableSet.builder();
      if (this.min != null) {
         immutableset_builder.addAll(this.min.getReferencedContextParams());
      }

      if (this.max != null) {
         immutableset_builder.addAll(this.max.getReferencedContextParams());
      }

      return immutableset_builder.build();
   }

   IntRange(@Nullable NumberProvider numberprovider, @Nullable NumberProvider numberprovider1) {
      this.min = numberprovider;
      this.max = numberprovider1;
      if (numberprovider == null) {
         if (numberprovider1 == null) {
            this.limiter = (lootcontext7, l1) -> l1;
            this.predicate = (lootcontext6, k1) -> true;
         } else {
            this.limiter = (lootcontext5, j1) -> Math.min(numberprovider1.getInt(lootcontext5), j1);
            this.predicate = (lootcontext4, i1) -> i1 <= numberprovider1.getInt(lootcontext4);
         }
      } else if (numberprovider1 == null) {
         this.limiter = (lootcontext3, l) -> Math.max(numberprovider.getInt(lootcontext3), l);
         this.predicate = (lootcontext2, k) -> k >= numberprovider.getInt(lootcontext2);
      } else {
         this.limiter = (lootcontext1, j) -> Mth.clamp(j, numberprovider.getInt(lootcontext1), numberprovider1.getInt(lootcontext1));
         this.predicate = (lootcontext, i) -> i >= numberprovider.getInt(lootcontext) && i <= numberprovider1.getInt(lootcontext);
      }

   }

   public static IntRange exact(int i) {
      ConstantValue constantvalue = ConstantValue.exactly((float)i);
      return new IntRange(constantvalue, constantvalue);
   }

   public static IntRange range(int i, int j) {
      return new IntRange(ConstantValue.exactly((float)i), ConstantValue.exactly((float)j));
   }

   public static IntRange lowerBound(int i) {
      return new IntRange(ConstantValue.exactly((float)i), (NumberProvider)null);
   }

   public static IntRange upperBound(int i) {
      return new IntRange((NumberProvider)null, ConstantValue.exactly((float)i));
   }

   public int clamp(LootContext lootcontext, int i) {
      return this.limiter.apply(lootcontext, i);
   }

   public boolean test(LootContext lootcontext, int i) {
      return this.predicate.test(lootcontext, i);
   }

   @FunctionalInterface
   interface IntChecker {
      boolean test(LootContext lootcontext, int i);
   }

   @FunctionalInterface
   interface IntLimiter {
      int apply(LootContext lootcontext, int i);
   }

   public static class Serializer implements JsonDeserializer<IntRange>, JsonSerializer<IntRange> {
      public IntRange deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) {
         if (jsonelement.isJsonPrimitive()) {
            return IntRange.exact(jsonelement.getAsInt());
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "value");
            NumberProvider numberprovider = jsonobject.has("min") ? GsonHelper.getAsObject(jsonobject, "min", jsondeserializationcontext, NumberProvider.class) : null;
            NumberProvider numberprovider1 = jsonobject.has("max") ? GsonHelper.getAsObject(jsonobject, "max", jsondeserializationcontext, NumberProvider.class) : null;
            return new IntRange(numberprovider, numberprovider1);
         }
      }

      public JsonElement serialize(IntRange intrange, Type type, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         if (Objects.equals(intrange.max, intrange.min)) {
            return jsonserializationcontext.serialize(intrange.min);
         } else {
            if (intrange.max != null) {
               jsonobject.add("max", jsonserializationcontext.serialize(intrange.max));
            }

            if (intrange.min != null) {
               jsonobject.add("min", jsonserializationcontext.serialize(intrange.min));
            }

            return jsonobject;
         }
      }
   }
}
