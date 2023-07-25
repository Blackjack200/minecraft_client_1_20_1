package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.function.Function;

public class AdvancementsRenameFix extends DataFix {
   private final String name;
   private final Function<String, String> renamer;

   public AdvancementsRenameFix(Schema schema, boolean flag, String s, Function<String, String> function) {
      super(schema, flag);
      this.name = s;
      this.renamer = function;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.updateMapValues((pair) -> {
               String s = pair.getFirst().asString("");
               return pair.mapFirst((dynamic3) -> dynamic.createString(this.renamer.apply(s)));
            })));
   }
}
