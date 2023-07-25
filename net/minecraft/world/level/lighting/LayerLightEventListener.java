package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.DataLayer;

public interface LayerLightEventListener extends LightEventListener {
   @Nullable
   DataLayer getDataLayerData(SectionPos sectionpos);

   int getLightValue(BlockPos blockpos);

   public static enum DummyLightLayerEventListener implements LayerLightEventListener {
      INSTANCE;

      @Nullable
      public DataLayer getDataLayerData(SectionPos sectionpos) {
         return null;
      }

      public int getLightValue(BlockPos blockpos) {
         return 0;
      }

      public void checkBlock(BlockPos blockpos) {
      }

      public boolean hasLightWork() {
         return false;
      }

      public int runLightUpdates() {
         return 0;
      }

      public void updateSectionStatus(SectionPos sectionpos, boolean flag) {
      }

      public void setLightEnabled(ChunkPos chunkpos, boolean flag) {
      }

      public void propagateLightSources(ChunkPos chunkpos) {
      }
   }
}
