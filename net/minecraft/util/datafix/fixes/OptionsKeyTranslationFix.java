package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.stream.Collectors;

public class OptionsKeyTranslationFix extends DataFix {
   public OptionsKeyTranslationFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(References.OPTIONS), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.getMapValues().map((map) -> dynamic.createMap(map.entrySet().stream().map((map_entry) -> {
                  if (map_entry.getKey().asString("").startsWith("key_")) {
                     String s = map_entry.getValue().asString("");
                     if (!s.startsWith("key.mouse") && !s.startsWith("scancode.")) {
                        return Pair.of(map_entry.getKey(), dynamic.createString("key.keyboard." + s.substring("key.".length())));
                     }
                  }

                  return Pair.of(map_entry.getKey(), map_entry.getValue());
               }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse(dynamic)));
   }
}
