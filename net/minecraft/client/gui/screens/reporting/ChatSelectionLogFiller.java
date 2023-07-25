package net.minecraft.client.gui.screens.reporting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageLink;

public class ChatSelectionLogFiller {
   private final ChatLog log;
   private final ChatReportContextBuilder contextBuilder;
   private final Predicate<LoggedChatMessage.Player> canReport;
   @Nullable
   private SignedMessageLink previousLink = null;
   private int eventId;
   private int missedCount;
   @Nullable
   private PlayerChatMessage lastMessage;

   public ChatSelectionLogFiller(ReportingContext reportingcontext, Predicate<LoggedChatMessage.Player> predicate) {
      this.log = reportingcontext.chatLog();
      this.contextBuilder = new ChatReportContextBuilder(reportingcontext.sender().reportLimits().leadingContextMessageCount());
      this.canReport = predicate;
      this.eventId = this.log.end();
   }

   public void fillNextPage(int i, ChatSelectionLogFiller.Output chatselectionlogfiller_output) {
      int j = 0;

      while(j < i) {
         LoggedChatEvent loggedchatevent = this.log.lookup(this.eventId);
         if (loggedchatevent == null) {
            break;
         }

         int k = this.eventId--;
         if (loggedchatevent instanceof LoggedChatMessage.Player loggedchatmessage_player) {
            if (!loggedchatmessage_player.message().equals(this.lastMessage)) {
               if (this.acceptMessage(chatselectionlogfiller_output, loggedchatmessage_player)) {
                  if (this.missedCount > 0) {
                     chatselectionlogfiller_output.acceptDivider(Component.translatable("gui.chatSelection.fold", this.missedCount));
                     this.missedCount = 0;
                  }

                  chatselectionlogfiller_output.acceptMessage(k, loggedchatmessage_player);
                  ++j;
               } else {
                  ++this.missedCount;
               }

               this.lastMessage = loggedchatmessage_player.message();
            }
         }
      }

   }

   private boolean acceptMessage(ChatSelectionLogFiller.Output chatselectionlogfiller_output, LoggedChatMessage.Player loggedchatmessage_player) {
      PlayerChatMessage playerchatmessage = loggedchatmessage_player.message();
      boolean flag = this.contextBuilder.acceptContext(playerchatmessage);
      if (this.canReport.test(loggedchatmessage_player)) {
         this.contextBuilder.trackContext(playerchatmessage);
         if (this.previousLink != null && !this.previousLink.isDescendantOf(playerchatmessage.link())) {
            chatselectionlogfiller_output.acceptDivider(Component.translatable("gui.chatSelection.join", loggedchatmessage_player.profile().getName()).withStyle(ChatFormatting.YELLOW));
         }

         this.previousLink = playerchatmessage.link();
         return true;
      } else {
         return flag;
      }
   }

   public interface Output {
      void acceptMessage(int i, LoggedChatMessage.Player loggedchatmessage_player);

      void acceptDivider(Component component);
   }
}
