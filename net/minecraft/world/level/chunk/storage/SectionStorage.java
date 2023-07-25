package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.slf4j.Logger;

public class SectionStorage<R> implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String SECTIONS_TAG = "Sections";
   private final IOWorker worker;
   private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
   private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
   private final Function<Runnable, Codec<R>> codec;
   private final Function<Runnable, R> factory;
   private final DataFixer fixerUpper;
   private final DataFixTypes type;
   private final RegistryAccess registryAccess;
   protected final LevelHeightAccessor levelHeightAccessor;

   public SectionStorage(Path path, Function<Runnable, Codec<R>> function, Function<Runnable, R> function1, DataFixer datafixer, DataFixTypes datafixtypes, boolean flag, RegistryAccess registryaccess, LevelHeightAccessor levelheightaccessor) {
      this.codec = function;
      this.factory = function1;
      this.fixerUpper = datafixer;
      this.type = datafixtypes;
      this.registryAccess = registryaccess;
      this.levelHeightAccessor = levelheightaccessor;
      this.worker = new IOWorker(path, flag, path.getFileName().toString());
   }

   protected void tick(BooleanSupplier booleansupplier) {
      while(this.hasWork() && booleansupplier.getAsBoolean()) {
         ChunkPos chunkpos = SectionPos.of(this.dirty.firstLong()).chunk();
         this.writeColumn(chunkpos);
      }

   }

   public boolean hasWork() {
      return !this.dirty.isEmpty();
   }

   @Nullable
   protected Optional<R> get(long i) {
      return this.storage.get(i);
   }

   protected Optional<R> getOrLoad(long i) {
      if (this.outsideStoredRange(i)) {
         return Optional.empty();
      } else {
         Optional<R> optional = this.get(i);
         if (optional != null) {
            return optional;
         } else {
            this.readColumn(SectionPos.of(i).chunk());
            optional = this.get(i);
            if (optional == null) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
            } else {
               return optional;
            }
         }
      }
   }

   protected boolean outsideStoredRange(long i) {
      int j = SectionPos.sectionToBlockCoord(SectionPos.y(i));
      return this.levelHeightAccessor.isOutsideBuildHeight(j);
   }

   protected R getOrCreate(long i) {
      if (this.outsideStoredRange(i)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
      } else {
         Optional<R> optional = this.getOrLoad(i);
         if (optional.isPresent()) {
            return optional.get();
         } else {
            R object = this.factory.apply(() -> this.setDirty(i));
            this.storage.put(i, Optional.of(object));
            return object;
         }
      }
   }

   private void readColumn(ChunkPos chunkpos) {
      Optional<CompoundTag> optional = this.tryRead(chunkpos).join();
      RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, this.registryAccess);
      this.readColumn(chunkpos, registryops, optional.orElse((CompoundTag)null));
   }

   private CompletableFuture<Optional<CompoundTag>> tryRead(ChunkPos chunkpos) {
      return this.worker.loadAsync(chunkpos).exceptionally((throwable) -> {
         if (throwable instanceof IOException ioexception) {
            LOGGER.error("Error reading chunk {} data from disk", chunkpos, ioexception);
            return Optional.empty();
         } else {
            throw new CompletionException(throwable);
         }
      });
   }

   private <T> void readColumn(ChunkPos chunkpos, DynamicOps<T> dynamicops, @Nullable T object) {
      if (object == null) {
         for(int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
            this.storage.put(getKey(chunkpos, i), Optional.empty());
         }
      } else {
         Dynamic<T> dynamic = new Dynamic<>(dynamicops, object);
         int j = getVersion(dynamic);
         int k = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
         boolean flag = j != k;
         Dynamic<T> dynamic1 = this.type.update(this.fixerUpper, dynamic, j, k);
         OptionalDynamic<T> optionaldynamic = dynamic1.get("Sections");

         for(int l = this.levelHeightAccessor.getMinSection(); l < this.levelHeightAccessor.getMaxSection(); ++l) {
            long i1 = getKey(chunkpos, l);
            Optional<R> optional = optionaldynamic.get(Integer.toString(l)).result().flatMap((dynamic2) -> this.codec.apply(() -> this.setDirty(i1)).parse(dynamic2).resultOrPartial(LOGGER::error));
            this.storage.put(i1, optional);
            optional.ifPresent((object1) -> {
               this.onSectionLoad(i1);
               if (flag) {
                  this.setDirty(i1);
               }

            });
         }
      }

   }

   private void writeColumn(ChunkPos chunkpos) {
      RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, this.registryAccess);
      Dynamic<Tag> dynamic = this.writeColumn(chunkpos, registryops);
      Tag tag = dynamic.getValue();
      if (tag instanceof CompoundTag) {
         this.worker.store(chunkpos, (CompoundTag)tag);
      } else {
         LOGGER.error("Expected compound tag, got {}", (Object)tag);
      }

   }

   private <T> Dynamic<T> writeColumn(ChunkPos chunkpos, DynamicOps<T> dynamicops) {
      Map<T, T> map = Maps.newHashMap();

      for(int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
         long j = getKey(chunkpos, i);
         this.dirty.remove(j);
         Optional<R> optional = this.storage.get(j);
         if (optional != null && optional.isPresent()) {
            DataResult<T> dataresult = this.codec.apply(() -> this.setDirty(j)).encodeStart(dynamicops, optional.get());
            String s = Integer.toString(i);
            dataresult.resultOrPartial(LOGGER::error).ifPresent((object) -> map.put(dynamicops.createString(s), object));
         }
      }

      return new Dynamic<>(dynamicops, dynamicops.createMap(ImmutableMap.of(dynamicops.createString("Sections"), dynamicops.createMap(map), dynamicops.createString("DataVersion"), dynamicops.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))));
   }

   private static long getKey(ChunkPos chunkpos, int i) {
      return SectionPos.asLong(chunkpos.x, i, chunkpos.z);
   }

   protected void onSectionLoad(long i) {
   }

   protected void setDirty(long i) {
      Optional<R> optional = this.storage.get(i);
      if (optional != null && optional.isPresent()) {
         this.dirty.add(i);
      } else {
         LOGGER.warn("No data for position: {}", (Object)SectionPos.of(i));
      }
   }

   private static int getVersion(Dynamic<?> dynamic) {
      return dynamic.get("DataVersion").asInt(1945);
   }

   public void flush(ChunkPos chunkpos) {
      if (this.hasWork()) {
         for(int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
            long j = getKey(chunkpos, i);
            if (this.dirty.contains(j)) {
               this.writeColumn(chunkpos);
               return;
            }
         }
      }

   }

   public void close() throws IOException {
      this.worker.close();
   }
}
