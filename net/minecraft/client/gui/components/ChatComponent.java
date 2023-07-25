package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;

public class ChatComponent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_CHAT_HISTORY = 100;
   private static final int MESSAGE_NOT_FOUND = -1;
   private static final int MESSAGE_INDENT = 4;
   private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
   private static final int BOTTOM_MARGIN = 40;
   private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
   private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
   private final Minecraft minecraft;
   private final List<String> recentChat = Lists.newArrayList();
   private final List<GuiMessage> allMessages = Lists.newArrayList();
   private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
   private int chatScrollbarPos;
   private boolean newMessageSinceScroll;
   private final List<ChatComponent.DelayedMessageDeletion> messageDeletionQueue = new ArrayList<>();

   public ChatComponent(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void tick() {
      if (!this.messageDeletionQueue.isEmpty()) {
         this.processMessageDeletionQueue();
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, int k) {
      if (!this.isChatHidden()) {
         int l = this.getLinesPerPage();
         int i1 = this.trimmedMessages.size();
         if (i1 > 0) {
            boolean flag = this.isChatFocused();
            float f = (float)this.getScale();
            int j1 = Mth.ceil((float)this.getWidth() / f);
            int k1 = guigraphics.guiHeight();
            guigraphics.pose().pushPose();
            guigraphics.pose().scale(f, f, 1.0F);
            guigraphics.pose().translate(4.0F, 0.0F, 0.0F);
            int l1 = Mth.floor((float)(k1 - 40) / f);
            int i2 = this.getMessageEndIndexAt(this.screenToChatX((double)j), this.screenToChatY((double)k));
            double d0 = this.minecraft.options.chatOpacity().get() * (double)0.9F + (double)0.1F;
            double d1 = this.minecraft.options.textBackgroundOpacity().get();
            double d2 = this.minecraft.options.chatLineSpacing().get();
            int j2 = this.getLineHeight();
            int k2 = (int)Math.round(-8.0D * (d2 + 1.0D) + 4.0D * d2);
            int l2 = 0;

            for(int i3 = 0; i3 + this.chatScrollbarPos < this.trimmedMessages.size() && i3 < l; ++i3) {
               int j3 = i3 + this.chatScrollbarPos;
               GuiMessage.Line guimessage_line = this.trimmedMessages.get(j3);
               if (guimessage_line != null) {
                  int k3 = i - guimessage_line.addedTime();
                  if (k3 < 200 || flag) {
                     double d3 = flag ? 1.0D : getTimeFactor(k3);
                     int l3 = (int)(255.0D * d3 * d0);
                     int i4 = (int)(255.0D * d3 * d1);
                     ++l2;
                     if (l3 > 3) {
                        int j4 = 0;
                        int k4 = l1 - i3 * j2;
                        int l4 = k4 + k2;
                        guigraphics.pose().pushPose();
                        guigraphics.pose().translate(0.0F, 0.0F, 50.0F);
                        guigraphics.fill(-4, k4 - j2, 0 + j1 + 4 + 4, k4, i4 << 24);
                        GuiMessageTag guimessagetag = guimessage_line.tag();
                        if (guimessagetag != null) {
                           int i5 = guimessagetag.indicatorColor() | l3 << 24;
                           guigraphics.fill(-4, k4 - j2, -2, k4, i5);
                           if (j3 == i2 && guimessagetag.icon() != null) {
                              int j5 = this.getTagIconLeft(guimessage_line);
                              int k5 = l4 + 9;
                              this.drawTagIcon(guigraphics, j5, k5, guimessagetag.icon());
                           }
                        }

                        guigraphics.pose().translate(0.0F, 0.0F, 50.0F);
                        guigraphics.drawString(this.minecraft.font, guimessage_line.content(), 0, l4, 16777215 + (l3 << 24));
                        guigraphics.pose().popPose();
                     }
                  }
               }
            }

            long l5 = this.minecraft.getChatListener().queueSize();
            if (l5 > 0L) {
               int i6 = (int)(128.0D * d0);
               int j6 = (int)(255.0D * d1);
               guigraphics.pose().pushPose();
               guigraphics.pose().translate(0.0F, (float)l1, 50.0F);
               guigraphics.fill(-2, 0, j1 + 4, 9, j6 << 24);
               guigraphics.pose().translate(0.0F, 0.0F, 50.0F);
               guigraphics.drawString(this.minecraft.font, Component.translatable("chat.queue", l5), 0, 1, 16777215 + (i6 << 24));
               guigraphics.pose().popPose();
            }

            if (flag) {
               int k6 = this.getLineHeight();
               int l6 = i1 * k6;
               int i7 = l2 * k6;
               int j7 = this.chatScrollbarPos * i7 / i1 - l1;
               int k7 = i7 * i7 / l6;
               if (l6 != i7) {
                  int l7 = j7 > 0 ? 170 : 96;
                  int i8 = this.newMessageSinceScroll ? 13382451 : 3355562;
                  int j8 = j1 + 4;
                  guigraphics.fill(j8, -j7, j8 + 2, -j7 - k7, i8 + (l7 << 24));
                  guigraphics.fill(j8 + 2, -j7, j8 + 1, -j7 - k7, 13421772 + (l7 << 24));
               }
            }

            guigraphics.pose().popPose();
         }
      }
   }

   private void drawTagIcon(GuiGraphics guigraphics, int i, int j, GuiMessageTag.Icon guimessagetag_icon) {
      int k = j - guimessagetag_icon.height - 1;
      guimessagetag_icon.draw(guigraphics, i, k);
   }

   private int getTagIconLeft(GuiMessage.Line guimessage_line) {
      return this.minecraft.font.width(guimessage_line.content()) + 4;
   }

   private boolean isChatHidden() {
      return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
   }

   private static double getTimeFactor(int i) {
      double d0 = (double)i / 200.0D;
      d0 = 1.0D - d0;
      d0 *= 10.0D;
      d0 = Mth.clamp(d0, 0.0D, 1.0D);
      return d0 * d0;
   }

   public void clearMessages(boolean flag) {
      this.minecraft.getChatListener().clearQueue();
      this.messageDeletionQueue.clear();
      this.trimmedMessages.clear();
      this.allMessages.clear();
      if (flag) {
         this.recentChat.clear();
      }

   }

   public void addMessage(Component component) {
      this.addMessage(component, (MessageSignature)null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
   }

   public void addMessage(Component component, @Nullable MessageSignature messagesignature, @Nullable GuiMessageTag guimessagetag) {
      this.logChatMessage(component, guimessagetag);
      this.addMessage(component, messagesignature, this.minecraft.gui.getGuiTicks(), guimessagetag, false);
   }

   private void logChatMessage(Component component, @Nullable GuiMessageTag guimessagetag) {
      String s = component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
      String s1 = Optionull.map(guimessagetag, GuiMessageTag::logTag);
      if (s1 != null) {
         LOGGER.info("[{}] [CHAT] {}", s1, s);
      } else {
         LOGGER.info("[CHAT] {}", (Object)s);
      }

   }

   private void addMessage(Component component, @Nullable MessageSignature messagesignature, int i, @Nullable GuiMessageTag guimessagetag, boolean flag) {
      int j = Mth.floor((double)this.getWidth() / this.getScale());
      if (guimessagetag != null && guimessagetag.icon() != null) {
         j -= guimessagetag.icon().width + 4 + 2;
      }

      List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(component, j, this.minecraft.font);
      boolean flag1 = this.isChatFocused();

      for(int k = 0; k < list.size(); ++k) {
         FormattedCharSequence formattedcharsequence = list.get(k);
         if (flag1 && this.chatScrollbarPos > 0) {
            this.newMessageSinceScroll = true;
            this.scrollChat(1);
         }

         boolean flag2 = k == list.size() - 1;
         this.trimmedMessages.add(0, new GuiMessage.Line(i, formattedcharsequence, guimessagetag, flag2));
      }

      while(this.trimmedMessages.size() > 100) {
         this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
      }

      if (!flag) {
         this.allMessages.add(0, new GuiMessage(i, component, messagesignature, guimessagetag));

         while(this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
         }
      }

   }

   private void processMessageDeletionQueue() {
      int i = this.minecraft.gui.getGuiTicks();
      this.messageDeletionQueue.removeIf((chatcomponent_delayedmessagedeletion) -> {
         if (i >= chatcomponent_delayedmessagedeletion.deletableAfter()) {
            return this.deleteMessageOrDelay(chatcomponent_delayedmessagedeletion.signature()) == null;
         } else {
            return false;
         }
      });
   }

   public void deleteMessage(MessageSignature messagesignature) {
      ChatComponent.DelayedMessageDeletion chatcomponent_delayedmessagedeletion = this.deleteMessageOrDelay(messagesignature);
      if (chatcomponent_delayedmessagedeletion != null) {
         this.messageDeletionQueue.add(chatcomponent_delayedmessagedeletion);
      }

   }

   @Nullable
   private ChatComponent.DelayedMessageDeletion deleteMessageOrDelay(MessageSignature messagesignature) {
      int i = this.minecraft.gui.getGuiTicks();
      ListIterator<GuiMessage> listiterator = this.allMessages.listIterator();

      while(listiterator.hasNext()) {
         GuiMessage guimessage = listiterator.next();
         if (messagesignature.equals(guimessage.signature())) {
            int j = guimessage.addedTime() + 60;
            if (i >= j) {
               listiterator.set(this.createDeletedMarker(guimessage));
               this.refreshTrimmedMessage();
               return null;
            }

            return new ChatComponent.DelayedMessageDeletion(messagesignature, j);
         }
      }

      return null;
   }

   private GuiMessage createDeletedMarker(GuiMessage guimessage) {
      return new GuiMessage(guimessage.addedTime(), DELETED_CHAT_MESSAGE, (MessageSignature)null, GuiMessageTag.system());
   }

   public void rescaleChat() {
      this.resetChatScroll();
      this.refreshTrimmedMessage();
   }

   private void refreshTrimmedMessage() {
      this.trimmedMessages.clear();

      for(int i = this.allMessages.size() - 1; i >= 0; --i) {
         GuiMessage guimessage = this.allMessages.get(i);
         this.addMessage(guimessage.content(), guimessage.signature(), guimessage.addedTime(), guimessage.tag(), true);
      }

   }

   public List<String> getRecentChat() {
      return this.recentChat;
   }

   public void addRecentChat(String s) {
      if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(s)) {
         this.recentChat.add(s);
      }

   }

   public void resetChatScroll() {
      this.chatScrollbarPos = 0;
      this.newMessageSinceScroll = false;
   }

   public void scrollChat(int i) {
      this.chatScrollbarPos += i;
      int j = this.trimmedMessages.size();
      if (this.chatScrollbarPos > j - this.getLinesPerPage()) {
         this.chatScrollbarPos = j - this.getLinesPerPage();
      }

      if (this.chatScrollbarPos <= 0) {
         this.chatScrollbarPos = 0;
         this.newMessageSinceScroll = false;
      }

   }

   public boolean handleChatQueueClicked(double d0, double d1) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         ChatListener chatlistener = this.minecraft.getChatListener();
         if (chatlistener.queueSize() == 0L) {
            return false;
         } else {
            double d2 = d0 - 2.0D;
            double d3 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d1 - 40.0D;
            if (d2 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d3 < 0.0D && d3 > (double)Mth.floor(-9.0D * this.getScale())) {
               chatlistener.acceptNextDelayedMessage();
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double d0, double d1) {
      double d2 = this.screenToChatX(d0);
      double d3 = this.screenToChatY(d1);
      int i = this.getMessageLineIndexAt(d2, d3);
      if (i >= 0 && i < this.trimmedMessages.size()) {
         GuiMessage.Line guimessage_line = this.trimmedMessages.get(i);
         return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage_line.content(), Mth.floor(d2));
      } else {
         return null;
      }
   }

   @Nullable
   public GuiMessageTag getMessageTagAt(double d0, double d1) {
      double d2 = this.screenToChatX(d0);
      double d3 = this.screenToChatY(d1);
      int i = this.getMessageEndIndexAt(d2, d3);
      if (i >= 0 && i < this.trimmedMessages.size()) {
         GuiMessage.Line guimessage_line = this.trimmedMessages.get(i);
         GuiMessageTag guimessagetag = guimessage_line.tag();
         if (guimessagetag != null && this.hasSelectedMessageTag(d2, guimessage_line, guimessagetag)) {
            return guimessagetag;
         }
      }

      return null;
   }

   private boolean hasSelectedMessageTag(double d0, GuiMessage.Line guimessage_line, GuiMessageTag guimessagetag) {
      if (d0 < 0.0D) {
         return true;
      } else {
         GuiMessageTag.Icon guimessagetag_icon = guimessagetag.icon();
         if (guimessagetag_icon == null) {
            return false;
         } else {
            int i = this.getTagIconLeft(guimessage_line);
            int j = i + guimessagetag_icon.width;
            return d0 >= (double)i && d0 <= (double)j;
         }
      }
   }

   private double screenToChatX(double d0) {
      return d0 / this.getScale() - 4.0D;
   }

   private double screenToChatY(double d0) {
      double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d0 - 40.0D;
      return d1 / (this.getScale() * (double)this.getLineHeight());
   }

   private int getMessageEndIndexAt(double d0, double d1) {
      int i = this.getMessageLineIndexAt(d0, d1);
      if (i == -1) {
         return -1;
      } else {
         while(i >= 0) {
            if (this.trimmedMessages.get(i).endOfEntry()) {
               return i;
            }

            --i;
         }

         return i;
      }
   }

   private int getMessageLineIndexAt(double d0, double d1) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         if (!(d0 < -4.0D) && !(d0 > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
            int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
            if (d1 >= 0.0D && d1 < (double)i) {
               int j = Mth.floor(d1 + (double)this.chatScrollbarPos);
               if (j >= 0 && j < this.trimmedMessages.size()) {
                  return j;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean isChatFocused() {
      return this.minecraft.screen instanceof ChatScreen;
   }

   public int getWidth() {
      return getWidth(this.minecraft.options.chatWidth().get());
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
   }

   public double getScale() {
      return this.minecraft.options.chatScale().get();
   }

   public static int getWidth(double d0) {
      int i = 320;
      int j = 40;
      return Mth.floor(d0 * 280.0D + 40.0D);
   }

   public static int getHeight(double d0) {
      int i = 180;
      int j = 20;
      return Mth.floor(d0 * 160.0D + 20.0D);
   }

   public static double defaultUnfocusedPct() {
      int i = 180;
      int j = 20;
      return 70.0D / (double)(getHeight(1.0D) - 20);
   }

   public int getLinesPerPage() {
      return this.getHeight() / this.getLineHeight();
   }

   private int getLineHeight() {
      return (int)(9.0D * (this.minecraft.options.chatLineSpacing().get() + 1.0D));
   }

   static record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {
   }
}
