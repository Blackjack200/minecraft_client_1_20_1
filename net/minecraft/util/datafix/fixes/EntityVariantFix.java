package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;

public class EntityVariantFix extends NamedEntityFix {
   private final String fieldName;
   private final IntFunction<String> idConversions;

   public EntityVariantFix(Schema schema, String s, DSL.TypeReference dsl_typereference, String s1, String s2, IntFunction<String> intfunction) {
      super(schema, false, s, dsl_typereference, s1);
      this.fieldName = s2;
      this.idConversions = intfunction;
   }

   private static <T> Dynamic<T> updateAndRename(Dynamic<T> dynamic, String s, String s1, Function<Dynamic<T>, Dynamic<T>> function) {
      return dynamic.map((object) -> {
         DynamicOps<T> dynamicops = dynamic.getOps();
         Function<T, T> function2 = (object3) -> function.apply(new Dynamic<>(dynamicops, object3)).getValue();
         return dynamicops.get(object, s).map((object2) -> dynamicops.set((T)object, s1, function2.apply(object2))).result().orElse(object);
      });
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> updateAndRename(dynamic, this.fieldName, "variant", (dynamic1) -> DataFixUtils.orElse(dynamic1.asNumber().map((number) -> dynamic1.createString(this.idConversions.apply(number.intValue()))).result(), dynamic1)));
   }
}
