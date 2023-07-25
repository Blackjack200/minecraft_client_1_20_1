package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class PlayerUUIDFix extends AbstractUUIDFix {
   public PlayerUUIDFix(Schema schema) {
      super(schema, References.PLAYER);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("PlayerUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         OpticFinder<?> opticfinder = typed.getType().findField("RootVehicle");
         return typed.updateTyped(opticfinder, opticfinder.type(), (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic1) -> replaceUUIDLeastMost(dynamic1, "Attach", "Attach").orElse(dynamic1))).update(DSL.remainderFinder(), (dynamic) -> EntityUUIDFix.updateEntityUUID(EntityUUIDFix.updateLivingEntity(dynamic)));
      });
   }
}
