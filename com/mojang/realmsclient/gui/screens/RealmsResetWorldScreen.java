package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsResetWorldScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Screen lastScreen;
   private final RealmsServer serverData;
   private Component subtitle = Component.translatable("mco.reset.world.warning");
   private Component buttonTitle = CommonComponents.GUI_CANCEL;
   private int subtitleColor = 16711680;
   private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
   private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
   private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
   private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
   private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
   private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
   WorldTemplatePaginatedList templates;
   WorldTemplatePaginatedList adventuremaps;
   WorldTemplatePaginatedList experiences;
   WorldTemplatePaginatedList inspirations;
   public int slot = -1;
   private Component resetTitle = Component.translatable("mco.reset.world.resetting.screen.title");
   private final Runnable resetWorldRunnable;
   private final Runnable callback;

   public RealmsResetWorldScreen(Screen screen, RealmsServer realmsserver, Component component, Runnable runnable, Runnable runnable1) {
      super(component);
      this.lastScreen = screen;
      this.serverData = realmsserver;
      this.resetWorldRunnable = runnable;
      this.callback = runnable1;
   }

   public RealmsResetWorldScreen(Screen screen, RealmsServer realmsserver, Runnable runnable, Runnable runnable1) {
      this(screen, realmsserver, Component.translatable("mco.reset.world.title"), runnable, runnable1);
   }

   public RealmsResetWorldScreen(Screen screen, RealmsServer realmsserver, Component component, Component component1, int i, Component component2, Runnable runnable, Runnable runnable1) {
      this(screen, realmsserver, component, runnable, runnable1);
      this.subtitle = component1;
      this.subtitleColor = i;
      this.buttonTitle = component2;
   }

   public void setSlot(int i) {
      this.slot = i;
   }

   public void setResetTitle(Component component) {
      this.resetTitle = component;
   }

   public void init() {
      this.addRenderableWidget(Button.builder(this.buttonTitle, (button6) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 40, row(14) - 10, 80, 20).build());
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               WorldTemplatePaginatedList worldtemplatepaginatedlist = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
               WorldTemplatePaginatedList worldtemplatepaginatedlist1 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
               WorldTemplatePaginatedList worldtemplatepaginatedlist2 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
               WorldTemplatePaginatedList worldtemplatepaginatedlist3 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
               RealmsResetWorldScreen.this.minecraft.execute(() -> {
                  RealmsResetWorldScreen.this.templates = worldtemplatepaginatedlist;
                  RealmsResetWorldScreen.this.adventuremaps = worldtemplatepaginatedlist1;
                  RealmsResetWorldScreen.this.experiences = worldtemplatepaginatedlist2;
                  RealmsResetWorldScreen.this.inspirations = worldtemplatepaginatedlist3;
               });
            } catch (RealmsServiceException var6) {
               RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", (Throwable)var6);
            }

         }
      }).start();
      this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(0) + 10, Component.translatable("mco.reset.world.generate"), NEW_WORLD_LOCATION, (button5) -> this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title))));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(0) + 10, Component.translatable("mco.reset.world.upload"), UPLOAD_LOCATION, (button4) -> this.minecraft.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback))));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(0) + 10, Component.translatable("mco.reset.world.template"), SURVIVAL_SPAWN_LOCATION, (button3) -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.template"), this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates))));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(6) + 20, Component.translatable("mco.reset.world.adventure"), ADVENTURE_MAP_LOCATION, (button2) -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.adventure"), this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps))));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(6) + 20, Component.translatable("mco.reset.world.experience"), EXPERIENCE_LOCATION, (button1) -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.experience"), this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences))));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(6) + 20, Component.translatable("mco.reset.world.inspiration"), INSPIRATION_LOCATION, (button) -> this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.inspiration"), this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations))));
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   private int frame(int i) {
      return this.width / 2 - 130 + (i - 1) * 100;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 7, 16777215);
      super.render(guigraphics, i, j, f);
   }

   void drawFrame(GuiGraphics guigraphics, int i, int j, Component component, ResourceLocation resourcelocation, boolean flag, boolean flag1) {
      if (flag) {
         guigraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      guigraphics.blit(resourcelocation, i + 2, j + 14, 0.0F, 0.0F, 56, 56, 56, 56);
      guigraphics.blit(SLOT_FRAME_LOCATION, i, j + 12, 0.0F, 0.0F, 60, 60, 60, 60);
      int k = flag ? 10526880 : 16777215;
      guigraphics.drawCenteredString(this.font, component, i + 30, j, k);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void startTask(LongRunningTask longrunningtask) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, longrunningtask));
   }

   public void switchSlot(Runnable runnable) {
      this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, () -> this.minecraft.execute(runnable)));
   }

   private void templateSelectionCallback(@Nullable WorldTemplate worldtemplate) {
      this.minecraft.setScreen(this);
      if (worldtemplate != null) {
         this.resetWorld(() -> this.startTask(new ResettingTemplateWorldTask(worldtemplate, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
      }

   }

   private void generationSelectionCallback(@Nullable WorldGenerationInfo worldgenerationinfo) {
      this.minecraft.setScreen(this);
      if (worldgenerationinfo != null) {
         this.resetWorld(() -> this.startTask(new ResettingGeneratedWorldTask(worldgenerationinfo, this.serverData.id, this.resetTitle, this.resetWorldRunnable)));
      }

   }

   private void resetWorld(Runnable runnable) {
      if (this.slot == -1) {
         runnable.run();
      } else {
         this.switchSlot(runnable);
      }

   }

   class FrameButton extends Button {
      private final ResourceLocation image;

      public FrameButton(int i, int j, Component component, ResourceLocation resourcelocation, Button.OnPress button_onpress) {
         super(i, j, 60, 72, component, button_onpress, DEFAULT_NARRATION);
         this.image = resourcelocation;
      }

      public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
         RealmsResetWorldScreen.this.drawFrame(guigraphics, this.getX(), this.getY(), this.getMessage(), this.image, this.isHoveredOrFocused(), this.isMouseOver((double)i, (double)j));
      }
   }
}
