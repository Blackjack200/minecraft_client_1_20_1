package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NamespacedTypeRenameFix extends DataFix {
   private final String name;
   private final DSL.TypeReference type;
   private final UnaryOperator<String> renamer;

   public NamespacedTypeRenameFix(Schema schema, String s, DSL.TypeReference dsl_typereference, UnaryOperator<String> unaryoperator) {
      super(schema, false);
      this.name = s;
      this.type = dsl_typereference;
      this.renamer = unaryoperator;
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, String>> type = DSL.named(this.type.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(type, this.getInputSchema().getType(this.type))) {
         throw new IllegalStateException("\"" + this.type.typeName() + "\" is not what was expected.");
      } else {
         return this.fixTypeEverywhere(this.name, type, (dynamicops) -> (pair) -> pair.mapSecond(this.renamer));
      }
   }
}
