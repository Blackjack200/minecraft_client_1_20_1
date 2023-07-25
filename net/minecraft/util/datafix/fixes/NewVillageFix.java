package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix extends DataFix {
   public NewVillageFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      CompoundList.CompoundListType<String, ?> compoundlist_compoundlisttype = DSL.compoundList(DSL.string(), this.getInputSchema().getType(References.STRUCTURE_FEATURE));
      OpticFinder<? extends List<? extends Pair<String, ?>>> opticfinder = compoundlist_compoundlisttype.finder();
      return this.cap(compoundlist_compoundlisttype);
   }

   private <SF> TypeRewriteRule cap(CompoundList.CompoundListType<String, SF> compoundlist_compoundlisttype) {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      Type<?> type1 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
      OpticFinder<?> opticfinder = type.findField("Level");
      OpticFinder<?> opticfinder1 = opticfinder.type().findField("Structures");
      OpticFinder<?> opticfinder2 = opticfinder1.type().findField("Starts");
      OpticFinder<List<Pair<String, SF>>> opticfinder3 = compoundlist_compoundlisttype.finder();
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("NewVillageFix", type, (typed1) -> typed1.updateTyped(opticfinder, (typed2) -> typed2.updateTyped(opticfinder1, (typed3) -> typed3.updateTyped(opticfinder2, (typed4) -> typed4.update(opticfinder3, (list) -> list.stream().filter((pair1) -> !Objects.equals(pair1.getFirst(), "Village")).map((pair) -> pair.mapFirst((s) -> s.equals("New_Village") ? "Village" : s)).collect(Collectors.toList()))).update(DSL.remainderFinder(), (dynamic2) -> dynamic2.update("References", (dynamic3) -> {
                     Optional<? extends Dynamic<?>> optional = dynamic3.get("New_Village").result();
                     return DataFixUtils.orElse(optional.map((dynamic5) -> dynamic3.remove("New_Village").set("Village", dynamic5)), dynamic3).remove("Village");
                  }))))), this.fixTypeEverywhereTyped("NewVillageStartFix", type1, (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("id", (dynamic1) -> Objects.equals(NamespacedSchema.ensureNamespaced(dynamic1.asString("")), "minecraft:new_village") ? dynamic1.createString("minecraft:village") : dynamic1))));
   }
}
