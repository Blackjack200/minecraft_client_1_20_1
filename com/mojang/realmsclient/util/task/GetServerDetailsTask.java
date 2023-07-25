package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class GetServerDetailsTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RealmsServer server;
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   private final ReentrantLock connectLock;

   public GetServerDetailsTask(RealmsMainScreen realmsmainscreen, Screen screen, RealmsServer realmsserver, ReentrantLock reentrantlock) {
      this.lastScreen = screen;
      this.mainScreen = realmsmainscreen;
      this.server = realmsserver;
      this.connectLock = reentrantlock;
   }

   public void run() {
      this.setTitle(Component.translatable("mco.connect.connecting"));

      RealmsServerAddress realmsserveraddress;
      try {
         realmsserveraddress = this.fetchServerAddress();
      } catch (CancellationException var4) {
         LOGGER.info("User aborted connecting to realms");
         return;
      } catch (RealmsServiceException var5) {
         switch (var5.realmsErrorCodeOrDefault(-1)) {
            case 6002:
               setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
               return;
            case 6006:
               boolean flag = this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid());
               setScreen((Screen)(flag ? new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME) : new RealmsGenericErrorScreen(Component.translatable("mco.brokenworld.nonowner.title"), Component.translatable("mco.brokenworld.nonowner.error"), this.lastScreen)));
               return;
            default:
               this.error(var5.toString());
               LOGGER.error("Couldn't connect to world", (Throwable)var5);
               return;
         }
      } catch (TimeoutException var6) {
         this.error(Component.translatable("mco.errorMessage.connectionFailure"));
         return;
      } catch (Exception var7) {
         LOGGER.error("Couldn't connect to world", (Throwable)var7);
         this.error(var7.getLocalizedMessage());
         return;
      }

      boolean flag1 = realmsserveraddress.resourcePackUrl != null && realmsserveraddress.resourcePackHash != null;
      Screen screen = (Screen)(flag1 ? this.resourcePackDownloadConfirmationScreen(realmsserveraddress, this::connectScreen) : this.connectScreen(realmsserveraddress));
      setScreen(screen);
   }

   private RealmsServerAddress fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
      RealmsClient realmsclient = RealmsClient.create();

      for(int i = 0; i < 40; ++i) {
         if (this.aborted()) {
            throw new CancellationException();
         }

         try {
            return realmsclient.join(this.server.id);
         } catch (RetryCallException var4) {
            pause((long)var4.delaySeconds);
         }
      }

      throw new TimeoutException();
   }

   public RealmsLongRunningMcoTaskScreen connectScreen(RealmsServerAddress realmsserveraddress) {
      return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, realmsserveraddress));
   }

   private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(RealmsServerAddress realmsserveraddress, Function<RealmsServerAddress, Screen> function) {
      BooleanConsumer booleanconsumer = (flag) -> {
         try {
            if (flag) {
               this.scheduleResourcePackDownload(realmsserveraddress).thenRun(() -> setScreen(function.apply(realmsserveraddress))).exceptionally((throwable) -> {
                  Minecraft.getInstance().getDownloadedPackSource().clearServerPack();
                  LOGGER.error("Failed to download resource pack from {}", realmsserveraddress, throwable);
                  setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.download.resourcePack.fail"), this.lastScreen));
                  return null;
               });
               return;
            }

            setScreen(this.lastScreen);
         } finally {
            if (this.connectLock.isHeldByCurrentThread()) {
               this.connectLock.unlock();
            }

         }

      };
      return new RealmsLongConfirmationScreen(booleanconsumer, RealmsLongConfirmationScreen.Type.INFO, Component.translatable("mco.configure.world.resourcepack.question.line1"), Component.translatable("mco.configure.world.resourcepack.question.line2"), true);
   }

   private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress realmsserveraddress) {
      try {
         return Minecraft.getInstance().getDownloadedPackSource().downloadAndSelectResourcePack(new URL(realmsserveraddress.resourcePackUrl), realmsserveraddress.resourcePackHash, false);
      } catch (Exception var4) {
         CompletableFuture<Void> completablefuture = new CompletableFuture<>();
         completablefuture.completeExceptionally(var4);
         return completablefuture;
      }
   }
}
