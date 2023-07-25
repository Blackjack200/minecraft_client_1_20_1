package com.mojang.realmsclient.client;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;

public class RealmsClient {
   public static RealmsClient.Environment currentEnvironment = RealmsClient.Environment.PRODUCTION;
   private static boolean initialized;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String sessionId;
   private final String username;
   private final Minecraft minecraft;
   private static final String WORLDS_RESOURCE_PATH = "worlds";
   private static final String INVITES_RESOURCE_PATH = "invites";
   private static final String MCO_RESOURCE_PATH = "mco";
   private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
   private static final String ACTIVITIES_RESOURCE = "activities";
   private static final String OPS_RESOURCE = "ops";
   private static final String REGIONS_RESOURCE = "regions/ping/stat";
   private static final String TRIALS_RESOURCE = "trial";
   private static final String NOTIFICATIONS_RESOURCE = "notifications";
   private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
   private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
   private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
   private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
   private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
   private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
   private static final String PATH_AVAILABLE = "/available";
   private static final String PATH_TEMPLATES = "/templates/$WORLD_TYPE";
   private static final String PATH_WORLD_JOIN = "/v1/$ID/join/pc";
   private static final String PATH_WORLD_GET = "/$ID";
   private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
   private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
   private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
   private static final String PATH_PENDING_INVITES = "/pending";
   private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
   private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
   private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
   private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
   private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
   private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
   private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
   private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
   private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
   private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
   private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/slot/$SLOT_ID/download";
   private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
   private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
   private static final String PATH_TOS_AGREED = "/tos/agreed";
   private static final String PATH_NEWS = "/v1/news";
   private static final String PATH_MARK_NOTIFICATIONS_SEEN = "/seen";
   private static final String PATH_DISMISS_NOTIFICATIONS = "/dismiss";
   private static final String PATH_STAGE_AVAILABLE = "/stageAvailable";
   private static final GuardedSerializer GSON = new GuardedSerializer();

   public static RealmsClient create() {
      Minecraft minecraft = Minecraft.getInstance();
      return create(minecraft);
   }

   public static RealmsClient create(Minecraft minecraft) {
      String s = minecraft.getUser().getName();
      String s1 = minecraft.getUser().getSessionId();
      if (!initialized) {
         initialized = true;
         Optional<String> optional = Optional.ofNullable(System.getenv("realms.environment")).or(() -> Optional.ofNullable(System.getProperty("realms.environment")));
         optional.flatMap(RealmsClient.Environment::byName).ifPresent((realmsclient_environment) -> currentEnvironment = realmsclient_environment);
      }

      return new RealmsClient(s1, s, minecraft);
   }

   public static void switchToStage() {
      currentEnvironment = RealmsClient.Environment.STAGE;
   }

   public static void switchToProd() {
      currentEnvironment = RealmsClient.Environment.PRODUCTION;
   }

   public static void switchToLocal() {
      currentEnvironment = RealmsClient.Environment.LOCAL;
   }

   public RealmsClient(String s, String s1, Minecraft minecraft) {
      this.sessionId = s;
      this.username = s1;
      this.minecraft = minecraft;
      RealmsClientConfig.setProxy(minecraft.getProxy());
   }

   public RealmsServerList listWorlds() throws RealmsServiceException {
      String s = this.url("worlds");
      String s1 = this.execute(Request.get(s));
      return RealmsServerList.parse(s1);
   }

   public List<RealmsNotification> getNotifications() throws RealmsServiceException {
      String s = this.url("notifications");
      String s1 = this.execute(Request.get(s));
      List<RealmsNotification> list = RealmsNotification.parseList(s1);
      return list.size() > 1 ? List.of(list.get(0)) : list;
   }

   private static JsonArray uuidListToJsonArray(List<UUID> list) {
      JsonArray jsonarray = new JsonArray();

      for(UUID uuid : list) {
         if (uuid != null) {
            jsonarray.add(uuid.toString());
         }
      }

      return jsonarray;
   }

