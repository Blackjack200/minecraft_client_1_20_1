package net.minecraft.core;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntityAccess;

public class SectionPos extends Vec3i {
   public static final int SECTION_BITS = 4;
   public static final int SECTION_SIZE = 16;
   public static final int SECTION_MASK = 15;
   public static final int SECTION_HALF_SIZE = 8;
   public static final int SECTION_MAX_INDEX = 15;
   private static final int PACKED_X_LENGTH = 22;
   private static final int PACKED_Y_LENGTH = 20;
   private static final int PACKED_Z_LENGTH = 22;
   private static final long PACKED_X_MASK = 4194303L;
   private static final long PACKED_Y_MASK = 1048575L;
   private static final long PACKED_Z_MASK = 4194303L;
   private static final int Y_OFFSET = 0;
   private static final int Z_OFFSET = 20;
   private static final int X_OFFSET = 42;
   private static final int RELATIVE_X_SHIFT = 8;
   private static final int RELATIVE_Y_SHIFT = 0;
   private static final int RELATIVE_Z_SHIFT = 4;

   SectionPos(int i, int j, int k) {
      super(i, j, k);
   }

   public static SectionPos of(int i, int j, int k) {
      return new SectionPos(i, j, k);
   }

   public static SectionPos of(BlockPos blockpos) {
      return new SectionPos(blockToSectionCoord(blockpos.getX()), blockToSectionCoord(blockpos.getY()), blockToSectionCoord(blockpos.getZ()));
   }

   public static SectionPos of(ChunkPos chunkpos, int i) {
      return new SectionPos(chunkpos.x, i, chunkpos.z);
   }

   public static SectionPos of(EntityAccess entityaccess) {
      return of(entityaccess.blockPosition());
   }

   public static SectionPos of(Position position) {
      return new SectionPos(blockToSectionCoord(position.x()), blockToSectionCoord(position.y()), blockToSectionCoord(position.z()));
   }

   public static SectionPos of(long i) {
      return new SectionPos(x(i), y(i), z(i));
   }

   public static SectionPos bottomOf(ChunkAccess chunkaccess) {
      return of(chunkaccess.getPos(), chunkaccess.getMinSection());
   }

   public static long offset(long i, Direction direction) {
      return offset(i, direction.getStepX(), direction.getStepY(), direction.getStepZ());
   }

   public static long offset(long i, int j, int k, int l) {
      return asLong(x(i) + j, y(i) + k, z(i) + l);
   }

   public static int posToSectionCoord(double d0) {
      return blockToSectionCoord(Mth.floor(d0));
   }

   public static int blockToSectionCoord(int i) {
      return i >> 4;
   }

   public static int blockToSectionCoord(double d0) {
      return Mth.floor(d0) >> 4;
   }

   public static int sectionRelative(int i) {
      return i & 15;
   }

   public static short sectionRelativePos(BlockPos blockpos) {
      int i = sectionRelative(blockpos.getX());
      int j = sectionRelative(blockpos.getY());
      int k = sectionRelative(blockpos.getZ());
      return (short)(i << 8 | k << 4 | j << 0);
   }

   public static int sectionRelativeX(short short0) {
      return short0 >>> 8 & 15;
   }

   public static int sectionRelativeY(short short0) {
      return short0 >>> 0 & 15;
   }

   public static int sectionRelativeZ(short short0) {
      return short0 >>> 4 & 15;
   }

   public int relativeToBlockX(short short0) {
      return this.minBlockX() + sectionRelativeX(short0);
   }

   public int relativeToBlockY(short short0) {
      return this.minBlockY() + sectionRelativeY(short0);
   }

   public int relativeToBlockZ(short short0) {
      return this.minBlockZ() + sectionRelativeZ(short0);
   }

   public BlockPos relativeToBlockPos(short short0) {
      return new BlockPos(this.relativeToBlockX(short0), this.relativeToBlockY(short0), this.relativeToBlockZ(short0));
   }

   public static int sectionToBlockCoord(int i) {
      return i << 4;
   }

   public static int sectionToBlockCoord(int i, int j) {
      return sectionToBlockCoord(i) + j;
   }

   public static int x(long i) {
      return (int)(i << 0 >> 42);
   }

   public static int y(long i) {
      return (int)(i << 44 >> 44);
   }

   public static int z(long i) {
      return (int)(i << 22 >> 42);
   }

   public int x() {
      return this.getX();
   }

   public int y() {
      return this.getY();
   }

