package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class Gui {
   private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
   private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
   private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
   private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
   private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
   private static final int COLOR_WHITE = 16777215;
   private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
   private static final int NUM_HEARTS_PER_ROW = 10;
   private static final int LINE_HEIGHT = 10;
   private static final String SPACER = ": ";
   private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
   private static final int HEART_SIZE = 9;
   private static final int HEART_SEPARATION = 8;
   private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
   private final RandomSource random = RandomSource.create();
   private final Minecraft minecraft;
   private final ItemRenderer itemRenderer;
   private final ChatComponent chat;
   private int tickCount;
   @Nullable
   private Component overlayMessageString;
   private int overlayMessageTime;
   private boolean animateOverlayMessageColor;
   private boolean chatDisabledByPlayerShown;
   public float vignetteBrightness = 1.0F;
   private int toolHighlightTimer;
   private ItemStack lastToolHighlight = ItemStack.EMPTY;
   private final DebugScreenOverlay debugScreen;
   private final SubtitleOverlay subtitleOverlay;
   private final SpectatorGui spectatorGui;
   private final PlayerTabOverlay tabList;
   private final BossHealthOverlay bossOverlay;
   private int titleTime;
   @Nullable
   private Component title;
   @Nullable
   private Component subtitle;
   private int titleFadeInTime;
   private int titleStayTime;
   private int titleFadeOutTime;
   private int lastHealth;
   private int displayHealth;
   private long lastHealthTime;
   private long healthBlinkTime;
   private int screenWidth;
   private int screenHeight;
   private float autosaveIndicatorValue;
   private float lastAutosaveIndicatorValue;
   private float scopeScale;

   public Gui(Minecraft minecraft, ItemRenderer itemrenderer) {
      this.minecraft = minecraft;
      this.itemRenderer = itemrenderer;
      this.debugScreen = new DebugScreenOverlay(minecraft);
      this.spectatorGui = new SpectatorGui(minecraft);
      this.chat = new ChatComponent(minecraft);
      this.tabList = new PlayerTabOverlay(minecraft, this);
      this.bossOverlay = new BossHealthOverlay(minecraft);
      this.subtitleOverlay = new SubtitleOverlay(minecraft);
      this.resetTitleTimes();
   }

   public void resetTitleTimes() {
      this.titleFadeInTime = 10;
      this.titleStayTime = 70;
      this.titleFadeOutTime = 20;
   }

   public void render(GuiGraphics guigraphics, float f) {
      Window window = this.minecraft.getWindow();
      this.screenWidth = guigraphics.guiWidth();
      this.screenHeight = guigraphics.guiHeight();
      Font font = this.getFont();
      RenderSystem.enableBlend();
      if (Minecraft.useFancyGraphics()) {
         this.renderVignette(guigraphics, this.minecraft.getCameraEntity());
      } else {
         RenderSystem.enableDepthTest();
      }

      float f1 = this.minecraft.getDeltaFrameTime();
      this.scopeScale = Mth.lerp(0.5F * f1, this.scopeScale, 1.125F);
      if (this.minecraft.options.getCameraType().isFirstPerson()) {
         if (this.minecraft.player.isScoping()) {
            this.renderSpyglassOverlay(guigraphics, this.scopeScale);
         } else {
            this.scopeScale = 0.5F;
            ItemStack itemstack = this.minecraft.player.getInventory().getArmor(3);
            if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
               this.renderTextureOverlay(guigraphics, PUMPKIN_BLUR_LOCATION, 1.0F);
            }
         }
      }

      if (this.minecraft.player.getTicksFrozen() > 0) {
         this.renderTextureOverlay(guigraphics, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
      }

      float f2 = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
      if (f2 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
         this.renderPortalOverlay(guigraphics, f2);
      }

      if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
         this.spectatorGui.renderHotbar(guigraphics);
      } else if (!this.minecraft.options.hideGui) {
         this.renderHotbar(f, guigraphics);
      }

      if (!this.minecraft.options.hideGui) {
         RenderSystem.enableBlend();
         this.renderCrosshair(guigraphics);
         this.minecraft.getProfiler().push("bossHealth");
         this.bossOverlay.render(guigraphics);
         this.minecraft.getProfiler().pop();
         if (this.minecraft.gameMode.canHurtPlayer()) {
            this.renderPlayerHealth(guigraphics);
         }

         this.renderVehicleHealth(guigraphics);
         RenderSystem.disableBlend();
         int i = this.screenWidth / 2 - 91;
         PlayerRideableJumping playerrideablejumping = this.minecraft.player.jumpableVehicle();
         if (playerrideablejumping != null) {
            this.renderJumpMeter(playerrideablejumping, guigraphics, i);
         } else if (this.minecraft.gameMode.hasExperience()) {
            this.renderExperienceBar(guigraphics, i);
         }

         if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.renderSelectedItemName(guigraphics);
         } else if (this.minecraft.player.isSpectator()) {
            this.spectatorGui.renderTooltip(guigraphics);
         }
      }

      if (this.minecraft.player.getSleepTimer() > 0) {
         this.minecraft.getProfiler().push("sleep");
         float f3 = (float)this.minecraft.player.getSleepTimer();
         float f4 = f3 / 100.0F;
         if (f4 > 1.0F) {
            f4 = 1.0F - (f3 - 100.0F) / 10.0F;
         }

         int j = (int)(220.0F * f4) << 24 | 1052704;
         guigraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, this.screenHeight, j);
         this.minecraft.getProfiler().pop();
      }

      if (this.minecraft.isDemo()) {
         this.renderDemoOverlay(guigraphics);
      }

      this.renderEffects(guigraphics);
      if (this.minecraft.options.renderDebug) {
         this.debugScreen.render(guigraphics);
      }

      if (!this.minecraft.options.hideGui) {
         if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
            this.minecraft.getProfiler().push("overlayMessage");
            float f5 = (float)this.overlayMessageTime - f;
            int k = (int)(f5 * 255.0F / 20.0F);
            if (k > 255) {
               k = 255;
            }

            if (k > 8) {
               guigraphics.pose().pushPose();
               guigraphics.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
               int l = 16777215;
               if (this.animateOverlayMessageColor) {
                  l = Mth.hsvToRgb(f5 / 50.0F, 0.7F, 0.6F) & 16777215;
               }

               int i1 = k << 24 & -16777216;
               int j1 = font.width(this.overlayMessageString);
               this.drawBackdrop(guigraphics, font, -4, j1, 16777215 | i1);
               guigraphics.drawString(font, this.overlayMessageString, -j1 / 2, -4, l | i1);
               guigraphics.pose().popPose();
            }

            this.minecraft.getProfiler().pop();
         }

         if (this.title != null && this.titleTime > 0) {
            this.minecraft.getProfiler().push("titleAndSubtitle");
            float f6 = (float)this.titleTime - f;
            int k1 = 255;
            if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
               float f7 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f6;
               k1 = (int)(f7 * 255.0F / (float)this.titleFadeInTime);
            }

            if (this.titleTime <= this.titleFadeOutTime) {
               k1 = (int)(f6 * 255.0F / (float)this.titleFadeOutTime);
            }

            k1 = Mth.clamp(k1, 0, 255);
            if (k1 > 8) {
               guigraphics.pose().pushPose();
               guigraphics.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
               RenderSystem.enableBlend();
               guigraphics.pose().pushPose();
               guigraphics.pose().scale(4.0F, 4.0F, 4.0F);
               int l1 = k1 << 24 & -16777216;
               int i2 = font.width(this.title);
               this.drawBackdrop(guigraphics, font, -10, i2, 16777215 | l1);
               guigraphics.drawString(font, this.title, -i2 / 2, -10, 16777215 | l1);
               guigraphics.pose().popPose();
               if (this.subtitle != null) {
                  guigraphics.pose().pushPose();
                  guigraphics.pose().scale(2.0F, 2.0F, 2.0F);
                  int j2 = font.width(this.subtitle);
                  this.drawBackdrop(guigraphics, font, 5, j2, 16777215 | l1);
                  guigraphics.drawString(font, this.subtitle, -j2 / 2, 5, 16777215 | l1);
                  guigraphics.pose().popPose();
               }

               RenderSystem.disableBlend();
               guigraphics.pose().popPose();
            }

            this.minecraft.getProfiler().pop();
         }

         this.subtitleOverlay.render(guigraphics);
         Scoreboard scoreboard = this.minecraft.level.getScoreboard();
         Objective objective = null;
         PlayerTeam playerteam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
         if (playerteam != null) {
            int k2 = playerteam.getColor().getId();
            if (k2 >= 0) {
               objective = scoreboard.getDisplayObjective(3 + k2);
            }
         }

         Objective objective1 = objective != null ? objective : scoreboard.getDisplayObjective(1);
         if (objective1 != null) {
            this.displayScoreboardSidebar(guigraphics, objective1);
         }

         RenderSystem.enableBlend();
         int l2 = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
         int i3 = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
         this.minecraft.getProfiler().push("chat");
         this.chat.render(guigraphics, this.tickCount, l2, i3);
         this.minecraft.getProfiler().pop();
         objective1 = scoreboard.getDisplayObjective(0);
         if (!this.minecraft.options.keyPlayerList.isDown() || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective1 == null) {
            this.tabList.setVisible(false);
         } else {
            this.tabList.setVisible(true);
            this.tabList.render(guigraphics, this.screenWidth, scoreboard, objective1);
         }

         this.renderSavingIndicator(guigraphics);
      }

   }

   private void drawBackdrop(GuiGraphics guigraphics, Font font, int i, int j, int k) {
      int l = this.minecraft.options.getBackgroundColor(0.0F);
      if (l != 0) {
         int i1 = -j / 2;
         guigraphics.fill(i1 - 2, i - 2, i1 + j + 2, i + 9 + 2, FastColor.ARGB32.multiply(l, k));
      }

   }

   private void renderCrosshair(GuiGraphics guigraphics) {
      Options options = this.minecraft.options;
      if (options.getCameraType().isFirstPerson()) {
         if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
               Camera camera = this.minecraft.gameRenderer.getMainCamera();
               PoseStack posestack = RenderSystem.getModelViewStack();
               posestack.pushPose();
               posestack.mulPoseMatrix(guigraphics.pose().last().pose());
               posestack.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
               posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
               posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
               posestack.scale(-1.0F, -1.0F, -1.0F);
               RenderSystem.applyModelViewMatrix();
               RenderSystem.renderCrosshair(10);
               posestack.popPose();
               RenderSystem.applyModelViewMatrix();
            } else {
               RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
               int i = 15;
               guigraphics.blit(GUI_ICONS_LOCATION, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
               if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                  float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                  boolean flag = false;
                  if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
                     flag = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                     flag &= this.minecraft.crosshairPickEntity.isAlive();
                  }

                  int j = this.screenHeight / 2 - 7 + 16;
                  int k = this.screenWidth / 2 - 8;
                  if (flag) {
                     guigraphics.blit(GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
                  } else if (f < 1.0F) {
                     int l = (int)(f * 17.0F);
                     guigraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
                     guigraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
                  }
               }

               RenderSystem.defaultBlendFunc();
            }

         }
      }
   }

   private boolean canRenderCrosshairForSpectator(HitResult hitresult) {
      if (hitresult == null) {
         return false;
      } else if (hitresult.getType() == HitResult.Type.ENTITY) {
         return ((EntityHitResult)hitresult).getEntity() instanceof MenuProvider;
      } else if (hitresult.getType() == HitResult.Type.BLOCK) {
         BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
         Level level = this.minecraft.level;
         return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
      } else {
         return false;
      }
   }

   protected void renderEffects(GuiGraphics guigraphics) {
      Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
      if (!collection.isEmpty()) {
         Screen j = this.minecraft.screen;
         if (j instanceof EffectRenderingInventoryScreen) {
            EffectRenderingInventoryScreen effectrenderinginventoryscreen = (EffectRenderingInventoryScreen)j;
            if (effectrenderinginventoryscreen.canSeeEffects()) {
               return;
            }
         }

         RenderSystem.enableBlend();
         int i = 0;
         int j = 0;
         MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
         List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

         for(MobEffectInstance mobeffectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
            MobEffect mobeffect = mobeffectinstance.getEffect();
            if (mobeffectinstance.showIcon()) {
               int k = this.screenWidth;
               int l = 1;
               if (this.minecraft.isDemo()) {
                  l += 15;
               }

               if (mobeffect.isBeneficial()) {
                  ++i;
                  k -= 25 * i;
               } else {
                  ++j;
                  k -= 25 * j;
                  l += 26;
               }

               float f = 1.0F;
               if (mobeffectinstance.isAmbient()) {
                  guigraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, k, l, 165, 166, 24, 24);
               } else {
                  guigraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, k, l, 141, 166, 24, 24);
                  if (mobeffectinstance.endsWithin(200)) {
                     int i1 = mobeffectinstance.getDuration();
                     int j1 = 10 - i1 / 20;
                     f = Mth.clamp((float)i1 / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float)i1 * (float)Math.PI / 5.0F) * Mth.clamp((float)j1 / 10.0F * 0.25F, 0.0F, 0.25F);
                  }
               }

               TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
               int l1 = l;
               float f1 = f;
               list.add(() -> {
                  guigraphics.setColor(1.0F, 1.0F, 1.0F, f1);
                  guigraphics.blit(k + 3, l1 + 3, 0, 18, 18, textureatlassprite);
                  guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               });
            }
         }

         list.forEach(Runnable::run);
      }
   }

   private void renderHotbar(float f, GuiGraphics guigraphics) {
      Player player = this.getCameraPlayer();
      if (player != null) {
         ItemStack itemstack = player.getOffhandItem();
         HumanoidArm humanoidarm = player.getMainArm().getOpposite();
         int i = this.screenWidth / 2;
         int j = 182;
         int k = 91;
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, -90.0F);
         guigraphics.blit(WIDGETS_LOCATION, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
         guigraphics.blit(WIDGETS_LOCATION, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
         if (!itemstack.isEmpty()) {
            if (humanoidarm == HumanoidArm.LEFT) {
               guigraphics.blit(WIDGETS_LOCATION, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
               guigraphics.blit(WIDGETS_LOCATION, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
            }
         }

         guigraphics.pose().popPose();
         int l = 1;

         for(int i1 = 0; i1 < 9; ++i1) {
            int j1 = i - 90 + i1 * 20 + 2;
            int k1 = this.screenHeight - 16 - 3;
            this.renderSlot(guigraphics, j1, k1, f, player, player.getInventory().items.get(i1), l++);
         }

         if (!itemstack.isEmpty()) {
            int l1 = this.screenHeight - 16 - 3;
            if (humanoidarm == HumanoidArm.LEFT) {
               this.renderSlot(guigraphics, i - 91 - 26, l1, f, player, itemstack, l++);
            } else {
               this.renderSlot(guigraphics, i + 91 + 10, l1, f, player, itemstack, l++);
            }
         }

         RenderSystem.enableBlend();
         if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float f1 = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (f1 < 1.0F) {
               int i2 = this.screenHeight - 20;
               int j2 = i + 91 + 6;
               if (humanoidarm == HumanoidArm.RIGHT) {
                  j2 = i - 91 - 22;
               }

               int k2 = (int)(f1 * 19.0F);
               guigraphics.blit(GUI_ICONS_LOCATION, j2, i2, 0, 94, 18, 18);
               guigraphics.blit(GUI_ICONS_LOCATION, j2, i2 + 18 - k2, 18, 112 - k2, 18, k2);
            }
         }

         RenderSystem.disableBlend();
      }
   }

   public void renderJumpMeter(PlayerRideableJumping playerrideablejumping, GuiGraphics guigraphics, int i) {
      this.minecraft.getProfiler().push("jumpBar");
      float f = this.minecraft.player.getJumpRidingScale();
      int j = 182;
      int k = (int)(f * 183.0F);
      int l = this.screenHeight - 32 + 3;
      guigraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 84, 182, 5);
      if (playerrideablejumping.getJumpCooldown() > 0) {
         guigraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 74, 182, 5);
      } else if (k > 0) {
         guigraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 89, k, 5);
      }

      this.minecraft.getProfiler().pop();
   }

   public void renderExperienceBar(GuiGraphics guigraphics, int i) {
      this.minecraft.getProfiler().push("expBar");
      int j = this.minecraft.player.getXpNeededForNextLevel();
      if (j > 0) {
         int k = 182;
         int l = (int)(this.minecraft.player.experienceProgress * 183.0F);
         int i1 = this.screenHeight - 32 + 3;
         guigraphics.blit(GUI_ICONS_LOCATION, i, i1, 0, 64, 182, 5);
         if (l > 0) {
            guigraphics.blit(GUI_ICONS_LOCATION, i, i1, 0, 69, l, 5);
         }
      }

      this.minecraft.getProfiler().pop();
      if (this.minecraft.player.experienceLevel > 0) {
         this.minecraft.getProfiler().push("expLevel");
         String s = "" + this.minecraft.player.experienceLevel;
         int j1 = (this.screenWidth - this.getFont().width(s)) / 2;
         int k1 = this.screenHeight - 31 - 4;
         guigraphics.drawString(this.getFont(), s, j1 + 1, k1, 0, false);
         guigraphics.drawString(this.getFont(), s, j1 - 1, k1, 0, false);
         guigraphics.drawString(this.getFont(), s, j1, k1 + 1, 0, false);
         guigraphics.drawString(this.getFont(), s, j1, k1 - 1, 0, false);
         guigraphics.drawString(this.getFont(), s, j1, k1, 8453920, false);
         this.minecraft.getProfiler().pop();
      }

   }

   public void renderSelectedItemName(GuiGraphics guigraphics) {
      this.minecraft.getProfiler().push("selectedItemName");
      if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
         MutableComponent mutablecomponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
         if (this.lastToolHighlight.hasCustomHoverName()) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
         }

         int i = this.getFont().width(mutablecomponent);
         int j = (this.screenWidth - i) / 2;
         int k = this.screenHeight - 59;
         if (!this.minecraft.gameMode.canHurtPlayer()) {
            k += 14;
         }

         int l = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
         if (l > 255) {
            l = 255;
         }

         if (l > 0) {
            guigraphics.fill(j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
            guigraphics.drawString(this.getFont(), mutablecomponent, j, k, 16777215 + (l << 24));
         }
      }

      this.minecraft.getProfiler().pop();
   }

   public void renderDemoOverlay(GuiGraphics guigraphics) {
      this.minecraft.getProfiler().push("demo");
      Component component;
      if (this.minecraft.level.getGameTime() >= 120500L) {
         component = DEMO_EXPIRED_TEXT;
      } else {
         component = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
      }

      int i = this.getFont().width(component);
      guigraphics.drawString(this.getFont(), component, this.screenWidth - i - 10, 5, 16777215);
      this.minecraft.getProfiler().pop();
   }

   private void displayScoreboardSidebar(GuiGraphics guigraphics, Objective objective) {
      Scoreboard scoreboard = objective.getScoreboard();
      Collection<Score> collection = scoreboard.getPlayerScores(objective);
      List<Score> list = collection.stream().filter((score2) -> score2.getOwner() != null && !score2.getOwner().startsWith("#")).collect(Collectors.toList());
      if (list.size() > 15) {
         collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
      } else {
         collection = list;
      }

      List<Pair<Score, Component>> list1 = Lists.newArrayListWithCapacity(collection.size());
      Component component = objective.getDisplayName();
      int i = this.getFont().width(component);
      int j = i;
      int k = this.getFont().width(": ");

      for(Score score : collection) {
         PlayerTeam playerteam = scoreboard.getPlayersTeam(score.getOwner());
         Component component1 = PlayerTeam.formatNameForTeam(playerteam, Component.literal(score.getOwner()));
         list1.add(Pair.of(score, component1));
         j = Math.max(j, this.getFont().width(component1) + k + this.getFont().width(Integer.toString(score.getScore())));
      }

      int l = collection.size() * 9;
      int i1 = this.screenHeight / 2 + l / 3;
      int j1 = 3;
      int k1 = this.screenWidth - j - 3;
      int l1 = 0;
      int i2 = this.minecraft.options.getBackgroundColor(0.3F);
      int j2 = this.minecraft.options.getBackgroundColor(0.4F);

      for(Pair<Score, Component> pair : list1) {
         ++l1;
         Score score1 = pair.getFirst();
         Component component2 = pair.getSecond();
         String s = "" + ChatFormatting.RED + score1.getScore();
         int l2 = i1 - l1 * 9;
         int i3 = this.screenWidth - 3 + 2;
         guigraphics.fill(k1 - 2, l2, i3, l2 + 9, i2);
         guigraphics.drawString(this.getFont(), component2, k1, l2, -1, false);
         guigraphics.drawString(this.getFont(), s, i3 - this.getFont().width(s), l2, -1, false);
         if (l1 == collection.size()) {
            guigraphics.fill(k1 - 2, l2 - 9 - 1, i3, l2 - 1, j2);
            guigraphics.fill(k1 - 2, l2 - 1, i3, l2, i2);
            guigraphics.drawString(this.getFont(), component, k1 + j / 2 - i / 2, l2 - 9, -1, false);
         }
      }

   }

   private Player getCameraPlayer() {
      return !(this.minecraft.getCameraEntity() instanceof Player) ? null : (Player)this.minecraft.getCameraEntity();
   }

   private LivingEntity getPlayerVehicleWithHealth() {
      Player player = this.getCameraPlayer();
      if (player != null) {
         Entity entity = player.getVehicle();
         if (entity == null) {
            return null;
         }

         if (entity instanceof LivingEntity) {
            return (LivingEntity)entity;
         }
      }

      return null;
   }

   private int getVehicleMaxHearts(LivingEntity livingentity) {
      if (livingentity != null && livingentity.showVehicleHealth()) {
         float f = livingentity.getMaxHealth();
         int i = (int)(f + 0.5F) / 2;
         if (i > 30) {
            i = 30;
         }

         return i;
      } else {
         return 0;
      }
   }

   private int getVisibleVehicleHeartRows(int i) {
      return (int)Math.ceil((double)i / 10.0D);
   }

   private void renderPlayerHealth(GuiGraphics guigraphics) {
      Player player = this.getCameraPlayer();
      if (player != null) {
         int i = Mth.ceil(player.getHealth());
         boolean flag = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
         long j = Util.getMillis();
         if (i < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = j;
            this.healthBlinkTime = (long)(this.tickCount + 20);
         } else if (i > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = j;
            this.healthBlinkTime = (long)(this.tickCount + 10);
         }

         if (j - this.lastHealthTime > 1000L) {
            this.lastHealth = i;
            this.displayHealth = i;
            this.lastHealthTime = j;
         }

         this.lastHealth = i;
         int k = this.displayHealth;
         this.random.setSeed((long)(this.tickCount * 312871));
         FoodData fooddata = player.getFoodData();
         int l = fooddata.getFoodLevel();
         int i1 = this.screenWidth / 2 - 91;
         int j1 = this.screenWidth / 2 + 91;
         int k1 = this.screenHeight - 39;
         float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(k, i));
         int l1 = Mth.ceil(player.getAbsorptionAmount());
         int i2 = Mth.ceil((f + (float)l1) / 2.0F / 10.0F);
         int j2 = Math.max(10 - (i2 - 2), 3);
         int k2 = k1 - (i2 - 1) * j2 - 10;
         int l2 = k1 - 10;
         int i3 = player.getArmorValue();
         int j3 = -1;
         if (player.hasEffect(MobEffects.REGENERATION)) {
            j3 = this.tickCount % Mth.ceil(f + 5.0F);
         }

         this.minecraft.getProfiler().push("armor");

         for(int k3 = 0; k3 < 10; ++k3) {
            if (i3 > 0) {
               int l3 = i1 + k3 * 8;
               if (k3 * 2 + 1 < i3) {
                  guigraphics.blit(GUI_ICONS_LOCATION, l3, k2, 34, 9, 9, 9);
               }

               if (k3 * 2 + 1 == i3) {
                  guigraphics.blit(GUI_ICONS_LOCATION, l3, k2, 25, 9, 9, 9);
               }

               if (k3 * 2 + 1 > i3) {
                  guigraphics.blit(GUI_ICONS_LOCATION, l3, k2, 16, 9, 9, 9);
               }
            }
         }

         this.minecraft.getProfiler().popPush("health");
         this.renderHearts(guigraphics, player, i1, k1, j2, j3, f, i, k, l1, flag);
         LivingEntity livingentity = this.getPlayerVehicleWithHealth();
         int i4 = this.getVehicleMaxHearts(livingentity);
         if (i4 == 0) {
            this.minecraft.getProfiler().popPush("food");

            for(int j4 = 0; j4 < 10; ++j4) {
               int k4 = k1;
               int l4 = 16;
               int i5 = 0;
               if (player.hasEffect(MobEffects.HUNGER)) {
                  l4 += 36;
                  i5 = 13;
               }

               if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (l * 3 + 1) == 0) {
                  k4 = k1 + (this.random.nextInt(3) - 1);
               }

               int j5 = j1 - j4 * 8 - 9;
               guigraphics.blit(GUI_ICONS_LOCATION, j5, k4, 16 + i5 * 9, 27, 9, 9);
               if (j4 * 2 + 1 < l) {
                  guigraphics.blit(GUI_ICONS_LOCATION, j5, k4, l4 + 36, 27, 9, 9);
               }

               if (j4 * 2 + 1 == l) {
                  guigraphics.blit(GUI_ICONS_LOCATION, j5, k4, l4 + 45, 27, 9, 9);
               }
            }

            l2 -= 10;
         }

         this.minecraft.getProfiler().popPush("air");
         int k5 = player.getMaxAirSupply();
         int l5 = Math.min(player.getAirSupply(), k5);
         if (player.isEyeInFluid(FluidTags.WATER) || l5 < k5) {
            int i6 = this.getVisibleVehicleHeartRows(i4) - 1;
            l2 -= i6 * 10;
            int j6 = Mth.ceil((double)(l5 - 2) * 10.0D / (double)k5);
            int k6 = Mth.ceil((double)l5 * 10.0D / (double)k5) - j6;

            for(int l6 = 0; l6 < j6 + k6; ++l6) {
               if (l6 < j6) {
                  guigraphics.blit(GUI_ICONS_LOCATION, j1 - l6 * 8 - 9, l2, 16, 18, 9, 9);
               } else {
                  guigraphics.blit(GUI_ICONS_LOCATION, j1 - l6 * 8 - 9, l2, 25, 18, 9, 9);
               }
            }
         }

         this.minecraft.getProfiler().pop();
      }
   }

   private void renderHearts(GuiGraphics guigraphics, Player player, int i, int j, int k, int l, float f, int i1, int j1, int k1, boolean flag) {
      Gui.HeartType gui_hearttype = Gui.HeartType.forPlayer(player);
      int l1 = 9 * (player.level().getLevelData().isHardcore() ? 5 : 0);
      int i2 = Mth.ceil((double)f / 2.0D);
      int j2 = Mth.ceil((double)k1 / 2.0D);
      int k2 = i2 * 2;

      for(int l2 = i2 + j2 - 1; l2 >= 0; --l2) {
         int i3 = l2 / 10;
         int j3 = l2 % 10;
         int k3 = i + j3 * 8;
         int l3 = j - i3 * k;
         if (i1 + k1 <= 4) {
            l3 += this.random.nextInt(2);
         }

         if (l2 < i2 && l2 == l) {
            l3 -= 2;
         }

         this.renderHeart(guigraphics, Gui.HeartType.CONTAINER, k3, l3, l1, flag, false);
         int i4 = l2 * 2;
         boolean flag1 = l2 >= i2;
         if (flag1) {
            int j4 = i4 - k2;
            if (j4 < k1) {
               boolean flag2 = j4 + 1 == k1;
               this.renderHeart(guigraphics, gui_hearttype == Gui.HeartType.WITHERED ? gui_hearttype : Gui.HeartType.ABSORBING, k3, l3, l1, false, flag2);
            }
         }

         if (flag && i4 < j1) {
            boolean flag3 = i4 + 1 == j1;
            this.renderHeart(guigraphics, gui_hearttype, k3, l3, l1, true, flag3);
         }

         if (i4 < i1) {
            boolean flag4 = i4 + 1 == i1;
            this.renderHeart(guigraphics, gui_hearttype, k3, l3, l1, false, flag4);
         }
      }

   }

   private void renderHeart(GuiGraphics guigraphics, Gui.HeartType gui_hearttype, int i, int j, int k, boolean flag, boolean flag1) {
      guigraphics.blit(GUI_ICONS_LOCATION, i, j, gui_hearttype.getX(flag1, flag), k, 9, 9);
   }

   private void renderVehicleHealth(GuiGraphics guigraphics) {
      LivingEntity livingentity = this.getPlayerVehicleWithHealth();
      if (livingentity != null) {
         int i = this.getVehicleMaxHearts(livingentity);
         if (i != 0) {
            int j = (int)Math.ceil((double)livingentity.getHealth());
            this.minecraft.getProfiler().popPush("mountHealth");
            int k = this.screenHeight - 39;
            int l = this.screenWidth / 2 + 91;
            int i1 = k;
            int j1 = 0;

            for(boolean flag = false; i > 0; j1 += 20) {
               int k1 = Math.min(i, 10);
               i -= k1;

               for(int l1 = 0; l1 < k1; ++l1) {
                  int i2 = 52;
                  int j2 = 0;
                  int k2 = l - l1 * 8 - 9;
                  guigraphics.blit(GUI_ICONS_LOCATION, k2, i1, 52 + j2 * 9, 9, 9, 9);
                  if (l1 * 2 + 1 + j1 < j) {
                     guigraphics.blit(GUI_ICONS_LOCATION, k2, i1, 88, 9, 9, 9);
                  }

                  if (l1 * 2 + 1 + j1 == j) {
                     guigraphics.blit(GUI_ICONS_LOCATION, k2, i1, 97, 9, 9, 9);
                  }
               }

               i1 -= 10;
            }

         }
      }
   }

   private void renderTextureOverlay(GuiGraphics guigraphics, ResourceLocation resourcelocation, float f) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, f);
      guigraphics.blit(resourcelocation, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderSpyglassOverlay(GuiGraphics guigraphics, float f) {
      float f1 = (float)Math.min(this.screenWidth, this.screenHeight);
      float f3 = Math.min((float)this.screenWidth / f1, (float)this.screenHeight / f1) * f;
      int i = Mth.floor(f1 * f3);
      int j = Mth.floor(f1 * f3);
      int k = (this.screenWidth - i) / 2;
      int l = (this.screenHeight - j) / 2;
      int i1 = k + i;
      int j1 = l + j;
      guigraphics.blit(SPYGLASS_SCOPE_LOCATION, k, l, -90, 0.0F, 0.0F, i, j, i, j);
      guigraphics.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
      guigraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
      guigraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
      guigraphics.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);
   }

   private void updateVignetteBrightness(Entity entity) {
      if (entity != null) {
         BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
         float f = LightTexture.getBrightness(entity.level().dimensionType(), entity.level().getMaxLocalRawBrightness(blockpos));
         float f1 = Mth.clamp(1.0F - f, 0.0F, 1.0F);
         this.vignetteBrightness += (f1 - this.vignetteBrightness) * 0.01F;
      }
   }

   private void renderVignette(GuiGraphics guigraphics, Entity entity) {
      WorldBorder worldborder = this.minecraft.level.getWorldBorder();
      float f = (float)worldborder.getDistanceToBorder(entity);
      double d0 = Math.min(worldborder.getLerpSpeed() * (double)worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getLerpTarget() - worldborder.getSize()));
      double d1 = Math.max((double)worldborder.getWarningBlocks(), d0);
      if ((double)f < d1) {
         f = 1.0F - (float)((double)f / d1);
      } else {
         f = 0.0F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      if (f > 0.0F) {
         f = Mth.clamp(f, 0.0F, 1.0F);
         guigraphics.setColor(0.0F, f, f, 1.0F);
      } else {
         float f1 = this.vignetteBrightness;
         f1 = Mth.clamp(f1, 0.0F, 1.0F);
         guigraphics.setColor(f1, f1, f1, 1.0F);
      }

      guigraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
   }

   private void renderPortalOverlay(GuiGraphics guigraphics, float f) {
      if (f < 1.0F) {
         f *= f;
         f *= f;
         f = f * 0.8F + 0.2F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, f);
      TextureAtlasSprite textureatlassprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
      guigraphics.blit(0, 0, -90, this.screenWidth, this.screenHeight, textureatlassprite);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderSlot(GuiGraphics guigraphics, int i, int j, float f, Player player, ItemStack itemstack, int k) {
      if (!itemstack.isEmpty()) {
         float f1 = (float)itemstack.getPopTime() - f;
         if (f1 > 0.0F) {
            float f2 = 1.0F + f1 / 5.0F;
            guigraphics.pose().pushPose();
            guigraphics.pose().translate((float)(i + 8), (float)(j + 12), 0.0F);
            guigraphics.pose().scale(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
            guigraphics.pose().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
         }

         guigraphics.renderItem(player, itemstack, i, j, k);
         if (f1 > 0.0F) {
            guigraphics.pose().popPose();
         }

         guigraphics.renderItemDecorations(this.minecraft.font, itemstack, i, j);
      }
   }

   public void tick(boolean flag) {
      this.tickAutosaveIndicator();
      if (!flag) {
         this.tick();
      }

   }

   private void tick() {
      if (this.overlayMessageTime > 0) {
         --this.overlayMessageTime;
      }

      if (this.titleTime > 0) {
         --this.titleTime;
         if (this.titleTime <= 0) {
            this.title = null;
            this.subtitle = null;
         }
      }

      ++this.tickCount;
      Entity entity = this.minecraft.getCameraEntity();
      if (entity != null) {
         this.updateVignetteBrightness(entity);
      }

      if (this.minecraft.player != null) {
         ItemStack itemstack = this.minecraft.player.getInventory().getSelected();
         if (itemstack.isEmpty()) {
            this.toolHighlightTimer = 0;
         } else if (!this.lastToolHighlight.isEmpty() && itemstack.is(this.lastToolHighlight.getItem()) && itemstack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
            if (this.toolHighlightTimer > 0) {
               --this.toolHighlightTimer;
            }
         } else {
            this.toolHighlightTimer = (int)(40.0D * this.minecraft.options.notificationDisplayTime().get());
         }

         this.lastToolHighlight = itemstack;
      }

      this.chat.tick();
   }

   private void tickAutosaveIndicator() {
      MinecraftServer minecraftserver = this.minecraft.getSingleplayerServer();
      boolean flag = minecraftserver != null && minecraftserver.isCurrentlySaving();
      this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
      this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, flag ? 1.0F : 0.0F);
   }

   public void setNowPlaying(Component component) {
      Component component1 = Component.translatable("record.nowPlaying", component);
      this.setOverlayMessage(component1, true);
      this.minecraft.getNarrator().sayNow(component1);
   }

   public void setOverlayMessage(Component component, boolean flag) {
      this.setChatDisabledByPlayerShown(false);
      this.overlayMessageString = component;
      this.overlayMessageTime = 60;
      this.animateOverlayMessageColor = flag;
   }

   public void setChatDisabledByPlayerShown(boolean flag) {
      this.chatDisabledByPlayerShown = flag;
   }

   public boolean isShowingChatDisabledByPlayer() {
      return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
   }

   public void setTimes(int i, int j, int k) {
      if (i >= 0) {
         this.titleFadeInTime = i;
      }

      if (j >= 0) {
         this.titleStayTime = j;
      }

      if (k >= 0) {
         this.titleFadeOutTime = k;
      }

      if (this.titleTime > 0) {
         this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
      }

   }

   public void setSubtitle(Component component) {
      this.subtitle = component;
   }

   public void setTitle(Component component) {
      this.title = component;
      this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
   }

   public void clear() {
      this.title = null;
      this.subtitle = null;
      this.titleTime = 0;
   }

   public ChatComponent getChat() {
      return this.chat;
   }

   public int getGuiTicks() {
      return this.tickCount;
   }

   public Font getFont() {
      return this.minecraft.font;
   }

   public SpectatorGui getSpectatorGui() {
      return this.spectatorGui;
   }

   public PlayerTabOverlay getTabList() {
      return this.tabList;
   }

   public void onDisconnected() {
      this.tabList.reset();
      this.bossOverlay.reset();
      this.minecraft.getToasts().clear();
      this.minecraft.options.renderDebug = false;
      this.chat.clearMessages(true);
   }

   public BossHealthOverlay getBossOverlay() {
      return this.bossOverlay;
   }

   public void clearCache() {
      this.debugScreen.clearChunkCache();
   }

   private void renderSavingIndicator(GuiGraphics guigraphics) {
      if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
         int i = Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F));
         if (i > 8) {
            Font font = this.getFont();
            int j = font.width(SAVING_TEXT);
            int k = 16777215 | i << 24 & -16777216;
            guigraphics.drawString(font, SAVING_TEXT, this.screenWidth - j - 10, this.screenHeight - 15, k);
         }
      }

   }

   static enum HeartType {
      CONTAINER(0, false),
      NORMAL(2, true),
      POISIONED(4, true),
      WITHERED(6, true),
      ABSORBING(8, false),
      FROZEN(9, false);

      private final int index;
      private final boolean canBlink;

      private HeartType(int i, boolean flag) {
         this.index = i;
         this.canBlink = flag;
      }

      public int getX(boolean flag, boolean flag1) {
         int i;
         if (this == CONTAINER) {
            i = flag1 ? 1 : 0;
         } else {
            int j = flag ? 1 : 0;
            int k = this.canBlink && flag1 ? 2 : 0;
            i = j + k;
         }

         return 16 + (this.index * 2 + i) * 9;
      }

      static Gui.HeartType forPlayer(Player player) {
         Gui.HeartType gui_hearttype;
         if (player.hasEffect(MobEffects.POISON)) {
            gui_hearttype = POISIONED;
         } else if (player.hasEffect(MobEffects.WITHER)) {
            gui_hearttype = WITHERED;
         } else if (player.isFullyFrozen()) {
            gui_hearttype = FROZEN;
         } else {
            gui_hearttype = NORMAL;
         }

         return gui_hearttype;
      }
   }
}
