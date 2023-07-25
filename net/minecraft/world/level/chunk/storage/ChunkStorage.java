package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
   public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
   private final IOWorker worker;
   protected final DataFixer fixerUpper;
   @Nullable
   private volatile LegacyStructureDataHandler legacyStructureHandler;

   public ChunkStorage(Path path, DataFixer datafixer, boolean flag) {
      this.fixerUpper = datafixer;
      this.worker = new IOWorker(path, flag, "chunk");
   }

   public boolean isOldChunkAround(ChunkPos chunkpos, int i) {
      return this.worker.isOldChunkAround(chunkpos, i);
   }

   public CompoundTag upgradeChunkTag(ResourceKey<Level> resourcekey, Supplier<DimensionDataStorage> supplier, CompoundTag compoundtag, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional) {
      int i = getVersion(compoundtag);
      if (i < 1493) {
         compoundtag = DataFixTypes.CHUNK.update(this.fixerUpper, compoundtag, i, 1493);
         if (compoundtag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
            LegacyStructureDataHandler legacystructuredatahandler = this.getLegacyStructureHandler(resourcekey, supplier);
            compoundtag = legacystructuredatahandler.updateFromLegacy(compoundtag);
         }
      }

      injectDatafixingContext(compoundtag, resourcekey, optional);
      compoundtag = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundtag, Math.max(1493, i));
      if (i < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
         NbtUtils.addCurrentDataVersion(compoundtag);
      }

      compoundtag.remove("__context");
      return compoundtag;
   }

   private LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourcekey, Supplier<DimensionDataStorage> supplier) {
      LegacyStructureDataHandler legacystructuredatahandler = this.legacyStructureHandler;
      if (legacystructuredatahandler == null) {
         synchronized(this) {
            legacystructuredatahandler = this.legacyStructureHandler;
            if (legacystructuredatahandler == null) {
               this.legacyStructureHandler = legacystructuredatahandler = LegacyStructureDataHandler.getLegacyStructureHandler(resourcekey, supplier.get());
            }
         }
      }

      return legacystructuredatahandler;
   }

   public static void injectDatafixingContext(CompoundTag compoundtag, ResourceKey<Level> resourcekey, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional) {
      CompoundTag compoundtag1 = new CompoundTag();
      compoundtag1.putString("dimension", resourcekey.location().toString());
      optional.ifPresent((resourcekey1) -> compoundtag1.putString("generator", resourcekey1.location().toString()));
      compoundtag.put("__context", compoundtag1);
   }

   public static int getVersion(CompoundTag compoundtag) {
      return NbtUtils.getDataVersion(compoundtag, -1);
   }

   public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkpos) {
      return this.worker.loadAsync(chunkpos);
   }

   public void write(ChunkPos chunkpos, CompoundTag compoundtag) {
      this.worker.store(chunkpos, compoundtag);
      if (this.legacyStructureHandler != null) {
         this.legacyStructureHandler.removeIndex(chunkpos.toLong());
      }

   }

   public void flushWorker() {
      this.worker.synchronize(true).join();
   }

   public void close() throws IOException {
      this.worker.close();
   }

   public ChunkScanAccess chunkScanner() {
      return this.worker;
   }
}
