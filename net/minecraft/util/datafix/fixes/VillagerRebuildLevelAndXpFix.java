package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.Mth;

public class VillagerRebuildLevelAndXpFix extends DataFix {
   private static final int TRADES_PER_LEVEL = 2;
   private static final int[] LEVEL_XP_THRESHOLDS = new int[]{0, 10, 50, 100, 150};

   public static int getMinXpPerLevel(int i) {
      return LEVEL_XP_THRESHOLDS[Mth.clamp(i - 1, 0, LEVEL_XP_THRESHOLDS.length - 1)];
   }

   public VillagerRebuildLevelAndXpFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:villager");
      OpticFinder<?> opticfinder = DSL.namedChoice("minecraft:villager", type);
      OpticFinder<?> opticfinder1 = type.findField("Offers");
      Type<?> type1 = opticfinder1.type();
      OpticFinder<?> opticfinder2 = type1.findField("Recipes");
      List.ListType<?> list_listtype = (List.ListType)opticfinder2.type();
      OpticFinder<?> opticfinder3 = list_listtype.getElement().finder();
      return this.fixTypeEverywhereTyped("Villager level and xp rebuild", this.getInputSchema().getType(References.ENTITY), (typed) -> typed.updateTyped(opticfinder, type, (typed1) -> {
            Dynamic<?> dynamic = typed1.get(DSL.remainderFinder());
            int i = dynamic.get("VillagerData").get("level").asInt(0);
            Typed<?> typed2 = typed1;
            if (i == 0 || i == 1) {
               int j = typed1.getOptionalTyped(opticfinder1).flatMap((typed4) -> typed4.getOptionalTyped(opticfinder2)).map((typed3) -> typed3.getAllTyped(opticfinder3).size()).orElse(0);
               i = Mth.clamp(j / 2, 1, 5);
               if (i > 1) {
                  typed2 = addLevel(typed1, i);
               }
            }

            Optional<Number> optional = dynamic.get("Xp").asNumber().result();
            if (!optional.isPresent()) {
               typed2 = addXpFromLevel(typed2, i);
            }

            return typed2;
         }));
   }

   private static Typed<?> addLevel(Typed<?> typed, int i) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("VillagerData", (dynamic1) -> dynamic1.set("level", dynamic1.createInt(i))));
   }

   private static Typed<?> addXpFromLevel(Typed<?> typed, int i) {
      int j = getMinXpPerLevel(i);
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.set("Xp", dynamic.createInt(j)));
   }
}
