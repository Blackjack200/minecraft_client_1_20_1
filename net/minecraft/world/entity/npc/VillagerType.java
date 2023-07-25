package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
   public static final VillagerType DESERT = register("desert");
   public static final VillagerType JUNGLE = register("jungle");
   public static final VillagerType PLAINS = register("plains");
   public static final VillagerType SAVANNA = register("savanna");
   public static final VillagerType SNOW = register("snow");
   public static final VillagerType SWAMP = register("swamp");
   public static final VillagerType TAIGA = register("taiga");
   private final String name;
   private static final Map<ResourceKey<Biome>, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put(Biomes.BADLANDS, DESERT);
      hashmap.put(Biomes.DESERT, DESERT);
      hashmap.put(Biomes.ERODED_BADLANDS, DESERT);
      hashmap.put(Biomes.WOODED_BADLANDS, DESERT);
      hashmap.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
      hashmap.put(Biomes.JUNGLE, JUNGLE);
      hashmap.put(Biomes.SPARSE_JUNGLE, JUNGLE);
      hashmap.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
      hashmap.put(Biomes.SAVANNA, SAVANNA);
      hashmap.put(Biomes.WINDSWEPT_SAVANNA, SAVANNA);
      hashmap.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
      hashmap.put(Biomes.FROZEN_OCEAN, SNOW);
      hashmap.put(Biomes.FROZEN_RIVER, SNOW);
      hashmap.put(Biomes.ICE_SPIKES, SNOW);
      hashmap.put(Biomes.SNOWY_BEACH, SNOW);
      hashmap.put(Biomes.SNOWY_TAIGA, SNOW);
      hashmap.put(Biomes.SNOWY_PLAINS, SNOW);
      hashmap.put(Biomes.GROVE, SNOW);
      hashmap.put(Biomes.SNOWY_SLOPES, SNOW);
      hashmap.put(Biomes.FROZEN_PEAKS, SNOW);
      hashmap.put(Biomes.JAGGED_PEAKS, SNOW);
      hashmap.put(Biomes.SWAMP, SWAMP);
      hashmap.put(Biomes.MANGROVE_SWAMP, SWAMP);
      hashmap.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
      hashmap.put(Biomes.OLD_GROWTH_PINE_TAIGA, TAIGA);
      hashmap.put(Biomes.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
      hashmap.put(Biomes.WINDSWEPT_HILLS, TAIGA);
      hashmap.put(Biomes.TAIGA, TAIGA);
      hashmap.put(Biomes.WINDSWEPT_FOREST, TAIGA);
   });

   private VillagerType(String s) {
      this.name = s;
   }

   public String toString() {
      return this.name;
   }

   private static VillagerType register(String s) {
      return Registry.register(BuiltInRegistries.VILLAGER_TYPE, new ResourceLocation(s), new VillagerType(s));
   }

   public static VillagerType byBiome(Holder<Biome> holder) {
      return holder.unwrapKey().map(BY_BIOME::get).orElse(PLAINS);
   }
}
