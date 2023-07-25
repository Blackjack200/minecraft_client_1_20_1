package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
   protected BlockLightSectionStorage(LightChunkGetter lightchunkgetter) {
      super(LightLayer.BLOCK, lightchunkgetter, new BlockLightSectionStorage.BlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
   }

   protected int getLightValue(long i) {
      long j = SectionPos.blockToSection(i);
      DataLayer datalayer = this.getDataLayer(j, false);
      return datalayer == null ? 0 : datalayer.get(SectionPos.sectionRelative(BlockPos.getX(i)), SectionPos.sectionRelative(BlockPos.getY(i)), SectionPos.sectionRelative(BlockPos.getZ(i)));
   }

   protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
      public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2objectopenhashmap) {
         super(long2objectopenhashmap);
      }

      public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
         return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
      }
   }
}
