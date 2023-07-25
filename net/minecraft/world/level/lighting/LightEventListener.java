package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener {
   void checkBlock(BlockPos blockpos);

   boolean hasLightWork();

   int runLightUpdates();

   default void updateSectionStatus(BlockPos blockpos, boolean flag) {
      this.updateSectionStatus(SectionPos.of(blockpos), flag);
   }

   void updateSectionStatus(SectionPos sectionpos, boolean flag);

   void setLightEnabled(ChunkPos chunkpos, boolean flag);

   void propagateLightSources(ChunkPos chunkpos);
}
