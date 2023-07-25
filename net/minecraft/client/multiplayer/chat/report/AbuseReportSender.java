package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;

public interface AbuseReportSender {
   static AbuseReportSender create(ReportEnvironment reportenvironment, UserApiService userapiservice) {
      return new AbuseReportSender.Services(reportenvironment, userapiservice);
   }

   CompletableFuture<Unit> send(UUID uuid, AbuseReport abusereport);

   boolean isEnabled();

   default AbuseReportLimits reportLimits() {
      return AbuseReportLimits.DEFAULTS;
   }

   public static class SendException extends ThrowingComponent {
      public SendException(Component component, Throwable throwable) {
         super(component, throwable);
      }
   }

   public static record Services(ReportEnvironment environment, UserApiService userApiService) implements AbuseReportSender {
      private static final Component SERVICE_UNAVAILABLE_TEXT = Component.translatable("gui.abuseReport.send.service_unavailable");
      private static final Component HTTP_ERROR_TEXT = Component.translatable("gui.abuseReport.send.http_error");
      private static final Component JSON_ERROR_TEXT = Component.translatable("gui.abuseReport.send.json_error");

      public CompletableFuture<Unit> send(UUID uuid, AbuseReport abusereport) {
         return CompletableFuture.supplyAsync(() -> {
            AbuseReportRequest abusereportrequest = new AbuseReportRequest(1, uuid, abusereport, this.environment.clientInfo(), this.environment.thirdPartyServerInfo(), this.environment.realmInfo());

            try {
               this.userApiService.reportAbuse(abusereportrequest);
               return Unit.INSTANCE;
            } catch (MinecraftClientHttpException var6) {
               Component component = this.getHttpErrorDescription(var6);
               throw new CompletionException(new AbuseReportSender.SendException(component, var6));
            } catch (MinecraftClientException var7) {
               Component component1 = this.getErrorDescription(var7);
               throw new CompletionException(new AbuseReportSender.SendException(component1, var7));
            }
         }, Util.ioPool());
      }

      public boolean isEnabled() {
         return this.userApiService.canSendReports();
      }

      private Component getHttpErrorDescription(MinecraftClientHttpException minecraftclienthttpexception) {
         return Component.translatable("gui.abuseReport.send.error_message", minecraftclienthttpexception.getMessage());
      }

      private Component getErrorDescription(MinecraftClientException minecraftclientexception) {
         Component var10000;
         switch (minecraftclientexception.getType()) {
            case SERVICE_UNAVAILABLE:
               var10000 = SERVICE_UNAVAILABLE_TEXT;
               break;
            case HTTP_ERROR:
               var10000 = HTTP_ERROR_TEXT;
               break;
            case JSON_ERROR:
               var10000 = JSON_ERROR_TEXT;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public AbuseReportLimits reportLimits() {
         return this.userApiService.getAbuseReportLimits();
      }
   }
}
