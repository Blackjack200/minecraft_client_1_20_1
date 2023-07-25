package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
   public static final int DEFAULT_BATCH_SIZE = 1000;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ProcessorMailbox<Runnable> taskMailbox;
   private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
   private final ChunkMap chunkMap;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
   private final int taskPerBatch = 1000;
   private final AtomicBoolean scheduled = new AtomicBoolean();

   public ThreadedLevelLightEngine(LightChunkGetter lightchunkgetter, ChunkMap chunkmap, boolean flag, ProcessorMailbox<Runnable> processormailbox, ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> processorhandle) {
      super(lightchunkgetter, true, flag);
      this.chunkMap = chunkmap;
      this.sorterMailbox = processorhandle;
      this.taskMailbox = processormailbox;
   }

   public void close() {
   }

   public int runLightUpdates() {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void checkBlock(BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.immutable();
      this.addTask(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()), ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.checkBlock(blockpos1), () -> "checkBlock " + blockpos1));
   }

   protected void updateChunkStatus(ChunkPos chunkpos) {
      this.addTask(chunkpos.x, chunkpos.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData(chunkpos, false);
         super.setLightEnabled(chunkpos, false);

         for(int i = this.getMinLightSection(); i < this.getMaxLightSection(); ++i) {
            super.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkpos, i), (DataLayer)null);
            super.queueSectionData(LightLayer.SKY, SectionPos.of(chunkpos, i), (DataLayer)null);
         }

         for(int j = this.levelHeightAccessor.getMinSection(); j < this.levelHeightAccessor.getMaxSection(); ++j) {
            super.updateSectionStatus(SectionPos.of(chunkpos, j), true);
         }

      }, () -> "updateChunkStatus " + chunkpos + " true"));
   }

   public void updateSectionStatus(SectionPos sectionpos, boolean flag) {
      this.addTask(sectionpos.x(), sectionpos.z(), () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.updateSectionStatus(sectionpos, flag), () -> "updateSectionStatus " + sectionpos + " " + flag));
   }

   public void propagateLightSources(ChunkPos chunkpos) {
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.propagateLightSources(chunkpos), () -> "propagateLight " + chunkpos));
   }

   public void setLightEnabled(ChunkPos chunkpos, boolean flag) {
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.setLightEnabled(chunkpos, flag), () -> "enableLight " + chunkpos + " " + flag));
   }

   public void queueSectionData(LightLayer lightlayer, SectionPos sectionpos, @Nullable DataLayer datalayer) {
      this.addTask(sectionpos.x(), sectionpos.z(), () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.queueSectionData(lightlayer, sectionpos, datalayer), () -> "queueData " + sectionpos));
   }

   private void addTask(int i, int j, ThreadedLevelLightEngine.TaskType threadedlevellightengine_tasktype, Runnable runnable) {
      this.addTask(i, j, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(i, j)), threadedlevellightengine_tasktype, runnable);
   }

   private void addTask(int i, int j, IntSupplier intsupplier, ThreadedLevelLightEngine.TaskType threadedlevellightengine_tasktype, Runnable runnable) {
      this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
         this.lightTasks.add(Pair.of(threadedlevellightengine_tasktype, runnable));
         if (this.lightTasks.size() >= 1000) {
            this.runUpdate();
         }

      }, ChunkPos.asLong(i, j), intsupplier));
   }

   public void retainData(ChunkPos chunkpos, boolean flag) {
      this.addTask(chunkpos.x, chunkpos.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.retainData(chunkpos, flag), () -> "retainData " + chunkpos));
   }

   public CompletableFuture<ChunkAccess> initializeLight(ChunkAccess chunkaccess, boolean flag) {
      ChunkPos chunkpos = chunkaccess.getPos();
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         LevelChunkSection[] alevelchunksection = chunkaccess.getSections();

         for(int i = 0; i < chunkaccess.getSectionsCount(); ++i) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            if (!levelchunksection.hasOnlyAir()) {
               int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
               super.updateSectionStatus(SectionPos.of(chunkpos, j), false);
            }
         }

      }, () -> "initializeLight: " + chunkpos));
      return CompletableFuture.supplyAsync(() -> {
         super.setLightEnabled(chunkpos, flag);
         super.retainData(chunkpos, false);
         return chunkaccess;
      }, (runnable) -> this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, runnable));
   }

   public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunkaccess, boolean flag) {
      ChunkPos chunkpos = chunkaccess.getPos();
      chunkaccess.setLightCorrect(false);
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         if (!flag) {
            super.propagateLightSources(chunkpos);
         }

      }, () -> "lightChunk " + chunkpos + " " + flag));
      return CompletableFuture.supplyAsync(() -> {
         chunkaccess.setLightCorrect(true);
         this.chunkMap.releaseLightTicket(chunkpos);
         return chunkaccess;
      }, (runnable) -> this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, runnable));
   }

   public void tryScheduleUpdate() {
      if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
         this.taskMailbox.tell(() -> {
            this.runUpdate();
            this.scheduled.set(false);
         });
      }

   }

   private void runUpdate() {
      int i = Math.min(this.lightTasks.size(), 1000);
      ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> objectlistiterator = this.lightTasks.iterator();

      int j;
      for(j = 0; objectlistiterator.hasNext() && j < i; ++j) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair = objectlistiterator.next();
         if (pair.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
            pair.getSecond().run();
         }
      }

      objectlistiterator.back(j);
      super.runLightUpdates();

      for(int var5 = 0; objectlistiterator.hasNext() && var5 < i; ++var5) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair1 = objectlistiterator.next();
         if (pair1.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
            pair1.getSecond().run();
         }

         objectlistiterator.remove();
      }

   }

   static enum TaskType {
      PRE_UPDATE,
      POST_UPDATE;
   }
}
