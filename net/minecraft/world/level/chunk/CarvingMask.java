package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMask {
   private final int minY;
   private final BitSet mask;
   private CarvingMask.Mask additionalMask = (k, l, i1) -> false;

   public CarvingMask(int i, int j) {
      this.minY = j;
      this.mask = new BitSet(256 * i);
   }

   public void setAdditionalMask(CarvingMask.Mask carvingmask_mask) {
      this.additionalMask = carvingmask_mask;
   }

   public CarvingMask(long[] along, int i) {
      this.minY = i;
      this.mask = BitSet.valueOf(along);
   }

   private int getIndex(int i, int j, int k) {
      return i & 15 | (k & 15) << 4 | j - this.minY << 8;
   }

   public void set(int i, int j, int k) {
      this.mask.set(this.getIndex(i, j, k));
   }

   public boolean get(int i, int j, int k) {
      return this.additionalMask.test(i, j, k) || this.mask.get(this.getIndex(i, j, k));
   }

   public Stream<BlockPos> stream(ChunkPos chunkpos) {
      return this.mask.stream().mapToObj((i) -> {
         int j = i & 15;
         int k = i >> 4 & 15;
         int l = i >> 8;
         return chunkpos.getBlockAt(j, l + this.minY, k);
      });
   }

   public long[] toArray() {
      return this.mask.toLongArray();
   }

   public interface Mask {
      boolean test(int i, int j, int k);
   }
}
