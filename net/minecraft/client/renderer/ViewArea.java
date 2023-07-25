package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class ViewArea {
   protected final LevelRenderer levelRenderer;
   protected final Level level;
   protected int chunkGridSizeY;
   protected int chunkGridSizeX;
   protected int chunkGridSizeZ;
   public ChunkRenderDispatcher.RenderChunk[] chunks;

   public ViewArea(ChunkRenderDispatcher chunkrenderdispatcher, Level level, int i, LevelRenderer levelrenderer) {
      this.levelRenderer = levelrenderer;
      this.level = level;
      this.setViewDistance(i);
      this.createChunks(chunkrenderdispatcher);
   }

   protected void createChunks(ChunkRenderDispatcher chunkrenderdispatcher) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
      } else {
         int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
         this.chunks = new ChunkRenderDispatcher.RenderChunk[i];

         for(int j = 0; j < this.chunkGridSizeX; ++j) {
            for(int k = 0; k < this.chunkGridSizeY; ++k) {
               for(int l = 0; l < this.chunkGridSizeZ; ++l) {
                  int i1 = this.getChunkIndex(j, k, l);
                  this.chunks[i1] = chunkrenderdispatcher.new RenderChunk(i1, j * 16, k * 16, l * 16);
               }
            }
         }

      }
   }

   public void releaseAllBuffers() {
      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk : this.chunks) {
         chunkrenderdispatcher_renderchunk.releaseBuffers();
      }

   }

   private int getChunkIndex(int i, int j, int k) {
      return (k * this.chunkGridSizeY + j) * this.chunkGridSizeX + i;
   }

   protected void setViewDistance(int i) {
      int j = i * 2 + 1;
      this.chunkGridSizeX = j;
      this.chunkGridSizeY = this.level.getSectionsCount();
      this.chunkGridSizeZ = j;
   }

   public void repositionCamera(double d0, double d1) {
      int i = Mth.ceil(d0);
      int j = Mth.ceil(d1);

      for(int k = 0; k < this.chunkGridSizeX; ++k) {
         int l = this.chunkGridSizeX * 16;
         int i1 = i - 8 - l / 2;
         int j1 = i1 + Math.floorMod(k * 16 - i1, l);

         for(int k1 = 0; k1 < this.chunkGridSizeZ; ++k1) {
            int l1 = this.chunkGridSizeZ * 16;
            int i2 = j - 8 - l1 / 2;
            int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

            for(int k2 = 0; k2 < this.chunkGridSizeY; ++k2) {
               int l2 = this.level.getMinBuildHeight() + k2 * 16;
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = this.chunks[this.getChunkIndex(k, k2, k1)];
               BlockPos blockpos = chunkrenderdispatcher_renderchunk.getOrigin();
               if (j1 != blockpos.getX() || l2 != blockpos.getY() || j2 != blockpos.getZ()) {
                  chunkrenderdispatcher_renderchunk.setOrigin(j1, l2, j2);
               }
            }
         }
      }

   }

   public void setDirty(int i, int j, int k, boolean flag) {
      int l = Math.floorMod(i, this.chunkGridSizeX);
      int i1 = Math.floorMod(j - this.level.getMinSection(), this.chunkGridSizeY);
      int j1 = Math.floorMod(k, this.chunkGridSizeZ);
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = this.chunks[this.getChunkIndex(l, i1, j1)];
      chunkrenderdispatcher_renderchunk.setDirty(flag);
   }

   @Nullable
   protected ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos blockpos) {
      int i = Mth.floorDiv(blockpos.getX(), 16);
      int j = Mth.floorDiv(blockpos.getY() - this.level.getMinBuildHeight(), 16);
      int k = Mth.floorDiv(blockpos.getZ(), 16);
      if (j >= 0 && j < this.chunkGridSizeY) {
         i = Mth.positiveModulo(i, this.chunkGridSizeX);
         k = Mth.positiveModulo(k, this.chunkGridSizeZ);
         return this.chunks[this.getChunkIndex(i, j, k)];
      } else {
         return null;
      }
   }
}
