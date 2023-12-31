package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;

public class ItemStackEnchantmentNamesFix extends DataFix {
   private static final Int2ObjectMap<String> MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), (int2objectopenhashmap) -> {
      int2objectopenhashmap.put(0, "minecraft:protection");
      int2objectopenhashmap.put(1, "minecraft:fire_protection");
      int2objectopenhashmap.put(2, "minecraft:feather_falling");
      int2objectopenhashmap.put(3, "minecraft:blast_protection");
      int2objectopenhashmap.put(4, "minecraft:projectile_protection");
      int2objectopenhashmap.put(5, "minecraft:respiration");
      int2objectopenhashmap.put(6, "minecraft:aqua_affinity");
      int2objectopenhashmap.put(7, "minecraft:thorns");
      int2objectopenhashmap.put(8, "minecraft:depth_strider");
      int2objectopenhashmap.put(9, "minecraft:frost_walker");
      int2objectopenhashmap.put(10, "minecraft:binding_curse");
      int2objectopenhashmap.put(16, "minecraft:sharpness");
      int2objectopenhashmap.put(17, "minecraft:smite");
      int2objectopenhashmap.put(18, "minecraft:bane_of_arthropods");
      int2objectopenhashmap.put(19, "minecraft:knockback");
      int2objectopenhashmap.put(20, "minecraft:fire_aspect");
      int2objectopenhashmap.put(21, "minecraft:looting");
      int2objectopenhashmap.put(22, "minecraft:sweeping");
      int2objectopenhashmap.put(32, "minecraft:efficiency");
      int2objectopenhashmap.put(33, "minecraft:silk_touch");
      int2objectopenhashmap.put(34, "minecraft:unbreaking");
      int2objectopenhashmap.put(35, "minecraft:fortune");
      int2objectopenhashmap.put(48, "minecraft:power");
      int2objectopenhashmap.put(49, "minecraft:punch");
      int2objectopenhashmap.put(50, "minecraft:flame");
      int2objectopenhashmap.put(51, "minecraft:infinity");
      int2objectopenhashmap.put(61, "minecraft:luck_of_the_sea");
      int2objectopenhashmap.put(62, "minecraft:lure");
      int2objectopenhashmap.put(65, "minecraft:loyalty");
      int2objectopenhashmap.put(66, "minecraft:impaling");
      int2objectopenhashmap.put(67, "minecraft:riptide");
      int2objectopenhashmap.put(68, "minecraft:channeling");
      int2objectopenhashmap.put(70, "minecraft:mending");
      int2objectopenhashmap.put(71, "minecraft:vanishing_curse");
   });

   public ItemStackEnchantmentNamesFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> opticfinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemStackEnchantmentFix", type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), this::fixTag)));
   }

   private Dynamic<?> fixTag(Dynamic<?> dynamic) {
      Optional<? extends Dynamic<?>> optional = dynamic.get("ench").asStreamOpt().map((stream1) -> stream1.map((dynamic3) -> dynamic3.set("id", dynamic3.createString(MAP.getOrDefault(dynamic3.get("id").asInt(0), "null"))))).map(dynamic::createList).result();
      if (optional.isPresent()) {
         dynamic = dynamic.remove("ench").set("Enchantments", optional.get());
      }

      return dynamic.update("StoredEnchantments", (dynamic1) -> DataFixUtils.orElse(dynamic1.asStreamOpt().map((stream) -> stream.map((dynamic2) -> dynamic2.set("id", dynamic2.createString(MAP.getOrDefault(dynamic2.get("id").asInt(0), "null"))))).map(dynamic1::createList).result(), dynamic1));
   }
}
