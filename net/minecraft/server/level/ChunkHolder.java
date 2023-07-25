package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.util.DebugBuffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ChunkHolder {
   public static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   public static final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
   public static final Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   private static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> NOT_DONE_YET = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   private static final CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray<>(CHUNK_STATUSES.size());
   private final LevelHeightAccessor levelHeightAccessor;
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture((ChunkAccess)null);
   @Nullable
   private final DebugBuffer<ChunkHolder.ChunkSaveDebug> chunkToSaveHistory = null;
   private int oldTicketLevel;
   private int ticketLevel;
   private int queueLevel;
   final ChunkPos pos;
   private boolean hasChangedSections;
   private final ShortSet[] changedBlocksPerSection;
   private final BitSet blockChangedLightSectionFilter = new BitSet();
   private final BitSet skyChangedLightSectionFilter = new BitSet();
   private final LevelLightEngine lightEngine;
   private final ChunkHolder.LevelChangeListener onLevelChange;
   private final ChunkHolder.PlayerProvider playerProvider;
   private boolean wasAccessibleSinceLastSave;
   private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture((Void)null);

   public ChunkHolder(ChunkPos chunkpos, int i, LevelHeightAccessor levelheightaccessor, LevelLightEngine levellightengine, ChunkHolder.LevelChangeListener chunkholder_levelchangelistener, ChunkHolder.PlayerProvider chunkholder_playerprovider) {
      this.pos = chunkpos;
      this.levelHeightAccessor = levelheightaccessor;
      this.lightEngine = levellightengine;
      this.onLevelChange = chunkholder_levelchangelistener;
      this.playerProvider = chunkholder_playerprovider;
      this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
      this.ticketLevel = this.oldTicketLevel;
      this.queueLevel = this.oldTicketLevel;
      this.setTicketLevel(i);
      this.changedBlocksPerSection = new ShortSet[levelheightaccessor.getSectionsCount()];
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus chunkstatus) {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(chunkstatus.getIndex());
      return completablefuture == null ? UNLOADED_CHUNK_FUTURE : completablefuture;
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus chunkstatus) {
      return ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkstatus) ? this.getFutureIfPresentUnchecked(chunkstatus) : UNLOADED_CHUNK_FUTURE;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture() {
      return this.tickingChunkFuture;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getEntityTickingChunkFuture() {
      return this.entityTickingChunkFuture;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getFullChunkFuture() {
      return this.fullChunkFuture;
   }

   @Nullable
   public LevelChunk getTickingChunk() {
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getTickingChunkFuture();
      Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
      return either == null ? null : either.left().orElse((LevelChunk)null);
   }

   @Nullable
   public LevelChunk getFullChunk() {
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFullChunkFuture();
      Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
      return either == null ? null : either.left().orElse((LevelChunk)null);
   }

   @Nullable
   public ChunkStatus getLastAvailableStatus() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkstatus = CHUNK_STATUSES.get(i);
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);
         if (completablefuture.getNow(UNLOADED_CHUNK).left().isPresent()) {
            return chunkstatus;
         }
      }

      return null;
   }

   @Nullable
   public ChunkAccess getLastAvailable() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkstatus = CHUNK_STATUSES.get(i);
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);
         if (!completablefuture.isCompletedExceptionally()) {
            Optional<ChunkAccess> optional = completablefuture.getNow(UNLOADED_CHUNK).left();
            if (optional.isPresent()) {
               return optional.get();
            }
         }
      }

      return null;
   }

   public CompletableFuture<ChunkAccess> getChunkToSave() {
      return this.chunkToSave;
   }

   public void blockChanged(BlockPos blockpos) {
      LevelChunk levelchunk = this.getTickingChunk();
      if (levelchunk != null) {
         int i = this.levelHeightAccessor.getSectionIndex(blockpos.getY());
         if (this.changedBlocksPerSection[i] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[i] = new ShortOpenHashSet();
         }

         this.changedBlocksPerSection[i].add(SectionPos.sectionRelativePos(blockpos));
      }
   }

   public void sectionLightChanged(LightLayer lightlayer, int i) {
      Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = this.getFutureIfPresent(ChunkStatus.INITIALIZE_LIGHT).getNow((Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)null);
      if (either != null) {
         ChunkAccess chunkaccess = either.left().orElse((ChunkAccess)null);
         if (chunkaccess != null) {
            chunkaccess.setUnsaved(true);
            LevelChunk levelchunk = this.getTickingChunk();
            if (levelchunk != null) {
               int j = this.lightEngine.getMinLightSection();
               int k = this.lightEngine.getMaxLightSection();
               if (i >= j && i <= k) {
                  int l = i - j;
                  if (lightlayer == LightLayer.SKY) {
                     this.skyChangedLightSectionFilter.set(l);
                  } else {
                     this.blockChangedLightSectionFilter.set(l);
                  }

               }
            }
         }
      }
   }

   public void broadcastChanges(LevelChunk levelchunk) {
      if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
         Level level = levelchunk.getLevel();
         if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            List<ServerPlayer> list = this.playerProvider.getPlayers(this.pos, true);
            if (!list.isEmpty()) {
               ClientboundLightUpdatePacket clientboundlightupdatepacket = new ClientboundLightUpdatePacket(levelchunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);
               this.broadcast(list, clientboundlightupdatepacket);
            }

            this.skyChangedLightSectionFilter.clear();
            this.blockChangedLightSectionFilter.clear();
         }

         if (this.hasChangedSections) {
            List<ServerPlayer> list1 = this.playerProvider.getPlayers(this.pos, false);

            for(int i = 0; i < this.changedBlocksPerSection.length; ++i) {
               ShortSet shortset = this.changedBlocksPerSection[i];
               if (shortset != null) {
                  this.changedBlocksPerSection[i] = null;
                  if (!list1.isEmpty()) {
                     int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                     SectionPos sectionpos = SectionPos.of(levelchunk.getPos(), j);
                     if (shortset.size() == 1) {
                        BlockPos blockpos = sectionpos.relativeToBlockPos(shortset.iterator().nextShort());
                        BlockState blockstate = level.getBlockState(blockpos);
                        this.broadcast(list1, new ClientboundBlockUpdatePacket(blockpos, blockstate));
                        this.broadcastBlockEntityIfNeeded(list1, level, blockpos, blockstate);
                     } else {
                        LevelChunkSection levelchunksection = levelchunk.getSection(i);
                        ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket = new ClientboundSectionBlocksUpdatePacket(sectionpos, shortset, levelchunksection);
                        this.broadcast(list1, clientboundsectionblocksupdatepacket);
                        clientboundsectionblocksupdatepacket.runUpdates((blockpos1, blockstate1) -> this.broadcastBlockEntityIfNeeded(list1, level, blockpos1, blockstate1));
                     }
                  }
               }
            }

            this.hasChangedSections = false;
         }
      }
   }

   private void broadcastBlockEntityIfNeeded(List<ServerPlayer> list, Level level, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.hasBlockEntity()) {
         this.broadcastBlockEntity(list, level, blockpos);
      }

   }

   private void broadcastBlockEntity(List<ServerPlayer> list, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity != null) {
         Packet<?> packet = blockentity.getUpdatePacket();
         if (packet != null) {
            this.broadcast(list, packet);
         }
      }

   }

   private void broadcast(List<ServerPlayer> list, Packet<?> packet) {
      list.forEach((serverplayer) -> serverplayer.connection.send(packet));
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus chunkstatus, ChunkMap chunkmap) {
      int i = chunkstatus.getIndex();
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
      if (completablefuture != null) {
         Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow(NOT_DONE_YET);
         if (either == null) {
            String s = "value in future for status: " + chunkstatus + " was incorrectly set to null at chunk: " + this.pos;
            throw chunkmap.debugFuturesAndCreateReportedException(new IllegalStateException("null value previously set for chunk status"), s);
         }

         if (either == NOT_DONE_YET || either.right().isEmpty()) {
            return completablefuture;
         }
      }

      if (ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkstatus)) {
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = chunkmap.schedule(this, chunkstatus);
         this.updateChunkToSave(completablefuture1, "schedule " + chunkstatus);
         this.futures.set(i, completablefuture1);
         return completablefuture1;
      } else {
         return completablefuture == null ? UNLOADED_CHUNK_FUTURE : completablefuture;
      }
   }

   protected void addSaveDependency(String s, CompletableFuture<?> completablefuture) {
      if (this.chunkToSaveHistory != null) {
         this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), completablefuture, s));
      }

      this.chunkToSave = this.chunkToSave.thenCombine(completablefuture, (chunkaccess, object) -> chunkaccess);
   }

   private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture, String s) {
      if (this.chunkToSaveHistory != null) {
         this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), completablefuture, s));
      }

      this.chunkToSave = this.chunkToSave.thenCombine(completablefuture, (chunkaccess, either) -> either.map((chunkaccess2) -> chunkaccess2, (chunkholder_chunkloadingfailure) -> chunkaccess));
   }

   public FullChunkStatus getFullStatus() {
      return ChunkLevel.fullStatus(this.ticketLevel);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public int getTicketLevel() {
      return this.ticketLevel;
   }

   public int getQueueLevel() {
      return this.queueLevel;
   }

   private void setQueueLevel(int i) {
      this.queueLevel = i;
   }

   public void setTicketLevel(int i) {
      this.ticketLevel = i;
   }

   private void scheduleFullChunkPromotion(ChunkMap chunkmap, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture, Executor executor, FullChunkStatus fullchunkstatus) {
      this.pendingFullStateConfirmation.cancel(false);
      CompletableFuture<Void> completablefuture1 = new CompletableFuture<>();
      completablefuture1.thenRunAsync(() -> chunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus), executor);
      this.pendingFullStateConfirmation = completablefuture1;
      completablefuture.thenAccept((either) -> either.ifLeft((levelchunk) -> completablefuture1.complete((Void)null)));
   }

   private void demoteFullChunk(ChunkMap chunkmap, FullChunkStatus fullchunkstatus) {
      this.pendingFullStateConfirmation.cancel(false);
      chunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus);
   }

   protected void updateFutures(ChunkMap chunkmap, Executor executor) {
      ChunkStatus chunkstatus = ChunkLevel.generationStatus(this.oldTicketLevel);
      ChunkStatus chunkstatus1 = ChunkLevel.generationStatus(this.ticketLevel);
      boolean flag = ChunkLevel.isLoaded(this.oldTicketLevel);
      boolean flag1 = ChunkLevel.isLoaded(this.ticketLevel);
      FullChunkStatus fullchunkstatus = ChunkLevel.fullStatus(this.oldTicketLevel);
      FullChunkStatus fullchunkstatus1 = ChunkLevel.fullStatus(this.ticketLevel);
      if (flag) {
         Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = Either.right(new ChunkHolder.ChunkLoadingFailure() {
            public String toString() {
               return "Unloaded ticket level " + ChunkHolder.this.pos;
            }
         });

         for(int i = flag1 ? chunkstatus1.getIndex() + 1 : 0; i <= chunkstatus.getIndex(); ++i) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
            if (completablefuture == null) {
               this.futures.set(i, CompletableFuture.completedFuture(either));
            }
         }
      }

      boolean flag2 = fullchunkstatus.isOrAfter(FullChunkStatus.FULL);
      boolean flag3 = fullchunkstatus1.isOrAfter(FullChunkStatus.FULL);
      this.wasAccessibleSinceLastSave |= flag3;
      if (!flag2 && flag3) {
         this.fullChunkFuture = chunkmap.prepareAccessibleChunk(this);
         this.scheduleFullChunkPromotion(chunkmap, this.fullChunkFuture, executor, FullChunkStatus.FULL);
         this.updateChunkToSave(this.fullChunkFuture, "full");
      }

      if (flag2 && !flag3) {
         this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      boolean flag4 = fullchunkstatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
      boolean flag5 = fullchunkstatus1.isOrAfter(FullChunkStatus.BLOCK_TICKING);
      if (!flag4 && flag5) {
         this.tickingChunkFuture = chunkmap.prepareTickingChunk(this);
         this.scheduleFullChunkPromotion(chunkmap, this.tickingChunkFuture, executor, FullChunkStatus.BLOCK_TICKING);
         this.updateChunkToSave(this.tickingChunkFuture, "ticking");
      }

      if (flag4 && !flag5) {
         this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      boolean flag6 = fullchunkstatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
      boolean flag7 = fullchunkstatus1.isOrAfter(FullChunkStatus.ENTITY_TICKING);
      if (!flag6 && flag7) {
         if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
         }

         this.entityTickingChunkFuture = chunkmap.prepareEntityTickingChunk(this);
         this.scheduleFullChunkPromotion(chunkmap, this.entityTickingChunkFuture, executor, FullChunkStatus.ENTITY_TICKING);
         this.updateChunkToSave(this.entityTickingChunkFuture, "entity ticking");
      }

      if (flag6 && !flag7) {
         this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      if (!fullchunkstatus1.isOrAfter(fullchunkstatus)) {
         this.demoteFullChunk(chunkmap, fullchunkstatus1);
      }

      this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
      this.oldTicketLevel = this.ticketLevel;
   }

   public boolean wasAccessibleSinceLastSave() {
      return this.wasAccessibleSinceLastSave;
   }

   public void refreshAccessibility() {
      this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
   }

   public void replaceProtoChunk(ImposterProtoChunk imposterprotochunk) {
      for(int i = 0; i < this.futures.length(); ++i) {
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
         if (completablefuture != null) {
            Optional<ChunkAccess> optional = completablefuture.getNow(UNLOADED_CHUNK).left();
            if (!optional.isEmpty() && optional.get() instanceof ProtoChunk) {
               this.futures.set(i, CompletableFuture.completedFuture(Either.left(imposterprotochunk)));
            }
         }
      }

      this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(imposterprotochunk.getWrapped())), "replaceProto");
   }

   public List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> getAllFutures() {
      List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> list = new ArrayList<>();

      for(int i = 0; i < CHUNK_STATUSES.size(); ++i) {
         list.add(Pair.of(CHUNK_STATUSES.get(i), this.futures.get(i)));
      }

      return list;
   }

   public interface ChunkLoadingFailure {
      ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
         public String toString() {
            return "UNLOADED";
         }
      };
   }

   static final class ChunkSaveDebug {
      private final Thread thread;
      private final CompletableFuture<?> future;
      private final String source;

      ChunkSaveDebug(Thread thread, CompletableFuture<?> completablefuture, String s) {
         this.thread = thread;
         this.future = completablefuture;
         this.source = s;
      }
   }

   @FunctionalInterface
   public interface LevelChangeListener {
      void onLevelChange(ChunkPos chunkpos, IntSupplier intsupplier, int i, IntConsumer intconsumer);
   }

   public interface PlayerProvider {
      List<ServerPlayer> getPlayers(ChunkPos chunkpos, boolean flag);
   }
}
