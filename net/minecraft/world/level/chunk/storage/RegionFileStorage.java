package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable {
   public static final String ANVIL_EXTENSION = ".mca";
   private static final int MAX_CACHE_SIZE = 256;
   private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
   private final Path folder;
   private final boolean sync;

   RegionFileStorage(Path path, boolean flag) {
      this.folder = path;
      this.sync = flag;
   }

   private RegionFile getRegionFile(ChunkPos chunkpos) throws IOException {
      long i = ChunkPos.asLong(chunkpos.getRegionX(), chunkpos.getRegionZ());
      RegionFile regionfile = this.regionCache.getAndMoveToFirst(i);
      if (regionfile != null) {
         return regionfile;
      } else {
         if (this.regionCache.size() >= 256) {
            this.regionCache.removeLast().close();
         }

         FileUtil.createDirectoriesSafe(this.folder);
         Path path = this.folder.resolve("r." + chunkpos.getRegionX() + "." + chunkpos.getRegionZ() + ".mca");
         RegionFile regionfile1 = new RegionFile(path, this.folder, this.sync);
         this.regionCache.putAndMoveToFirst(i, regionfile1);
         return regionfile1;
      }
   }

   @Nullable
   public CompoundTag read(ChunkPos chunkpos) throws IOException {
      RegionFile regionfile = this.getRegionFile(chunkpos);
      DataInputStream datainputstream = regionfile.getChunkDataInputStream(chunkpos);

      CompoundTag var8;
      label43: {
         try {
            if (datainputstream == null) {
               var8 = null;
               break label43;
            }

            var8 = NbtIo.read(datainputstream);
         } catch (Throwable var7) {
            if (datainputstream != null) {
               try {
                  datainputstream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (datainputstream != null) {
            datainputstream.close();
         }

         return var8;
      }

      if (datainputstream != null) {
         datainputstream.close();
      }

      return var8;
   }

   public void scanChunk(ChunkPos chunkpos, StreamTagVisitor streamtagvisitor) throws IOException {
      RegionFile regionfile = this.getRegionFile(chunkpos);
      DataInputStream datainputstream = regionfile.getChunkDataInputStream(chunkpos);

      try {
         if (datainputstream != null) {
            NbtIo.parse(datainputstream, streamtagvisitor);
         }
      } catch (Throwable var8) {
         if (datainputstream != null) {
            try {
               datainputstream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (datainputstream != null) {
         datainputstream.close();
      }

   }

   protected void write(ChunkPos chunkpos, @Nullable CompoundTag compoundtag) throws IOException {
      RegionFile regionfile = this.getRegionFile(chunkpos);
      if (compoundtag == null) {
         regionfile.clear(chunkpos);
      } else {
         DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(chunkpos);

         try {
            NbtIo.write(compoundtag, dataoutputstream);
         } catch (Throwable var8) {
            if (dataoutputstream != null) {
               try {
                  dataoutputstream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (dataoutputstream != null) {
            dataoutputstream.close();
         }
      }

   }

   public void close() throws IOException {
      ExceptionCollector<IOException> exceptioncollector = new ExceptionCollector<>();

      for(RegionFile regionfile : this.regionCache.values()) {
         try {
            regionfile.close();
         } catch (IOException var5) {
            exceptioncollector.add(var5);
         }
      }

      exceptioncollector.throwIfPresent();
   }

   public void flush() throws IOException {
      for(RegionFile regionfile : this.regionCache.values()) {
         regionfile.flush();
      }

   }
}
