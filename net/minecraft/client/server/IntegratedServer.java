package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MIN_SIM_DISTANCE = 2;
   private final Minecraft minecraft;
   private boolean paused = true;
   private int publishedPort = -1;
   @Nullable
   private GameType publishedGameType;
   @Nullable
   private LanServerPinger lanPinger;
   @Nullable
   private UUID uuid;
   private int previousSimulationDistance = 0;

   public IntegratedServer(Thread thread, Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, PackRepository packrepository, WorldStem worldstem, Services services, ChunkProgressListenerFactory chunkprogresslistenerfactory) {
      super(thread, levelstoragesource_levelstorageaccess, packrepository, worldstem, minecraft.getProxy(), minecraft.getFixerUpper(), services, chunkprogresslistenerfactory);
      this.setSingleplayerProfile(minecraft.getUser().getGameProfile());
      this.setDemo(minecraft.isDemo());
      this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
      this.minecraft = minecraft;
   }

   public boolean initServer() {
      LOGGER.info("Starting integrated minecraft server version {}", (Object)SharedConstants.getCurrentVersion().getName());
      this.setUsesAuthentication(true);
      this.setPvpAllowed(true);
      this.setFlightAllowed(true);
      this.initializeKeyPair();
      this.loadLevel();
      GameProfile gameprofile = this.getSingleplayerProfile();
      String s = this.getWorldData().getLevelName();
      this.setMotd(gameprofile != null ? gameprofile.getName() + " - " + s : s);
      return true;
   }

   public void tickServer(BooleanSupplier booleansupplier) {
      boolean flag = this.paused;
      this.paused = Minecraft.getInstance().isPaused();
      ProfilerFiller profilerfiller = this.getProfiler();
      if (!flag && this.paused) {
         profilerfiller.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.saveEverything(false, false, false);
         profilerfiller.pop();
      }

      boolean flag1 = Minecraft.getInstance().getConnection() != null;
      if (flag1 && this.paused) {
         this.tickPaused();
      } else {
         if (flag && !this.paused) {
            this.forceTimeSynchronization();
         }

         super.tickServer(booleansupplier);
         int i = Math.max(2, this.minecraft.options.renderDistance().get());
         if (i != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(i);
         }

         int j = Math.max(2, this.minecraft.options.simulationDistance().get());
         if (j != this.previousSimulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
            this.getPlayerList().setSimulationDistance(j);
            this.previousSimulationDistance = j;
         }

      }
   }

   private void tickPaused() {
      for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
         serverplayer.awardStat(Stats.TOTAL_WORLD_TIME);
      }

   }

   public boolean shouldRconBroadcast() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public File getServerDirectory() {
      return this.minecraft.gameDirectory;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   public boolean isEpollEnabled() {
      return false;
   }

   public void onServerCrash(CrashReport crashreport) {
      this.minecraft.delayCrashRaw(crashreport);
   }

   public SystemReport fillServerSystemReport(SystemReport systemreport) {
      systemreport.setDetail("Type", "Integrated Server (map_client.txt)");
      systemreport.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
      systemreport.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
      return systemreport;
   }

   public ModCheck getModdedStatus() {
      return Minecraft.checkModStatus().merge(super.getModdedStatus());
   }

   public boolean publishServer(@Nullable GameType gametype, boolean flag, int i) {
      try {
         this.minecraft.prepareForMultiplayer();
         this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync((optional) -> optional.ifPresent((profilekeypair) -> {
               ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
               if (clientpacketlistener != null) {
                  clientpacketlistener.setKeyPair(profilekeypair);
               }

            }), this.minecraft);
         this.getConnection().startTcpServerListener((InetAddress)null, i);
         LOGGER.info("Started serving on {}", (int)i);
         this.publishedPort = i;
         this.lanPinger = new LanServerPinger(this.getMotd(), "" + i);
         this.lanPinger.start();
         this.publishedGameType = gametype;
         this.getPlayerList().setAllowCheatsForAllPlayers(flag);
         int j = this.getProfilePermissions(this.minecraft.player.getGameProfile());
         this.minecraft.player.setPermissionLevel(j);

         for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
            this.getCommands().sendCommands(serverplayer);
         }

         return true;
      } catch (IOException var7) {
         return false;
      }
   }

   public void stopServer() {
      super.stopServer();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public void halt(boolean flag) {
      this.executeBlocking(() -> {
         for(ServerPlayer serverplayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
            if (!serverplayer.getUUID().equals(this.uuid)) {
               this.getPlayerList().remove(serverplayer);
            }
         }

      });
      super.halt(flag);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public boolean isPublished() {
      return this.publishedPort > -1;
   }

   public int getPort() {
      return this.publishedPort;
   }

   public void setDefaultGameType(GameType gametype) {
      super.setDefaultGameType(gametype);
      this.publishedGameType = null;
   }

   public boolean isCommandBlockEnabled() {
      return true;
   }

   public int getOperatorUserPermissionLevel() {
      return 2;
   }

   public int getFunctionCompilationLevel() {
      return 2;
   }

   public void setUUID(UUID uuid) {
      this.uuid = uuid;
   }

   public boolean isSingleplayerOwner(GameProfile gameprofile) {
      return this.getSingleplayerProfile() != null && gameprofile.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
   }

   public int getScaledTrackingDistance(int i) {
      return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)i);
   }

   public boolean forceSynchronousWrites() {
      return this.minecraft.options.syncWrites;
   }

   @Nullable
   public GameType getForcedGameType() {
      return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
   }
}
