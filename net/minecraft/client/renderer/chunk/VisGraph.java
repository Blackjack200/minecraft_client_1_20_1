package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class VisGraph {
   private static final int SIZE_IN_BITS = 4;
   private static final int LEN = 16;
   private static final int MASK = 15;
   private static final int SIZE = 4096;
   private static final int X_SHIFT = 0;
   private static final int Z_SHIFT = 4;
   private static final int Y_SHIFT = 8;
   private static final int DX = (int)Math.pow(16.0D, 0.0D);
   private static final int DZ = (int)Math.pow(16.0D, 1.0D);
   private static final int DY = (int)Math.pow(16.0D, 2.0D);
   private static final int INVALID_INDEX = -1;
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BitSet bitSet = new BitSet(4096);
   private static final int[] INDEX_OF_EDGES = Util.make(new int[1352], (aint) -> {
      int i = 0;
      int j = 15;
      int k = 0;

      for(int l = 0; l < 16; ++l) {
         for(int i1 = 0; i1 < 16; ++i1) {
            for(int j1 = 0; j1 < 16; ++j1) {
               if (l == 0 || l == 15 || i1 == 0 || i1 == 15 || j1 == 0 || j1 == 15) {
                  aint[k++] = getIndex(l, i1, j1);
               }
            }
         }
      }

   });
   private int empty = 4096;

   public void setOpaque(BlockPos blockpos) {
      this.bitSet.set(getIndex(blockpos), true);
      --this.empty;
   }

   private static int getIndex(BlockPos blockpos) {
      return getIndex(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
   }

   private static int getIndex(int i, int j, int k) {
      return i << 0 | j << 8 | k << 4;
   }

   public VisibilitySet resolve() {
      VisibilitySet visibilityset = new VisibilitySet();
      if (4096 - this.empty < 256) {
         visibilityset.setAll(true);
      } else if (this.empty == 0) {
         visibilityset.setAll(false);
      } else {
         for(int i : INDEX_OF_EDGES) {
            if (!this.bitSet.get(i)) {
               visibilityset.add(this.floodFill(i));
            }
         }
      }

      return visibilityset;
   }

   private Set<Direction> floodFill(int i) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);
      IntPriorityQueue intpriorityqueue = new IntArrayFIFOQueue();
      intpriorityqueue.enqueue(i);
      this.bitSet.set(i, true);

      while(!intpriorityqueue.isEmpty()) {
         int j = intpriorityqueue.dequeueInt();
         this.addEdges(j, set);

         for(Direction direction : DIRECTIONS) {
            int k = this.getNeighborIndexAtFace(j, direction);
            if (k >= 0 && !this.bitSet.get(k)) {
               this.bitSet.set(k, true);
               intpriorityqueue.enqueue(k);
            }
         }
      }

      return set;
   }

   private void addEdges(int i, Set<Direction> set) {
      int j = i >> 0 & 15;
      if (j == 0) {
         set.add(Direction.WEST);
      } else if (j == 15) {
         set.add(Direction.EAST);
      }

      int k = i >> 8 & 15;
      if (k == 0) {
         set.add(Direction.DOWN);
      } else if (k == 15) {
         set.add(Direction.UP);
      }

      int l = i >> 4 & 15;
      if (l == 0) {
         set.add(Direction.NORTH);
      } else if (l == 15) {
         set.add(Direction.SOUTH);
      }

   }

   private int getNeighborIndexAtFace(int i, Direction direction) {
      switch (direction) {
         case DOWN:
            if ((i >> 8 & 15) == 0) {
               return -1;
            }

            return i - DY;
         case UP:
            if ((i >> 8 & 15) == 15) {
               return -1;
            }

            return i + DY;
         case NORTH:
            if ((i >> 4 & 15) == 0) {
               return -1;
            }

            return i - DZ;
         case SOUTH:
            if ((i >> 4 & 15) == 15) {
               return -1;
            }

            return i + DZ;
         case WEST:
            if ((i >> 0 & 15) == 0) {
               return -1;
            }

            return i - DX;
         case EAST:
            if ((i >> 0 & 15) == 15) {
               return -1;
            }

            return i + DX;
         default:
            return -1;
      }
   }
}
