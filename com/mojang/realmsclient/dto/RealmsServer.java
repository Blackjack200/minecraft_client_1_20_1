package com.mojang.realmsclient.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

public class RealmsServer extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String motd;
   public RealmsServer.State state;
   public String owner;
   public String ownerUUID;
   public List<PlayerInfo> players;
   public Map<Integer, RealmsWorldOptions> slots;
   public boolean expired;
   public boolean expiredTrial;
   public int daysLeft;
   public RealmsServer.WorldType worldType;
   public int activeSlot;
   public String minigameName;
   public int minigameId;
   public String minigameImage;
   public RealmsServerPing serverPing = new RealmsServerPing();

   public String getDescription() {
      return this.motd;
   }

   public String getName() {
      return this.name;
   }

   public String getMinigameName() {
      return this.minigameName;
   }

   public void setName(String s) {
      this.name = s;
   }

   public void setDescription(String s) {
      this.motd = s;
   }

   public void updateServerPing(RealmsServerPlayerList realmsserverplayerlist) {
      List<String> list = Lists.newArrayList();
      int i = 0;

      for(String s : realmsserverplayerlist.players) {
         if (!s.equals(Minecraft.getInstance().getUser().getUuid())) {
            String s1 = "";

            try {
               s1 = RealmsUtil.uuidToName(s);
            } catch (Exception var8) {
               LOGGER.error("Could not get name for {}", s, var8);
               continue;
            }

            list.add(s1);
            ++i;
         }
      }

      this.serverPing.nrOfPlayers = String.valueOf(i);
      this.serverPing.playerList = Joiner.on('\n').join(list);
   }

   public static RealmsServer parse(JsonObject jsonobject) {
      RealmsServer realmsserver = new RealmsServer();

      try {
         realmsserver.id = JsonUtils.getLongOr("id", jsonobject, -1L);
         realmsserver.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", jsonobject, (String)null);
         realmsserver.name = JsonUtils.getStringOr("name", jsonobject, (String)null);
         realmsserver.motd = JsonUtils.getStringOr("motd", jsonobject, (String)null);
         realmsserver.state = getState(JsonUtils.getStringOr("state", jsonobject, RealmsServer.State.CLOSED.name()));
         realmsserver.owner = JsonUtils.getStringOr("owner", jsonobject, (String)null);
         if (jsonobject.get("players") != null && jsonobject.get("players").isJsonArray()) {
            realmsserver.players = parseInvited(jsonobject.get("players").getAsJsonArray());
            sortInvited(realmsserver);
         } else {
            realmsserver.players = Lists.newArrayList();
         }

         realmsserver.daysLeft = JsonUtils.getIntOr("daysLeft", jsonobject, 0);
         realmsserver.expired = JsonUtils.getBooleanOr("expired", jsonobject, false);
         realmsserver.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", jsonobject, false);
         realmsserver.worldType = getWorldType(JsonUtils.getStringOr("worldType", jsonobject, RealmsServer.WorldType.NORMAL.name()));
         realmsserver.ownerUUID = JsonUtils.getStringOr("ownerUUID", jsonobject, "");
         if (jsonobject.get("slots") != null && jsonobject.get("slots").isJsonArray()) {
            realmsserver.slots = parseSlots(jsonobject.get("slots").getAsJsonArray());
         } else {
            realmsserver.slots = createEmptySlots();
         }

         realmsserver.minigameName = JsonUtils.getStringOr("minigameName", jsonobject, (String)null);
         realmsserver.activeSlot = JsonUtils.getIntOr("activeSlot", jsonobject, -1);
         realmsserver.minigameId = JsonUtils.getIntOr("minigameId", jsonobject, -1);
         realmsserver.minigameImage = JsonUtils.getStringOr("minigameImage", jsonobject, (String)null);
      } catch (Exception var3) {
         LOGGER.error("Could not parse McoServer: {}", (Object)var3.getMessage());
      }

      return realmsserver;
   }

   private static void sortInvited(RealmsServer realmsserver) {
      realmsserver.players.sort((playerinfo, playerinfo1) -> ComparisonChain.start().compareFalseFirst(playerinfo1.getAccepted(), playerinfo.getAccepted()).compare(playerinfo.getName().toLowerCase(Locale.ROOT), playerinfo1.getName().toLowerCase(Locale.ROOT)).result());
   }

   private static List<PlayerInfo> parseInvited(JsonArray jsonarray) {
      List<PlayerInfo> list = Lists.newArrayList();

      for(JsonElement jsonelement : jsonarray) {
         try {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            PlayerInfo playerinfo = new PlayerInfo();
            playerinfo.setName(JsonUtils.getStringOr("name", jsonobject, (String)null));
            playerinfo.setUuid(JsonUtils.getStringOr("uuid", jsonobject, (String)null));
            playerinfo.setOperator(JsonUtils.getBooleanOr("operator", jsonobject, false));
            playerinfo.setAccepted(JsonUtils.getBooleanOr("accepted", jsonobject, false));
            playerinfo.setOnline(JsonUtils.getBooleanOr("online", jsonobject, false));
            list.add(playerinfo);
         } catch (Exception var6) {
         }
      }

      return list;
   }

   private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray jsonarray) {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();

      for(JsonElement jsonelement : jsonarray) {
         try {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            JsonParser jsonparser = new JsonParser();
            JsonElement jsonelement1 = jsonparser.parse(jsonobject.get("options").getAsString());
            RealmsWorldOptions realmsworldoptions;
            if (jsonelement1 == null) {
               realmsworldoptions = RealmsWorldOptions.createDefaults();
            } else {
               realmsworldoptions = RealmsWorldOptions.parse(jsonelement1.getAsJsonObject());
            }

            int i = JsonUtils.getIntOr("slotId", jsonobject, -1);
            map.put(i, realmsworldoptions);
         } catch (Exception var9) {
         }
      }

      for(int j = 1; j <= 3; ++j) {
         if (!map.containsKey(j)) {
            map.put(j, RealmsWorldOptions.createEmptyDefaults());
         }
      }

      return map;
   }

   private static Map<Integer, RealmsWorldOptions> createEmptySlots() {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();
      map.put(1, RealmsWorldOptions.createEmptyDefaults());
      map.put(2, RealmsWorldOptions.createEmptyDefaults());
      map.put(3, RealmsWorldOptions.createEmptyDefaults());
      return map;
   }

   public static RealmsServer parse(String s) {
      try {
         return parse((new JsonParser()).parse(s).getAsJsonObject());
      } catch (Exception var2) {
         LOGGER.error("Could not parse McoServer: {}", (Object)var2.getMessage());
         return new RealmsServer();
      }
   }

   private static RealmsServer.State getState(String s) {
      try {
         return RealmsServer.State.valueOf(s);
      } catch (Exception var2) {
         return RealmsServer.State.CLOSED;
      }
   }

   private static RealmsServer.WorldType getWorldType(String s) {
      try {
         return RealmsServer.WorldType.valueOf(s);
      } catch (Exception var2) {
         return RealmsServer.WorldType.NORMAL;
      }
   }

   public int hashCode() {
      return Objects.hash(this.id, this.name, this.motd, this.state, this.owner, this.expired);
   }

   public boolean equals(Object object) {
      if (object == null) {
         return false;
      } else if (object == this) {
         return true;
      } else if (object.getClass() != this.getClass()) {
         return false;
      } else {
         RealmsServer realmsserver = (RealmsServer)object;
         return (new EqualsBuilder()).append(this.id, realmsserver.id).append((Object)this.name, (Object)realmsserver.name).append((Object)this.motd, (Object)realmsserver.motd).append((Object)this.state, (Object)realmsserver.state).append((Object)this.owner, (Object)realmsserver.owner).append(this.expired, realmsserver.expired).append((Object)this.worldType, (Object)this.worldType).isEquals();
      }
   }

   public RealmsServer clone() {
      RealmsServer realmsserver = new RealmsServer();
      realmsserver.id = this.id;
      realmsserver.remoteSubscriptionId = this.remoteSubscriptionId;
      realmsserver.name = this.name;
      realmsserver.motd = this.motd;
      realmsserver.state = this.state;
      realmsserver.owner = this.owner;
      realmsserver.players = this.players;
      realmsserver.slots = this.cloneSlots(this.slots);
      realmsserver.expired = this.expired;
      realmsserver.expiredTrial = this.expiredTrial;
      realmsserver.daysLeft = this.daysLeft;
      realmsserver.serverPing = new RealmsServerPing();
      realmsserver.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
      realmsserver.serverPing.playerList = this.serverPing.playerList;
      realmsserver.worldType = this.worldType;
      realmsserver.ownerUUID = this.ownerUUID;
      realmsserver.minigameName = this.minigameName;
      realmsserver.activeSlot = this.activeSlot;
      realmsserver.minigameId = this.minigameId;
      realmsserver.minigameImage = this.minigameImage;
      return realmsserver;
   }

   public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> map) {
      Map<Integer, RealmsWorldOptions> map1 = Maps.newHashMap();

      for(Map.Entry<Integer, RealmsWorldOptions> map_entry : map.entrySet()) {
         map1.put(map_entry.getKey(), map_entry.getValue().clone());
      }

      return map1;
   }

   public String getWorldName(int i) {
      return this.name + " (" + this.slots.get(i).getSlotName(i) + ")";
   }

   public ServerData toServerData(String s) {
      return new ServerData(this.name, s, false);
   }

   public static class McoServerComparator implements Comparator<RealmsServer> {
      private final String refOwner;

      public McoServerComparator(String s) {
         this.refOwner = s;
      }

      public int compare(RealmsServer realmsserver, RealmsServer realmsserver1) {
         return ComparisonChain.start().compareTrueFirst(realmsserver.state == RealmsServer.State.UNINITIALIZED, realmsserver1.state == RealmsServer.State.UNINITIALIZED).compareTrueFirst(realmsserver.expiredTrial, realmsserver1.expiredTrial).compareTrueFirst(realmsserver.owner.equals(this.refOwner), realmsserver1.owner.equals(this.refOwner)).compareFalseFirst(realmsserver.expired, realmsserver1.expired).compareTrueFirst(realmsserver.state == RealmsServer.State.OPEN, realmsserver1.state == RealmsServer.State.OPEN).compare(realmsserver.id, realmsserver1.id).result();
      }
   }

   public static enum State {
      CLOSED,
      OPEN,
      UNINITIALIZED;
   }

   public static enum WorldType {
      NORMAL,
      MINIGAME,
      ADVENTUREMAP,
      EXPERIENCE,
      INSPIRATION;
   }
}
