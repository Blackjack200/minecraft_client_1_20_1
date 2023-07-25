package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.network.chat.Component;

public final class ReportingContext {
   private static final int LOG_CAPACITY = 1024;
   private final AbuseReportSender sender;
   private final ReportEnvironment environment;
   private final ChatLog chatLog;
   @Nullable
   private ChatReportBuilder.ChatReport chatReportDraft;

   public ReportingContext(AbuseReportSender abusereportsender, ReportEnvironment reportenvironment, ChatLog chatlog) {
      this.sender = abusereportsender;
      this.environment = reportenvironment;
      this.chatLog = chatlog;
   }

   public static ReportingContext create(ReportEnvironment reportenvironment, UserApiService userapiservice) {
      ChatLog chatlog = new ChatLog(1024);
      AbuseReportSender abusereportsender = AbuseReportSender.create(reportenvironment, userapiservice);
      return new ReportingContext(abusereportsender, reportenvironment, chatlog);
   }

   public void draftReportHandled(Minecraft minecraft, @Nullable Screen screen, Runnable runnable, boolean flag) {
      if (this.chatReportDraft != null) {
         ChatReportBuilder.ChatReport chatreportbuilder_chatreport = this.chatReportDraft.copy();
         minecraft.setScreen(new ConfirmScreen((flag1) -> {
            this.setChatReportDraft((ChatReportBuilder.ChatReport)null);
            if (flag1) {
               minecraft.setScreen(new ChatReportScreen(screen, this, chatreportbuilder_chatreport));
            } else {
               runnable.run();
            }

         }, Component.translatable(flag ? "gui.chatReport.draft.quittotitle.title" : "gui.chatReport.draft.title"), Component.translatable(flag ? "gui.chatReport.draft.quittotitle.content" : "gui.chatReport.draft.content"), Component.translatable("gui.chatReport.draft.edit"), Component.translatable("gui.chatReport.draft.discard")));
      } else {
         runnable.run();
      }

   }

   public AbuseReportSender sender() {
      return this.sender;
   }

   public ChatLog chatLog() {
      return this.chatLog;
   }

   public boolean matches(ReportEnvironment reportenvironment) {
      return Objects.equals(this.environment, reportenvironment);
   }

   public void setChatReportDraft(@Nullable ChatReportBuilder.ChatReport chatreportbuilder_chatreport) {
      this.chatReportDraft = chatreportbuilder_chatreport;
   }

   public boolean hasDraftReport() {
      return this.chatReportDraft != null;
   }

   public boolean hasDraftReportFor(UUID uuid) {
      return this.hasDraftReport() && this.chatReportDraft.isReportedPlayer(uuid);
   }
}
