package net.minecraft.world.phys.shapes;

import java.util.BitSet;
import net.minecraft.core.Direction;

public final class BitSetDiscreteVoxelShape extends DiscreteVoxelShape {
   private final BitSet storage;
   private int xMin;
   private int yMin;
   private int zMin;
   private int xMax;
   private int yMax;
   private int zMax;

   public BitSetDiscreteVoxelShape(int i, int j, int k) {
      super(i, j, k);
      this.storage = new BitSet(i * j * k);
      this.xMin = i;
      this.yMin = j;
      this.zMin = k;
   }

   public static BitSetDiscreteVoxelShape withFilledBounds(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(i, j, k);
      bitsetdiscretevoxelshape.xMin = l;
      bitsetdiscretevoxelshape.yMin = i1;
      bitsetdiscretevoxelshape.zMin = j1;
      bitsetdiscretevoxelshape.xMax = k1;
      bitsetdiscretevoxelshape.yMax = l1;
      bitsetdiscretevoxelshape.zMax = i2;

      for(int j2 = l; j2 < k1; ++j2) {
         for(int k2 = i1; k2 < l1; ++k2) {
            for(int l2 = j1; l2 < i2; ++l2) {
               bitsetdiscretevoxelshape.fillUpdateBounds(j2, k2, l2, false);
            }
         }
      }

      return bitsetdiscretevoxelshape;
   }

   public BitSetDiscreteVoxelShape(DiscreteVoxelShape discretevoxelshape) {
      super(discretevoxelshape.xSize, discretevoxelshape.ySize, discretevoxelshape.zSize);
      if (discretevoxelshape instanceof BitSetDiscreteVoxelShape) {
         this.storage = (BitSet)((BitSetDiscreteVoxelShape)discretevoxelshape).storage.clone();
      } else {
         this.storage = new BitSet(this.xSize * this.ySize * this.zSize);

         for(int i = 0; i < this.xSize; ++i) {
            for(int j = 0; j < this.ySize; ++j) {
               for(int k = 0; k < this.zSize; ++k) {
                  if (discretevoxelshape.isFull(i, j, k)) {
                     this.storage.set(this.getIndex(i, j, k));
                  }
               }
            }
         }
      }

      this.xMin = discretevoxelshape.firstFull(Direction.Axis.X);
      this.yMin = discretevoxelshape.firstFull(Direction.Axis.Y);
      this.zMin = discretevoxelshape.firstFull(Direction.Axis.Z);
      this.xMax = discretevoxelshape.lastFull(Direction.Axis.X);
      this.yMax = discretevoxelshape.lastFull(Direction.Axis.Y);
      this.zMax = discretevoxelshape.lastFull(Direction.Axis.Z);
   }

   protected int getIndex(int i, int j, int k) {
      return (i * this.ySize + j) * this.zSize + k;
   }

   public boolean isFull(int i, int j, int k) {
      return this.storage.get(this.getIndex(i, j, k));
   }

   private void fillUpdateBounds(int i, int j, int k, boolean flag) {
      this.storage.set(this.getIndex(i, j, k));
      if (flag) {
         this.xMin = Math.min(this.xMin, i);
         this.yMin = Math.min(this.yMin, j);
         this.zMin = Math.min(this.zMin, k);
         this.xMax = Math.max(this.xMax, i + 1);
         this.yMax = Math.max(this.yMax, j + 1);
         this.zMax = Math.max(this.zMax, k + 1);
      }

   }

