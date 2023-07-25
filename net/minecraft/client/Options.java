package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class Options {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Gson GSON = new Gson();
   private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
   };
   public static final int RENDER_DISTANCE_TINY = 2;
   public static final int RENDER_DISTANCE_SHORT = 4;
   public static final int RENDER_DISTANCE_NORMAL = 8;
   public static final int RENDER_DISTANCE_FAR = 12;
   public static final int RENDER_DISTANCE_REALLY_FAR = 16;
   public static final int RENDER_DISTANCE_EXTREME = 32;
   private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
   private static final float DEFAULT_VOLUME = 1.0F;
   public static final String DEFAULT_SOUND_DEVICE = "";
   private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = Component.translatable("options.darkMojangStudiosBackgroundColor.tooltip");
   private final OptionInstance<Boolean> darkMojangStudiosBackground = OptionInstance.createBoolean("options.darkMojangStudiosBackgroundColor", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND), false);
   private static final Component ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES = Component.translatable("options.hideLightningFlashes.tooltip");
   private final OptionInstance<Boolean> hideLightningFlash = OptionInstance.createBoolean("options.hideLightningFlashes", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES), false);
   private final OptionInstance<Double> sensitivity = new OptionInstance<>("options.sensitivity", OptionInstance.noTooltip(), (component30, odouble38) -> {
      if (odouble38 == 0.0D) {
         return genericValueLabel(component30, Component.translatable("options.sensitivity.min"));
      } else {
         return odouble38 == 1.0D ? genericValueLabel(component30, Component.translatable("options.sensitivity.max")) : percentValueLabel(component30, 2.0D * odouble38);
      }
   }, OptionInstance.UnitDouble.INSTANCE, 0.5D, (odouble37) -> {
   });
   private final OptionInstance<Integer> renderDistance;
   private final OptionInstance<Integer> simulationDistance;
   private int serverRenderDistance = 0;
   private final OptionInstance<Double> entityDistanceScaling = new OptionInstance<>("options.entityDistanceScaling", OptionInstance.noTooltip(), Options::percentValueLabel, (new OptionInstance.IntRange(2, 20)).xmap((j1) -> (double)j1 / 4.0D, (odouble36) -> (int)(odouble36 * 4.0D)), Codec.doubleRange(0.5D, 5.0D), 1.0D, (odouble35) -> {
   });
   public static final int UNLIMITED_FRAMERATE_CUTOFF = 260;
   private final OptionInstance<Integer> framerateLimit = new OptionInstance<>("options.framerateLimit", OptionInstance.noTooltip(), (component29, integer15) -> integer15 == 260 ? genericValueLabel(component29, Component.translatable("options.framerateLimit.max")) : genericValueLabel(component29, Component.translatable("options.framerate", integer15)), (new OptionInstance.IntRange(1, 26)).xmap((i1) -> i1 * 10, (integer14) -> integer14 / 10), Codec.intRange(10, 260), 120, (integer13) -> Minecraft.getInstance().getWindow().setFramerateLimit(integer13));
   private final OptionInstance<CloudStatus> cloudStatus = new OptionInstance<>("options.renderClouds", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(CloudStatus.values()), Codec.either(Codec.BOOL, Codec.STRING).xmap((either) -> either.map((obool17) -> obool17 ? CloudStatus.FANCY : CloudStatus.OFF, (s4) -> {
         CloudStatus var10000;
         switch (s4) {
            case "true":
               var10000 = CloudStatus.FANCY;
               break;
            case "fast":
               var10000 = CloudStatus.FAST;
               break;
            default:
               var10000 = CloudStatus.OFF;
         }

         return var10000;
      }), (cloudstatus1) -> {
      String var10000;
      switch (cloudstatus1) {
         case FANCY:
            var10000 = "true";
            break;
         case FAST:
            var10000 = "fast";
            break;
         case OFF:
            var10000 = "false";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return Either.right(var10000);
   })), CloudStatus.FANCY, (cloudstatus) -> {
      if (Minecraft.useShaderTransparency()) {
         RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.getCloudsTarget();
         if (rendertarget != null) {
            rendertarget.clear(Minecraft.ON_OSX);
         }
      }

   });
   private static final Component GRAPHICS_TOOLTIP_FAST = Component.translatable("options.graphics.fast.tooltip");
   private static final Component GRAPHICS_TOOLTIP_FABULOUS = Component.translatable("options.graphics.fabulous.tooltip", Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC));
   private static final Component GRAPHICS_TOOLTIP_FANCY = Component.translatable("options.graphics.fancy.tooltip");
   private final OptionInstance<GraphicsStatus> graphicsMode = new OptionInstance<>("options.graphics", (graphicsstatus4) -> {
      Tooltip var10000;
      switch (graphicsstatus4) {
         case FANCY:
            var10000 = Tooltip.create(GRAPHICS_TOOLTIP_FANCY);
            break;
         case FAST:
            var10000 = Tooltip.create(GRAPHICS_TOOLTIP_FAST);
            break;
         case FABULOUS:
            var10000 = Tooltip.create(GRAPHICS_TOOLTIP_FABULOUS);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }, (component28, graphicsstatus3) -> {
      MutableComponent mutablecomponent = Component.translatable(graphicsstatus3.getKey());
      return graphicsstatus3 == GraphicsStatus.FABULOUS ? mutablecomponent.withStyle(ChatFormatting.ITALIC) : mutablecomponent;
   }, new OptionInstance.AltEnum<>(Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter((graphicsstatus2) -> graphicsstatus2 != GraphicsStatus.FABULOUS).collect(Collectors.toList()), () -> Minecraft.getInstance().isRunning() && Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(), (optioninstance, graphicsstatus1) -> {
      Minecraft minecraft5 = Minecraft.getInstance();
      GpuWarnlistManager gpuwarnlistmanager = minecraft5.getGpuWarnlistManager();
      if (graphicsstatus1 == GraphicsStatus.FABULOUS && gpuwarnlistmanager.willShowWarning()) {
         gpuwarnlistmanager.showWarning();
      } else {
         optioninstance.set(graphicsstatus1);
         minecraft5.levelRenderer.allChanged();
      }
   }, Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)), GraphicsStatus.FANCY, (graphicsstatus) -> {
   });
   private final OptionInstance<Boolean> ambientOcclusion = OptionInstance.createBoolean("options.ao", true, (obool16) -> Minecraft.getInstance().levelRenderer.allChanged());
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_NONE = Component.translatable("options.prioritizeChunkUpdates.none.tooltip");
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED = Component.translatable("options.prioritizeChunkUpdates.byPlayer.tooltip");
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_NEARBY = Component.translatable("options.prioritizeChunkUpdates.nearby.tooltip");
   private final OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates = new OptionInstance<>("options.prioritizeChunkUpdates", (prioritizechunkupdates1) -> {
      Tooltip var10000;
      switch (prioritizechunkupdates1) {
         case NONE:
            var10000 = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NONE);
            break;
         case PLAYER_AFFECTED:
            var10000 = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED);
            break;
         case NEARBY:
            var10000 = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NEARBY);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }, OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(PrioritizeChunkUpdates.values()), Codec.INT.xmap(PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId)), PrioritizeChunkUpdates.NONE, (prioritizechunkupdates) -> {
   });
   public List<String> resourcePacks = Lists.newArrayList();
   public List<String> incompatibleResourcePacks = Lists.newArrayList();
   private final OptionInstance<ChatVisiblity> chatVisibility = new OptionInstance<>("options.chat.visibility", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(ChatVisiblity.values()), Codec.INT.xmap(ChatVisiblity::byId, ChatVisiblity::getId)), ChatVisiblity.FULL, (chatvisiblity) -> {
   });
   private final OptionInstance<Double> chatOpacity = new OptionInstance<>("options.chat.opacity", OptionInstance.noTooltip(), (component27, odouble34) -> percentValueLabel(component27, odouble34 * 0.9D + 0.1D), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble33) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> chatLineSpacing = new OptionInstance<>("options.chat.line_spacing", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.0D, (odouble32) -> {
   });
   private final OptionInstance<Double> textBackgroundOpacity = new OptionInstance<>("options.accessibility.text_background_opacity", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.5D, (odouble31) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> panoramaSpeed = new OptionInstance<>("options.accessibility.panorama_speed", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble30) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_CONTRAST_MODE = Component.translatable("options.accessibility.high_contrast.tooltip");
   private final OptionInstance<Boolean> highContrast = OptionInstance.createBoolean("options.accessibility.high_contrast", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_CONTRAST_MODE), false, (obool15) -> {
      PackRepository packrepository = Minecraft.getInstance().getResourcePackRepository();
      boolean flag2 = packrepository.getSelectedIds().contains("high_contrast");
      if (!flag2 && obool15) {
         if (packrepository.addPack("high_contrast")) {
            this.updateResourcePacks(packrepository);
         }
      } else if (flag2 && !obool15 && packrepository.removePack("high_contrast")) {
         this.updateResourcePacks(packrepository);
      }

   });
   @Nullable
   public String fullscreenVideoModeString;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus = true;
   private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
   private final OptionInstance<HumanoidArm> mainHand = new OptionInstance<>("options.mainHand", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(HumanoidArm.values()), Codec.STRING.xmap((s3) -> "left".equals(s3) ? HumanoidArm.LEFT : HumanoidArm.RIGHT, (humanoidarm1) -> humanoidarm1 == HumanoidArm.LEFT ? "left" : "right")), HumanoidArm.RIGHT, (humanoidarm) -> this.broadcastOptions());
   public int overrideWidth;
   public int overrideHeight;
   private final OptionInstance<Double> chatScale = new OptionInstance<>("options.chat.scale", OptionInstance.noTooltip(), (component26, odouble29) -> (Component)(odouble29 == 0.0D ? CommonComponents.optionStatus(component26, false) : percentValueLabel(component26, odouble29)), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble28) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> chatWidth = new OptionInstance<>("options.chat.width", OptionInstance.noTooltip(), (component25, odouble27) -> pixelValueLabel(component25, ChatComponent.getWidth(odouble27)), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble26) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> chatHeightUnfocused = new OptionInstance<>("options.chat.height.unfocused", OptionInstance.noTooltip(), (component24, odouble25) -> pixelValueLabel(component24, ChatComponent.getHeight(odouble25)), OptionInstance.UnitDouble.INSTANCE, ChatComponent.defaultUnfocusedPct(), (odouble24) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> chatHeightFocused = new OptionInstance<>("options.chat.height.focused", OptionInstance.noTooltip(), (component23, odouble23) -> pixelValueLabel(component23, ChatComponent.getHeight(odouble23)), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble22) -> Minecraft.getInstance().gui.getChat().rescaleChat());
   private final OptionInstance<Double> chatDelay = new OptionInstance<>("options.chat.delay_instant", OptionInstance.noTooltip(), (component22, odouble21) -> odouble21 <= 0.0D ? Component.translatable("options.chat.delay_none") : Component.translatable("options.chat.delay", String.format(Locale.ROOT, "%.1f", odouble21)), (new OptionInstance.IntRange(0, 60)).xmap((l) -> (double)l / 10.0D, (odouble20) -> (int)(odouble20 * 10.0D)), Codec.doubleRange(0.0D, 6.0D), 0.0D, (odouble19) -> Minecraft.getInstance().getChatListener().setMessageDelay(odouble19));
   private static final Component ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME = Component.translatable("options.notifications.display_time.tooltip");
   private final OptionInstance<Double> notificationDisplayTime = new OptionInstance<>("options.notifications.display_time", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME), (component21, odouble18) -> genericValueLabel(component21, Component.translatable("options.multiplier", odouble18)), (new OptionInstance.IntRange(5, 100)).xmap((k) -> (double)k / 10.0D, (odouble17) -> (int)(odouble17 * 10.0D)), Codec.doubleRange(0.5D, 10.0D), 1.0D, (odouble16) -> {
   });
   private final OptionInstance<Integer> mipmapLevels = new OptionInstance<>("options.mipmapLevels", OptionInstance.noTooltip(), (component20, integer12) -> (Component)(integer12 == 0 ? CommonComponents.optionStatus(component20, false) : genericValueLabel(component20, integer12)), new OptionInstance.IntRange(0, 4), 4, (integer11) -> {
   });
   public boolean useNativeTransport = true;
   private final OptionInstance<AttackIndicatorStatus> attackIndicator = new OptionInstance<>("options.attackIndicator", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)), AttackIndicatorStatus.CROSSHAIR, (attackindicatorstatus) -> {
   });
   public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
   public boolean joinedFirstServer = false;
   public boolean hideBundleTutorial = false;
   private final OptionInstance<Integer> biomeBlendRadius = new OptionInstance<>("options.biomeBlendRadius", OptionInstance.noTooltip(), (component19, integer10) -> {
      int j = integer10 * 2 + 1;
      return genericValueLabel(component19, Component.translatable("options.biomeBlendRadius." + j));
   }, new OptionInstance.IntRange(0, 7), 2, (integer9) -> Minecraft.getInstance().levelRenderer.allChanged());
   private final OptionInstance<Double> mouseWheelSensitivity = new OptionInstance<>("options.mouseWheelSensitivity", OptionInstance.noTooltip(), (component18, odouble15) -> genericValueLabel(component18, Component.literal(String.format(Locale.ROOT, "%.2f", odouble15))), (new OptionInstance.IntRange(-200, 100)).xmap(Options::logMouse, Options::unlogMouse), Codec.doubleRange(logMouse(-200), logMouse(100)), logMouse(0), (odouble14) -> {
   });
   private final OptionInstance<Boolean> rawMouseInput = OptionInstance.createBoolean("options.rawMouseInput", true, (obool14) -> {
      Window window = Minecraft.getInstance().getWindow();
      if (window != null) {
         window.updateRawMouseInput(obool14);
      }

   });
   public int glDebugVerbosity = 1;
   private final OptionInstance<Boolean> autoJump = OptionInstance.createBoolean("options.autoJump", false);
   private final OptionInstance<Boolean> operatorItemsTab = OptionInstance.createBoolean("options.operatorItemsTab", false);
   private final OptionInstance<Boolean> autoSuggestions = OptionInstance.createBoolean("options.autoSuggestCommands", true);
   private final OptionInstance<Boolean> chatColors = OptionInstance.createBoolean("options.chat.color", true);
   private final OptionInstance<Boolean> chatLinks = OptionInstance.createBoolean("options.chat.links", true);
   private final OptionInstance<Boolean> chatLinksPrompt = OptionInstance.createBoolean("options.chat.links.prompt", true);
   private final OptionInstance<Boolean> enableVsync = OptionInstance.createBoolean("options.vsync", true, (obool13) -> {
      if (Minecraft.getInstance().getWindow() != null) {
         Minecraft.getInstance().getWindow().updateVsync(obool13);
      }

   });
   private final OptionInstance<Boolean> entityShadows = OptionInstance.createBoolean("options.entityShadows", true);
   private final OptionInstance<Boolean> forceUnicodeFont = OptionInstance.createBoolean("options.forceUnicodeFont", false, (obool12) -> {
      Minecraft minecraft4 = Minecraft.getInstance();
      if (minecraft4.getWindow() != null) {
         minecraft4.selectMainFont(obool12);
         minecraft4.resizeDisplay();
      }

   });
   private final OptionInstance<Boolean> invertYMouse = OptionInstance.createBoolean("options.invertMouse", false);
   private final OptionInstance<Boolean> discreteMouseScroll = OptionInstance.createBoolean("options.discrete_mouse_scroll", false);
   private final OptionInstance<Boolean> realmsNotifications = OptionInstance.createBoolean("options.realmsNotifications", true);
   private static final Component ALLOW_SERVER_LISTING_TOOLTIP = Component.translatable("options.allowServerListing.tooltip");
   private final OptionInstance<Boolean> allowServerListing = OptionInstance.createBoolean("options.allowServerListing", OptionInstance.cachedConstantTooltip(ALLOW_SERVER_LISTING_TOOLTIP), true, (obool11) -> this.broadcastOptions());
   private final OptionInstance<Boolean> reducedDebugInfo = OptionInstance.createBoolean("options.reducedDebugInfo", false);
   private final Map<SoundSource, OptionInstance<Double>> soundSourceVolumes = Util.make(new EnumMap<>(SoundSource.class), (enummap) -> {
      for(SoundSource soundsource : SoundSource.values()) {
         enummap.put(soundsource, this.createSoundSliderOptionInstance("soundCategory." + soundsource.getName(), soundsource));
      }

   });
   private final OptionInstance<Boolean> showSubtitles = OptionInstance.createBoolean("options.showSubtitles", false);
   private static final Component DIRECTIONAL_AUDIO_TOOLTIP_ON = Component.translatable("options.directionalAudio.on.tooltip");
   private static final Component DIRECTIONAL_AUDIO_TOOLTIP_OFF = Component.translatable("options.directionalAudio.off.tooltip");
   private final OptionInstance<Boolean> directionalAudio = OptionInstance.createBoolean("options.directionalAudio", (obool10) -> obool10 ? Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_ON) : Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_OFF), false, (obool9) -> {
      SoundManager soundmanager1 = Minecraft.getInstance().getSoundManager();
      soundmanager1.reload();
      soundmanager1.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   });
   private final OptionInstance<Boolean> backgroundForChatOnly = new OptionInstance<>("options.accessibility.text_background", OptionInstance.noTooltip(), (component17, obool8) -> obool8 ? Component.translatable("options.accessibility.text_background.chat") : Component.translatable("options.accessibility.text_background.everywhere"), OptionInstance.BOOLEAN_VALUES, true, (obool7) -> {
   });
   private final OptionInstance<Boolean> touchscreen = OptionInstance.createBoolean("options.touchscreen", false);
   private final OptionInstance<Boolean> fullscreen = OptionInstance.createBoolean("options.fullscreen", false, (obool6) -> {
      Minecraft minecraft3 = Minecraft.getInstance();
      if (minecraft3.getWindow() != null && minecraft3.getWindow().isFullscreen() != obool6) {
         minecraft3.getWindow().toggleFullScreen();
         this.fullscreen().set(minecraft3.getWindow().isFullscreen());
      }

   });
   private final OptionInstance<Boolean> bobView = OptionInstance.createBoolean("options.viewBobbing", true);
   private static final Component MOVEMENT_TOGGLE = Component.translatable("options.key.toggle");
   private static final Component MOVEMENT_HOLD = Component.translatable("options.key.hold");
   private final OptionInstance<Boolean> toggleCrouch = new OptionInstance<>("key.sneak", OptionInstance.noTooltip(), (component16, obool5) -> obool5 ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, (obool4) -> {
   });
   private final OptionInstance<Boolean> toggleSprint = new OptionInstance<>("key.sprint", OptionInstance.noTooltip(), (component15, obool3) -> obool3 ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, (obool2) -> {
   });
   public boolean skipMultiplayerWarning;
   public boolean skipRealms32bitWarning;
   private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = Component.translatable("options.hideMatchedNames.tooltip");
   private final OptionInstance<Boolean> hideMatchedNames = OptionInstance.createBoolean("options.hideMatchedNames", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_HIDE_MATCHED_NAMES), true);
   private final OptionInstance<Boolean> showAutosaveIndicator = OptionInstance.createBoolean("options.autosaveIndicator", true);
   private static final Component CHAT_TOOLTIP_ONLY_SHOW_SECURE = Component.translatable("options.onlyShowSecureChat.tooltip");
   private final OptionInstance<Boolean> onlyShowSecureChat = OptionInstance.createBoolean("options.onlyShowSecureChat", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_ONLY_SHOW_SECURE), false);
   public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
   public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
   public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
   public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
   public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
   public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", this.toggleCrouch::get);
   public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", this.toggleSprint::get);
   public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
   public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
   public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
   public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
   public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
   public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
   public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
   public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
   public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
   public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
   public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
   public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
   public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
   public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
   public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
   public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
   public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"), new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"), new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"), new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"), new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"), new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"), new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"), new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"), new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")};
   public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
   public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
   public final KeyMapping[] keyMappings = ArrayUtils.addAll((KeyMapping[])(new KeyMapping[]{this.keyAttack, this.keyUse, this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keyShift, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapOffhand, this.keySaveHotbarActivator, this.keyLoadHotbarActivator, this.keyAdvancements}), (KeyMapping[])this.keyHotbarSlots);
   protected Minecraft minecraft;
   private final File optionsFile;
   public boolean hideGui;
   private CameraType cameraType = CameraType.FIRST_PERSON;
   public boolean renderDebug;
   public boolean renderDebugCharts;
   public boolean renderFpsChart;
   public String lastMpIp = "";
   public boolean smoothCamera;
   private final OptionInstance<Integer> fov = new OptionInstance<>("options.fov", OptionInstance.noTooltip(), (component14, integer8) -> {
      Component var10000;
      switch (integer8) {
         case 70:
            var10000 = genericValueLabel(component14, Component.translatable("options.fov.min"));
            break;
         case 110:
            var10000 = genericValueLabel(component14, Component.translatable("options.fov.max"));
            break;
         default:
            var10000 = genericValueLabel(component14, integer8);
      }

      return var10000;
   }, new OptionInstance.IntRange(30, 110), Codec.DOUBLE.xmap((odouble13) -> (int)(odouble13 * 40.0D + 70.0D), (integer7) -> ((double)integer7.intValue() - 70.0D) / 40.0D), 70, (integer6) -> Minecraft.getInstance().levelRenderer.needsUpdate());
   private static final Component TELEMETRY_TOOLTIP = Component.translatable("options.telemetry.button.tooltip", Component.translatable("options.telemetry.state.minimal"), Component.translatable("options.telemetry.state.all"));
   private final OptionInstance<Boolean> telemetryOptInExtra = OptionInstance.createBoolean("options.telemetry.button", OptionInstance.cachedConstantTooltip(TELEMETRY_TOOLTIP), (component13, obool1) -> {
      Minecraft minecraft2 = Minecraft.getInstance();
      if (!minecraft2.allowsTelemetry()) {
         return Component.translatable("options.telemetry.state.none");
      } else {
         return obool1 && minecraft2.extraTelemetryAvailable() ? Component.translatable("options.telemetry.state.all") : Component.translatable("options.telemetry.state.minimal");
      }
   }, false, (obool) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = Component.translatable("options.screenEffectScale.tooltip");
   private final OptionInstance<Double> screenEffectScale = new OptionInstance<>("options.screenEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT), (component12, odouble12) -> odouble12 == 0.0D ? genericValueLabel(component12, CommonComponents.OPTION_OFF) : percentValueLabel(component12, odouble12), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble11) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = Component.translatable("options.fovEffectScale.tooltip");
   private final OptionInstance<Double> fovEffectScale = new OptionInstance<>("options.fovEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_FOV_EFFECT), (component11, odouble10) -> odouble10 == 0.0D ? genericValueLabel(component11, CommonComponents.OPTION_OFF) : percentValueLabel(component11, odouble10), OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), Codec.doubleRange(0.0D, 1.0D), 1.0D, (odouble9) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT = Component.translatable("options.darknessEffectScale.tooltip");
   private final OptionInstance<Double> darknessEffectScale = new OptionInstance<>("options.darknessEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT), (component10, odouble8) -> odouble8 == 0.0D ? genericValueLabel(component10, CommonComponents.OPTION_OFF) : percentValueLabel(component10, odouble8), OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), 1.0D, (odouble7) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_GLINT_SPEED = Component.translatable("options.glintSpeed.tooltip");
   private final OptionInstance<Double> glintSpeed = new OptionInstance<>("options.glintSpeed", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_SPEED), (component9, odouble6) -> odouble6 == 0.0D ? genericValueLabel(component9, CommonComponents.OPTION_OFF) : percentValueLabel(component9, odouble6), OptionInstance.UnitDouble.INSTANCE, 0.5D, (odouble5) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH = Component.translatable("options.glintStrength.tooltip");
   private final OptionInstance<Double> glintStrength = new OptionInstance<>("options.glintStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH), (component8, odouble4) -> odouble4 == 0.0D ? genericValueLabel(component8, CommonComponents.OPTION_OFF) : percentValueLabel(component8, odouble4), OptionInstance.UnitDouble.INSTANCE, 0.75D, RenderSystem::setShaderGlintAlpha);
   private static final Component ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH = Component.translatable("options.damageTiltStrength.tooltip");
   private final OptionInstance<Double> damageTiltStrength = new OptionInstance<>("options.damageTiltStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH), (component7, odouble3) -> odouble3 == 0.0D ? genericValueLabel(component7, CommonComponents.OPTION_OFF) : percentValueLabel(component7, odouble3), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble2) -> {
   });
   private final OptionInstance<Double> gamma = new OptionInstance<>("options.gamma", OptionInstance.noTooltip(), (component6, odouble1) -> {
      int i = (int)(odouble1 * 100.0D);
      if (i == 0) {
         return genericValueLabel(component6, Component.translatable("options.gamma.min"));
      } else if (i == 50) {
         return genericValueLabel(component6, Component.translatable("options.gamma.default"));
      } else {
         return i == 100 ? genericValueLabel(component6, Component.translatable("options.gamma.max")) : genericValueLabel(component6, i);
      }
   }, OptionInstance.UnitDouble.INSTANCE, 0.5D, (odouble) -> {
   });
   public static final int AUTO_GUI_SCALE = 0;
   private static final int MAX_GUI_SCALE_INCLUSIVE = 2147483646;
   private final OptionInstance<Integer> guiScale = new OptionInstance<>("options.guiScale", OptionInstance.noTooltip(), (component5, integer5) -> integer5 == 0 ? Component.translatable("options.guiScale.auto") : Component.literal(Integer.toString(integer5)), new OptionInstance.ClampingLazyMaxIntRange(0, () -> {
      Minecraft minecraft1 = Minecraft.getInstance();
      return !minecraft1.isRunning() ? 2147483646 : minecraft1.getWindow().calculateScale(0, minecraft1.isEnforceUnicode());
   }, 2147483646), 0, (integer4) -> {
   });
   private final OptionInstance<ParticleStatus> particles = new OptionInstance<>("options.particles", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(ParticleStatus.values()), Codec.INT.xmap(ParticleStatus::byId, ParticleStatus::getId)), ParticleStatus.ALL, (particlestatus) -> {
   });
   private final OptionInstance<NarratorStatus> narrator = new OptionInstance<>("options.narrator", OptionInstance.noTooltip(), (component4, narratorstatus1) -> (Component)(this.minecraft.getNarrator().isActive() ? narratorstatus1.getName() : Component.translatable("options.narrator.notavailable")), new OptionInstance.Enum<>(Arrays.asList(NarratorStatus.values()), Codec.INT.xmap(NarratorStatus::byId, NarratorStatus::getId)), NarratorStatus.OFF, (narratorstatus) -> this.minecraft.getNarrator().updateNarratorStatus(narratorstatus));
   public String languageCode = "en_us";
   private final OptionInstance<String> soundDevice = new OptionInstance<>("options.audioDevice", OptionInstance.noTooltip(), (component3, s2) -> {
      if ("".equals(s2)) {
         return Component.translatable("options.audioDevice.default");
      } else {
         return s2.startsWith("OpenAL Soft on ") ? Component.literal(s2.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH)) : Component.literal(s2);
      }
   }, new OptionInstance.LazyEnum<>(() -> Stream.concat(Stream.of(""), Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream()).toList(), (s1) -> Minecraft.getInstance().isRunning() && s1 != "" && !Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().contains(s1) ? Optional.empty() : Optional.of(s1), Codec.STRING), "", (s) -> {
      SoundManager soundmanager = Minecraft.getInstance().getSoundManager();
      soundmanager.reload();
      soundmanager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   });
   public boolean onboardAccessibility = true;
   public boolean syncWrites;

   public OptionInstance<Boolean> darkMojangStudiosBackground() {
      return this.darkMojangStudiosBackground;
   }

   public OptionInstance<Boolean> hideLightningFlash() {
      return this.hideLightningFlash;
   }

   public OptionInstance<Double> sensitivity() {
      return this.sensitivity;
   }

   public OptionInstance<Integer> renderDistance() {
      return this.renderDistance;
   }

   public OptionInstance<Integer> simulationDistance() {
      return this.simulationDistance;
   }

   public OptionInstance<Double> entityDistanceScaling() {
      return this.entityDistanceScaling;
   }

   public OptionInstance<Integer> framerateLimit() {
      return this.framerateLimit;
   }

   public OptionInstance<CloudStatus> cloudStatus() {
      return this.cloudStatus;
   }

   public OptionInstance<GraphicsStatus> graphicsMode() {
      return this.graphicsMode;
   }

   public OptionInstance<Boolean> ambientOcclusion() {
      return this.ambientOcclusion;
   }

   public OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates() {
      return this.prioritizeChunkUpdates;
   }

   public void updateResourcePacks(PackRepository packrepository) {
      List<String> list = ImmutableList.copyOf(this.resourcePacks);
      this.resourcePacks.clear();
      this.incompatibleResourcePacks.clear();

      for(Pack pack : packrepository.getSelectedPacks()) {
         if (!pack.isFixedPosition()) {
            this.resourcePacks.add(pack.getId());
            if (!pack.getCompatibility().isCompatible()) {
               this.incompatibleResourcePacks.add(pack.getId());
            }
         }
      }

      this.save();
      List<String> list1 = ImmutableList.copyOf(this.resourcePacks);
      if (!list1.equals(list)) {
         this.minecraft.reloadResourcePacks();
      }

   }

   public OptionInstance<ChatVisiblity> chatVisibility() {
      return this.chatVisibility;
   }

   public OptionInstance<Double> chatOpacity() {
      return this.chatOpacity;
   }

   public OptionInstance<Double> chatLineSpacing() {
      return this.chatLineSpacing;
   }

   public OptionInstance<Double> textBackgroundOpacity() {
      return this.textBackgroundOpacity;
   }

   public OptionInstance<Double> panoramaSpeed() {
      return this.panoramaSpeed;
   }

   public OptionInstance<Boolean> highContrast() {
      return this.highContrast;
   }

   public OptionInstance<HumanoidArm> mainHand() {
      return this.mainHand;
   }

   public OptionInstance<Double> chatScale() {
      return this.chatScale;
   }

   public OptionInstance<Double> chatWidth() {
      return this.chatWidth;
   }

   public OptionInstance<Double> chatHeightUnfocused() {
      return this.chatHeightUnfocused;
   }

   public OptionInstance<Double> chatHeightFocused() {
      return this.chatHeightFocused;
   }

   public OptionInstance<Double> chatDelay() {
      return this.chatDelay;
   }

   public OptionInstance<Double> notificationDisplayTime() {
      return this.notificationDisplayTime;
   }

   public OptionInstance<Integer> mipmapLevels() {
      return this.mipmapLevels;
   }

   public OptionInstance<AttackIndicatorStatus> attackIndicator() {
      return this.attackIndicator;
   }

   public OptionInstance<Integer> biomeBlendRadius() {
      return this.biomeBlendRadius;
   }

   private static double logMouse(int i) {
      return Math.pow(10.0D, (double)i / 100.0D);
   }

   private static int unlogMouse(double d0) {
      return Mth.floor(Math.log10(d0) * 100.0D);
   }

   public OptionInstance<Double> mouseWheelSensitivity() {
      return this.mouseWheelSensitivity;
   }

   public OptionInstance<Boolean> rawMouseInput() {
      return this.rawMouseInput;
   }

   public OptionInstance<Boolean> autoJump() {
      return this.autoJump;
   }

   public OptionInstance<Boolean> operatorItemsTab() {
      return this.operatorItemsTab;
   }

   public OptionInstance<Boolean> autoSuggestions() {
      return this.autoSuggestions;
   }

   public OptionInstance<Boolean> chatColors() {
      return this.chatColors;
   }

   public OptionInstance<Boolean> chatLinks() {
      return this.chatLinks;
   }

   public OptionInstance<Boolean> chatLinksPrompt() {
      return this.chatLinksPrompt;
   }

   public OptionInstance<Boolean> enableVsync() {
      return this.enableVsync;
   }

   public OptionInstance<Boolean> entityShadows() {
      return this.entityShadows;
   }

   public OptionInstance<Boolean> forceUnicodeFont() {
      return this.forceUnicodeFont;
   }

   public OptionInstance<Boolean> invertYMouse() {
      return this.invertYMouse;
   }

   public OptionInstance<Boolean> discreteMouseScroll() {
      return this.discreteMouseScroll;
   }

   public OptionInstance<Boolean> realmsNotifications() {
      return this.realmsNotifications;
   }

   public OptionInstance<Boolean> allowServerListing() {
      return this.allowServerListing;
   }

   public OptionInstance<Boolean> reducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public final float getSoundSourceVolume(SoundSource soundsource) {
      return this.getSoundSourceOptionInstance(soundsource).get().floatValue();
   }

   public final OptionInstance<Double> getSoundSourceOptionInstance(SoundSource soundsource) {
      return Objects.requireNonNull(this.soundSourceVolumes.get(soundsource));
   }

   private OptionInstance<Double> createSoundSliderOptionInstance(String s, SoundSource soundsource) {
      return new OptionInstance<>(s, OptionInstance.noTooltip(), (component, odouble1) -> odouble1 == 0.0D ? genericValueLabel(component, CommonComponents.OPTION_OFF) : percentValueLabel(component, odouble1), OptionInstance.UnitDouble.INSTANCE, 1.0D, (odouble) -> Minecraft.getInstance().getSoundManager().updateSourceVolume(soundsource, odouble.floatValue()));
   }

   public OptionInstance<Boolean> showSubtitles() {
      return this.showSubtitles;
   }

   public OptionInstance<Boolean> directionalAudio() {
      return this.directionalAudio;
   }

   public OptionInstance<Boolean> backgroundForChatOnly() {
      return this.backgroundForChatOnly;
   }

   public OptionInstance<Boolean> touchscreen() {
      return this.touchscreen;
   }

   public OptionInstance<Boolean> fullscreen() {
      return this.fullscreen;
   }

   public OptionInstance<Boolean> bobView() {
      return this.bobView;
   }

   public OptionInstance<Boolean> toggleCrouch() {
      return this.toggleCrouch;
   }

   public OptionInstance<Boolean> toggleSprint() {
      return this.toggleSprint;
   }

   public OptionInstance<Boolean> hideMatchedNames() {
      return this.hideMatchedNames;
   }

   public OptionInstance<Boolean> showAutosaveIndicator() {
      return this.showAutosaveIndicator;
   }

   public OptionInstance<Boolean> onlyShowSecureChat() {
      return this.onlyShowSecureChat;
   }

   public OptionInstance<Integer> fov() {
      return this.fov;
   }

   public OptionInstance<Boolean> telemetryOptInExtra() {
      return this.telemetryOptInExtra;
   }

   public OptionInstance<Double> screenEffectScale() {
      return this.screenEffectScale;
   }

   public OptionInstance<Double> fovEffectScale() {
      return this.fovEffectScale;
   }

   public OptionInstance<Double> darknessEffectScale() {
      return this.darknessEffectScale;
   }

   public OptionInstance<Double> glintSpeed() {
      return this.glintSpeed;
   }

   public OptionInstance<Double> glintStrength() {
      return this.glintStrength;
   }

   public OptionInstance<Double> damageTiltStrength() {
      return this.damageTiltStrength;
   }

   public OptionInstance<Double> gamma() {
      return this.gamma;
   }

   public OptionInstance<Integer> guiScale() {
      return this.guiScale;
   }

   public OptionInstance<ParticleStatus> particles() {
      return this.particles;
   }

   public OptionInstance<NarratorStatus> narrator() {
      return this.narrator;
   }

   public OptionInstance<String> soundDevice() {
      return this.soundDevice;
   }

   public Options(Minecraft minecraft, File file) {
      this.minecraft = minecraft;
      this.optionsFile = new File(file, "options.txt");
      boolean flag = minecraft.is64Bit();
      boolean flag1 = flag && Runtime.getRuntime().maxMemory() >= 1000000000L;
      this.renderDistance = new OptionInstance<>("options.renderDistance", OptionInstance.noTooltip(), (component2, integer3) -> genericValueLabel(component2, Component.translatable("options.chunks", integer3)), new OptionInstance.IntRange(2, flag1 ? 32 : 16), flag ? 12 : 8, (integer2) -> Minecraft.getInstance().levelRenderer.needsUpdate());
      this.simulationDistance = new OptionInstance<>("options.simulationDistance", OptionInstance.noTooltip(), (component1, integer1) -> genericValueLabel(component1, Component.translatable("options.chunks", integer1)), new OptionInstance.IntRange(5, flag1 ? 32 : 16), flag ? 12 : 8, (integer) -> {
      });
      this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
      this.load();
   }

   public float getBackgroundOpacity(float f) {
      return this.backgroundForChatOnly.get() ? f : this.textBackgroundOpacity().get().floatValue();
   }

   public int getBackgroundColor(float f) {
      return (int)(this.getBackgroundOpacity(f) * 255.0F) << 24 & -16777216;
   }

   public int getBackgroundColor(int i) {
      return this.backgroundForChatOnly.get() ? i : (int)(this.textBackgroundOpacity.get() * 255.0D) << 24 & -16777216;
   }

   public void setKey(KeyMapping keymapping, InputConstants.Key inputconstants_key) {
      keymapping.setKey(inputconstants_key);
      this.save();
   }

   private void processOptions(Options.FieldAccess options_fieldaccess) {
      options_fieldaccess.process("autoJump", this.autoJump);
      options_fieldaccess.process("operatorItemsTab", this.operatorItemsTab);
      options_fieldaccess.process("autoSuggestions", this.autoSuggestions);
      options_fieldaccess.process("chatColors", this.chatColors);
      options_fieldaccess.process("chatLinks", this.chatLinks);
      options_fieldaccess.process("chatLinksPrompt", this.chatLinksPrompt);
      options_fieldaccess.process("enableVsync", this.enableVsync);
      options_fieldaccess.process("entityShadows", this.entityShadows);
      options_fieldaccess.process("forceUnicodeFont", this.forceUnicodeFont);
      options_fieldaccess.process("discrete_mouse_scroll", this.discreteMouseScroll);
      options_fieldaccess.process("invertYMouse", this.invertYMouse);
      options_fieldaccess.process("realmsNotifications", this.realmsNotifications);
      options_fieldaccess.process("reducedDebugInfo", this.reducedDebugInfo);
      options_fieldaccess.process("showSubtitles", this.showSubtitles);
      options_fieldaccess.process("directionalAudio", this.directionalAudio);
      options_fieldaccess.process("touchscreen", this.touchscreen);
      options_fieldaccess.process("fullscreen", this.fullscreen);
      options_fieldaccess.process("bobView", this.bobView);
      options_fieldaccess.process("toggleCrouch", this.toggleCrouch);
      options_fieldaccess.process("toggleSprint", this.toggleSprint);
      options_fieldaccess.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
      options_fieldaccess.process("hideLightningFlashes", this.hideLightningFlash);
      options_fieldaccess.process("mouseSensitivity", this.sensitivity);
      options_fieldaccess.process("fov", this.fov);
      options_fieldaccess.process("screenEffectScale", this.screenEffectScale);
      options_fieldaccess.process("fovEffectScale", this.fovEffectScale);
      options_fieldaccess.process("darknessEffectScale", this.darknessEffectScale);
      options_fieldaccess.process("glintSpeed", this.glintSpeed);
      options_fieldaccess.process("glintStrength", this.glintStrength);
      options_fieldaccess.process("damageTiltStrength", this.damageTiltStrength);
      options_fieldaccess.process("highContrast", this.highContrast);
      options_fieldaccess.process("gamma", this.gamma);
      options_fieldaccess.process("renderDistance", this.renderDistance);
      options_fieldaccess.process("simulationDistance", this.simulationDistance);
      options_fieldaccess.process("entityDistanceScaling", this.entityDistanceScaling);
      options_fieldaccess.process("guiScale", this.guiScale);
      options_fieldaccess.process("particles", this.particles);
      options_fieldaccess.process("maxFps", this.framerateLimit);
      options_fieldaccess.process("graphicsMode", this.graphicsMode);
      options_fieldaccess.process("ao", this.ambientOcclusion);
      options_fieldaccess.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates);
      options_fieldaccess.process("biomeBlendRadius", this.biomeBlendRadius);
      options_fieldaccess.process("renderClouds", this.cloudStatus);
      this.resourcePacks = options_fieldaccess.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
      this.incompatibleResourcePacks = options_fieldaccess.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
      this.lastMpIp = options_fieldaccess.process("lastServer", this.lastMpIp);
      this.languageCode = options_fieldaccess.process("lang", this.languageCode);
      options_fieldaccess.process("soundDevice", this.soundDevice);
      options_fieldaccess.process("chatVisibility", this.chatVisibility);
      options_fieldaccess.process("chatOpacity", this.chatOpacity);
      options_fieldaccess.process("chatLineSpacing", this.chatLineSpacing);
      options_fieldaccess.process("textBackgroundOpacity", this.textBackgroundOpacity);
      options_fieldaccess.process("backgroundForChatOnly", this.backgroundForChatOnly);
      this.hideServerAddress = options_fieldaccess.process("hideServerAddress", this.hideServerAddress);
      this.advancedItemTooltips = options_fieldaccess.process("advancedItemTooltips", this.advancedItemTooltips);
      this.pauseOnLostFocus = options_fieldaccess.process("pauseOnLostFocus", this.pauseOnLostFocus);
      this.overrideWidth = options_fieldaccess.process("overrideWidth", this.overrideWidth);
      this.overrideHeight = options_fieldaccess.process("overrideHeight", this.overrideHeight);
      options_fieldaccess.process("chatHeightFocused", this.chatHeightFocused);
      options_fieldaccess.process("chatDelay", this.chatDelay);
      options_fieldaccess.process("chatHeightUnfocused", this.chatHeightUnfocused);
      options_fieldaccess.process("chatScale", this.chatScale);
      options_fieldaccess.process("chatWidth", this.chatWidth);
      options_fieldaccess.process("notificationDisplayTime", this.notificationDisplayTime);
      options_fieldaccess.process("mipmapLevels", this.mipmapLevels);
      this.useNativeTransport = options_fieldaccess.process("useNativeTransport", this.useNativeTransport);
      options_fieldaccess.process("mainHand", this.mainHand);
      options_fieldaccess.process("attackIndicator", this.attackIndicator);
      options_fieldaccess.process("narrator", this.narrator);
      this.tutorialStep = options_fieldaccess.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
      options_fieldaccess.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
      options_fieldaccess.process("rawMouseInput", this.rawMouseInput);
      this.glDebugVerbosity = options_fieldaccess.process("glDebugVerbosity", this.glDebugVerbosity);
      this.skipMultiplayerWarning = options_fieldaccess.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
      this.skipRealms32bitWarning = options_fieldaccess.process("skipRealms32bitWarning", this.skipRealms32bitWarning);
      options_fieldaccess.process("hideMatchedNames", this.hideMatchedNames);
      this.joinedFirstServer = options_fieldaccess.process("joinedFirstServer", this.joinedFirstServer);
      this.hideBundleTutorial = options_fieldaccess.process("hideBundleTutorial", this.hideBundleTutorial);
      this.syncWrites = options_fieldaccess.process("syncChunkWrites", this.syncWrites);
      options_fieldaccess.process("showAutosaveIndicator", this.showAutosaveIndicator);
      options_fieldaccess.process("allowServerListing", this.allowServerListing);
      options_fieldaccess.process("onlyShowSecureChat", this.onlyShowSecureChat);
      options_fieldaccess.process("panoramaScrollSpeed", this.panoramaSpeed);
      options_fieldaccess.process("telemetryOptInExtra", this.telemetryOptInExtra);
      this.onboardAccessibility = options_fieldaccess.process("onboardAccessibility", this.onboardAccessibility);

      for(KeyMapping keymapping : this.keyMappings) {
         String s = keymapping.saveString();
         String s1 = options_fieldaccess.process("key_" + keymapping.getName(), s);
         if (!s.equals(s1)) {
            keymapping.setKey(InputConstants.getKey(s1));
         }
      }

      for(SoundSource soundsource : SoundSource.values()) {
         options_fieldaccess.process("soundCategory_" + soundsource.getName(), this.soundSourceVolumes.get(soundsource));
      }

      for(PlayerModelPart playermodelpart : PlayerModelPart.values()) {
         boolean flag = this.modelParts.contains(playermodelpart);
         boolean flag1 = options_fieldaccess.process("modelPart_" + playermodelpart.getId(), flag);
         if (flag1 != flag) {
            this.setModelPart(playermodelpart, flag1);
         }
      }

   }

   public void load() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         CompoundTag compoundtag = new CompoundTag();
         BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8);

         try {
            bufferedreader.lines().forEach((s) -> {
               try {
                  Iterator<String> iterator = OPTION_SPLITTER.split(s).iterator();
                  compoundtag.putString(iterator.next(), iterator.next());
               } catch (Exception var3) {
                  LOGGER.warn("Skipping bad option: {}", (Object)s);
               }

            });
         } catch (Throwable var6) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

         final CompoundTag compoundtag1 = this.dataFix(compoundtag);
         if (!compoundtag1.contains("graphicsMode") && compoundtag1.contains("fancyGraphics")) {
            if (isTrue(compoundtag1.getString("fancyGraphics"))) {
               this.graphicsMode.set(GraphicsStatus.FANCY);
            } else {
               this.graphicsMode.set(GraphicsStatus.FAST);
            }
         }

         this.processOptions(new Options.FieldAccess() {
            @Nullable
            private String getValueOrNull(String s) {
               return compoundtag1.contains(s) ? compoundtag1.getString(s) : null;
            }

            public <T> void process(String s, OptionInstance<T> optioninstance) {
               String s1 = this.getValueOrNull(s);
               if (s1 != null) {
                  JsonReader jsonreader = new JsonReader(new StringReader(s1.isEmpty() ? "\"\"" : s1));
                  JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                  DataResult<T> dataresult = optioninstance.codec().parse(JsonOps.INSTANCE, jsonelement);
                  dataresult.error().ifPresent((dataresult_partialresult) -> Options.LOGGER.error("Error parsing option value " + s1 + " for option " + optioninstance + ": " + dataresult_partialresult.message()));
                  dataresult.result().ifPresent(optioninstance::set);
               }

            }

            public int process(String s, int i) {
               String s1 = this.getValueOrNull(s);
               if (s1 != null) {
                  try {
                     return Integer.parseInt(s1);
                  } catch (NumberFormatException var5) {
                     Options.LOGGER.warn("Invalid integer value for option {} = {}", s, s1, var5);
                  }
               }

               return i;
            }

            public boolean process(String s, boolean flag) {
               String s1 = this.getValueOrNull(s);
               return s1 != null ? Options.isTrue(s1) : flag;
            }

            public String process(String s, String s1) {
               return MoreObjects.firstNonNull(this.getValueOrNull(s), s1);
            }

            public float process(String s, float f) {
               String s1 = this.getValueOrNull(s);
               if (s1 != null) {
                  if (Options.isTrue(s1)) {
                     return 1.0F;
                  }

                  if (Options.isFalse(s1)) {
                     return 0.0F;
                  }

                  try {
                     return Float.parseFloat(s1);
                  } catch (NumberFormatException var5) {
                     Options.LOGGER.warn("Invalid floating point value for option {} = {}", s, s1, var5);
                  }
               }

               return f;
            }

            public <T> T process(String s, T object, Function<String, T> function, Function<T, String> function1) {
               String s1 = this.getValueOrNull(s);
               return (T)(s1 == null ? object : function.apply(s1));
            }
         });
         if (compoundtag1.contains("fullscreenResolution")) {
            this.fullscreenVideoModeString = compoundtag1.getString("fullscreenResolution");
         }

         if (this.minecraft.getWindow() != null) {
            this.minecraft.getWindow().setFramerateLimit(this.framerateLimit.get());
         }

         KeyMapping.resetMapping();
      } catch (Exception var7) {
         LOGGER.error("Failed to load options", (Throwable)var7);
      }

   }

   static boolean isTrue(String s) {
      return "true".equals(s);
   }

   static boolean isFalse(String s) {
      return "false".equals(s);
   }

   private CompoundTag dataFix(CompoundTag compoundtag) {
      int i = 0;

      try {
         i = Integer.parseInt(compoundtag.getString("version"));
      } catch (RuntimeException var4) {
      }

      return DataFixTypes.OPTIONS.updateToCurrentVersion(this.minecraft.getFixerUpper(), compoundtag, i);
   }

   public void save() {
      try {
         final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));

         try {
            printwriter.println("version:" + SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            this.processOptions(new Options.FieldAccess() {
               public void writePrefix(String s) {
                  printwriter.print(s);
                  printwriter.print(':');
               }

               public <T> void process(String s, OptionInstance<T> optioninstance) {
                  DataResult<JsonElement> dataresult = optioninstance.codec().encodeStart(JsonOps.INSTANCE, optioninstance.get());
                  dataresult.error().ifPresent((dataresult_partialresult) -> Options.LOGGER.error("Error saving option " + optioninstance + ": " + dataresult_partialresult));
                  dataresult.result().ifPresent((jsonelement) -> {
                     this.writePrefix(s);
                     printwriter.println(Options.GSON.toJson(jsonelement));
                  });
               }

               public int process(String s, int i) {
                  this.writePrefix(s);
                  printwriter.println(i);
                  return i;
               }

               public boolean process(String s, boolean flag) {
                  this.writePrefix(s);
                  printwriter.println(flag);
                  return flag;
               }

               public String process(String s, String s1) {
                  this.writePrefix(s);
                  printwriter.println(s1);
                  return s1;
               }

               public float process(String s, float f) {
                  this.writePrefix(s);
                  printwriter.println(f);
                  return f;
               }

               public <T> T process(String s, T object, Function<String, T> function, Function<T, String> function1) {
                  this.writePrefix(s);
                  printwriter.println(function1.apply(object));
                  return object;
               }
            });
            if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
               printwriter.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
            }
         } catch (Throwable var5) {
            try {
               printwriter.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         printwriter.close();
      } catch (Exception var6) {
         LOGGER.error("Failed to save options", (Throwable)var6);
      }

      this.broadcastOptions();
   }

   public void broadcastOptions() {
      if (this.minecraft.player != null) {
         int i = 0;

         for(PlayerModelPart playermodelpart : this.modelParts) {
            i |= playermodelpart.getMask();
         }

         this.minecraft.player.connection.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance.get(), this.chatVisibility.get(), this.chatColors.get(), i, this.mainHand.get(), this.minecraft.isTextFilteringEnabled(), this.allowServerListing.get()));
      }

   }

   private void setModelPart(PlayerModelPart playermodelpart, boolean flag) {
      if (flag) {
         this.modelParts.add(playermodelpart);
      } else {
         this.modelParts.remove(playermodelpart);
      }

   }

   public boolean isModelPartEnabled(PlayerModelPart playermodelpart) {
      return this.modelParts.contains(playermodelpart);
   }

   public void toggleModelPart(PlayerModelPart playermodelpart, boolean flag) {
      this.setModelPart(playermodelpart, flag);
      this.broadcastOptions();
   }

   public CloudStatus getCloudsType() {
      return this.getEffectiveRenderDistance() >= 4 ? this.cloudStatus.get() : CloudStatus.OFF;
   }

   public boolean useNativeTransport() {
      return this.useNativeTransport;
   }

   public void loadSelectedResourcePacks(PackRepository packrepository) {
      Set<String> set = Sets.newLinkedHashSet();
      Iterator<String> iterator = this.resourcePacks.iterator();

      while(iterator.hasNext()) {
         String s = iterator.next();
         Pack pack = packrepository.getPack(s);
         if (pack == null && !s.startsWith("file/")) {
            pack = packrepository.getPack("file/" + s);
         }

         if (pack == null) {
            LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)s);
            iterator.remove();
         } else if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(s)) {
            LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)s);
            iterator.remove();
         } else if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(s)) {
            LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)s);
            this.incompatibleResourcePacks.remove(s);
         } else {
            set.add(pack.getId());
         }
      }

      packrepository.setSelected(set);
   }

   public CameraType getCameraType() {
      return this.cameraType;
   }

   public void setCameraType(CameraType cameratype) {
      this.cameraType = cameratype;
   }

   private static List<String> readPackList(String s2) {
      List<String> list = GsonHelper.fromNullableJson(GSON, s2, RESOURCE_PACK_TYPE);
      return (List<String>)(list != null ? list : Lists.newArrayList());
   }

   public File getFile() {
      return this.optionsFile;
   }

   public String dumpOptionsForReport() {
      Stream<Pair<String, Object>> stream = Stream.<Pair<String, Object>>builder().add(Pair.of("ao", this.ambientOcclusion.get())).add(Pair.of("biomeBlendRadius", this.biomeBlendRadius.get())).add(Pair.of("enableVsync", this.enableVsync.get())).add(Pair.of("entityDistanceScaling", this.entityDistanceScaling.get())).add(Pair.of("entityShadows", this.entityShadows.get())).add(Pair.of("forceUnicodeFont", this.forceUnicodeFont.get())).add(Pair.of("fov", this.fov.get())).add(Pair.of("fovEffectScale", this.fovEffectScale.get())).add(Pair.of("darknessEffectScale", this.darknessEffectScale.get())).add(Pair.of("glintSpeed", this.glintSpeed.get())).add(Pair.of("glintStrength", this.glintStrength.get())).add(Pair.of("prioritizeChunkUpdates", this.prioritizeChunkUpdates.get())).add(Pair.of("fullscreen", this.fullscreen.get())).add(Pair.of("fullscreenResolution", String.valueOf((Object)this.fullscreenVideoModeString))).add(Pair.of("gamma", this.gamma.get())).add(Pair.of("glDebugVerbosity", this.glDebugVerbosity)).add(Pair.of("graphicsMode", this.graphicsMode.get())).add(Pair.of("guiScale", this.guiScale.get())).add(Pair.of("maxFps", this.framerateLimit.get())).add(Pair.of("mipmapLevels", this.mipmapLevels.get())).add(Pair.of("narrator", this.narrator.get())).add(Pair.of("overrideHeight", this.overrideHeight)).add(Pair.of("overrideWidth", this.overrideWidth)).add(Pair.of("particles", this.particles.get())).add(Pair.of("reducedDebugInfo", this.reducedDebugInfo.get())).add(Pair.of("renderClouds", this.cloudStatus.get())).add(Pair.of("renderDistance", this.renderDistance.get())).add(Pair.of("simulationDistance", this.simulationDistance.get())).add(Pair.of("resourcePacks", this.resourcePacks)).add(Pair.of("screenEffectScale", this.screenEffectScale.get())).add(Pair.of("syncChunkWrites", this.syncWrites)).add(Pair.of("useNativeTransport", this.useNativeTransport)).add(Pair.of("soundDevice", this.soundDevice.get())).build();
      return stream.map((pair) -> (String)pair.getFirst() + ": " + pair.getSecond()).collect(Collectors.joining(System.lineSeparator()));
   }

   public void setServerRenderDistance(int i) {
      this.serverRenderDistance = i;
   }

   public int getEffectiveRenderDistance() {
      return this.serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), this.serverRenderDistance) : this.renderDistance.get();
   }

   private static Component pixelValueLabel(Component component, int i) {
      return Component.translatable("options.pixel_value", component, i);
   }

   private static Component percentValueLabel(Component component, double d0) {
      return Component.translatable("options.percent_value", component, (int)(d0 * 100.0D));
   }

   public static Component genericValueLabel(Component component, Component component1) {
      return Component.translatable("options.generic_value", component, component1);
   }

   public static Component genericValueLabel(Component component, int i) {
      return genericValueLabel(component, Component.literal(Integer.toString(i)));
   }

   interface FieldAccess {
      <T> void process(String s, OptionInstance<T> optioninstance);

      int process(String s, int i);

      boolean process(String s, boolean flag);

      String process(String s, String s1);

      float process(String s, float f);

      <T> T process(String s, T object, Function<String, T> function, Function<T, String> function1);
   }
}
