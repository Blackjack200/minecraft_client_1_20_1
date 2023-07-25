package net.minecraft.world.level.block.state.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

public class BlockPattern {
   private final Predicate<BlockInWorld>[][][] pattern;
   private final int depth;
   private final int height;
   private final int width;

   public BlockPattern(Predicate<BlockInWorld>[][][] apredicate) {
      this.pattern = apredicate;
      this.depth = apredicate.length;
      if (this.depth > 0) {
         this.height = apredicate[0].length;
         if (this.height > 0) {
            this.width = apredicate[0][0].length;
         } else {
            this.width = 0;
         }
      } else {
         this.height = 0;
         this.width = 0;
      }

   }

   public int getDepth() {
      return this.depth;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   @VisibleForTesting
   public Predicate<BlockInWorld>[][][] getPattern() {
      return this.pattern;
   }

   @Nullable
   @VisibleForTesting
   public BlockPattern.BlockPatternMatch matches(LevelReader levelreader, BlockPos blockpos, Direction direction, Direction direction1) {
      LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(levelreader, false);
      return this.matches(blockpos, direction, direction1, loadingcache);
   }

   @Nullable
   private BlockPattern.BlockPatternMatch matches(BlockPos blockpos, Direction direction, Direction direction1, LoadingCache<BlockPos, BlockInWorld> loadingcache) {
      for(int i = 0; i < this.width; ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               if (!this.pattern[k][j][i].test(loadingcache.getUnchecked(translateAndRotate(blockpos, direction, direction1, i, j, k)))) {
                  return null;
               }
            }
         }
      }

      return new BlockPattern.BlockPatternMatch(blockpos, direction, direction1, loadingcache, this.width, this.height, this.depth);
   }

   @Nullable
   public BlockPattern.BlockPatternMatch find(LevelReader levelreader, BlockPos blockpos) {
      LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(levelreader, false);
      int i = Math.max(Math.max(this.width, this.height), this.depth);

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos, blockpos.offset(i - 1, i - 1, i - 1))) {
         for(Direction direction : Direction.values()) {
            for(Direction direction1 : Direction.values()) {
               if (direction1 != direction && direction1 != direction.getOpposite()) {
                  BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch = this.matches(blockpos1, direction, direction1, loadingcache);
                  if (blockpattern_blockpatternmatch != null) {
                     return blockpattern_blockpatternmatch;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader levelreader, boolean flag) {
      return CacheBuilder.newBuilder().build(new BlockPattern.BlockCacheLoader(levelreader, flag));
   }

   protected static BlockPos translateAndRotate(BlockPos blockpos, Direction direction, Direction direction1, int i, int j, int k) {
      if (direction != direction1 && direction != direction1.getOpposite()) {
         Vec3i vec3i = new Vec3i(direction.getStepX(), direction.getStepY(), direction.getStepZ());
         Vec3i vec3i1 = new Vec3i(direction1.getStepX(), direction1.getStepY(), direction1.getStepZ());
         Vec3i vec3i2 = vec3i.cross(vec3i1);
         return blockpos.offset(vec3i1.getX() * -j + vec3i2.getX() * i + vec3i.getX() * k, vec3i1.getY() * -j + vec3i2.getY() * i + vec3i.getY() * k, vec3i1.getZ() * -j + vec3i2.getZ() * i + vec3i.getZ() * k);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
      private final LevelReader level;
      private final boolean loadChunks;

      public BlockCacheLoader(LevelReader levelreader, boolean flag) {
         this.level = levelreader;
         this.loadChunks = flag;
      }

      public BlockInWorld load(BlockPos blockpos) {
         return new BlockInWorld(this.level, blockpos, this.loadChunks);
      }
   }

   public static class BlockPatternMatch {
      private final BlockPos frontTopLeft;
      private final Direction forwards;
      private final Direction up;
      private final LoadingCache<BlockPos, BlockInWorld> cache;
      private final int width;
      private final int height;
      private final int depth;

      public BlockPatternMatch(BlockPos blockpos, Direction direction, Direction direction1, LoadingCache<BlockPos, BlockInWorld> loadingcache, int i, int j, int k) {
         this.frontTopLeft = blockpos;
         this.forwards = direction;
         this.up = direction1;
         this.cache = loadingcache;
         this.width = i;
         this.height = j;
         this.depth = k;
      }

      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public Direction getForwards() {
         return this.forwards;
      }

      public Direction getUp() {
         return this.up;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public int getDepth() {
         return this.depth;
      }

      public BlockInWorld getBlock(int i, int j, int k) {
         return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), i, j, k));
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }
   }
}