   public void fill(int i, int j, int k) {
      this.fillUpdateBounds(i, j, k, true);
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public int firstFull(Direction.Axis direction_axis) {
      return direction_axis.choose(this.xMin, this.yMin, this.zMin);
   }

   public int lastFull(Direction.Axis direction_axis) {
      return direction_axis.choose(this.xMax, this.yMax, this.zMax);
   }

   static BitSetDiscreteVoxelShape join(DiscreteVoxelShape discretevoxelshape, DiscreteVoxelShape discretevoxelshape1, IndexMerger indexmerger, IndexMerger indexmerger1, IndexMerger indexmerger2, BooleanOp booleanop) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(indexmerger.size() - 1, indexmerger1.size() - 1, indexmerger2.size() - 1);
      int[] aint = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
      indexmerger.forMergedIndexes((i, j, k) -> {
         boolean[] aboolean = new boolean[]{false};
         indexmerger1.forMergedIndexes((k1, l1, i2) -> {
            boolean[] aboolean2 = new boolean[]{false};
            indexmerger2.forMergedIndexes((l3, i4, j4) -> {
               if (booleanop.apply(discretevoxelshape.isFullWide(i, k1, l3), discretevoxelshape1.isFullWide(j, l1, i4))) {
                  bitsetdiscretevoxelshape.storage.set(bitsetdiscretevoxelshape.getIndex(k, i2, j4));
                  aint[2] = Math.min(aint[2], j4);
                  aint[5] = Math.max(aint[5], j4);
                  aboolean2[0] = true;
               }

               return true;
            });
            if (aboolean2[0]) {
               aint[1] = Math.min(aint[1], i2);
               aint[4] = Math.max(aint[4], i2);
               aboolean[0] = true;
            }

            return true;
         });
         if (aboolean[0]) {
            aint[0] = Math.min(aint[0], k);
            aint[3] = Math.max(aint[3], k);
         }

         return true;
      });
      bitsetdiscretevoxelshape.xMin = aint[0];
      bitsetdiscretevoxelshape.yMin = aint[1];
      bitsetdiscretevoxelshape.zMin = aint[2];
      bitsetdiscretevoxelshape.xMax = aint[3] + 1;
      bitsetdiscretevoxelshape.yMax = aint[4] + 1;
      bitsetdiscretevoxelshape.zMax = aint[5] + 1;
      return bitsetdiscretevoxelshape;
   }

   protected static void forAllBoxes(DiscreteVoxelShape discretevoxelshape, DiscreteVoxelShape.IntLineConsumer discretevoxelshape_intlineconsumer, boolean flag) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(discretevoxelshape);

      for(int i = 0; i < bitsetdiscretevoxelshape.ySize; ++i) {
         for(int j = 0; j < bitsetdiscretevoxelshape.xSize; ++j) {
            int k = -1;

            for(int l = 0; l <= bitsetdiscretevoxelshape.zSize; ++l) {
               if (bitsetdiscretevoxelshape.isFullWide(j, i, l)) {
                  if (flag) {
                     if (k == -1) {
                        k = l;
                     }
                  } else {
                     discretevoxelshape_intlineconsumer.consume(j, i, l, j + 1, i + 1, l + 1);
                  }
               } else if (k != -1) {
                  int i1 = j;
                  int j1 = i;
                  bitsetdiscretevoxelshape.clearZStrip(k, l, j, i);

                  while(bitsetdiscretevoxelshape.isZStripFull(k, l, i1 + 1, i)) {
                     bitsetdiscretevoxelshape.clearZStrip(k, l, i1 + 1, i);
                     ++i1;
                  }

                  while(bitsetdiscretevoxelshape.isXZRectangleFull(j, i1 + 1, k, l, j1 + 1)) {
                     for(int k1 = j; k1 <= i1; ++k1) {
                        bitsetdiscretevoxelshape.clearZStrip(k, l, k1, j1 + 1);
                     }

                     ++j1;
                  }

                  discretevoxelshape_intlineconsumer.consume(j, i, k, i1 + 1, j1 + 1, l);
                  k = -1;
               }
            }
         }
      }

   }

   private boolean isZStripFull(int i, int j, int k, int l) {
      if (k < this.xSize && l < this.ySize) {
         return this.storage.nextClearBit(this.getIndex(k, l, i)) >= this.getIndex(k, l, j);
      } else {
         return false;
      }
   }

   private boolean isXZRectangleFull(int i, int j, int k, int l, int i1) {
      for(int j1 = i; j1 < j; ++j1) {
         if (!this.isZStripFull(k, l, j1, i1)) {
            return false;
         }
      }

      return true;
   }

   private void clearZStrip(int i, int j, int k, int l) {
      this.storage.clear(this.getIndex(k, l, i), this.getIndex(k, l, j));
   }
}
