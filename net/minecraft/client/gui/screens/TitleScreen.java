package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

public class TitleScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEMO_LEVEL_ID = "Demo_World";
   public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
   public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
   private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
   @Nullable
   private SplashRenderer splash;
   private Button resetDemoButton;
   @Nullable
   private RealmsNotificationsScreen realmsNotificationsScreen;
   private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
   private final boolean fading;
   private long fadeInStart;
   @Nullable
   private TitleScreen.WarningLabel warningLabel;
   private final LogoRenderer logoRenderer;

   public TitleScreen() {
      this(false);
   }

   public TitleScreen(boolean flag) {
      this(flag, (LogoRenderer)null);
   }

   public TitleScreen(boolean flag, @Nullable LogoRenderer logorenderer) {
      super(Component.translatable("narrator.screen.title"));
      this.fading = flag;
      this.logoRenderer = Objects.requireNonNullElseGet(logorenderer, () -> new LogoRenderer(false));
   }

   private boolean realmsNotificationsEnabled() {
      return this.realmsNotificationsScreen != null;
   }

   public void tick() {
      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.tick();
      }

      this.minecraft.getRealms32BitWarningStatus().showRealms32BitWarningIfNeeded(this);
   }

   public static CompletableFuture<Void> preloadResources(TextureManager texturemanager, Executor executor) {
      return CompletableFuture.allOf(texturemanager.preload(LogoRenderer.MINECRAFT_LOGO, executor), texturemanager.preload(LogoRenderer.MINECRAFT_EDITION, executor), texturemanager.preload(PANORAMA_OVERLAY, executor), CUBE_MAP.preload(texturemanager, executor));
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      if (this.splash == null) {
         this.splash = this.minecraft.getSplashManager().getSplash();
      }

      int i = this.font.width(COPYRIGHT_TEXT);
      int j = this.width - i - 2;
      int k = 24;
      int l = this.height / 4 + 48;
      if (this.minecraft.isDemo()) {
         this.createDemoMenuOptions(l, 24);
      } else {
         this.createNormalMenuOptions(l, 24);
      }

      this.addRenderableWidget(new ImageButton(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button4) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Component.translatable("narrator.button.language")));
      this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), (button3) -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button2) -> this.minecraft.stop()).bounds(this.width / 2 + 2, l + 72 + 12, 98, 20).build());
      this.addRenderableWidget(new ImageButton(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, Button.ACCESSIBILITY_TEXTURE, 32, 64, (button1) -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), Component.translatable("narrator.button.accessibility")));
      this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, (button) -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font));
      this.minecraft.setConnectedToRealms(false);
      if (this.realmsNotificationsScreen == null) {
         this.realmsNotificationsScreen = new RealmsNotificationsScreen();
      }

      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
      }

      if (!this.minecraft.is64Bit()) {
         this.warningLabel = new TitleScreen.WarningLabel(this.font, MultiLineLabel.create(this.font, Component.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24);
      }

   }

   private void createNormalMenuOptions(int i, int j) {
      this.addRenderableWidget(Button.builder(Component.translatable("menu.singleplayer"), (button2) -> this.minecraft.setScreen(new SelectWorldScreen(this))).bounds(this.width / 2 - 100, i, 200, 20).build());
      Component component = this.getMultiplayerDisabledReason();
      boolean flag = component == null;
      Tooltip tooltip = component != null ? Tooltip.create(component) : null;
      (this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), (button1) -> {
         Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
         this.minecraft.setScreen(screen);
      }).bounds(this.width / 2 - 100, i + j * 1, 200, 20).tooltip(tooltip).build())).active = flag;
      (this.addRenderableWidget(Button.builder(Component.translatable("menu.online"), (button) -> this.realmsButtonClicked()).bounds(this.width / 2 - 100, i + j * 2, 200, 20).tooltip(tooltip).build())).active = flag;
   }

   @Nullable
   private Component getMultiplayerDisabledReason() {
      if (this.minecraft.allowsMultiplayer()) {
         return null;
      } else {
         BanDetails bandetails = this.minecraft.multiplayerBan();
         if (bandetails != null) {
            return bandetails.expires() != null ? Component.translatable("title.multiplayer.disabled.banned.temporary") : Component.translatable("title.multiplayer.disabled.banned.permanent");
         } else {
            return Component.translatable("title.multiplayer.disabled");
         }
      }
   }

   private void createDemoMenuOptions(int i, int j) {
      boolean flag = this.checkDemoWorldPresence();
      this.addRenderableWidget(Button.builder(Component.translatable("menu.playdemo"), (button1) -> {
         if (flag) {
            this.minecraft.createWorldOpenFlows().loadLevel(this, "Demo_World");
         } else {
            this.minecraft.createWorldOpenFlows().createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions);
         }

      }).bounds(this.width / 2 - 100, i, 200, 20).build());
      this.resetDemoButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.resetdemo"), (button) -> {
         LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();

         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = levelstoragesource.createAccess("Demo_World");

            try {
               LevelSummary levelsummary = levelstoragesource_levelstorageaccess.getSummary();
               if (levelsummary != null) {
                  this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", levelsummary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
               }
            } catch (Throwable var7) {
               if (levelstoragesource_levelstorageaccess != null) {
                  try {
                     levelstoragesource_levelstorageaccess.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (levelstoragesource_levelstorageaccess != null) {
               levelstoragesource_levelstorageaccess.close();
            }
         } catch (IOException var8) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to access demo world", (Throwable)var8);
         }

      }).bounds(this.width / 2 - 100, i + j * 1, 200, 20).build());
      this.resetDemoButton.active = flag;
   }

   private boolean checkDemoWorldPresence() {
      try {
         LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World");

         boolean var2;
         try {
            var2 = levelstoragesource_levelstorageaccess.getSummary() != null;
         } catch (Throwable var5) {
            if (levelstoragesource_levelstorageaccess != null) {
               try {
                  levelstoragesource_levelstorageaccess.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (levelstoragesource_levelstorageaccess != null) {
            levelstoragesource_levelstorageaccess.close();
         }

         return var2;
      } catch (IOException var6) {
         SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
         LOGGER.warn("Failed to read demo world data", (Throwable)var6);
         return false;
      }
   }

   private void realmsButtonClicked() {
      this.minecraft.setScreen(new RealmsMainScreen(this));
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.fadeInStart == 0L && this.fading) {
         this.fadeInStart = Util.getMillis();
      }

      float f1 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
      this.panorama.render(f, Mth.clamp(f1, 0.0F, 1.0F));
      RenderSystem.enableBlend();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(f1, 0.0F, 1.0F)) : 1.0F);
      guigraphics.blit(PANORAMA_OVERLAY, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      float f2 = this.fading ? Mth.clamp(f1 - 1.0F, 0.0F, 1.0F) : 1.0F;
      this.logoRenderer.renderLogo(guigraphics, this.width, f2);
      int k = Mth.ceil(f2 * 255.0F) << 24;
      if ((k & -67108864) != 0) {
         if (this.warningLabel != null) {
            this.warningLabel.render(guigraphics, k);
         }

         if (this.splash != null) {
            this.splash.render(guigraphics, this.width, this.font, k);
         }

         String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
         if (this.minecraft.isDemo()) {
            s = s + " Demo";
         } else {
            s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
         }

         if (Minecraft.checkModStatus().shouldReportAsModified()) {
            s = s + I18n.get("menu.modded");
         }

         guigraphics.drawString(this.font, s, 2, this.height - 10, 16777215 | k);

         for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener instanceof AbstractWidget) {
               ((AbstractWidget)guieventlistener).setAlpha(f2);
            }
         }

         super.render(guigraphics, i, j, f);
         if (this.realmsNotificationsEnabled() && f2 >= 1.0F) {
            RenderSystem.enableDepthTest();
            this.realmsNotificationsScreen.render(guigraphics, i, j, f);
         }

      }
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (super.mouseClicked(d0, d1, i)) {
         return true;
      } else {
         return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d0, d1, i);
      }
   }

   public void removed() {
      if (this.realmsNotificationsScreen != null) {
         this.realmsNotificationsScreen.removed();
      }

   }

   public void added() {
      super.added();
      if (this.realmsNotificationsScreen != null) {
         this.realmsNotificationsScreen.added();
      }

   }

   private void confirmDemo(boolean flag) {
      if (flag) {
         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World");

            try {
               levelstoragesource_levelstorageaccess.deleteLevel();
            } catch (Throwable var6) {
               if (levelstoragesource_levelstorageaccess != null) {
                  try {
                     levelstoragesource_levelstorageaccess.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (levelstoragesource_levelstorageaccess != null) {
               levelstoragesource_levelstorageaccess.close();
            }
         } catch (IOException var7) {
            SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to delete demo world", (Throwable)var7);
         }
      }

      this.minecraft.setScreen(this);
   }

   static record WarningLabel(Font font, MultiLineLabel label, int x, int y) {
      public void render(GuiGraphics guigraphics, int i) {
         this.label.renderBackgroundCentered(guigraphics, this.x, this.y, 9, 2, 2097152 | Math.min(i, 1426063360));
         this.label.renderCentered(guigraphics, this.x, this.y, 9, 16777215 | i);
      }
   }
}
