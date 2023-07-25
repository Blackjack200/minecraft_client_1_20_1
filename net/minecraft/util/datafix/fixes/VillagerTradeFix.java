package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends NamedEntityFix {
   public VillagerTradeFix(Schema schema, boolean flag) {
      super(schema, flag, "Villager trade fix", References.ENTITY, "minecraft:villager");
   }

   protected Typed<?> fix(Typed<?> typed) {
      OpticFinder<?> opticfinder = typed.getType().findField("Offers");
      OpticFinder<?> opticfinder1 = opticfinder.type().findField("Recipes");
      Type<?> type = opticfinder1.type();
      if (!(type instanceof List.ListType<?> list_listtype)) {
         throw new IllegalStateException("Recipes are expected to be a list.");
      } else {
         Type<?> type1 = list_listtype.getElement();
         OpticFinder<?> opticfinder2 = DSL.typeFinder(type1);
         OpticFinder<?> opticfinder3 = type1.findField("buy");
         OpticFinder<?> opticfinder4 = type1.findField("buyB");
         OpticFinder<?> opticfinder5 = type1.findField("sell");
         OpticFinder<Pair<String, String>> opticfinder6 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
         Function<Typed<?>, Typed<?>> function = (typed4) -> this.updateItemStack(opticfinder6, typed4);
         return typed.updateTyped(opticfinder, (typed1) -> typed1.updateTyped(opticfinder1, (typed2) -> typed2.updateTyped(opticfinder2, (typed3) -> typed3.updateTyped(opticfinder3, function).updateTyped(opticfinder4, function).updateTyped(opticfinder5, function))));
      }
   }

   private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> opticfinder, Typed<?> typed) {
      return typed.update(opticfinder, (pair) -> pair.mapSecond((s) -> Objects.equals(s, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : s));
   }
}
