package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import net.minecraft.network.chat.Component;

public class ResettingGeneratedWorldTask extends ResettingWorldTask {
   private final WorldGenerationInfo generationInfo;

   public ResettingGeneratedWorldTask(WorldGenerationInfo worldgenerationinfo, long i, Component component, Runnable runnable) {
      super(i, component, runnable);
      this.generationInfo = worldgenerationinfo;
   }

   protected void sendResetRequest(RealmsClient realmsclient, long i) throws RealmsServiceException {
      realmsclient.resetWorldWithSeed(i, this.generationInfo);
   }
}
