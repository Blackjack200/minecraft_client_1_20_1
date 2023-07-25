package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;

public class EntityTippedArrowFix extends SimplestEntityRenameFix {
   public EntityTippedArrowFix(Schema schema, boolean flag) {
      super("EntityTippedArrowFix", schema, flag);
   }

   protected String rename(String s) {
      return Objects.equals(s, "TippedArrow") ? "Arrow" : s;
   }
}
