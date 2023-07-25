package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class LevelUUIDFix extends AbstractUUIDFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public LevelUUIDFix(Schema schema) {
      super(schema, References.LEVEL);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("LevelUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> typed.updateTyped(DSL.remainderFinder(), (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> {
               dynamic = this.updateCustomBossEvents(dynamic);
               dynamic = this.updateDragonFight(dynamic);
               return this.updateWanderingTrader(dynamic);
            })));
   }

   private Dynamic<?> updateWanderingTrader(Dynamic<?> dynamic) {
      return replaceUUIDString(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
   }

   private Dynamic<?> updateDragonFight(Dynamic<?> dynamic) {
      return dynamic.update("DimensionData", (dynamic1) -> dynamic1.updateMapValues((pair) -> pair.mapSecond((dynamic2) -> dynamic2.update("DragonFight", (dynamic3) -> replaceUUIDLeastMost(dynamic3, "DragonUUID", "Dragon").orElse(dynamic3)))));
   }

   private Dynamic<?> updateCustomBossEvents(Dynamic<?> dynamic) {
      return dynamic.update("CustomBossEvents", (dynamic1) -> dynamic1.updateMapValues((pair) -> pair.mapSecond((dynamic2) -> dynamic2.update("Players", (dynamic4) -> dynamic2.createList(dynamic4.asStream().map((dynamic5) -> createUUIDFromML(dynamic5).orElseGet(() -> {
                        LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
                        return dynamic5;
                     })))))));
   }
}
