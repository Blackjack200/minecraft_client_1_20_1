package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class RealmsBrokenWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_BUTTON_WIDTH = 80;
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   @Nullable
   private RealmsServer serverData;
   private final long serverId;
   private final Component[] message = new Component[]{Component.translatable("mco.brokenworld.message.line1"), Component.translatable("mco.brokenworld.message.line2")};
   private int leftX;
   private int rightX;
   private final List<Integer> slotsThatHasBeenDownloaded = Lists.newArrayList();
   private int animTick;

   public RealmsBrokenWorldScreen(Screen screen, RealmsMainScreen realmsmainscreen, long i, boolean flag) {
      super(flag ? Component.translatable("mco.brokenworld.minigame.title") : Component.translatable("mco.brokenworld.title"));
      this.lastScreen = screen;
      this.mainScreen = realmsmainscreen;
      this.serverId = i;
   }

   public void init() {
      this.leftX = this.width / 2 - 150;
      this.rightX = this.width / 2 + 190;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.backButtonClicked()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build());
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      } else {
         this.addButtons();
      }

   }

   public Component getNarrationMessage() {
      return ComponentUtils.formatList(Stream.concat(Stream.of(this.title), Stream.of(this.message)).collect(Collectors.toList()), CommonComponents.SPACE);
   }

   private void addButtons() {
      for(Map.Entry<Integer, RealmsWorldOptions> map_entry : this.serverData.slots.entrySet()) {
         int i = map_entry.getKey();
         boolean flag = i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
         Button button;
         if (flag) {
            button = Button.builder(Component.translatable("mco.brokenworld.play"), (button4) -> {
               if ((this.serverData.slots.get(i)).empty) {
                  RealmsResetWorldScreen realmsresetworldscreen1 = new RealmsResetWorldScreen(this, this.serverData, Component.translatable("mco.configure.world.switch.slot"), Component.translatable("mco.configure.world.switch.slot.subtitle"), 10526880, CommonComponents.GUI_CANCEL, this::doSwitchOrReset, () -> {
                     this.minecraft.setScreen(this);
                     this.doSwitchOrReset();
                  });
                  realmsresetworldscreen1.setSlot(i);
                  realmsresetworldscreen1.setResetTitle(Component.translatable("mco.create.world.reset.title"));
                  this.minecraft.setScreen(realmsresetworldscreen1);
               } else {
                  this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, i, this::doSwitchOrReset)));
               }

            }).bounds(this.getFramePositionX(i), row(8), 80, 20).build();
         } else {
            button = Button.builder(Component.translatable("mco.brokenworld.download"), (button3) -> {
               Component component = Component.translatable("mco.configure.world.restore.download.question.line1");
               Component component1 = Component.translatable("mco.configure.world.restore.download.question.line2");
               this.minecraft.setScreen(new RealmsLongConfirmationScreen((flag1) -> {
                  if (flag1) {
                     this.downloadWorld(i);
                  } else {
                     this.minecraft.setScreen(this);
                  }

               }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
            }).bounds(this.getFramePositionX(i), row(8), 80, 20).build();
         }

         if (this.slotsThatHasBeenDownloaded.contains(i)) {
            button.active = false;
            button.setMessage(Component.translatable("mco.brokenworld.downloaded"));
         }

         this.addRenderableWidget(button);
         this.addRenderableWidget(Button.builder(Component.translatable("mco.brokenworld.reset"), (button2) -> {
            RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this, this.serverData, this::doSwitchOrReset, () -> {
               this.minecraft.setScreen(this);
               this.doSwitchOrReset();
            });
            if (i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
               realmsresetworldscreen.setSlot(i);
            }

            this.minecraft.setScreen(realmsresetworldscreen);
         }).bounds(this.getFramePositionX(i), row(10), 80, 20).build());
      }

   }

   public void tick() {
      ++this.animTick;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);

      for(int k = 0; k < this.message.length; ++k) {
         guigraphics.drawCenteredString(this.font, this.message[k], this.width / 2, row(-1) + 3 + k * 12, 10526880);
      }

      if (this.serverData != null) {
         for(Map.Entry<Integer, RealmsWorldOptions> map_entry : this.serverData.slots.entrySet()) {
            if ((map_entry.getValue()).templateImage != null && (map_entry.getValue()).templateId != -1L) {
               this.drawSlotFrame(guigraphics, this.getFramePositionX(map_entry.getKey()), row(1) + 5, i, j, this.serverData.activeSlot == map_entry.getKey() && !this.isMinigame(), map_entry.getValue().getSlotName(map_entry.getKey()), map_entry.getKey(), (map_entry.getValue()).templateId, (map_entry.getValue()).templateImage, (map_entry.getValue()).empty);
            } else {
               this.drawSlotFrame(guigraphics, this.getFramePositionX(map_entry.getKey()), row(1) + 5, i, j, this.serverData.activeSlot == map_entry.getKey() && !this.isMinigame(), map_entry.getValue().getSlotName(map_entry.getKey()), map_entry.getKey(), -1L, (String)null, (map_entry.getValue()).empty);
            }
         }

      }
   }

   private int getFramePositionX(int i) {
      return this.leftX + (i - 1) * 110;
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
      this.minecraft.setScreen(this.lastScreen);
   }

   private void fetchServerData(long i) {
      (new Thread(() -> {
         RealmsClient realmsclient = RealmsClient.create();

         try {
            this.serverData = realmsclient.getOwnWorld(i);
            this.addButtons();
         } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get own world");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen));
         }

      })).start();
   }

   public void doSwitchOrReset() {
      (new Thread(() -> {
         RealmsClient realmsclient = RealmsClient.create();
         if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.minecraft.execute(() -> this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this, new OpenServerTask(this.serverData, this, this.mainScreen, true, this.minecraft))));
         } else {
            try {
               RealmsServer realmsserver = realmsclient.getOwnWorld(this.serverId);
               this.minecraft.execute(() -> this.mainScreen.newScreen().play(realmsserver, this));
            } catch (RealmsServiceException var3) {
               LOGGER.error("Couldn't get own world");
               this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen));
            }
         }

      })).start();
   }

   private void downloadWorld(int i) {
      RealmsClient realmsclient = RealmsClient.create();

      try {
         WorldDownload worlddownload = realmsclient.requestDownloadInfo(this.serverData.id, i);
         RealmsDownloadLatestWorldScreen realmsdownloadlatestworldscreen = new RealmsDownloadLatestWorldScreen(this, worlddownload, this.serverData.getWorldName(i), (flag) -> {
            if (flag) {
               this.slotsThatHasBeenDownloaded.add(i);
               this.clearWidgets();
               this.addButtons();
            } else {
               this.minecraft.setScreen(this);
            }

         });
         this.minecraft.setScreen(realmsdownloadlatestworldscreen);
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't download world data");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
   }

   private void drawSlotFrame(GuiGraphics guigraphics, int i, int j, int k, int l, boolean flag, String s, int i1, long j1, @Nullable String s1, boolean flag1) {
      ResourceLocation resourcelocation;
      if (flag1) {
         resourcelocation = RealmsWorldSlotButton.EMPTY_SLOT_LOCATION;
      } else if (s1 != null && j1 != -1L) {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(j1), s1);
      } else if (i1 == 1) {
         resourcelocation = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_1;
      } else if (i1 == 2) {
         resourcelocation = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_2;
      } else if (i1 == 3) {
         resourcelocation = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_3;
      } else {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
      }

      if (!flag) {
         guigraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      } else if (flag) {
         float f = 0.9F + 0.1F * Mth.cos((float)this.animTick * 0.2F);
         guigraphics.setColor(f, f, f, 1.0F);
      }

      guigraphics.blit(resourcelocation, i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      if (flag) {
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         guigraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      guigraphics.blit(RealmsWorldSlotButton.SLOT_FRAME_LOCATION, i, j, 0.0F, 0.0F, 80, 80, 80, 80);
      guigraphics.drawCenteredString(this.font, s, i + 40, j + 66, 16777215);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
