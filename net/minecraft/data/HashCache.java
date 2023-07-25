package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.WorldVersion;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String HEADER_MARKER = "// ";
   private final Path rootDir;
   private final Path cacheDir;
   private final String versionId;
   private final Map<String, HashCache.ProviderCache> caches;
   private final Set<String> cachesToWrite = new HashSet<>();
   private final Set<Path> cachePaths = new HashSet<>();
   private final int initialCount;
   private int writes;

   private Path getProviderCachePath(String s) {
      return this.cacheDir.resolve(Hashing.sha1().hashString(s, StandardCharsets.UTF_8).toString());
   }

   public HashCache(Path path, Collection<String> collection, WorldVersion worldversion) throws IOException {
      this.versionId = worldversion.getName();
      this.rootDir = path;
      this.cacheDir = path.resolve(".cache");
      Files.createDirectories(this.cacheDir);
      Map<String, HashCache.ProviderCache> map = new HashMap<>();
      int i = 0;

      for(String s : collection) {
         Path path1 = this.getProviderCachePath(s);
         this.cachePaths.add(path1);
         HashCache.ProviderCache hashcache_providercache = readCache(path, path1);
         map.put(s, hashcache_providercache);
         i += hashcache_providercache.count();
      }

      this.caches = map;
      this.initialCount = i;
   }

   private static HashCache.ProviderCache readCache(Path path, Path path1) {
      if (Files.isReadable(path1)) {
         try {
            return HashCache.ProviderCache.load(path, path1);
         } catch (Exception var3) {
            LOGGER.warn("Failed to parse cache {}, discarding", path1, var3);
         }
      }

      return new HashCache.ProviderCache("unknown", ImmutableMap.of());
   }

   public boolean shouldRunInThisVersion(String s) {
      HashCache.ProviderCache hashcache_providercache = this.caches.get(s);
      return hashcache_providercache == null || !hashcache_providercache.version.equals(this.versionId);
   }

   public CompletableFuture<HashCache.UpdateResult> generateUpdate(String s, HashCache.UpdateFunction hashcache_updatefunction) {
      HashCache.ProviderCache hashcache_providercache = this.caches.get(s);
      if (hashcache_providercache == null) {
         throw new IllegalStateException("Provider not registered: " + s);
      } else {
         HashCache.CacheUpdater hashcache_cacheupdater = new HashCache.CacheUpdater(s, this.versionId, hashcache_providercache);
         return hashcache_updatefunction.update(hashcache_cacheupdater).thenApply((object) -> hashcache_cacheupdater.close());
      }
   }

   public void applyUpdate(HashCache.UpdateResult hashcache_updateresult) {
      this.caches.put(hashcache_updateresult.providerId(), hashcache_updateresult.cache());
      this.cachesToWrite.add(hashcache_updateresult.providerId());
      this.writes += hashcache_updateresult.writes();
   }

   public void purgeStaleAndWrite() throws IOException {
      Set<Path> set = new HashSet<>();
      this.caches.forEach((s, hashcache_providercache) -> {
         if (this.cachesToWrite.contains(s)) {
            Path path1 = this.getProviderCachePath(s);
            hashcache_providercache.save(this.rootDir, path1, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + s);
         }

         set.addAll(hashcache_providercache.data().keySet());
      });
      set.add(this.rootDir.resolve("version.json"));
      MutableInt mutableint = new MutableInt();
      MutableInt mutableint1 = new MutableInt();
      Stream<Path> stream = Files.walk(this.rootDir);

      try {
         stream.forEach((path) -> {
            if (!Files.isDirectory(path)) {
               if (!this.cachePaths.contains(path)) {
                  mutableint.increment();
                  if (!set.contains(path)) {
                     try {
                        Files.delete(path);
                     } catch (IOException var6) {
                        LOGGER.warn("Failed to delete file {}", path, var6);
                     }

                     mutableint1.increment();
                  }
               }
            }
         });
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

      LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", mutableint, this.initialCount, set.size(), mutableint1, this.writes);
   }

   class CacheUpdater implements CachedOutput {
      private final String provider;
      private final HashCache.ProviderCache oldCache;
      private final HashCache.ProviderCacheBuilder newCache;
      private final AtomicInteger writes = new AtomicInteger();
      private volatile boolean closed;

      CacheUpdater(String s, String s1, HashCache.ProviderCache hashcache_providercache) {
         this.provider = s;
         this.oldCache = hashcache_providercache;
         this.newCache = new HashCache.ProviderCacheBuilder(s1);
      }

      private boolean shouldWrite(Path path, HashCode hashcode) {
         return !Objects.equals(this.oldCache.get(path), hashcode) || !Files.exists(path);
      }

      public void writeIfNeeded(Path path, byte[] abyte, HashCode hashcode) throws IOException {
         if (this.closed) {
            throw new IllegalStateException("Cannot write to cache as it has already been closed");
         } else {
            if (this.shouldWrite(path, hashcode)) {
               this.writes.incrementAndGet();
               Files.createDirectories(path.getParent());
               Files.write(path, abyte);
            }

            this.newCache.put(path, hashcode);
         }
      }

      public HashCache.UpdateResult close() {
         this.closed = true;
         return new HashCache.UpdateResult(this.provider, this.newCache.build(), this.writes.get());
      }
   }

   static record ProviderCache(String version, ImmutableMap<Path, HashCode> data) {
      final String version;

      @Nullable
      public HashCode get(Path path) {
         return this.data.get(path);
      }

      public int count() {
         return this.data.size();
      }

      public static HashCache.ProviderCache load(Path path, Path path1) throws IOException {
         BufferedReader bufferedreader = Files.newBufferedReader(path1, StandardCharsets.UTF_8);

         HashCache.ProviderCache var7;
         try {
            String s = bufferedreader.readLine();
            if (!s.startsWith("// ")) {
               throw new IllegalStateException("Missing cache file header");
            }

            String[] astring = s.substring("// ".length()).split("\t", 2);
            String s1 = astring[0];
            ImmutableMap.Builder<Path, HashCode> immutablemap_builder = ImmutableMap.builder();
            bufferedreader.lines().forEach((s2) -> {
               int i = s2.indexOf(32);
               immutablemap_builder.put(path.resolve(s2.substring(i + 1)), HashCode.fromString(s2.substring(0, i)));
            });
            var7 = new HashCache.ProviderCache(s1, immutablemap_builder.build());
         } catch (Throwable var9) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

         return var7;
      }

      public void save(Path path, Path path1, String s) {
         try {
            BufferedWriter bufferedwriter = Files.newBufferedWriter(path1, StandardCharsets.UTF_8);

            try {
               bufferedwriter.write("// ");
               bufferedwriter.write(this.version);
               bufferedwriter.write(9);
               bufferedwriter.write(s);
               bufferedwriter.newLine();

               for(Map.Entry<Path, HashCode> map_entry : this.data.entrySet()) {
                  bufferedwriter.write(map_entry.getValue().toString());
                  bufferedwriter.write(32);
                  bufferedwriter.write(path.relativize(map_entry.getKey()).toString());
                  bufferedwriter.newLine();
               }
            } catch (Throwable var8) {
               if (bufferedwriter != null) {
                  try {
                     bufferedwriter.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (bufferedwriter != null) {
               bufferedwriter.close();
            }
         } catch (IOException var9) {
            HashCache.LOGGER.warn("Unable write cachefile {}: {}", path1, var9);
         }

      }
   }

   static record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
      ProviderCacheBuilder(String s) {
         this(s, new ConcurrentHashMap<>());
      }

      public void put(Path path, HashCode hashcode) {
         this.data.put(path, hashcode);
      }

      public HashCache.ProviderCache build() {
         return new HashCache.ProviderCache(this.version, ImmutableMap.copyOf(this.data));
      }
   }

   @FunctionalInterface
   public interface UpdateFunction {
      CompletableFuture<?> update(CachedOutput cachedoutput);
   }

   public static record UpdateResult(String providerId, HashCache.ProviderCache cache, int writes) {
   }
}
