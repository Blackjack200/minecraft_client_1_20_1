package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityShulkerColorFix extends NamedEntityFix {
   public EntityShulkerColorFix(Schema schema, boolean flag) {
      super(schema, flag, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
   }

   public Dynamic<?> fixTag(Dynamic<?> dynamic) {
      return !dynamic.get("Color").map(Dynamic::asNumber).result().isPresent() ? dynamic.set("Color", dynamic.createByte((byte)10)) : dynamic;
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), this::fixTag);
   }
}
