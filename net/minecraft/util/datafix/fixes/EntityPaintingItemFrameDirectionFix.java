package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class EntityPaintingItemFrameDirectionFix extends DataFix {
   private static final int[][] DIRECTIONS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

   public EntityPaintingItemFrameDirectionFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   private Dynamic<?> doFix(Dynamic<?> dynamic, boolean flag, boolean flag1) {
      if ((flag || flag1) && !dynamic.get("Facing").asNumber().result().isPresent()) {
         int i;
         if (dynamic.get("Direction").asNumber().result().isPresent()) {
            i = dynamic.get("Direction").asByte((byte)0) % DIRECTIONS.length;
            int[] aint = DIRECTIONS[i];
            dynamic = dynamic.set("TileX", dynamic.createInt(dynamic.get("TileX").asInt(0) + aint[0]));
            dynamic = dynamic.set("TileY", dynamic.createInt(dynamic.get("TileY").asInt(0) + aint[1]));
            dynamic = dynamic.set("TileZ", dynamic.createInt(dynamic.get("TileZ").asInt(0) + aint[2]));
            dynamic = dynamic.remove("Direction");
            if (flag1 && dynamic.get("ItemRotation").asNumber().result().isPresent()) {
               dynamic = dynamic.set("ItemRotation", dynamic.createByte((byte)(dynamic.get("ItemRotation").asByte((byte)0) * 2)));
            }
         } else {
            i = dynamic.get("Dir").asByte((byte)0) % DIRECTIONS.length;
            dynamic = dynamic.remove("Dir");
         }

         dynamic = dynamic.set("Facing", dynamic.createByte((byte)i));
      }

      return dynamic;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, "Painting");
      OpticFinder<?> opticfinder = DSL.namedChoice("Painting", type);
      Type<?> type1 = this.getInputSchema().getChoiceType(References.ENTITY, "ItemFrame");
      OpticFinder<?> opticfinder1 = DSL.namedChoice("ItemFrame", type1);
      Type<?> type2 = this.getInputSchema().getType(References.ENTITY);
      TypeRewriteRule typerewriterule = this.fixTypeEverywhereTyped("EntityPaintingFix", type2, (typed2) -> typed2.updateTyped(opticfinder, type, (typed3) -> typed3.update(DSL.remainderFinder(), (dynamic1) -> this.doFix(dynamic1, true, false))));
      TypeRewriteRule typerewriterule1 = this.fixTypeEverywhereTyped("EntityItemFrameFix", type2, (typed) -> typed.updateTyped(opticfinder1, type1, (typed1) -> typed1.update(DSL.remainderFinder(), (dynamic) -> this.doFix(dynamic, false, true))));
      return TypeRewriteRule.seq(typerewriterule, typerewriterule1);
   }
}
