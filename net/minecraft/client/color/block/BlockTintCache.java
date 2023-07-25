package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BlockTintCache {
   private static final int MAX_CACHE_ENTRIES = 256;
   private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
   private final Long2ObjectLinkedOpenHashMap<BlockTintCache.CacheData> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final ToIntFunction<BlockPos> source;

   public BlockTintCache(ToIntFunction<BlockPos> tointfunction) {
      this.source = tointfunction;
   }

   public int getColor(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX());
      int j = SectionPos.blockToSectionCoord(blockpos.getZ());
      BlockTintCache.LatestCacheInfo blocktintcache_latestcacheinfo = this.latestChunkOnThread.get();
      if (blocktintcache_latestcacheinfo.x != i || blocktintcache_latestcacheinfo.z != j || blocktintcache_latestcacheinfo.cache == null || blocktintcache_latestcacheinfo.cache.isInvalidated()) {
         blocktintcache_latestcacheinfo.x = i;
         blocktintcache_latestcacheinfo.z = j;
         blocktintcache_latestcacheinfo.cache = this.findOrCreateChunkCache(i, j);
      }

      int[] aint = blocktintcache_latestcacheinfo.cache.getLayer(blockpos.getY());
      int k = blockpos.getX() & 15;
      int l = blockpos.getZ() & 15;
      int i1 = l << 4 | k;
      int j1 = aint[i1];
      if (j1 != -1) {
         return j1;
      } else {
         int k1 = this.source.applyAsInt(blockpos);
         aint[i1] = k1;
         return k1;
      }
   }

   public void invalidateForChunk(int i, int j) {
      try {
         this.lock.writeLock().lock();

         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               long i1 = ChunkPos.asLong(i + k, j + l);
               BlockTintCache.CacheData blocktintcache_cachedata = this.cache.remove(i1);
               if (blocktintcache_cachedata != null) {
                  blocktintcache_cachedata.invalidate();
               }
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   public void invalidateAll() {
      try {
         this.lock.writeLock().lock();
         this.cache.values().forEach(BlockTintCache.CacheData::invalidate);
         this.cache.clear();
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   private BlockTintCache.CacheData findOrCreateChunkCache(int i, int j) {
      long k = ChunkPos.asLong(i, j);
      this.lock.readLock().lock();

      try {
         BlockTintCache.CacheData blocktintcache_cachedata = this.cache.get(k);
         if (blocktintcache_cachedata != null) {
            return blocktintcache_cachedata;
         }
      } finally {
         this.lock.readLock().unlock();
      }

      this.lock.writeLock().lock();

      BlockTintCache.CacheData blocktintcache_cachedata2;
      try {
         BlockTintCache.CacheData blocktintcache_cachedata1 = this.cache.get(k);
         if (blocktintcache_cachedata1 == null) {
            blocktintcache_cachedata2 = new BlockTintCache.CacheData();
            if (this.cache.size() >= 256) {
               BlockTintCache.CacheData blocktintcache_cachedata3 = this.cache.removeFirst();
               if (blocktintcache_cachedata3 != null) {
                  blocktintcache_cachedata3.invalidate();
               }
            }

            this.cache.put(k, blocktintcache_cachedata2);
            return blocktintcache_cachedata2;
         }

         blocktintcache_cachedata2 = blocktintcache_cachedata1;
      } finally {
         this.lock.writeLock().unlock();
      }

      return blocktintcache_cachedata2;
   }

   static class CacheData {
      private final Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap<>(16);
      private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
      private static final int BLOCKS_PER_LAYER = Mth.square(16);
      private volatile boolean invalidated;

      public int[] getLayer(int i) {
         this.lock.readLock().lock();

         try {
            int[] aint = this.cache.get(i);
            if (aint != null) {
               return aint;
            }
         } finally {
            this.lock.readLock().unlock();
         }

         this.lock.writeLock().lock();

         int[] var12;
         try {
            var12 = this.cache.computeIfAbsent(i, (j) -> this.allocateLayer());
         } finally {
            this.lock.writeLock().unlock();
         }

         return var12;
      }

      private int[] allocateLayer() {
         int[] aint = new int[BLOCKS_PER_LAYER];
         Arrays.fill(aint, -1);
         return aint;
      }

      public boolean isInvalidated() {
         return this.invalidated;
      }

      public void invalidate() {
         this.invalidated = true;
      }
   }

   static class LatestCacheInfo {
      public int x = Integer.MIN_VALUE;
      public int z = Integer.MIN_VALUE;
      @Nullable
      BlockTintCache.CacheData cache;

      private LatestCacheInfo() {
      }
   }
}
