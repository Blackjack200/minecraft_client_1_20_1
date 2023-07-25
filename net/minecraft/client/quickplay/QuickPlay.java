package net.minecraft.client.quickplay;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;

public class QuickPlay {
   public static final Component ERROR_TITLE = Component.translatable("quickplay.error.title");
   private static final Component INVALID_IDENTIFIER = Component.translatable("quickplay.error.invalid_identifier");
   private static final Component REALM_CONNECT = Component.translatable("quickplay.error.realm_connect");
   private static final Component REALM_PERMISSION = Component.translatable("quickplay.error.realm_permission");
   private static final Component TO_TITLE = Component.translatable("gui.toTitle");
   private static final Component TO_WORLD_LIST = Component.translatable("gui.toWorld");
   private static final Component TO_REALMS_LIST = Component.translatable("gui.toRealms");

   public static void connect(Minecraft minecraft, GameConfig.QuickPlayData gameconfig_quickplaydata, ReloadInstance reloadinstance, RealmsClient realmsclient) {
      String s = gameconfig_quickplaydata.singleplayer();
      String s1 = gameconfig_quickplaydata.multiplayer();
      String s2 = gameconfig_quickplaydata.realms();
      reloadinstance.done().thenRunAsync(() -> {
         if (!Util.isBlank(s)) {
            joinSingleplayerWorld(minecraft, s);
         } else if (!Util.isBlank(s1)) {
            joinMultiplayerWorld(minecraft, s1);
         } else if (!Util.isBlank(s2)) {
            joinRealmsWorld(minecraft, realmsclient, s2);
         }

      }, minecraft);
   }

   private static void joinSingleplayerWorld(Minecraft minecraft, String s) {
      if (!minecraft.getLevelSource().levelExists(s)) {
         Screen screen = new SelectWorldScreen(new TitleScreen());
         minecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, INVALID_IDENTIFIER, TO_WORLD_LIST));
      } else {
         minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
         minecraft.createWorldOpenFlows().loadLevel(new TitleScreen(), s);
      }
   }

   private static void joinMultiplayerWorld(Minecraft minecraft, String s) {
      ServerList serverlist = new ServerList(minecraft);
      serverlist.load();
      ServerData serverdata = serverlist.get(s);
      if (serverdata == null) {
         serverdata = new ServerData(I18n.get("selectServer.defaultName"), s, false);
         serverlist.add(serverdata, true);
         serverlist.save();
      }

      ServerAddress serveraddress = ServerAddress.parseString(s);
      ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), minecraft, serveraddress, serverdata, true);
   }

   private static void joinRealmsWorld(Minecraft minecraft, RealmsClient realmsclient, String s) {
      long i;
      RealmsServerList realmsserverlist;
      try {
         i = Long.parseLong(s);
         realmsserverlist = realmsclient.listWorlds();
      } catch (NumberFormatException var9) {
         Screen screen = new RealmsMainScreen(new TitleScreen());
         minecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, INVALID_IDENTIFIER, TO_REALMS_LIST));
         return;
      } catch (RealmsServiceException var10) {
         Screen screen1 = new TitleScreen();
         minecraft.setScreen(new DisconnectedScreen(screen1, ERROR_TITLE, REALM_CONNECT, TO_TITLE));
         return;
      }

      RealmsServer realmsserver = realmsserverlist.servers.stream().filter((realmsserver1) -> realmsserver1.id == i).findFirst().orElse((RealmsServer)null);
      if (realmsserver == null) {
         Screen screen2 = new RealmsMainScreen(new TitleScreen());
         minecraft.setScreen(new DisconnectedScreen(screen2, ERROR_TITLE, REALM_PERMISSION, TO_REALMS_LIST));
      } else {
         TitleScreen titlescreen = new TitleScreen();
         GetServerDetailsTask getserverdetailstask = new GetServerDetailsTask(new RealmsMainScreen(titlescreen), titlescreen, realmsserver, new ReentrantLock());
         minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(titlescreen, getserverdetailstask));
      }
   }
}
