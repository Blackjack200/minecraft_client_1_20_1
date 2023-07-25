package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;

public class PathPackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Joiner PATH_JOINER = Joiner.on("/");
   private final Path root;

   public PathPackResources(String s, Path path, boolean flag) {
      super(s, flag);
      this.root = path;
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... astring) {
      FileUtil.validatePath(astring);
      Path path = FileUtil.resolvePath(this.root, List.of(astring));
      return Files.exists(path) ? IoSupplier.create(path) : null;
   }

   public static boolean validatePath(Path path) {
      return true;
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType packtype, ResourceLocation resourcelocation) {
      Path path = this.root.resolve(packtype.getDirectory()).resolve(resourcelocation.getNamespace());
      return getResource(resourcelocation, path);
   }

   public static IoSupplier<InputStream> getResource(ResourceLocation resourcelocation, Path path) {
      return FileUtil.decomposePath(resourcelocation.getPath()).get().map((list) -> {
         Path path2 = FileUtil.resolvePath(path, list);
         return returnFileIfExists(path2);
      }, (dataresult_partialresult) -> {
         LOGGER.error("Invalid path {}: {}", resourcelocation, dataresult_partialresult.message());
         return null;
      });
   }

   @Nullable
   private static IoSupplier<InputStream> returnFileIfExists(Path path) {
      return Files.exists(path) && validatePath(path) ? IoSupplier.create(path) : null;
   }

   public void listResources(PackType packtype, String s, String s1, PackResources.ResourceOutput packresources_resourceoutput) {
      FileUtil.decomposePath(s1).get().ifLeft((list) -> {
         Path path = this.root.resolve(packtype.getDirectory()).resolve(s);
         listPath(s, path, list, packresources_resourceoutput);
      }).ifRight((dataresult_partialresult) -> LOGGER.error("Invalid path {}: {}", s1, dataresult_partialresult.message()));
   }

   public static void listPath(String s, Path path, List<String> list, PackResources.ResourceOutput packresources_resourceoutput) {
      Path path1 = FileUtil.resolvePath(path, list);

      try {
         Stream<Path> stream = Files.find(path1, Integer.MAX_VALUE, (path4, basicfileattributes) -> basicfileattributes.isRegularFile());

         try {
            stream.forEach((path3) -> {
               String s2 = PATH_JOINER.join(path.relativize(path3));
               ResourceLocation resourcelocation = ResourceLocation.tryBuild(s, s2);
               if (resourcelocation == null) {
                  Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", s, s2));
               } else {
                  packresources_resourceoutput.accept(resourcelocation, IoSupplier.create(path3));
               }

            });
         } catch (Throwable var9) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (NoSuchFileException var10) {
      } catch (IOException var11) {
         LOGGER.error("Failed to list path {}", path1, var11);
      }

   }

   public Set<String> getNamespaces(PackType packtype) {
      Set<String> set = Sets.newHashSet();
      Path path = this.root.resolve(packtype.getDirectory());

      try {
         DirectoryStream<Path> directorystream = Files.newDirectoryStream(path);

         try {
            for(Path path1 : directorystream) {
               String s = path1.getFileName().toString();
               if (s.equals(s.toLowerCase(Locale.ROOT))) {
                  set.add(s);
               } else {
                  LOGGER.warn("Ignored non-lowercase namespace: {} in {}", s, this.root);
               }
            }
         } catch (Throwable var9) {
            if (directorystream != null) {
               try {
                  directorystream.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (directorystream != null) {
            directorystream.close();
         }
      } catch (NoSuchFileException var10) {
      } catch (IOException var11) {
         LOGGER.error("Failed to list path {}", path, var11);
      }

      return set;
   }

   public void close() {
   }
}
