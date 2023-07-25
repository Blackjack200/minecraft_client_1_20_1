package net.minecraft.world.level;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class ChunkPos {
   private static final int SAFETY_MARGIN = 1056;
   public static final long INVALID_CHUNK_POS = asLong(1875066, 1875066);
   public static final ChunkPos ZERO = new ChunkPos(0, 0);
   private static final long COORD_BITS = 32L;
   private static final long COORD_MASK = 4294967295L;
   private static final int REGION_BITS = 5;
   public static final int REGION_SIZE = 32;
   private static final int REGION_MASK = 31;
   public static final int REGION_MAX_INDEX = 31;
   public final int x;
   public final int z;
   private static final int HASH_A = 1664525;
   private static final int HASH_C = 1013904223;
   private static final int HASH_Z_XOR = -559038737;

   public ChunkPos(int i, int j) {
      this.x = i;
      this.z = j;
   }

   public ChunkPos(BlockPos blockpos) {
      this.x = SectionPos.blockToSectionCoord(blockpos.getX());
      this.z = SectionPos.blockToSectionCoord(blockpos.getZ());
   }

   public ChunkPos(long i) {
      this.x = (int)i;
      this.z = (int)(i >> 32);
   }

   public static ChunkPos minFromRegion(int i, int j) {
      return new ChunkPos(i << 5, j << 5);
   }

   public static ChunkPos maxFromRegion(int i, int j) {
      return new ChunkPos((i << 5) + 31, (j << 5) + 31);
   }

   public long toLong() {
      return asLong(this.x, this.z);
   }

   public static long asLong(int i, int j) {
      return (long)i & 4294967295L | ((long)j & 4294967295L) << 32;
   }

   public static long asLong(BlockPos blockpos) {
      return asLong(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
   }

   public static int getX(long i) {
      return (int)(i & 4294967295L);
   }

   public static int getZ(long i) {
      return (int)(i >>> 32 & 4294967295L);
   }

   public int hashCode() {
      return hash(this.x, this.z);
   }

   public static int hash(int i, int j) {
      int k = 1664525 * i + 1013904223;
      int l = 1664525 * (j ^ -559038737) + 1013904223;
      return k ^ l;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof ChunkPos)) {
         return false;
      } else {
         ChunkPos chunkpos = (ChunkPos)object;
         return this.x == chunkpos.x && this.z == chunkpos.z;
      }
   }

   public int getMiddleBlockX() {
      return this.getBlockX(8);
   }

   public int getMiddleBlockZ() {
      return this.getBlockZ(8);
   }

   public int getMinBlockX() {
      return SectionPos.sectionToBlockCoord(this.x);
   }

   public int getMinBlockZ() {
      return SectionPos.sectionToBlockCoord(this.z);
   }

   public int getMaxBlockX() {
      return this.getBlockX(15);
   }

   public int getMaxBlockZ() {
      return this.getBlockZ(15);
   }

   public int getRegionX() {
      return this.x >> 5;
   }

   public int getRegionZ() {
      return this.z >> 5;
   }

   public int getRegionLocalX() {
      return this.x & 31;
   }

   public int getRegionLocalZ() {
      return this.z & 31;
   }

   public BlockPos getBlockAt(int i, int j, int k) {
      return new BlockPos(this.getBlockX(i), j, this.getBlockZ(k));
   }

   public int getBlockX(int i) {
      return SectionPos.sectionToBlockCoord(this.x, i);
   }

   public int getBlockZ(int i) {
      return SectionPos.sectionToBlockCoord(this.z, i);
   }

   public BlockPos getMiddleBlockPosition(int i) {
      return new BlockPos(this.getMiddleBlockX(), i, this.getMiddleBlockZ());
   }

   public String toString() {
      return "[" + this.x + ", " + this.z + "]";
   }

   public BlockPos getWorldPosition() {
      return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
   }

   public int getChessboardDistance(ChunkPos chunkpos) {
      return Math.max(Math.abs(this.x - chunkpos.x), Math.abs(this.z - chunkpos.z));
   }

   public static Stream<ChunkPos> rangeClosed(ChunkPos chunkpos, int i) {
      return rangeClosed(new ChunkPos(chunkpos.x - i, chunkpos.z - i), new ChunkPos(chunkpos.x + i, chunkpos.z + i));
   }

   public static Stream<ChunkPos> rangeClosed(final ChunkPos chunkpos, final ChunkPos chunkpos1) {
      int i = Math.abs(chunkpos.x - chunkpos1.x) + 1;
      int j = Math.abs(chunkpos.z - chunkpos1.z) + 1;
      final int k = chunkpos.x < chunkpos1.x ? 1 : -1;
      final int l = chunkpos.z < chunkpos1.z ? 1 : -1;
      return StreamSupport.stream(new Spliterators.AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
         @Nullable
         private ChunkPos pos;

         public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
            if (this.pos == null) {
               this.pos = chunkpos;
            } else {
               int i = this.pos.x;
               int j = this.pos.z;
               if (i == chunkpos1.x) {
                  if (j == chunkpos1.z) {
                     return false;
                  }

                  this.pos = new ChunkPos(chunkpos.x, j + l);
               } else {
                  this.pos = new ChunkPos(i + k, j);
               }
            }

            consumer.accept(this.pos);
            return true;
         }
      }, false);
   }
}
