package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class WorldGenSettingsFix extends DataFix {
   private static final String VILLAGE = "minecraft:village";
   private static final String DESERT_PYRAMID = "minecraft:desert_pyramid";
   private static final String IGLOO = "minecraft:igloo";
   private static final String JUNGLE_TEMPLE = "minecraft:jungle_pyramid";
   private static final String SWAMP_HUT = "minecraft:swamp_hut";
   private static final String PILLAGER_OUTPOST = "minecraft:pillager_outpost";
   private static final String END_CITY = "minecraft:endcity";
   private static final String WOODLAND_MANSION = "minecraft:mansion";
   private static final String OCEAN_MONUMENT = "minecraft:monument";
   private static final ImmutableMap<String, WorldGenSettingsFix.StructureFeatureConfiguration> DEFAULTS = ImmutableMap.<String, WorldGenSettingsFix.StructureFeatureConfiguration>builder().put("minecraft:village", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 10387312)).put("minecraft:desert_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357617)).put("minecraft:igloo", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357618)).put("minecraft:jungle_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357619)).put("minecraft:swamp_hut", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357620)).put("minecraft:pillager_outpost", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 165745296)).put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 5, 10387313)).put("minecraft:endcity", new WorldGenSettingsFix.StructureFeatureConfiguration(20, 11, 10387313)).put("minecraft:mansion", new WorldGenSettingsFix.StructureFeatureConfiguration(80, 20, 10387319)).build();

   public WorldGenSettingsFix(Schema schema) {
      super(schema, true);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("WorldGenSettings building", this.getInputSchema().getType(References.WORLD_GEN_SETTINGS), (typed) -> typed.update(DSL.remainderFinder(), WorldGenSettingsFix::fix));
   }

   private static <T> Dynamic<T> noise(long i, DynamicLike<T> dynamiclike, Dynamic<T> dynamic, Dynamic<T> dynamic1) {
      return dynamiclike.createMap(ImmutableMap.of(dynamiclike.createString("type"), dynamiclike.createString("minecraft:noise"), dynamiclike.createString("biome_source"), dynamic1, dynamiclike.createString("seed"), dynamiclike.createLong(i), dynamiclike.createString("settings"), dynamic));
   }

   private static <T> Dynamic<T> vanillaBiomeSource(Dynamic<T> dynamic, long i, boolean flag, boolean flag1) {
      ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> immutablemap_builder = ImmutableMap.<Dynamic<T>, Dynamic<T>>builder().put(dynamic.createString("type"), dynamic.createString("minecraft:vanilla_layered")).put(dynamic.createString("seed"), dynamic.createLong(i)).put(dynamic.createString("large_biomes"), dynamic.createBoolean(flag1));
      if (flag) {
         immutablemap_builder.put(dynamic.createString("legacy_biome_init_layer"), dynamic.createBoolean(flag));
      }

      return dynamic.createMap(immutablemap_builder.build());
   }

   private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
      DynamicOps<T> dynamicops = dynamic.getOps();
      long i = dynamic.get("RandomSeed").asLong(0L);
      Optional<String> optional = dynamic.get("generatorName").asString().map((s2) -> s2.toLowerCase(Locale.ROOT)).result();
      Optional<String> optional1 = dynamic.get("legacy_custom_options").asString().result().map(Optional::of).orElseGet(() -> optional.equals(Optional.of("customized")) ? dynamic.get("generatorOptions").asString().result() : Optional.empty());
      boolean flag = false;
      Dynamic<T> dynamic1;
      if (optional.equals(Optional.of("customized"))) {
         dynamic1 = defaultOverworld(dynamic, i);
      } else if (!optional.isPresent()) {
         dynamic1 = defaultOverworld(dynamic, i);
      } else {
         switch ((String)optional.get()) {
            case "flat":
               OptionalDynamic<T> optionaldynamic = dynamic.get("generatorOptions");
               Map<Dynamic<T>, Dynamic<T>> map = fixFlatStructures(dynamicops, optionaldynamic);
               dynamic1 = dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:flat"), dynamic.createString("settings"), dynamic.createMap(ImmutableMap.of(dynamic.createString("structures"), dynamic.createMap(map), dynamic.createString("layers"), optionaldynamic.get("layers").result().orElseGet(() -> dynamic.createList(Stream.of(dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:bedrock"))), dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(2), dynamic.createString("block"), dynamic.createString("minecraft:dirt"))), dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:grass_block")))))), dynamic.createString("biome"), dynamic.createString(optionaldynamic.get("biome").asString("minecraft:plains"))))));
               break;
            case "debug_all_block_states":
               dynamic1 = dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:debug")));
               break;
            case "buffet":
               OptionalDynamic<T> optionaldynamic1 = dynamic.get("generatorOptions");
               OptionalDynamic<?> optionaldynamic2 = optionaldynamic1.get("chunk_generator");
               Optional<String> optional2 = optionaldynamic2.get("type").asString().result();
               Dynamic<T> dynamic5;
               if (Objects.equals(optional2, Optional.of("minecraft:caves"))) {
                  dynamic5 = dynamic.createString("minecraft:caves");
                  flag = true;
               } else if (Objects.equals(optional2, Optional.of("minecraft:floating_islands"))) {
                  dynamic5 = dynamic.createString("minecraft:floating_islands");
               } else {
                  dynamic5 = dynamic.createString("minecraft:overworld");
               }

               Dynamic<T> dynamic8 = optionaldynamic1.get("biome_source").result().orElseGet(() -> dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:fixed"))));
               Dynamic<T> dynamic9;
               if (dynamic8.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
                  String s = dynamic8.get("options").get("biomes").asStream().findFirst().flatMap((dynamic13) -> dynamic13.asString().result()).orElse("minecraft:ocean");
                  dynamic9 = dynamic8.remove("options").set("biome", dynamic.createString(s));
               } else {
                  dynamic9 = dynamic8;
               }

               dynamic1 = noise(i, dynamic, dynamic5, dynamic9);
               break;
            default:
               boolean flag1 = optional.get().equals("default");
               boolean flag2 = optional.get().equals("default_1_1") || flag1 && dynamic.get("generatorVersion").asInt(0) == 0;
               boolean flag3 = optional.get().equals("amplified");
               boolean flag4 = optional.get().equals("largebiomes");
               dynamic1 = noise(i, dynamic, dynamic.createString(flag3 ? "minecraft:amplified" : "minecraft:overworld"), vanillaBiomeSource(dynamic, i, flag2, flag4));
         }
      }

      boolean flag5 = dynamic.get("MapFeatures").asBoolean(true);
      boolean flag6 = dynamic.get("BonusChest").asBoolean(false);
      ImmutableMap.Builder<T, T> immutablemap_builder = ImmutableMap.builder();
      immutablemap_builder.put(dynamicops.createString("seed"), dynamicops.createLong(i));
      immutablemap_builder.put(dynamicops.createString("generate_features"), dynamicops.createBoolean(flag5));
      immutablemap_builder.put(dynamicops.createString("bonus_chest"), dynamicops.createBoolean(flag6));
      immutablemap_builder.put(dynamicops.createString("dimensions"), vanillaLevels(dynamic, i, dynamic1, flag));
      optional1.ifPresent((s1) -> immutablemap_builder.put(dynamicops.createString("legacy_custom_options"), dynamicops.createString(s1)));
      return new Dynamic<>(dynamicops, dynamicops.createMap(immutablemap_builder.build()));
   }

   protected static <T> Dynamic<T> defaultOverworld(Dynamic<T> dynamic, long i) {
      return noise(i, dynamic, dynamic.createString("minecraft:overworld"), vanillaBiomeSource(dynamic, i, false, false));
   }

   protected static <T> T vanillaLevels(Dynamic<T> dynamic, long i, Dynamic<T> dynamic1, boolean flag) {
      DynamicOps<T> dynamicops = dynamic.getOps();
      return dynamicops.createMap(ImmutableMap.of(dynamicops.createString("minecraft:overworld"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:overworld" + (flag ? "_caves" : "")), dynamicops.createString("generator"), dynamic1.getValue())), dynamicops.createString("minecraft:the_nether"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:the_nether"), dynamicops.createString("generator"), noise(i, dynamic, dynamic.createString("minecraft:nether"), dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:multi_noise"), dynamic.createString("seed"), dynamic.createLong(i), dynamic.createString("preset"), dynamic.createString("minecraft:nether")))).getValue())), dynamicops.createString("minecraft:the_end"), dynamicops.createMap(ImmutableMap.of(dynamicops.createString("type"), dynamicops.createString("minecraft:the_end"), dynamicops.createString("generator"), noise(i, dynamic, dynamic.createString("minecraft:end"), dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:the_end"), dynamic.createString("seed"), dynamic.createLong(i)))).getValue()))));
   }

   private static <T> Map<Dynamic<T>, Dynamic<T>> fixFlatStructures(DynamicOps<T> dynamicops, OptionalDynamic<T> optionaldynamic) {
      MutableInt mutableint = new MutableInt(32);
      MutableInt mutableint1 = new MutableInt(3);
      MutableInt mutableint2 = new MutableInt(128);
      MutableBoolean mutableboolean = new MutableBoolean(false);
      Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> map = Maps.newHashMap();
      if (!optionaldynamic.result().isPresent()) {
         mutableboolean.setTrue();
         map.put("minecraft:village", DEFAULTS.get("minecraft:village"));
      }

      optionaldynamic.get("structures").flatMap(Dynamic::getMapValues).result().ifPresent((map2) -> map2.forEach((dynamic, dynamic1) -> dynamic1.getMapValues().result().ifPresent((map5) -> map5.forEach((dynamic4, dynamic5) -> {
                  String s = dynamic.asString("");
                  String s1 = dynamic4.asString("");
                  String s2 = dynamic5.asString("");
                  if ("stronghold".equals(s)) {
                     mutableboolean.setTrue();
                     switch (s1) {
                        case "distance":
                           mutableint.setValue(getInt(s2, mutableint.getValue(), 1));
                           return;
                        case "spread":
                           mutableint1.setValue(getInt(s2, mutableint1.getValue(), 1));
                           return;
                        case "count":
                           mutableint2.setValue(getInt(s2, mutableint2.getValue(), 1));
                           return;
                        default:
                     }
                  } else {
                     switch (s1) {
                        case "distance":
                           switch (s) {
                              case "village":
                                 setSpacing(map, "minecraft:village", s2, 9);
                                 return;
                              case "biome_1":
                                 setSpacing(map, "minecraft:desert_pyramid", s2, 9);
                                 setSpacing(map, "minecraft:igloo", s2, 9);
                                 setSpacing(map, "minecraft:jungle_pyramid", s2, 9);
                                 setSpacing(map, "minecraft:swamp_hut", s2, 9);
                                 setSpacing(map, "minecraft:pillager_outpost", s2, 9);
                                 return;
                              case "endcity":
                                 setSpacing(map, "minecraft:endcity", s2, 1);
                                 return;
                              case "mansion":
                                 setSpacing(map, "minecraft:mansion", s2, 1);
                                 return;
                              default:
                                 return;
                           }
                        case "separation":
                           if ("oceanmonument".equals(s)) {
                              WorldGenSettingsFix.StructureFeatureConfiguration worldgensettingsfix_structurefeatureconfiguration = map.getOrDefault("minecraft:monument", DEFAULTS.get("minecraft:monument"));
                              int i = getInt(s2, worldgensettingsfix_structurefeatureconfiguration.separation, 1);
                              map.put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(i, worldgensettingsfix_structurefeatureconfiguration.separation, worldgensettingsfix_structurefeatureconfiguration.salt));
                           }

                           return;
                        case "spacing":
                           if ("oceanmonument".equals(s)) {
                              setSpacing(map, "minecraft:monument", s2, 1);
                           }

                           return;
                        default:
                     }
                  }
               }))));
      ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> immutablemap_builder = ImmutableMap.builder();
      immutablemap_builder.put(optionaldynamic.createString("structures"), optionaldynamic.createMap(map.entrySet().stream().collect(Collectors.toMap((map_entry1) -> optionaldynamic.createString(map_entry1.getKey()), (map_entry) -> map_entry.getValue().serialize(dynamicops)))));
      if (mutableboolean.isTrue()) {
         immutablemap_builder.put(optionaldynamic.createString("stronghold"), optionaldynamic.createMap(ImmutableMap.of(optionaldynamic.createString("distance"), optionaldynamic.createInt(mutableint.getValue()), optionaldynamic.createString("spread"), optionaldynamic.createInt(mutableint1.getValue()), optionaldynamic.createString("count"), optionaldynamic.createInt(mutableint2.getValue()))));
      }

      return immutablemap_builder.build();
   }

   private static int getInt(String s, int i) {
      return NumberUtils.toInt(s, i);
   }

   private static int getInt(String s, int i, int j) {
      return Math.max(j, getInt(s, i));
   }

   private static void setSpacing(Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> map, String s, String s1, int i) {
      WorldGenSettingsFix.StructureFeatureConfiguration worldgensettingsfix_structurefeatureconfiguration = map.getOrDefault(s, DEFAULTS.get(s));
      int j = getInt(s1, worldgensettingsfix_structurefeatureconfiguration.spacing, i);
      map.put(s, new WorldGenSettingsFix.StructureFeatureConfiguration(j, worldgensettingsfix_structurefeatureconfiguration.separation, worldgensettingsfix_structurefeatureconfiguration.salt));
   }

   static final class StructureFeatureConfiguration {
      public static final Codec<WorldGenSettingsFix.StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("spacing").forGetter((worldgensettingsfix_structurefeatureconfiguration2) -> worldgensettingsfix_structurefeatureconfiguration2.spacing), Codec.INT.fieldOf("separation").forGetter((worldgensettingsfix_structurefeatureconfiguration1) -> worldgensettingsfix_structurefeatureconfiguration1.separation), Codec.INT.fieldOf("salt").forGetter((worldgensettingsfix_structurefeatureconfiguration) -> worldgensettingsfix_structurefeatureconfiguration.salt)).apply(recordcodecbuilder_instance, WorldGenSettingsFix.StructureFeatureConfiguration::new));
      final int spacing;
      final int separation;
      final int salt;

      public StructureFeatureConfiguration(int i, int j, int k) {
         this.spacing = i;
         this.separation = j;
         this.salt = k;
      }

      public <T> Dynamic<T> serialize(DynamicOps<T> dynamicops) {
         return new Dynamic<>(dynamicops, CODEC.encodeStart(dynamicops, this).result().orElse(dynamicops.emptyMap()));
      }
   }
}
