package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.task.CloseServerTask;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchMinigameTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsConfigureWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   private static final Component WORLD_LIST_TITLE = Component.translatable("mco.configure.worlds.title");
   private static final Component TITLE = Component.translatable("mco.configure.world.title");
   private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
   private static final Component SERVER_EXPIRING_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRING_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
   private static final int DEFAULT_BUTTON_WIDTH = 80;
   private static final int DEFAULT_BUTTON_OFFSET = 5;
   @Nullable
   private Component toolTip;
   private final RealmsMainScreen lastScreen;
   @Nullable
   private RealmsServer serverData;
   private final long serverId;
   private int leftX;
   private int rightX;
   private Button playersButton;
   private Button settingsButton;
   private Button subscriptionButton;
   private Button optionsButton;
   private Button backupButton;
   private Button resetWorldButton;
   private Button switchMinigameButton;
   private boolean stateChanged;
   private int animTick;
   private int clicks;
   private final List<RealmsWorldSlotButton> slotButtonList = Lists.newArrayList();

   public RealmsConfigureWorldScreen(RealmsMainScreen realmsmainscreen, long i) {
      super(TITLE);
      this.lastScreen = realmsmainscreen;
      this.serverId = i;
   }

   public void init() {
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      }

      this.leftX = this.width / 2 - 187;
      this.rightX = this.width / 2 + 190;
      this.playersButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.players"), (button7) -> this.minecraft.setScreen(new RealmsPlayerScreen(this, this.serverData))).bounds(this.centerButton(0, 3), row(0), 100, 20).build());
      this.settingsButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.settings"), (button6) -> this.minecraft.setScreen(new RealmsSettingsScreen(this, this.serverData.clone()))).bounds(this.centerButton(1, 3), row(0), 100, 20).build());
      this.subscriptionButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.subscription"), (button5) -> this.minecraft.setScreen(new RealmsSubscriptionInfoScreen(this, this.serverData.clone(), this.lastScreen))).bounds(this.centerButton(2, 3), row(0), 100, 20).build());
      this.slotButtonList.clear();

      for(int i = 1; i < 5; ++i) {
         this.slotButtonList.add(this.addSlotButton(i));
      }

      this.switchMinigameButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.switchminigame"), (button4) -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME))).bounds(this.leftButton(0), row(13) - 5, 100, 20).build());
      this.optionsButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.options"), (button3) -> this.minecraft.setScreen(new RealmsSlotOptionsScreen(this, this.serverData.slots.get(this.serverData.activeSlot).clone(), this.serverData.worldType, this.serverData.activeSlot))).bounds(this.leftButton(0), row(13) - 5, 90, 20).build());
      this.backupButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.backup"), (button2) -> this.minecraft.setScreen(new RealmsBackupScreen(this, this.serverData.clone(), this.serverData.activeSlot))).bounds(this.leftButton(1), row(13) - 5, 90, 20).build());
      this.resetWorldButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.resetworld"), (button1) -> this.minecraft.setScreen(new RealmsResetWorldScreen(this, this.serverData.clone(), () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())), () -> this.minecraft.setScreen(this.getNewScreen())))).bounds(this.leftButton(2), row(13) - 5, 90, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.backButtonClicked()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build());
      this.backupButton.active = true;
      if (this.serverData == null) {
         this.hideMinigameButtons();
         this.hideRegularButtons();
         this.playersButton.active = false;
         this.settingsButton.active = false;
         this.subscriptionButton.active = false;
      } else {
         this.disableButtons();
         if (this.isMinigame()) {
            this.hideRegularButtons();
         } else {
            this.hideMinigameButtons();
         }
      }

   }

   private RealmsWorldSlotButton addSlotButton(int i) {
      int j = this.frame(i);
      int k = row(5) + 5;
      RealmsWorldSlotButton realmsworldslotbutton = new RealmsWorldSlotButton(j, k, 80, 80, () -> this.serverData, (component) -> this.toolTip = component, i, (button) -> {
         RealmsWorldSlotButton.State realmsworldslotbutton_state = ((RealmsWorldSlotButton)button).getState();
         if (realmsworldslotbutton_state != null) {
            switch (realmsworldslotbutton_state.action) {
               case NOTHING:
                  break;
               case JOIN:
                  this.joinRealm(this.serverData);
                  break;
               case SWITCH_SLOT:
                  if (realmsworldslotbutton_state.minigame) {
                     this.switchToMinigame();
                  } else if (realmsworldslotbutton_state.empty) {
                     this.switchToEmptySlot(i, this.serverData);
                  } else {
                     this.switchToFullSlot(i, this.serverData);
                  }
                  break;
               default:
                  throw new IllegalStateException("Unknown action " + realmsworldslotbutton_state.action);
            }
         }

      });
      return this.addRenderableWidget(realmsworldslotbutton);
   }

   private int leftButton(int i) {
      return this.leftX + i * 95;
   }

   private int centerButton(int i, int j) {
      return this.width / 2 - (j * 105 - 5) / 2 + i * 105;
   }

   public void tick() {
      super.tick();
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

      this.slotButtonList.forEach(RealmsWorldSlotButton::tick);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.toolTip = null;
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, WORLD_LIST_TITLE, this.width / 2, row(4), 16777215);
      super.render(guigraphics, i, j, f);
      if (this.serverData == null) {
         guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      } else {
         String s = this.serverData.getName();
         int k = this.font.width(s);
         int l = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
         int i1 = this.font.width(this.title);
         guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
         guigraphics.drawCenteredString(this.font, s, this.width / 2, 24, l);
         int j1 = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + k / 2 + i1 / 2 + 10);
         this.drawServerStatus(guigraphics, j1, 7, i, j);
         if (this.isMinigame()) {
            guigraphics.drawString(this.font, Component.translatable("mco.configure.world.minigame", this.serverData.getMinigameName()), this.leftX + 80 + 20 + 10, row(13), 16777215, false);
         }

         if (this.toolTip != null) {
            this.renderMousehoverTooltip(guigraphics, this.toolTip, i, j);
         }

      }
   }

   private int frame(int i) {
      return this.leftX + (i - 1) * 98;
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   private void backButtonClicked() {
      if (this.stateChanged) {
         this.lastScreen.resetScreen();
      }

      this.minecraft.setScreen(this.lastScreen);
   }

   private void fetchServerData(long i) {
      (new Thread(() -> {
         RealmsClient realmsclient = RealmsClient.create();

         try {
            RealmsServer realmsserver = realmsclient.getOwnWorld(i);
            this.minecraft.execute(() -> {
               this.serverData = realmsserver;
               this.disableButtons();
               if (this.isMinigame()) {
                  this.show(this.switchMinigameButton);
               } else {
                  this.show(this.optionsButton);
                  this.show(this.backupButton);
                  this.show(this.resetWorldButton);
               }

            });
         } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get own world");
            this.minecraft.execute(() -> this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen)));
         }

      })).start();
   }

   private void disableButtons() {
      this.playersButton.active = !this.serverData.expired;
      this.settingsButton.active = !this.serverData.expired;
      this.subscriptionButton.active = true;
      this.switchMinigameButton.active = !this.serverData.expired;
      this.optionsButton.active = !this.serverData.expired;
      this.resetWorldButton.active = !this.serverData.expired;
   }

   private void joinRealm(RealmsServer realmsserver) {
      if (this.serverData.state == RealmsServer.State.OPEN) {
         this.lastScreen.play(realmsserver, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      } else {
         this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
      }

   }

   private void switchToMinigame() {
      RealmsSelectWorldTemplateScreen realmsselectworldtemplatescreen = new RealmsSelectWorldTemplateScreen(Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME);
      realmsselectworldtemplatescreen.setWarning(Component.translatable("mco.minigame.world.info.line1"), Component.translatable("mco.minigame.world.info.line2"));
      this.minecraft.setScreen(realmsselectworldtemplatescreen);
   }

   private void switchToFullSlot(int i, RealmsServer realmsserver) {
      Component component = Component.translatable("mco.configure.world.slot.switch.question.line1");
      Component component1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
      this.minecraft.setScreen(new RealmsLongConfirmationScreen((flag) -> {
         if (flag) {
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(realmsserver.id, i, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())))));
         } else {
            this.minecraft.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
   }

   private void switchToEmptySlot(int i, RealmsServer realmsserver) {
      Component component = Component.translatable("mco.configure.world.slot.switch.question.line1");
      Component component1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
      this.minecraft.setScreen(new RealmsLongConfirmationScreen((flag) -> {
         if (flag) {
            RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this, realmsserver, Component.translatable("mco.configure.world.switch.slot"), Component.translatable("mco.configure.world.switch.slot.subtitle"), 10526880, CommonComponents.GUI_CANCEL, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())), () -> this.minecraft.setScreen(this.getNewScreen()));
            realmsresetworldscreen.setSlot(i);
            realmsresetworldscreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
            this.minecraft.setScreen(realmsresetworldscreen);
         } else {
            this.minecraft.setScreen(this);
         }

      }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
   }

   protected void renderMousehoverTooltip(GuiGraphics guigraphics, @Nullable Component component, int i, int j) {
      int k = i + 12;
      int l = j - 12;
      int i1 = this.font.width(component);
      if (k + i1 + 3 > this.rightX) {
         k = k - i1 - 20;
      }

      guigraphics.fillGradient(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
      guigraphics.drawString(this.font, component, k, l, 16777215);
   }

   private void drawServerStatus(GuiGraphics guigraphics, int i, int j, int k, int l) {
      if (this.serverData.expired) {
         this.drawExpired(guigraphics, i, j, k, l);
      } else if (this.serverData.state == RealmsServer.State.CLOSED) {
         this.drawClose(guigraphics, i, j, k, l);
      } else if (this.serverData.state == RealmsServer.State.OPEN) {
         if (this.serverData.daysLeft < 7) {
            this.drawExpiring(guigraphics, i, j, k, l, this.serverData.daysLeft);
         } else {
            this.drawOpen(guigraphics, i, j, k, l);
         }
      }

   }

   private void drawExpired(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.blit(EXPIRED_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
         this.toolTip = SERVER_EXPIRED_TOOLTIP;
      }

   }

   private void drawExpiring(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      if (this.animTick % 20 < 10) {
         guigraphics.blit(EXPIRES_SOON_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         guigraphics.blit(EXPIRES_SOON_ICON_LOCATION, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
         if (i1 <= 0) {
            this.toolTip = SERVER_EXPIRING_SOON_TOOLTIP;
         } else if (i1 == 1) {
            this.toolTip = SERVER_EXPIRING_IN_DAY_TOOLTIP;
         } else {
            this.toolTip = Component.translatable("mco.selectServer.expires.days", i1);
         }
      }

   }

   private void drawOpen(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.blit(ON_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
         this.toolTip = SERVER_OPEN_TOOLTIP;
      }

   }

   private void drawClose(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.blit(OFF_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27) {
         this.toolTip = SERVER_CLOSED_TOOLTIP;
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
   }

   private void hideRegularButtons() {
      this.hide(this.optionsButton);
      this.hide(this.backupButton);
      this.hide(this.resetWorldButton);
   }

   private void hide(Button button) {
      button.visible = false;
      this.removeWidget(button);
   }

   private void show(Button button) {
      button.visible = true;
      this.addRenderableWidget(button);
   }

   private void hideMinigameButtons() {
      this.hide(this.switchMinigameButton);
   }

   public void saveSlotSettings(RealmsWorldOptions realmsworldoptions) {
      RealmsWorldOptions realmsworldoptions1 = this.serverData.slots.get(this.serverData.activeSlot);
      realmsworldoptions.templateId = realmsworldoptions1.templateId;
      realmsworldoptions.templateImage = realmsworldoptions1.templateImage;
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.updateSlot(this.serverData.id, this.serverData.activeSlot, realmsworldoptions);
         this.serverData.slots.put(this.serverData.activeSlot, realmsworldoptions);
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't save slot settings");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
         return;
      }

      this.minecraft.setScreen(this);
   }

   public void saveSettings(String s, String s1) {
      String s2 = s1.trim().isEmpty() ? null : s1;
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.update(this.serverData.id, s, s2);
         this.serverData.setName(s);
         this.serverData.setDescription(s2);
      } catch (RealmsServiceException var6) {
         LOGGER.error("Couldn't save settings");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(var6, this));
         return;
      }

      this.minecraft.setScreen(this);
   }

   public void openTheWorld(boolean flag, Screen screen) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(screen, new OpenServerTask(this.serverData, this, this.lastScreen, flag, this.minecraft)));
   }

   public void closeTheWorld(Screen screen) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(screen, new CloseServerTask(this.serverData, this)));
   }

   public void stateChanged() {
      this.stateChanged = true;
   }

   private void templateSelectionCallback(@Nullable WorldTemplate worldtemplate) {
      if (worldtemplate != null && WorldTemplate.WorldTemplateType.MINIGAME == worldtemplate.type) {
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, worldtemplate, this.getNewScreen())));
      } else {
         this.minecraft.setScreen(this);
      }

   }

   public RealmsConfigureWorldScreen getNewScreen() {
      return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
   }
}
