package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class ItemCustomNameToComponentFix extends DataFix {
   public ItemCustomNameToComponentFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   private Dynamic<?> fixTag(Dynamic<?> dynamic) {
      Optional<? extends Dynamic<?>> optional = dynamic.get("display").result();
      if (optional.isPresent()) {
         Dynamic<?> dynamic1 = optional.get();
         Optional<String> optional1 = dynamic1.get("Name").asString().result();
         if (optional1.isPresent()) {
            dynamic1 = dynamic1.set("Name", dynamic1.createString(Component.Serializer.toJson(Component.literal(optional1.get()))));
         } else {
            Optional<String> optional2 = dynamic1.get("LocName").asString().result();
            if (optional2.isPresent()) {
               dynamic1 = dynamic1.set("Name", dynamic1.createString(Component.Serializer.toJson(Component.translatable(optional2.get()))));
               dynamic1 = dynamic1.remove("LocName");
            }
         }

         return dynamic.set("display", dynamic1);
      } else {
         return dynamic;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> opticfinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemCustomNameToComponentFix", type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), this::fixTag)));
   }
}
