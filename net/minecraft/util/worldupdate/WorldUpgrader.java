package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class WorldUpgrader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ThreadFactory THREAD_FACTORY = (new ThreadFactoryBuilder()).setDaemon(true).build();
   private final Registry<LevelStem> dimensions;
   private final Set<ResourceKey<Level>> levels;
   private final boolean eraseCache;
   private final LevelStorageSource.LevelStorageAccess levelStorage;
   private final Thread thread;
   private final DataFixer dataFixer;
   private volatile boolean running = true;
   private volatile boolean finished;
   private volatile float progress;
   private volatile int totalChunks;
   private volatile int converted;
   private volatile int skipped;
   private final Object2FloatMap<ResourceKey<Level>> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
   private volatile Component status = Component.translatable("optimizeWorld.stage.counting");
   private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
   private final DimensionDataStorage overworldDataStorage;

   public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer, Registry<LevelStem> registry, boolean flag) {
      this.dimensions = registry;
      this.levels = registry.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
      this.eraseCache = flag;
      this.dataFixer = datafixer;
      this.levelStorage = levelstoragesource_levelstorageaccess;
      this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), datafixer);
      this.thread = THREAD_FACTORY.newThread(this::work);
      this.thread.setUncaughtExceptionHandler((thread, throwable1) -> {
         LOGGER.error("Error upgrading world", throwable1);
         this.status = Component.translatable("optimizeWorld.stage.failed");
         this.finished = true;
      });
      this.thread.start();
   }

   public void cancel() {
      this.running = false;

      try {
         this.thread.join();
      } catch (InterruptedException var2) {
      }

   }

   private void work() {
      this.totalChunks = 0;
      ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap_builder = ImmutableMap.builder();

      for(ResourceKey<Level> resourcekey : this.levels) {
         List<ChunkPos> list = this.getAllChunkPos(resourcekey);
         immutablemap_builder.put(resourcekey, list.listIterator());
         this.totalChunks += list.size();
      }

      if (this.totalChunks == 0) {
         this.finished = true;
      } else {
         float f = (float)this.totalChunks;
         ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap = immutablemap_builder.build();
         ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> immutablemap_builder1 = ImmutableMap.builder();

         for(ResourceKey<Level> resourcekey1 : this.levels) {
            Path path = this.levelStorage.getDimensionPath(resourcekey1);
            immutablemap_builder1.put(resourcekey1, new ChunkStorage(path.resolve("region"), this.dataFixer, true));
         }

         ImmutableMap<ResourceKey<Level>, ChunkStorage> immutablemap1 = immutablemap_builder1.build();
         long i = Util.getMillis();
         this.status = Component.translatable("optimizeWorld.stage.upgrading");

         while(this.running) {
            boolean flag1 = false;
            float f1 = 0.0F;

            for(ResourceKey<Level> resourcekey2 : this.levels) {
               ListIterator<ChunkPos> listiterator = immutablemap.get(resourcekey2);
               ChunkStorage chunkstorage = immutablemap1.get(resourcekey2);
               if (listiterator.hasNext()) {
                  ChunkPos chunkpos = listiterator.next();
                  boolean flag2 = false;

                  try {
                     CompoundTag compoundtag = chunkstorage.read(chunkpos).join().orElse((CompoundTag)null);
                     if (compoundtag != null) {
                        int j = ChunkStorage.getVersion(compoundtag);
                        ChunkGenerator chunkgenerator = this.dimensions.getOrThrow(Registries.levelToLevelStem(resourcekey2)).generator();
                        CompoundTag compoundtag1 = chunkstorage.upgradeChunkTag(resourcekey2, () -> this.overworldDataStorage, compoundtag, chunkgenerator.getTypeNameForDataFixer());
                        ChunkPos chunkpos1 = new ChunkPos(compoundtag1.getInt("xPos"), compoundtag1.getInt("zPos"));
                        if (!chunkpos1.equals(chunkpos)) {
                           LOGGER.warn("Chunk {} has invalid position {}", chunkpos, chunkpos1);
                        }

                        boolean flag3 = j < SharedConstants.getCurrentVersion().getDataVersion().getVersion();
                        if (this.eraseCache) {
                           flag3 = flag3 || compoundtag1.contains("Heightmaps");
                           compoundtag1.remove("Heightmaps");
                           flag3 = flag3 || compoundtag1.contains("isLightOn");
                           compoundtag1.remove("isLightOn");
                           ListTag listtag = compoundtag1.getList("sections", 10);

                           for(int k = 0; k < listtag.size(); ++k) {
                              CompoundTag compoundtag2 = listtag.getCompound(k);
                              flag3 = flag3 || compoundtag2.contains("BlockLight");
                              compoundtag2.remove("BlockLight");
                              flag3 = flag3 || compoundtag2.contains("SkyLight");
                              compoundtag2.remove("SkyLight");
                           }
                        }

                        if (flag3) {
                           chunkstorage.write(chunkpos, compoundtag1);
                           flag2 = true;
                        }
                     }
                  } catch (CompletionException | ReportedException var26) {
                     Throwable throwable = var26.getCause();
                     if (!(throwable instanceof IOException)) {
                        throw var26;
                     }

                     LOGGER.error("Error upgrading chunk {}", chunkpos, throwable);
                  }

                  if (flag2) {
                     ++this.converted;
                  } else {
                     ++this.skipped;
                  }

                  flag1 = true;
               }

               float f2 = (float)listiterator.nextIndex() / f;
               this.progressMap.put(resourcekey2, f2);
               f1 += f2;
            }

            this.progress = f1;
            if (!flag1) {
               this.running = false;
            }
         }

         this.status = Component.translatable("optimizeWorld.stage.finished");

         for(ChunkStorage chunkstorage1 : immutablemap1.values()) {
            try {
               chunkstorage1.close();
            } catch (IOException var25) {
               LOGGER.error("Error upgrading chunk", (Throwable)var25);
            }
         }

         this.overworldDataStorage.save();
         i = Util.getMillis() - i;
         LOGGER.info("World optimizaton finished after {} ms", (long)i);
         this.finished = true;
      }
   }

   private List<ChunkPos> getAllChunkPos(ResourceKey<Level> resourcekey) {
      File file = this.levelStorage.getDimensionPath(resourcekey).toFile();
      File file1 = new File(file, "region");
      File[] afile = file1.listFiles((file3, s) -> s.endsWith(".mca"));
      if (afile == null) {
         return ImmutableList.of();
      } else {
         List<ChunkPos> list = Lists.newArrayList();

         for(File file2 : afile) {
            Matcher matcher = REGEX.matcher(file2.getName());
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1)) << 5;
               int j = Integer.parseInt(matcher.group(2)) << 5;

               try {
                  RegionFile regionfile = new RegionFile(file2.toPath(), file1.toPath(), true);

                  try {
                     for(int k = 0; k < 32; ++k) {
                        for(int l = 0; l < 32; ++l) {
                           ChunkPos chunkpos = new ChunkPos(k + i, l + j);
                           if (regionfile.doesChunkExist(chunkpos)) {
                              list.add(chunkpos);
                           }
                        }
                     }
                  } catch (Throwable var18) {
                     try {
                        regionfile.close();
                     } catch (Throwable var17) {
                        var18.addSuppressed(var17);
                     }

                     throw var18;
                  }

                  regionfile.close();
               } catch (Throwable var19) {
               }
            }
         }

         return list;
      }
   }

   public boolean isFinished() {
      return this.finished;
   }

   public Set<ResourceKey<Level>> levels() {
      return this.levels;
   }

   public float dimensionProgress(ResourceKey<Level> resourcekey) {
      return this.progressMap.getFloat(resourcekey);
   }

   public float getProgress() {
      return this.progress;
   }

   public int getTotalChunks() {
      return this.totalChunks;
   }

   public int getConverted() {
      return this.converted;
   }

   public int getSkipped() {
      return this.skipped;
   }

   public Component getStatus() {
      return this.status;
   }
}
