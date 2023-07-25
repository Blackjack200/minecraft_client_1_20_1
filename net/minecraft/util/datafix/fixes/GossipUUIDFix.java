package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class GossipUUIDFix extends NamedEntityFix {
   public GossipUUIDFix(Schema schema, String s) {
      super(schema, false, "Gossip for for " + s, References.ENTITY, s);
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("Gossips", (dynamic1) -> DataFixUtils.orElse(dynamic1.asStreamOpt().result().map((stream) -> stream.map((dynamic2) -> AbstractUUIDFix.replaceUUIDLeastMost(dynamic2, "Target", "Target").orElse(dynamic2))).map(dynamic1::createList), dynamic1)));
   }
}
