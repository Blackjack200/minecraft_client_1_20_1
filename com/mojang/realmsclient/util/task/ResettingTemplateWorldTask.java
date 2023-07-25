package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.network.chat.Component;

public class ResettingTemplateWorldTask extends ResettingWorldTask {
   private final WorldTemplate template;

   public ResettingTemplateWorldTask(WorldTemplate worldtemplate, long i, Component component, Runnable runnable) {
      super(i, component, runnable);
      this.template = worldtemplate;
   }

   protected void sendResetRequest(RealmsClient realmsclient, long i) throws RealmsServiceException {
      realmsclient.resetWorldWithTemplate(i, this.template.id);
   }
}
