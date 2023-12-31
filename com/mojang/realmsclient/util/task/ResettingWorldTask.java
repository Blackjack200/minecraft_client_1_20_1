package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public abstract class ResettingWorldTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final long serverId;
   private final Component title;
   private final Runnable callback;

   public ResettingWorldTask(long i, Component component, Runnable runnable) {
      this.serverId = i;
      this.title = component;
      this.callback = runnable;
   }

   protected abstract void sendResetRequest(RealmsClient realmsclient, long i) throws RealmsServiceException;

   public void run() {
      RealmsClient realmsclient = RealmsClient.create();
      this.setTitle(this.title);
      int i = 0;

      while(i < 25) {
         try {
            if (this.aborted()) {
               return;
            }

            this.sendResetRequest(realmsclient, this.serverId);
            if (this.aborted()) {
               return;
            }

            this.callback.run();
            return;
         } catch (RetryCallException var4) {
            if (this.aborted()) {
               return;
            }

            pause((long)var4.delaySeconds);
            ++i;
         } catch (Exception var5) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't reset world");
            this.error(var5.toString());
            return;
         }
      }

   }
}
