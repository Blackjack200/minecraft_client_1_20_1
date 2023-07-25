package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public class ItemLoreFix extends DataFix {
   public ItemLoreFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> opticfinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("Item Lore componentize", type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("display", (dynamic1) -> dynamic1.update("Lore", (dynamic2) -> DataFixUtils.orElse(dynamic2.asStreamOpt().map(ItemLoreFix::fixLoreList).map(dynamic2::createList).result(), dynamic2))))));
   }

   private static <T> Stream<Dynamic<T>> fixLoreList(Stream<Dynamic<T>> stream) {
      return stream.map((dynamic) -> DataFixUtils.orElse(dynamic.asString().map(ItemLoreFix::fixLoreEntry).map(dynamic::createString).result(), dynamic));
   }

   private static String fixLoreEntry(String s) {
      return Component.Serializer.toJson(Component.literal(s));
   }
}
