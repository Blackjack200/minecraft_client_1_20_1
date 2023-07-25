package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ReportReasonSelectionScreen extends Screen {
   private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
   private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
   private static final Component READ_INFO_LABEL = Component.translatable("gui.chatReport.read_info");
   private static final int FOOTER_HEIGHT = 95;
   private static final int BUTTON_WIDTH = 150;
   private static final int BUTTON_HEIGHT = 20;
   private static final int CONTENT_WIDTH = 320;
   private static final int PADDING = 4;
   @Nullable
   private final Screen lastScreen;
   @Nullable
   private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
   @Nullable
   ReportReason currentlySelectedReason;
   private final Consumer<ReportReason> onSelectedReason;

   public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportreason, Consumer<ReportReason> consumer) {
      super(REASON_TITLE);
      this.lastScreen = screen;
      this.currentlySelectedReason = reportreason;
      this.onSelectedReason = consumer;
   }

   protected void init() {
      this.reasonSelectionList = new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft);
      this.reasonSelectionList.setRenderBackground(false);
      this.addWidget(this.reasonSelectionList);
      ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen_reasonselectionlist_entry = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
      this.reasonSelectionList.setSelected(reportreasonselectionscreen_reasonselectionlist_entry);
      int i = this.width / 2 - 150 - 5;
      this.addRenderableWidget(Button.builder(READ_INFO_LABEL, (button1) -> this.minecraft.setScreen(new ConfirmLinkScreen((flag) -> {
            if (flag) {
               Util.getPlatform().openUri("https://aka.ms/aboutjavareporting");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/aboutjavareporting", true))).bounds(i, this.buttonTop(), 150, 20).build());
      int j = this.width / 2 + 5;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen_reasonselectionlist_entry1 = this.reasonSelectionList.getSelected();
         if (reportreasonselectionscreen_reasonselectionlist_entry1 != null) {
            this.onSelectedReason.accept(reportreasonselectionscreen_reasonselectionlist_entry1.getReason());
         }

         this.minecraft.setScreen(this.lastScreen);
      }).bounds(j, this.buttonTop(), 150, 20).build());
      super.init();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.reasonSelectionList.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      super.render(guigraphics, i, j, f);
      guigraphics.fill(this.contentLeft(), this.descriptionTop(), this.contentRight(), this.descriptionBottom(), 2130706432);
      guigraphics.drawString(this.font, REASON_DESCRIPTION, this.contentLeft() + 4, this.descriptionTop() + 4, -8421505);
      ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen_reasonselectionlist_entry = this.reasonSelectionList.getSelected();
      if (reportreasonselectionscreen_reasonselectionlist_entry != null) {
         int k = this.contentLeft() + 4 + 16;
         int l = this.contentRight() - 4;
         int i1 = this.descriptionTop() + 4 + 9 + 2;
         int j1 = this.descriptionBottom() - 4;
         int k1 = l - k;
         int l1 = j1 - i1;
         int i2 = this.font.wordWrapHeight(reportreasonselectionscreen_reasonselectionlist_entry.reason.description(), k1);
         guigraphics.drawWordWrap(this.font, reportreasonselectionscreen_reasonselectionlist_entry.reason.description(), k, i1 + (l1 - i2) / 2, k1, -1);
      }

   }

   private int buttonTop() {
      return this.height - 20 - 4;
   }

   private int contentLeft() {
      return (this.width - 320) / 2;
   }

   private int contentRight() {
      return (this.width + 320) / 2;
   }

   private int descriptionTop() {
      return this.height - 95 + 4;
   }

   private int descriptionBottom() {
      return this.buttonTop() - 4;
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
      public ReasonSelectionList(Minecraft minecraft) {
         super(minecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 95, 18);

         for(ReportReason reportreason : ReportReason.values()) {
            this.addEntry(new ReportReasonSelectionScreen.ReasonSelectionList.Entry(reportreason));
         }

      }

      @Nullable
      public ReportReasonSelectionScreen.ReasonSelectionList.Entry findEntry(ReportReason reportreason) {
         return this.children().stream().filter((reportreasonselectionscreen_reasonselectionlist_entry) -> reportreasonselectionscreen_reasonselectionlist_entry.reason == reportreason).findFirst().orElse((ReportReasonSelectionScreen.ReasonSelectionList.Entry)null);
      }

      public int getRowWidth() {
         return 320;
      }

      protected int getScrollbarPosition() {
         return this.getRowRight() - 2;
      }

      public void setSelected(@Nullable ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen_reasonselectionlist_entry) {
         super.setSelected(reportreasonselectionscreen_reasonselectionlist_entry);
         ReportReasonSelectionScreen.this.currentlySelectedReason = reportreasonselectionscreen_reasonselectionlist_entry != null ? reportreasonselectionscreen_reasonselectionlist_entry.getReason() : null;
      }

      public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
         final ReportReason reason;

         public Entry(ReportReason reportreason) {
            this.reason = reportreason;
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            int l1 = k + 1;
            int i2 = j + (i1 - 9) / 2 + 1;
            guigraphics.drawString(ReportReasonSelectionScreen.this.font, this.reason.title(), l1, i2, -1);
         }

         public Component getNarration() {
            return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               ReasonSelectionList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         public ReportReason getReason() {
            return this.reason;
         }
      }
   }
}
