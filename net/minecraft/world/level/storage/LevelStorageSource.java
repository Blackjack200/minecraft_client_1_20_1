package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
import org.slf4j.Logger;

public class LevelStorageSource {
   static final Logger LOGGER = LogUtils.getLogger();
   static final DateTimeFormatter FORMATTER = (new DateTimeFormatterBuilder()).appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
   private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
   private static final String TAG_DATA = "Data";
   private static final PathAllowList NO_SYMLINKS_ALLOWED = new PathAllowList(List.of());
   public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
   private final Path baseDir;
   private final Path backupDir;
   final DataFixer fixerUpper;
   private final DirectoryValidator worldDirValidator;

   public LevelStorageSource(Path path, Path path1, DirectoryValidator directoryvalidator, DataFixer datafixer) {
      this.fixerUpper = datafixer;

      try {
         FileUtil.createDirectoriesSafe(path);
      } catch (IOException var6) {
         throw new UncheckedIOException(var6);
      }

      this.baseDir = path;
      this.backupDir = path1;
      this.worldDirValidator = directoryvalidator;
   }

   public static DirectoryValidator parseValidator(Path path) {
      if (Files.exists(path)) {
         try {
            BufferedReader bufferedreader = Files.newBufferedReader(path);

            DirectoryValidator var2;
            try {
               var2 = new DirectoryValidator(PathAllowList.readPlain(bufferedreader));
            } catch (Throwable var5) {
               if (bufferedreader != null) {
                  try {
                     bufferedreader.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (bufferedreader != null) {
               bufferedreader.close();
            }

            return var2;
         } catch (Exception var6) {
            LOGGER.error("Failed to parse {}, disallowing all symbolic links", "allowed_symlinks.txt", var6);
         }
      }

      return new DirectoryValidator(NO_SYMLINKS_ALLOWED);
   }

   public static LevelStorageSource createDefault(Path path) {
      DirectoryValidator directoryvalidator = parseValidator(path.resolve("allowed_symlinks.txt"));
      return new LevelStorageSource(path, path.resolve("../backups"), directoryvalidator, DataFixers.getDataFixer());
   }

   private static <T> DataResult<WorldGenSettings> readWorldGenSettings(Dynamic<T> dynamic, DataFixer datafixer, int i) {
      Dynamic<T> dynamic1 = dynamic.get("WorldGenSettings").orElseEmptyMap();

      for(String s : OLD_SETTINGS_KEYS) {
         Optional<Dynamic<T>> optional = dynamic.get(s).result();
         if (optional.isPresent()) {
            dynamic1 = dynamic1.set(s, optional.get());
         }
      }

      Dynamic<T> dynamic2 = DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(datafixer, dynamic1, i);
      return WorldGenSettings.CODEC.parse(dynamic2);
   }

   private static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
      return WorldDataConfiguration.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
   }

   public String getName() {
      return "Anvil";
   }

   public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
      if (!Files.isDirectory(this.baseDir)) {
         throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
      } else {
         try {
            Stream<Path> stream = Files.list(this.baseDir);

            LevelStorageSource.LevelCandidates var3;
            try {
               List<LevelStorageSource.LevelDirectory> list = stream.filter((path) -> Files.isDirectory(path)).map(LevelStorageSource.LevelDirectory::new).filter((levelstoragesource_leveldirectory) -> Files.isRegularFile(levelstoragesource_leveldirectory.dataFile()) || Files.isRegularFile(levelstoragesource_leveldirectory.oldDataFile())).toList();
               var3 = new LevelStorageSource.LevelCandidates(list);
            } catch (Throwable var5) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (stream != null) {
               stream.close();
            }

            return var3;
         } catch (IOException var6) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
         }
      }
   }

