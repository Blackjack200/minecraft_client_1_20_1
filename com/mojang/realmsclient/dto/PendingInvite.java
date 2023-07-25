package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import org.slf4j.Logger;

public class PendingInvite extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String invitationId;
   public String worldName;
   public String worldOwnerName;
   public String worldOwnerUuid;
   public Date date;

   public static PendingInvite parse(JsonObject jsonobject) {
      PendingInvite pendinginvite = new PendingInvite();

      try {
         pendinginvite.invitationId = JsonUtils.getStringOr("invitationId", jsonobject, "");
         pendinginvite.worldName = JsonUtils.getStringOr("worldName", jsonobject, "");
         pendinginvite.worldOwnerName = JsonUtils.getStringOr("worldOwnerName", jsonobject, "");
         pendinginvite.worldOwnerUuid = JsonUtils.getStringOr("worldOwnerUuid", jsonobject, "");
         pendinginvite.date = JsonUtils.getDateOr("date", jsonobject);
      } catch (Exception var3) {
         LOGGER.error("Could not parse PendingInvite: {}", (Object)var3.getMessage());
      }

      return pendinginvite;
   }
}
