package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class VariantRenameFix extends NamedEntityFix {
   private final Map<String, String> renames;

   public VariantRenameFix(Schema schema, String s, DSL.TypeReference dsl_typereference, String s1, Map<String, String> map) {
      super(schema, false, s, dsl_typereference, s1);
      this.renames = map;
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("variant", (dynamic1) -> DataFixUtils.orElse(dynamic1.asString().map((s) -> dynamic1.createString(this.renames.getOrDefault(s, s))).result(), dynamic1)));
   }
}
