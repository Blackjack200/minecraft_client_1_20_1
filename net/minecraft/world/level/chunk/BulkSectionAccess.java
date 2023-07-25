package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BulkSectionAccess implements AutoCloseable {
   private final LevelAccessor level;
   private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap<>();
   @Nullable
   private LevelChunkSection lastSection;
   private long lastSectionKey;

   public BulkSectionAccess(LevelAccessor levelaccessor) {
      this.level = levelaccessor;
   }

   @Nullable
   public LevelChunkSection getSection(BlockPos blockpos) {
      int i = this.level.getSectionIndex(blockpos.getY());
      if (i >= 0 && i < this.level.getSectionsCount()) {
         long j = SectionPos.asLong(blockpos);
         if (this.lastSection == null || this.lastSectionKey != j) {
            this.lastSection = this.acquiredSections.computeIfAbsent(j, (l) -> {
               ChunkAccess chunkaccess = this.level.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
               LevelChunkSection levelchunksection = chunkaccess.getSection(i);
               levelchunksection.acquire();
               return levelchunksection;
            });
            this.lastSectionKey = j;
         }

         return this.lastSection;
      } else {
         return null;
      }
   }

   public BlockState getBlockState(BlockPos blockpos) {
      LevelChunkSection levelchunksection = this.getSection(blockpos);
      if (levelchunksection == null) {
         return Blocks.AIR.defaultBlockState();
      } else {
         int i = SectionPos.sectionRelative(blockpos.getX());
         int j = SectionPos.sectionRelative(blockpos.getY());
         int k = SectionPos.sectionRelative(blockpos.getZ());
         return levelchunksection.getBlockState(i, j, k);
      }
   }

   public void close() {
      for(LevelChunkSection levelchunksection : this.acquiredSections.values()) {
         levelchunksection.release();
      }

   }
}
