package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
   protected SkyLightSectionStorage(LightChunkGetter lightchunkgetter) {
      super(LightLayer.SKY, lightchunkgetter, new SkyLightSectionStorage.SkyDataLayerStorageMap(new Long2ObjectOpenHashMap<>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLightValue(long i) {
      return this.getLightValue(i, false);
   }

   protected int getLightValue(long i, boolean flag) {
      long j = SectionPos.blockToSection(i);
      int k = SectionPos.y(j);
      SkyLightSectionStorage.SkyDataLayerStorageMap skylightsectionstorage_skydatalayerstoragemap = flag ? this.updatingSectionData : this.visibleSectionData;
      int l = skylightsectionstorage_skydatalayerstoragemap.topSections.get(SectionPos.getZeroNode(j));
      if (l != skylightsectionstorage_skydatalayerstoragemap.currentLowestY && k < l) {
         DataLayer datalayer = this.getDataLayer(skylightsectionstorage_skydatalayerstoragemap, j);
         if (datalayer == null) {
            for(i = BlockPos.getFlatIndex(i); datalayer == null; datalayer = this.getDataLayer(skylightsectionstorage_skydatalayerstoragemap, j)) {
               ++k;
               if (k >= l) {
                  return 15;
               }

               j = SectionPos.offset(j, Direction.UP);
            }
         }

         return datalayer.get(SectionPos.sectionRelative(BlockPos.getX(i)), SectionPos.sectionRelative(BlockPos.getY(i)), SectionPos.sectionRelative(BlockPos.getZ(i)));
      } else {
         return flag && !this.lightOnInSection(j) ? 0 : 15;
      }
   }

   protected void onNodeAdded(long i) {
      int j = SectionPos.y(i);
      if ((this.updatingSectionData).currentLowestY > j) {
         (this.updatingSectionData).currentLowestY = j;
         (this.updatingSectionData).topSections.defaultReturnValue((this.updatingSectionData).currentLowestY);
      }

      long k = SectionPos.getZeroNode(i);
      int l = (this.updatingSectionData).topSections.get(k);
      if (l < j + 1) {
         (this.updatingSectionData).topSections.put(k, j + 1);
      }

   }

   protected void onNodeRemoved(long i) {
      long j = SectionPos.getZeroNode(i);
      int k = SectionPos.y(i);
      if ((this.updatingSectionData).topSections.get(j) == k + 1) {
         long l;
         for(l = i; !this.storingLightForSection(l) && this.hasLightDataAtOrBelow(k); l = SectionPos.offset(l, Direction.DOWN)) {
            --k;
         }

         if (this.storingLightForSection(l)) {
            (this.updatingSectionData).topSections.put(j, k + 1);
         } else {
            (this.updatingSectionData).topSections.remove(j);
         }
      }

   }

   protected DataLayer createDataLayer(long i) {
      DataLayer datalayer = this.queuedSections.get(i);
      if (datalayer != null) {
         return datalayer;
      } else {
         int j = (this.updatingSectionData).topSections.get(SectionPos.getZeroNode(i));
         if (j != (this.updatingSectionData).currentLowestY && SectionPos.y(i) < j) {
            DataLayer datalayer1;
            for(long k = SectionPos.offset(i, Direction.UP); (datalayer1 = this.getDataLayer(k, true)) == null; k = SectionPos.offset(k, Direction.UP)) {
            }

            return repeatFirstLayer(datalayer1);
         } else {
            return this.lightOnInSection(i) ? new DataLayer(15) : new DataLayer();
         }
      }
   }

   private static DataLayer repeatFirstLayer(DataLayer datalayer) {
      if (datalayer.isDefinitelyHomogenous()) {
         return datalayer.copy();
      } else {
         byte[] abyte = datalayer.getData();
         byte[] abyte1 = new byte[2048];

         for(int i = 0; i < 16; ++i) {
            System.arraycopy(abyte, 0, abyte1, i * 128, 128);
         }

         return new DataLayer(abyte1);
      }
   }

   protected boolean hasLightDataAtOrBelow(int i) {
      return i >= (this.updatingSectionData).currentLowestY;
   }

   protected boolean isAboveData(long i) {
      long j = SectionPos.getZeroNode(i);
      int k = (this.updatingSectionData).topSections.get(j);
      return k == (this.updatingSectionData).currentLowestY || SectionPos.y(i) >= k;
   }

   protected int getTopSectionY(long i) {
      return (this.updatingSectionData).topSections.get(i);
   }

   protected int getBottomSectionY() {
      return (this.updatingSectionData).currentLowestY;
   }

   protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
      int currentLowestY;
      final Long2IntOpenHashMap topSections;

      public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2objectopenhashmap, Long2IntOpenHashMap long2intopenhashmap, int i) {
         super(long2objectopenhashmap);
         this.topSections = long2intopenhashmap;
         long2intopenhashmap.defaultReturnValue(i);
         this.currentLowestY = i;
      }

      public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
         return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
      }
   }
}
