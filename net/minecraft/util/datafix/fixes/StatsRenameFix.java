package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix extends DataFix {
   private final String name;
   private final Map<String, String> renames;

   public StatsRenameFix(Schema schema, String s, Map<String, String> map) {
      super(schema, false);
      this.name = s;
      this.renames = map;
   }

   protected TypeRewriteRule makeRule() {
      return TypeRewriteRule.seq(this.createStatRule(), this.createCriteriaRule());
   }

   private TypeRewriteRule createCriteriaRule() {
      Type<?> type = this.getOutputSchema().getType(References.OBJECTIVE);
      Type<?> type1 = this.getInputSchema().getType(References.OBJECTIVE);
      OpticFinder<?> opticfinder = type1.findField("CriteriaType");
      TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype = opticfinder.type().findChoiceType("type", -1).orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
      Type<?> type2 = taggedchoice_taggedchoicetype.types().get("minecraft:custom");
      if (type2 == null) {
         throw new IllegalStateException("Failed to find custom criterion type variant");
      } else {
         OpticFinder<?> opticfinder1 = DSL.namedChoice("minecraft:custom", type2);
         OpticFinder<String> opticfinder2 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
         return this.fixTypeEverywhereTyped(this.name, type1, type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.updateTyped(opticfinder1, (typed2) -> typed2.update(opticfinder2, (s) -> this.renames.getOrDefault(s, s)))));
      }
   }

   private TypeRewriteRule createStatRule() {
      Type<?> type = this.getOutputSchema().getType(References.STATS);
      Type<?> type1 = this.getInputSchema().getType(References.STATS);
      OpticFinder<?> opticfinder = type1.findField("stats");
      OpticFinder<?> opticfinder1 = opticfinder.type().findField("minecraft:custom");
      OpticFinder<String> opticfinder2 = NamespacedSchema.namespacedString().finder();
      return this.fixTypeEverywhereTyped(this.name, type1, type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.updateTyped(opticfinder1, (typed2) -> typed2.update(opticfinder2, (s) -> this.renames.getOrDefault(s, s)))));
   }
}
