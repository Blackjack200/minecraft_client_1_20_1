package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
   private static final ResourceLocation REPORT_BUTTON_LOCATION = new ResourceLocation("textures/gui/report_button.png");
   private static final int TOOLTIP_DELAY = 10;
   private final Minecraft minecraft;
   private final List<AbstractWidget> children;
   private final UUID id;
   private final String playerName;
   private final Supplier<ResourceLocation> skinGetter;
   private boolean isRemoved;
   private boolean hasRecentMessages;
   private final boolean reportingEnabled;
   private final boolean playerReportable;
   private final boolean hasDraftReport;
   @Nullable
   private Button hideButton;
   @Nullable
   private Button showButton;
   @Nullable
   private Button reportButton;
   private float tooltipHoverTime;
   private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
   private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
   private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
   private static final Component NOT_REPORTABLE_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.not_reportable");
   private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
   private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
   private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
   private static final int SKIN_SIZE = 24;
   private static final int PADDING = 4;
   private static final int CHAT_TOGGLE_ICON_SIZE = 20;
   private static final int CHAT_TOGGLE_ICON_X = 0;
   private static final int CHAT_TOGGLE_ICON_Y = 38;
   public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
   public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
   public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
   public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
   public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

   public PlayerEntry(Minecraft minecraft, SocialInteractionsScreen socialinteractionsscreen, UUID uuid, String s, Supplier<ResourceLocation> supplier, boolean flag) {
      this.minecraft = minecraft;
      this.id = uuid;
      this.playerName = s;
      this.skinGetter = supplier;
      ReportingContext reportingcontext = minecraft.getReportingContext();
      this.reportingEnabled = reportingcontext.sender().isEnabled();
      this.playerReportable = flag;
      this.hasDraftReport = reportingcontext.hasDraftReportFor(uuid);
      Component component = Component.translatable("gui.socialInteractions.narration.hide", s);
      Component component1 = Component.translatable("gui.socialInteractions.narration.show", s);
      PlayerSocialManager playersocialmanager = minecraft.getPlayerSocialManager();
      boolean flag1 = minecraft.getChatStatus().isChatAllowed(minecraft.isLocalServer());
      boolean flag2 = !minecraft.player.getUUID().equals(uuid);
      if (flag2 && flag1 && !playersocialmanager.isBlocked(uuid)) {
         this.reportButton = new ImageButton(0, 0, 20, 20, 0, 0, 20, REPORT_BUTTON_LOCATION, 64, 64, (button2) -> reportingcontext.draftReportHandled(minecraft, socialinteractionsscreen, () -> minecraft.setScreen(new ChatReportScreen(socialinteractionsscreen, reportingcontext, uuid)), false), Component.translatable("gui.socialInteractions.report")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.reportButton.setTooltip(this.createReportButtonTooltip());
         this.reportButton.setTooltipDelay(10);
         this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (button1) -> {
            playersocialmanager.hidePlayer(uuid);
            this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", s));
         }, Component.translatable("gui.socialInteractions.hide")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, component));
         this.hideButton.setTooltipDelay(10);
         this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (button) -> {
            playersocialmanager.showPlayer(uuid);
            this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", s));
         }, Component.translatable("gui.socialInteractions.show")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, component1));
         this.showButton.setTooltipDelay(10);
         this.reportButton.active = false;
         this.children = new ArrayList<>();
         this.children.add(this.hideButton);
         this.children.add(this.reportButton);
         this.updateHideAndShowButton(playersocialmanager.isHidden(this.id));
      } else {
         this.children = ImmutableList.of();
      }

   }

   private Tooltip createReportButtonTooltip() {
      if (!this.playerReportable) {
         return Tooltip.create(NOT_REPORTABLE_TOOLTIP);
      } else if (!this.reportingEnabled) {
         return Tooltip.create(REPORT_DISABLED_TOOLTIP);
      } else {
         return !this.hasRecentMessages ? Tooltip.create(Component.translatable("gui.socialInteractions.tooltip.report.no_messages", this.playerName)) : Tooltip.create(REPORT_PLAYER_TOOLTIP, Component.translatable("gui.socialInteractions.narration.report", this.playerName));
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
      int l1 = k + 4;
      int i2 = j + (i1 - 24) / 2;
      int j2 = l1 + 24 + 4;
      Component component = this.getStatusComponent();
      int k2;
      if (component == CommonComponents.EMPTY) {
         guigraphics.fill(k, j, k + l, j + i1, BG_FILL);
         k2 = j + (i1 - 9) / 2;
      } else {
         guigraphics.fill(k, j, k + l, j + i1, BG_FILL_REMOVED);
         k2 = j + (i1 - (9 + 9)) / 2;
         guigraphics.drawString(this.minecraft.font, component, j2, k2 + 12, PLAYER_STATUS_COLOR, false);
      }

      PlayerFaceRenderer.draw(guigraphics, this.skinGetter.get(), l1, i2, 24);
      guigraphics.drawString(this.minecraft.font, this.playerName, j2, k2, PLAYERNAME_COLOR, false);
      if (this.isRemoved) {
         guigraphics.fill(l1, i2, l1 + 24, i2 + 24, SKIN_SHADE);
      }

      if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
         float f1 = this.tooltipHoverTime;
         this.hideButton.setX(k + (l - this.hideButton.getWidth() - 4) - 20 - 4);
         this.hideButton.setY(j + (i1 - this.hideButton.getHeight()) / 2);
         this.hideButton.render(guigraphics, j1, k1, f);
         this.showButton.setX(k + (l - this.showButton.getWidth() - 4) - 20 - 4);
         this.showButton.setY(j + (i1 - this.showButton.getHeight()) / 2);
         this.showButton.render(guigraphics, j1, k1, f);
         this.reportButton.setX(k + (l - this.showButton.getWidth() - 4));
         this.reportButton.setY(j + (i1 - this.showButton.getHeight()) / 2);
         this.reportButton.render(guigraphics, j1, k1, f);
         if (f1 == this.tooltipHoverTime) {
            this.tooltipHoverTime = 0.0F;
         }
      }

      if (this.hasDraftReport && this.reportButton != null) {
         guigraphics.blit(AbstractWidget.WIDGETS_LOCATION, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 182.0F, 24.0F, 15, 15, 256, 256);
      }

   }

   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   public List<? extends NarratableEntry> narratables() {
      return this.children;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public UUID getPlayerId() {
      return this.id;
   }

   public void setRemoved(boolean flag) {
      this.isRemoved = flag;
   }

   public boolean isRemoved() {
      return this.isRemoved;
   }

   public void setHasRecentMessages(boolean flag) {
      this.hasRecentMessages = flag;
      if (this.reportButton != null) {
         this.reportButton.active = this.reportingEnabled && this.playerReportable && flag;
         this.reportButton.setTooltip(this.createReportButtonTooltip());
      }

   }

   public boolean hasRecentMessages() {
      return this.hasRecentMessages;
   }

   private void onHiddenOrShown(boolean flag, Component component) {
      this.updateHideAndShowButton(flag);
      this.minecraft.gui.getChat().addMessage(component);
      this.minecraft.getNarrator().sayNow(component);
   }

   private void updateHideAndShowButton(boolean flag) {
      this.showButton.visible = flag;
      this.hideButton.visible = !flag;
      this.children.set(0, flag ? this.showButton : this.hideButton);
   }

   MutableComponent getEntryNarationMessage(MutableComponent mutablecomponent) {
      Component component = this.getStatusComponent();
      return component == CommonComponents.EMPTY ? Component.literal(this.playerName).append(", ").append(mutablecomponent) : Component.literal(this.playerName).append(", ").append(component).append(", ").append(mutablecomponent);
   }

   private Component getStatusComponent() {
      boolean flag = this.minecraft.getPlayerSocialManager().isHidden(this.id);
      boolean flag1 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
      if (flag1 && this.isRemoved) {
         return BLOCKED_OFFLINE;
      } else if (flag && this.isRemoved) {
         return HIDDEN_OFFLINE;
      } else if (flag1) {
         return BLOCKED;
      } else if (flag) {
         return HIDDEN;
      } else {
         return this.isRemoved ? OFFLINE : CommonComponents.EMPTY;
      }
   }
}
