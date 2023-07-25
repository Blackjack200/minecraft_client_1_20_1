package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsRenameFieldFix extends DataFix {
   private final String fixName;
   private final String fieldFrom;
   private final String fieldTo;

   public OptionsRenameFieldFix(Schema schema, boolean flag, String s, String s1, String s2) {
      super(schema, flag);
      this.fixName = s;
      this.fieldFrom = s1;
      this.fieldTo = s2;
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.fixName, this.getInputSchema().getType(References.OPTIONS), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> DataFixUtils.orElse(dynamic.get(this.fieldFrom).result().map((dynamic2) -> dynamic.set(this.fieldTo, dynamic2).remove(this.fieldFrom)), dynamic)));
   }
}
