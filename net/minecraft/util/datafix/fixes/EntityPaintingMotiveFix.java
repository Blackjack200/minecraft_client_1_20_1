package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class EntityPaintingMotiveFix extends NamedEntityFix {
   private static final Map<String, String> MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("donkeykong", "donkey_kong");
      hashmap.put("burningskull", "burning_skull");
      hashmap.put("skullandroses", "skull_and_roses");
   });

   public EntityPaintingMotiveFix(Schema schema, boolean flag) {
      super(schema, flag, "EntityPaintingMotiveFix", References.ENTITY, "minecraft:painting");
   }

   public Dynamic<?> fixTag(Dynamic<?> dynamic) {
      Optional<String> optional = dynamic.get("Motive").asString().result();
      if (optional.isPresent()) {
         String s = optional.get().toLowerCase(Locale.ROOT);
         return dynamic.set("Motive", dynamic.createString((new ResourceLocation(MAP.getOrDefault(s, s))).toString()));
      } else {
         return dynamic;
      }
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), this::fixTag);
   }
}
