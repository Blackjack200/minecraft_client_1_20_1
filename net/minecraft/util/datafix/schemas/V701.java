package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V701 extends Schema {
   public V701(int i, Schema schema) {
      super(i, schema);
   }

   protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> V100.equipment(schema));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
      registerMob(schema, map, "WitherSkeleton");
      registerMob(schema, map, "Stray");
      return map;
   }
}
