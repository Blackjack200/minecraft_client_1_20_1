package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BuiltInMetadata metadata;
   private final Set<String> namespaces;
   private final List<Path> rootPaths;
   private final Map<PackType, List<Path>> pathsForType;

   VanillaPackResources(BuiltInMetadata builtinmetadata, Set<String> set, List<Path> list, Map<PackType, List<Path>> map) {
      this.metadata = builtinmetadata;
      this.namespaces = set;
      this.rootPaths = list;
      this.pathsForType = map;
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... astring) {
      FileUtil.validatePath(astring);
      List<String> list = List.of(astring);

      for(Path path : this.rootPaths) {
         Path path1 = FileUtil.resolvePath(path, list);
         if (Files.exists(path1) && PathPackResources.validatePath(path1)) {
            return IoSupplier.create(path1);
         }
      }

      return null;
   }

   public void listRawPaths(PackType packtype, ResourceLocation resourcelocation, Consumer<Path> consumer) {
      FileUtil.decomposePath(resourcelocation.getPath()).get().ifLeft((list) -> {
         String s = resourcelocation.getNamespace();

         for(Path path : this.pathsForType.get(packtype)) {
            Path path1 = path.resolve(s);
            consumer.accept(FileUtil.resolvePath(path1, list));
         }

      }).ifRight((dataresult_partialresult) -> LOGGER.error("Invalid path {}: {}", resourcelocation, dataresult_partialresult.message()));
   }

   public void listResources(PackType packtype, String s, String s1, PackResources.ResourceOutput packresources_resourceoutput) {
      FileUtil.decomposePath(s1).get().ifLeft((list) -> {
         List<Path> list1 = this.pathsForType.get(packtype);
         int i = list1.size();
         if (i == 1) {
            getResources(packresources_resourceoutput, s, list1.get(0), list);
         } else if (i > 1) {
            Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();

            for(int j = 0; j < i - 1; ++j) {
               getResources(map::putIfAbsent, s, list1.get(j), list);
            }

            Path path = list1.get(i - 1);
            if (map.isEmpty()) {
               getResources(packresources_resourceoutput, s, path, list);
            } else {
               getResources(map::putIfAbsent, s, path, list);
               map.forEach(packresources_resourceoutput);
            }
         }

      }).ifRight((dataresult_partialresult) -> LOGGER.error("Invalid path {}: {}", s1, dataresult_partialresult.message()));
   }

   private static void getResources(PackResources.ResourceOutput packresources_resourceoutput, String s, Path path, List<String> list) {
      Path path1 = path.resolve(s);
      PathPackResources.listPath(s, path1, list, packresources_resourceoutput);
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType packtype, ResourceLocation resourcelocation) {
      return FileUtil.decomposePath(resourcelocation.getPath()).get().map((list) -> {
         String s = resourcelocation.getNamespace();

         for(Path path : this.pathsForType.get(packtype)) {
            Path path1 = FileUtil.resolvePath(path.resolve(s), list);
            if (Files.exists(path1) && PathPackResources.validatePath(path1)) {
               return IoSupplier.create(path1);
            }
         }

         return null;
      }, (dataresult_partialresult) -> {
         LOGGER.error("Invalid path {}: {}", resourcelocation, dataresult_partialresult.message());
         return null;
      });
   }

   public Set<String> getNamespaces(PackType packtype) {
      return this.namespaces;
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> metadatasectionserializer) {
      IoSupplier<InputStream> iosupplier = this.getRootResource("pack.mcmeta");
      if (iosupplier != null) {
         try {
            InputStream inputstream = iosupplier.get();

            Object var5;
            label54: {
               try {
                  T object = AbstractPackResources.getMetadataFromStream(metadatasectionserializer, inputstream);
                  if (object != null) {
                     var5 = object;
                     break label54;
                  }
               } catch (Throwable var7) {
                  if (inputstream != null) {
                     try {
                        inputstream.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (inputstream != null) {
                  inputstream.close();
               }

               return this.metadata.get(metadatasectionserializer);
            }

            if (inputstream != null) {
               inputstream.close();
            }

            return (T)var5;
         } catch (IOException var8) {
         }
      }

      return this.metadata.get(metadatasectionserializer);
   }

   public String packId() {
      return "vanilla";
   }

   public boolean isBuiltin() {
      return true;
   }

   public void close() {
   }

   public ResourceProvider asProvider() {
      return (resourcelocation) -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, resourcelocation)).map((iosupplier) -> new Resource(this, iosupplier));
   }
}
