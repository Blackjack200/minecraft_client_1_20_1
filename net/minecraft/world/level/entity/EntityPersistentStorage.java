package net.minecraft.world.level.entity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.ChunkPos;

public interface EntityPersistentStorage<T> extends AutoCloseable {
   CompletableFuture<ChunkEntities<T>> loadEntities(ChunkPos chunkpos);

   void storeEntities(ChunkEntities<T> chunkentities);

   void flush(boolean flag);

   default void close() throws IOException {
   }
}
