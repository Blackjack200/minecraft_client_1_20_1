package net.minecraft.client.multiplayer.chat.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import org.apache.commons.lang3.StringUtils;

public class ChatReportBuilder {
   private final ChatReportBuilder.ChatReport report;
   private final AbuseReportLimits limits;

   public ChatReportBuilder(ChatReportBuilder.ChatReport chatreportbuilder_chatreport, AbuseReportLimits abusereportlimits) {
      this.report = chatreportbuilder_chatreport;
      this.limits = abusereportlimits;
   }

   public ChatReportBuilder(UUID uuid, AbuseReportLimits abusereportlimits) {
      this.report = new ChatReportBuilder.ChatReport(UUID.randomUUID(), Instant.now(), uuid);
      this.limits = abusereportlimits;
   }

   public ChatReportBuilder.ChatReport report() {
      return this.report;
   }

   public UUID reportedProfileId() {
      return this.report.reportedProfileId;
   }

   public IntSet reportedMessages() {
      return this.report.reportedMessages;
   }

   public String comments() {
      return this.report.comments;
   }

   public void setComments(String s) {
      this.report.comments = s;
   }

   @Nullable
   public ReportReason reason() {
      return this.report.reason;
   }

   public void setReason(ReportReason reportreason) {
      this.report.reason = reportreason;
   }

   public void toggleReported(int i) {
      this.report.toggleReported(i, this.limits);
   }

   public boolean isReported(int i) {
      return this.report.reportedMessages.contains(i);
   }

   public boolean hasContent() {
      return StringUtils.isNotEmpty(this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
   }

   @Nullable
   public ChatReportBuilder.CannotBuildReason checkBuildable() {
      if (this.report.reportedMessages.isEmpty()) {
         return ChatReportBuilder.CannotBuildReason.NO_REPORTED_MESSAGES;
      } else if (this.report.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
         return ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES;
      } else if (this.report.reason == null) {
         return ChatReportBuilder.CannotBuildReason.NO_REASON;
      } else {
         return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? ChatReportBuilder.CannotBuildReason.COMMENTS_TOO_LONG : null;
      }
   }

   public Either<ChatReportBuilder.Result, ChatReportBuilder.CannotBuildReason> build(ReportingContext reportingcontext) {
      ChatReportBuilder.CannotBuildReason chatreportbuilder_cannotbuildreason = this.checkBuildable();
      if (chatreportbuilder_cannotbuildreason != null) {
         return Either.right(chatreportbuilder_cannotbuildreason);
      } else {
         String s = Objects.requireNonNull(this.report.reason).backendName();
         ReportEvidence reportevidence = this.buildEvidence(reportingcontext.chatLog());
         ReportedEntity reportedentity = new ReportedEntity(this.report.reportedProfileId);
         AbuseReport abusereport = new AbuseReport(this.report.comments, s, reportevidence, reportedentity, this.report.createdAt);
         return Either.left(new ChatReportBuilder.Result(this.report.reportId, abusereport));
      }
   }

   private ReportEvidence buildEvidence(ChatLog chatlog) {
      List<ReportChatMessage> list = new ArrayList<>();
      ChatReportContextBuilder chatreportcontextbuilder = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
      chatreportcontextbuilder.collectAllContext(chatlog, this.report.reportedMessages, (i, loggedchatmessage_player) -> list.add(this.buildReportedChatMessage(loggedchatmessage_player, this.isReported(i))));
      return new ReportEvidence(Lists.reverse(list));
   }

   private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player loggedchatmessage_player, boolean flag) {
      SignedMessageLink signedmessagelink = loggedchatmessage_player.message().link();
      SignedMessageBody signedmessagebody = loggedchatmessage_player.message().signedBody();
      List<ByteBuffer> list = signedmessagebody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
      ByteBuffer bytebuffer = Optionull.map(loggedchatmessage_player.message().signature(), MessageSignature::asByteBuffer);
      return new ReportChatMessage(signedmessagelink.index(), signedmessagelink.sender(), signedmessagelink.sessionId(), signedmessagebody.timeStamp(), signedmessagebody.salt(), list, signedmessagebody.content(), bytebuffer, flag);
   }

   public ChatReportBuilder copy() {
      return new ChatReportBuilder(this.report.copy(), this.limits);
   }

   public static record CannotBuildReason(Component message) {
      public static final ChatReportBuilder.CannotBuildReason NO_REASON = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.no_reason"));
      public static final ChatReportBuilder.CannotBuildReason NO_REPORTED_MESSAGES = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.no_reported_messages"));
      public static final ChatReportBuilder.CannotBuildReason TOO_MANY_MESSAGES = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.too_many_messages"));
      public static final ChatReportBuilder.CannotBuildReason COMMENTS_TOO_LONG = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.comments_too_long"));
   }

   public class ChatReport {
      final UUID reportId;
      final Instant createdAt;
      final UUID reportedProfileId;
      final IntSet reportedMessages = new IntOpenHashSet();
      String comments = "";
      @Nullable
      ReportReason reason;

      ChatReport(UUID uuid, Instant instant, UUID uuid1) {
         this.reportId = uuid;
         this.createdAt = instant;
         this.reportedProfileId = uuid1;
      }

      public void toggleReported(int i, AbuseReportLimits abusereportlimits) {
         if (this.reportedMessages.contains(i)) {
            this.reportedMessages.remove(i);
         } else if (this.reportedMessages.size() < abusereportlimits.maxReportedMessageCount()) {
            this.reportedMessages.add(i);
         }

      }

      public ChatReportBuilder.ChatReport copy() {
         ChatReportBuilder.ChatReport chatreportbuilder_chatreport = ChatReportBuilder.this.new ChatReport(this.reportId, this.createdAt, this.reportedProfileId);
         chatreportbuilder_chatreport.reportedMessages.addAll(this.reportedMessages);
         chatreportbuilder_chatreport.comments = this.comments;
         chatreportbuilder_chatreport.reason = this.reason;
         return chatreportbuilder_chatreport;
      }

      public boolean isReportedPlayer(UUID uuid) {
         return uuid.equals(this.reportedProfileId);
      }
   }

   public static record Result(UUID id, AbuseReport report) {
   }
}
