package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String STRUCTURE_DIRECTORY_NAME = "structures";
   private static final String TEST_STRUCTURES_DIR = "gameteststructures";
   private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
   private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
   private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
   private final DataFixer fixerUpper;
   private ResourceManager resourceManager;
   private final Path generatedDir;
   private final List<StructureTemplateManager.Source> sources;
   private final HolderGetter<Block> blockLookup;
   private static final FileToIdConverter LISTER = new FileToIdConverter("structures", ".nbt");

   public StructureTemplateManager(ResourceManager resourcemanager, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer, HolderGetter<Block> holdergetter) {
      this.resourceManager = resourcemanager;
      this.fixerUpper = datafixer;
      this.generatedDir = levelstoragesource_levelstorageaccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
      this.blockLookup = holdergetter;
      ImmutableList.Builder<StructureTemplateManager.Source> immutablelist_builder = ImmutableList.builder();
      immutablelist_builder.add(new StructureTemplateManager.Source(this::loadFromGenerated, this::listGenerated));
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         immutablelist_builder.add(new StructureTemplateManager.Source(this::loadFromTestStructures, this::listTestStructures));
      }

      immutablelist_builder.add(new StructureTemplateManager.Source(this::loadFromResource, this::listResources));
      this.sources = immutablelist_builder.build();
   }

   public StructureTemplate getOrCreate(ResourceLocation resourcelocation) {
      Optional<StructureTemplate> optional = this.get(resourcelocation);
      if (optional.isPresent()) {
         return optional.get();
      } else {
         StructureTemplate structuretemplate = new StructureTemplate();
         this.structureRepository.put(resourcelocation, Optional.of(structuretemplate));
         return structuretemplate;
      }
   }

   public Optional<StructureTemplate> get(ResourceLocation resourcelocation) {
      return this.structureRepository.computeIfAbsent(resourcelocation, this::tryLoad);
   }

   public Stream<ResourceLocation> listTemplates() {
      return this.sources.stream().flatMap((structuretemplatemanager_source) -> structuretemplatemanager_source.lister().get()).distinct();
   }

   private Optional<StructureTemplate> tryLoad(ResourceLocation resourcelocation1) {
      for(StructureTemplateManager.Source structuretemplatemanager_source : this.sources) {
         try {
            Optional<StructureTemplate> optional = structuretemplatemanager_source.loader().apply(resourcelocation1);
            if (optional.isPresent()) {
               return optional;
            }
         } catch (Exception var5) {
         }
      }

      return Optional.empty();
   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      this.resourceManager = resourcemanager;
      this.structureRepository.clear();
   }

   private Optional<StructureTemplate> loadFromResource(ResourceLocation resourcelocation) {
      ResourceLocation resourcelocation1 = LISTER.idToFile(resourcelocation);
      return this.load(() -> this.resourceManager.open(resourcelocation1), (throwable1) -> LOGGER.error("Couldn't load structure {}", resourcelocation, throwable1));
   }

   private Stream<ResourceLocation> listResources() {
      return LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(LISTER::fileToId);
   }

   private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation resourcelocation2) {
      return this.loadFromSnbt(resourcelocation2, Paths.get("gameteststructures"));
   }

   private Stream<ResourceLocation> listTestStructures() {
      return this.listFolderContents(Paths.get("gameteststructures"), "minecraft", ".snbt");
   }

   private Optional<StructureTemplate> loadFromGenerated(ResourceLocation resourcelocation3) {
      if (!Files.isDirectory(this.generatedDir)) {
         return Optional.empty();
      } else {
         Path path = createAndValidatePathToStructure(this.generatedDir, resourcelocation3, ".nbt");
         return this.load(() -> new FileInputStream(path.toFile()), (throwable) -> LOGGER.error("Couldn't load structure from {}", path, throwable));
      }
   }

   private Stream<ResourceLocation> listGenerated() {
      if (!Files.isDirectory(this.generatedDir)) {
         return Stream.empty();
      } else {
         try {
            return Files.list(this.generatedDir).filter((path2) -> Files.isDirectory(path2)).flatMap((path1) -> this.listGeneratedInNamespace(path1));
         } catch (IOException var2) {
            return Stream.empty();
         }
      }
   }

   private Stream<ResourceLocation> listGeneratedInNamespace(Path path) {
      Path path1 = path.resolve("structures");
      return this.listFolderContents(path1, path.getFileName().toString(), ".nbt");
   }

   private Stream<ResourceLocation> listFolderContents(Path path, String s, String s1) {
      if (!Files.isDirectory(path)) {
         return Stream.empty();
      } else {
         int i = s1.length();
         Function<String, String> function = (s4) -> s4.substring(0, s4.length() - i);

         try {
            return Files.walk(path).filter((path3) -> path3.toString().endsWith(s1)).mapMulti((path2, consumer) -> {
               try {
                  consumer.accept(new ResourceLocation(s, function.apply(this.relativize(path, path2))));
               } catch (ResourceLocationException var7) {
                  LOGGER.error("Invalid location while listing pack contents", (Throwable)var7);
               }

            });
         } catch (IOException var7) {
            LOGGER.error("Failed to list folder contents", (Throwable)var7);
            return Stream.empty();
         }
      }
   }

   private String relativize(Path path, Path path1) {
      return path.relativize(path1).toString().replace(File.separator, "/");
   }

   private Optional<StructureTemplate> loadFromSnbt(ResourceLocation resourcelocation, Path path) {
      if (!Files.isDirectory(path)) {
         return Optional.empty();
      } else {
         Path path1 = FileUtil.createPathToResource(path, resourcelocation.getPath(), ".snbt");

         try {
            BufferedReader bufferedreader = Files.newBufferedReader(path1);

            Optional var6;
            try {
               String s = IOUtils.toString((Reader)bufferedreader);
               var6 = Optional.of(this.readStructure(NbtUtils.snbtToStructure(s)));
            } catch (Throwable var8) {
               if (bufferedreader != null) {
                  try {
                     bufferedreader.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (bufferedreader != null) {
               bufferedreader.close();
            }

            return var6;
         } catch (NoSuchFileException var9) {
            return Optional.empty();
         } catch (CommandSyntaxException | IOException var10) {
            LOGGER.error("Couldn't load structure from {}", path1, var10);
            return Optional.empty();
         }
      }
   }

   private Optional<StructureTemplate> load(StructureTemplateManager.InputStreamOpener structuretemplatemanager_inputstreamopener, Consumer<Throwable> consumer) {
      try {
         InputStream inputstream = structuretemplatemanager_inputstreamopener.open();

         Optional var4;
         try {
            var4 = Optional.of(this.readStructure(inputstream));
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

         return var4;
      } catch (FileNotFoundException var8) {
         return Optional.empty();
      } catch (Throwable var9) {
         consumer.accept(var9);
         return Optional.empty();
      }
   }

   private StructureTemplate readStructure(InputStream inputstream) throws IOException {
      CompoundTag compoundtag = NbtIo.readCompressed(inputstream);
      return this.readStructure(compoundtag);
   }

   public StructureTemplate readStructure(CompoundTag compoundtag) {
      StructureTemplate structuretemplate = new StructureTemplate();
      int i = NbtUtils.getDataVersion(compoundtag, 500);
      structuretemplate.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, compoundtag, i));
      return structuretemplate;
   }

   public boolean save(ResourceLocation resourcelocation) {
      Optional<StructureTemplate> optional = this.structureRepository.get(resourcelocation);
      if (!optional.isPresent()) {
         return false;
      } else {
         StructureTemplate structuretemplate = optional.get();
         Path path = createAndValidatePathToStructure(this.generatedDir, resourcelocation, ".nbt");
         Path path1 = path.getParent();
         if (path1 == null) {
            return false;
         } else {
            try {
               Files.createDirectories(Files.exists(path1) ? path1.toRealPath() : path1);
            } catch (IOException var13) {
               LOGGER.error("Failed to create parent directory: {}", (Object)path1);
               return false;
            }

            CompoundTag compoundtag = structuretemplate.save(new CompoundTag());

            try {
               OutputStream outputstream = new FileOutputStream(path.toFile());

               try {
                  NbtIo.writeCompressed(compoundtag, outputstream);
               } catch (Throwable var11) {
                  try {
                     outputstream.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }

                  throw var11;
               }

               outputstream.close();
               return true;
            } catch (Throwable var12) {
               return false;
            }
         }
      }
   }

   public Path getPathToGeneratedStructure(ResourceLocation resourcelocation, String s) {
      return createPathToStructure(this.generatedDir, resourcelocation, s);
   }

   public static Path createPathToStructure(Path path, ResourceLocation resourcelocation, String s) {
      try {
         Path path1 = path.resolve(resourcelocation.getNamespace());
         Path path2 = path1.resolve("structures");
         return FileUtil.createPathToResource(path2, resourcelocation.getPath(), s);
      } catch (InvalidPathException var5) {
         throw new ResourceLocationException("Invalid resource path: " + resourcelocation, var5);
      }
   }

   private static Path createAndValidatePathToStructure(Path path, ResourceLocation resourcelocation, String s) {
      if (resourcelocation.getPath().contains("//")) {
         throw new ResourceLocationException("Invalid resource path: " + resourcelocation);
      } else {
         Path path1 = createPathToStructure(path, resourcelocation, s);
         if (path1.startsWith(path) && FileUtil.isPathNormalized(path1) && FileUtil.isPathPortable(path1)) {
            return path1;
         } else {
            throw new ResourceLocationException("Invalid resource path: " + path1);
         }
      }
   }

   public void remove(ResourceLocation resourcelocation) {
      this.structureRepository.remove(resourcelocation);
   }

   @FunctionalInterface
   interface InputStreamOpener {
      InputStream open() throws IOException;
   }

   static record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
   }
}
