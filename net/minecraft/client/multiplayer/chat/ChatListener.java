package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.StringUtils;

public class ChatListener {
   private final Minecraft minecraft;
   private final Deque<ChatListener.Message> delayedMessageQueue = Queues.newArrayDeque();
   private long messageDelay;
   private long previousMessageTime;

   public ChatListener(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void tick() {
      if (this.messageDelay != 0L) {
         if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
            for(ChatListener.Message chatlistener_message = this.delayedMessageQueue.poll(); chatlistener_message != null && !chatlistener_message.accept(); chatlistener_message = this.delayedMessageQueue.poll()) {
            }
         }

      }
   }

   public void setMessageDelay(double d0) {
      long i = (long)(d0 * 1000.0D);
      if (i == 0L && this.messageDelay > 0L) {
         this.delayedMessageQueue.forEach(ChatListener.Message::accept);
         this.delayedMessageQueue.clear();
      }

      this.messageDelay = i;
   }

   public void acceptNextDelayedMessage() {
      this.delayedMessageQueue.remove().accept();
   }

   public long queueSize() {
      return (long)this.delayedMessageQueue.size();
   }

   public void clearQueue() {
      this.delayedMessageQueue.forEach(ChatListener.Message::accept);
      this.delayedMessageQueue.clear();
   }

   public boolean removeFromDelayedMessageQueue(MessageSignature messagesignature) {
      return this.delayedMessageQueue.removeIf((chatlistener_message) -> messagesignature.equals(chatlistener_message.signature()));
   }

   private boolean willDelayMessages() {
      return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
   }

   private void handleMessage(@Nullable MessageSignature messagesignature, BooleanSupplier booleansupplier) {
      if (this.willDelayMessages()) {
         this.delayedMessageQueue.add(new ChatListener.Message(messagesignature, booleansupplier));
      } else {
         booleansupplier.getAsBoolean();
      }

   }

   public void handlePlayerChatMessage(PlayerChatMessage playerchatmessage, GameProfile gameprofile, ChatType.Bound chattype_bound) {
      boolean flag = this.minecraft.options.onlyShowSecureChat().get();
      PlayerChatMessage playerchatmessage1 = flag ? playerchatmessage.removeUnsignedContent() : playerchatmessage;
      Component component = chattype_bound.decorate(playerchatmessage1.decoratedContent());
      Instant instant = Instant.now();
      this.handleMessage(playerchatmessage.signature(), () -> {
         boolean flag2 = this.showMessageToPlayer(chattype_bound, playerchatmessage, component, gameprofile, flag, instant);
         ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
         if (clientpacketlistener != null) {
            clientpacketlistener.markMessageAsProcessed(playerchatmessage, flag2);
         }

         return flag2;
      });
   }

   public void handleDisguisedChatMessage(Component component, ChatType.Bound chattype_bound) {
      Instant instant = Instant.now();
      this.handleMessage((MessageSignature)null, () -> {
         Component component2 = chattype_bound.decorate(component);
         this.minecraft.gui.getChat().addMessage(component2);
         this.narrateChatMessage(chattype_bound, component);
         this.logSystemMessage(component2, instant);
         this.previousMessageTime = Util.getMillis();
         return true;
      });
   }

   private boolean showMessageToPlayer(ChatType.Bound chattype_bound, PlayerChatMessage playerchatmessage, Component component, GameProfile gameprofile, boolean flag, Instant instant) {
      ChatTrustLevel chattrustlevel = this.evaluateTrustLevel(playerchatmessage, component, instant);
      if (flag && chattrustlevel.isNotSecure()) {
         return false;
      } else if (!this.minecraft.isBlocked(playerchatmessage.sender()) && !playerchatmessage.isFullyFiltered()) {
         GuiMessageTag guimessagetag = chattrustlevel.createTag(playerchatmessage);
         MessageSignature messagesignature = playerchatmessage.signature();
         FilterMask filtermask = playerchatmessage.filterMask();
         if (filtermask.isEmpty()) {
            this.minecraft.gui.getChat().addMessage(component, messagesignature, guimessagetag);
            this.narrateChatMessage(chattype_bound, playerchatmessage.decoratedContent());
         } else {
            Component component1 = filtermask.applyWithFormatting(playerchatmessage.signedContent());
            if (component1 != null) {
               this.minecraft.gui.getChat().addMessage(chattype_bound.decorate(component1), messagesignature, guimessagetag);
               this.narrateChatMessage(chattype_bound, component1);
            }
         }

         this.logPlayerMessage(playerchatmessage, chattype_bound, gameprofile, chattrustlevel);
         this.previousMessageTime = Util.getMillis();
         return true;
      } else {
         return false;
      }
   }

   private void narrateChatMessage(ChatType.Bound chattype_bound, Component component) {
      this.minecraft.getNarrator().sayChat(chattype_bound.decorateNarration(component));
   }

   private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage playerchatmessage, Component component, Instant instant) {
      return this.isSenderLocalPlayer(playerchatmessage.sender()) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(playerchatmessage, component, instant);
   }

   private void logPlayerMessage(PlayerChatMessage playerchatmessage, ChatType.Bound chattype_bound, GameProfile gameprofile, ChatTrustLevel chattrustlevel) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.player(gameprofile, playerchatmessage, chattrustlevel));
   }

   private void logSystemMessage(Component component, Instant instant) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.system(component, instant));
   }

   public void handleSystemMessage(Component component, boolean flag) {
      if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(component))) {
         if (flag) {
            this.minecraft.gui.setOverlayMessage(component, false);
         } else {
            this.minecraft.gui.getChat().addMessage(component);
            this.logSystemMessage(component, Instant.now());
         }

         this.minecraft.getNarrator().say(component);
      }
   }

   private UUID guessChatUUID(Component component) {
      String s = StringDecomposer.getPlainText(component);
      String s1 = StringUtils.substringBetween(s, "<", ">");
      return s1 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(s1);
   }

   private boolean isSenderLocalPlayer(UUID uuid) {
      if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
         UUID uuid1 = this.minecraft.player.getGameProfile().getId();
         return uuid1.equals(uuid);
      } else {
         return false;
      }
   }

   static record Message(@Nullable MessageSignature signature, BooleanSupplier handler) {
      public boolean accept() {
         return this.handler.getAsBoolean();
      }
   }
}
