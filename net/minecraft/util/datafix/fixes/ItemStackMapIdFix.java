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
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackMapIdFix extends DataFix {
   public ItemStackMapIdFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> opticfinder1 = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemInstanceMapIdFix", type, (typed) -> {
         Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);
         if (optional.isPresent() && Objects.equals(optional.get().getSecond(), "minecraft:filled_map")) {
            Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
            Typed<?> typed1 = typed.getOrCreateTyped(opticfinder1);
            Dynamic<?> dynamic1 = typed1.get(DSL.remainderFinder());
            dynamic1 = dynamic1.set("map", dynamic1.createInt(dynamic.get("Damage").asInt(0)));
            return typed.set(opticfinder1, typed1.set(DSL.remainderFinder(), dynamic1));
         } else {
            return typed;
         }
      });
   }
}
