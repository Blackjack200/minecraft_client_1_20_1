package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix extends DataFix {
   private final String name;

   public BlockRenameFix(Schema schema, String s) {
      super(schema, false);
      this.name = s;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
      Type<Pair<String, String>> type1 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(type, type1)) {
         throw new IllegalStateException("block type is not what was expected.");
      } else {
         TypeRewriteRule typerewriterule = this.fixTypeEverywhere(this.name + " for block", type1, (dynamicops) -> (pair) -> pair.mapSecond(this::fixBlock));
         TypeRewriteRule typerewriterule1 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> {
               Optional<String> optional = dynamic.get("Name").asString().result();
               return optional.isPresent() ? dynamic.set("Name", dynamic.createString(this.fixBlock(optional.get()))) : dynamic;
            }));
         return TypeRewriteRule.seq(typerewriterule, typerewriterule1);
      }
   }

   protected abstract String fixBlock(String s);

   public static DataFix create(Schema schema, String s, final Function<String, String> function) {
      return new BlockRenameFix(schema, s) {
         protected String fixBlock(String s) {
            return function.apply(s);
         }
      };
   }
}
