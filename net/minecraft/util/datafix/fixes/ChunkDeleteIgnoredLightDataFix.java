package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteIgnoredLightDataFix extends DataFix {
   public ChunkDeleteIgnoredLightDataFix(Schema schema) {
      super(schema, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> opticfinder = type.findField("sections");
      return this.fixTypeEverywhereTyped("ChunkDeleteIgnoredLightDataFix", type, (typed) -> {
         boolean flag = typed.get(DSL.remainderFinder()).get("isLightOn").asBoolean(false);
         return !flag ? typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> dynamic.remove("BlockLight").remove("SkyLight"))) : typed;
      });
   }
}
