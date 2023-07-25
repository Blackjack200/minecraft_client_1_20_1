package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class RenderRegionCache {
   private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

   @Nullable
   public RenderChunkRegion createRegion(Level level, BlockPos blockpos, BlockPos blockpos1, int i) {
      int j = SectionPos.blockToSectionCoord(blockpos.getX() - i);
      int k = SectionPos.blockToSectionCoord(blockpos.getZ() - i);
      int l = SectionPos.blockToSectionCoord(blockpos1.getX() + i);
      int i1 = SectionPos.blockToSectionCoord(blockpos1.getZ() + i);
      RenderRegionCache.ChunkInfo[][] arenderregioncache_chunkinfo = new RenderRegionCache.ChunkInfo[l - j + 1][i1 - k + 1];

      for(int j1 = j; j1 <= l; ++j1) {
         for(int k1 = k; k1 <= i1; ++k1) {
            arenderregioncache_chunkinfo[j1 - j][k1 - k] = this.chunkInfoCache.computeIfAbsent(ChunkPos.asLong(j1, k1), (j2) -> new RenderRegionCache.ChunkInfo(level.getChunk(ChunkPos.getX(j2), ChunkPos.getZ(j2))));
         }
      }

      if (isAllEmpty(blockpos, blockpos1, j, k, arenderregioncache_chunkinfo)) {
         return null;
      } else {
         RenderChunk[][] arenderchunk = new RenderChunk[l - j + 1][i1 - k + 1];

         for(int l1 = j; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= i1; ++i2) {
               arenderchunk[l1 - j][i2 - k] = arenderregioncache_chunkinfo[l1 - j][i2 - k].renderChunk();
            }
         }

         return new RenderChunkRegion(level, j, k, arenderchunk);
      }
   }

   private static boolean isAllEmpty(BlockPos blockpos, BlockPos blockpos1, int i, int j, RenderRegionCache.ChunkInfo[][] arenderregioncache_chunkinfo) {
      int k = SectionPos.blockToSectionCoord(blockpos.getX());
      int l = SectionPos.blockToSectionCoord(blockpos.getZ());
      int i1 = SectionPos.blockToSectionCoord(blockpos1.getX());
      int j1 = SectionPos.blockToSectionCoord(blockpos1.getZ());

      for(int k1 = k; k1 <= i1; ++k1) {
         for(int l1 = l; l1 <= j1; ++l1) {
            LevelChunk levelchunk = arenderregioncache_chunkinfo[k1 - i][l1 - j].chunk();
            if (!levelchunk.isYSpaceEmpty(blockpos.getY(), blockpos1.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   static final class ChunkInfo {
      private final LevelChunk chunk;
      @Nullable
      private RenderChunk renderChunk;

      ChunkInfo(LevelChunk levelchunk) {
         this.chunk = levelchunk;
      }

      public LevelChunk chunk() {
         return this.chunk;
      }

      public RenderChunk renderChunk() {
         if (this.renderChunk == null) {
            this.renderChunk = new RenderChunk(this.chunk);
         }

         return this.renderChunk;
      }
   }
}
