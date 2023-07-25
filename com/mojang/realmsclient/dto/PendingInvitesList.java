package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;

public class PendingInvitesList extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public List<PendingInvite> pendingInvites = Lists.newArrayList();

   public static PendingInvitesList parse(String s) {
      PendingInvitesList pendinginviteslist = new PendingInvitesList();

      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
         if (jsonobject.get("invites").isJsonArray()) {
            Iterator<JsonElement> iterator = jsonobject.get("invites").getAsJsonArray().iterator();

            while(iterator.hasNext()) {
               pendinginviteslist.pendingInvites.add(PendingInvite.parse(iterator.next().getAsJsonObject()));
            }
         }
      } catch (Exception var5) {
         LOGGER.error("Could not parse PendingInvitesList: {}", (Object)var5.getMessage());
      }

      return pendinginviteslist;
   }
}
