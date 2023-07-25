package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Variant implements Supplier<JsonElement> {
   private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.newLinkedHashMap();

   public <T> Variant with(VariantProperty<T> variantproperty, T object) {
      VariantProperty<?>.Value variantproperty_value = this.values.put(variantproperty, variantproperty.withValue(object));
      if (variantproperty_value != null) {
         throw new IllegalStateException("Replacing value of " + variantproperty_value + " with " + object);
      } else {
         return this;
      }
   }

   public static Variant variant() {
      return new Variant();
   }

   public static Variant merge(Variant variant, Variant variant1) {
      Variant variant2 = new Variant();
      variant2.values.putAll(variant.values);
      variant2.values.putAll(variant1.values);
      return variant2;
   }

   public JsonElement get() {
      JsonObject jsonobject = new JsonObject();
      this.values.values().forEach((variantproperty_value) -> variantproperty_value.addToVariant(jsonobject));
      return jsonobject;
   }

   public static JsonElement convertList(List<Variant> list) {
      if (list.size() == 1) {
         return list.get(0).get();
      } else {
         JsonArray jsonarray = new JsonArray();
         list.forEach((variant) -> jsonarray.add(variant.get()));
         return jsonarray;
      }
   }
}
