package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.DataLayer;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
   private static final int CACHE_SIZE = 2;
   private final long[] lastSectionKeys = new long[2];
   private final DataLayer[] lastSections = new DataLayer[2];
   private boolean cacheEnabled;
   protected final Long2ObjectOpenHashMap<DataLayer> map;

   protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2objectopenhashmap) {
      this.map = long2objectopenhashmap;
      this.clearCache();
      this.cacheEnabled = true;
   }

   public abstract M copy();

   public DataLayer copyDataLayer(long i) {
      DataLayer datalayer = this.map.get(i).copy();
      this.map.put(i, datalayer);
      this.clearCache();
      return datalayer;
   }

   public boolean hasLayer(long i) {
      return this.map.containsKey(i);
   }

   @Nullable
   public DataLayer getLayer(long i) {
      if (this.cacheEnabled) {
         for(int j = 0; j < 2; ++j) {
            if (i == this.lastSectionKeys[j]) {
               return this.lastSections[j];
            }
         }
      }

      DataLayer datalayer = this.map.get(i);
      if (datalayer == null) {
         return null;
      } else {
         if (this.cacheEnabled) {
            for(int k = 1; k > 0; --k) {
               this.lastSectionKeys[k] = this.lastSectionKeys[k - 1];
               this.lastSections[k] = this.lastSections[k - 1];
            }

            this.lastSectionKeys[0] = i;
            this.lastSections[0] = datalayer;
         }

         return datalayer;
      }
   }

   @Nullable
   public DataLayer removeLayer(long i) {
      return this.map.remove(i);
   }

   public void setLayer(long i, DataLayer datalayer) {
      this.map.put(i, datalayer);
   }

   public void clearCache() {
      for(int i = 0; i < 2; ++i) {
         this.lastSectionKeys[i] = Long.MAX_VALUE;
         this.lastSections[i] = null;
      }

   }

   public void disableCache() {
      this.cacheEnabled = false;
   }
}
