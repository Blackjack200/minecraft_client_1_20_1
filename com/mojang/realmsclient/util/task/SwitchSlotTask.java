package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class SwitchSlotTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final long worldId;
   private final int slot;
   private final Runnable callback;

   public SwitchSlotTask(long i, int j, Runnable runnable) {
      this.worldId = i;
      this.slot = j;
      this.callback = runnable;
   }

   public void run() {
      RealmsClient realmsclient = RealmsClient.create();
      this.setTitle(Component.translatable("mco.minigame.world.slot.screen.title"));

      for(int i = 0; i < 25; ++i) {
         try {
            if (this.aborted()) {
               return;
            }

            if (realmsclient.switchSlot(this.worldId, this.slot)) {
               this.callback.run();
               break;
            }
         } catch (RetryCallException var4) {
            if (this.aborted()) {
               return;
            }

            pause((long)var4.delaySeconds);
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't switch world!");
            this.error(var5.toString());
         }
      }

   }
}
