package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Iterable<Path> inputFolders;
   private final PackOutput output;

   public NbtToSnbt(PackOutput packoutput, Collection<Path> collection) {
      this.inputFolders = collection;
      this.output = packoutput;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      Path path = this.output.getOutputFolder();
      List<CompletableFuture<?>> list = new ArrayList<>();

      for(Path path1 : this.inputFolders) {
         list.add(CompletableFuture.supplyAsync(() -> {
            try {
               Stream<Path> stream = Files.walk(path1);

               CompletableFuture var4;
               try {
                  var4 = CompletableFuture.allOf(stream.filter((path10) -> path10.toString().endsWith(".nbt")).map((path6) -> CompletableFuture.runAsync(() -> convertStructure(cachedoutput, path6, getName(path1, path6), path), Util.ioPool())).toArray((j) -> new CompletableFuture[j]));
               } catch (Throwable var7) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (stream != null) {
                  stream.close();
               }

               return var4;
            } catch (IOException var8) {
               LOGGER.error("Failed to read structure input directory", (Throwable)var8);
               return CompletableFuture.completedFuture((Object)null);
            }
         }, Util.backgroundExecutor()).thenCompose((completablefuture) -> completablefuture));
      }

      return CompletableFuture.allOf(list.toArray((i) -> new CompletableFuture[i]));
   }

   public final String getName() {
      return "NBT -> SNBT";
   }

   private static String getName(Path path, Path path1) {
      String s = path.relativize(path1).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertStructure(CachedOutput cachedoutput, Path path, String s, Path path1) {
      try {
         InputStream inputstream = Files.newInputStream(path);

         Path var6;
         try {
            Path path2 = path1.resolve(s + ".snbt");
            writeSnbt(cachedoutput, path2, NbtUtils.structureToSnbt(NbtIo.readCompressed(inputstream)));
            LOGGER.info("Converted {} from NBT to SNBT", (Object)s);
            var6 = path2;
         } catch (Throwable var8) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var6;
      } catch (IOException var9) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", s, path, var9);
         return null;
      }
   }

   public static void writeSnbt(CachedOutput cachedoutput, Path path, String s) throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
      HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
      hashingoutputstream.write(s.getBytes(StandardCharsets.UTF_8));
      hashingoutputstream.write(10);
      cachedoutput.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
   }
}
