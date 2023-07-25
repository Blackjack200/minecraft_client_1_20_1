package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;

public class V1451_6 extends NamespacedSchema {
   public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
   protected static final Hook.HookFunction UNPACK_OBJECTIVE_ID = new Hook.HookFunction() {
      public <T> T apply(DynamicOps<T> dynamicops, T object) {
         Dynamic<T> dynamic = new Dynamic<>(dynamicops, object);
         return DataFixUtils.orElse(dynamic.get("CriteriaName").asString().get().left().map((s) -> {
            int i = s.indexOf(58);
            if (i < 0) {
               return Pair.of("_special", s);
            } else {
               try {
                  ResourceLocation resourcelocation = ResourceLocation.of(s.substring(0, i), '.');
                  ResourceLocation resourcelocation1 = ResourceLocation.of(s.substring(i + 1), '.');
                  return Pair.of(resourcelocation.toString(), resourcelocation1.toString());
               } catch (Exception var4) {
                  return Pair.of("_special", s);
               }
            }
         }).map((pair) -> dynamic.set("CriteriaType", dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString(pair.getFirst()), dynamic.createString("id"), dynamic.createString(pair.getSecond()))))), dynamic).getValue();
      }
   };
   protected static final Hook.HookFunction REPACK_OBJECTIVE_ID = new Hook.HookFunction() {
      private String packWithDot(String s) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         return resourcelocation != null ? resourcelocation.getNamespace() + "." + resourcelocation.getPath() : s;
      }

      public <T> T apply(DynamicOps<T> dynamicops, T object) {
         Dynamic<T> dynamic = new Dynamic<>(dynamicops, object);
         Optional<Dynamic<T>> optional = dynamic.get("CriteriaType").get().get().left().flatMap((dynamic4) -> {
            Optional<String> optional1 = dynamic4.get("type").asString().get().left();
            Optional<String> optional2 = dynamic4.get("id").asString().get().left();
            if (optional1.isPresent() && optional2.isPresent()) {
               String s = optional1.get();
               return s.equals("_special") ? Optional.of(dynamic.createString(optional2.get())) : Optional.of(dynamic4.createString(this.packWithDot(s) + ":" + this.packWithDot(optional2.get())));
            } else {
               return Optional.empty();
            }
         });
         return DataFixUtils.orElse(optional.map((dynamic2) -> dynamic.set("CriteriaName", dynamic2).remove("CriteriaType")), dynamic).getValue();
      }
   };

   public V1451_6(int i, Schema schema) {
      super(i, schema);
   }

   public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
      super.registerTypes(schema, map, map1);
      Supplier<TypeTemplate> supplier = () -> DSL.compoundList(References.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
      schema.registerType(false, References.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", supplier.get(), "minecraft:used", supplier.get(), "minecraft:broken", supplier.get(), "minecraft:picked_up", supplier.get(), DSL.optionalFields("minecraft:dropped", supplier.get(), "minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType()))))));
      Map<String, Supplier<TypeTemplate>> map2 = createCriterionTypes(schema);
      schema.registerType(false, References.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map2)), UNPACK_OBJECTIVE_ID, REPACK_OBJECTIVE_ID));
   }

   protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema schema) {
      Supplier<TypeTemplate> supplier = () -> DSL.optionalFields("id", References.ITEM_NAME.in(schema));
      Supplier<TypeTemplate> supplier1 = () -> DSL.optionalFields("id", References.BLOCK_NAME.in(schema));
      Supplier<TypeTemplate> supplier2 = () -> DSL.optionalFields("id", References.ENTITY_NAME.in(schema));
      Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
      map.put("minecraft:mined", supplier1);
      map.put("minecraft:crafted", supplier);
      map.put("minecraft:used", supplier);
      map.put("minecraft:broken", supplier);
      map.put("minecraft:picked_up", supplier);
      map.put("minecraft:dropped", supplier);
      map.put("minecraft:killed", supplier2);
      map.put("minecraft:killed_by", supplier2);
      map.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(namespacedString())));
      map.put("_special", () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
      return map;
   }
}
