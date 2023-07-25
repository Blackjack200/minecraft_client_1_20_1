package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path folder;
   private final PackType packType;
   private final PackSource packSource;

   public FolderRepositorySource(Path path, PackType packtype, PackSource packsource) {
      this.folder = path;
      this.packType = packtype;
      this.packSource = packsource;
   }

   private static String nameFromPath(Path path) {
      return path.getFileName().toString();
   }

   public void loadPacks(Consumer<Pack> consumer) {
      try {
         FileUtil.createDirectoriesSafe(this.folder);
         discoverPacks(this.folder, false, (path, pack_resourcessupplier) -> {
            String s = nameFromPath(path);
            Pack pack = Pack.readMetaAndCreate("file/" + s, Component.literal(s), false, pack_resourcessupplier, this.packType, Pack.Position.TOP, this.packSource);
            if (pack != null) {
               consumer.accept(pack);
            }

         });
      } catch (IOException var3) {
         LOGGER.warn("Failed to list packs in {}", this.folder, var3);
      }

   }

   public static void discoverPacks(Path path, boolean flag, BiConsumer<Path, Pack.ResourcesSupplier> biconsumer) throws IOException {
      DirectoryStream<Path> directorystream = Files.newDirectoryStream(path);

      try {
         for(Path path1 : directorystream) {
            Pack.ResourcesSupplier pack_resourcessupplier = detectPackResources(path1, flag);
            if (pack_resourcessupplier != null) {
               biconsumer.accept(path1, pack_resourcessupplier);
            }
         }
      } catch (Throwable var8) {
         if (directorystream != null) {
            try {
               directorystream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (directorystream != null) {
         directorystream.close();
      }

   }

   @Nullable
   public static Pack.ResourcesSupplier detectPackResources(Path path, boolean flag) {
      BasicFileAttributes basicfileattributes;
      try {
         basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class);
      } catch (NoSuchFileException var5) {
         return null;
      } catch (IOException var6) {
         LOGGER.warn("Failed to read properties of '{}', ignoring", path, var6);
         return null;
      }

      if (basicfileattributes.isDirectory() && Files.isRegularFile(path.resolve("pack.mcmeta"))) {
         return (s1) -> new PathPackResources(s1, path, flag);
      } else {
         if (basicfileattributes.isRegularFile() && path.getFileName().toString().endsWith(".zip")) {
            FileSystem filesystem = path.getFileSystem();
            if (filesystem == FileSystems.getDefault() || filesystem instanceof LinkFileSystem) {
               File file = path.toFile();
               return (s) -> new FilePackResources(s, file, flag);
            }
         }

         LOGGER.info("Found non-pack entry '{}', ignoring", (Object)path);
         return null;
      }
   }
}
