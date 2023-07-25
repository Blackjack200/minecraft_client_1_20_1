package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;

public record ReportEnvironment(String clientVersion, @Nullable ReportEnvironment.Server server) {
   public static ReportEnvironment local() {
      return create((ReportEnvironment.Server)null);
   }

   public static ReportEnvironment thirdParty(String s) {
      return create(new ReportEnvironment.Server.ThirdParty(s));
   }

   public static ReportEnvironment realm(RealmsServer realmsserver) {
      return create(new ReportEnvironment.Server.Realm(realmsserver));
   }

   public static ReportEnvironment create(@Nullable ReportEnvironment.Server reportenvironment_server) {
      return new ReportEnvironment(getClientVersion(), reportenvironment_server);
   }

   public AbuseReportRequest.ClientInfo clientInfo() {
      return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
   }

   @Nullable
   public AbuseReportRequest.ThirdPartyServerInfo thirdPartyServerInfo() {
      ReportEnvironment.Server var2 = this.server;
      if (var2 instanceof ReportEnvironment.Server.ThirdParty reportenvironment_server_thirdparty) {
         return new AbuseReportRequest.ThirdPartyServerInfo(reportenvironment_server_thirdparty.ip);
      } else {
         return null;
      }
   }

   @Nullable
   public AbuseReportRequest.RealmInfo realmInfo() {
      ReportEnvironment.Server var2 = this.server;
      if (var2 instanceof ReportEnvironment.Server.Realm reportenvironment_server_realm) {
         return new AbuseReportRequest.RealmInfo(String.valueOf(reportenvironment_server_realm.realmId()), reportenvironment_server_realm.slotId());
      } else {
         return null;
      }
   }

   private static String getClientVersion() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("1.20.1");
      if (Minecraft.checkModStatus().shouldReportAsModified()) {
         stringbuilder.append(" (modded)");
      }

      return stringbuilder.toString();
   }

   public interface Server {
      public static record Realm(long realmId, int slotId) implements ReportEnvironment.Server {
         public Realm(RealmsServer realmsserver) {
            this(realmsserver.id, realmsserver.activeSlot);
         }
      }

      public static record ThirdParty(String ip) implements ReportEnvironment.Server {
         final String ip;
      }
   }
}
