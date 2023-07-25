package net.minecraft.server.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import org.slf4j.Logger;

public class GameProfileCache {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int GAMEPROFILES_MRU_LIMIT = 1000;
   private static final int GAMEPROFILES_EXPIRATION_MONTHS = 1;
   private static boolean usesAuthentication;
   private final Map<String, GameProfileCache.GameProfileInfo> profilesByName = Maps.newConcurrentMap();
   private final Map<UUID, GameProfileCache.GameProfileInfo> profilesByUUID = Maps.newConcurrentMap();
   private final Map<String, CompletableFuture<Optional<GameProfile>>> requests = Maps.newConcurrentMap();
   private final GameProfileRepository profileRepository;
   private final Gson gson = (new GsonBuilder()).create();
   private final File file;
   private final AtomicLong operationCount = new AtomicLong();
   @Nullable
   private Executor executor;

   public GameProfileCache(GameProfileRepository gameprofilerepository, File file) {
      this.profileRepository = gameprofilerepository;
      this.file = file;
      Lists.reverse(this.load()).forEach(this::safeAdd);
   }

   private void safeAdd(GameProfileCache.GameProfileInfo gameprofilecache_gameprofileinfo) {
      GameProfile gameprofile = gameprofilecache_gameprofileinfo.getProfile();
      gameprofilecache_gameprofileinfo.setLastAccess(this.getNextOperation());
      String s = gameprofile.getName();
      if (s != null) {
         this.profilesByName.put(s.toLowerCase(Locale.ROOT), gameprofilecache_gameprofileinfo);
      }

      UUID uuid = gameprofile.getId();
      if (uuid != null) {
         this.profilesByUUID.put(uuid, gameprofilecache_gameprofileinfo);
      }

   }

