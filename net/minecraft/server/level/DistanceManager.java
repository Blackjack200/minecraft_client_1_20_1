package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public abstract class DistanceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   static final int PLAYER_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
   private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
   final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap<>();
   final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();
   private final DistanceManager.ChunkTicketTracker ticketTracker = new DistanceManager.ChunkTicketTracker();
   private final DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new DistanceManager.FixedPlayerDistanceChunkTracker(8);
   private final TickingTracker tickingTicketsTracker = new TickingTracker();
   private final DistanceManager.PlayerTicketTracker playerTicketManager = new DistanceManager.PlayerTicketTracker(32);
   final Set<ChunkHolder> chunksToUpdateFutures = Sets.newHashSet();
   final ChunkTaskPriorityQueueSorter ticketThrottler;
   final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
   final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
   final LongSet ticketsToRelease = new LongOpenHashSet();
   final Executor mainThreadExecutor;
   private long ticketTickCounter;
   private int simulationDistance = 10;

   protected DistanceManager(Executor executor, Executor executor1) {
      ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("player ticket throttler", executor1::execute);
      ChunkTaskPriorityQueueSorter chunktaskpriorityqueuesorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorhandle), executor, 4);
      this.ticketThrottler = chunktaskpriorityqueuesorter;
      this.ticketThrottlerInput = chunktaskpriorityqueuesorter.getProcessor(processorhandle, true);
      this.ticketThrottlerReleaser = chunktaskpriorityqueuesorter.getReleaseProcessor(processorhandle);
      this.mainThreadExecutor = executor1;
   }

   protected void purgeStaleTickets() {
      ++this.ticketTickCounter;
      ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> long2objectmap_entry = objectiterator.next();
         Iterator<Ticket<?>> iterator = long2objectmap_entry.getValue().iterator();
         boolean flag = false;

         while(iterator.hasNext()) {
            Ticket<?> ticket = iterator.next();
            if (ticket.timedOut(this.ticketTickCounter)) {
               iterator.remove();
               flag = true;
               this.tickingTicketsTracker.removeTicket(long2objectmap_entry.getLongKey(), ticket);
            }
         }

         if (flag) {
            this.ticketTracker.update(long2objectmap_entry.getLongKey(), getTicketLevelAt(long2objectmap_entry.getValue()), false);
         }

         if (long2objectmap_entry.getValue().isEmpty()) {
            objectiterator.remove();
         }
      }

   }

   private static int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedarrayset) {
      return !sortedarrayset.isEmpty() ? sortedarrayset.first().getTicketLevel() : ChunkLevel.MAX_LEVEL + 1;
   }

   protected abstract boolean isChunkToRemove(long i);

   @Nullable
   protected abstract ChunkHolder getChunk(long i);

   @Nullable
   protected abstract ChunkHolder updateChunkScheduling(long i, int j, @Nullable ChunkHolder chunkholder, int k);

   public boolean runAllUpdates(ChunkMap chunkmap) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      this.tickingTicketsTracker.runAllUpdates();
      this.playerTicketManager.runAllUpdates();
      int i = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
      boolean flag = i != 0;
      if (flag) {
      }

      if (!this.chunksToUpdateFutures.isEmpty()) {
         this.chunksToUpdateFutures.forEach((chunkholder1) -> chunkholder1.updateFutures(chunkmap, this.mainThreadExecutor));
         this.chunksToUpdateFutures.clear();
         return true;
      } else {
         if (!this.ticketsToRelease.isEmpty()) {
            LongIterator longiterator = this.ticketsToRelease.iterator();

            while(longiterator.hasNext()) {
               long j = longiterator.nextLong();
               if (this.getTickets(j).stream().anyMatch((ticket) -> ticket.getType() == TicketType.PLAYER)) {
                  ChunkHolder chunkholder = chunkmap.getUpdatingChunkIfPresent(j);
                  if (chunkholder == null) {
                     throw new IllegalStateException();
                  }

                  CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder.getEntityTickingChunkFuture();
                  completablefuture.thenAccept((either) -> this.mainThreadExecutor.execute(() -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, j, false))));
               }
            }

            this.ticketsToRelease.clear();
         }

         return flag;
      }
   }

   void addTicket(long i, Ticket<?> ticket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(i);
      int j = getTicketLevelAt(sortedarrayset);
      Ticket<?> ticket1 = sortedarrayset.addOrGet(ticket);
      ticket1.setCreatedTick(this.ticketTickCounter);
      if (ticket.getTicketLevel() < j) {
         this.ticketTracker.update(i, ticket.getTicketLevel(), true);
      }

   }

   void removeTicket(long i, Ticket<?> ticket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(i);
      if (sortedarrayset.remove(ticket)) {
      }

      if (sortedarrayset.isEmpty()) {
         this.tickets.remove(i);
      }

      this.ticketTracker.update(i, getTicketLevelAt(sortedarrayset), false);
   }

   public <T> void addTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      this.addTicket(chunkpos.toLong(), new Ticket<>(tickettype, i, object));
   }

   public <T> void removeTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      Ticket<T> ticket = new Ticket<>(tickettype, i, object);
      this.removeTicket(chunkpos.toLong(), ticket);
   }

   public <T> void addRegionTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      Ticket<T> ticket = new Ticket<>(tickettype, ChunkLevel.byStatus(FullChunkStatus.FULL) - i, object);
      long j = chunkpos.toLong();
      this.addTicket(j, ticket);
      this.tickingTicketsTracker.addTicket(j, ticket);
   }

   public <T> void removeRegionTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      Ticket<T> ticket = new Ticket<>(tickettype, ChunkLevel.byStatus(FullChunkStatus.FULL) - i, object);
      long j = chunkpos.toLong();
      this.removeTicket(j, ticket);
      this.tickingTicketsTracker.removeTicket(j, ticket);
   }

   private SortedArraySet<Ticket<?>> getTickets(long i) {
      return this.tickets.computeIfAbsent(i, (j) -> SortedArraySet.create(4));
   }

   protected void updateChunkForced(ChunkPos chunkpos, boolean flag) {
      Ticket<ChunkPos> ticket = new Ticket<>(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL, chunkpos);
      long i = chunkpos.toLong();
      if (flag) {
         this.addTicket(i, ticket);
         this.tickingTicketsTracker.addTicket(i, ticket);
      } else {
         this.removeTicket(i, ticket);
         this.tickingTicketsTracker.removeTicket(i, ticket);
      }

   }

   public void addPlayer(SectionPos sectionpos, ServerPlayer serverplayer) {
      ChunkPos chunkpos = sectionpos.chunk();
      long i = chunkpos.toLong();
      this.playersPerChunk.computeIfAbsent(i, (j) -> new ObjectOpenHashSet()).add(serverplayer);
      this.naturalSpawnChunkCounter.update(i, 0, true);
      this.playerTicketManager.update(i, 0, true);
      this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkpos, this.getPlayerTicketLevel(), chunkpos);
   }

   public void removePlayer(SectionPos sectionpos, ServerPlayer serverplayer) {
      ChunkPos chunkpos = sectionpos.chunk();
      long i = chunkpos.toLong();
      ObjectSet<ServerPlayer> objectset = this.playersPerChunk.get(i);
      objectset.remove(serverplayer);
      if (objectset.isEmpty()) {
         this.playersPerChunk.remove(i);
         this.naturalSpawnChunkCounter.update(i, Integer.MAX_VALUE, false);
         this.playerTicketManager.update(i, Integer.MAX_VALUE, false);
         this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkpos, this.getPlayerTicketLevel(), chunkpos);
      }

   }

   private int getPlayerTicketLevel() {
      return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.simulationDistance);
   }

   public boolean inEntityTickingRange(long i) {
      return ChunkLevel.isEntityTicking(this.tickingTicketsTracker.getLevel(i));
   }

   public boolean inBlockTickingRange(long i) {
      return ChunkLevel.isBlockTicking(this.tickingTicketsTracker.getLevel(i));
   }

   protected String getTicketDebugString(long i) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(i);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().toString() : "no_ticket";
   }

   protected void updatePlayerTickets(int i) {
      this.playerTicketManager.updateViewDistance(i);
   }

   public void updateSimulationDistance(int i) {
      if (i != this.simulationDistance) {
         this.simulationDistance = i;
         this.tickingTicketsTracker.replacePlayerTicketsLevel(this.getPlayerTicketLevel());
      }

   }

   public int getNaturalSpawnChunkCount() {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.size();
   }

   public boolean hasPlayersNearby(long i) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.containsKey(i);
   }

   public String getDebugStatus() {
      return this.ticketThrottler.getDebugStatus();
   }

   private void dumpTickets(String s) {
      try {
         FileOutputStream fileoutputstream = new FileOutputStream(new File(s));

         try {
            for(Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> long2objectmap_entry : this.tickets.long2ObjectEntrySet()) {
               ChunkPos chunkpos = new ChunkPos(long2objectmap_entry.getLongKey());

               for(Ticket<?> ticket : long2objectmap_entry.getValue()) {
                  fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + ticket.getType() + "\t" + ticket.getTicketLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
               }
            }
         } catch (Throwable var9) {
            try {
               fileoutputstream.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         fileoutputstream.close();
      } catch (IOException var10) {
         LOGGER.error("Failed to dump tickets to {}", s, var10);
      }

   }

   @VisibleForTesting
   TickingTracker tickingTracker() {
      return this.tickingTicketsTracker;
   }

   public void removeTicketsOnClosing() {
      ImmutableSet<TicketType<?>> immutableset = ImmutableSet.of(TicketType.UNKNOWN, TicketType.POST_TELEPORT, TicketType.LIGHT);
      ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> long2objectmap_entry = objectiterator.next();
         Iterator<Ticket<?>> iterator = long2objectmap_entry.getValue().iterator();
         boolean flag = false;

         while(iterator.hasNext()) {
            Ticket<?> ticket = iterator.next();
            if (!immutableset.contains(ticket.getType())) {
               iterator.remove();
               flag = true;
               this.tickingTicketsTracker.removeTicket(long2objectmap_entry.getLongKey(), ticket);
            }
         }

         if (flag) {
            this.ticketTracker.update(long2objectmap_entry.getLongKey(), getTicketLevelAt(long2objectmap_entry.getValue()), false);
         }

         if (long2objectmap_entry.getValue().isEmpty()) {
            objectiterator.remove();
         }
      }

   }

   public boolean hasTickets() {
      return !this.tickets.isEmpty();
   }

   class ChunkTicketTracker extends ChunkTracker {
      private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;

      public ChunkTicketTracker() {
         super(MAX_LEVEL + 1, 16, 256);
      }

      protected int getLevelFromSource(long i) {
         SortedArraySet<Ticket<?>> sortedarrayset = DistanceManager.this.tickets.get(i);
         if (sortedarrayset == null) {
            return Integer.MAX_VALUE;
         } else {
            return sortedarrayset.isEmpty() ? Integer.MAX_VALUE : sortedarrayset.first().getTicketLevel();
         }
      }

      protected int getLevel(long i) {
         if (!DistanceManager.this.isChunkToRemove(i)) {
            ChunkHolder chunkholder = DistanceManager.this.getChunk(i);
            if (chunkholder != null) {
               return chunkholder.getTicketLevel();
            }
         }

         return MAX_LEVEL;
      }

      protected void setLevel(long i, int j) {
         ChunkHolder chunkholder = DistanceManager.this.getChunk(i);
         int k = chunkholder == null ? MAX_LEVEL : chunkholder.getTicketLevel();
         if (k != j) {
            chunkholder = DistanceManager.this.updateChunkScheduling(i, j, chunkholder, k);
            if (chunkholder != null) {
               DistanceManager.this.chunksToUpdateFutures.add(chunkholder);
            }

         }
      }

      public int runDistanceUpdates(int i) {
         return this.runUpdates(i);
      }
   }

   class FixedPlayerDistanceChunkTracker extends ChunkTracker {
      protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
      protected final int maxDistance;

      protected FixedPlayerDistanceChunkTracker(int i) {
         super(i + 2, 16, 256);
         this.maxDistance = i;
         this.chunks.defaultReturnValue((byte)(i + 2));
      }

      protected int getLevel(long i) {
         return this.chunks.get(i);
      }

      protected void setLevel(long i, int j) {
         byte b0;
         if (j > this.maxDistance) {
            b0 = this.chunks.remove(i);
         } else {
            b0 = this.chunks.put(i, (byte)j);
         }

         this.onLevelChange(i, b0, j);
      }

      protected void onLevelChange(long i, int j, int k) {
      }

      protected int getLevelFromSource(long i) {
         return this.havePlayer(i) ? 0 : Integer.MAX_VALUE;
      }

      private boolean havePlayer(long i) {
         ObjectSet<ServerPlayer> objectset = DistanceManager.this.playersPerChunk.get(i);
         return objectset != null && !objectset.isEmpty();
      }

      public void runAllUpdates() {
         this.runUpdates(Integer.MAX_VALUE);
      }

      private void dumpChunks(String s) {
         try {
            FileOutputStream fileoutputstream = new FileOutputStream(new File(s));

            try {
               for(Long2ByteMap.Entry long2bytemap_entry : this.chunks.long2ByteEntrySet()) {
                  ChunkPos chunkpos = new ChunkPos(long2bytemap_entry.getLongKey());
                  String s1 = Byte.toString(long2bytemap_entry.getByteValue());
                  fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + s1 + "\n").getBytes(StandardCharsets.UTF_8));
               }
            } catch (Throwable var8) {
               try {
                  fileoutputstream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            fileoutputstream.close();
         } catch (IOException var9) {
            DistanceManager.LOGGER.error("Failed to dump chunks to {}", s, var9);
         }

      }
   }

   class PlayerTicketTracker extends DistanceManager.FixedPlayerDistanceChunkTracker {
      private int viewDistance;
      private final Long2IntMap queueLevels = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
      private final LongSet toUpdate = new LongOpenHashSet();

      protected PlayerTicketTracker(int i) {
         super(i);
         this.viewDistance = 0;
         this.queueLevels.defaultReturnValue(i + 2);
      }

      protected void onLevelChange(long i, int j, int k) {
         this.toUpdate.add(i);
      }

      public void updateViewDistance(int i) {
         for(Long2ByteMap.Entry long2bytemap_entry : this.chunks.long2ByteEntrySet()) {
            byte b0 = long2bytemap_entry.getByteValue();
            long j = long2bytemap_entry.getLongKey();
            this.onLevelChange(j, b0, this.haveTicketFor(b0), b0 <= i);
         }

         this.viewDistance = i;
      }

      private void onLevelChange(long i, int j, boolean flag, boolean flag1) {
         if (flag != flag1) {
            Ticket<?> ticket = new Ticket<>(TicketType.PLAYER, DistanceManager.PLAYER_TICKET_LEVEL, new ChunkPos(i));
            if (flag1) {
               DistanceManager.this.ticketThrottlerInput.tell(ChunkTaskPriorityQueueSorter.message(() -> DistanceManager.this.mainThreadExecutor.execute(() -> {
                     if (this.haveTicketFor(this.getLevel(i))) {
                        DistanceManager.this.addTicket(i, ticket);
                        DistanceManager.this.ticketsToRelease.add(i);
                     } else {
                        DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, i, false));
                     }

                  }), i, () -> j));
            } else {
               DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> DistanceManager.this.mainThreadExecutor.execute(() -> DistanceManager.this.removeTicket(i, ticket)), i, true));
            }
         }

      }

      public void runAllUpdates() {
         super.runAllUpdates();
         if (!this.toUpdate.isEmpty()) {
            LongIterator longiterator = this.toUpdate.iterator();

            while(longiterator.hasNext()) {
               long i = longiterator.nextLong();
               int j = this.queueLevels.get(i);
               int k = this.getLevel(i);
               if (j != k) {
                  DistanceManager.this.ticketThrottler.onLevelChange(new ChunkPos(i), () -> this.queueLevels.get(i), k, (i1) -> {
                     if (i1 >= this.queueLevels.defaultReturnValue()) {
                        this.queueLevels.remove(i);
                     } else {
                        this.queueLevels.put(i, i1);
                     }

                  });
                  this.onLevelChange(i, k, this.haveTicketFor(j), this.haveTicketFor(k));
               }
            }

            this.toUpdate.clear();
         }

      }

      private boolean haveTicketFor(int i) {
         return i <= this.viewDistance;
      }
   }
}
