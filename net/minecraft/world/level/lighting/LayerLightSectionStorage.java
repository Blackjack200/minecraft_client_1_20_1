package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> {
   private final LightLayer layer;
   protected final LightChunkGetter chunkSource;
   protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
   private final LongSet columnsWithSources = new LongOpenHashSet();
   protected volatile M visibleSectionData;
   protected final M updatingSectionData;
   protected final LongSet changedSections = new LongOpenHashSet();
   protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
   protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
   private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
   private final LongSet toRemove = new LongOpenHashSet();
   protected volatile boolean hasInconsistencies;

   protected LayerLightSectionStorage(LightLayer lightlayer, LightChunkGetter lightchunkgetter, M datalayerstoragemap) {
      this.layer = lightlayer;
      this.chunkSource = lightchunkgetter;
      this.updatingSectionData = datalayerstoragemap;
      this.visibleSectionData = datalayerstoragemap.copy();
      this.visibleSectionData.disableCache();
      this.sectionStates.defaultReturnValue((byte)0);
   }

   protected boolean storingLightForSection(long i) {
      return this.getDataLayer(i, true) != null;
   }

   @Nullable
   protected DataLayer getDataLayer(long i, boolean flag) {
      return this.getDataLayer((M)(flag ? this.updatingSectionData : this.visibleSectionData), i);
   }

   @Nullable
   protected DataLayer getDataLayer(M datalayerstoragemap, long i) {
      return datalayerstoragemap.getLayer(i);
   }

   @Nullable
   protected DataLayer getDataLayerToWrite(long i) {
      DataLayer datalayer = this.updatingSectionData.getLayer(i);
      if (datalayer == null) {
         return null;
      } else {
         if (this.changedSections.add(i)) {
            datalayer = datalayer.copy();
            this.updatingSectionData.setLayer(i, datalayer);
            this.updatingSectionData.clearCache();
         }

         return datalayer;
      }
   }

   @Nullable
   public DataLayer getDataLayerData(long i) {
      DataLayer datalayer = this.queuedSections.get(i);
      return datalayer != null ? datalayer : this.getDataLayer(i, false);
   }

   protected abstract int getLightValue(long i);

   protected int getStoredLevel(long i) {
      long j = SectionPos.blockToSection(i);
      DataLayer datalayer = this.getDataLayer(j, true);
      return datalayer.get(SectionPos.sectionRelative(BlockPos.getX(i)), SectionPos.sectionRelative(BlockPos.getY(i)), SectionPos.sectionRelative(BlockPos.getZ(i)));
   }

   protected void setStoredLevel(long i, int j) {
      long k = SectionPos.blockToSection(i);
      DataLayer datalayer;
      if (this.changedSections.add(k)) {
         datalayer = this.updatingSectionData.copyDataLayer(k);
      } else {
         datalayer = this.getDataLayer(k, true);
      }

      datalayer.set(SectionPos.sectionRelative(BlockPos.getX(i)), SectionPos.sectionRelative(BlockPos.getY(i)), SectionPos.sectionRelative(BlockPos.getZ(i)), j);
      SectionPos.aroundAndAtBlockPos(i, this.sectionsAffectedByLightUpdates::add);
   }

   protected void markSectionAndNeighborsAsAffected(long i) {
      int j = SectionPos.x(i);
      int k = SectionPos.y(i);
      int l = SectionPos.z(i);

      for(int i1 = -1; i1 <= 1; ++i1) {
         for(int j1 = -1; j1 <= 1; ++j1) {
            for(int k1 = -1; k1 <= 1; ++k1) {
               this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(j + j1, k + k1, l + i1));
            }
         }
      }

   }

   protected DataLayer createDataLayer(long i) {
      DataLayer datalayer = this.queuedSections.get(i);
      return datalayer != null ? datalayer : new DataLayer();
   }

   protected boolean hasInconsistencies() {
      return this.hasInconsistencies;
   }

   protected void markNewInconsistencies(LightEngine<M, ?> lightengine) {
      if (this.hasInconsistencies) {
         this.hasInconsistencies = false;

         for(long i : this.toRemove) {
            DataLayer datalayer = this.queuedSections.remove(i);
            DataLayer datalayer1 = this.updatingSectionData.removeLayer(i);
            if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(i))) {
               if (datalayer != null) {
                  this.queuedSections.put(i, datalayer);
               } else if (datalayer1 != null) {
                  this.queuedSections.put(i, datalayer1);
               }
            }
         }

         this.updatingSectionData.clearCache();

         for(long j : this.toRemove) {
            this.onNodeRemoved(j);
            this.changedSections.add(j);
         }

         this.toRemove.clear();
         ObjectIterator<Long2ObjectMap.Entry<DataLayer>> objectiterator = Long2ObjectMaps.fastIterator(this.queuedSections);

         while(objectiterator.hasNext()) {
            Long2ObjectMap.Entry<DataLayer> long2objectmap_entry = objectiterator.next();
            long k = long2objectmap_entry.getLongKey();
            if (this.storingLightForSection(k)) {
               DataLayer datalayer2 = long2objectmap_entry.getValue();
               if (this.updatingSectionData.getLayer(k) != datalayer2) {
                  this.updatingSectionData.setLayer(k, datalayer2);
                  this.changedSections.add(k);
               }

               objectiterator.remove();
            }
         }

         this.updatingSectionData.clearCache();
      }
   }

   protected void onNodeAdded(long i) {
   }

   protected void onNodeRemoved(long i) {
   }

   protected void setLightEnabled(long i, boolean flag) {
      if (flag) {
         this.columnsWithSources.add(i);
      } else {
         this.columnsWithSources.remove(i);
      }

   }

   protected boolean lightOnInSection(long i) {
      long j = SectionPos.getZeroNode(i);
      return this.columnsWithSources.contains(j);
   }

   public void retainData(long i, boolean flag) {
      if (flag) {
         this.columnsToRetainQueuedDataFor.add(i);
      } else {
         this.columnsToRetainQueuedDataFor.remove(i);
      }

   }

   protected void queueSectionData(long i, @Nullable DataLayer datalayer) {
      if (datalayer != null) {
         this.queuedSections.put(i, datalayer);
         this.hasInconsistencies = true;
      } else {
         this.queuedSections.remove(i);
      }

   }

   protected void updateSectionStatus(long i, boolean flag) {
      byte b0 = this.sectionStates.get(i);
      byte b1 = LayerLightSectionStorage.SectionState.hasData(b0, !flag);
      if (b0 != b1) {
         this.putSectionState(i, b1);
         int j = flag ? -1 : 1;

         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               for(int i1 = -1; i1 <= 1; ++i1) {
                  if (k != 0 || l != 0 || i1 != 0) {
                     long j1 = SectionPos.offset(i, k, l, i1);
                     byte b2 = this.sectionStates.get(j1);
                     this.putSectionState(j1, LayerLightSectionStorage.SectionState.neighborCount(b2, LayerLightSectionStorage.SectionState.neighborCount(b2) + j));
                  }
               }
            }
         }

      }
   }

   protected void putSectionState(long i, byte b0) {
      if (b0 != 0) {
         if (this.sectionStates.put(i, b0) == 0) {
            this.initializeSection(i);
         }
      } else if (this.sectionStates.remove(i) != 0) {
         this.removeSection(i);
      }

   }

   private void initializeSection(long i) {
      if (!this.toRemove.remove(i)) {
         this.updatingSectionData.setLayer(i, this.createDataLayer(i));
         this.changedSections.add(i);
         this.onNodeAdded(i);
         this.markSectionAndNeighborsAsAffected(i);
         this.hasInconsistencies = true;
      }

   }

   private void removeSection(long i) {
      this.toRemove.add(i);
      this.hasInconsistencies = true;
   }

   protected void swapSectionMap() {
      if (!this.changedSections.isEmpty()) {
         M datalayerstoragemap = this.updatingSectionData.copy();
         datalayerstoragemap.disableCache();
         this.visibleSectionData = datalayerstoragemap;
         this.changedSections.clear();
      }

      if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
         LongIterator longiterator = this.sectionsAffectedByLightUpdates.iterator();

         while(longiterator.hasNext()) {
            long i = longiterator.nextLong();
            this.chunkSource.onLightUpdate(this.layer, SectionPos.of(i));
         }

         this.sectionsAffectedByLightUpdates.clear();
      }

   }

   public LayerLightSectionStorage.SectionType getDebugSectionType(long i) {
      return LayerLightSectionStorage.SectionState.type(this.sectionStates.get(i));
   }

   protected static class SectionState {
      public static final byte EMPTY = 0;
      private static final int MIN_NEIGHBORS = 0;
      private static final int MAX_NEIGHBORS = 26;
      private static final byte HAS_DATA_BIT = 32;
      private static final byte NEIGHBOR_COUNT_BITS = 31;

      public static byte hasData(byte b0, boolean flag) {
         return (byte)(flag ? b0 | 32 : b0 & -33);
      }

      public static byte neighborCount(byte b0, int i) {
         if (i >= 0 && i <= 26) {
            return (byte)(b0 & -32 | i & 31);
         } else {
            throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
         }
      }

      public static boolean hasData(byte b0) {
         return (b0 & 32) != 0;
      }

      public static int neighborCount(byte b0) {
         return b0 & 31;
      }

      public static LayerLightSectionStorage.SectionType type(byte b0) {
         if (b0 == 0) {
            return LayerLightSectionStorage.SectionType.EMPTY;
         } else {
            return hasData(b0) ? LayerLightSectionStorage.SectionType.LIGHT_AND_DATA : LayerLightSectionStorage.SectionType.LIGHT_ONLY;
         }
      }
   }

   public static enum SectionType {
      EMPTY("2"),
      LIGHT_ONLY("1"),
      LIGHT_AND_DATA("0");

      private final String display;

      private SectionType(String s) {
         this.display = s;
      }

      public String display() {
         return this.display;
      }
   }
}
