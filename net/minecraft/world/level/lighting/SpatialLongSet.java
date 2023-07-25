package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.Mth;

public class SpatialLongSet extends LongLinkedOpenHashSet {
   private final SpatialLongSet.InternalMap map;

   public SpatialLongSet(int i, float f) {
      super(i, f);
      this.map = new SpatialLongSet.InternalMap(i / 64, f);
   }

   public boolean add(long i) {
      return this.map.addBit(i);
   }

   public boolean rem(long i) {
      return this.map.removeBit(i);
   }

   public long removeFirstLong() {
      return this.map.removeFirstBit();
   }

   public int size() {
      throw new UnsupportedOperationException();
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   protected static class InternalMap extends Long2LongLinkedOpenHashMap {
      private static final int X_BITS = Mth.log2(60000000);
      private static final int Z_BITS = Mth.log2(60000000);
      private static final int Y_BITS = 64 - X_BITS - Z_BITS;
      private static final int Y_OFFSET = 0;
      private static final int Z_OFFSET = Y_BITS;
      private static final int X_OFFSET = Y_BITS + Z_BITS;
      private static final long OUTER_MASK = 3L << X_OFFSET | 3L | 3L << Z_OFFSET;
      private int lastPos = -1;
      private long lastOuterKey;
      private final int minSize;

      public InternalMap(int i, float f) {
         super(i, f);
         this.minSize = i;
      }

      static long getOuterKey(long i) {
         return i & ~OUTER_MASK;
      }

      static int getInnerKey(long i) {
         int j = (int)(i >>> X_OFFSET & 3L);
         int k = (int)(i >>> 0 & 3L);
         int l = (int)(i >>> Z_OFFSET & 3L);
         return j << 4 | l << 2 | k;
      }

      static long getFullKey(long i, int j) {
         i |= (long)(j >>> 4 & 3) << X_OFFSET;
         i |= (long)(j >>> 2 & 3) << Z_OFFSET;
         return i | (long)(j >>> 0 & 3) << 0;
      }

      public boolean addBit(long i) {
         long j = getOuterKey(i);
         int k = getInnerKey(i);
         long l = 1L << k;
         int i1;
         if (j == 0L) {
            if (this.containsNullKey) {
               return this.replaceBit(this.n, l);
            }

            this.containsNullKey = true;
            i1 = this.n;
         } else {
            if (this.lastPos != -1 && j == this.lastOuterKey) {
               return this.replaceBit(this.lastPos, l);
            }

            long[] along = this.key;
            i1 = (int)HashCommon.mix(j) & this.mask;

            for(long k1 = along[i1]; k1 != 0L; k1 = along[i1]) {
               if (k1 == j) {
                  this.lastPos = i1;
                  this.lastOuterKey = j;
                  return this.replaceBit(i1, l);
               }

               i1 = i1 + 1 & this.mask;
            }
         }

         this.key[i1] = j;
         this.value[i1] = l;
         if (this.size == 0) {
            this.first = this.last = i1;
            this.link[i1] = -1L;
         } else {
            this.link[this.last] ^= (this.link[this.last] ^ (long)i1 & 4294967295L) & 4294967295L;
            this.link[i1] = ((long)this.last & 4294967295L) << 32 | 4294967295L;
            this.last = i1;
         }

         if (this.size++ >= this.maxFill) {
            this.rehash(HashCommon.arraySize(this.size + 1, this.f));
         }

         return false;
      }

      private boolean replaceBit(int i, long j) {
         boolean flag = (this.value[i] & j) != 0L;
         this.value[i] |= j;
         return flag;
      }

      public boolean removeBit(long i) {
         long j = getOuterKey(i);
         int k = getInnerKey(i);
         long l = 1L << k;
         if (j == 0L) {
            return this.containsNullKey ? this.removeFromNullEntry(l) : false;
         } else if (this.lastPos != -1 && j == this.lastOuterKey) {
            return this.removeFromEntry(this.lastPos, l);
         } else {
            long[] along = this.key;
            int i1 = (int)HashCommon.mix(j) & this.mask;

            for(long j1 = along[i1]; j1 != 0L; j1 = along[i1]) {
               if (j == j1) {
                  this.lastPos = i1;
                  this.lastOuterKey = j;
                  return this.removeFromEntry(i1, l);
               }

               i1 = i1 + 1 & this.mask;
            }

            return false;
         }
      }

      private boolean removeFromNullEntry(long i) {
         if ((this.value[this.n] & i) == 0L) {
            return false;
         } else {
            this.value[this.n] &= ~i;
            if (this.value[this.n] != 0L) {
               return true;
            } else {
               this.containsNullKey = false;
               --this.size;
               this.fixPointers(this.n);
               if (this.size < this.maxFill / 4 && this.n > 16) {
                  this.rehash(this.n / 2);
               }

               return true;
            }
         }
      }

      private boolean removeFromEntry(int i, long j) {
         if ((this.value[i] & j) == 0L) {
            return false;
         } else {
            this.value[i] &= ~j;
            if (this.value[i] != 0L) {
               return true;
            } else {
               this.lastPos = -1;
               --this.size;
               this.fixPointers(i);
               this.shiftKeys(i);
               if (this.size < this.maxFill / 4 && this.n > 16) {
                  this.rehash(this.n / 2);
               }

               return true;
            }
         }
      }

      public long removeFirstBit() {
         if (this.size == 0) {
            throw new NoSuchElementException();
         } else {
            int i = this.first;
            long j = this.key[i];
            int k = Long.numberOfTrailingZeros(this.value[i]);
            this.value[i] &= ~(1L << k);
            if (this.value[i] == 0L) {
               this.removeFirstLong();
               this.lastPos = -1;
            }

            return getFullKey(j, k);
         }
      }

      protected void rehash(int i) {
         if (i > this.minSize) {
            super.rehash(i);
         }

      }
   }
}
