package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class OminousBannerRenameFix extends ItemStackTagFix {
   public OminousBannerRenameFix(Schema schema) {
      super(schema, "OminousBannerRenameFix", (s) -> s.equals("minecraft:white_banner"));
   }

   protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
      Optional<? extends Dynamic<?>> optional = dynamic.get("display").result();
      if (optional.isPresent()) {
         Dynamic<?> dynamic1 = optional.get();
         Optional<String> optional1 = dynamic1.get("Name").asString().result();
         if (optional1.isPresent()) {
            String s = optional1.get();
            s = s.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
            dynamic1 = dynamic1.set("Name", dynamic1.createString(s));
         }

         return dynamic.set("display", dynamic1);
      } else {
         return dynamic;
      }
   }
}
