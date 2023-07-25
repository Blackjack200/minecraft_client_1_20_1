package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
   public static final String FILE_NAME = "idcounts";
   private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

   public MapIndex() {
      this.usedAuxIds.defaultReturnValue(-1);
   }

   public static MapIndex load(CompoundTag compoundtag) {
      MapIndex mapindex = new MapIndex();

      for(String s : compoundtag.getAllKeys()) {
         if (compoundtag.contains(s, 99)) {
            mapindex.usedAuxIds.put(s, compoundtag.getInt(s));
         }
      }

      return mapindex;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      for(Object2IntMap.Entry<String> object2intmap_entry : this.usedAuxIds.object2IntEntrySet()) {
         compoundtag.putInt(object2intmap_entry.getKey(), object2intmap_entry.getIntValue());
      }

      return compoundtag;
   }

   public int getFreeAuxValueForMap() {
      int i = this.usedAuxIds.getInt("map") + 1;
      this.usedAuxIds.put("map", i);
      this.setDirty();
      return i;
   }
}
