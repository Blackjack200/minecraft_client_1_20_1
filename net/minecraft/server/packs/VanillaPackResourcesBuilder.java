package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.Util;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static Consumer<VanillaPackResourcesBuilder> developmentConfig = (vanillapackresourcesbuilder) -> {
   };
   private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() -> {
      synchronized(VanillaPackResources.class) {
         ImmutableMap.Builder<PackType, Path> immutablemap_builder = ImmutableMap.builder();

         for(PackType packtype : PackType.values()) {
            String s = "/" + packtype.getDirectory() + "/.mcassetsroot";
            URL url = VanillaPackResources.class.getResource(s);
            if (url == null) {
               LOGGER.error("File {} does not exist in classpath", (Object)s);
            } else {
               try {
                  URI uri = url.toURI();
                  String s1 = uri.getScheme();
                  if (!"jar".equals(s1) && !"file".equals(s1)) {
                     LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uri);
                  }

                  Path path = safeGetPath(uri);
                  immutablemap_builder.put(packtype, path.getParent());
               } catch (Exception var12) {
                  LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)var12);
               }
            }
         }

         return immutablemap_builder.build();
      }
   });
   private final Set<Path> rootPaths = new LinkedHashSet<>();
   private final Map<PackType, Set<Path>> pathsForType = new EnumMap<>(PackType.class);
   private BuiltInMetadata metadata = BuiltInMetadata.of();
   private final Set<String> namespaces = new HashSet<>();

   private static Path safeGetPath(URI uri) throws IOException {
      try {
         return Paths.get(uri);
      } catch (FileSystemNotFoundException var3) {
      } catch (Throwable var4) {
         LOGGER.warn("Unable to get path for: {}", uri, var4);
      }

      try {
         FileSystems.newFileSystem(uri, Collections.emptyMap());
      } catch (FileSystemAlreadyExistsException var2) {
      }

      return Paths.get(uri);
   }

   private boolean validateDirPath(Path path) {
      if (!Files.exists(path)) {
         return false;
      } else if (!Files.isDirectory(path)) {
         throw new IllegalArgumentException("Path " + path.toAbsolutePath() + " is not directory");
      } else {
         return true;
      }
   }

   private void pushRootPath(Path path) {
      if (this.validateDirPath(path)) {
         this.rootPaths.add(path);
      }

   }

   private void pushPathForType(PackType packtype, Path path) {
      if (this.validateDirPath(path)) {
         this.pathsForType.computeIfAbsent(packtype, (packtype1) -> new LinkedHashSet()).add(path);
      }

   }

   public VanillaPackResourcesBuilder pushJarResources() {
      ROOT_DIR_BY_TYPE.forEach((packtype, path) -> {
         this.pushRootPath(path.getParent());
         this.pushPathForType(packtype, path);
      });
      return this;
   }

   public VanillaPackResourcesBuilder pushClasspathResources(PackType packtype, Class<?> oclass) {
      Enumeration<URL> enumeration = null;

      try {
         enumeration = oclass.getClassLoader().getResources(packtype.getDirectory() + "/");
      } catch (IOException var8) {
      }

      while(enumeration != null && enumeration.hasMoreElements()) {
         URL url = enumeration.nextElement();

         try {
            URI uri = url.toURI();
            if ("file".equals(uri.getScheme())) {
               Path path = Paths.get(uri);
               this.pushRootPath(path.getParent());
               this.pushPathForType(packtype, path);
            }
         } catch (Exception var7) {
            LOGGER.error("Failed to extract path from {}", url, var7);
         }
      }

      return this;
   }

   public VanillaPackResourcesBuilder applyDevelopmentConfig() {
      developmentConfig.accept(this);
      return this;
   }

   public VanillaPackResourcesBuilder pushUniversalPath(Path path) {
      this.pushRootPath(path);

      for(PackType packtype : PackType.values()) {
         this.pushPathForType(packtype, path.resolve(packtype.getDirectory()));
      }

      return this;
   }

   public VanillaPackResourcesBuilder pushAssetPath(PackType packtype, Path path) {
      this.pushRootPath(path);
      this.pushPathForType(packtype, path);
      return this;
   }

   public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata builtinmetadata) {
      this.metadata = builtinmetadata;
      return this;
   }

   public VanillaPackResourcesBuilder exposeNamespace(String... astring) {
      this.namespaces.addAll(Arrays.asList(astring));
      return this;
   }

   public VanillaPackResources build() {
      Map<PackType, List<Path>> map = new EnumMap<>(PackType.class);

      for(PackType packtype : PackType.values()) {
         List<Path> list = copyAndReverse(this.pathsForType.getOrDefault(packtype, Set.of()));
         map.put(packtype, list);
      }

      return new VanillaPackResources(this.metadata, Set.copyOf(this.namespaces), copyAndReverse(this.rootPaths), map);
   }

   private static List<Path> copyAndReverse(Collection<Path> collection) {
      List<Path> list = new ArrayList<>(collection);
      Collections.reverse(list);
      return List.copyOf(list);
   }
}
