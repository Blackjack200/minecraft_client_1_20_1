package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;

public class ServerActivity extends ValueObject {
   public String profileUuid;
   public long joinTime;
   public long leaveTime;

   public static ServerActivity parse(JsonObject jsonobject) {
      ServerActivity serveractivity = new ServerActivity();

      try {
         serveractivity.profileUuid = JsonUtils.getStringOr("profileUuid", jsonobject, (String)null);
         serveractivity.joinTime = JsonUtils.getLongOr("joinTime", jsonobject, Long.MIN_VALUE);
         serveractivity.leaveTime = JsonUtils.getLongOr("leaveTime", jsonobject, Long.MIN_VALUE);
      } catch (Exception var3) {
      }

      return serveractivity;
   }
}
