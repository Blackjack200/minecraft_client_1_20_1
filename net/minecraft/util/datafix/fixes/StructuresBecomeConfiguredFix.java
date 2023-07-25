package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StructuresBecomeConfiguredFix extends DataFix {
   private static final Map<String, StructuresBecomeConfiguredFix.Conversion> CONVERSION_MAP = ImmutableMap.<String, StructuresBecomeConfiguredFix.Conversion>builder().put("mineshaft", StructuresBecomeConfiguredFix.Conversion.biomeMapped(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put("shipwreck", StructuresBecomeConfiguredFix.Conversion.biomeMapped(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put("ocean_ruin", StructuresBecomeConfiguredFix.Conversion.biomeMapped(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put("village", StructuresBecomeConfiguredFix.Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put("ruined_portal", StructuresBecomeConfiguredFix.Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put("pillager_outpost", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:pillager_outpost")).put("mansion", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:mansion")).put("jungle_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:jungle_pyramid")).put("desert_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:desert_pyramid")).put("igloo", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:igloo")).put("swamp_hut", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:swamp_hut")).put("stronghold", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:stronghold")).put("monument", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:monument")).put("fortress", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:fortress")).put("endcity", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:end_city")).put("buried_treasure", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:buried_treasure")).put("nether_fossil", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:nether_fossil")).put("bastion_remnant", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:bastion_remnant")).build();

   public StructuresBecomeConfiguredFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      Type<?> type1 = this.getInputSchema().getType(References.CHUNK);
      return this.writeFixAndRead("StucturesToConfiguredStructures", type, type1, this::fix);
   }

   private Dynamic<?> fix(Dynamic<?> dynamic) {
      return dynamic.update("structures", (dynamic2) -> dynamic2.update("starts", (dynamic6) -> this.updateStarts(dynamic6, dynamic)).update("References", (dynamic4) -> this.updateReferences(dynamic4, dynamic)));
   }

   private Dynamic<?> updateStarts(Dynamic<?> dynamic, Dynamic<?> dynamic1) {
      Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = dynamic.getMapValues().result().get();
      List<Dynamic<?>> list = new ArrayList<>();
      map.forEach((dynamic4, dynamic5) -> {
         if (dynamic5.get("id").asString("INVALID").equals("INVALID")) {
            list.add(dynamic4);
         }

      });

      for(Dynamic<?> dynamic2 : list) {
         dynamic = dynamic.remove(dynamic2.asString(""));
      }

      return dynamic.updateMapValues((pair) -> this.updateStart(pair, dynamic1));
   }

   private Pair<Dynamic<?>, Dynamic<?>> updateStart(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
      Dynamic<?> dynamic1 = this.findUpdatedStructureType(pair, dynamic);
      return new Pair<>(dynamic1, pair.getSecond().set("id", dynamic1));
   }

   private Dynamic<?> updateReferences(Dynamic<?> dynamic, Dynamic<?> dynamic1) {
      Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = dynamic.getMapValues().result().get();
      List<Dynamic<?>> list = new ArrayList<>();
      map.forEach((dynamic4, dynamic5) -> {
         if (dynamic5.asLongStream().count() == 0L) {
            list.add(dynamic4);
         }

      });

      for(Dynamic<?> dynamic2 : list) {
         dynamic = dynamic.remove(dynamic2.asString(""));
      }

      return dynamic.updateMapValues((pair) -> this.updateReference(pair, dynamic1));
   }

   private Pair<Dynamic<?>, Dynamic<?>> updateReference(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
      return pair.mapFirst((dynamic2) -> this.findUpdatedStructureType(pair, dynamic));
   }

   private Dynamic<?> findUpdatedStructureType(Pair<Dynamic<?>, Dynamic<?>> pair, Dynamic<?> dynamic) {
      String s = pair.getFirst().asString("UNKNOWN").toLowerCase(Locale.ROOT);
      StructuresBecomeConfiguredFix.Conversion structuresbecomeconfiguredfix_conversion = CONVERSION_MAP.get(s);
      if (structuresbecomeconfiguredfix_conversion == null) {
         throw new IllegalStateException("Found unknown structure: " + s);
      } else {
         Dynamic<?> dynamic1 = pair.getSecond();
         String s1 = structuresbecomeconfiguredfix_conversion.fallback;
         if (!structuresbecomeconfiguredfix_conversion.biomeMapping().isEmpty()) {
            Optional<String> optional = this.guessConfiguration(dynamic, structuresbecomeconfiguredfix_conversion);
            if (optional.isPresent()) {
               s1 = optional.get();
            }
         }

         Dynamic<?> dynamic2 = dynamic1.createString(s1);
         return dynamic2;
      }
   }

   private Optional<String> guessConfiguration(Dynamic<?> dynamic, StructuresBecomeConfiguredFix.Conversion structuresbecomeconfiguredfix_conversion) {
      Object2IntArrayMap<String> object2intarraymap = new Object2IntArrayMap<>();
      dynamic.get("sections").asList(Function.identity()).forEach((dynamic1) -> dynamic1.get("biomes").get("palette").asList(Function.identity()).forEach((dynamic2) -> {
            String s = structuresbecomeconfiguredfix_conversion.biomeMapping().get(dynamic2.asString(""));
            if (s != null) {
               object2intarraymap.mergeInt(s, 1, Integer::sum);
            }

         }));
      return object2intarraymap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
   }

   static record Conversion(Map<String, String> biomeMapping, String fallback) {
      final String fallback;

      public static StructuresBecomeConfiguredFix.Conversion trivial(String s) {
         return new StructuresBecomeConfiguredFix.Conversion(Map.of(), s);
      }

      public static StructuresBecomeConfiguredFix.Conversion biomeMapped(Map<List<String>, String> map, String s) {
         return new StructuresBecomeConfiguredFix.Conversion(unpack(map), s);
      }

      private static Map<String, String> unpack(Map<List<String>, String> map) {
         ImmutableMap.Builder<String, String> immutablemap_builder = ImmutableMap.builder();

         for(Map.Entry<List<String>, String> map_entry : map.entrySet()) {
            map_entry.getKey().forEach((s) -> immutablemap_builder.put(s, map_entry.getValue()));
         }

         return immutablemap_builder.build();
      }
   }
}
