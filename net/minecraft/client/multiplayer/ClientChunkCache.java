package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ClientChunkCache extends ChunkSource {
   static final Logger LOGGER = LogUtils.getLogger();
   private final LevelChunk emptyChunk;
   private final LevelLightEngine lightEngine;
   volatile ClientChunkCache.Storage storage;
   final ClientLevel level;

   public ClientChunkCache(ClientLevel clientlevel, int i) {
      this.level = clientlevel;
      this.emptyChunk = new EmptyLevelChunk(clientlevel, new ChunkPos(0, 0), clientlevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
      this.lightEngine = new LevelLightEngine(this, true, clientlevel.dimensionType().hasSkyLight());
      this.storage = new ClientChunkCache.Storage(calculateStorageRange(i));
   }

   public LevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   private static boolean isValidChunk(@Nullable LevelChunk levelchunk, int i, int j) {
      if (levelchunk == null) {
         return false;
      } else {
         ChunkPos chunkpos = levelchunk.getPos();
         return chunkpos.x == i && chunkpos.z == j;
      }
   }

   public void drop(int i, int j) {
      if (this.storage.inRange(i, j)) {
         int k = this.storage.getIndex(i, j);
         LevelChunk levelchunk = this.storage.getChunk(k);
         if (isValidChunk(levelchunk, i, j)) {
            this.storage.replace(k, levelchunk, (LevelChunk)null);
         }

      }
   }

   @Nullable
   public LevelChunk getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      if (this.storage.inRange(i, j)) {
         LevelChunk levelchunk = this.storage.getChunk(this.storage.getIndex(i, j));
         if (isValidChunk(levelchunk, i, j)) {
            return levelchunk;
         }
      }

      return flag ? this.emptyChunk : null;
   }

   public BlockGetter getLevel() {
      return this.level;
   }

   public void replaceBiomes(int i, int j, FriendlyByteBuf friendlybytebuf) {
      if (!this.storage.inRange(i, j)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", i, j);
      } else {
         int k = this.storage.getIndex(i, j);
         LevelChunk levelchunk = this.storage.chunks.get(k);
         if (!isValidChunk(levelchunk, i, j)) {
            LOGGER.warn("Ignoring chunk since it's not present: {}, {}", i, j);
         } else {
            levelchunk.replaceBiomes(friendlybytebuf);
         }

      }
   }

   @Nullable
   public LevelChunk replaceWithPacketData(int i, int j, FriendlyByteBuf friendlybytebuf, CompoundTag compoundtag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
      if (!this.storage.inRange(i, j)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", i, j);
         return null;
      } else {
         int k = this.storage.getIndex(i, j);
         LevelChunk levelchunk = this.storage.chunks.get(k);
         ChunkPos chunkpos = new ChunkPos(i, j);
         if (!isValidChunk(levelchunk, i, j)) {
            levelchunk = new LevelChunk(this.level, chunkpos);
            levelchunk.replaceWithPacketData(friendlybytebuf, compoundtag, consumer);
            this.storage.replace(k, levelchunk);
         } else {
            levelchunk.replaceWithPacketData(friendlybytebuf, compoundtag, consumer);
         }

         this.level.onChunkLoaded(chunkpos);
         return levelchunk;
      }
   }

   public void tick(BooleanSupplier booleansupplier, boolean flag) {
   }

   public void updateViewCenter(int i, int j) {
      this.storage.viewCenterX = i;
      this.storage.viewCenterZ = j;
   }

   public void updateViewRadius(int i) {
      int j = this.storage.chunkRadius;
      int k = calculateStorageRange(i);
      if (j != k) {
         ClientChunkCache.Storage clientchunkcache_storage = new ClientChunkCache.Storage(k);
         clientchunkcache_storage.viewCenterX = this.storage.viewCenterX;
         clientchunkcache_storage.viewCenterZ = this.storage.viewCenterZ;

         for(int l = 0; l < this.storage.chunks.length(); ++l) {
            LevelChunk levelchunk = this.storage.chunks.get(l);
            if (levelchunk != null) {
               ChunkPos chunkpos = levelchunk.getPos();
               if (clientchunkcache_storage.inRange(chunkpos.x, chunkpos.z)) {
                  clientchunkcache_storage.replace(clientchunkcache_storage.getIndex(chunkpos.x, chunkpos.z), levelchunk);
               }
            }
         }

         this.storage = clientchunkcache_storage;
      }

   }

   private static int calculateStorageRange(int i) {
      return Math.max(2, i) + 3;
   }

   public String gatherStats() {
      return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
   }

   public int getLoadedChunksCount() {
      return this.storage.chunkCount;
   }

   public void onLightUpdate(LightLayer lightlayer, SectionPos sectionpos) {
      Minecraft.getInstance().levelRenderer.setSectionDirty(sectionpos.x(), sectionpos.y(), sectionpos.z());
   }

   final class Storage {
      final AtomicReferenceArray<LevelChunk> chunks;
      final int chunkRadius;
      private final int viewRange;
      volatile int viewCenterX;
      volatile int viewCenterZ;
      int chunkCount;

      Storage(int i) {
         this.chunkRadius = i;
         this.viewRange = i * 2 + 1;
         this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
      }

      int getIndex(int i, int j) {
         return Math.floorMod(j, this.viewRange) * this.viewRange + Math.floorMod(i, this.viewRange);
      }

      protected void replace(int i, @Nullable LevelChunk levelchunk) {
         LevelChunk levelchunk1 = this.chunks.getAndSet(i, levelchunk);
         if (levelchunk1 != null) {
            --this.chunkCount;
            ClientChunkCache.this.level.unload(levelchunk1);
         }

         if (levelchunk != null) {
            ++this.chunkCount;
         }

      }

      protected LevelChunk replace(int i, LevelChunk levelchunk, @Nullable LevelChunk levelchunk1) {
         if (this.chunks.compareAndSet(i, levelchunk, levelchunk1) && levelchunk1 == null) {
            --this.chunkCount;
         }

         ClientChunkCache.this.level.unload(levelchunk);
         return levelchunk;
      }

      boolean inRange(int i, int j) {
         return Math.abs(i - this.viewCenterX) <= this.chunkRadius && Math.abs(j - this.viewCenterZ) <= this.chunkRadius;
      }

      @Nullable
      protected LevelChunk getChunk(int i) {
         return this.chunks.get(i);
      }

      private void dumpChunks(String s) {
         try {
            FileOutputStream fileoutputstream = new FileOutputStream(s);

            try {
               int i = ClientChunkCache.this.storage.chunkRadius;

               for(int j = this.viewCenterZ - i; j <= this.viewCenterZ + i; ++j) {
                  for(int k = this.viewCenterX - i; k <= this.viewCenterX + i; ++k) {
                     LevelChunk levelchunk = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(k, j));
                     if (levelchunk != null) {
                        ChunkPos chunkpos = levelchunk.getPos();
                        fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + levelchunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                     }
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
            ClientChunkCache.LOGGER.error("Failed to dump chunks to file {}", s, var10);
         }

      }
   }
}
