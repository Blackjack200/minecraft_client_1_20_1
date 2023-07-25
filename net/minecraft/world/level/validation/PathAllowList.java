package net.minecraft.world.level.validation;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList implements PathMatcher {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String COMMENT_PREFIX = "#";
   private final List<PathAllowList.ConfigEntry> entries;
   private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap<>();

   public PathAllowList(List<PathAllowList.ConfigEntry> list) {
      this.entries = list;
   }

   public PathMatcher getForFileSystem(FileSystem filesystem) {
      return this.compiledPaths.computeIfAbsent(filesystem.provider().getScheme(), (s) -> {
         List<PathMatcher> list;
         try {
            list = this.entries.stream().map((pathallowlist_configentry) -> pathallowlist_configentry.compile(filesystem)).toList();
         } catch (Exception var5) {
            LOGGER.error("Failed to compile file pattern list", (Throwable)var5);
            return (path2) -> false;
         }

         PathMatcher var10000;
         switch (list.size()) {
            case 0:
               var10000 = (path1) -> false;
               break;
            case 1:
               var10000 = list.get(0);
               break;
            default:
               var10000 = (path) -> {
                  for(PathMatcher pathmatcher : list) {
                     if (pathmatcher.matches(path)) {
                        return true;
                     }
                  }

                  return false;
               };
         }

         return var10000;
      });
   }

   public boolean matches(Path path) {
      return this.getForFileSystem(path.getFileSystem()).matches(path);
   }

   public static PathAllowList readPlain(BufferedReader bufferedreader) {
      return new PathAllowList(bufferedreader.lines().flatMap((s) -> PathAllowList.ConfigEntry.parse(s).stream()).toList());
   }

   public static record ConfigEntry(PathAllowList.EntryType type, String pattern) {
      public PathMatcher compile(FileSystem filesystem) {
         return this.type().compile(filesystem, this.pattern);
      }

      static Optional<PathAllowList.ConfigEntry> parse(String s) {
         if (!s.isBlank() && !s.startsWith("#")) {
            if (!s.startsWith("[")) {
               return Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, s));
            } else {
               int i = s.indexOf(93, 1);
               if (i == -1) {
                  throw new IllegalArgumentException("Unterminated type in line '" + s + "'");
               } else {
                  String s1 = s.substring(1, i);
                  String s2 = s.substring(i + 1);
                  Optional var10000;
                  switch (s1) {
                     case "glob":
                     case "regex":
                        var10000 = Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, s1 + ":" + s2));
                        break;
                     case "prefix":
                        var10000 = Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, s2));
                        break;
                     default:
                        throw new IllegalArgumentException("Unsupported definition type in line '" + s + "'");
                  }

                  return var10000;
               }
            }
         } else {
            return Optional.empty();
         }
      }

      static PathAllowList.ConfigEntry glob(String s) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "glob:" + s);
      }

      static PathAllowList.ConfigEntry regex(String s) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "regex:" + s);
      }

      static PathAllowList.ConfigEntry prefix(String s) {
         return new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, s);
      }
   }

   @FunctionalInterface
   public interface EntryType {
      PathAllowList.EntryType FILESYSTEM = FileSystem::getPathMatcher;
      PathAllowList.EntryType PREFIX = (filesystem, s) -> (path) -> path.toString().startsWith(s);

      PathMatcher compile(FileSystem filesystem, String s);
   }
}
