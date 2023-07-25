package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WorldGenSettingsHeightAndBiomeFix extends DataFix {
   private static final String NAME = "WorldGenSettingsHeightAndBiomeFix";
   public static final String WAS_PREVIOUSLY_INCREASED_KEY = "has_increased_height_already";

   public WorldGenSettingsHeightAndBiomeFix(Schema schema) {
      super(schema, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
      OpticFinder<?> opticfinder = type.findField("dimensions");
      Type<?> type1 = this.getOutputSchema().getType(References.WORLD_GEN_SETTINGS);
      Type<?> type2 = type1.findFieldType("dimensions");
      return this.fixTypeEverywhereTyped("WorldGenSettingsHeightAndBiomeFix", type, type1, (typed) -> {
         OptionalDynamic<?> optionaldynamic = typed.get(DSL.remainderFinder()).get("has_increased_height_already");
         boolean flag = optionaldynamic.result().isEmpty();
         boolean flag1 = optionaldynamic.asBoolean(true);
         return typed.update(DSL.remainderFinder(), (dynamic6) -> dynamic6.remove("has_increased_height_already")).updateTyped(opticfinder, type2, (typed1) -> {
            Dynamic<?> dynamic = typed1.write().result().orElseThrow(() -> new IllegalStateException("Malformed WorldGenSettings.dimensions"));
            dynamic = dynamic.update("minecraft:overworld", (dynamic1) -> dynamic1.update("generator", (dynamic2) -> {
                  String s = dynamic2.get("type").asString("");
                  if ("minecraft:noise".equals(s)) {
                     MutableBoolean mutableboolean = new MutableBoolean();
                     dynamic2 = dynamic2.update("biome_source", (dynamic5) -> {
                        String s1 = dynamic5.get("type").asString("");
                        if ("minecraft:vanilla_layered".equals(s1) || flag && "minecraft:multi_noise".equals(s1)) {
                           if (dynamic5.get("large_biomes").asBoolean(false)) {
                              mutableboolean.setTrue();
                           }

                           return dynamic5.createMap(ImmutableMap.of(dynamic5.createString("preset"), dynamic5.createString("minecraft:overworld"), dynamic5.createString("type"), dynamic5.createString("minecraft:multi_noise")));
                        } else {
                           return dynamic5;
                        }
                     });
                     return mutableboolean.booleanValue() ? dynamic2.update("settings", (dynamic4) -> "minecraft:overworld".equals(dynamic4.asString("")) ? dynamic4.createString("minecraft:large_biomes") : dynamic4) : dynamic2;
                  } else if ("minecraft:flat".equals(s)) {
                     return flag1 ? dynamic2 : dynamic2.update("settings", (dynamic3) -> dynamic3.update("layers", WorldGenSettingsHeightAndBiomeFix::updateLayers));
                  } else {
                     return dynamic2;
                  }
               }));
            return type2.readTyped(dynamic).result().orElseThrow(() -> new IllegalStateException("WorldGenSettingsHeightAndBiomeFix failed.")).getFirst();
         });
      });
   }

   private static Dynamic<?> updateLayers(Dynamic<?> dynamic) {
      Dynamic<?> dynamic1 = dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(64), dynamic.createString("block"), dynamic.createString("minecraft:air")));
      return dynamic.createList(Stream.concat(Stream.of(dynamic1), dynamic.asStream()));
   }
}
