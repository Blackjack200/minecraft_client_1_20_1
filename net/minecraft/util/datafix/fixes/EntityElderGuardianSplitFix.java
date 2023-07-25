package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityElderGuardianSplitFix extends SimpleEntityRenameFix {
   public EntityElderGuardianSplitFix(Schema schema, boolean flag) {
      super("EntityElderGuardianSplitFix", schema, flag);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String s, Dynamic<?> dynamic) {
      return Pair.of(Objects.equals(s, "Guardian") && dynamic.get("Elder").asBoolean(false) ? "ElderGuardian" : s, dynamic);
   }
}
