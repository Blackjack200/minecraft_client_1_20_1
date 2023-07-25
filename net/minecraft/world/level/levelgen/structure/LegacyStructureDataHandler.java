package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler {
   private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("Village", "Village");
      hashmap.put("Mineshaft", "Mineshaft");
      hashmap.put("Mansion", "Mansion");
      hashmap.put("Igloo", "Temple");
      hashmap.put("Desert_Pyramid", "Temple");
      hashmap.put("Jungle_Pyramid", "Temple");
      hashmap.put("Swamp_Hut", "Temple");
      hashmap.put("Stronghold", "Stronghold");
      hashmap.put("Monument", "Monument");
      hashmap.put("Fortress", "Fortress");
      hashmap.put("EndCity", "EndCity");
   });
   private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("Iglu", "Igloo");
      hashmap.put("TeDP", "Desert_Pyramid");
      hashmap.put("TeJP", "Jungle_Pyramid");
      hashmap.put("TeSH", "Swamp_Hut");
   });
   private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
   private final boolean hasLegacyData;
   private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
   private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
   private final List<String> legacyKeys;
   private final List<String> currentKeys;

   public LegacyStructureDataHandler(@Nullable DimensionDataStorage dimensiondatastorage, List<String> list, List<String> list1) {
      this.legacyKeys = list;
      this.currentKeys = list1;
      this.populateCaches(dimensiondatastorage);
      boolean flag = false;

      for(String s : this.currentKeys) {
         flag |= this.dataMap.get(s) != null;
      }

      this.hasLegacyData = flag;
   }

   public void removeIndex(long i) {
      for(String s : this.legacyKeys) {
         StructureFeatureIndexSavedData structurefeatureindexsaveddata = this.indexMap.get(s);
         if (structurefeatureindexsaveddata != null && structurefeatureindexsaveddata.hasUnhandledIndex(i)) {
            structurefeatureindexsaveddata.removeIndex(i);
            structurefeatureindexsaveddata.setDirty();
         }
      }

   }

   public CompoundTag updateFromLegacy(CompoundTag compoundtag) {
      CompoundTag compoundtag1 = compoundtag.getCompound("Level");
      ChunkPos chunkpos = new ChunkPos(compoundtag1.getInt("xPos"), compoundtag1.getInt("zPos"));
      if (this.isUnhandledStructureStart(chunkpos.x, chunkpos.z)) {
         compoundtag = this.updateStructureStart(compoundtag, chunkpos);
      }

      CompoundTag compoundtag2 = compoundtag1.getCompound("Structures");
      CompoundTag compoundtag3 = compoundtag2.getCompound("References");

      for(String s : this.currentKeys) {
         boolean flag = OLD_STRUCTURE_REGISTRY_KEYS.contains(s.toLowerCase(Locale.ROOT));
         if (!compoundtag3.contains(s, 12) && flag) {
            int i = 8;
            LongList longlist = new LongArrayList();

            for(int j = chunkpos.x - 8; j <= chunkpos.x + 8; ++j) {
               for(int k = chunkpos.z - 8; k <= chunkpos.z + 8; ++k) {
                  if (this.hasLegacyStart(j, k, s)) {
                     longlist.add(ChunkPos.asLong(j, k));
                  }
               }
            }

            compoundtag3.putLongArray(s, (List<Long>)longlist);
         }
      }

      compoundtag2.put("References", compoundtag3);
      compoundtag1.put("Structures", compoundtag2);
      compoundtag.put("Level", compoundtag1);
      return compoundtag;
   }

   private boolean hasLegacyStart(int i, int j, String s) {
      if (!this.hasLegacyData) {
         return false;
      } else {
         return this.dataMap.get(s) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(s)).hasStartIndex(ChunkPos.asLong(i, j));
      }
   }

   private boolean isUnhandledStructureStart(int i, int j) {
      if (!this.hasLegacyData) {
         return false;
      } else {
         for(String s : this.currentKeys) {
            if (this.dataMap.get(s) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(s)).hasUnhandledIndex(ChunkPos.asLong(i, j))) {
               return true;
            }
         }

         return false;
      }
   }

   private CompoundTag updateStructureStart(CompoundTag compoundtag, ChunkPos chunkpos) {
      CompoundTag compoundtag1 = compoundtag.getCompound("Level");
      CompoundTag compoundtag2 = compoundtag1.getCompound("Structures");
      CompoundTag compoundtag3 = compoundtag2.getCompound("Starts");

      for(String s : this.currentKeys) {
         Long2ObjectMap<CompoundTag> long2objectmap = this.dataMap.get(s);
         if (long2objectmap != null) {
            long i = chunkpos.toLong();
            if (this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(s)).hasUnhandledIndex(i)) {
               CompoundTag compoundtag4 = long2objectmap.get(i);
               if (compoundtag4 != null) {
                  compoundtag3.put(s, compoundtag4);
               }
            }
         }
      }

      compoundtag2.put("Starts", compoundtag3);
      compoundtag1.put("Structures", compoundtag2);
      compoundtag.put("Level", compoundtag1);
      return compoundtag;
   }

   private void populateCaches(@Nullable DimensionDataStorage dimensiondatastorage) {
      if (dimensiondatastorage != null) {
         for(String s : this.legacyKeys) {
            CompoundTag compoundtag = new CompoundTag();

            try {
               compoundtag = dimensiondatastorage.readTagFromDisk(s, 1493).getCompound("data").getCompound("Features");
               if (compoundtag.isEmpty()) {
                  continue;
               }
            } catch (IOException var13) {
            }

            for(String s1 : compoundtag.getAllKeys()) {
               CompoundTag compoundtag1 = compoundtag.getCompound(s1);
               long i = ChunkPos.asLong(compoundtag1.getInt("ChunkX"), compoundtag1.getInt("ChunkZ"));
               ListTag listtag = compoundtag1.getList("Children", 10);
               if (!listtag.isEmpty()) {
                  String s2 = listtag.getCompound(0).getString("id");
                  String s3 = LEGACY_TO_CURRENT_MAP.get(s2);
                  if (s3 != null) {
                     compoundtag1.putString("id", s3);
                  }
               }

               String s4 = compoundtag1.getString("id");
               this.dataMap.computeIfAbsent(s4, (s7) -> new Long2ObjectOpenHashMap()).put(i, compoundtag1);
            }

            String s5 = s + "_index";
            StructureFeatureIndexSavedData structurefeatureindexsaveddata = dimensiondatastorage.computeIfAbsent(StructureFeatureIndexSavedData::load, StructureFeatureIndexSavedData::new, s5);
            if (!structurefeatureindexsaveddata.getAll().isEmpty()) {
               this.indexMap.put(s, structurefeatureindexsaveddata);
            } else {
               StructureFeatureIndexSavedData structurefeatureindexsaveddata1 = new StructureFeatureIndexSavedData();
               this.indexMap.put(s, structurefeatureindexsaveddata1);

               for(String s6 : compoundtag.getAllKeys()) {
                  CompoundTag compoundtag2 = compoundtag.getCompound(s6);
                  structurefeatureindexsaveddata1.addIndex(ChunkPos.asLong(compoundtag2.getInt("ChunkX"), compoundtag2.getInt("ChunkZ")));
               }

               structurefeatureindexsaveddata1.setDirty();
            }
         }

      }
   }

   public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourcekey, @Nullable DimensionDataStorage dimensiondatastorage) {
      if (resourcekey == Level.OVERWORLD) {
         return new LegacyStructureDataHandler(dimensiondatastorage, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
      } else if (resourcekey == Level.NETHER) {
         List<String> list = ImmutableList.of("Fortress");
         return new LegacyStructureDataHandler(dimensiondatastorage, list, list);
      } else if (resourcekey == Level.END) {
         List<String> list1 = ImmutableList.of("EndCity");
         return new LegacyStructureDataHandler(dimensiondatastorage, list1, list1);
      } else {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", resourcekey));
      }
   }
}
