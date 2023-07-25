package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>> implements LayerLightEventListener {
   public static final int MAX_LEVEL = 15;
   protected static final int MIN_OPACITY = 1;
   protected static final long PULL_LIGHT_IN_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(1);
   private static final int MIN_QUEUE_SIZE = 512;
   protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
   protected final LightChunkGetter chunkSource;
   protected final S storage;
   private final LongOpenHashSet blockNodesToCheck = new LongOpenHashSet(512, 0.5F);
   private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
   private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
   private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
   private static final int CACHE_SIZE = 2;
   private final long[] lastChunkPos = new long[2];
   private final LightChunk[] lastChunk = new LightChunk[2];

   protected LightEngine(LightChunkGetter lightchunkgetter, S layerlightsectionstorage) {
      this.chunkSource = lightchunkgetter;
      this.storage = layerlightsectionstorage;
      this.clearChunkCache();
   }

   public static boolean hasDifferentLightProperties(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      if (blockstate1 == blockstate) {
         return false;
      } else {
         return blockstate1.getLightBlock(blockgetter, blockpos) != blockstate.getLightBlock(blockgetter, blockpos) || blockstate1.getLightEmission() != blockstate.getLightEmission() || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion();
      }
   }

   public static int getLightBlockInto(BlockGetter blockgetter, BlockState blockstate, BlockPos blockpos, BlockState blockstate1, BlockPos blockpos1, Direction direction, int i) {
      boolean flag = isEmptyShape(blockstate);
      boolean flag1 = isEmptyShape(blockstate1);
      if (flag && flag1) {
         return i;
      } else {
         VoxelShape voxelshape = flag ? Shapes.empty() : blockstate.getOcclusionShape(blockgetter, blockpos);
         VoxelShape voxelshape1 = flag1 ? Shapes.empty() : blockstate1.getOcclusionShape(blockgetter, blockpos1);
         return Shapes.mergedFaceOccludes(voxelshape, voxelshape1, direction) ? 16 : i;
      }
   }

   public static VoxelShape getOcclusionShape(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Direction direction) {
      return isEmptyShape(blockstate) ? Shapes.empty() : blockstate.getFaceOcclusionShape(blockgetter, blockpos, direction);
   }

   protected static boolean isEmptyShape(BlockState blockstate) {
      return !blockstate.canOcclude() || !blockstate.useShapeForLightOcclusion();
   }

   protected BlockState getState(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX());
      int j = SectionPos.blockToSectionCoord(blockpos.getZ());
      LightChunk lightchunk = this.getChunk(i, j);
      return lightchunk == null ? Blocks.BEDROCK.defaultBlockState() : lightchunk.getBlockState(blockpos);
   }

   protected int getOpacity(BlockState blockstate, BlockPos blockpos) {
      return Math.max(1, blockstate.getLightBlock(this.chunkSource.getLevel(), blockpos));
   }

   protected boolean shapeOccludes(long i, BlockState blockstate, long j, BlockState blockstate1, Direction direction) {
      VoxelShape voxelshape = this.getOcclusionShape(blockstate, i, direction);
      VoxelShape voxelshape1 = this.getOcclusionShape(blockstate1, j, direction.getOpposite());
      return Shapes.faceShapeOccludes(voxelshape, voxelshape1);
   }

   protected VoxelShape getOcclusionShape(BlockState blockstate, long i, Direction direction) {
      return getOcclusionShape(this.chunkSource.getLevel(), this.mutablePos.set(i), blockstate, direction);
   }

   @Nullable
   protected LightChunk getChunk(int i, int j) {
      long k = ChunkPos.asLong(i, j);

      for(int l = 0; l < 2; ++l) {
         if (k == this.lastChunkPos[l]) {
            return this.lastChunk[l];
         }
      }

      LightChunk lightchunk = this.chunkSource.getChunkForLighting(i, j);

      for(int i1 = 1; i1 > 0; --i1) {
         this.lastChunkPos[i1] = this.lastChunkPos[i1 - 1];
         this.lastChunk[i1] = this.lastChunk[i1 - 1];
      }

      this.lastChunkPos[0] = k;
      this.lastChunk[0] = lightchunk;
      return lightchunk;
   }

   private void clearChunkCache() {
      Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunk, (Object)null);
   }

   public void checkBlock(BlockPos blockpos) {
      this.blockNodesToCheck.add(blockpos.asLong());
   }

   public void queueSectionData(long i, @Nullable DataLayer datalayer) {
      this.storage.queueSectionData(i, datalayer);
   }

   public void retainData(ChunkPos chunkpos, boolean flag) {
      this.storage.retainData(SectionPos.getZeroNode(chunkpos.x, chunkpos.z), flag);
   }

   public void updateSectionStatus(SectionPos sectionpos, boolean flag) {
      this.storage.updateSectionStatus(sectionpos.asLong(), flag);
   }

   public void setLightEnabled(ChunkPos chunkpos, boolean flag) {
      this.storage.setLightEnabled(SectionPos.getZeroNode(chunkpos.x, chunkpos.z), flag);
   }

   public int runLightUpdates() {
      LongIterator longiterator = this.blockNodesToCheck.iterator();

      while(longiterator.hasNext()) {
         this.checkNode(longiterator.nextLong());
      }

      this.blockNodesToCheck.clear();
      this.blockNodesToCheck.trim(512);
      int i = 0;
      i += this.propagateDecreases();
      i += this.propagateIncreases();
      this.clearChunkCache();
      this.storage.markNewInconsistencies(this);
      this.storage.swapSectionMap();
      return i;
   }

   private int propagateIncreases() {
      int i;
      for(i = 0; !this.increaseQueue.isEmpty(); ++i) {
         long j = this.increaseQueue.dequeueLong();
         long k = this.increaseQueue.dequeueLong();
         int l = this.storage.getStoredLevel(j);
         int i1 = LightEngine.QueueEntry.getFromLevel(k);
         if (LightEngine.QueueEntry.isIncreaseFromEmission(k) && l < i1) {
            this.storage.setStoredLevel(j, i1);
            l = i1;
         }

         if (l == i1) {
            this.propagateIncrease(j, k, l);
         }
      }

      return i;
   }

   private int propagateDecreases() {
      int i;
      for(i = 0; !this.decreaseQueue.isEmpty(); ++i) {
         long j = this.decreaseQueue.dequeueLong();
         long k = this.decreaseQueue.dequeueLong();
         this.propagateDecrease(j, k);
      }

      return i;
   }

   protected void enqueueDecrease(long i, long j) {
      this.decreaseQueue.enqueue(i);
      this.decreaseQueue.enqueue(j);
   }

   protected void enqueueIncrease(long i, long j) {
      this.increaseQueue.enqueue(i);
      this.increaseQueue.enqueue(j);
   }

   public boolean hasLightWork() {
      return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
   }

   @Nullable
   public DataLayer getDataLayerData(SectionPos sectionpos) {
      return this.storage.getDataLayerData(sectionpos.asLong());
   }

   public int getLightValue(BlockPos blockpos) {
      return this.storage.getLightValue(blockpos.asLong());
   }

   public String getDebugData(long i) {
      return this.getDebugSectionType(i).display();
   }

   public LayerLightSectionStorage.SectionType getDebugSectionType(long i) {
      return this.storage.getDebugSectionType(i);
   }

   protected abstract void checkNode(long i);

   protected abstract void propagateIncrease(long i, long j, int k);

   protected abstract void propagateDecrease(long i, long j);

   public static class QueueEntry {
      private static final int FROM_LEVEL_BITS = 4;
      private static final int DIRECTION_BITS = 6;
      private static final long LEVEL_MASK = 15L;
      private static final long DIRECTIONS_MASK = 1008L;
      private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
      private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

      public static long decreaseSkipOneDirection(int i, Direction direction) {
         long j = withoutDirection(1008L, direction);
         return withLevel(j, i);
      }

      public static long decreaseAllDirections(int i) {
         return withLevel(1008L, i);
      }

      public static long increaseLightFromEmission(int i, boolean flag) {
         long j = 1008L;
         j |= 2048L;
         if (flag) {
            j |= 1024L;
         }

         return withLevel(j, i);
      }

      public static long increaseSkipOneDirection(int i, boolean flag, Direction direction) {
         long j = withoutDirection(1008L, direction);
         if (flag) {
            j |= 1024L;
         }

         return withLevel(j, i);
      }

      public static long increaseOnlyOneDirection(int i, boolean flag, Direction direction) {
         long j = 0L;
         if (flag) {
            j |= 1024L;
         }

         j = withDirection(j, direction);
         return withLevel(j, i);
      }

      public static long increaseSkySourceInDirections(boolean flag, boolean flag1, boolean flag2, boolean flag3, boolean flag4) {
         long i = withLevel(0L, 15);
         if (flag) {
            i = withDirection(i, Direction.DOWN);
         }

         if (flag1) {
            i = withDirection(i, Direction.NORTH);
         }

         if (flag2) {
            i = withDirection(i, Direction.SOUTH);
         }

         if (flag3) {
            i = withDirection(i, Direction.WEST);
         }

         if (flag4) {
            i = withDirection(i, Direction.EAST);
         }

         return i;
      }

      public static int getFromLevel(long i) {
         return (int)(i & 15L);
      }

      public static boolean isFromEmptyShape(long i) {
         return (i & 1024L) != 0L;
      }

      public static boolean isIncreaseFromEmission(long i) {
         return (i & 2048L) != 0L;
      }

      public static boolean shouldPropagateInDirection(long i, Direction direction) {
         return (i & 1L << direction.ordinal() + 4) != 0L;
      }

      private static long withLevel(long i, int j) {
         return i & -16L | (long)j & 15L;
      }

      private static long withDirection(long i, Direction direction) {
         return i | 1L << direction.ordinal() + 4;
      }

      private static long withoutDirection(long i, Direction direction) {
         return i & ~(1L << direction.ordinal() + 4);
      }
   }
}
