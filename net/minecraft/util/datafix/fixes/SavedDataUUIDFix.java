package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class SavedDataUUIDFix extends AbstractUUIDFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public SavedDataUUIDFix(Schema schema) {
      super(schema, References.SAVED_DATA);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("SavedDataUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> typed.updateTyped(typed.getType().findField("data"), (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("Raids", (dynamic1) -> dynamic1.createList(dynamic1.asStream().map((dynamic2) -> dynamic2.update("HeroesOfTheVillage", (dynamic3) -> dynamic3.createList(dynamic3.asStream().map((dynamic4) -> createUUIDFromLongs(dynamic4, "UUIDMost", "UUIDLeast").orElseGet(() -> {
                              LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
                              return dynamic4;
                           }))))))))));
   }
}
