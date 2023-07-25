package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
   public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>() {
      public <T> DataResult<String> read(DynamicOps<T> dynamicops, T object) {
         return dynamicops.getStringValue(object).map(NamespacedSchema::ensureNamespaced);
      }

      public <T> T write(DynamicOps<T> dynamicops, String s) {
         return dynamicops.createString(s);
      }

      public String toString() {
         return "NamespacedString";
      }
   };
   private static final Type<String> NAMESPACED_STRING = new Const.PrimitiveType<>(NAMESPACED_STRING_CODEC);

   public NamespacedSchema(int i, Schema schema) {
      super(i, schema);
   }

   public static String ensureNamespaced(String s) {
      ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
      return resourcelocation != null ? resourcelocation.toString() : s;
   }

   public static Type<String> namespacedString() {
      return NAMESPACED_STRING;
   }

   public Type<?> getChoiceType(DSL.TypeReference dsl_typereference, String s) {
      return super.getChoiceType(dsl_typereference, ensureNamespaced(s));
   }
}
