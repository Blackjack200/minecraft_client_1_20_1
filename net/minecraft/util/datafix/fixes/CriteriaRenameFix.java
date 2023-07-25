package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class CriteriaRenameFix extends DataFix {
   private final String name;
   private final String advancementId;
   private final UnaryOperator<String> conversions;

   public CriteriaRenameFix(Schema schema, String s, String s1, UnaryOperator<String> unaryoperator) {
      super(schema, false);
      this.name = s;
      this.advancementId = s1;
      this.conversions = unaryoperator;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), (typed) -> typed.update(DSL.remainderFinder(), this::fixAdvancements));
   }

   private Dynamic<?> fixAdvancements(Dynamic<?> dynamic) {
      return dynamic.update(this.advancementId, (dynamic1) -> dynamic1.update("criteria", (dynamic2) -> dynamic2.updateMapValues((pair) -> pair.mapFirst((dynamic3) -> DataFixUtils.orElse(dynamic3.asString().map((s) -> dynamic3.createString(this.conversions.apply(s))).result(), dynamic3)))));
   }
}