   private static Optional<GameProfile> lookupGameProfile(GameProfileRepository gameprofilerepository, String s) {
      final AtomicReference<GameProfile> atomicreference = new AtomicReference<>();
      ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
         public void onProfileLookupSucceeded(GameProfile gameprofile) {
            atomicreference.set(gameprofile);
         }

         public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
            atomicreference.set((GameProfile)null);
         }
      };
      gameprofilerepository.findProfilesByNames(new String[]{s}, Agent.MINECRAFT, profilelookupcallback);
      GameProfile gameprofile = atomicreference.get();
      if (!usesAuthentication() && gameprofile == null) {
         UUID uuid = UUIDUtil.getOrCreatePlayerUUID(new GameProfile((UUID)null, s));
         return Optional.of(new GameProfile(uuid, s));
      } else {
         return Optional.ofNullable(gameprofile);
      }
   }

   public static void setUsesAuthentication(boolean flag) {
      usesAuthentication = flag;
   }

   private static boolean usesAuthentication() {
      return usesAuthentication;
   }

   public void add(GameProfile gameprofile) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(2, 1);
      Date date = calendar.getTime();
      GameProfileCache.GameProfileInfo gameprofilecache_gameprofileinfo = new GameProfileCache.GameProfileInfo(gameprofile, date);
      this.safeAdd(gameprofilecache_gameprofileinfo);
      this.save();
   }

   private long getNextOperation() {
      return this.operationCount.incrementAndGet();
   }

   public Optional<GameProfile> get(String s) {
      String s1 = s.toLowerCase(Locale.ROOT);
      GameProfileCache.GameProfileInfo gameprofilecache_gameprofileinfo = this.profilesByName.get(s1);
      boolean flag = false;
      if (gameprofilecache_gameprofileinfo != null && (new Date()).getTime() >= gameprofilecache_gameprofileinfo.expirationDate.getTime()) {
         this.profilesByUUID.remove(gameprofilecache_gameprofileinfo.getProfile().getId());
         this.profilesByName.remove(gameprofilecache_gameprofileinfo.getProfile().getName().toLowerCase(Locale.ROOT));
         flag = true;
         gameprofilecache_gameprofileinfo = null;
      }

      Optional<GameProfile> optional;
      if (gameprofilecache_gameprofileinfo != null) {
         gameprofilecache_gameprofileinfo.setLastAccess(this.getNextOperation());
         optional = Optional.of(gameprofilecache_gameprofileinfo.getProfile());
      } else {
         optional = lookupGameProfile(this.profileRepository, s1);
         if (optional.isPresent()) {
            this.add(optional.get());
            flag = false;
         }
      }

      if (flag) {
         this.save();
      }

      return optional;
   }

   public void getAsync(String s, Consumer<Optional<GameProfile>> consumer) {
      if (this.executor == null) {
         throw new IllegalStateException("No executor");
      } else {
         CompletableFuture<Optional<GameProfile>> completablefuture = this.requests.get(s);
         if (completablefuture != null) {
            this.requests.put(s, completablefuture.whenCompleteAsync((optional2, throwable2) -> consumer.accept(optional2), this.executor));
         } else {
            this.requests.put(s, CompletableFuture.supplyAsync(() -> this.get(s), Util.backgroundExecutor()).whenCompleteAsync((optional1, throwable1) -> this.requests.remove(s), this.executor).whenCompleteAsync((optional, throwable) -> consumer.accept(optional), this.executor));
         }

      }
   }

   public Optional<GameProfile> get(UUID uuid) {
      GameProfileCache.GameProfileInfo gameprofilecache_gameprofileinfo = this.profilesByUUID.get(uuid);
      if (gameprofilecache_gameprofileinfo == null) {
         return Optional.empty();
      } else {
         gameprofilecache_gameprofileinfo.setLastAccess(this.getNextOperation());
         return Optional.of(gameprofilecache_gameprofileinfo.getProfile());
      }
   }

   public void setExecutor(Executor executor) {
      this.executor = executor;
   }

   public void clearExecutor() {
      this.executor = null;
   }

   private static DateFormat createDateFormat() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   }

   public List<GameProfileCache.GameProfileInfo> load() {
      List<GameProfileCache.GameProfileInfo> list = Lists.newArrayList();

      try {
         Reader reader = Files.newReader(this.file, StandardCharsets.UTF_8);

         Object var9;
         label61: {
            try {
               JsonArray jsonarray = this.gson.fromJson(reader, JsonArray.class);
               if (jsonarray == null) {
                  var9 = list;
                  break label61;
               }

               DateFormat dateformat = createDateFormat();
               jsonarray.forEach((jsonelement) -> readGameProfile(jsonelement, dateformat).ifPresent(list::add));
            } catch (Throwable var6) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (reader != null) {
               reader.close();
            }

            return list;
         }

         if (reader != null) {
            reader.close();
         }

         return (List<GameProfileCache.GameProfileInfo>)var9;
      } catch (FileNotFoundException var7) {
      } catch (JsonParseException | IOException var8) {
         LOGGER.warn("Failed to load profile cache {}", this.file, var8);
      }

      return list;
   }

   public void save() {
      JsonArray jsonarray = new JsonArray();
      DateFormat dateformat = createDateFormat();
      this.getTopMRUProfiles(1000).forEach((gameprofilecache_gameprofileinfo) -> jsonarray.add(writeGameProfile(gameprofilecache_gameprofileinfo, dateformat)));
      String s = this.gson.toJson((JsonElement)jsonarray);

      try {
         Writer writer = Files.newWriter(this.file, StandardCharsets.UTF_8);

         try {
            writer.write(s);
         } catch (Throwable var8) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var9) {
      }

   }

   private Stream<GameProfileCache.GameProfileInfo> getTopMRUProfiles(int i) {
      return ImmutableList.copyOf(this.profilesByUUID.values()).stream().sorted(Comparator.comparing(GameProfileCache.GameProfileInfo::getLastAccess).reversed()).limit((long)i);
   }

   private static JsonElement writeGameProfile(GameProfileCache.GameProfileInfo gameprofilecache_gameprofileinfo, DateFormat dateformat) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("name", gameprofilecache_gameprofileinfo.getProfile().getName());
      UUID uuid = gameprofilecache_gameprofileinfo.getProfile().getId();
      jsonobject.addProperty("uuid", uuid == null ? "" : uuid.toString());
      jsonobject.addProperty("expiresOn", dateformat.format(gameprofilecache_gameprofileinfo.getExpirationDate()));
      return jsonobject;
   }

   private static Optional<GameProfileCache.GameProfileInfo> readGameProfile(JsonElement jsonelement, DateFormat dateformat) {
      if (jsonelement.isJsonObject()) {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         JsonElement jsonelement1 = jsonobject.get("name");
         JsonElement jsonelement2 = jsonobject.get("uuid");
         JsonElement jsonelement3 = jsonobject.get("expiresOn");
         if (jsonelement1 != null && jsonelement2 != null) {
            String s = jsonelement2.getAsString();
            String s1 = jsonelement1.getAsString();
            Date date = null;
            if (jsonelement3 != null) {
               try {
                  date = dateformat.parse(jsonelement3.getAsString());
               } catch (ParseException var12) {
               }
            }

            if (s1 != null && s != null && date != null) {
               UUID uuid;
               try {
                  uuid = UUID.fromString(s);
               } catch (Throwable var11) {
                  return Optional.empty();
               }

               return Optional.of(new GameProfileCache.GameProfileInfo(new GameProfile(uuid, s1), date));
            } else {
               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   static class GameProfileInfo {
      private final GameProfile profile;
      final Date expirationDate;
      private volatile long lastAccess;

      GameProfileInfo(GameProfile gameprofile, Date date) {
         this.profile = gameprofile;
         this.expirationDate = date;
      }

      public GameProfile getProfile() {
         return this.profile;
      }

      public Date getExpirationDate() {
         return this.expirationDate;
      }

      public void setLastAccess(long i) {
         this.lastAccess = i;
      }

      public long getLastAccess() {
         return this.lastAccess;
      }
   }
}
