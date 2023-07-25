package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class WorldCreationTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final String motd;
   private final long worldId;
   private final Screen lastScreen;

   public WorldCreationTask(long i, String s, String s1, Screen screen) {
      this.worldId = i;
      this.name = s;
      this.motd = s1;
      this.lastScreen = screen;
   }

   public void run() {
      this.setTitle(Component.translatable("mco.create.world.wait"));
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.initializeWorld(this.worldId, this.name, this.motd);
         setScreen(this.lastScreen);
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't create world");
         this.error(var3.toString());
      } catch (Exception var4) {
         LOGGER.error("Could not create world");
         this.error(var4.getLocalizedMessage());
      }

   }
}
