package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

public class LevelDataGeneratorOptionsFix extends DataFix {
   static final Map<String, String> MAP = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("0", "minecraft:ocean");
      hashmap.put("1", "minecraft:plains");
      hashmap.put("2", "minecraft:desert");
      hashmap.put("3", "minecraft:mountains");
      hashmap.put("4", "minecraft:forest");
      hashmap.put("5", "minecraft:taiga");
      hashmap.put("6", "minecraft:swamp");
      hashmap.put("7", "minecraft:river");
      hashmap.put("8", "minecraft:nether");
      hashmap.put("9", "minecraft:the_end");
      hashmap.put("10", "minecraft:frozen_ocean");
      hashmap.put("11", "minecraft:frozen_river");
      hashmap.put("12", "minecraft:snowy_tundra");
      hashmap.put("13", "minecraft:snowy_mountains");
      hashmap.put("14", "minecraft:mushroom_fields");
      hashmap.put("15", "minecraft:mushroom_field_shore");
      hashmap.put("16", "minecraft:beach");
      hashmap.put("17", "minecraft:desert_hills");
      hashmap.put("18", "minecraft:wooded_hills");
      hashmap.put("19", "minecraft:taiga_hills");
      hashmap.put("20", "minecraft:mountain_edge");
      hashmap.put("21", "minecraft:jungle");
      hashmap.put("22", "minecraft:jungle_hills");
      hashmap.put("23", "minecraft:jungle_edge");
      hashmap.put("24", "minecraft:deep_ocean");
      hashmap.put("25", "minecraft:stone_shore");
      hashmap.put("26", "minecraft:snowy_beach");
      hashmap.put("27", "minecraft:birch_forest");
      hashmap.put("28", "minecraft:birch_forest_hills");
      hashmap.put("29", "minecraft:dark_forest");
      hashmap.put("30", "minecraft:snowy_taiga");
      hashmap.put("31", "minecraft:snowy_taiga_hills");
      hashmap.put("32", "minecraft:giant_tree_taiga");
      hashmap.put("33", "minecraft:giant_tree_taiga_hills");
      hashmap.put("34", "minecraft:wooded_mountains");
      hashmap.put("35", "minecraft:savanna");
      hashmap.put("36", "minecraft:savanna_plateau");
      hashmap.put("37", "minecraft:badlands");
      hashmap.put("38", "minecraft:wooded_badlands_plateau");
      hashmap.put("39", "minecraft:badlands_plateau");
      hashmap.put("40", "minecraft:small_end_islands");
      hashmap.put("41", "minecraft:end_midlands");
      hashmap.put("42", "minecraft:end_highlands");
      hashmap.put("43", "minecraft:end_barrens");
      hashmap.put("44", "minecraft:warm_ocean");
      hashmap.put("45", "minecraft:lukewarm_ocean");
      hashmap.put("46", "minecraft:cold_ocean");
      hashmap.put("47", "minecraft:deep_warm_ocean");
      hashmap.put("48", "minecraft:deep_lukewarm_ocean");
      hashmap.put("49", "minecraft:deep_cold_ocean");
      hashmap.put("50", "minecraft:deep_frozen_ocean");
      hashmap.put("127", "minecraft:the_void");
      hashmap.put("129", "minecraft:sunflower_plains");
      hashmap.put("130", "minecraft:desert_lakes");
      hashmap.put("131", "minecraft:gravelly_mountains");
      hashmap.put("132", "minecraft:flower_forest");
      hashmap.put("133", "minecraft:taiga_mountains");
      hashmap.put("134", "minecraft:swamp_hills");
      hashmap.put("140", "minecraft:ice_spikes");
      hashmap.put("149", "minecraft:modified_jungle");
      hashmap.put("151", "minecraft:modified_jungle_edge");
      hashmap.put("155", "minecraft:tall_birch_forest");
      hashmap.put("156", "minecraft:tall_birch_hills");
      hashmap.put("157", "minecraft:dark_forest_hills");
      hashmap.put("158", "minecraft:snowy_taiga_mountains");
      hashmap.put("160", "minecraft:giant_spruce_taiga");
      hashmap.put("161", "minecraft:giant_spruce_taiga_hills");
      hashmap.put("162", "minecraft:modified_gravelly_mountains");
      hashmap.put("163", "minecraft:shattered_savanna");
      hashmap.put("164", "minecraft:shattered_savanna_plateau");
      hashmap.put("165", "minecraft:eroded_badlands");
      hashmap.put("166", "minecraft:modified_wooded_badlands_plateau");
      hashmap.put("167", "minecraft:modified_badlands_plateau");
   });
   public static final String GENERATOR_OPTIONS = "generatorOptions";

   public LevelDataGeneratorOptionsFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.LEVEL);
      return this.fixTypeEverywhereTyped("LevelDataGeneratorOptionsFix", this.getInputSchema().getType(References.LEVEL), type, (typed) -> typed.write().flatMap((dynamic) -> {
            Optional<String> optional = dynamic.get("generatorOptions").asString().result();
            Dynamic<?> dynamic1;
            if ("flat".equalsIgnoreCase(dynamic.get("generatorName").asString(""))) {
               String s = optional.orElse("");
               dynamic1 = dynamic.set("generatorOptions", convert(s, dynamic.getOps()));
            } else if ("buffet".equalsIgnoreCase(dynamic.get("generatorName").asString("")) && optional.isPresent()) {
               Dynamic<JsonElement> dynamic2 = new Dynamic<>(JsonOps.INSTANCE, GsonHelper.parse(optional.get(), true));
               dynamic1 = dynamic.set("generatorOptions", dynamic2.convert(dynamic.getOps()));
            } else {
               dynamic1 = dynamic;
            }

            return type.readTyped(dynamic1);
         }).map(Pair::getFirst).result().orElseThrow(() -> new IllegalStateException("Could not read new level type.")));
   }

   private static <T> Dynamic<T> convert(String s, DynamicOps<T> dynamicops) {
      Iterator<String> iterator = Splitter.on(';').split(s).iterator();
      String s1 = "minecraft:plains";
      Map<String, Map<String, String>> map = Maps.newHashMap();
      List<Pair<Integer, String>> list;
      if (!s.isEmpty() && iterator.hasNext()) {
         list = getLayersInfoFromString(iterator.next());
         if (!list.isEmpty()) {
            if (iterator.hasNext()) {
               s1 = MAP.getOrDefault(iterator.next(), "minecraft:plains");
            }

            if (iterator.hasNext()) {
               String[] astring = iterator.next().toLowerCase(Locale.ROOT).split(",");

               for(String s2 : astring) {
                  String[] astring1 = s2.split("\\(", 2);
                  if (!astring1[0].isEmpty()) {
                     map.put(astring1[0], Maps.newHashMap());
                     if (astring1.length > 1 && astring1[1].endsWith(")") && astring1[1].length() > 1) {
                        String[] astring2 = astring1[1].substring(0, astring1[1].length() - 1).split(" ");

                        for(String s3 : astring2) {
                           String[] astring3 = s3.split("=", 2);
                           if (astring3.length == 2) {
                              map.get(astring1[0]).put(astring3[0], astring3[1]);
                           }
                        }
                     }
                  }
               }
            } else {
               map.put("village", Maps.newHashMap());
            }
         }
      } else {
         list = Lists.newArrayList();
         list.add(Pair.of(1, "minecraft:bedrock"));
         list.add(Pair.of(2, "minecraft:dirt"));
         list.add(Pair.of(1, "minecraft:grass_block"));
         map.put("village", Maps.newHashMap());
      }

      T object = dynamicops.createList(list.stream().map((pair) -> dynamicops.createMap(ImmutableMap.of(dynamicops.createString("height"), dynamicops.createInt(pair.getFirst()), dynamicops.createString("block"), dynamicops.createString(pair.getSecond())))));
      T object1 = dynamicops.createMap(map.entrySet().stream().map((map_entry) -> Pair.of(dynamicops.createString(map_entry.getKey().toLowerCase(Locale.ROOT)), dynamicops.createMap(map_entry.getValue().entrySet().stream().map((map_entry1) -> Pair.of(dynamicops.createString(map_entry1.getKey()), dynamicops.createString(map_entry1.getValue()))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
      return new Dynamic<>(dynamicops, dynamicops.createMap(ImmutableMap.of(dynamicops.createString("layers"), object, dynamicops.createString("biome"), dynamicops.createString(s1), dynamicops.createString("structures"), object1)));
   }

   @Nullable
   private static Pair<Integer, String> getLayerInfoFromString(String s) {
      String[] astring = s.split("\\*", 2);
      int i;
      if (astring.length == 2) {
         try {
            i = Integer.parseInt(astring[0]);
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         i = 1;
      }

      String s1 = astring[astring.length - 1];
      return Pair.of(i, s1);
   }

   private static List<Pair<Integer, String>> getLayersInfoFromString(String s) {
      List<Pair<Integer, String>> list = Lists.newArrayList();
      String[] astring = s.split(",");

      for(String s1 : astring) {
         Pair<Integer, String> pair = getLayerInfoFromString(s1);
         if (pair == null) {
            return Collections.emptyList();
         }

         list.add(pair);
      }

      return list;
   }
}
