package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackUUIDFix extends AbstractUUIDFix {
   public ItemStackUUIDFix(Schema schema) {
      super(schema, References.ITEM_STACK);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         OpticFinder<?> opticfinder2 = typed.getType().findField("tag");
         return typed.updateTyped(opticfinder2, (typed2) -> typed2.update(DSL.remainderFinder(), (dynamic) -> {
               dynamic = this.updateAttributeModifiers(dynamic);
               if (typed.getOptional(opticfinder).map((pair) -> "minecraft:player_head".equals(pair.getSecond())).orElse(false)) {
                  dynamic = this.updateSkullOwner(dynamic);
               }

               return dynamic;
            }));
      });
   }

   private Dynamic<?> updateAttributeModifiers(Dynamic<?> dynamic) {
      return dynamic.update("AttributeModifiers", (dynamic2) -> dynamic.createList(dynamic2.asStream().map((dynamic3) -> replaceUUIDLeastMost(dynamic3, "UUID", "UUID").orElse(dynamic3))));
   }

   private Dynamic<?> updateSkullOwner(Dynamic<?> dynamic) {
      return dynamic.update("SkullOwner", (dynamic1) -> replaceUUIDString(dynamic1, "Id", "Id").orElse(dynamic1));
   }
}
