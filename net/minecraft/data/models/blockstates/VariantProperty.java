package net.minecraft.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class VariantProperty<T> {
   final String key;
   final Function<T, JsonElement> serializer;

   public VariantProperty(String s, Function<T, JsonElement> function) {
      this.key = s;
      this.serializer = function;
   }

   public VariantProperty<T>.Value withValue(T object) {
      return new VariantProperty.Value(object);
   }

   public String toString() {
      return this.key;
   }

   public class Value {
      private final T value;

      public Value(T object) {
         this.value = object;
      }

      public VariantProperty<T> getKey() {
         return VariantProperty.this;
      }

      public void addToVariant(JsonObject jsonobject) {
         jsonobject.add(VariantProperty.this.key, VariantProperty.this.serializer.apply(this.value));
      }

      public String toString() {
         return VariantProperty.this.key + "=" + this.value;
      }
   }
}
