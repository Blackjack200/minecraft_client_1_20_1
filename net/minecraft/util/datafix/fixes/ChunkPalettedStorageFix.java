package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.PackedBitStorage;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix extends DataFix {
   private static final int NORTH_WEST_MASK = 128;
   private static final int WEST_MASK = 64;
   private static final int SOUTH_WEST_MASK = 32;
   private static final int SOUTH_MASK = 16;
   private static final int SOUTH_EAST_MASK = 8;
   private static final int EAST_MASK = 4;
   private static final int NORTH_EAST_MASK = 2;
   private static final int NORTH_MASK = 1;
   static final Logger LOGGER = LogUtils.getLogger();
   static final BitSet VIRTUAL = new BitSet(256);
   static final BitSet FIX = new BitSet(256);
   static final Dynamic<?> PUMPKIN = BlockStateData.parse("{Name:'minecraft:pumpkin'}");
   static final Dynamic<?> SNOWY_PODZOL = BlockStateData.parse("{Name:'minecraft:podzol',Properties:{snowy:'true'}}");
   static final Dynamic<?> SNOWY_GRASS = BlockStateData.parse("{Name:'minecraft:grass_block',Properties:{snowy:'true'}}");
   static final Dynamic<?> SNOWY_MYCELIUM = BlockStateData.parse("{Name:'minecraft:mycelium',Properties:{snowy:'true'}}");
   static final Dynamic<?> UPPER_SUNFLOWER = BlockStateData.parse("{Name:'minecraft:sunflower',Properties:{half:'upper'}}");
   static final Dynamic<?> UPPER_LILAC = BlockStateData.parse("{Name:'minecraft:lilac',Properties:{half:'upper'}}");
   static final Dynamic<?> UPPER_TALL_GRASS = BlockStateData.parse("{Name:'minecraft:tall_grass',Properties:{half:'upper'}}");
   static final Dynamic<?> UPPER_LARGE_FERN = BlockStateData.parse("{Name:'minecraft:large_fern',Properties:{half:'upper'}}");
   static final Dynamic<?> UPPER_ROSE_BUSH = BlockStateData.parse("{Name:'minecraft:rose_bush',Properties:{half:'upper'}}");
   static final Dynamic<?> UPPER_PEONY = BlockStateData.parse("{Name:'minecraft:peony',Properties:{half:'upper'}}");
   static final Map<String, Dynamic<?>> FLOWER_POT_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("minecraft:air0", BlockStateData.parse("{Name:'minecraft:flower_pot'}"));
      hashmap.put("minecraft:red_flower0", BlockStateData.parse("{Name:'minecraft:potted_poppy'}"));
      hashmap.put("minecraft:red_flower1", BlockStateData.parse("{Name:'minecraft:potted_blue_orchid'}"));
      hashmap.put("minecraft:red_flower2", BlockStateData.parse("{Name:'minecraft:potted_allium'}"));
      hashmap.put("minecraft:red_flower3", BlockStateData.parse("{Name:'minecraft:potted_azure_bluet'}"));
      hashmap.put("minecraft:red_flower4", BlockStateData.parse("{Name:'minecraft:potted_red_tulip'}"));
      hashmap.put("minecraft:red_flower5", BlockStateData.parse("{Name:'minecraft:potted_orange_tulip'}"));
      hashmap.put("minecraft:red_flower6", BlockStateData.parse("{Name:'minecraft:potted_white_tulip'}"));
      hashmap.put("minecraft:red_flower7", BlockStateData.parse("{Name:'minecraft:potted_pink_tulip'}"));
      hashmap.put("minecraft:red_flower8", BlockStateData.parse("{Name:'minecraft:potted_oxeye_daisy'}"));
      hashmap.put("minecraft:yellow_flower0", BlockStateData.parse("{Name:'minecraft:potted_dandelion'}"));
      hashmap.put("minecraft:sapling0", BlockStateData.parse("{Name:'minecraft:potted_oak_sapling'}"));
      hashmap.put("minecraft:sapling1", BlockStateData.parse("{Name:'minecraft:potted_spruce_sapling'}"));
      hashmap.put("minecraft:sapling2", BlockStateData.parse("{Name:'minecraft:potted_birch_sapling'}"));
      hashmap.put("minecraft:sapling3", BlockStateData.parse("{Name:'minecraft:potted_jungle_sapling'}"));
      hashmap.put("minecraft:sapling4", BlockStateData.parse("{Name:'minecraft:potted_acacia_sapling'}"));
      hashmap.put("minecraft:sapling5", BlockStateData.parse("{Name:'minecraft:potted_dark_oak_sapling'}"));
      hashmap.put("minecraft:red_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_red_mushroom'}"));
      hashmap.put("minecraft:brown_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_brown_mushroom'}"));
      hashmap.put("minecraft:deadbush0", BlockStateData.parse("{Name:'minecraft:potted_dead_bush'}"));
      hashmap.put("minecraft:tallgrass2", BlockStateData.parse("{Name:'minecraft:potted_fern'}"));
      hashmap.put("minecraft:cactus0", BlockStateData.getTag(2240));
   });
   static final Map<String, Dynamic<?>> SKULL_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      mapSkull(hashmap, 0, "skeleton", "skull");
      mapSkull(hashmap, 1, "wither_skeleton", "skull");
      mapSkull(hashmap, 2, "zombie", "head");
      mapSkull(hashmap, 3, "player", "head");
      mapSkull(hashmap, 4, "creeper", "head");
      mapSkull(hashmap, 5, "dragon", "head");
   });
   static final Map<String, Dynamic<?>> DOOR_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      mapDoor(hashmap, "oak_door", 1024);
      mapDoor(hashmap, "iron_door", 1136);
      mapDoor(hashmap, "spruce_door", 3088);
      mapDoor(hashmap, "birch_door", 3104);
      mapDoor(hashmap, "jungle_door", 3120);
      mapDoor(hashmap, "acacia_door", 3136);
      mapDoor(hashmap, "dark_oak_door", 3152);
   });
   static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      for(int i = 0; i < 26; ++i) {
         hashmap.put("true" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'true',note:'" + i + "'}}"));
         hashmap.put("false" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'false',note:'" + i + "'}}"));
      }

   });
   private static final Int2ObjectMap<String> DYE_COLOR_MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), (int2objectopenhashmap) -> {
      int2objectopenhashmap.put(0, "white");
      int2objectopenhashmap.put(1, "orange");
      int2objectopenhashmap.put(2, "magenta");
      int2objectopenhashmap.put(3, "light_blue");
      int2objectopenhashmap.put(4, "yellow");
      int2objectopenhashmap.put(5, "lime");
      int2objectopenhashmap.put(6, "pink");
      int2objectopenhashmap.put(7, "gray");
      int2objectopenhashmap.put(8, "light_gray");
      int2objectopenhashmap.put(9, "cyan");
      int2objectopenhashmap.put(10, "purple");
      int2objectopenhashmap.put(11, "blue");
      int2objectopenhashmap.put(12, "brown");
      int2objectopenhashmap.put(13, "green");
      int2objectopenhashmap.put(14, "red");
      int2objectopenhashmap.put(15, "black");
   });
   static final Map<String, Dynamic<?>> BED_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      for(Int2ObjectMap.Entry<String> int2objectmap_entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
         if (!Objects.equals(int2objectmap_entry.getValue(), "red")) {
            addBeds(hashmap, int2objectmap_entry.getIntKey(), int2objectmap_entry.getValue());
         }
      }

   });
   static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      for(Int2ObjectMap.Entry<String> int2objectmap_entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
         if (!Objects.equals(int2objectmap_entry.getValue(), "white")) {
            addBanners(hashmap, 15 - int2objectmap_entry.getIntKey(), int2objectmap_entry.getValue());
         }
      }

   });
   static final Dynamic<?> AIR;
   private static final int SIZE = 4096;

   public ChunkPalettedStorageFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   private static void mapSkull(Map<String, Dynamic<?>> map, int i, String s, String s1) {
      map.put(i + "north", BlockStateData.parse("{Name:'minecraft:" + s + "_wall_" + s1 + "',Properties:{facing:'north'}}"));
      map.put(i + "east", BlockStateData.parse("{Name:'minecraft:" + s + "_wall_" + s1 + "',Properties:{facing:'east'}}"));
      map.put(i + "south", BlockStateData.parse("{Name:'minecraft:" + s + "_wall_" + s1 + "',Properties:{facing:'south'}}"));
      map.put(i + "west", BlockStateData.parse("{Name:'minecraft:" + s + "_wall_" + s1 + "',Properties:{facing:'west'}}"));

      for(int j = 0; j < 16; ++j) {
         map.put("" + i + j, BlockStateData.parse("{Name:'minecraft:" + s + "_" + s1 + "',Properties:{rotation:'" + j + "'}}"));
      }

   }

   private static void mapDoor(Map<String, Dynamic<?>> map, String s, int i) {
      map.put("minecraft:" + s + "eastlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "eastlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "eastlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "eastlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "eastlowerrightfalsefalse", BlockStateData.getTag(i));
      map.put("minecraft:" + s + "eastlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "eastlowerrighttruefalse", BlockStateData.getTag(i + 4));
      map.put("minecraft:" + s + "eastlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "eastupperleftfalsefalse", BlockStateData.getTag(i + 8));
      map.put("minecraft:" + s + "eastupperleftfalsetrue", BlockStateData.getTag(i + 10));
      map.put("minecraft:" + s + "eastupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "eastupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "eastupperrightfalsefalse", BlockStateData.getTag(i + 9));
      map.put("minecraft:" + s + "eastupperrightfalsetrue", BlockStateData.getTag(i + 11));
      map.put("minecraft:" + s + "eastupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "eastupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "northlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "northlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "northlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "northlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "northlowerrightfalsefalse", BlockStateData.getTag(i + 3));
      map.put("minecraft:" + s + "northlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "northlowerrighttruefalse", BlockStateData.getTag(i + 7));
      map.put("minecraft:" + s + "northlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "northupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "northupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "northupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "northupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "northupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "northupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "northupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "northupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "southlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "southlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "southlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "southlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "southlowerrightfalsefalse", BlockStateData.getTag(i + 1));
      map.put("minecraft:" + s + "southlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "southlowerrighttruefalse", BlockStateData.getTag(i + 5));
      map.put("minecraft:" + s + "southlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "southupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "southupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "southupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "southupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "southupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "southupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "southupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "southupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "westlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "westlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "westlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "westlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "westlowerrightfalsefalse", BlockStateData.getTag(i + 2));
      map.put("minecraft:" + s + "westlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "westlowerrighttruefalse", BlockStateData.getTag(i + 6));
      map.put("minecraft:" + s + "westlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "westupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "westupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "westupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "westupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      map.put("minecraft:" + s + "westupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      map.put("minecraft:" + s + "westupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      map.put("minecraft:" + s + "westupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      map.put("minecraft:" + s + "westupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + s + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
   }

   private static void addBeds(Map<String, Dynamic<?>> map, int i, String s) {
      map.put("southfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'south',occupied:'false',part:'foot'}}"));
      map.put("westfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'west',occupied:'false',part:'foot'}}"));
      map.put("northfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'north',occupied:'false',part:'foot'}}"));
      map.put("eastfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'east',occupied:'false',part:'foot'}}"));
      map.put("southfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'south',occupied:'false',part:'head'}}"));
      map.put("westfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'west',occupied:'false',part:'head'}}"));
      map.put("northfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'north',occupied:'false',part:'head'}}"));
      map.put("eastfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'east',occupied:'false',part:'head'}}"));
      map.put("southtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'south',occupied:'true',part:'head'}}"));
      map.put("westtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'west',occupied:'true',part:'head'}}"));
      map.put("northtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'north',occupied:'true',part:'head'}}"));
      map.put("easttruehead" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_bed',Properties:{facing:'east',occupied:'true',part:'head'}}"));
   }

   private static void addBanners(Map<String, Dynamic<?>> map, int i, String s) {
      for(int j = 0; j < 16; ++j) {
         map.put(j + "_" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_banner',Properties:{rotation:'" + j + "'}}"));
      }

      map.put("north_" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_wall_banner',Properties:{facing:'north'}}"));
      map.put("south_" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_wall_banner',Properties:{facing:'south'}}"));
      map.put("west_" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_wall_banner',Properties:{facing:'west'}}"));
      map.put("east_" + i, BlockStateData.parse("{Name:'minecraft:" + s + "_wall_banner',Properties:{facing:'east'}}"));
   }

   public static String getName(Dynamic<?> dynamic) {
      return dynamic.get("Name").asString("");
   }

   public static String getProperty(Dynamic<?> dynamic, String s) {
      return dynamic.get("Properties").get(s).asString("");
   }

   public static int idFor(CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> crudeincrementalintidentityhashbimap, Dynamic<?> dynamic) {
      int i = crudeincrementalintidentityhashbimap.getId(dynamic);
      if (i == -1) {
         i = crudeincrementalintidentityhashbimap.add(dynamic);
      }

      return i;
   }

   private Dynamic<?> fix(Dynamic<?> dynamic) {
      Optional<? extends Dynamic<?>> optional = dynamic.get("Level").result();
      return optional.isPresent() && optional.get().get("Sections").asStreamOpt().result().isPresent() ? dynamic.set("Level", (new ChunkPalettedStorageFix.UpgradeChunk(optional.get())).write()) : dynamic;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      Type<?> type1 = this.getOutputSchema().getType(References.CHUNK);
      return this.writeFixAndRead("ChunkPalettedStorageFix", type, type1, this::fix);
   }

   public static int getSideMask(boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      int i = 0;
      if (flag2) {
         if (flag1) {
            i |= 2;
         } else if (flag) {
            i |= 128;
         } else {
            i |= 1;
         }
      } else if (flag3) {
         if (flag) {
            i |= 32;
         } else if (flag1) {
            i |= 8;
         } else {
            i |= 16;
         }
      } else if (flag1) {
         i |= 4;
      } else if (flag) {
         i |= 64;
      }

      return i;
   }

   static {
      FIX.set(2);
      FIX.set(3);
      FIX.set(110);
      FIX.set(140);
      FIX.set(144);
      FIX.set(25);
      FIX.set(86);
      FIX.set(26);
      FIX.set(176);
      FIX.set(177);
      FIX.set(175);
      FIX.set(64);
      FIX.set(71);
      FIX.set(193);
      FIX.set(194);
      FIX.set(195);
      FIX.set(196);
      FIX.set(197);
      VIRTUAL.set(54);
      VIRTUAL.set(146);
      VIRTUAL.set(25);
      VIRTUAL.set(26);
      VIRTUAL.set(51);
      VIRTUAL.set(53);
      VIRTUAL.set(67);
      VIRTUAL.set(108);
      VIRTUAL.set(109);
      VIRTUAL.set(114);
      VIRTUAL.set(128);
      VIRTUAL.set(134);
      VIRTUAL.set(135);
      VIRTUAL.set(136);
      VIRTUAL.set(156);
      VIRTUAL.set(163);
      VIRTUAL.set(164);
      VIRTUAL.set(180);
      VIRTUAL.set(203);
      VIRTUAL.set(55);
      VIRTUAL.set(85);
      VIRTUAL.set(113);
      VIRTUAL.set(188);
      VIRTUAL.set(189);
      VIRTUAL.set(190);
      VIRTUAL.set(191);
      VIRTUAL.set(192);
      VIRTUAL.set(93);
      VIRTUAL.set(94);
      VIRTUAL.set(101);
      VIRTUAL.set(102);
      VIRTUAL.set(160);
      VIRTUAL.set(106);
      VIRTUAL.set(107);
      VIRTUAL.set(183);
      VIRTUAL.set(184);
      VIRTUAL.set(185);
      VIRTUAL.set(186);
      VIRTUAL.set(187);
      VIRTUAL.set(132);
      VIRTUAL.set(139);
      VIRTUAL.set(199);
      AIR = BlockStateData.getTag(0);
   }

   static class DataLayer {
      private static final int SIZE = 2048;
      private static final int NIBBLE_SIZE = 4;
      private final byte[] data;

      public DataLayer() {
         this.data = new byte[2048];
      }

      public DataLayer(byte[] abyte) {
         this.data = abyte;
         if (abyte.length != 2048) {
            throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + abyte.length);
         }
      }

      public int get(int i, int j, int k) {
         int l = this.getPosition(j << 8 | k << 4 | i);
         return this.isFirst(j << 8 | k << 4 | i) ? this.data[l] & 15 : this.data[l] >> 4 & 15;
      }

      private boolean isFirst(int i) {
         return (i & 1) == 0;
      }

      private int getPosition(int i) {
         return i >> 1;
      }
   }

   public static enum Direction {
      DOWN(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
      UP(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
      NORTH(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
      SOUTH(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
      WEST(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.X),
      EAST(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.X);

      private final ChunkPalettedStorageFix.Direction.Axis axis;
      private final ChunkPalettedStorageFix.Direction.AxisDirection axisDirection;

      private Direction(ChunkPalettedStorageFix.Direction.AxisDirection chunkpalettedstoragefix_direction_axisdirection, ChunkPalettedStorageFix.Direction.Axis chunkpalettedstoragefix_direction_axis) {
         this.axis = chunkpalettedstoragefix_direction_axis;
         this.axisDirection = chunkpalettedstoragefix_direction_axisdirection;
      }

      public ChunkPalettedStorageFix.Direction.AxisDirection getAxisDirection() {
         return this.axisDirection;
      }

      public ChunkPalettedStorageFix.Direction.Axis getAxis() {
         return this.axis;
      }

      public static enum Axis {
         X,
         Y,
         Z;
      }

      public static enum AxisDirection {
         POSITIVE(1),
         NEGATIVE(-1);

         private final int step;

         private AxisDirection(int i) {
            this.step = i;
         }

         public int getStep() {
            return this.step;
         }
      }
   }

   static class Section {
      private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = CrudeIncrementalIntIdentityHashBiMap.create(32);
      private final List<Dynamic<?>> listTag;
      private final Dynamic<?> section;
      private final boolean hasData;
      final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap<>();
      final IntList update = new IntArrayList();
      public final int y;
      private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
      private final int[] buffer = new int[4096];

      public Section(Dynamic<?> dynamic) {
         this.listTag = Lists.newArrayList();
         this.section = dynamic;
         this.y = dynamic.get("Y").asInt(0);
         this.hasData = dynamic.get("Blocks").result().isPresent();
      }

      public Dynamic<?> getBlock(int i) {
         if (i >= 0 && i <= 4095) {
            Dynamic<?> dynamic = this.palette.byId(this.buffer[i]);
            return dynamic == null ? ChunkPalettedStorageFix.AIR : dynamic;
         } else {
            return ChunkPalettedStorageFix.AIR;
         }
      }

      public void setBlock(int i, Dynamic<?> dynamic) {
         if (this.seen.add(dynamic)) {
            this.listTag.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? ChunkPalettedStorageFix.AIR : dynamic);
         }

         this.buffer[i] = ChunkPalettedStorageFix.idFor(this.palette, dynamic);
      }

      public int upgrade(int i) {
         if (!this.hasData) {
            return i;
         } else {
            ByteBuffer bytebuffer = this.section.get("Blocks").asByteBufferOpt().result().get();
            ChunkPalettedStorageFix.DataLayer chunkpalettedstoragefix_datalayer = this.section.get("Data").asByteBufferOpt().map((bytebuffer2) -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(bytebuffer2))).result().orElseGet(ChunkPalettedStorageFix.DataLayer::new);
            ChunkPalettedStorageFix.DataLayer chunkpalettedstoragefix_datalayer1 = this.section.get("Add").asByteBufferOpt().map((bytebuffer1) -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(bytebuffer1))).result().orElseGet(ChunkPalettedStorageFix.DataLayer::new);
            this.seen.add(ChunkPalettedStorageFix.AIR);
            ChunkPalettedStorageFix.idFor(this.palette, ChunkPalettedStorageFix.AIR);
            this.listTag.add(ChunkPalettedStorageFix.AIR);

            for(int j = 0; j < 4096; ++j) {
               int k = j & 15;
               int l = j >> 8 & 15;
               int i1 = j >> 4 & 15;
               int j1 = chunkpalettedstoragefix_datalayer1.get(k, l, i1) << 12 | (bytebuffer.get(j) & 255) << 4 | chunkpalettedstoragefix_datalayer.get(k, l, i1);
               if (ChunkPalettedStorageFix.FIX.get(j1 >> 4)) {
                  this.addFix(j1 >> 4, j);
               }

               if (ChunkPalettedStorageFix.VIRTUAL.get(j1 >> 4)) {
                  int k1 = ChunkPalettedStorageFix.getSideMask(k == 0, k == 15, i1 == 0, i1 == 15);
                  if (k1 == 0) {
                     this.update.add(j);
                  } else {
                     i |= k1;
                  }
               }

               this.setBlock(j, BlockStateData.getTag(j1));
            }

            return i;
         }
      }

      private void addFix(int i, int j) {
         IntList intlist = this.toFix.get(i);
         if (intlist == null) {
            intlist = new IntArrayList();
            this.toFix.put(i, intlist);
         }

         intlist.add(j);
      }

      public Dynamic<?> write() {
         Dynamic<?> dynamic = this.section;
         if (!this.hasData) {
            return dynamic;
         } else {
            dynamic = dynamic.set("Palette", dynamic.createList(this.listTag.stream()));
            int i = Math.max(4, DataFixUtils.ceillog2(this.seen.size()));
            PackedBitStorage packedbitstorage = new PackedBitStorage(i, 4096);

            for(int j = 0; j < this.buffer.length; ++j) {
               packedbitstorage.set(j, this.buffer[j]);
            }

            dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(packedbitstorage.getRaw())));
            dynamic = dynamic.remove("Blocks");
            dynamic = dynamic.remove("Data");
            return dynamic.remove("Add");
         }
      }
   }

   static final class UpgradeChunk {
      private int sides;
      private final ChunkPalettedStorageFix.Section[] sections = new ChunkPalettedStorageFix.Section[16];
      private final Dynamic<?> level;
      private final int x;
      private final int z;
      private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap<>(16);

      public UpgradeChunk(Dynamic<?> dynamic) {
         this.level = dynamic;
         this.x = dynamic.get("xPos").asInt(0) << 4;
         this.z = dynamic.get("zPos").asInt(0) << 4;
         dynamic.get("TileEntities").asStreamOpt().result().ifPresent((stream1) -> stream1.forEach((dynamic18) -> {
               int l3 = dynamic18.get("x").asInt(0) - this.x & 15;
               int i4 = dynamic18.get("y").asInt(0);
               int j4 = dynamic18.get("z").asInt(0) - this.z & 15;
               int k4 = i4 << 8 | j4 << 4 | l3;
               if (this.blockEntities.put(k4, dynamic18) != null) {
                  ChunkPalettedStorageFix.LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", this.x, this.z, l3, i4, j4);
               }

            }));
         boolean flag = dynamic.get("convertedFromAlphaFormat").asBoolean(false);
         dynamic.get("Sections").asStreamOpt().result().ifPresent((stream) -> stream.forEach((dynamic17) -> {
               ChunkPalettedStorageFix.Section chunkpalettedstoragefix_section1 = new ChunkPalettedStorageFix.Section(dynamic17);
               this.sides = chunkpalettedstoragefix_section1.upgrade(this.sides);
               this.sections[chunkpalettedstoragefix_section1.y] = chunkpalettedstoragefix_section1;
            }));

         for(ChunkPalettedStorageFix.Section chunkpalettedstoragefix_section : this.sections) {
            if (chunkpalettedstoragefix_section != null) {
               for(Map.Entry<Integer, IntList> map_entry : chunkpalettedstoragefix_section.toFix.entrySet()) {
                  int i = chunkpalettedstoragefix_section.y << 12;
                  switch (map_entry.getKey()) {
                     case 2:
                        for(int j : map_entry.getValue()) {
                           j |= i;
                           Dynamic<?> dynamic1 = this.getBlock(j);
                           if ("minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(dynamic1))) {
                              String s = ChunkPalettedStorageFix.getName(this.getBlock(relative(j, ChunkPalettedStorageFix.Direction.UP)));
                              if ("minecraft:snow".equals(s) || "minecraft:snow_layer".equals(s)) {
                                 this.setBlock(j, ChunkPalettedStorageFix.SNOWY_GRASS);
                              }
                           }
                        }
                        break;
                     case 3:
                        for(int k : map_entry.getValue()) {
                           k |= i;
                           Dynamic<?> dynamic2 = this.getBlock(k);
                           if ("minecraft:podzol".equals(ChunkPalettedStorageFix.getName(dynamic2))) {
                              String s1 = ChunkPalettedStorageFix.getName(this.getBlock(relative(k, ChunkPalettedStorageFix.Direction.UP)));
                              if ("minecraft:snow".equals(s1) || "minecraft:snow_layer".equals(s1)) {
                                 this.setBlock(k, ChunkPalettedStorageFix.SNOWY_PODZOL);
                              }
                           }
                        }
                        break;
                     case 25:
                        for(int i1 : map_entry.getValue()) {
                           i1 |= i;
                           Dynamic<?> dynamic4 = this.removeBlockEntity(i1);
                           if (dynamic4 != null) {
                              String s3 = Boolean.toString(dynamic4.get("powered").asBoolean(false)) + (byte)Math.min(Math.max(dynamic4.get("note").asInt(0), 0), 24);
                              this.setBlock(i1, ChunkPalettedStorageFix.NOTE_BLOCK_MAP.getOrDefault(s3, ChunkPalettedStorageFix.NOTE_BLOCK_MAP.get("false0")));
                           }
                        }
                        break;
                     case 26:
                        for(int j1 : map_entry.getValue()) {
                           j1 |= i;
                           Dynamic<?> dynamic5 = this.getBlockEntity(j1);
                           Dynamic<?> dynamic6 = this.getBlock(j1);
                           if (dynamic5 != null) {
                              int k1 = dynamic5.get("color").asInt(0);
                              if (k1 != 14 && k1 >= 0 && k1 < 16) {
                                 String s4 = ChunkPalettedStorageFix.getProperty(dynamic6, "facing") + ChunkPalettedStorageFix.getProperty(dynamic6, "occupied") + ChunkPalettedStorageFix.getProperty(dynamic6, "part") + k1;
                                 if (ChunkPalettedStorageFix.BED_BLOCK_MAP.containsKey(s4)) {
                                    this.setBlock(j1, ChunkPalettedStorageFix.BED_BLOCK_MAP.get(s4));
                                 }
                              }
                           }
                        }
                        break;
                     case 64:
                     case 71:
                     case 193:
                     case 194:
                     case 195:
                     case 196:
                     case 197:
                        for(int i3 : map_entry.getValue()) {
                           i3 |= i;
                           Dynamic<?> dynamic12 = this.getBlock(i3);
                           if (ChunkPalettedStorageFix.getName(dynamic12).endsWith("_door")) {
                              Dynamic<?> dynamic13 = this.getBlock(i3);
                              if ("lower".equals(ChunkPalettedStorageFix.getProperty(dynamic13, "half"))) {
                                 int j3 = relative(i3, ChunkPalettedStorageFix.Direction.UP);
                                 Dynamic<?> dynamic14 = this.getBlock(j3);
                                 String s12 = ChunkPalettedStorageFix.getName(dynamic13);
                                 if (s12.equals(ChunkPalettedStorageFix.getName(dynamic14))) {
                                    String s13 = ChunkPalettedStorageFix.getProperty(dynamic13, "facing");
                                    String s14 = ChunkPalettedStorageFix.getProperty(dynamic13, "open");
                                    String s15 = flag ? "left" : ChunkPalettedStorageFix.getProperty(dynamic14, "hinge");
                                    String s16 = flag ? "false" : ChunkPalettedStorageFix.getProperty(dynamic14, "powered");
                                    this.setBlock(i3, ChunkPalettedStorageFix.DOOR_MAP.get(s12 + s13 + "lower" + s15 + s14 + s16));
                                    this.setBlock(j3, ChunkPalettedStorageFix.DOOR_MAP.get(s12 + s13 + "upper" + s15 + s14 + s16));
                                 }
                              }
                           }
                        }
                        break;
                     case 86:
                        for(int j2 : map_entry.getValue()) {
                           j2 |= i;
                           Dynamic<?> dynamic9 = this.getBlock(j2);
                           if ("minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(dynamic9))) {
                              String s6 = ChunkPalettedStorageFix.getName(this.getBlock(relative(j2, ChunkPalettedStorageFix.Direction.DOWN)));
                              if ("minecraft:grass_block".equals(s6) || "minecraft:dirt".equals(s6)) {
                                 this.setBlock(j2, ChunkPalettedStorageFix.PUMPKIN);
                              }
                           }
                        }
                        break;
                     case 110:
                        for(int l : map_entry.getValue()) {
                           l |= i;
                           Dynamic<?> dynamic3 = this.getBlock(l);
                           if ("minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(dynamic3))) {
                              String s2 = ChunkPalettedStorageFix.getName(this.getBlock(relative(l, ChunkPalettedStorageFix.Direction.UP)));
                              if ("minecraft:snow".equals(s2) || "minecraft:snow_layer".equals(s2)) {
                                 this.setBlock(l, ChunkPalettedStorageFix.SNOWY_MYCELIUM);
                              }
                           }
                        }
                        break;
                     case 140:
                        for(int k2 : map_entry.getValue()) {
                           k2 |= i;
                           Dynamic<?> dynamic10 = this.removeBlockEntity(k2);
                           if (dynamic10 != null) {
                              String s7 = dynamic10.get("Item").asString("") + dynamic10.get("Data").asInt(0);
                              this.setBlock(k2, ChunkPalettedStorageFix.FLOWER_POT_MAP.getOrDefault(s7, ChunkPalettedStorageFix.FLOWER_POT_MAP.get("minecraft:air0")));
                           }
                        }
                        break;
                     case 144:
                        for(int l2 : map_entry.getValue()) {
                           l2 |= i;
                           Dynamic<?> dynamic11 = this.getBlockEntity(l2);
                           if (dynamic11 != null) {
                              String s8 = String.valueOf(dynamic11.get("SkullType").asInt(0));
                              String s9 = ChunkPalettedStorageFix.getProperty(this.getBlock(l2), "facing");
                              String s11;
                              if (!"up".equals(s9) && !"down".equals(s9)) {
                                 s11 = s8 + s9;
                              } else {
                                 s11 = s8 + String.valueOf(dynamic11.get("Rot").asInt(0));
                              }

                              dynamic11.remove("SkullType");
                              dynamic11.remove("facing");
                              dynamic11.remove("Rot");
                              this.setBlock(l2, ChunkPalettedStorageFix.SKULL_MAP.getOrDefault(s11, ChunkPalettedStorageFix.SKULL_MAP.get("0north")));
                           }
                        }
                        break;
                     case 175:
                        for(int k3 : map_entry.getValue()) {
                           k3 |= i;
                           Dynamic<?> dynamic15 = this.getBlock(k3);
                           if ("upper".equals(ChunkPalettedStorageFix.getProperty(dynamic15, "half"))) {
                              Dynamic<?> dynamic16 = this.getBlock(relative(k3, ChunkPalettedStorageFix.Direction.DOWN));
                              String s17 = ChunkPalettedStorageFix.getName(dynamic16);
                              if ("minecraft:sunflower".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_SUNFLOWER);
                              } else if ("minecraft:lilac".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_LILAC);
                              } else if ("minecraft:tall_grass".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_TALL_GRASS);
                              } else if ("minecraft:large_fern".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_LARGE_FERN);
                              } else if ("minecraft:rose_bush".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_ROSE_BUSH);
                              } else if ("minecraft:peony".equals(s17)) {
                                 this.setBlock(k3, ChunkPalettedStorageFix.UPPER_PEONY);
                              }
                           }
                        }
                        break;
                     case 176:
                     case 177:
                        for(int l1 : map_entry.getValue()) {
                           l1 |= i;
                           Dynamic<?> dynamic7 = this.getBlockEntity(l1);
                           Dynamic<?> dynamic8 = this.getBlock(l1);
                           if (dynamic7 != null) {
                              int i2 = dynamic7.get("Base").asInt(0);
                              if (i2 != 15 && i2 >= 0 && i2 < 16) {
                                 String s5 = ChunkPalettedStorageFix.getProperty(dynamic8, map_entry.getKey() == 176 ? "rotation" : "facing") + "_" + i2;
                                 if (ChunkPalettedStorageFix.BANNER_BLOCK_MAP.containsKey(s5)) {
                                    this.setBlock(l1, ChunkPalettedStorageFix.BANNER_BLOCK_MAP.get(s5));
                                 }
                              }
                           }
                        }
                  }
               }
            }
         }

      }

      @Nullable
      private Dynamic<?> getBlockEntity(int i) {
         return this.blockEntities.get(i);
      }

      @Nullable
      private Dynamic<?> removeBlockEntity(int i) {
         return this.blockEntities.remove(i);
      }

      public static int relative(int i, ChunkPalettedStorageFix.Direction chunkpalettedstoragefix_direction) {
         switch (chunkpalettedstoragefix_direction.getAxis()) {
            case X:
               int j = (i & 15) + chunkpalettedstoragefix_direction.getAxisDirection().getStep();
               return j >= 0 && j <= 15 ? i & -16 | j : -1;
            case Y:
               int k = (i >> 8) + chunkpalettedstoragefix_direction.getAxisDirection().getStep();
               return k >= 0 && k <= 255 ? i & 255 | k << 8 : -1;
            case Z:
               int l = (i >> 4 & 15) + chunkpalettedstoragefix_direction.getAxisDirection().getStep();
               return l >= 0 && l <= 15 ? i & -241 | l << 4 : -1;
            default:
               return -1;
         }
      }

      private void setBlock(int i, Dynamic<?> dynamic) {
         if (i >= 0 && i <= 65535) {
            ChunkPalettedStorageFix.Section chunkpalettedstoragefix_section = this.getSection(i);
            if (chunkpalettedstoragefix_section != null) {
               chunkpalettedstoragefix_section.setBlock(i & 4095, dynamic);
            }
         }
      }

      @Nullable
      private ChunkPalettedStorageFix.Section getSection(int i) {
         int j = i >> 12;
         return j < this.sections.length ? this.sections[j] : null;
      }

      public Dynamic<?> getBlock(int i) {
         if (i >= 0 && i <= 65535) {
            ChunkPalettedStorageFix.Section chunkpalettedstoragefix_section = this.getSection(i);
            return chunkpalettedstoragefix_section == null ? ChunkPalettedStorageFix.AIR : chunkpalettedstoragefix_section.getBlock(i & 4095);
         } else {
            return ChunkPalettedStorageFix.AIR;
         }
      }

      public Dynamic<?> write() {
         Dynamic<?> dynamic = this.level;
         if (this.blockEntities.isEmpty()) {
            dynamic = dynamic.remove("TileEntities");
         } else {
            dynamic = dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
         }

         Dynamic<?> dynamic1 = dynamic.emptyMap();
         List<Dynamic<?>> list = Lists.newArrayList();

         for(ChunkPalettedStorageFix.Section chunkpalettedstoragefix_section : this.sections) {
            if (chunkpalettedstoragefix_section != null) {
               list.add(chunkpalettedstoragefix_section.write());
               dynamic1 = dynamic1.set(String.valueOf(chunkpalettedstoragefix_section.y), dynamic1.createIntList(Arrays.stream(chunkpalettedstoragefix_section.update.toIntArray())));
            }
         }

         Dynamic<?> dynamic2 = dynamic.emptyMap();
         dynamic2 = dynamic2.set("Sides", dynamic2.createByte((byte)this.sides));
         dynamic2 = dynamic2.set("Indices", dynamic1);
         return dynamic.set("UpgradeData", dynamic2).set("Sections", dynamic2.createList(list.stream()));
      }
   }
}
