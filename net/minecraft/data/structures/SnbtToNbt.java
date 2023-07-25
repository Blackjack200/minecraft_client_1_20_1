package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements DataProvider {
   @Nullable
   private static final Path DUMP_SNBT_TO = null;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackOutput output;
   private final Iterable<Path> inputFolders;
   private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

   public SnbtToNbt(PackOutput packoutput, Iterable<Path> iterable) {
      this.output = packoutput;
      this.inputFolders = iterable;
   }

   public SnbtToNbt addFilter(SnbtToNbt.Filter snbttonbt_filter) {
      this.filters.add(snbttonbt_filter);
      return this;
   }

   private CompoundTag applyFilters(String s, CompoundTag compoundtag) {
      CompoundTag compoundtag1 = compoundtag;

      for(SnbtToNbt.Filter snbttonbt_filter : this.filters) {
         compoundtag1 = snbttonbt_filter.apply(s, compoundtag1);
      }

      return compoundtag1;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      Path path = this.output.getOutputFolder();
      List<CompletableFuture<?>> list = Lists.newArrayList();

      for(Path path1 : this.inputFolders) {
         list.add(CompletableFuture.supplyAsync(() -> {
            try {
               Stream<Path> stream = Files.walk(path1);

               CompletableFuture var5;
               try {
                  var5 = CompletableFuture.allOf(stream.filter((path10) -> path10.toString().endsWith(".snbt")).map((path6) -> CompletableFuture.runAsync(() -> {
                        SnbtToNbt.TaskResult snbttonbt_taskresult = this.readStructure(path6, this.getName(path1, path6));
                        this.storeStructureIfChanged(cachedoutput, snbttonbt_taskresult, path);
                     }, Util.backgroundExecutor())).toArray((i) -> new CompletableFuture[i]));
               } catch (Throwable var8) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (stream != null) {
                  stream.close();
               }

               return var5;
            } catch (Exception var9) {
               throw new RuntimeException("Failed to read structure input directory, aborting", var9);
            }
         }, Util.backgroundExecutor()).thenCompose((completablefuture) -> completablefuture));
      }

      return Util.sequenceFailFast(list);
   }

   public final String getName() {
      return "SNBT -> NBT";
   }

   private String getName(Path path, Path path1) {
      String s = path.relativize(path1).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".snbt".length());
   }

   private SnbtToNbt.TaskResult readStructure(Path path, String s) {
      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path);

         SnbtToNbt.TaskResult var11;
         try {
            String s1 = IOUtils.toString((Reader)bufferedreader);
            CompoundTag compoundtag = this.applyFilters(s, NbtUtils.snbtToStructure(s1));
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
            NbtIo.writeCompressed(compoundtag, hashingoutputstream);
            byte[] abyte = bytearrayoutputstream.toByteArray();
            HashCode hashcode = hashingoutputstream.hash();
            String s2;
            if (DUMP_SNBT_TO != null) {
               s2 = NbtUtils.structureToSnbt(compoundtag);
            } else {
               s2 = null;
            }

            var11 = new SnbtToNbt.TaskResult(s, abyte, s2, hashcode);
         } catch (Throwable var13) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

         return var11;
      } catch (Throwable var14) {
         throw new SnbtToNbt.StructureConversionException(path, var14);
      }
   }

   private void storeStructureIfChanged(CachedOutput cachedoutput, SnbtToNbt.TaskResult snbttonbt_taskresult, Path path) {
      if (snbttonbt_taskresult.snbtPayload != null) {
         Path path1 = DUMP_SNBT_TO.resolve(snbttonbt_taskresult.name + ".snbt");

         try {
            NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, path1, snbttonbt_taskresult.snbtPayload);
         } catch (IOException var7) {
            LOGGER.error("Couldn't write structure SNBT {} at {}", snbttonbt_taskresult.name, path1, var7);
         }
      }

      Path path2 = path.resolve(snbttonbt_taskresult.name + ".nbt");

      try {
         cachedoutput.writeIfNeeded(path2, snbttonbt_taskresult.payload, snbttonbt_taskresult.hash);
      } catch (IOException var6) {
         LOGGER.error("Couldn't write structure {} at {}", snbttonbt_taskresult.name, path2, var6);
      }

   }

   @FunctionalInterface
   public interface Filter {
      CompoundTag apply(String s, CompoundTag compoundtag);
   }

   static class StructureConversionException extends RuntimeException {
      public StructureConversionException(Path path, Throwable throwable) {
         super(path.toAbsolutePath().toString(), throwable);
      }
   }

   static record TaskResult(String name, byte[] payload, @Nullable String snbtPayload, HashCode hash) {
      final String name;
      final byte[] payload;
      @Nullable
      final String snbtPayload;
      final HashCode hash;
   }
}
