package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RemoveGolemGossipFix extends NamedEntityFix {
   public RemoveGolemGossipFix(Schema schema, boolean flag) {
      super(schema, flag, "Remove Golem Gossip Fix", References.ENTITY, "minecraft:villager");
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), RemoveGolemGossipFix::fixValue);
   }

   private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
      return dynamic.update("Gossips", (dynamic2) -> dynamic.createList(dynamic2.asStream().filter((dynamic3) -> !dynamic3.get("Type").asString("").equals("golem"))));
   }
}
