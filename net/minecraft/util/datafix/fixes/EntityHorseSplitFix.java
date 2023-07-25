package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityHorseSplitFix extends EntityRenameFix {
   public EntityHorseSplitFix(Schema schema, boolean flag) {
      super("EntityHorseSplitFix", schema, flag);
   }

   protected Pair<String, Typed<?>> fix(String s, Typed<?> typed) {
      Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", s)) {
         int i = dynamic.get("Type").asInt(0);
         String s1;
         switch (i) {
            case 0:
            default:
               s1 = "Horse";
               break;
            case 1:
               s1 = "Donkey";
               break;
            case 2:
               s1 = "Mule";
               break;
            case 3:
               s1 = "ZombieHorse";
               break;
            case 4:
               s1 = "SkeletonHorse";
         }

         dynamic.remove("Type");
         Type<?> type = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(s1);
         return Pair.of(s1, (Typed)((Pair)typed.write().flatMap(type::readTyped).result().orElseThrow(() -> new IllegalStateException("Could not parse the new horse"))).getFirst());
      } else {
         return Pair.of(s, typed);
      }
   }
}
