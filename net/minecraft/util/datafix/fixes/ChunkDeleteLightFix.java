package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteLightFix extends DataFix {
   public ChunkDeleteLightFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> opticfinder = type.findField("sections");
      return this.fixTypeEverywhereTyped("ChunkDeleteLightFix for " + this.getOutputSchema().getVersionKey(), type, (typed) -> {
         typed = typed.update(DSL.remainderFinder(), (dynamic1) -> dynamic1.remove("isLightOn"));
         return typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> dynamic.remove("BlockLight").remove("SkyLight")));
      });
   }
}
