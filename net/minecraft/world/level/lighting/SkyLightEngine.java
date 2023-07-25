package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
   private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
   private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
   private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
   private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
   private final ChunkSkyLightSources emptyChunkSources;

   public SkyLightEngine(LightChunkGetter lightchunkgetter) {
      this(lightchunkgetter, new SkyLightSectionStorage(lightchunkgetter));
   }

   @VisibleForTesting
   protected SkyLightEngine(LightChunkGetter lightchunkgetter, SkyLightSectionStorage skylightsectionstorage) {
      super(lightchunkgetter, skylightsectionstorage);
      this.emptyChunkSources = new ChunkSkyLightSources(lightchunkgetter.getLevel());
   }

   private static boolean isSourceLevel(int i) {
      return i == 15;
   }

   private int getLowestSourceY(int i, int j, int k) {
      ChunkSkyLightSources chunkskylightsources = this.getChunkSources(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
      return chunkskylightsources == null ? k : chunkskylightsources.getLowestSourceY(SectionPos.sectionRelative(i), SectionPos.sectionRelative(j));
   }

   @Nullable
   private ChunkSkyLightSources getChunkSources(int i, int j) {
      LightChunk lightchunk = this.chunkSource.getChunkForLighting(i, j);
      return lightchunk != null ? lightchunk.getSkyLightSources() : null;
   }

   protected void checkNode(long i) {
      int j = BlockPos.getX(i);
      int k = BlockPos.getY(i);
      int l = BlockPos.getZ(i);
      long i1 = SectionPos.blockToSection(i);
      int j1 = this.storage.lightOnInSection(i1) ? this.getLowestSourceY(j, l, Integer.MAX_VALUE) : Integer.MAX_VALUE;
      if (j1 != Integer.MAX_VALUE) {
         this.updateSourcesInColumn(j, l, j1);
      }

      if (this.storage.storingLightForSection(i1)) {
         boolean flag = k >= j1;
         if (flag) {
            this.enqueueDecrease(i, REMOVE_SKY_SOURCE_ENTRY);
            this.enqueueIncrease(i, ADD_SKY_SOURCE_ENTRY);
         } else {
            int k1 = this.storage.getStoredLevel(i);
            if (k1 > 0) {
               this.storage.setStoredLevel(i, 0);
               this.enqueueDecrease(i, LightEngine.QueueEntry.decreaseAllDirections(k1));
            } else {
               this.enqueueDecrease(i, PULL_LIGHT_IN_ENTRY);
            }
         }

      }
   }

   private void updateSourcesInColumn(int i, int j, int k) {
      int l = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
      this.removeSourcesBelow(i, j, k, l);
      this.addSourcesAbove(i, j, k, l);
   }

   private void removeSourcesBelow(int i, int j, int k, int l) {
      if (k > l) {
         int i1 = SectionPos.blockToSectionCoord(i);
         int j1 = SectionPos.blockToSectionCoord(j);
         int k1 = k - 1;

         for(int l1 = SectionPos.blockToSectionCoord(k1); this.storage.hasLightDataAtOrBelow(l1); --l1) {
            if (this.storage.storingLightForSection(SectionPos.asLong(i1, l1, j1))) {
               int i2 = SectionPos.sectionToBlockCoord(l1);
               int j2 = i2 + 15;

               for(int k2 = Math.min(j2, k1); k2 >= i2; --k2) {
                  long l2 = BlockPos.asLong(i, k2, j);
                  if (!isSourceLevel(this.storage.getStoredLevel(l2))) {
                     return;
                  }

                  this.storage.setStoredLevel(l2, 0);
                  this.enqueueDecrease(l2, k2 == k - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
               }
            }
         }

      }
   }

   private void addSourcesAbove(int i, int j, int k, int l) {
      int i1 = SectionPos.blockToSectionCoord(i);
      int j1 = SectionPos.blockToSectionCoord(j);
      int k1 = Math.max(Math.max(this.getLowestSourceY(i - 1, j, Integer.MIN_VALUE), this.getLowestSourceY(i + 1, j, Integer.MIN_VALUE)), Math.max(this.getLowestSourceY(i, j - 1, Integer.MIN_VALUE), this.getLowestSourceY(i, j + 1, Integer.MIN_VALUE)));
      int l1 = Math.max(k, l);

      for(long i2 = SectionPos.asLong(i1, SectionPos.blockToSectionCoord(l1), j1); !this.storage.isAboveData(i2); i2 = SectionPos.offset(i2, Direction.UP)) {
         if (this.storage.storingLightForSection(i2)) {
            int j2 = SectionPos.sectionToBlockCoord(SectionPos.y(i2));
            int k2 = j2 + 15;

            for(int l2 = Math.max(j2, l1); l2 <= k2; ++l2) {
               long i3 = BlockPos.asLong(i, l2, j);
               if (isSourceLevel(this.storage.getStoredLevel(i3))) {
                  return;
               }

               this.storage.setStoredLevel(i3, 15);
               if (l2 < k1 || l2 == k) {
                  this.enqueueIncrease(i3, ADD_SKY_SOURCE_ENTRY);
               }
            }
         }
      }

   }

   protected void propagateIncrease(long i, long j, int k) {
      BlockState blockstate = null;
      int l = this.countEmptySectionsBelowIfAtBorder(i);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(j, direction)) {
            long i1 = BlockPos.offset(i, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(i1))) {
               int j1 = this.storage.getStoredLevel(i1);
               int k1 = k - 1;
               if (k1 > j1) {
                  this.mutablePos.set(i1);
                  BlockState blockstate1 = this.getState(this.mutablePos);
                  int l1 = k - this.getOpacity(blockstate1, this.mutablePos);
                  if (l1 > j1) {
                     if (blockstate == null) {
                        blockstate = LightEngine.QueueEntry.isFromEmptyShape(j) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(i));
                     }

                     if (!this.shapeOccludes(i, blockstate, i1, blockstate1, direction)) {
                        this.storage.setStoredLevel(i1, l1);
                        if (l1 > 1) {
                           this.enqueueIncrease(i1, LightEngine.QueueEntry.increaseSkipOneDirection(l1, isEmptyShape(blockstate1), direction.getOpposite()));
                        }

                        this.propagateFromEmptySections(i1, direction, l1, true, l);
                     }
                  }
               }
            }
         }
      }

   }

   protected void propagateDecrease(long i, long j) {
      int k = this.countEmptySectionsBelowIfAtBorder(i);
      int l = LightEngine.QueueEntry.getFromLevel(j);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(j, direction)) {
            long i1 = BlockPos.offset(i, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(i1))) {
               int j1 = this.storage.getStoredLevel(i1);
               if (j1 != 0) {
                  if (j1 <= l - 1) {
                     this.storage.setStoredLevel(i1, 0);
                     this.enqueueDecrease(i1, LightEngine.QueueEntry.decreaseSkipOneDirection(j1, direction.getOpposite()));
                     this.propagateFromEmptySections(i1, direction, j1, false, k);
                  } else {
                     this.enqueueIncrease(i1, LightEngine.QueueEntry.increaseOnlyOneDirection(j1, false, direction.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int countEmptySectionsBelowIfAtBorder(long i) {
      int j = BlockPos.getY(i);
      int k = SectionPos.sectionRelative(j);
      if (k != 0) {
         return 0;
      } else {
         int l = BlockPos.getX(i);
         int i1 = BlockPos.getZ(i);
         int j1 = SectionPos.sectionRelative(l);
         int k1 = SectionPos.sectionRelative(i1);
         if (j1 != 0 && j1 != 15 && k1 != 0 && k1 != 15) {
            return 0;
         } else {
            int l1 = SectionPos.blockToSectionCoord(l);
            int i2 = SectionPos.blockToSectionCoord(j);
            int j2 = SectionPos.blockToSectionCoord(i1);

            int k2;
            for(k2 = 0; !this.storage.storingLightForSection(SectionPos.asLong(l1, i2 - k2 - 1, j2)) && this.storage.hasLightDataAtOrBelow(i2 - k2 - 1); ++k2) {
            }

            return k2;
         }
      }
   }

   private void propagateFromEmptySections(long i, Direction direction, int j, boolean flag, int k) {
      if (k != 0) {
         int l = BlockPos.getX(i);
         int i1 = BlockPos.getZ(i);
         if (crossedSectionEdge(direction, SectionPos.sectionRelative(l), SectionPos.sectionRelative(i1))) {
            int j1 = BlockPos.getY(i);
            int k1 = SectionPos.blockToSectionCoord(l);
            int l1 = SectionPos.blockToSectionCoord(i1);
            int i2 = SectionPos.blockToSectionCoord(j1) - 1;
            int j2 = i2 - k + 1;

            while(i2 >= j2) {
               if (!this.storage.storingLightForSection(SectionPos.asLong(k1, i2, l1))) {
                  --i2;
               } else {
                  int k2 = SectionPos.sectionToBlockCoord(i2);

                  for(int l2 = 15; l2 >= 0; --l2) {
                     long i3 = BlockPos.asLong(l, k2 + l2, i1);
                     if (flag) {
                        this.storage.setStoredLevel(i3, j);
                        if (j > 1) {
                           this.enqueueIncrease(i3, LightEngine.QueueEntry.increaseSkipOneDirection(j, true, direction.getOpposite()));
                        }
                     } else {
                        this.storage.setStoredLevel(i3, 0);
                        this.enqueueDecrease(i3, LightEngine.QueueEntry.decreaseSkipOneDirection(j, direction.getOpposite()));
                     }
                  }

                  --i2;
               }
            }

         }
      }
   }

   private static boolean crossedSectionEdge(Direction direction, int i, int j) {
      boolean var10000;
      switch (direction) {
         case NORTH:
            var10000 = j == 15;
            break;
         case SOUTH:
            var10000 = j == 0;
            break;
         case WEST:
            var10000 = i == 15;
            break;
         case EAST:
            var10000 = i == 0;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public void setLightEnabled(ChunkPos chunkpos, boolean flag) {
      super.setLightEnabled(chunkpos, flag);
      if (flag) {
         ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x, chunkpos.z), this.emptyChunkSources);
         int i = chunkskylightsources.getHighestLowestSourceY() - 1;
         int j = SectionPos.blockToSectionCoord(i) + 1;
         long k = SectionPos.getZeroNode(chunkpos.x, chunkpos.z);
         int l = this.storage.getTopSectionY(k);
         int i1 = Math.max(this.storage.getBottomSectionY(), j);

         for(int j1 = l - 1; j1 >= i1; --j1) {
            DataLayer datalayer = this.storage.getDataLayerToWrite(SectionPos.asLong(chunkpos.x, j1, chunkpos.z));
            if (datalayer != null && datalayer.isEmpty()) {
               datalayer.fill(15);
            }
         }
      }

   }

   public void propagateLightSources(ChunkPos chunkpos) {
      long i = SectionPos.getZeroNode(chunkpos.x, chunkpos.z);
      this.storage.setLightEnabled(i, true);
      ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x, chunkpos.z), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources1 = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x, chunkpos.z - 1), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources2 = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x, chunkpos.z + 1), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources3 = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x - 1, chunkpos.z), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources4 = Objects.requireNonNullElse(this.getChunkSources(chunkpos.x + 1, chunkpos.z), this.emptyChunkSources);
      int j = this.storage.getTopSectionY(i);
      int k = this.storage.getBottomSectionY();
      int l = SectionPos.sectionToBlockCoord(chunkpos.x);
      int i1 = SectionPos.sectionToBlockCoord(chunkpos.z);

      for(int j1 = j - 1; j1 >= k; --j1) {
         long k1 = SectionPos.asLong(chunkpos.x, j1, chunkpos.z);
         DataLayer datalayer = this.storage.getDataLayerToWrite(k1);
         if (datalayer != null) {
            int l1 = SectionPos.sectionToBlockCoord(j1);
            int i2 = l1 + 15;
            boolean flag = false;

            for(int j2 = 0; j2 < 16; ++j2) {
               for(int k2 = 0; k2 < 16; ++k2) {
                  int l2 = chunkskylightsources.getLowestSourceY(k2, j2);
                  if (l2 <= i2) {
                     int i3 = j2 == 0 ? chunkskylightsources1.getLowestSourceY(k2, 15) : chunkskylightsources.getLowestSourceY(k2, j2 - 1);
                     int j3 = j2 == 15 ? chunkskylightsources2.getLowestSourceY(k2, 0) : chunkskylightsources.getLowestSourceY(k2, j2 + 1);
                     int k3 = k2 == 0 ? chunkskylightsources3.getLowestSourceY(15, j2) : chunkskylightsources.getLowestSourceY(k2 - 1, j2);
                     int l3 = k2 == 15 ? chunkskylightsources4.getLowestSourceY(0, j2) : chunkskylightsources.getLowestSourceY(k2 + 1, j2);
                     int i4 = Math.max(Math.max(i3, j3), Math.max(k3, l3));

                     for(int j4 = i2; j4 >= Math.max(l1, l2); --j4) {
                        datalayer.set(k2, SectionPos.sectionRelative(j4), j2, 15);
                        if (j4 == l2 || j4 < i4) {
                           long k4 = BlockPos.asLong(l + k2, j4, i1 + j2);
                           this.enqueueIncrease(k4, LightEngine.QueueEntry.increaseSkySourceInDirections(j4 == l2, j4 < i3, j4 < j3, j4 < k3, j4 < l3));
                        }
                     }

                     if (l2 < l1) {
                        flag = true;
                     }
                  }
               }
            }

            if (!flag) {
               break;
            }
         }
      }

   }
}
