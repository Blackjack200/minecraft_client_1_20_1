package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerFollowRangeFix extends NamedEntityFix {
   private static final double ORIGINAL_VALUE = 16.0D;
   private static final double NEW_BASE_VALUE = 48.0D;

   public VillagerFollowRangeFix(Schema schema) {
      super(schema, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
   }

   private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
      return dynamic.update("Attributes", (dynamic2) -> dynamic.createList(dynamic2.asStream().map((dynamic3) -> dynamic3.get("Name").asString("").equals("generic.follow_range") && dynamic3.get("Base").asDouble(0.0D) == 16.0D ? dynamic3.set("Base", dynamic3.createDouble(48.0D)) : dynamic3)));
   }
}