   public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates levelstoragesource_levelcandidates) {
      List<CompletableFuture<LevelSummary>> list = new ArrayList<>(levelstoragesource_levelcandidates.levels.size());

      for(LevelStorageSource.LevelDirectory levelstoragesource_leveldirectory : levelstoragesource_levelcandidates.levels) {
         list.add(CompletableFuture.supplyAsync(() -> {
            boolean flag;
            try {
               flag = DirectoryLock.isLocked(levelstoragesource_leveldirectory.path());
            } catch (Exception var6) {
               LOGGER.warn("Failed to read {} lock", levelstoragesource_leveldirectory.path(), var6);
               return null;
            }

            try {
               LevelSummary levelsummary = this.readLevelData(levelstoragesource_leveldirectory, this.levelSummaryReader(levelstoragesource_leveldirectory, flag));
               return levelsummary != null ? levelsummary : null;
            } catch (OutOfMemoryError var4) {
               MemoryReserve.release();
               System.gc();
               LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", (Object)levelstoragesource_leveldirectory.directoryName());
               throw var4;
            } catch (StackOverflowError var5) {
               LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.", (Object)levelstoragesource_leveldirectory.directoryName());
               Util.safeReplaceOrMoveFile(levelstoragesource_leveldirectory.dataFile(), levelstoragesource_leveldirectory.oldDataFile(), levelstoragesource_leveldirectory.corruptedDataFile(LocalDateTime.now()), true);
               throw var5;
            }
         }, Util.backgroundExecutor()));
      }

      return Util.sequenceFailFastAndCancel(list).thenApply((list1) -> list1.stream().filter(Objects::nonNull).sorted().toList());
   }

   private int getStorageVersion() {
      return 19133;
   }

   @Nullable
   <T> T readLevelData(LevelStorageSource.LevelDirectory levelstoragesource_leveldirectory, BiFunction<Path, DataFixer, T> bifunction) {
      if (!Files.exists(levelstoragesource_leveldirectory.path())) {
         return (T)null;
      } else {
         Path path = levelstoragesource_leveldirectory.dataFile();
         if (Files.exists(path)) {
            T object = bifunction.apply(path, this.fixerUpper);
            if (object != null) {
               return object;
            }
         }

         path = levelstoragesource_leveldirectory.oldDataFile();
         return (T)(Files.exists(path) ? bifunction.apply(path, this.fixerUpper) : null);
      }
   }

   @Nullable
   private static WorldDataConfiguration getDataConfiguration(Path path, DataFixer datafixer) {
      try {
         Tag tag = readLightweightData(path);
         if (tag instanceof CompoundTag compoundtag) {
            CompoundTag compoundtag1 = compoundtag.getCompound("Data");
            int i = NbtUtils.getDataVersion(compoundtag1, -1);
            Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(datafixer, new Dynamic<>(NbtOps.INSTANCE, compoundtag1), i);
            return readDataConfig(dynamic);
         }
      } catch (Exception var7) {
         LOGGER.error("Exception reading {}", path, var7);
      }

      return null;
   }

   static BiFunction<Path, DataFixer, Pair<WorldData, WorldDimensions.Complete>> getLevelData(DynamicOps<Tag> dynamicops, WorldDataConfiguration worlddataconfiguration, Registry<LevelStem> registry, Lifecycle lifecycle) {
      return (path, datafixer) -> {
         CompoundTag compoundtag;
         try {
            compoundtag = NbtIo.readCompressed(path.toFile());
         } catch (IOException var17) {
            throw new UncheckedIOException(var17);
         }

         CompoundTag compoundtag2 = compoundtag.getCompound("Data");
         CompoundTag compoundtag3 = compoundtag2.contains("Player", 10) ? compoundtag2.getCompound("Player") : null;
         compoundtag2.remove("Player");
         int i = NbtUtils.getDataVersion(compoundtag2, -1);
         Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(datafixer, new Dynamic<>(dynamicops, compoundtag2), i);
         WorldGenSettings worldgensettings = readWorldGenSettings(dynamic, datafixer, i).getOrThrow(false, Util.prefix("WorldGenSettings: ", LOGGER::error));
         LevelVersion levelversion = LevelVersion.parse(dynamic);
         LevelSettings levelsettings = LevelSettings.parse(dynamic, worlddataconfiguration);
         WorldDimensions.Complete worlddimensions_complete = worldgensettings.dimensions().bake(registry);
         Lifecycle lifecycle2 = worlddimensions_complete.lifecycle().add(lifecycle);
         PrimaryLevelData primaryleveldata = PrimaryLevelData.parse(dynamic, datafixer, i, compoundtag3, levelsettings, levelversion, worlddimensions_complete.specialWorldProperty(), worldgensettings.options(), lifecycle2);
         return Pair.of(primaryleveldata, worlddimensions_complete);
      };
   }

   BiFunction<Path, DataFixer, LevelSummary> levelSummaryReader(LevelStorageSource.LevelDirectory levelstoragesource_leveldirectory, boolean flag) {
      return (path, datafixer) -> {
         try {
            if (Files.isSymbolicLink(path)) {
               List<ForbiddenSymlinkInfo> list = new ArrayList<>();
               this.worldDirValidator.validateSymlink(path, list);
               if (!list.isEmpty()) {
                  LOGGER.warn(ContentValidationException.getMessage(path, list));
                  return new LevelSummary.SymlinkLevelSummary(levelstoragesource_leveldirectory.directoryName(), levelstoragesource_leveldirectory.iconFile());
               }
            }

            Tag tag = readLightweightData(path);
            if (tag instanceof CompoundTag compoundtag) {
               CompoundTag compoundtag1 = compoundtag.getCompound("Data");
               int i = NbtUtils.getDataVersion(compoundtag1, -1);
               Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(datafixer, new Dynamic<>(NbtOps.INSTANCE, compoundtag1), i);
               LevelVersion levelversion = LevelVersion.parse(dynamic);
               int j = levelversion.levelDataVersion();
               if (j == 19132 || j == 19133) {
                  boolean flag2 = j != this.getStorageVersion();
                  Path path1 = levelstoragesource_leveldirectory.iconFile();
                  WorldDataConfiguration worlddataconfiguration = readDataConfig(dynamic);
                  LevelSettings levelsettings = LevelSettings.parse(dynamic, worlddataconfiguration);
                  FeatureFlagSet featureflagset = parseFeatureFlagsFromSummary(dynamic);
                  boolean flag3 = FeatureFlags.isExperimental(featureflagset);
                  return new LevelSummary(levelsettings, levelversion, levelstoragesource_leveldirectory.directoryName(), flag2, flag, flag3, path1);
               }
            } else {
               LOGGER.warn("Invalid root tag in {}", (Object)path);
            }

            return null;
         } catch (Exception var18) {
            LOGGER.error("Exception reading {}", path, var18);
            return null;
         }
      };
   }

   private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> dynamic) {
      Set<ResourceLocation> set = dynamic.get("enabled_features").asStream().flatMap((dynamic1) -> dynamic1.asString().result().map(ResourceLocation::tryParse).stream()).collect(Collectors.toSet());
      return FeatureFlags.REGISTRY.fromNames(set, (resourcelocation) -> {
      });
   }

   @Nullable
   private static Tag readLightweightData(Path path) throws IOException {
      SkipFields skipfields = new SkipFields(new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings"));
      NbtIo.parseCompressed(path.toFile(), skipfields);
      return skipfields.getResult();
   }

   public boolean isNewLevelIdAcceptable(String s) {
      try {
         Path path = this.getLevelPath(s);
         Files.createDirectory(path);
         Files.deleteIfExists(path);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public boolean levelExists(String s) {
      return Files.isDirectory(this.getLevelPath(s));
   }

   private Path getLevelPath(String s) {
      return this.baseDir.resolve(s);
   }

   public Path getBaseDir() {
      return this.baseDir;
   }

   public Path getBackupPath() {
      return this.backupDir;
   }

   public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String s) throws IOException, ContentValidationException {
      Path path = this.getLevelPath(s);
      List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateSave(path, true);
      if (!list.isEmpty()) {
         throw new ContentValidationException(path, list);
      } else {
         return new LevelStorageSource.LevelStorageAccess(s, path);
      }
   }

   public LevelStorageSource.LevelStorageAccess createAccess(String s) throws IOException {
      Path path = this.getLevelPath(s);
      return new LevelStorageSource.LevelStorageAccess(s, path);
   }

   public DirectoryValidator getWorldDirValidator() {
      return this.worldDirValidator;
   }

   public static record LevelCandidates(List<LevelStorageSource.LevelDirectory> levels) implements Iterable<LevelStorageSource.LevelDirectory> {
      final List<LevelStorageSource.LevelDirectory> levels;

      public boolean isEmpty() {
         return this.levels.isEmpty();
      }

      public Iterator<LevelStorageSource.LevelDirectory> iterator() {
         return this.levels.iterator();
      }
   }

   public static record LevelDirectory(Path path) {
      public String directoryName() {
         return this.path.getFileName().toString();
      }

      public Path dataFile() {
         return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
      }

      public Path oldDataFile() {
         return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
      }

      public Path corruptedDataFile(LocalDateTime localdatetime) {
         return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + localdatetime.format(LevelStorageSource.FORMATTER));
      }

      public Path iconFile() {
         return this.resourcePath(LevelResource.ICON_FILE);
      }

      public Path lockFile() {
         return this.resourcePath(LevelResource.LOCK_FILE);
      }

      public Path resourcePath(LevelResource levelresource) {
         return this.path.resolve(levelresource.getId());
      }
   }

   public class LevelStorageAccess implements AutoCloseable {
      final DirectoryLock lock;
      final LevelStorageSource.LevelDirectory levelDirectory;
      private final String levelId;
      private final Map<LevelResource, Path> resources = Maps.newHashMap();

      LevelStorageAccess(String s, Path path) throws IOException {
         this.levelId = s;
         this.levelDirectory = new LevelStorageSource.LevelDirectory(path);
         this.lock = DirectoryLock.create(path);
      }

      public String getLevelId() {
         return this.levelId;
      }

      public Path getLevelPath(LevelResource levelresource) {
         return this.resources.computeIfAbsent(levelresource, this.levelDirectory::resourcePath);
      }

      public Path getDimensionPath(ResourceKey<Level> resourcekey) {
         return DimensionType.getStorageFolder(resourcekey, this.levelDirectory.path());
      }

      private void checkLock() {
         if (!this.lock.isValid()) {
            throw new IllegalStateException("Lock is no longer valid");
         }
      }

      public PlayerDataStorage createPlayerStorage() {
         this.checkLock();
         return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
      }

      @Nullable
      public LevelSummary getSummary() {
         this.checkLock();
         return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.this.levelSummaryReader(this.levelDirectory, false));
      }

      @Nullable
      public Pair<WorldData, WorldDimensions.Complete> getDataTag(DynamicOps<Tag> dynamicops, WorldDataConfiguration worlddataconfiguration, Registry<LevelStem> registry, Lifecycle lifecycle) {
         this.checkLock();
         return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.getLevelData(dynamicops, worlddataconfiguration, registry, lifecycle));
      }

      @Nullable
      public WorldDataConfiguration getDataConfiguration() {
         this.checkLock();
         return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource::getDataConfiguration);
      }

      public void saveDataTag(RegistryAccess registryaccess, WorldData worlddata) {
         this.saveDataTag(registryaccess, worlddata, (CompoundTag)null);
      }

      public void saveDataTag(RegistryAccess registryaccess, WorldData worlddata, @Nullable CompoundTag compoundtag) {
         File file = this.levelDirectory.path().toFile();
         CompoundTag compoundtag1 = worlddata.createTag(registryaccess, compoundtag);
         CompoundTag compoundtag2 = new CompoundTag();
         compoundtag2.put("Data", compoundtag1);

         try {
            File file1 = File.createTempFile("level", ".dat", file);
            NbtIo.writeCompressed(compoundtag2, file1);
            File file2 = this.levelDirectory.oldDataFile().toFile();
            File file3 = this.levelDirectory.dataFile().toFile();
            Util.safeReplaceFile(file3, file1, file2);
         } catch (Exception var10) {
            LevelStorageSource.LOGGER.error("Failed to save level {}", file, var10);
         }

      }

      public Optional<Path> getIconFile() {
         return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
      }

      public void deleteLevel() throws IOException {
         this.checkLock();
         final Path path = this.levelDirectory.lockFile();
         LevelStorageSource.LOGGER.info("Deleting level {}", (Object)this.levelId);

         for(int i = 1; i <= 5; ++i) {
            LevelStorageSource.LOGGER.info("Attempt {}...", (int)i);

            try {
               Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                  public FileVisitResult visitFile(Path pathx, BasicFileAttributes basicfileattributes) throws IOException {
                     if (!path.equals(path)) {
                        LevelStorageSource.LOGGER.debug("Deleting {}", (Object)path);
                        Files.delete(path);
                     }

                     return FileVisitResult.CONTINUE;
                  }

                  public FileVisitResult postVisitDirectory(Path pathx, @Nullable IOException ioexception) throws IOException {
                     if (ioexception != null) {
                        throw ioexception;
                     } else {
                        if (path.equals(LevelStorageAccess.this.levelDirectory.path())) {
                           LevelStorageAccess.this.lock.close();
                           Files.deleteIfExists(path);
                        }

                        Files.delete(path);
                        return FileVisitResult.CONTINUE;
                     }
                  }
               });
               break;
            } catch (IOException var6) {
               if (i >= 5) {
                  throw var6;
               }

               LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), var6);

               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
               }
            }
         }

      }

      public void renameLevel(String s) throws IOException {
         this.checkLock();
         Path path = this.levelDirectory.dataFile();
         if (Files.exists(path)) {
            CompoundTag compoundtag = NbtIo.readCompressed(path.toFile());
            CompoundTag compoundtag1 = compoundtag.getCompound("Data");
            compoundtag1.putString("LevelName", s);
            NbtIo.writeCompressed(compoundtag, path.toFile());
         }

      }

      public long makeWorldBackup() throws IOException {
         this.checkLock();
         String s = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
         Path path = LevelStorageSource.this.getBackupPath();

         try {
            FileUtil.createDirectoriesSafe(path);
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }

         Path path1 = path.resolve(FileUtil.findAvailableName(path, s, ".zip"));
         final ZipOutputStream zipoutputstream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path1)));

         try {
            final Path path2 = Paths.get(this.levelId);
            Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
               public FileVisitResult visitFile(Path path, BasicFileAttributes basicfileattributes) throws IOException {
                  if (path.endsWith("session.lock")) {
                     return FileVisitResult.CONTINUE;
                  } else {
                     String s = path2.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(path)).toString().replace('\\', '/');
                     ZipEntry zipentry = new ZipEntry(s);
                     zipoutputstream.putNextEntry(zipentry);
                     com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipoutputstream);
                     zipoutputstream.closeEntry();
                     return FileVisitResult.CONTINUE;
                  }
               }
            });
         } catch (Throwable var8) {
            try {
               zipoutputstream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         zipoutputstream.close();
         return Files.size(path1);
      }

      public void close() throws IOException {
         this.lock.close();
      }
   }
}
