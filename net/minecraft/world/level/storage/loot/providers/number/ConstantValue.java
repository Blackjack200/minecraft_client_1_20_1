package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;

public final class ConstantValue implements NumberProvider {
   final float value;

   ConstantValue(float f) {
      this.value = f;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.CONSTANT;
   }

   public float getFloat(LootContext lootcontext) {
      return this.value;
   }

   public static ConstantValue exactly(float f) {
      return new ConstantValue(f);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         return Float.compare(((ConstantValue)object).value, this.value) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ConstantValue> {
      public JsonElement serialize(ConstantValue constantvalue, JsonSerializationContext jsonserializationcontext) {
         return new JsonPrimitive(constantvalue.value);
      }

      public ConstantValue deserialize(JsonElement jsonelement, JsonDeserializationContext jsondeserializationcontext) {
         return new ConstantValue(GsonHelper.convertToFloat(jsonelement, "value"));
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConstantValue> {
      public void serialize(JsonObject jsonobject, ConstantValue constantvalue, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("value", constantvalue.value);
      }

      public ConstantValue deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         float f = GsonHelper.getAsFloat(jsonobject, "value");
         return new ConstantValue(f);
      }
   }
}
