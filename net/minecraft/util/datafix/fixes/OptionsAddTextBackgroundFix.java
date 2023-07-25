package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAddTextBackgroundFix extends DataFix {
   public OptionsAddTextBackgroundFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsAddTextBackgroundFix", this.getInputSchema().getType(References.OPTIONS), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> DataFixUtils.orElse(dynamic.get("chatOpacity").asString().map((s) -> dynamic.set("textBackgroundOpacity", dynamic.createDouble(this.calculateBackground(s)))).result(), dynamic)));
   }

   private double calculateBackground(String s) {
      try {
         double d0 = 0.9D * Double.parseDouble(s) + 0.1D;
         return d0 / 2.0D;
      } catch (NumberFormatException var4) {
         return 0.5D;
      }
   }
}
