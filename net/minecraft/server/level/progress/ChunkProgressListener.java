package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ChunkProgressListener {
   void updateSpawnPos(ChunkPos chunkpos);

   void onStatusChange(ChunkPos chunkpos, @Nullable ChunkStatus chunkstatus);

   void start();

   void stop();
}
