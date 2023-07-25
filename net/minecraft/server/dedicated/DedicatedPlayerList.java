package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

public class DedicatedPlayerList extends PlayerList {
   private static final Logger LOGGER = LogUtils.getLogger();

   public DedicatedPlayerList(DedicatedServer dedicatedserver, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, PlayerDataStorage playerdatastorage) {
      super(dedicatedserver, layeredregistryaccess, playerdatastorage, dedicatedserver.getProperties().maxPlayers);
      DedicatedServerProperties dedicatedserverproperties = dedicatedserver.getProperties();
      this.setViewDistance(dedicatedserverproperties.viewDistance);
      this.setSimulationDistance(dedicatedserverproperties.simulationDistance);
      super.setUsingWhiteList(dedicatedserverproperties.whiteList.get());
      this.loadUserBanList();
      this.saveUserBanList();
      this.loadIpBanList();
      this.saveIpBanList();
      this.loadOps();
      this.loadWhiteList();
      this.saveOps();
      if (!this.getWhiteList().getFile().exists()) {
         this.saveWhiteList();
      }

   }

   public void setUsingWhiteList(boolean flag) {
      super.setUsingWhiteList(flag);
      this.getServer().storeUsingWhiteList(flag);
   }

   public void op(GameProfile gameprofile) {
      super.op(gameprofile);
      this.saveOps();
   }

   public void deop(GameProfile gameprofile) {
      super.deop(gameprofile);
      this.saveOps();
   }

   public void reloadWhiteList() {
      this.loadWhiteList();
   }

   private void saveIpBanList() {
      try {
         this.getIpBans().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save ip banlist: ", (Throwable)var2);
      }

   }

   private void saveUserBanList() {
      try {
         this.getBans().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save user banlist: ", (Throwable)var2);
      }

   }

   private void loadIpBanList() {
      try {
         this.getIpBans().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load ip banlist: ", (Throwable)var2);
      }

   }

   private void loadUserBanList() {
      try {
         this.getBans().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load user banlist: ", (Throwable)var2);
      }

   }

   private void loadOps() {
      try {
         this.getOps().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load operators list: ", (Throwable)var2);
      }

   }

   private void saveOps() {
      try {
         this.getOps().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save operators list: ", (Throwable)var2);
      }

   }

   private void loadWhiteList() {
      try {
         this.getWhiteList().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load white-list: ", (Throwable)var2);
      }

   }

   private void saveWhiteList() {
      try {
         this.getWhiteList().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save white-list: ", (Throwable)var2);
      }

   }

   public boolean isWhiteListed(GameProfile gameprofile) {
      return !this.isUsingWhitelist() || this.isOp(gameprofile) || this.getWhiteList().isWhiteListed(gameprofile);
   }

   public DedicatedServer getServer() {
      return (DedicatedServer)super.getServer();
   }

   public boolean canBypassPlayerLimit(GameProfile gameprofile) {
      return this.getOps().canBypassPlayerLimit(gameprofile);
   }
}
