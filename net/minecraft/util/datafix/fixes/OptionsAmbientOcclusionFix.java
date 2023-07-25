package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAmbientOcclusionFix extends DataFix {
   public OptionsAmbientOcclusionFix(Schema schema) {
      super(schema, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsAmbientOcclusionFix", this.getInputSchema().getType(References.OPTIONS), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> DataFixUtils.orElse(dynamic.get("ao").asString().map((s) -> dynamic.set("ao", dynamic.createString(updateValue(s)))).result(), dynamic)));
   }

   private static String updateValue(String s) {
      String var10000;
      switch (s) {
         case "0":
            var10000 = "false";
            break;
         case "1":
         case "2":
            var10000 = "true";
            break;
         default:
            var10000 = s;
      }

      return var10000;
   }
}
