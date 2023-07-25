package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
   private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

   public BlockLightEngine(LightChunkGetter lightchunkgetter) {
      this(lightchunkgetter, new BlockLightSectionStorage(lightchunkgetter));
   }

   @VisibleForTesting
   public BlockLightEngine(LightChunkGetter lightchunkgetter, BlockLightSectionStorage blocklightsectionstorage) {
      super(lightchunkgetter, blocklightsectionstorage);
   }

   protected void checkNode(long i) {
      long j = SectionPos.blockToSection(i);
      if (this.storage.storingLightForSection(j)) {
         BlockState blockstate = this.getState(this.mutablePos.set(i));
         int k = this.getEmission(i, blockstate);
         int l = this.storage.getStoredLevel(i);
         if (k < l) {
            this.storage.setStoredLevel(i, 0);
            this.enqueueDecrease(i, LightEngine.QueueEntry.decreaseAllDirections(l));
         } else {
            this.enqueueDecrease(i, PULL_LIGHT_IN_ENTRY);
         }

         if (k > 0) {
            this.enqueueIncrease(i, LightEngine.QueueEntry.increaseLightFromEmission(k, isEmptyShape(blockstate)));
         }

      }
   }

   protected void propagateIncrease(long i, long j, int k) {
      BlockState blockstate = null;

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(j, direction)) {
            long l = BlockPos.offset(i, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(l))) {
               int i1 = this.storage.getStoredLevel(l);
               int j1 = k - 1;
               if (j1 > i1) {
                  this.mutablePos.set(l);
                  BlockState blockstate1 = this.getState(this.mutablePos);
                  int k1 = k - this.getOpacity(blockstate1, this.mutablePos);
                  if (k1 > i1) {
                     if (blockstate == null) {
                        blockstate = LightEngine.QueueEntry.isFromEmptyShape(j) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(i));
                     }

                     if (!this.shapeOccludes(i, blockstate, l, blockstate1, direction)) {
                        this.storage.setStoredLevel(l, k1);
                        if (k1 > 1) {
                           this.enqueueIncrease(l, LightEngine.QueueEntry.increaseSkipOneDirection(k1, isEmptyShape(blockstate1), direction.getOpposite()));
                        }
                     }
                  }
               }
            }
         }
      }

   }

   protected void propagateDecrease(long i, long j) {
      int k = LightEngine.QueueEntry.getFromLevel(j);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(j, direction)) {
            long l = BlockPos.offset(i, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(l))) {
               int i1 = this.storage.getStoredLevel(l);
               if (i1 != 0) {
                  if (i1 <= k - 1) {
                     BlockState blockstate = this.getState(this.mutablePos.set(l));
                     int j1 = this.getEmission(l, blockstate);
                     this.storage.setStoredLevel(l, 0);
                     if (j1 < i1) {
                        this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseSkipOneDirection(i1, direction.getOpposite()));
                     }

                     if (j1 > 0) {
                        this.enqueueIncrease(l, LightEngine.QueueEntry.increaseLightFromEmission(j1, isEmptyShape(blockstate)));
                     }
                  } else {
                     this.enqueueIncrease(l, LightEngine.QueueEntry.increaseOnlyOneDirection(i1, false, direction.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int getEmission(long i, BlockState blockstate) {
      int j = blockstate.getLightEmission();
      return j > 0 && this.storage.lightOnInSection(SectionPos.blockToSection(i)) ? j : 0;
   }

   public void propagateLightSources(ChunkPos chunkpos) {
      this.setLightEnabled(chunkpos, true);
      LightChunk lightchunk = this.chunkSource.getChunkForLighting(chunkpos.x, chunkpos.z);
      if (lightchunk != null) {
         lightchunk.findBlockLightSources((blockpos, blockstate) -> {
            int i = blockstate.getLightEmission();
            this.enqueueIncrease(blockpos.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(i, isEmptyShape(blockstate)));
         });
      }

   }
}
