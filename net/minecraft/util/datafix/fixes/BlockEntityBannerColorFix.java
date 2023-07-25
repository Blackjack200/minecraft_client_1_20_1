package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityBannerColorFix extends NamedEntityFix {
   public BlockEntityBannerColorFix(Schema schema, boolean flag) {
      super(schema, flag, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
   }

   public Dynamic<?> fixTag(Dynamic<?> dynamic) {
      dynamic = dynamic.update("Base", (dynamic4) -> dynamic4.createInt(15 - dynamic4.asInt(0)));
      return dynamic.update("Patterns", (dynamic1) -> DataFixUtils.orElse(dynamic1.asStreamOpt().map((stream) -> stream.map((dynamic2) -> dynamic2.update("Color", (dynamic3) -> dynamic3.createInt(15 - dynamic3.asInt(0))))).map(dynamic1::createList).result(), dynamic1));
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), this::fixTag);
   }
}
