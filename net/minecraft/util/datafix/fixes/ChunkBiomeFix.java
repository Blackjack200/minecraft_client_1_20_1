package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ChunkBiomeFix extends DataFix {
   public ChunkBiomeFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> opticfinder = type.findField("Level");
      return this.fixTypeEverywhereTyped("Leaves fix", type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> {
               Optional<IntStream> optional = dynamic.get("Biomes").asIntStreamOpt().result();
               if (optional.isEmpty()) {
                  return dynamic;
               } else {
                  int[] aint = optional.get().toArray();
                  if (aint.length != 256) {
                     return dynamic;
                  } else {
                     int[] aint1 = new int[1024];

                     for(int i = 0; i < 4; ++i) {
                        for(int j = 0; j < 4; ++j) {
                           int k = (j << 2) + 2;
                           int l = (i << 2) + 2;
                           int i1 = l << 4 | k;
                           aint1[i << 2 | j] = aint[i1];
                        }
                     }

                     for(int j1 = 1; j1 < 64; ++j1) {
                        System.arraycopy(aint1, 0, aint1, j1 * 16, 16);
                     }

                     return dynamic.set("Biomes", dynamic.createIntList(Arrays.stream(aint1)));
                  }
               }
            })));
   }
}
