package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class WeaponSmithChestLootTableFix extends NamedEntityFix {
   public WeaponSmithChestLootTableFix(Schema schema, boolean flag) {
      super(schema, flag, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> {
         String s = dynamic.get("LootTable").asString("");
         return s.equals("minecraft:chests/village_blacksmith") ? dynamic.set("LootTable", dynamic.createString("minecraft:chests/village/village_weaponsmith")) : dynamic;
      });
   }
}
