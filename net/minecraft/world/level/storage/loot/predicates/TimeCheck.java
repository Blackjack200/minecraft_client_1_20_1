package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TimeCheck implements LootItemCondition {
   @Nullable
   final Long period;
   final IntRange value;

   TimeCheck(@Nullable Long olong, IntRange intrange) {
      this.period = olong;
      this.value = intrange;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.TIME_CHECK;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   public boolean test(LootContext lootcontext) {
      ServerLevel serverlevel = lootcontext.getLevel();
      long i = serverlevel.getDayTime();
      if (this.period != null) {
         i %= this.period;
      }

      return this.value.test(lootcontext, (int)i);
   }

   public static TimeCheck.Builder time(IntRange intrange) {
      return new TimeCheck.Builder(intrange);
   }

   public static class Builder implements LootItemCondition.Builder {
      @Nullable
      private Long period;
      private final IntRange value;

      public Builder(IntRange intrange) {
         this.value = intrange;
      }

      public TimeCheck.Builder setPeriod(long i) {
         this.period = i;
         return this;
      }

      public TimeCheck build() {
         return new TimeCheck(this.period, this.value);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
      public void serialize(JsonObject jsonobject, TimeCheck timecheck, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("period", timecheck.period);
         jsonobject.add("value", jsonserializationcontext.serialize(timecheck.value));
      }

      public TimeCheck deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         Long olong = jsonobject.has("period") ? GsonHelper.getAsLong(jsonobject, "period") : null;
         IntRange intrange = GsonHelper.getAsObject(jsonobject, "value", jsondeserializationcontext, IntRange.class);
         return new TimeCheck(olong, intrange);
      }
   }
}
