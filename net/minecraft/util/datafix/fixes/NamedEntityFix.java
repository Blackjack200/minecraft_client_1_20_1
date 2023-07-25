package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public abstract class NamedEntityFix extends DataFix {
   private final String name;
   private final String entityName;
   private final DSL.TypeReference type;

   public NamedEntityFix(Schema schema, boolean flag, String s, DSL.TypeReference dsl_typereference, String s1) {
      super(schema, flag);
      this.name = s;
      this.type = dsl_typereference;
      this.entityName = s1;
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, this.getInputSchema().getChoiceType(this.type, this.entityName));
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type), (typed) -> typed.updateTyped(opticfinder, this.getOutputSchema().getChoiceType(this.type, this.entityName), this::fix));
   }

   protected abstract Typed<?> fix(Typed<?> typed);
}