   public void notificationsSeen(List<UUID> list) throws RealmsServiceException {
      String s = this.url("notifications/seen");
      this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(list))));
   }

   public void notificationsDismiss(List<UUID> list) throws RealmsServiceException {
      String s = this.url("notifications/dismiss");
      this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(list))));
   }

   public RealmsServer getOwnWorld(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(i)));
      String s1 = this.execute(Request.get(s));
      return RealmsServer.parse(s1);
   }

   public ServerActivityList getActivity(long i) throws RealmsServiceException {
      String s = this.url("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.get(s));
      return ServerActivityList.parse(s1);
   }

   public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
      String s = this.url("activities/liveplayerlist");
      String s1 = this.execute(Request.get(s));
      return RealmsServerPlayerLists.parse(s1);
   }

   public RealmsServerAddress join(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + i));
      String s1 = this.execute(Request.get(s, 5000, 30000));
      return RealmsServerAddress.parse(s1);
   }

   public void initializeWorld(long i, String s, String s1) throws RealmsServiceException {
      RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(s, s1);
      String s2 = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(i)));
      String s3 = GSON.toJson(realmsdescriptiondto);
      this.execute(Request.post(s2, s3, 5000, 10000));
   }

   public Boolean mcoEnabled() throws RealmsServiceException {
      String s = this.url("mco/available");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public Boolean stageAvailable() throws RealmsServiceException {
      String s = this.url("mco/stageAvailable");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
      String s = this.url("mco/client/compatible");
      String s1 = this.execute(Request.get(s));

      try {
         return RealmsClient.CompatibleVersionResponse.valueOf(s1);
      } catch (IllegalArgumentException var5) {
         throw new RealmsServiceException(500, "Could not check compatible version, got response: " + s1);
      }
   }

   public void uninvite(long i, String s) throws RealmsServiceException {
      String s1 = this.url("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(i)).replace("$UUID", s));
      this.execute(Request.delete(s1));
   }

   public void uninviteMyselfFrom(long i) throws RealmsServiceException {
      String s = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      this.execute(Request.delete(s));
   }

   public RealmsServer invite(long i, String s) throws RealmsServiceException {
      PlayerInfo playerinfo = new PlayerInfo();
      playerinfo.setName(s);
      String s1 = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      String s2 = this.execute(Request.post(s1, GSON.toJson(playerinfo)));
      return RealmsServer.parse(s2);
   }

   public BackupList backupsFor(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.get(s));
      return BackupList.parse(s1);
   }

   public void update(long i, String s, String s1) throws RealmsServiceException {
      RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(s, s1);
      String s2 = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      this.execute(Request.post(s2, GSON.toJson(realmsdescriptiondto)));
   }

   public void updateSlot(long i, int j, RealmsWorldOptions realmsworldoptions) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(i)).replace("$SLOT_ID", String.valueOf(j)));
      String s1 = realmsworldoptions.toJson();
      this.execute(Request.post(s, s1));
   }

   public boolean switchSlot(long i, int j) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(i)).replace("$SLOT_ID", String.valueOf(j)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public void restoreWorld(long i, String s) throws RealmsServiceException {
      String s1 = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(i)), "backupId=" + s);
      this.execute(Request.put(s1, "", 40000, 600000));
   }

   public WorldTemplatePaginatedList fetchWorldTemplates(int i, int j, RealmsServer.WorldType realmsserver_worldtype) throws RealmsServiceException {
      String s = this.url("worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", realmsserver_worldtype.toString()), String.format(Locale.ROOT, "page=%d&pageSize=%d", i, j));
      String s1 = this.execute(Request.get(s));
      return WorldTemplatePaginatedList.parse(s1);
   }

   public Boolean putIntoMinigameMode(long i, String s) throws RealmsServiceException {
      String s1 = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", s).replace("$WORLD_ID", String.valueOf(i));
      String s2 = this.url("worlds" + s1);
      return Boolean.valueOf(this.execute(Request.put(s2, "")));
   }

   public Ops op(long i, String s) throws RealmsServiceException {
      String s1 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(i)).replace("$PROFILE_UUID", s);
      String s2 = this.url("ops" + s1);
      return Ops.parse(this.execute(Request.post(s2, "")));
   }

   public Ops deop(long i, String s) throws RealmsServiceException {
      String s1 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(i)).replace("$PROFILE_UUID", s);
      String s2 = this.url("ops" + s1);
      return Ops.parse(this.execute(Request.delete(s2)));
   }

   public Boolean open(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public Boolean close(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public Boolean resetWorldWithSeed(long i, WorldGenerationInfo worldgenerationinfo) throws RealmsServiceException {
      RealmsWorldResetDto realmsworldresetdto = new RealmsWorldResetDto(worldgenerationinfo.getSeed(), -1L, worldgenerationinfo.getLevelType().getDtoIndex(), worldgenerationinfo.shouldGenerateStructures());
      String s = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.post(s, GSON.toJson(realmsworldresetdto), 30000, 80000));
      return Boolean.valueOf(s1);
   }

   public Boolean resetWorldWithTemplate(long i, String s) throws RealmsServiceException {
      RealmsWorldResetDto realmsworldresetdto = new RealmsWorldResetDto((String)null, Long.valueOf(s), -1, false);
      String s1 = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(i)));
      String s2 = this.execute(Request.post(s1, GSON.toJson(realmsworldresetdto), 30000, 80000));
      return Boolean.valueOf(s2);
   }

   public Subscription subscriptionFor(long i) throws RealmsServiceException {
      String s = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      String s1 = this.execute(Request.get(s));
      return Subscription.parse(s1);
   }

   public int pendingInvitesCount() throws RealmsServiceException {
      return this.pendingInvites().pendingInvites.size();
   }

   public PendingInvitesList pendingInvites() throws RealmsServiceException {
      String s = this.url("invites/pending");
      String s1 = this.execute(Request.get(s));
      PendingInvitesList pendinginviteslist = PendingInvitesList.parse(s1);
      pendinginviteslist.pendingInvites.removeIf(this::isBlocked);
      return pendinginviteslist;
   }

   private boolean isBlocked(PendingInvite pendinginvite) {
      try {
         UUID uuid = UUID.fromString(pendinginvite.worldOwnerUuid);
         return this.minecraft.getPlayerSocialManager().isBlocked(uuid);
      } catch (IllegalArgumentException var3) {
         return false;
      }
   }

   public void acceptInvitation(String s) throws RealmsServiceException {
      String s1 = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", s));
      this.execute(Request.put(s1, ""));
   }

   public WorldDownload requestDownloadInfo(long i, int j) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(i)).replace("$SLOT_ID", String.valueOf(j)));
      String s1 = this.execute(Request.get(s));
      return WorldDownload.parse(s1);
   }

   @Nullable
   public UploadInfo requestUploadInfo(long i, @Nullable String s) throws RealmsServiceException {
      String s1 = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(i)));
      return UploadInfo.parse(this.execute(Request.put(s1, UploadInfo.createRequest(s))));
   }

   public void rejectInvitation(String s) throws RealmsServiceException {
      String s1 = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", s));
      this.execute(Request.put(s1, ""));
   }

   public void agreeToTos() throws RealmsServiceException {
      String s = this.url("mco/tos/agreed");
      this.execute(Request.post(s, ""));
   }

   public RealmsNews getNews() throws RealmsServiceException {
      String s = this.url("mco/v1/news");
      String s1 = this.execute(Request.get(s, 5000, 10000));
      return RealmsNews.parse(s1);
   }

   public void sendPingResults(PingResult pingresult) throws RealmsServiceException {
      String s = this.url("regions/ping/stat");
      this.execute(Request.post(s, GSON.toJson(pingresult)));
   }

   public Boolean trialAvailable() throws RealmsServiceException {
      String s = this.url("trial");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public void deleteWorld(long i) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(i)));
      this.execute(Request.delete(s));
   }

   private String url(String s) {
      return this.url(s, (String)null);
   }

   private String url(String s, @Nullable String s1) {
      try {
         return (new URI(currentEnvironment.protocol, currentEnvironment.baseUrl, "/" + s, s1, (String)null)).toASCIIString();
      } catch (URISyntaxException var4) {
         throw new IllegalArgumentException(s, var4);
      }
   }

   private String execute(Request<?> request) throws RealmsServiceException {
      request.cookie("sid", this.sessionId);
      request.cookie("user", this.username);
      request.cookie("version", SharedConstants.getCurrentVersion().getName());

      try {
         int i = request.responseCode();
         if (i != 503 && i != 277) {
            String s = request.text();
            if (i >= 200 && i < 300) {
               return s;
            } else if (i == 401) {
               String s1 = request.getHeader("WWW-Authenticate");
               LOGGER.info("Could not authorize you against Realms server: {}", (Object)s1);
               throw new RealmsServiceException(i, s1);
            } else {
               RealmsError realmserror = RealmsError.parse(s);
               if (realmserror != null) {
                  LOGGER.error("Realms http code: {} -  error code: {} -  message: {} - raw body: {}", i, realmserror.getErrorCode(), realmserror.getErrorMessage(), s);
                  throw new RealmsServiceException(i, s, realmserror);
               } else {
                  LOGGER.error("Realms http code: {} - raw body (message failed to parse): {}", i, s);
                  String s2 = getHttpCodeDescription(i);
                  throw new RealmsServiceException(i, s2);
               }
            }
         } else {
            int j = request.getRetryAfterHeader();
            throw new RetryCallException(j, i);
         }
      } catch (RealmsHttpException var6) {
         throw new RealmsServiceException(500, "Could not connect to Realms: " + var6.getMessage());
      }
   }

   private static String getHttpCodeDescription(int i) {
      String var10000;
      switch (i) {
         case 429:
            var10000 = I18n.get("mco.errorMessage.serviceBusy");
            break;
         default:
            var10000 = "Unknown error";
      }

      return var10000;
   }

   public static enum CompatibleVersionResponse {
      COMPATIBLE,
      OUTDATED,
      OTHER;
   }

   public static enum Environment {
      PRODUCTION("pc.realms.minecraft.net", "https"),
      STAGE("pc-stage.realms.minecraft.net", "https"),
      LOCAL("localhost:8080", "http");

      public String baseUrl;
      public String protocol;

      private Environment(String s, String s1) {
         this.baseUrl = s;
         this.protocol = s1;
      }

      public static Optional<RealmsClient.Environment> byName(String s) {
         Optional var10000;
         switch (s.toLowerCase(Locale.ROOT)) {
            case "production":
               var10000 = Optional.of(PRODUCTION);
               break;
            case "local":
               var10000 = Optional.of(LOCAL);
               break;
            case "stage":
               var10000 = Optional.of(STAGE);
               break;
            default:
               var10000 = Optional.empty();
         }

         return var10000;
      }
   }
}
