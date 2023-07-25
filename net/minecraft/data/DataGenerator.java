package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path rootOutputFolder;
   private final PackOutput vanillaPackOutput;
   final Set<String> allProviderIds = new HashSet<>();
   final Map<String, DataProvider> providersToRun = new LinkedHashMap<>();
   private final WorldVersion version;
   private final boolean alwaysGenerate;

   public DataGenerator(Path path, WorldVersion worldversion, boolean flag) {
      this.rootOutputFolder = path;
      this.vanillaPackOutput = new PackOutput(this.rootOutputFolder);
      this.version = worldversion;
      this.alwaysGenerate = flag;
   }

   public void run() throws IOException {
      HashCache hashcache = new HashCache(this.rootOutputFolder, this.allProviderIds, this.version);
      Stopwatch stopwatch = Stopwatch.createStarted();
      Stopwatch stopwatch1 = Stopwatch.createUnstarted();
      this.providersToRun.forEach((s, dataprovider) -> {
         if (!this.alwaysGenerate && !hashcache.shouldRunInThisVersion(s)) {
            LOGGER.debug("Generator {} already run for version {}", s, this.version.getName());
         } else {
            LOGGER.info("Starting provider: {}", (Object)s);
            stopwatch1.start();
            hashcache.applyUpdate(hashcache.generateUpdate(s, dataprovider::run).join());
            stopwatch1.stop();
            LOGGER.info("{} finished after {} ms", s, stopwatch1.elapsed(TimeUnit.MILLISECONDS));
            stopwatch1.reset();
         }
      });
      LOGGER.info("All providers took: {} ms", (long)stopwatch.elapsed(TimeUnit.MILLISECONDS));
      hashcache.purgeStaleAndWrite();
   }

   public DataGenerator.PackGenerator getVanillaPack(boolean flag) {
      return new DataGenerator.PackGenerator(flag, "vanilla", this.vanillaPackOutput);
   }

   public DataGenerator.PackGenerator getBuiltinDatapack(boolean flag, String s) {
      Path path = this.vanillaPackOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve(s);
      return new DataGenerator.PackGenerator(flag, s, new PackOutput(path));
   }

   static {
      Bootstrap.bootStrap();
   }

   public class PackGenerator {
      private final boolean toRun;
      private final String providerPrefix;
      private final PackOutput output;

      PackGenerator(boolean flag, String s, PackOutput packoutput) {
         this.toRun = flag;
         this.providerPrefix = s;
         this.output = packoutput;
      }

      public <T extends DataProvider> T addProvider(DataProvider.Factory<T> dataprovider_factory) {
         T dataprovider = dataprovider_factory.create(this.output);
         String s = this.providerPrefix + "/" + dataprovider.getName();
         if (!DataGenerator.this.allProviderIds.add(s)) {
            throw new IllegalStateException("Duplicate provider: " + s);
         } else {
            if (this.toRun) {
               DataGenerator.this.providersToRun.put(s, dataprovider);
            }

            return dataprovider;
         }
      }
   }
}
