package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class StoringChunkProgressListener implements ChunkProgressListener {
   private final LoggerChunkProgressListener delegate;
   private final Long2ObjectOpenHashMap<ChunkStatus> statuses;
   private ChunkPos spawnPos = new ChunkPos(0, 0);
   private final int fullDiameter;
   private final int radius;
   private final int diameter;
   private boolean started;

   public StoringChunkProgressListener(int i) {
      this.delegate = new LoggerChunkProgressListener(i);
      this.fullDiameter = i * 2 + 1;
      this.radius = i + ChunkStatus.maxDistance();
      this.diameter = this.radius * 2 + 1;
      this.statuses = new Long2ObjectOpenHashMap<>();
   }

   public void updateSpawnPos(ChunkPos chunkpos) {
      if (this.started) {
         this.delegate.updateSpawnPos(chunkpos);
         this.spawnPos = chunkpos;
      }
   }

   public void onStatusChange(ChunkPos chunkpos, @Nullable ChunkStatus chunkstatus) {
      if (this.started) {
         this.delegate.onStatusChange(chunkpos, chunkstatus);
         if (chunkstatus == null) {
            this.statuses.remove(chunkpos.toLong());
         } else {
            this.statuses.put(chunkpos.toLong(), chunkstatus);
         }

      }
   }

   public void start() {
      this.started = true;
      this.statuses.clear();
      this.delegate.start();
   }

   public void stop() {
      this.started = false;
      this.delegate.stop();
   }

   public int getFullDiameter() {
      return this.fullDiameter;
   }

   public int getDiameter() {
      return this.diameter;
   }

   public int getProgress() {
      return this.delegate.getProgress();
   }

   @Nullable
   public ChunkStatus getStatus(int i, int j) {
      return this.statuses.get(ChunkPos.asLong(i + this.spawnPos.x - this.radius, j + this.spawnPos.z - this.radius));
   }
}