   public int z() {
      return this.getZ();
   }

   public int minBlockX() {
      return sectionToBlockCoord(this.x());
   }

   public int minBlockY() {
      return sectionToBlockCoord(this.y());
   }

   public int minBlockZ() {
      return sectionToBlockCoord(this.z());
   }

   public int maxBlockX() {
      return sectionToBlockCoord(this.x(), 15);
   }

   public int maxBlockY() {
      return sectionToBlockCoord(this.y(), 15);
   }

   public int maxBlockZ() {
      return sectionToBlockCoord(this.z(), 15);
   }

   public static long blockToSection(long i) {
      return asLong(blockToSectionCoord(BlockPos.getX(i)), blockToSectionCoord(BlockPos.getY(i)), blockToSectionCoord(BlockPos.getZ(i)));
   }

   public static long getZeroNode(int i, int j) {
      return getZeroNode(asLong(i, 0, j));
   }

   public static long getZeroNode(long i) {
      return i & -1048576L;
   }

   public BlockPos origin() {
      return new BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
   }

   public BlockPos center() {
      int i = 8;
      return this.origin().offset(8, 8, 8);
   }

   public ChunkPos chunk() {
      return new ChunkPos(this.x(), this.z());
   }

   public static long asLong(BlockPos blockpos) {
      return asLong(blockToSectionCoord(blockpos.getX()), blockToSectionCoord(blockpos.getY()), blockToSectionCoord(blockpos.getZ()));
   }

   public static long asLong(int i, int j, int k) {
      long l = 0L;
      l |= ((long)i & 4194303L) << 42;
      l |= ((long)j & 1048575L) << 0;
      return l | ((long)k & 4194303L) << 20;
   }

   public long asLong() {
      return asLong(this.x(), this.y(), this.z());
   }

   public SectionPos offset(int i, int j, int k) {
      return i == 0 && j == 0 && k == 0 ? this : new SectionPos(this.x() + i, this.y() + j, this.z() + k);
   }

   public Stream<BlockPos> blocksInside() {
      return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
   }

   public static Stream<SectionPos> cube(SectionPos sectionpos, int i) {
      int j = sectionpos.x();
      int k = sectionpos.y();
      int l = sectionpos.z();
      return betweenClosedStream(j - i, k - i, l - i, j + i, k + i, l + i);
   }

   public static Stream<SectionPos> aroundChunk(ChunkPos chunkpos, int i, int j, int k) {
      int l = chunkpos.x;
      int i1 = chunkpos.z;
      return betweenClosedStream(l - i, j, i1 - i, l + i, k - 1, i1 + i);
   }

   public static Stream<SectionPos> betweenClosedStream(final int i, final int j, final int k, final int l, final int i1, final int j1) {
      return StreamSupport.stream(new Spliterators.AbstractSpliterator<SectionPos>((long)((l - i + 1) * (i1 - j + 1) * (j1 - k + 1)), 64) {
         final Cursor3D cursor = new Cursor3D(i, j, k, l, i1, j1);

         public boolean tryAdvance(Consumer<? super SectionPos> consumer) {
            if (this.cursor.advance()) {
               consumer.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
               return true;
            } else {
               return false;
            }
         }
      }, false);
   }

   public static void aroundAndAtBlockPos(BlockPos blockpos, LongConsumer longconsumer) {
      aroundAndAtBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ(), longconsumer);
   }

   public static void aroundAndAtBlockPos(long i, LongConsumer longconsumer) {
      aroundAndAtBlockPos(BlockPos.getX(i), BlockPos.getY(i), BlockPos.getZ(i), longconsumer);
   }

   public static void aroundAndAtBlockPos(int i, int j, int k, LongConsumer longconsumer) {
      int l = blockToSectionCoord(i - 1);
      int i1 = blockToSectionCoord(i + 1);
      int j1 = blockToSectionCoord(j - 1);
      int k1 = blockToSectionCoord(j + 1);
      int l1 = blockToSectionCoord(k - 1);
      int i2 = blockToSectionCoord(k + 1);
      if (l == i1 && j1 == k1 && l1 == i2) {
         longconsumer.accept(asLong(l, j1, l1));
      } else {
         for(int j2 = l; j2 <= i1; ++j2) {
            for(int k2 = j1; k2 <= k1; ++k2) {
               for(int l2 = l1; l2 <= i2; ++l2) {
                  longconsumer.accept(asLong(j2, k2, l2));
               }
            }
         }
      }

   }
}
