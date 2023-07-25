package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements LightChunkGetter, AutoCloseable {
   @Nullable
   public LevelChunk getChunk(int i, int j, boolean flag) {
      return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL, flag);
   }

   @Nullable
   public LevelChunk getChunkNow(int i, int j) {
      return this.getChunk(i, j, false);
   }

   @Nullable
   public LightChunk getChunkForLighting(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.EMPTY, false);
   }

   public boolean hasChunk(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.FULL, false) != null;
   }

   @Nullable
   public abstract ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag);

   public abstract void tick(BooleanSupplier booleansupplier, boolean flag);

   public abstract String gatherStats();

   public abstract int getLoadedChunksCount();

   public void close() throws IOException {
   }

   public abstract LevelLightEngine getLightEngine();

   public void setSpawnSettings(boolean flag, boolean flag1) {
   }

   public void updateChunkForced(ChunkPos chunkpos, boolean flag) {
   }
}
