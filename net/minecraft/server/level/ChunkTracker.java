package net.minecraft.server.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class ChunkTracker extends DynamicGraphMinFixedPoint {
   protected ChunkTracker(int i, int j, int k) {
      super(i, j, k);
   }

   protected boolean isSource(long i) {
      return i == ChunkPos.INVALID_CHUNK_POS;
   }

   protected void checkNeighborsAfterUpdate(long i, int j, boolean flag) {
      if (!flag || j < this.levelCount - 2) {
         ChunkPos chunkpos = new ChunkPos(i);
         int k = chunkpos.x;
         int l = chunkpos.z;

         for(int i1 = -1; i1 <= 1; ++i1) {
            for(int j1 = -1; j1 <= 1; ++j1) {
               long k1 = ChunkPos.asLong(k + i1, l + j1);
               if (k1 != i) {
                  this.checkNeighbor(i, k1, j, flag);
               }
            }
         }

      }
   }

   protected int getComputedLevel(long i, long j, int k) {
      int l = k;
      ChunkPos chunkpos = new ChunkPos(i);
      int i1 = chunkpos.x;
      int j1 = chunkpos.z;

      for(int k1 = -1; k1 <= 1; ++k1) {
         for(int l1 = -1; l1 <= 1; ++l1) {
            long i2 = ChunkPos.asLong(i1 + k1, j1 + l1);
            if (i2 == i) {
               i2 = ChunkPos.INVALID_CHUNK_POS;
            }

            if (i2 != j) {
               int j2 = this.computeLevelFromNeighbor(i2, i, this.getLevel(i2));
               if (l > j2) {
                  l = j2;
               }

               if (l == 0) {
                  return l;
               }
            }
         }
      }

      return l;
   }

   protected int computeLevelFromNeighbor(long i, long j, int k) {
      return i == ChunkPos.INVALID_CHUNK_POS ? this.getLevelFromSource(j) : k + 1;
   }

   protected abstract int getLevelFromSource(long i);

   public void update(long i, int j, boolean flag) {
      this.checkEdge(ChunkPos.INVALID_CHUNK_POS, i, j, flag);
   }
}
