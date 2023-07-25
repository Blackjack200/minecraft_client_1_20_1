package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemWaterPotionFix extends DataFix {
   public ItemWaterPotionFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> opticfinder1 = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemWaterPotionFix", type, (typed) -> {
         Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);
         if (optional.isPresent()) {
            String s = optional.get().getSecond();
            if ("minecraft:potion".equals(s) || "minecraft:splash_potion".equals(s) || "minecraft:lingering_potion".equals(s) || "minecraft:tipped_arrow".equals(s)) {
               Typed<?> typed1 = typed.getOrCreateTyped(opticfinder1);
               Dynamic<?> dynamic = typed1.get(DSL.remainderFinder());
               if (!dynamic.get("Potion").asString().result().isPresent()) {
                  dynamic = dynamic.set("Potion", dynamic.createString("minecraft:water"));
               }

               return typed.set(opticfinder1, typed1.set(DSL.remainderFinder(), dynamic));
            }
         }

         return typed;
      });
   }
}
