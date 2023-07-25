package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ENTITIES_TAG = "Entities";
   private static final String POSITION_TAG = "Position";
   private final ServerLevel level;
   private final IOWorker worker;
   private final LongSet emptyChunks = new LongOpenHashSet();
   private final ProcessorMailbox<Runnable> entityDeserializerQueue;
   protected final DataFixer fixerUpper;

   public EntityStorage(ServerLevel serverlevel, Path path, DataFixer datafixer, boolean flag, Executor executor) {
      this.level = serverlevel;
      this.fixerUpper = datafixer;
      this.entityDeserializerQueue = ProcessorMailbox.create(executor, "entity-deserializer");
      this.worker = new IOWorker(path, flag, "entities");
   }

   public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkpos) {
      return this.emptyChunks.contains(chunkpos.toLong()) ? CompletableFuture.completedFuture(emptyChunk(chunkpos)) : this.worker.loadAsync(chunkpos).thenApplyAsync((optional) -> {
         if (optional.isEmpty()) {
            this.emptyChunks.add(chunkpos.toLong());
            return emptyChunk(chunkpos);
         } else {
            try {
               ChunkPos chunkpos2 = readChunkPos(optional.get());
               if (!Objects.equals(chunkpos, chunkpos2)) {
                  LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", chunkpos, chunkpos, chunkpos2);
               }
            } catch (Exception var6) {
               LOGGER.warn("Failed to parse chunk {} position info", chunkpos, var6);
            }

            CompoundTag compoundtag = this.upgradeChunkTag(optional.get());
            ListTag listtag = compoundtag.getList("Entities", 10);
            List<Entity> list = EntityType.loadEntitiesRecursive(listtag, this.level).collect(ImmutableList.toImmutableList());
            return new ChunkEntities<>(chunkpos, list);
         }
      }, this.entityDeserializerQueue::tell);
   }

   private static ChunkPos readChunkPos(CompoundTag compoundtag) {
      int[] aint = compoundtag.getIntArray("Position");
      return new ChunkPos(aint[0], aint[1]);
   }

   private static void writeChunkPos(CompoundTag compoundtag, ChunkPos chunkpos) {
      compoundtag.put("Position", new IntArrayTag(new int[]{chunkpos.x, chunkpos.z}));
   }

   private static ChunkEntities<Entity> emptyChunk(ChunkPos chunkpos) {
      return new ChunkEntities<>(chunkpos, ImmutableList.of());
   }

   public void storeEntities(ChunkEntities<Entity> chunkentities) {
      ChunkPos chunkpos = chunkentities.getPos();
      if (chunkentities.isEmpty()) {
         if (this.emptyChunks.add(chunkpos.toLong())) {
            this.worker.store(chunkpos, (CompoundTag)null);
         }

      } else {
         ListTag listtag = new ListTag();
         chunkentities.getEntities().forEach((entity) -> {
            CompoundTag compoundtag1 = new CompoundTag();
            if (entity.save(compoundtag1)) {
               listtag.add(compoundtag1);
            }

         });
         CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
         compoundtag.put("Entities", listtag);
         writeChunkPos(compoundtag, chunkpos);
         this.worker.store(chunkpos, compoundtag).exceptionally((throwable) -> {
            LOGGER.error("Failed to store chunk {}", chunkpos, throwable);
            return null;
         });
         this.emptyChunks.remove(chunkpos.toLong());
      }
   }

   public void flush(boolean flag) {
      this.worker.synchronize(flag).join();
      this.entityDeserializerQueue.runAll();
   }

   private CompoundTag upgradeChunkTag(CompoundTag compoundtag) {
      int i = NbtUtils.getDataVersion(compoundtag, -1);
      return DataFixTypes.ENTITY_CHUNK.updateToCurrentVersion(this.fixerUpper, compoundtag, i);
   }

   public void close() throws IOException {
      this.worker.close();
   }
}
