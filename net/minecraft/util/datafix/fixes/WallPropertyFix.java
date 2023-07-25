package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Set;

public class WallPropertyFix extends DataFix {
   private static final Set<String> WALL_BLOCKS = ImmutableSet.of("minecraft:andesite_wall", "minecraft:brick_wall", "minecraft:cobblestone_wall", "minecraft:diorite_wall", "minecraft:end_stone_brick_wall", "minecraft:granite_wall", "minecraft:mossy_cobblestone_wall", "minecraft:mossy_stone_brick_wall", "minecraft:nether_brick_wall", "minecraft:prismarine_wall", "minecraft:red_nether_brick_wall", "minecraft:red_sandstone_wall", "minecraft:sandstone_wall", "minecraft:stone_brick_wall");

   public WallPropertyFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("WallPropertyFix", this.getInputSchema().getType(References.BLOCK_STATE), (typed) -> typed.update(DSL.remainderFinder(), WallPropertyFix::upgradeBlockStateTag));
   }

   private static String mapProperty(String s) {
      return "true".equals(s) ? "low" : "none";
   }

   private static <T> Dynamic<T> fixWallProperty(Dynamic<T> dynamic, String s) {
      return dynamic.update(s, (dynamic1) -> DataFixUtils.orElse(dynamic1.asString().result().map(WallPropertyFix::mapProperty).map(dynamic1::createString), dynamic1));
   }

   private static <T> Dynamic<T> upgradeBlockStateTag(Dynamic<T> dynamic) {
      boolean flag = dynamic.get("Name").asString().result().filter(WALL_BLOCKS::contains).isPresent();
      return !flag ? dynamic : dynamic.update("Properties", (dynamic1) -> {
         Dynamic<?> dynamic2 = fixWallProperty(dynamic1, "east");
         dynamic2 = fixWallProperty(dynamic2, "west");
         dynamic2 = fixWallProperty(dynamic2, "north");
         return fixWallProperty(dynamic2, "south");
      });
   }
}
