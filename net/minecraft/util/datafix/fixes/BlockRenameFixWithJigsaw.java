package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.function.Function;

public abstract class BlockRenameFixWithJigsaw extends BlockRenameFix {
   private final String name;

   public BlockRenameFixWithJigsaw(Schema schema, String s) {
      super(schema, s);
      this.name = s;
   }

   public TypeRewriteRule makeRule() {
      DSL.TypeReference dsl_typereference = References.BLOCK_ENTITY;
      String s = "minecraft:jigsaw";
      OpticFinder<?> opticfinder = DSL.namedChoice("minecraft:jigsaw", this.getInputSchema().getChoiceType(dsl_typereference, "minecraft:jigsaw"));
      TypeRewriteRule typerewriterule = this.fixTypeEverywhereTyped(this.name + " for jigsaw state", this.getInputSchema().getType(dsl_typereference), this.getOutputSchema().getType(dsl_typereference), (typed) -> typed.updateTyped(opticfinder, this.getOutputSchema().getChoiceType(dsl_typereference, "minecraft:jigsaw"), (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("final_state", (dynamic2) -> DataFixUtils.orElse(dynamic2.asString().result().map((s1) -> {
                     int i = s1.indexOf(91);
                     int j = s1.indexOf(123);
                     int k = s1.length();
                     if (i > 0) {
                        k = Math.min(k, i);
                     }

                     if (j > 0) {
                        k = Math.min(k, j);
                     }

                     String s2 = s1.substring(0, k);
                     String s3 = this.fixBlock(s2);
                     return s3 + s1.substring(k);
                  }).map(dynamic::createString), dynamic2)))));
      return TypeRewriteRule.seq(super.makeRule(), typerewriterule);
   }

   public static DataFix create(Schema schema, String s, final Function<String, String> function) {
      return new BlockRenameFixWithJigsaw(schema, s) {
         protected String fixBlock(String s) {
            return function.apply(s);
         }
      };
   }
}
