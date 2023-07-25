package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChatSelectionScreen extends Screen {
   private static final Component TITLE = Component.translatable("gui.chatSelection.title");
   private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
   @Nullable
   private final Screen lastScreen;
   private final ReportingContext reportingContext;
   private Button confirmSelectedButton;
   private MultiLineLabel contextInfoLabel;
   @Nullable
   private ChatSelectionScreen.ChatSelectionList chatSelectionList;
   final ChatReportBuilder report;
   private final Consumer<ChatReportBuilder> onSelected;
   private ChatSelectionLogFiller chatLogFiller;

   public ChatSelectionScreen(@Nullable Screen screen, ReportingContext reportingcontext, ChatReportBuilder chatreportbuilder, Consumer<ChatReportBuilder> consumer) {
      super(TITLE);
      this.lastScreen = screen;
      this.reportingContext = reportingcontext;
      this.report = chatreportbuilder.copy();
      this.onSelected = consumer;
   }

   protected void init() {
      this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
      this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
      this.chatSelectionList = new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9);
      this.chatSelectionList.setRenderBackground(false);
      this.addWidget(this.chatSelectionList);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button1) -> this.onClose()).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build());
      this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onSelected.accept(this.report);
         this.onClose();
      }).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
      this.updateConfirmSelectedButton();
      this.extendLog();
      this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
   }

   private boolean canReport(LoggedChatMessage loggedchatmessage) {
      return loggedchatmessage.canReport(this.report.reportedProfileId());
   }

   private void extendLog() {
      int i = this.chatSelectionList.getMaxVisibleEntries();
      this.chatLogFiller.fillNextPage(i, this.chatSelectionList);
   }

   void onReachedScrollTop() {
      this.extendLog();
   }

   void updateConfirmSelectedButton() {
      this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.chatSelectionList.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      AbuseReportLimits abusereportlimits = this.reportingContext.sender().reportLimits();
      int k = this.report.reportedMessages().size();
      int l = abusereportlimits.maxReportedMessageCount();
      Component component = Component.translatable("gui.chatSelection.selected", k, l);
      guigraphics.drawCenteredString(this.font, component, this.width / 2, 16 + 9 * 3 / 2, 10526880);
      this.contextInfoLabel.renderCentered(guigraphics, this.width / 2, this.chatSelectionList.getFooterTop());
      super.render(guigraphics, i, j, f);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
   }

   public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output {
      @Nullable
      private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

      public ChatSelectionList(Minecraft minecraft, int i) {
         super(minecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - i, 16);
      }

      public void setScrollAmount(double d0) {
         double d1 = this.getScrollAmount();
         super.setScrollAmount(d0);
         if ((float)this.getMaxScroll() > 1.0E-5F && d0 <= (double)1.0E-5F && !Mth.equal(d0, d1)) {
            ChatSelectionScreen.this.onReachedScrollTop();
         }

      }

      public void acceptMessage(int i, LoggedChatMessage.Player loggedchatmessage_player) {
         boolean flag = loggedchatmessage_player.canReport(ChatSelectionScreen.this.report.reportedProfileId());
         ChatTrustLevel chattrustlevel = loggedchatmessage_player.trustLevel();
         GuiMessageTag guimessagetag = chattrustlevel.createTag(loggedchatmessage_player.message());
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry = new ChatSelectionScreen.ChatSelectionList.MessageEntry(i, loggedchatmessage_player.toContentComponent(), loggedchatmessage_player.toNarrationComponent(), guimessagetag, flag, true);
         this.addEntryToTop(chatselectionscreen_chatselectionlist_entry);
         this.updateHeading(loggedchatmessage_player, flag);
      }

      private void updateHeading(LoggedChatMessage.Player loggedchatmessage_player, boolean flag) {
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry = new ChatSelectionScreen.ChatSelectionList.MessageHeadingEntry(loggedchatmessage_player.profile(), loggedchatmessage_player.toHeadingComponent(), flag);
         this.addEntryToTop(chatselectionscreen_chatselectionlist_entry);
         ChatSelectionScreen.ChatSelectionList.Heading chatselectionscreen_chatselectionlist_heading = new ChatSelectionScreen.ChatSelectionList.Heading(loggedchatmessage_player.profileId(), chatselectionscreen_chatselectionlist_entry);
         if (this.previousHeading != null && this.previousHeading.canCombine(chatselectionscreen_chatselectionlist_heading)) {
            this.removeEntryFromTop(this.previousHeading.entry());
         }

         this.previousHeading = chatselectionscreen_chatselectionlist_heading;
      }

      public void acceptDivider(Component component) {
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.DividerEntry(component));
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
         this.previousHeading = null;
      }

      protected int getScrollbarPosition() {
         return (this.width + this.getRowWidth()) / 2;
      }

      public int getRowWidth() {
         return Math.min(350, this.width - 50);
      }

      public int getMaxVisibleEntries() {
         return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
      }

      protected void renderItem(GuiGraphics guigraphics, int i, int j, float f, int k, int l, int i1, int j1, int k1) {
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry = this.getEntry(k);
         if (this.shouldHighlightEntry(chatselectionscreen_chatselectionlist_entry)) {
            boolean flag = this.getSelected() == chatselectionscreen_chatselectionlist_entry;
            int l1 = this.isFocused() && flag ? -1 : -8355712;
            this.renderSelection(guigraphics, i1, j1, k1, l1, -16777216);
         }

         chatselectionscreen_chatselectionlist_entry.render(guigraphics, k, i1, l, j1, k1, i, j, this.getHovered() == chatselectionscreen_chatselectionlist_entry, f);
      }

      private boolean shouldHighlightEntry(ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry) {
         if (chatselectionscreen_chatselectionlist_entry.canSelect()) {
            boolean flag = this.getSelected() == chatselectionscreen_chatselectionlist_entry;
            boolean flag1 = this.getSelected() == null;
            boolean flag2 = this.getHovered() == chatselectionscreen_chatselectionlist_entry;
            return flag || flag1 && flag2 && chatselectionscreen_chatselectionlist_entry.canReport();
         } else {
            return false;
         }
      }

      @Nullable
      protected ChatSelectionScreen.ChatSelectionList.Entry nextEntry(ScreenDirection screendirection) {
         return this.nextEntry(screendirection, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
      }

      public void setSelected(@Nullable ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry) {
         super.setSelected(chatselectionscreen_chatselectionlist_entry);
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry1 = this.nextEntry(ScreenDirection.UP);
         if (chatselectionscreen_chatselectionlist_entry1 == null) {
            ChatSelectionScreen.this.onReachedScrollTop();
         }

      }

      public boolean keyPressed(int i, int j, int k) {
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen_chatselectionlist_entry = this.getSelected();
         return chatselectionscreen_chatselectionlist_entry != null && chatselectionscreen_chatselectionlist_entry.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
      }

      public int getFooterTop() {
         return this.y1 + 9;
      }

      public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final int COLOR = -6250336;
         private final Component text;

         public DividerEntry(Component component) {
            this.text = component;
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            int l1 = j + i1 / 2;
            int i2 = k + l - 8;
            int j2 = ChatSelectionScreen.this.font.width(this.text);
            int k2 = (k + i2 - j2) / 2;
            int l2 = l1 - 9 / 2;
            guigraphics.drawString(ChatSelectionScreen.this.font, this.text, k2, l2, -6250336);
         }

         public Component getNarration() {
            return this.text;
         }
      }

      public abstract class Entry extends ObjectSelectionList.Entry<ChatSelectionScreen.ChatSelectionList.Entry> {
         public Component getNarration() {
            return CommonComponents.EMPTY;
         }

         public boolean isSelected() {
            return false;
         }

         public boolean canSelect() {
            return false;
         }

         public boolean canReport() {
            return this.canSelect();
         }
      }

      static record Heading(UUID sender, ChatSelectionScreen.ChatSelectionList.Entry entry) {
         public boolean canCombine(ChatSelectionScreen.ChatSelectionList.Heading chatselectionscreen_chatselectionlist_heading) {
            return chatselectionscreen_chatselectionlist_heading.sender.equals(this.sender);
         }
      }

      public class MessageEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final ResourceLocation CHECKMARK_TEXTURE = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
         private static final int CHECKMARK_WIDTH = 9;
         private static final int CHECKMARK_HEIGHT = 8;
         private static final int INDENT_AMOUNT = 11;
         private static final int TAG_MARGIN_LEFT = 4;
         private final int chatId;
         private final FormattedText text;
         private final Component narration;
         @Nullable
         private final List<FormattedCharSequence> hoverText;
         @Nullable
         private final GuiMessageTag.Icon tagIcon;
         @Nullable
         private final List<FormattedCharSequence> tagHoverText;
         private final boolean canReport;
         private final boolean playerMessage;

         public MessageEntry(int i, Component component, Component component1, @Nullable GuiMessageTag guimessagetag, boolean flag, boolean flag1) {
            this.chatId = i;
            this.tagIcon = Optionull.map(guimessagetag, GuiMessageTag::icon);
            this.tagHoverText = guimessagetag != null && guimessagetag.text() != null ? ChatSelectionScreen.this.font.split(guimessagetag.text(), ChatSelectionList.this.getRowWidth()) : null;
            this.canReport = flag;
            this.playerMessage = flag1;
            FormattedText formattedtext = ChatSelectionScreen.this.font.substrByWidth(component, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
            if (component != formattedtext) {
               this.text = FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS);
               this.hoverText = ChatSelectionScreen.this.font.split(component, ChatSelectionList.this.getRowWidth());
            } else {
               this.text = component;
               this.hoverText = null;
            }

            this.narration = component1;
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            if (this.isSelected() && this.canReport) {
               this.renderSelectedCheckmark(guigraphics, j, k, i1);
            }

            int l1 = k + this.getTextIndent();
            int i2 = j + 1 + (i1 - 9) / 2;
            guigraphics.drawString(ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), l1, i2, this.canReport ? -1 : -1593835521);
            if (this.hoverText != null && flag) {
               ChatSelectionScreen.this.setTooltipForNextRenderPass(this.hoverText);
            }

            int j2 = ChatSelectionScreen.this.font.width(this.text);
            this.renderTag(guigraphics, l1 + j2 + 4, j, i1, j1, k1);
         }

         private void renderTag(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
            if (this.tagIcon != null) {
               int j1 = j + (k - this.tagIcon.height) / 2;
               this.tagIcon.draw(guigraphics, i, j1);
               if (this.tagHoverText != null && l >= i && l <= i + this.tagIcon.width && i1 >= j1 && i1 <= j1 + this.tagIcon.height) {
                  ChatSelectionScreen.this.setTooltipForNextRenderPass(this.tagHoverText);
               }
            }

         }

         private void renderSelectedCheckmark(GuiGraphics guigraphics, int i, int j, int k) {
            int i1 = i + (k - 8) / 2;
            RenderSystem.enableBlend();
            guigraphics.blit(CHECKMARK_TEXTURE, j, i1, 0.0F, 0.0F, 9, 8, 9, 8);
            RenderSystem.disableBlend();
         }

         private int getMaximumTextWidth() {
            int i = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
            return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - i;
         }

         private int getTextIndent() {
            return this.playerMessage ? 11 : 0;
         }

         public Component getNarration() {
            return (Component)(this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration);
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               ChatSelectionList.this.setSelected((ChatSelectionScreen.ChatSelectionList.Entry)null);
               return this.toggleReport();
            } else {
               return false;
            }
         }

         public boolean keyPressed(int i, int j, int k) {
            return CommonInputs.selected(i) ? this.toggleReport() : false;
         }

         public boolean isSelected() {
            return ChatSelectionScreen.this.report.isReported(this.chatId);
         }

         public boolean canSelect() {
            return true;
         }

         public boolean canReport() {
            return this.canReport;
         }

         private boolean toggleReport() {
            if (this.canReport) {
               ChatSelectionScreen.this.report.toggleReported(this.chatId);
               ChatSelectionScreen.this.updateConfirmSelectedButton();
               return true;
            } else {
               return false;
            }
         }
      }

      public class MessageHeadingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final int FACE_SIZE = 12;
         private final Component heading;
         private final ResourceLocation skin;
         private final boolean canReport;

         public MessageHeadingEntry(GameProfile gameprofile, Component component, boolean flag) {
            this.heading = component;
            this.canReport = flag;
            this.skin = ChatSelectionList.this.minecraft.getSkinManager().getInsecureSkinLocation(gameprofile);
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            int l1 = k - 12 - 4;
            int i2 = j + (i1 - 12) / 2;
            PlayerFaceRenderer.draw(guigraphics, this.skin, l1, i2, 12);
            int j2 = j + 1 + (i1 - 9) / 2;
            guigraphics.drawString(ChatSelectionScreen.this.font, this.heading, k, j2, this.canReport ? -1 : -1593835521);
         }
      }

      public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         }
      }
   }
}
