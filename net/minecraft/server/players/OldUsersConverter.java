package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class OldUsersConverter {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final File OLD_IPBANLIST = new File("banned-ips.txt");
   public static final File OLD_USERBANLIST = new File("banned-players.txt");
   public static final File OLD_OPLIST = new File("ops.txt");
   public static final File OLD_WHITELIST = new File("white-list.txt");

   static List<String> readOldListFormat(File file, Map<String, String[]> map) throws IOException {
      List<String> list = Files.readLines(file, StandardCharsets.UTF_8);

      for(String s : list) {
         s = s.trim();
         if (!s.startsWith("#") && s.length() >= 1) {
            String[] astring = s.split("\\|");
            map.put(astring[0].toLowerCase(Locale.ROOT), astring);
         }
      }

      return list;
   }

   private static void lookupPlayers(MinecraftServer minecraftserver, Collection<String> collection, ProfileLookupCallback profilelookupcallback) {
      String[] astring = collection.stream().filter((s1) -> !StringUtil.isNullOrEmpty(s1)).toArray((i) -> new String[i]);
      if (minecraftserver.usesAuthentication()) {
         minecraftserver.getProfileRepository().findProfilesByNames(astring, Agent.MINECRAFT, profilelookupcallback);
      } else {
         for(String s : astring) {
            UUID uuid = UUIDUtil.getOrCreatePlayerUUID(new GameProfile((UUID)null, s));
            GameProfile gameprofile = new GameProfile(uuid, s);
            profilelookupcallback.onProfileLookupSucceeded(gameprofile);
         }
      }

   }

   public static boolean convertUserBanlist(final MinecraftServer minecraftserver) {
      final UserBanList userbanlist = new UserBanList(PlayerList.USERBANLIST_FILE);
      if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
         if (userbanlist.getFile().exists()) {
            try {
               userbanlist.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", userbanlist.getFile().getName(), var6);
            }
         }

         try {
            final Map<String, String[]> map = Maps.newHashMap();
            readOldListFormat(OLD_USERBANLIST, map);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getProfileCache().add(gameprofile);
                  String[] astring = map.get(gameprofile.getName().toLowerCase(Locale.ROOT));
                  if (astring == null) {
                     OldUsersConverter.LOGGER.warn("Could not convert user banlist entry for {}", (Object)gameprofile.getName());
                     throw new OldUsersConverter.ConversionError("Profile not in the conversionlist");
                  } else {
                     Date date = astring.length > 1 ? OldUsersConverter.parseDate(astring[1], (Date)null) : null;
                     String s = astring.length > 2 ? astring[2] : null;
                     Date date1 = astring.length > 3 ? OldUsersConverter.parseDate(astring[3], (Date)null) : null;
                     String s1 = astring.length > 4 ? astring[4] : null;
                     userbanlist.add(new UserBanListEntry(gameprofile, date, s, date1, s1));
                  }
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user banlist entry for {}", gameprofile.getName(), exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupPlayers(minecraftserver, map.keySet(), profilelookupcallback);
            userbanlist.save();
            renameOldFile(OLD_USERBANLIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old user banlist to convert it!", (Throwable)var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertIpBanlist(MinecraftServer minecraftserver) {
      IpBanList ipbanlist = new IpBanList(PlayerList.IPBANLIST_FILE);
      if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
         if (ipbanlist.getFile().exists()) {
            try {
               ipbanlist.load();
            } catch (IOException var11) {
               LOGGER.warn("Could not load existing file {}", ipbanlist.getFile().getName(), var11);
            }
         }

         try {
            Map<String, String[]> map = Maps.newHashMap();
            readOldListFormat(OLD_IPBANLIST, map);

            for(String s : map.keySet()) {
               String[] astring = map.get(s);
               Date date = astring.length > 1 ? parseDate(astring[1], (Date)null) : null;
               String s1 = astring.length > 2 ? astring[2] : null;
               Date date1 = astring.length > 3 ? parseDate(astring[3], (Date)null) : null;
               String s2 = astring.length > 4 ? astring[4] : null;
               ipbanlist.add(new IpBanListEntry(s, date, s1, date1, s2));
            }

            ipbanlist.save();
            renameOldFile(OLD_IPBANLIST);
            return true;
         } catch (IOException var10) {
            LOGGER.warn("Could not parse old ip banlist to convert it!", (Throwable)var10);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertOpsList(final MinecraftServer minecraftserver) {
      final ServerOpList serveroplist = new ServerOpList(PlayerList.OPLIST_FILE);
      if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
         if (serveroplist.getFile().exists()) {
            try {
               serveroplist.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", serveroplist.getFile().getName(), var6);
            }
         }

         try {
            List<String> list = Files.readLines(OLD_OPLIST, StandardCharsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getProfileCache().add(gameprofile);
                  serveroplist.add(new ServerOpListEntry(gameprofile, minecraftserver.getOperatorUserPermissionLevel(), false));
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  OldUsersConverter.LOGGER.warn("Could not lookup oplist entry for {}", gameprofile.getName(), exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupPlayers(minecraftserver, list, profilelookupcallback);
            serveroplist.save();
            renameOldFile(OLD_OPLIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old oplist to convert it!", (Throwable)var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertWhiteList(final MinecraftServer minecraftserver) {
      final UserWhiteList userwhitelist = new UserWhiteList(PlayerList.WHITELIST_FILE);
      if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
         if (userwhitelist.getFile().exists()) {
            try {
               userwhitelist.load();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", userwhitelist.getFile().getName(), var6);
            }
         }

         try {
            List<String> list = Files.readLines(OLD_WHITELIST, StandardCharsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getProfileCache().add(gameprofile);
                  userwhitelist.add(new UserWhiteListEntry(gameprofile));
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", gameprofile.getName(), exception);
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception);
                  }
               }
            };
            lookupPlayers(minecraftserver, list, profilelookupcallback);
            userwhitelist.save();
            renameOldFile(OLD_WHITELIST);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old whitelist to convert it!", (Throwable)var4);
            return false;
         } catch (OldUsersConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)var5);
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   public static UUID convertMobOwnerIfNecessary(final MinecraftServer minecraftserver, String s) {
      if (!StringUtil.isNullOrEmpty(s) && s.length() <= 16) {
         Optional<UUID> optional = minecraftserver.getProfileCache().get(s).map(GameProfile::getId);
         if (optional.isPresent()) {
            return optional.get();
         } else if (!minecraftserver.isSingleplayer() && minecraftserver.usesAuthentication()) {
            final List<GameProfile> list = Lists.newArrayList();
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getProfileCache().add(gameprofile);
                  list.add(gameprofile);
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", gameprofile.getName(), exception);
               }
            };
            lookupPlayers(minecraftserver, Lists.newArrayList(s), profilelookupcallback);
            return !list.isEmpty() && list.get(0).getId() != null ? list.get(0).getId() : null;
         } else {
            return UUIDUtil.getOrCreatePlayerUUID(new GameProfile((UUID)null, s));
         }
      } else {
         try {
            return UUID.fromString(s);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }

   public static boolean convertPlayers(final DedicatedServer dedicatedserver) {
      final File file = getWorldPlayersDirectory(dedicatedserver);
      final File file1 = new File(file.getParentFile(), "playerdata");
      final File file2 = new File(file.getParentFile(), "unknownplayers");
      if (file.exists() && file.isDirectory()) {
         File[] afile = file.listFiles();
         List<String> list = Lists.newArrayList();

         for(File file3 : afile) {
            String s = file3.getName();
            if (s.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String s1 = s.substring(0, s.length() - ".dat".length());
               if (!s1.isEmpty()) {
                  list.add(s1);
               }
            }
         }

         try {
            final String[] astring = list.toArray(new String[list.size()]);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  dedicatedserver.getProfileCache().add(gameprofile);
                  UUID uuid = gameprofile.getId();
                  if (uuid == null) {
                     throw new OldUsersConverter.ConversionError("Missing UUID for user profile " + gameprofile.getName());
                  } else {
                     this.movePlayerFile(file1, this.getFileNameForProfile(gameprofile), uuid.toString());
                  }
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user uuid for {}", gameprofile.getName(), exception);
                  if (exception instanceof ProfileNotFoundException) {
                     String s = this.getFileNameForProfile(gameprofile);
                     this.movePlayerFile(file2, s, s);
                  } else {
                     throw new OldUsersConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception);
                  }
               }

               private void movePlayerFile(File filex, String s, String s1) {
                  File file1 = new File(file, s + ".dat");
                  File file2 = new File(file, s1 + ".dat");
                  OldUsersConverter.ensureDirectoryExists(file);
                  if (!file1.renameTo(file2)) {
                     throw new OldUsersConverter.ConversionError("Could not convert file for " + s);
                  }
               }

               private String getFileNameForProfile(GameProfile gameprofile) {
                  String s = null;

                  for(String s1 : astring) {
                     if (s1 != null && s1.equalsIgnoreCase(gameprofile.getName())) {
                        s = s1;
                        break;
                     }
                  }

                  if (s == null) {
                     throw new OldUsersConverter.ConversionError("Could not find the filename for " + gameprofile.getName() + " anymore");
                  } else {
                     return s;
                  }
               }
            };
            lookupPlayers(dedicatedserver, Lists.newArrayList(astring), profilelookupcallback);
            return true;
         } catch (OldUsersConverter.ConversionError var12) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)var12);
            return false;
         }
      } else {
         return true;
      }
   }

   static void ensureDirectoryExists(File file) {
      if (file.exists()) {
         if (!file.isDirectory()) {
            throw new OldUsersConverter.ConversionError("Can't create directory " + file.getName() + " in world save directory.");
         }
      } else if (!file.mkdirs()) {
         throw new OldUsersConverter.ConversionError("Can't create directory " + file.getName() + " in world save directory.");
      }
   }

   public static boolean serverReadyAfterUserconversion(MinecraftServer minecraftserver) {
      boolean flag = areOldUserlistsRemoved();
      return flag && areOldPlayersConverted(minecraftserver);
   }

   private static boolean areOldUserlistsRemoved() {
      boolean flag = false;
      if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
         flag = true;
      }

      boolean flag1 = false;
      if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
         flag1 = true;
      }

      boolean flag2 = false;
      if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
         flag2 = true;
      }

      boolean flag3 = false;
      if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
         flag3 = true;
      }

      if (!flag && !flag1 && !flag2 && !flag3) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if (flag) {
            LOGGER.warn("* {}", (Object)OLD_USERBANLIST.getName());
         }

         if (flag1) {
            LOGGER.warn("* {}", (Object)OLD_IPBANLIST.getName());
         }

         if (flag2) {
            LOGGER.warn("* {}", (Object)OLD_OPLIST.getName());
         }

         if (flag3) {
            LOGGER.warn("* {}", (Object)OLD_WHITELIST.getName());
         }

         return false;
      }
   }

   private static boolean areOldPlayersConverted(MinecraftServer minecraftserver) {
      File file = getWorldPlayersDirectory(minecraftserver);
      if (!file.exists() || !file.isDirectory() || file.list().length <= 0 && file.delete()) {
         return true;
      } else {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", (Object)file.getPath());
         return false;
      }
   }

   private static File getWorldPlayersDirectory(MinecraftServer minecraftserver) {
      return minecraftserver.getWorldPath(LevelResource.PLAYER_OLD_DATA_DIR).toFile();
   }

   private static void renameOldFile(File file) {
      File file1 = new File(file.getName() + ".converted");
      file.renameTo(file1);
   }

   static Date parseDate(String s, Date date) {
      Date date1;
      try {
         date1 = BanListEntry.DATE_FORMAT.parse(s);
      } catch (ParseException var4) {
         date1 = date;
      }

      return date1;
   }

   static class ConversionError extends RuntimeException {
      ConversionError(String s, Throwable throwable) {
         super(s, throwable);
      }

      ConversionError(String s) {
         super(s);
      }
   }
}
