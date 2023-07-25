package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class LevelLightEngine implements LightEventListener {
   public static final int LIGHT_SECTION_PADDING = 1;
   protected final LevelHeightAccessor levelHeightAccessor;
   @Nullable
   private final LightEngine<?, ?> blockEngine;
   @Nullable
   private final LightEngine<?, ?> skyEngine;

   public LevelLightEngine(LightChunkGetter lightchunkgetter, boolean flag, boolean flag1) {
      this.levelHeightAccessor = lightchunkgetter.getLevel();
      this.blockEngine = flag ? new BlockLightEngine(lightchunkgetter) : null;
      this.skyEngine = flag1 ? new SkyLightEngine(lightchunkgetter) : null;
   }

   public void checkBlock(BlockPos blockpos) {
      if (this.blockEngine != null) {
         this.blockEngine.checkBlock(blockpos);
      }

      if (this.skyEngine != null) {
         this.skyEngine.checkBlock(blockpos);
      }

   }

   public boolean hasLightWork() {
      if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
         return true;
      } else {
         return this.blockEngine != null && this.blockEngine.hasLightWork();
      }
   }

   public int runLightUpdates() {
      int i = 0;
      if (this.blockEngine != null) {
         i += this.blockEngine.runLightUpdates();
      }

      if (this.skyEngine != null) {
         i += this.skyEngine.runLightUpdates();
      }

      return i;
   }

   public void updateSectionStatus(SectionPos sectionpos, boolean flag) {
      if (this.blockEngine != null) {
         this.blockEngine.updateSectionStatus(sectionpos, flag);
      }

      if (this.skyEngine != null) {
         this.skyEngine.updateSectionStatus(sectionpos, flag);
      }

   }

   public void setLightEnabled(ChunkPos chunkpos, boolean flag) {
      if (this.blockEngine != null) {
         this.blockEngine.setLightEnabled(chunkpos, flag);
      }

      if (this.skyEngine != null) {
         this.skyEngine.setLightEnabled(chunkpos, flag);
      }

   }

   public void propagateLightSources(ChunkPos chunkpos) {
      if (this.blockEngine != null) {
         this.blockEngine.propagateLightSources(chunkpos);
      }

      if (this.skyEngine != null) {
         this.skyEngine.propagateLightSources(chunkpos);
      }

   }

   public LayerLightEventListener getLayerListener(LightLayer lightlayer) {
      if (lightlayer == LightLayer.BLOCK) {
         return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
      } else {
         return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
      }
   }

   public String getDebugData(LightLayer lightlayer, SectionPos sectionpos) {
      if (lightlayer == LightLayer.BLOCK) {
         if (this.blockEngine != null) {
            return this.blockEngine.getDebugData(sectionpos.asLong());
         }
      } else if (this.skyEngine != null) {
         return this.skyEngine.getDebugData(sectionpos.asLong());
      }

      return "n/a";
   }

   public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer lightlayer, SectionPos sectionpos) {
      if (lightlayer == LightLayer.BLOCK) {
         if (this.blockEngine != null) {
            return this.blockEngine.getDebugSectionType(sectionpos.asLong());
         }
      } else if (this.skyEngine != null) {
         return this.skyEngine.getDebugSectionType(sectionpos.asLong());
      }

      return LayerLightSectionStorage.SectionType.EMPTY;
   }

   public void queueSectionData(LightLayer lightlayer, SectionPos sectionpos, @Nullable DataLayer datalayer) {
      if (lightlayer == LightLayer.BLOCK) {
         if (this.blockEngine != null) {
            this.blockEngine.queueSectionData(sectionpos.asLong(), datalayer);
         }
      } else if (this.skyEngine != null) {
         this.skyEngine.queueSectionData(sectionpos.asLong(), datalayer);
      }

   }

   public void retainData(ChunkPos chunkpos, boolean flag) {
      if (this.blockEngine != null) {
         this.blockEngine.retainData(chunkpos, flag);
      }

      if (this.skyEngine != null) {
         this.skyEngine.retainData(chunkpos, flag);
      }

   }

   public int getRawBrightness(BlockPos blockpos, int i) {
      int j = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockpos) - i;
      int k = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockpos);
      return Math.max(k, j);
   }

   public boolean lightOnInSection(SectionPos sectionpos) {
      long i = sectionpos.asLong();
      return this.blockEngine == null || this.blockEngine.storage.lightOnInSection(i) && (this.skyEngine == null || this.skyEngine.storage.lightOnInSection(i));
   }

   public int getLightSectionCount() {
      return this.levelHeightAccessor.getSectionsCount() + 2;
   }

   public int getMinLightSection() {
      return this.levelHeightAccessor.getMinSection() - 1;
   }

   public int getMaxLightSection() {
      return this.getMinLightSection() + this.getLightSectionCount();
   }
}
