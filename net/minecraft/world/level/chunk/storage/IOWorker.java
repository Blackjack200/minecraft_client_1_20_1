package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class IOWorker implements ChunkScanAccess, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final AtomicBoolean shutdownRequested = new AtomicBoolean();
   private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
   private final RegionFileStorage storage;
   private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.newLinkedHashMap();
   private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap<>();
   private static final int REGION_CACHE_SIZE = 1024;

   protected IOWorker(Path path, boolean flag, String s) {
      this.storage = new RegionFileStorage(path, flag);
      this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + s);
   }

   public boolean isOldChunkAround(ChunkPos chunkpos, int i) {
      ChunkPos chunkpos1 = new ChunkPos(chunkpos.x - i, chunkpos.z - i);
      ChunkPos chunkpos2 = new ChunkPos(chunkpos.x + i, chunkpos.z + i);

      for(int j = chunkpos1.getRegionX(); j <= chunkpos2.getRegionX(); ++j) {
         for(int k = chunkpos1.getRegionZ(); k <= chunkpos2.getRegionZ(); ++k) {
            BitSet bitset = this.getOrCreateOldDataForRegion(j, k).join();
            if (!bitset.isEmpty()) {
               ChunkPos chunkpos3 = ChunkPos.minFromRegion(j, k);
               int l = Math.max(chunkpos1.x - chunkpos3.x, 0);
               int i1 = Math.max(chunkpos1.z - chunkpos3.z, 0);
               int j1 = Math.min(chunkpos2.x - chunkpos3.x, 31);
               int k1 = Math.min(chunkpos2.z - chunkpos3.z, 31);

               for(int l1 = l; l1 <= j1; ++l1) {
                  for(int i2 = i1; i2 <= k1; ++i2) {
                     int j2 = i2 * 32 + l1;
                     if (bitset.get(j2)) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
      long k = ChunkPos.asLong(i, j);
      synchronized(this.regionCacheForBlender) {
         CompletableFuture<BitSet> completablefuture = this.regionCacheForBlender.getAndMoveToFirst(k);
         if (completablefuture == null) {
            completablefuture = this.createOldDataForRegion(i, j);
            this.regionCacheForBlender.putAndMoveToFirst(k, completablefuture);
            if (this.regionCacheForBlender.size() > 1024) {
               this.regionCacheForBlender.removeLast();
            }
         }

         return completablefuture;
      }
   }

   private CompletableFuture<BitSet> createOldDataForRegion(int i, int j) {
      return CompletableFuture.supplyAsync(() -> {
         ChunkPos chunkpos = ChunkPos.minFromRegion(i, j);
         ChunkPos chunkpos1 = ChunkPos.maxFromRegion(i, j);
         BitSet bitset = new BitSet();
         ChunkPos.rangeClosed(chunkpos, chunkpos1).forEach((chunkpos2) -> {
            CollectFields collectfields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));

            try {
               this.scanChunk(chunkpos2, collectfields).join();
            } catch (Exception var7) {
               LOGGER.warn("Failed to scan chunk {}", chunkpos2, var7);
               return;
            }

            Tag tag = collectfields.getResult();
            if (tag instanceof CompoundTag compoundtag) {
               if (this.isOldChunk(compoundtag)) {
                  int i1 = chunkpos2.getRegionLocalZ() * 32 + chunkpos2.getRegionLocalX();
                  bitset.set(i1);
               }
            }

         });
         return bitset;
      }, Util.backgroundExecutor());
   }

   private boolean isOldChunk(CompoundTag compoundtag) {
      return compoundtag.contains("DataVersion", 99) && compoundtag.getInt("DataVersion") >= 3441 ? compoundtag.contains("blending_data", 10) : true;
   }

   public CompletableFuture<Void> store(ChunkPos chunkpos, @Nullable CompoundTag compoundtag) {
      return this.submitTask(() -> {
         IOWorker.PendingStore ioworker_pendingstore = this.pendingWrites.computeIfAbsent(chunkpos, (chunkpos2) -> new IOWorker.PendingStore(compoundtag));
         ioworker_pendingstore.data = compoundtag;
         return Either.left(ioworker_pendingstore.result);
      }).thenCompose(Function.identity());
   }

   public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkpos) {
      return this.submitTask(() -> {
         IOWorker.PendingStore ioworker_pendingstore = this.pendingWrites.get(chunkpos);
         if (ioworker_pendingstore != null) {
            return Either.left(Optional.ofNullable(ioworker_pendingstore.data));
         } else {
            try {
               CompoundTag compoundtag = this.storage.read(chunkpos);
               return Either.left(Optional.ofNullable(compoundtag));
            } catch (Exception var4) {
               LOGGER.warn("Failed to read chunk {}", chunkpos, var4);
               return Either.right(var4);
            }
         }
      });
   }

   public CompletableFuture<Void> synchronize(boolean flag) {
      CompletableFuture<Void> completablefuture = this.submitTask(() -> Either.left(CompletableFuture.allOf(this.pendingWrites.values().stream().map((ioworker_pendingstore) -> ioworker_pendingstore.result).toArray((i) -> new CompletableFuture[i])))).thenCompose(Function.identity());
      return flag ? completablefuture.thenCompose((ovoid1) -> this.submitTask(() -> {
            try {
               this.storage.flush();
               return Either.left((Void)null);
            } catch (Exception var2) {
               LOGGER.warn("Failed to synchronize chunks", (Throwable)var2);
               return Either.right(var2);
            }
         })) : completablefuture.thenCompose((ovoid) -> this.submitTask(() -> Either.left((Void)null)));
   }

   public CompletableFuture<Void> scanChunk(ChunkPos chunkpos, StreamTagVisitor streamtagvisitor) {
      return this.submitTask(() -> {
         try {
            IOWorker.PendingStore ioworker_pendingstore = this.pendingWrites.get(chunkpos);
            if (ioworker_pendingstore != null) {
               if (ioworker_pendingstore.data != null) {
                  ioworker_pendingstore.data.acceptAsRoot(streamtagvisitor);
               }
            } else {
               this.storage.scanChunk(chunkpos, streamtagvisitor);
            }

            return Either.left((Void)null);
         } catch (Exception var4) {
            LOGGER.warn("Failed to bulk scan chunk {}", chunkpos, var4);
            return Either.right(var4);
         }
      });
   }

   private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier) {
      return this.mailbox.askEither((processorhandle) -> new StrictQueue.IntRunnable(IOWorker.Priority.FOREGROUND.ordinal(), () -> {
            if (!this.shutdownRequested.get()) {
               processorhandle.tell(supplier.get());
            }

            this.tellStorePending();
         }));
   }

   private void storePendingChunk() {
      if (!this.pendingWrites.isEmpty()) {
         Iterator<Map.Entry<ChunkPos, IOWorker.PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
         Map.Entry<ChunkPos, IOWorker.PendingStore> map_entry = iterator.next();
         iterator.remove();
         this.runStore(map_entry.getKey(), map_entry.getValue());
         this.tellStorePending();
      }
   }

   private void tellStorePending() {
      this.mailbox.tell(new StrictQueue.IntRunnable(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
   }

   private void runStore(ChunkPos chunkpos, IOWorker.PendingStore ioworker_pendingstore) {
      try {
         this.storage.write(chunkpos, ioworker_pendingstore.data);
         ioworker_pendingstore.result.complete((Void)null);
      } catch (Exception var4) {
         LOGGER.error("Failed to store chunk {}", chunkpos, var4);
         ioworker_pendingstore.result.completeExceptionally(var4);
      }

   }

   public void close() throws IOException {
      if (this.shutdownRequested.compareAndSet(false, true)) {
         this.mailbox.ask((processorhandle) -> new StrictQueue.IntRunnable(IOWorker.Priority.SHUTDOWN.ordinal(), () -> processorhandle.tell(Unit.INSTANCE))).join();
         this.mailbox.close();

         try {
            this.storage.close();
         } catch (Exception var2) {
            LOGGER.error("Failed to close storage", (Throwable)var2);
         }

      }
   }

   static class PendingStore {
      @Nullable
      CompoundTag data;
      final CompletableFuture<Void> result = new CompletableFuture<>();

      public PendingStore(@Nullable CompoundTag compoundtag) {
         this.data = compoundtag;
      }
   }

   static enum Priority {
      FOREGROUND,
      BACKGROUND,
      SHUTDOWN;
   }
}
