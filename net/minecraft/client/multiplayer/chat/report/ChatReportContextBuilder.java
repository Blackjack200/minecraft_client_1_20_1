package net.minecraft.client.multiplayer.chat.report;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;

public class ChatReportContextBuilder {
   final int leadingCount;
   private final List<ChatReportContextBuilder.Collector> activeCollectors = new ArrayList<>();

   public ChatReportContextBuilder(int i) {
      this.leadingCount = i;
   }

   public void collectAllContext(ChatLog chatlog, IntCollection intcollection, ChatReportContextBuilder.Handler chatreportcontextbuilder_handler) {
      IntSortedSet intsortedset = new IntRBTreeSet(intcollection);

      for(int i = intsortedset.lastInt(); i >= chatlog.start() && (this.isActive() || !intsortedset.isEmpty()); --i) {
         LoggedChatEvent flag = chatlog.lookup(i);
         if (flag instanceof LoggedChatMessage.Player loggedchatmessage_player) {
            boolean flag = this.acceptContext(loggedchatmessage_player.message());
            if (intsortedset.remove(i)) {
               this.trackContext(loggedchatmessage_player.message());
               chatreportcontextbuilder_handler.accept(i, loggedchatmessage_player);
            } else if (flag) {
               chatreportcontextbuilder_handler.accept(i, loggedchatmessage_player);
            }
         }
      }

   }

   public void trackContext(PlayerChatMessage playerchatmessage) {
      this.activeCollectors.add(new ChatReportContextBuilder.Collector(playerchatmessage));
   }

   public boolean acceptContext(PlayerChatMessage playerchatmessage) {
      boolean flag = false;
      Iterator<ChatReportContextBuilder.Collector> iterator = this.activeCollectors.iterator();

      while(iterator.hasNext()) {
         ChatReportContextBuilder.Collector chatreportcontextbuilder_collector = iterator.next();
         if (chatreportcontextbuilder_collector.accept(playerchatmessage)) {
            flag = true;
            if (chatreportcontextbuilder_collector.isComplete()) {
               iterator.remove();
            }
         }
      }

      return flag;
   }

   public boolean isActive() {
      return !this.activeCollectors.isEmpty();
   }

   class Collector {
      private final Set<MessageSignature> lastSeenSignatures;
      private PlayerChatMessage lastChainMessage;
      private boolean collectingChain = true;
      private int count;

      Collector(PlayerChatMessage playerchatmessage) {
         this.lastSeenSignatures = new ObjectOpenHashSet<>(playerchatmessage.signedBody().lastSeen().entries());
         this.lastChainMessage = playerchatmessage;
      }

      boolean accept(PlayerChatMessage playerchatmessage) {
         if (playerchatmessage.equals(this.lastChainMessage)) {
            return false;
         } else {
            boolean flag = this.lastSeenSignatures.remove(playerchatmessage.signature());
            if (this.collectingChain && this.lastChainMessage.sender().equals(playerchatmessage.sender())) {
               if (this.lastChainMessage.link().isDescendantOf(playerchatmessage.link())) {
                  flag = true;
                  this.lastChainMessage = playerchatmessage;
               } else {
                  this.collectingChain = false;
               }
            }

            if (flag) {
               ++this.count;
            }

            return flag;
         }
      }

      boolean isComplete() {
         return this.count >= ChatReportContextBuilder.this.leadingCount || !this.collectingChain && this.lastSeenSignatures.isEmpty();
      }
   }

   public interface Handler {
      void accept(int i, LoggedChatMessage.Player loggedchatmessage_player);
   }
}
