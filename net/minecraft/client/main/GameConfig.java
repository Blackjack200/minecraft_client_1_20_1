package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.User;
import net.minecraft.client.resources.IndexedAssetSource;

public class GameConfig {
   public final GameConfig.UserData user;
   public final DisplayData display;
   public final GameConfig.FolderData location;
   public final GameConfig.GameData game;
   public final GameConfig.QuickPlayData quickPlay;

   public GameConfig(GameConfig.UserData gameconfig_userdata, DisplayData displaydata, GameConfig.FolderData gameconfig_folderdata, GameConfig.GameData gameconfig_gamedata, GameConfig.QuickPlayData gameconfig_quickplaydata) {
      this.user = gameconfig_userdata;
      this.display = displaydata;
      this.location = gameconfig_folderdata;
      this.game = gameconfig_gamedata;
      this.quickPlay = gameconfig_quickplaydata;
   }

   public static class FolderData {
      public final File gameDirectory;
      public final File resourcePackDirectory;
      public final File assetDirectory;
      @Nullable
      public final String assetIndex;

      public FolderData(File file, File file1, File file2, @Nullable String s) {
         this.gameDirectory = file;
         this.resourcePackDirectory = file1;
         this.assetDirectory = file2;
         this.assetIndex = s;
      }

      public Path getExternalAssetSource() {
         return this.assetIndex == null ? this.assetDirectory.toPath() : IndexedAssetSource.createIndexFs(this.assetDirectory.toPath(), this.assetIndex);
      }
   }

   public static class GameData {
      public final boolean demo;
      public final String launchVersion;
      public final String versionType;
      public final boolean disableMultiplayer;
      public final boolean disableChat;

      public GameData(boolean flag, String s, String s1, boolean flag1, boolean flag2) {
         this.demo = flag;
         this.launchVersion = s;
         this.versionType = s1;
         this.disableMultiplayer = flag1;
         this.disableChat = flag2;
      }
   }

   public static record QuickPlayData(@Nullable String path, @Nullable String singleplayer, @Nullable String multiplayer, @Nullable String realms) {
      public boolean isEnabled() {
         return !Util.isBlank(this.singleplayer) || !Util.isBlank(this.multiplayer) || !Util.isBlank(this.realms);
      }
   }

   public static class UserData {
      public final User user;
      public final PropertyMap userProperties;
      public final PropertyMap profileProperties;
      public final Proxy proxy;

      public UserData(User user, PropertyMap propertymap, PropertyMap propertymap1, Proxy proxy) {
         this.user = user;
         this.userProperties = propertymap;
         this.profileProperties = propertymap1;
         this.proxy = proxy;
      }
   }
}
