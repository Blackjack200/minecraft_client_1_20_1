package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class WeatherCheck implements LootItemCondition {
   @Nullable
   final Boolean isRaining;
   @Nullable
   final Boolean isThundering;

   WeatherCheck(@Nullable Boolean obool, @Nullable Boolean obool1) {
      this.isRaining = obool;
      this.isThundering = obool1;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.WEATHER_CHECK;
   }

   public boolean test(LootContext lootcontext) {
      ServerLevel serverlevel = lootcontext.getLevel();
      if (this.isRaining != null && this.isRaining != serverlevel.isRaining()) {
         return false;
      } else {
         return this.isThundering == null || this.isThundering == serverlevel.isThundering();
      }
   }

   public static WeatherCheck.Builder weather() {
      return new WeatherCheck.Builder();
   }

   public static class Builder implements LootItemCondition.Builder {
      @Nullable
      private Boolean isRaining;
      @Nullable
      private Boolean isThundering;

      public WeatherCheck.Builder setRaining(@Nullable Boolean obool) {
         this.isRaining = obool;
         return this;
      }

      public WeatherCheck.Builder setThundering(@Nullable Boolean obool) {
         this.isThundering = obool;
         return this;
      }

      public WeatherCheck build() {
         return new WeatherCheck(this.isRaining, this.isThundering);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WeatherCheck> {
      public void serialize(JsonObject jsonobject, WeatherCheck weathercheck, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("raining", weathercheck.isRaining);
         jsonobject.addProperty("thundering", weathercheck.isThundering);
      }

      public WeatherCheck deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         Boolean obool = jsonobject.has("raining") ? GsonHelper.getAsBoolean(jsonobject, "raining") : null;
         Boolean obool1 = jsonobject.has("thundering") ? GsonHelper.getAsBoolean(jsonobject, "thundering") : null;
         return new WeatherCheck(obool, obool1);
      }
   }
}
