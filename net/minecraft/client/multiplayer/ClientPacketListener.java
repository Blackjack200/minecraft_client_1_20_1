package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ClientPacketListener implements TickablePacketListener, ClientGamePacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
   private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
   private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
   private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
   private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
   private static final int PENDING_OFFSET_THRESHOLD = 64;
   private final Connection connection;
   private final List<ClientPacketListener.DeferredPacket> deferredPackets = new ArrayList<>();
   @Nullable
   private final ServerData serverData;
   private final GameProfile localGameProfile;
   private final Screen callbackScreen;
   private final Minecraft minecraft;
   private ClientLevel level;
   private ClientLevel.ClientLevelData levelData;
   private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
   private final Set<PlayerInfo> listedPlayers = new ReferenceOpenHashSet<>();
   private final ClientAdvancements advancements;
   private final ClientSuggestionProvider suggestionsProvider;
   private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
   private int serverChunkRadius = 3;
   private int serverSimulationDistance = 3;
   private final RandomSource random = RandomSource.createThreadSafe();
   private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
   private final RecipeManager recipeManager = new RecipeManager();
   private final UUID id = UUID.randomUUID();
   private Set<ResourceKey<Level>> levels;
   private LayeredRegistryAccess<ClientRegistryLayer> registryAccess = ClientRegistryLayer.createRegistryAccess();
   private FeatureFlagSet enabledFeatures = FeatureFlags.DEFAULT_FLAGS;
   private final WorldSessionTelemetryManager telemetryManager;
   @Nullable
   private LocalChatSession chatSession;
   private SignedMessageChain.Encoder signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
   private LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
   private MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();

   public ClientPacketListener(Minecraft minecraft, Screen screen, Connection connection, @Nullable ServerData serverdata, GameProfile gameprofile, WorldSessionTelemetryManager worldsessiontelemetrymanager) {
      this.minecraft = minecraft;
      this.callbackScreen = screen;
      this.connection = connection;
      this.serverData = serverdata;
      this.localGameProfile = gameprofile;
      this.advancements = new ClientAdvancements(minecraft, worldsessiontelemetrymanager);
      this.suggestionsProvider = new ClientSuggestionProvider(this, minecraft);
      this.telemetryManager = worldsessiontelemetrymanager;
   }

   public ClientSuggestionProvider getSuggestionsProvider() {
      return this.suggestionsProvider;
   }

   public void close() {
      this.level = null;
      this.telemetryManager.onDisconnect();
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public void handleLogin(ClientboundLoginPacket clientboundloginpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundloginpacket, this, this.minecraft);
      this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
      this.registryAccess = this.registryAccess.replaceFrom(ClientRegistryLayer.REMOTE, clientboundloginpacket.registryHolder());
      if (!this.connection.isMemoryConnection()) {
         this.registryAccess.compositeAccess().registries().forEach((registryaccess_registryentry) -> registryaccess_registryentry.value().resetTags());
      }

      List<ResourceKey<Level>> list = Lists.newArrayList(clientboundloginpacket.levels());
      Collections.shuffle(list);
      this.levels = Sets.newLinkedHashSet(list);
      ResourceKey<Level> resourcekey = clientboundloginpacket.dimension();
      Holder<DimensionType> holder = this.registryAccess.compositeAccess().<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(clientboundloginpacket.dimensionType());
      this.serverChunkRadius = clientboundloginpacket.chunkRadius();
      this.serverSimulationDistance = clientboundloginpacket.simulationDistance();
      boolean flag = clientboundloginpacket.isDebug();
      boolean flag1 = clientboundloginpacket.isFlat();
      ClientLevel.ClientLevelData clientlevel_clientleveldata = new ClientLevel.ClientLevelData(Difficulty.NORMAL, clientboundloginpacket.hardcore(), flag1);
      this.levelData = clientlevel_clientleveldata;
      this.level = new ClientLevel(this, clientlevel_clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, clientboundloginpacket.seed());
      this.minecraft.setLevel(this.level);
      if (this.minecraft.player == null) {
         this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
         this.minecraft.player.setYRot(-180.0F);
         if (this.minecraft.getSingleplayerServer() != null) {
            this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
         }
      }

      this.minecraft.debugRenderer.clear();
      this.minecraft.player.resetPos();
      int i = clientboundloginpacket.playerId();
      this.minecraft.player.setId(i);
      this.level.addPlayer(i, this.minecraft.player);
      this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
      this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
      this.minecraft.cameraEntity = this.minecraft.player;
      this.minecraft.setScreen(new ReceivingLevelScreen());
      this.minecraft.player.setReducedDebugInfo(clientboundloginpacket.reducedDebugInfo());
      this.minecraft.player.setShowDeathScreen(clientboundloginpacket.showDeathScreen());
      this.minecraft.player.setLastDeathLocation(clientboundloginpacket.lastDeathLocation());
      this.minecraft.player.setPortalCooldown(clientboundloginpacket.portalCooldown());
      this.minecraft.gameMode.setLocalMode(clientboundloginpacket.gameType(), clientboundloginpacket.previousGameType());
      this.minecraft.options.setServerRenderDistance(clientboundloginpacket.chunkRadius());
      this.minecraft.options.broadcastOptions();
      this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(ClientBrandRetriever.getClientModName())));
      this.chatSession = null;
      this.lastSeenMessages = new LastSeenMessagesTracker(20);
      this.messageSignatureCache = MessageSignatureCache.createDefault();
      if (this.connection.isEncrypted()) {
         this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync((optional) -> optional.ifPresent(this::setKeyPair), this.minecraft);
      }

      this.telemetryManager.onPlayerInfoReceived(clientboundloginpacket.gameType(), clientboundloginpacket.hardcore());
      this.minecraft.quickPlayLog().log(this.minecraft);
   }

   public void handleAddEntity(ClientboundAddEntityPacket clientboundaddentitypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundaddentitypacket, this, this.minecraft);
      EntityType<?> entitytype = clientboundaddentitypacket.getType();
      Entity entity = entitytype.create(this.level);
      if (entity != null) {
         entity.recreateFromPacket(clientboundaddentitypacket);
         int i = clientboundaddentitypacket.getId();
         this.level.putNonPlayerEntity(i, entity);
         this.postAddEntitySoundInstance(entity);
      } else {
         LOGGER.warn("Skipping Entity with id {}", (Object)entitytype);
      }

   }

   private void postAddEntitySoundInstance(Entity entity) {
      if (entity instanceof AbstractMinecart) {
         this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)entity));
      } else if (entity instanceof Bee) {
         boolean flag = ((Bee)entity).isAngry();
         BeeSoundInstance beesoundinstance;
         if (flag) {
            beesoundinstance = new BeeAggressiveSoundInstance((Bee)entity);
         } else {
            beesoundinstance = new BeeFlyingSoundInstance((Bee)entity);
         }

         this.minecraft.getSoundManager().queueTickingSound(beesoundinstance);
      }

   }

   public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundaddexperienceorbpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundaddexperienceorbpacket, this, this.minecraft);
      double d0 = clientboundaddexperienceorbpacket.getX();
      double d1 = clientboundaddexperienceorbpacket.getY();
      double d2 = clientboundaddexperienceorbpacket.getZ();
      Entity entity = new ExperienceOrb(this.level, d0, d1, d2, clientboundaddexperienceorbpacket.getValue());
      entity.syncPacketPositionCodec(d0, d1, d2);
      entity.setYRot(0.0F);
      entity.setXRot(0.0F);
      entity.setId(clientboundaddexperienceorbpacket.getId());
      this.level.putNonPlayerEntity(clientboundaddexperienceorbpacket.getId(), entity);
   }

   public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundsetentitymotionpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetentitymotionpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsetentitymotionpacket.getId());
      if (entity != null) {
         entity.lerpMotion((double)clientboundsetentitymotionpacket.getXa() / 8000.0D, (double)clientboundsetentitymotionpacket.getYa() / 8000.0D, (double)clientboundsetentitymotionpacket.getZa() / 8000.0D);
      }
   }

   public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundsetentitydatapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetentitydatapacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsetentitydatapacket.id());
      if (entity != null) {
         entity.getEntityData().assignValues(clientboundsetentitydatapacket.packedItems());
      }

   }

   public void handleAddPlayer(ClientboundAddPlayerPacket clientboundaddplayerpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundaddplayerpacket, this, this.minecraft);
      PlayerInfo playerinfo = this.getPlayerInfo(clientboundaddplayerpacket.getPlayerId());
      if (playerinfo == null) {
         LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)clientboundaddplayerpacket.getPlayerId());
      } else {
         double d0 = clientboundaddplayerpacket.getX();
         double d1 = clientboundaddplayerpacket.getY();
         double d2 = clientboundaddplayerpacket.getZ();
         float f = (float)(clientboundaddplayerpacket.getyRot() * 360) / 256.0F;
         float f1 = (float)(clientboundaddplayerpacket.getxRot() * 360) / 256.0F;
         int i = clientboundaddplayerpacket.getEntityId();
         RemotePlayer remoteplayer = new RemotePlayer(this.minecraft.level, playerinfo.getProfile());
         remoteplayer.setId(i);
         remoteplayer.syncPacketPositionCodec(d0, d1, d2);
         remoteplayer.absMoveTo(d0, d1, d2, f, f1);
         remoteplayer.setOldPosAndRot();
         this.level.addPlayer(i, remoteplayer);
      }
   }

   public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundteleportentitypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundteleportentitypacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundteleportentitypacket.getId());
      if (entity != null) {
         double d0 = clientboundteleportentitypacket.getX();
         double d1 = clientboundteleportentitypacket.getY();
         double d2 = clientboundteleportentitypacket.getZ();
         entity.syncPacketPositionCodec(d0, d1, d2);
         if (!entity.isControlledByLocalInstance()) {
            float f = (float)(clientboundteleportentitypacket.getyRot() * 360) / 256.0F;
            float f1 = (float)(clientboundteleportentitypacket.getxRot() * 360) / 256.0F;
            entity.lerpTo(d0, d1, d2, f, f1, 3, true);
            entity.setOnGround(clientboundteleportentitypacket.isOnGround());
         }

      }
   }

   public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundsetcarrieditempacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetcarrieditempacket, this, this.minecraft);
      if (Inventory.isHotbarSlot(clientboundsetcarrieditempacket.getSlot())) {
         this.minecraft.player.getInventory().selected = clientboundsetcarrieditempacket.getSlot();
      }

   }

   public void handleMoveEntity(ClientboundMoveEntityPacket clientboundmoveentitypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundmoveentitypacket, this, this.minecraft);
      Entity entity = clientboundmoveentitypacket.getEntity(this.level);
      if (entity != null) {
         if (!entity.isControlledByLocalInstance()) {
            if (clientboundmoveentitypacket.hasPosition()) {
               VecDeltaCodec vecdeltacodec = entity.getPositionCodec();
               Vec3 vec3 = vecdeltacodec.decode((long)clientboundmoveentitypacket.getXa(), (long)clientboundmoveentitypacket.getYa(), (long)clientboundmoveentitypacket.getZa());
               vecdeltacodec.setBase(vec3);
               float f = clientboundmoveentitypacket.hasRotation() ? (float)(clientboundmoveentitypacket.getyRot() * 360) / 256.0F : entity.getYRot();
               float f1 = clientboundmoveentitypacket.hasRotation() ? (float)(clientboundmoveentitypacket.getxRot() * 360) / 256.0F : entity.getXRot();
               entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f1, 3, false);
            } else if (clientboundmoveentitypacket.hasRotation()) {
               float f2 = (float)(clientboundmoveentitypacket.getyRot() * 360) / 256.0F;
               float f3 = (float)(clientboundmoveentitypacket.getxRot() * 360) / 256.0F;
               entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), f2, f3, 3, false);
            }

            entity.setOnGround(clientboundmoveentitypacket.isOnGround());
         }

      }
   }

   public void handleRotateMob(ClientboundRotateHeadPacket clientboundrotateheadpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundrotateheadpacket, this, this.minecraft);
      Entity entity = clientboundrotateheadpacket.getEntity(this.level);
      if (entity != null) {
         float f = (float)(clientboundrotateheadpacket.getYHeadRot() * 360) / 256.0F;
         entity.lerpHeadTo(f, 3);
      }
   }

   public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundremoveentitiespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundremoveentitiespacket, this, this.minecraft);
      clientboundremoveentitiespacket.getEntityIds().forEach((i) -> this.level.removeEntity(i, Entity.RemovalReason.DISCARDED));
   }

   public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundplayerpositionpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerpositionpacket, this, this.minecraft);
      Player player = this.minecraft.player;
      Vec3 vec3 = player.getDeltaMovement();
      boolean flag = clientboundplayerpositionpacket.getRelativeArguments().contains(RelativeMovement.X);
      boolean flag1 = clientboundplayerpositionpacket.getRelativeArguments().contains(RelativeMovement.Y);
      boolean flag2 = clientboundplayerpositionpacket.getRelativeArguments().contains(RelativeMovement.Z);
      double d0;
      double d1;
      if (flag) {
         d0 = vec3.x();
         d1 = player.getX() + clientboundplayerpositionpacket.getX();
         player.xOld += clientboundplayerpositionpacket.getX();
         player.xo += clientboundplayerpositionpacket.getX();
      } else {
         d0 = 0.0D;
         d1 = clientboundplayerpositionpacket.getX();
         player.xOld = d1;
         player.xo = d1;
      }

      double d4;
      double d5;
      if (flag1) {
         d4 = vec3.y();
         d5 = player.getY() + clientboundplayerpositionpacket.getY();
         player.yOld += clientboundplayerpositionpacket.getY();
         player.yo += clientboundplayerpositionpacket.getY();
      } else {
         d4 = 0.0D;
         d5 = clientboundplayerpositionpacket.getY();
         player.yOld = d5;
         player.yo = d5;
      }

      double d8;
      double d9;
      if (flag2) {
         d8 = vec3.z();
         d9 = player.getZ() + clientboundplayerpositionpacket.getZ();
         player.zOld += clientboundplayerpositionpacket.getZ();
         player.zo += clientboundplayerpositionpacket.getZ();
      } else {
         d8 = 0.0D;
         d9 = clientboundplayerpositionpacket.getZ();
         player.zOld = d9;
         player.zo = d9;
      }

      player.setPos(d1, d5, d9);
      player.setDeltaMovement(d0, d4, d8);
      float f = clientboundplayerpositionpacket.getYRot();
      float f1 = clientboundplayerpositionpacket.getXRot();
      if (clientboundplayerpositionpacket.getRelativeArguments().contains(RelativeMovement.X_ROT)) {
         player.setXRot(player.getXRot() + f1);
         player.xRotO += f1;
      } else {
         player.setXRot(f1);
         player.xRotO = f1;
      }

      if (clientboundplayerpositionpacket.getRelativeArguments().contains(RelativeMovement.Y_ROT)) {
         player.setYRot(player.getYRot() + f);
         player.yRotO += f;
      } else {
         player.setYRot(f);
         player.yRotO = f;
      }

      this.connection.send(new ServerboundAcceptTeleportationPacket(clientboundplayerpositionpacket.getId()));
      this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
   }

   public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsectionblocksupdatepacket, this, this.minecraft);
      clientboundsectionblocksupdatepacket.runUpdates((blockpos, blockstate) -> this.level.setServerVerifiedBlockState(blockpos, blockstate, 19));
   }

   public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundlevelchunkwithlightpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundlevelchunkwithlightpacket, this, this.minecraft);
      int i = clientboundlevelchunkwithlightpacket.getX();
      int j = clientboundlevelchunkwithlightpacket.getZ();
      this.updateLevelChunk(i, j, clientboundlevelchunkwithlightpacket.getChunkData());
      ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = clientboundlevelchunkwithlightpacket.getLightData();
      this.level.queueLightUpdate(() -> {
         this.applyLightData(i, j, clientboundlightupdatepacketdata);
         LevelChunk levelchunk = this.level.getChunkSource().getChunk(i, j, false);
         if (levelchunk != null) {
            this.enableChunkLight(levelchunk, i, j);
         }

      });
   }

   public void handleChunksBiomes(ClientboundChunksBiomesPacket clientboundchunksbiomespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundchunksbiomespacket, this, this.minecraft);

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket_chunkbiomedata : clientboundchunksbiomespacket.chunkBiomeData()) {
         this.level.getChunkSource().replaceBiomes(clientboundchunksbiomespacket_chunkbiomedata.pos().x, clientboundchunksbiomespacket_chunkbiomedata.pos().z, clientboundchunksbiomespacket_chunkbiomedata.getReadBuffer());
      }

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket_chunkbiomedata1 : clientboundchunksbiomespacket.chunkBiomeData()) {
         this.level.onChunkLoaded(new ChunkPos(clientboundchunksbiomespacket_chunkbiomedata1.pos().x, clientboundchunksbiomespacket_chunkbiomedata1.pos().z));
      }

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket_chunkbiomedata2 : clientboundchunksbiomespacket.chunkBiomeData()) {
         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = this.level.getMinSection(); k < this.level.getMaxSection(); ++k) {
                  this.minecraft.levelRenderer.setSectionDirty(clientboundchunksbiomespacket_chunkbiomedata2.pos().x + i, k, clientboundchunksbiomespacket_chunkbiomedata2.pos().z + j);
               }
            }
         }
      }

   }

   private void updateLevelChunk(int i, int j, ClientboundLevelChunkPacketData clientboundlevelchunkpacketdata) {
      this.level.getChunkSource().replaceWithPacketData(i, j, clientboundlevelchunkpacketdata.getReadBuffer(), clientboundlevelchunkpacketdata.getHeightmaps(), clientboundlevelchunkpacketdata.getBlockEntitiesTagsConsumer(i, j));
   }

   private void enableChunkLight(LevelChunk levelchunk, int i, int j) {
      LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
      LevelChunkSection[] alevelchunksection = levelchunk.getSections();
      ChunkPos chunkpos = levelchunk.getPos();

      for(int k = 0; k < alevelchunksection.length; ++k) {
         LevelChunkSection levelchunksection = alevelchunksection[k];
         int l = this.level.getSectionYFromSectionIndex(k);
         levellightengine.updateSectionStatus(SectionPos.of(chunkpos, l), levelchunksection.hasOnlyAir());
         this.level.setSectionDirtyWithNeighbors(i, l, j);
      }

   }

   public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundforgetlevelchunkpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundforgetlevelchunkpacket, this, this.minecraft);
      int i = clientboundforgetlevelchunkpacket.getX();
      int j = clientboundforgetlevelchunkpacket.getZ();
      ClientChunkCache clientchunkcache = this.level.getChunkSource();
      clientchunkcache.drop(i, j);
      this.queueLightRemoval(clientboundforgetlevelchunkpacket);
   }

   private void queueLightRemoval(ClientboundForgetLevelChunkPacket clientboundforgetlevelchunkpacket) {
      ChunkPos chunkpos = new ChunkPos(clientboundforgetlevelchunkpacket.getX(), clientboundforgetlevelchunkpacket.getZ());
      this.level.queueLightUpdate(() -> {
         LevelLightEngine levellightengine = this.level.getLightEngine();
         levellightengine.setLightEnabled(chunkpos, false);

         for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
            SectionPos sectionpos = SectionPos.of(chunkpos, i);
            levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, (DataLayer)null);
            levellightengine.queueSectionData(LightLayer.SKY, sectionpos, (DataLayer)null);
         }

         for(int j = this.level.getMinSection(); j < this.level.getMaxSection(); ++j) {
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), true);
         }

      });
   }

   public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundblockupdatepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundblockupdatepacket, this, this.minecraft);
      this.level.setServerVerifiedBlockState(clientboundblockupdatepacket.getPos(), clientboundblockupdatepacket.getBlockState(), 19);
   }

   public void handleDisconnect(ClientboundDisconnectPacket clientbounddisconnectpacket) {
      this.connection.disconnect(clientbounddisconnectpacket.getReason());
   }

   public void onDisconnect(Component component) {
      this.minecraft.clearLevel();
      this.telemetryManager.onDisconnect();
      if (this.callbackScreen != null) {
         if (this.callbackScreen instanceof RealmsScreen) {
            this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
         } else {
            this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
         }
      } else {
         this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, component));
      }

   }

   public void send(Packet<?> packet) {
      this.connection.send(packet);
   }

   public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundtakeitementitypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundtakeitementitypacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundtakeitementitypacket.getItemId());
      LivingEntity livingentity = (LivingEntity)this.level.getEntity(clientboundtakeitementitypacket.getPlayerId());
      if (livingentity == null) {
         livingentity = this.minecraft.player;
      }

      if (entity != null) {
         if (entity instanceof ExperienceOrb) {
            this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
         } else {
            this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
         }

         this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, entity, livingentity));
         if (entity instanceof ItemEntity) {
            ItemEntity itementity = (ItemEntity)entity;
            ItemStack itemstack = itementity.getItem();
            if (!itemstack.isEmpty()) {
               itemstack.shrink(clientboundtakeitementitypacket.getAmount());
            }

            if (itemstack.isEmpty()) {
               this.level.removeEntity(clientboundtakeitementitypacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
         } else if (!(entity instanceof ExperienceOrb)) {
            this.level.removeEntity(clientboundtakeitementitypacket.getItemId(), Entity.RemovalReason.DISCARDED);
         }
      }

   }

   public void handleSystemChat(ClientboundSystemChatPacket clientboundsystemchatpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsystemchatpacket, this, this.minecraft);
      this.minecraft.getChatListener().handleSystemMessage(clientboundsystemchatpacket.content(), clientboundsystemchatpacket.overlay());
   }

   public void handlePlayerChat(ClientboundPlayerChatPacket clientboundplayerchatpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerchatpacket, this, this.minecraft);
      Optional<SignedMessageBody> optional = clientboundplayerchatpacket.body().unpack(this.messageSignatureCache);
      Optional<ChatType.Bound> optional1 = clientboundplayerchatpacket.chatType().resolve(this.registryAccess.compositeAccess());
      if (!optional.isEmpty() && !optional1.isEmpty()) {
         UUID uuid = clientboundplayerchatpacket.sender();
         PlayerInfo playerinfo = this.getPlayerInfo(uuid);
         if (playerinfo == null) {
            this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
         } else {
            RemoteChatSession remotechatsession = playerinfo.getChatSession();
            SignedMessageLink signedmessagelink;
            if (remotechatsession != null) {
               signedmessagelink = new SignedMessageLink(clientboundplayerchatpacket.index(), uuid, remotechatsession.sessionId());
            } else {
               signedmessagelink = SignedMessageLink.unsigned(uuid);
            }

            PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, clientboundplayerchatpacket.signature(), optional.get(), clientboundplayerchatpacket.unsignedContent(), clientboundplayerchatpacket.filterMask());
            if (!playerinfo.getMessageValidator().updateAndValidate(playerchatmessage)) {
               this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
            } else {
               this.minecraft.getChatListener().handlePlayerChatMessage(playerchatmessage, playerinfo.getProfile(), optional1.get());
               this.messageSignatureCache.push(playerchatmessage);
            }
         }
      } else {
         this.connection.disconnect(INVALID_PACKET);
      }
   }

   public void handleDisguisedChat(ClientboundDisguisedChatPacket clientbounddisguisedchatpacket) {
      PacketUtils.ensureRunningOnSameThread(clientbounddisguisedchatpacket, this, this.minecraft);
      Optional<ChatType.Bound> optional = clientbounddisguisedchatpacket.chatType().resolve(this.registryAccess.compositeAccess());
      if (optional.isEmpty()) {
         this.connection.disconnect(INVALID_PACKET);
      } else {
         this.minecraft.getChatListener().handleDisguisedChatMessage(clientbounddisguisedchatpacket.message(), optional.get());
      }
   }

   public void handleDeleteChat(ClientboundDeleteChatPacket clientbounddeletechatpacket) {
      PacketUtils.ensureRunningOnSameThread(clientbounddeletechatpacket, this, this.minecraft);
      Optional<MessageSignature> optional = clientbounddeletechatpacket.messageSignature().unpack(this.messageSignatureCache);
      if (optional.isEmpty()) {
         this.connection.disconnect(INVALID_PACKET);
      } else {
         this.lastSeenMessages.ignorePending(optional.get());
         if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue(optional.get())) {
            this.minecraft.gui.getChat().deleteMessage(optional.get());
         }

      }
   }

   public void handleAnimate(ClientboundAnimatePacket clientboundanimatepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundanimatepacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundanimatepacket.getId());
      if (entity != null) {
         if (clientboundanimatepacket.getAction() == 0) {
            LivingEntity livingentity = (LivingEntity)entity;
            livingentity.swing(InteractionHand.MAIN_HAND);
         } else if (clientboundanimatepacket.getAction() == 3) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            livingentity1.swing(InteractionHand.OFF_HAND);
         } else if (clientboundanimatepacket.getAction() == 2) {
            Player player = (Player)entity;
            player.stopSleepInBed(false, false);
         } else if (clientboundanimatepacket.getAction() == 4) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
         } else if (clientboundanimatepacket.getAction() == 5) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
         }

      }
   }

   public void handleHurtAnimation(ClientboundHurtAnimationPacket clientboundhurtanimationpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundhurtanimationpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundhurtanimationpacket.id());
      if (entity != null) {
         entity.animateHurt(clientboundhurtanimationpacket.yaw());
      }
   }

   public void handleSetTime(ClientboundSetTimePacket clientboundsettimepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsettimepacket, this, this.minecraft);
      this.minecraft.level.setGameTime(clientboundsettimepacket.getGameTime());
      this.minecraft.level.setDayTime(clientboundsettimepacket.getDayTime());
      this.telemetryManager.setTime(clientboundsettimepacket.getGameTime());
   }

   public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundsetdefaultspawnpositionpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetdefaultspawnpositionpacket, this, this.minecraft);
      this.minecraft.level.setDefaultSpawnPos(clientboundsetdefaultspawnpositionpacket.getPos(), clientboundsetdefaultspawnpositionpacket.getAngle());
      Screen var3 = this.minecraft.screen;
      if (var3 instanceof ReceivingLevelScreen receivinglevelscreen) {
         receivinglevelscreen.loadingPacketsReceived();
      }

   }

   public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundsetpassengerspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetpassengerspacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsetpassengerspacket.getVehicle());
      if (entity == null) {
         LOGGER.warn("Received passengers for unknown entity");
      } else {
         boolean flag = entity.hasIndirectPassenger(this.minecraft.player);
         entity.ejectPassengers();

         for(int i : clientboundsetpassengerspacket.getPassengers()) {
            Entity entity1 = this.level.getEntity(i);
            if (entity1 != null) {
               entity1.startRiding(entity, true);
               if (entity1 == this.minecraft.player && !flag) {
                  if (entity instanceof Boat) {
                     this.minecraft.player.yRotO = entity.getYRot();
                     this.minecraft.player.setYRot(entity.getYRot());
                     this.minecraft.player.setYHeadRot(entity.getYRot());
                  }

                  Component component = Component.translatable("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
                  this.minecraft.gui.setOverlayMessage(component, false);
                  this.minecraft.getNarrator().sayNow(component);
               }
            }
         }

      }
   }

   public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundsetentitylinkpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetentitylinkpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsetentitylinkpacket.getSourceId());
      if (entity instanceof Mob) {
         ((Mob)entity).setDelayedLeashHolderId(clientboundsetentitylinkpacket.getDestId());
      }

   }

   private static ItemStack findTotem(Player player) {
      for(InteractionHand interactionhand : InteractionHand.values()) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (itemstack.is(Items.TOTEM_OF_UNDYING)) {
            return itemstack;
         }
      }

      return new ItemStack(Items.TOTEM_OF_UNDYING);
   }

   public void handleEntityEvent(ClientboundEntityEventPacket clientboundentityeventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundentityeventpacket, this, this.minecraft);
      Entity entity = clientboundentityeventpacket.getEntity(this.level);
      if (entity != null) {
         switch (clientboundentityeventpacket.getEventId()) {
            case 21:
               this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
               break;
            case 35:
               int i = 40;
               this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
               this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
               if (entity == this.minecraft.player) {
                  this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
               }
               break;
            case 63:
               this.minecraft.getSoundManager().play(new SnifferSoundInstance((Sniffer)entity));
               break;
            default:
               entity.handleEntityEvent(clientboundentityeventpacket.getEventId());
         }
      }

   }

   public void handleDamageEvent(ClientboundDamageEventPacket clientbounddamageeventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientbounddamageeventpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientbounddamageeventpacket.entityId());
      if (entity != null) {
         entity.handleDamageEvent(clientbounddamageeventpacket.getSource(this.level));
      }
   }

   public void handleSetHealth(ClientboundSetHealthPacket clientboundsethealthpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsethealthpacket, this, this.minecraft);
      this.minecraft.player.hurtTo(clientboundsethealthpacket.getHealth());
      this.minecraft.player.getFoodData().setFoodLevel(clientboundsethealthpacket.getFood());
      this.minecraft.player.getFoodData().setSaturation(clientboundsethealthpacket.getSaturation());
   }

   public void handleSetExperience(ClientboundSetExperiencePacket clientboundsetexperiencepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetexperiencepacket, this, this.minecraft);
      this.minecraft.player.setExperienceValues(clientboundsetexperiencepacket.getExperienceProgress(), clientboundsetexperiencepacket.getTotalExperience(), clientboundsetexperiencepacket.getExperienceLevel());
   }

   public void handleRespawn(ClientboundRespawnPacket clientboundrespawnpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundrespawnpacket, this, this.minecraft);
      ResourceKey<Level> resourcekey = clientboundrespawnpacket.getDimension();
      Holder<DimensionType> holder = this.registryAccess.compositeAccess().<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(clientboundrespawnpacket.getDimensionType());
      LocalPlayer localplayer = this.minecraft.player;
      int i = localplayer.getId();
      if (resourcekey != localplayer.level().dimension()) {
         Scoreboard scoreboard = this.level.getScoreboard();
         Map<String, MapItemSavedData> map = this.level.getAllMapData();
         boolean flag = clientboundrespawnpacket.isDebug();
         boolean flag1 = clientboundrespawnpacket.isFlat();
         ClientLevel.ClientLevelData clientlevel_clientleveldata = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flag1);
         this.levelData = clientlevel_clientleveldata;
         this.level = new ClientLevel(this, clientlevel_clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, clientboundrespawnpacket.getSeed());
         this.level.setScoreboard(scoreboard);
         this.level.addMapData(map);
         this.minecraft.setLevel(this.level);
         this.minecraft.setScreen(new ReceivingLevelScreen());
      }

      String s = localplayer.getServerBrand();
      this.minecraft.cameraEntity = null;
      if (localplayer.hasContainerOpen()) {
         localplayer.closeContainer();
      }

      LocalPlayer localplayer1;
      if (clientboundrespawnpacket.shouldKeep((byte)2)) {
         localplayer1 = this.minecraft.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook(), localplayer.isShiftKeyDown(), localplayer.isSprinting());
      } else {
         localplayer1 = this.minecraft.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook());
      }

      localplayer1.setId(i);
      this.minecraft.player = localplayer1;
      if (resourcekey != localplayer.level().dimension()) {
         this.minecraft.getMusicManager().stopPlaying();
      }

      this.minecraft.cameraEntity = localplayer1;
      if (clientboundrespawnpacket.shouldKeep((byte)2)) {
         List<SynchedEntityData.DataValue<?>> list = localplayer.getEntityData().getNonDefaultValues();
         if (list != null) {
            localplayer1.getEntityData().assignValues(list);
         }
      }

      if (clientboundrespawnpacket.shouldKeep((byte)1)) {
         localplayer1.getAttributes().assignValues(localplayer.getAttributes());
      }

      localplayer1.resetPos();
      localplayer1.setServerBrand(s);
      this.level.addPlayer(i, localplayer1);
      localplayer1.setYRot(-180.0F);
      localplayer1.input = new KeyboardInput(this.minecraft.options);
      this.minecraft.gameMode.adjustPlayer(localplayer1);
      localplayer1.setReducedDebugInfo(localplayer.isReducedDebugInfo());
      localplayer1.setShowDeathScreen(localplayer.shouldShowDeathScreen());
      localplayer1.setLastDeathLocation(clientboundrespawnpacket.getLastDeathLocation());
      localplayer1.setPortalCooldown(clientboundrespawnpacket.getPortalCooldown());
      localplayer1.spinningEffectIntensity = localplayer.spinningEffectIntensity;
      localplayer1.oSpinningEffectIntensity = localplayer.oSpinningEffectIntensity;
      if (this.minecraft.screen instanceof DeathScreen || this.minecraft.screen instanceof DeathScreen.TitleConfirmScreen) {
         this.minecraft.setScreen((Screen)null);
      }

      this.minecraft.gameMode.setLocalMode(clientboundrespawnpacket.getPlayerGameType(), clientboundrespawnpacket.getPreviousPlayerGameType());
   }

   public void handleExplosion(ClientboundExplodePacket clientboundexplodepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundexplodepacket, this, this.minecraft);
      Explosion explosion = new Explosion(this.minecraft.level, (Entity)null, clientboundexplodepacket.getX(), clientboundexplodepacket.getY(), clientboundexplodepacket.getZ(), clientboundexplodepacket.getPower(), clientboundexplodepacket.getToBlow());
      explosion.finalizeExplosion(true);
      this.minecraft.player.setDeltaMovement(this.minecraft.player.getDeltaMovement().add((double)clientboundexplodepacket.getKnockbackX(), (double)clientboundexplodepacket.getKnockbackY(), (double)clientboundexplodepacket.getKnockbackZ()));
   }

   public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundhorsescreenopenpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundhorsescreenopenpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundhorsescreenopenpacket.getEntityId());
      if (entity instanceof AbstractHorse) {
         LocalPlayer localplayer = this.minecraft.player;
         AbstractHorse abstracthorse = (AbstractHorse)entity;
         SimpleContainer simplecontainer = new SimpleContainer(clientboundhorsescreenopenpacket.getSize());
         HorseInventoryMenu horseinventorymenu = new HorseInventoryMenu(clientboundhorsescreenopenpacket.getContainerId(), localplayer.getInventory(), simplecontainer, abstracthorse);
         localplayer.containerMenu = horseinventorymenu;
         this.minecraft.setScreen(new HorseInventoryScreen(horseinventorymenu, localplayer.getInventory(), abstracthorse));
      }

   }

   public void handleOpenScreen(ClientboundOpenScreenPacket clientboundopenscreenpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundopenscreenpacket, this, this.minecraft);
      MenuScreens.create(clientboundopenscreenpacket.getType(), this.minecraft, clientboundopenscreenpacket.getContainerId(), clientboundopenscreenpacket.getTitle());
   }

   public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundcontainersetslotpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcontainersetslotpacket, this, this.minecraft);
      Player player = this.minecraft.player;
      ItemStack itemstack = clientboundcontainersetslotpacket.getItem();
      int i = clientboundcontainersetslotpacket.getSlot();
      this.minecraft.getTutorial().onGetItem(itemstack);
      if (clientboundcontainersetslotpacket.getContainerId() == -1) {
         if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
            player.containerMenu.setCarried(itemstack);
         }
      } else if (clientboundcontainersetslotpacket.getContainerId() == -2) {
         player.getInventory().setItem(i, itemstack);
      } else {
         boolean flag = false;
         Screen var7 = this.minecraft.screen;
         if (var7 instanceof CreativeModeInventoryScreen) {
            CreativeModeInventoryScreen creativemodeinventoryscreen = (CreativeModeInventoryScreen)var7;
            flag = !creativemodeinventoryscreen.isInventoryOpen();
         }

         if (clientboundcontainersetslotpacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(i)) {
            if (!itemstack.isEmpty()) {
               ItemStack itemstack1 = player.inventoryMenu.getSlot(i).getItem();
               if (itemstack1.isEmpty() || itemstack1.getCount() < itemstack.getCount()) {
                  itemstack.setPopTime(5);
               }
            }

            player.inventoryMenu.setItem(i, clientboundcontainersetslotpacket.getStateId(), itemstack);
         } else if (clientboundcontainersetslotpacket.getContainerId() == player.containerMenu.containerId && (clientboundcontainersetslotpacket.getContainerId() != 0 || !flag)) {
            player.containerMenu.setItem(i, clientboundcontainersetslotpacket.getStateId(), itemstack);
         }
      }

   }

   public void handleContainerContent(ClientboundContainerSetContentPacket clientboundcontainersetcontentpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcontainersetcontentpacket, this, this.minecraft);
      Player player = this.minecraft.player;
      if (clientboundcontainersetcontentpacket.getContainerId() == 0) {
         player.inventoryMenu.initializeContents(clientboundcontainersetcontentpacket.getStateId(), clientboundcontainersetcontentpacket.getItems(), clientboundcontainersetcontentpacket.getCarriedItem());
      } else if (clientboundcontainersetcontentpacket.getContainerId() == player.containerMenu.containerId) {
         player.containerMenu.initializeContents(clientboundcontainersetcontentpacket.getStateId(), clientboundcontainersetcontentpacket.getItems(), clientboundcontainersetcontentpacket.getCarriedItem());
      }

   }

   public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundopensigneditorpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundopensigneditorpacket, this, this.minecraft);
      BlockPos blockpos = clientboundopensigneditorpacket.getPos();
      BlockEntity blockstate = this.level.getBlockEntity(blockpos);
      if (blockstate instanceof SignBlockEntity signblockentity) {
         this.minecraft.player.openTextEdit(signblockentity, clientboundopensigneditorpacket.isFrontText());
      } else {
         BlockState blockstate = this.level.getBlockState(blockpos);
         SignBlockEntity signblockentity1 = new SignBlockEntity(blockpos, blockstate);
         signblockentity1.setLevel(this.level);
         this.minecraft.player.openTextEdit(signblockentity1, clientboundopensigneditorpacket.isFrontText());
      }

   }

   public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundblockentitydatapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundblockentitydatapacket, this, this.minecraft);
      BlockPos blockpos = clientboundblockentitydatapacket.getPos();
      this.minecraft.level.getBlockEntity(blockpos, clientboundblockentitydatapacket.getType()).ifPresent((blockentity) -> {
         CompoundTag compoundtag = clientboundblockentitydatapacket.getTag();
         if (compoundtag != null) {
            blockentity.load(compoundtag);
         }

         if (blockentity instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
            ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
         }

      });
   }

   public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundcontainersetdatapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcontainersetdatapacket, this, this.minecraft);
      Player player = this.minecraft.player;
      if (player.containerMenu != null && player.containerMenu.containerId == clientboundcontainersetdatapacket.getContainerId()) {
         player.containerMenu.setData(clientboundcontainersetdatapacket.getId(), clientboundcontainersetdatapacket.getValue());
      }

   }

   public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundsetequipmentpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetequipmentpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsetequipmentpacket.getEntity());
      if (entity != null) {
         clientboundsetequipmentpacket.getSlots().forEach((pair) -> entity.setItemSlot(pair.getFirst(), pair.getSecond()));
      }

   }

   public void handleContainerClose(ClientboundContainerClosePacket clientboundcontainerclosepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcontainerclosepacket, this, this.minecraft);
      this.minecraft.player.clientSideCloseContainer();
   }

   public void handleBlockEvent(ClientboundBlockEventPacket clientboundblockeventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundblockeventpacket, this, this.minecraft);
      this.minecraft.level.blockEvent(clientboundblockeventpacket.getPos(), clientboundblockeventpacket.getBlock(), clientboundblockeventpacket.getB0(), clientboundblockeventpacket.getB1());
   }

   public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundblockdestructionpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundblockdestructionpacket, this, this.minecraft);
      this.minecraft.level.destroyBlockProgress(clientboundblockdestructionpacket.getId(), clientboundblockdestructionpacket.getPos(), clientboundblockdestructionpacket.getProgress());
   }

   public void handleGameEvent(ClientboundGameEventPacket clientboundgameeventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundgameeventpacket, this, this.minecraft);
      Player player = this.minecraft.player;
      ClientboundGameEventPacket.Type clientboundgameeventpacket_type = clientboundgameeventpacket.getEvent();
      float f = clientboundgameeventpacket.getParam();
      int i = Mth.floor(f + 0.5F);
      if (clientboundgameeventpacket_type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
         player.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.START_RAINING) {
         this.level.getLevelData().setRaining(true);
         this.level.setRainLevel(0.0F);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.STOP_RAINING) {
         this.level.getLevelData().setRaining(false);
         this.level.setRainLevel(1.0F);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
         this.minecraft.gameMode.setLocalMode(GameType.byId(i));
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.WIN_GAME) {
         if (i == 0) {
            this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            this.minecraft.setScreen(new ReceivingLevelScreen());
         } else if (i == 1) {
            this.minecraft.setScreen(new WinScreen(true, () -> {
               this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
               this.minecraft.setScreen((Screen)null);
            }));
         }
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.DEMO_EVENT) {
         Options options = this.minecraft.options;
         if (f == 0.0F) {
            this.minecraft.setScreen(new DemoIntroScreen());
         } else if (f == 101.0F) {
            this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()));
         } else if (f == 102.0F) {
            this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
         } else if (f == 103.0F) {
            this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
         } else if (f == 104.0F) {
            this.minecraft.gui.getChat().addMessage(Component.translatable("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
         }
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
         this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
         this.level.setRainLevel(f);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
         this.level.setThunderLevel(f);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
         this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
         this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0D, 0.0D, 0.0D);
         if (i == 1) {
            this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
         }
      } else if (clientboundgameeventpacket_type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
         this.minecraft.player.setShowDeathScreen(f == 0.0F);
      }

   }

   public void handleMapItemData(ClientboundMapItemDataPacket clientboundmapitemdatapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundmapitemdatapacket, this, this.minecraft);
      MapRenderer maprenderer = this.minecraft.gameRenderer.getMapRenderer();
      int i = clientboundmapitemdatapacket.getMapId();
      String s = MapItem.makeKey(i);
      MapItemSavedData mapitemsaveddata = this.minecraft.level.getMapData(s);
      if (mapitemsaveddata == null) {
         mapitemsaveddata = MapItemSavedData.createForClient(clientboundmapitemdatapacket.getScale(), clientboundmapitemdatapacket.isLocked(), this.minecraft.level.dimension());
         this.minecraft.level.overrideMapData(s, mapitemsaveddata);
      }

      clientboundmapitemdatapacket.applyToMap(mapitemsaveddata);
      maprenderer.update(i, mapitemsaveddata);
   }

   public void handleLevelEvent(ClientboundLevelEventPacket clientboundleveleventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundleveleventpacket, this, this.minecraft);
      if (clientboundleveleventpacket.isGlobalEvent()) {
         this.minecraft.level.globalLevelEvent(clientboundleveleventpacket.getType(), clientboundleveleventpacket.getPos(), clientboundleveleventpacket.getData());
      } else {
         this.minecraft.level.levelEvent(clientboundleveleventpacket.getType(), clientboundleveleventpacket.getPos(), clientboundleveleventpacket.getData());
      }

   }

   public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundupdateadvancementspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdateadvancementspacket, this, this.minecraft);
      this.advancements.update(clientboundupdateadvancementspacket);
   }

   public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundselectadvancementstabpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundselectadvancementstabpacket, this, this.minecraft);
      ResourceLocation resourcelocation = clientboundselectadvancementstabpacket.getTab();
      if (resourcelocation == null) {
         this.advancements.setSelectedTab((Advancement)null, false);
      } else {
         Advancement advancement = this.advancements.getAdvancements().get(resourcelocation);
         this.advancements.setSelectedTab(advancement, false);
      }

   }

   public void handleCommands(ClientboundCommandsPacket clientboundcommandspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcommandspacket, this, this.minecraft);
      this.commands = new CommandDispatcher<>(clientboundcommandspacket.getRoot(CommandBuildContext.simple(this.registryAccess.compositeAccess(), this.enabledFeatures)));
   }

   public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundstopsoundpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundstopsoundpacket, this, this.minecraft);
      this.minecraft.getSoundManager().stop(clientboundstopsoundpacket.getName(), clientboundstopsoundpacket.getSource());
   }

   public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundcommandsuggestionspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcommandsuggestionspacket, this, this.minecraft);
      this.suggestionsProvider.completeCustomSuggestions(clientboundcommandsuggestionspacket.getId(), clientboundcommandsuggestionspacket.getSuggestions());
   }

   public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundupdaterecipespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdaterecipespacket, this, this.minecraft);
      this.recipeManager.replaceRecipes(clientboundupdaterecipespacket.getRecipes());
      ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
      clientrecipebook.setupCollections(this.recipeManager.getRecipes(), this.minecraft.level.registryAccess());
      this.minecraft.populateSearchTree(SearchRegistry.RECIPE_COLLECTIONS, clientrecipebook.getCollections());
   }

   public void handleLookAt(ClientboundPlayerLookAtPacket clientboundplayerlookatpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerlookatpacket, this, this.minecraft);
      Vec3 vec3 = clientboundplayerlookatpacket.getPosition(this.level);
      if (vec3 != null) {
         this.minecraft.player.lookAt(clientboundplayerlookatpacket.getFromAnchor(), vec3);
      }

   }

   public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundtagquerypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundtagquerypacket, this, this.minecraft);
      if (!this.debugQueryHandler.handleResponse(clientboundtagquerypacket.getTransactionId(), clientboundtagquerypacket.getTag())) {
         LOGGER.debug("Got unhandled response to tag query {}", (int)clientboundtagquerypacket.getTransactionId());
      }

   }

   public void handleAwardStats(ClientboundAwardStatsPacket clientboundawardstatspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundawardstatspacket, this, this.minecraft);

      for(Map.Entry<Stat<?>, Integer> map_entry : clientboundawardstatspacket.getStats().entrySet()) {
         Stat<?> stat = map_entry.getKey();
         int i = map_entry.getValue();
         this.minecraft.player.getStats().setValue(this.minecraft.player, stat, i);
      }

      if (this.minecraft.screen instanceof StatsUpdateListener) {
         ((StatsUpdateListener)this.minecraft.screen).onStatsUpdated();
      }

   }

   public void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundrecipepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundrecipepacket, this, this.minecraft);
      ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
      clientrecipebook.setBookSettings(clientboundrecipepacket.getBookSettings());
      ClientboundRecipePacket.State clientboundrecipepacket_state = clientboundrecipepacket.getState();
      switch (clientboundrecipepacket_state) {
         case REMOVE:
            for(ResourceLocation resourcelocation : clientboundrecipepacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation).ifPresent(clientrecipebook::remove);
            }
            break;
         case INIT:
            for(ResourceLocation resourcelocation1 : clientboundrecipepacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation1).ifPresent(clientrecipebook::add);
            }

            for(ResourceLocation resourcelocation2 : clientboundrecipepacket.getHighlights()) {
               this.recipeManager.byKey(resourcelocation2).ifPresent(clientrecipebook::addHighlight);
            }
            break;
         case ADD:
            for(ResourceLocation resourcelocation3 : clientboundrecipepacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation3).ifPresent((recipe) -> {
                  clientrecipebook.add(recipe);
                  clientrecipebook.addHighlight(recipe);
                  if (recipe.showNotification()) {
                     RecipeToast.addOrUpdate(this.minecraft.getToasts(), recipe);
                  }

               });
            }
      }

      clientrecipebook.getCollections().forEach((recipecollection) -> recipecollection.updateKnownRecipes(clientrecipebook));
      if (this.minecraft.screen instanceof RecipeUpdateListener) {
         ((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
      }

   }

   public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundupdatemobeffectpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdatemobeffectpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundupdatemobeffectpacket.getEntityId());
      if (entity instanceof LivingEntity) {
         MobEffect mobeffect = clientboundupdatemobeffectpacket.getEffect();
         if (mobeffect != null) {
            MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, clientboundupdatemobeffectpacket.getEffectDurationTicks(), clientboundupdatemobeffectpacket.getEffectAmplifier(), clientboundupdatemobeffectpacket.isEffectAmbient(), clientboundupdatemobeffectpacket.isEffectVisible(), clientboundupdatemobeffectpacket.effectShowsIcon(), (MobEffectInstance)null, Optional.ofNullable(clientboundupdatemobeffectpacket.getFactorData()));
            ((LivingEntity)entity).forceAddEffect(mobeffectinstance, (Entity)null);
         }
      }
   }

   public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundupdatetagspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdatetagspacket, this, this.minecraft);
      clientboundupdatetagspacket.getTags().forEach(this::updateTagsForRegistry);
      if (!this.connection.isMemoryConnection()) {
         Blocks.rebuildCache();
      }

      CreativeModeTabs.searchTab().rebuildSearchTree();
   }

   public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundupdateenabledfeaturespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdateenabledfeaturespacket, this, this.minecraft);
      this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(clientboundupdateenabledfeaturespacket.features());
   }

   private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> resourcekey, TagNetworkSerialization.NetworkPayload tagnetworkserialization_networkpayload) {
      if (!tagnetworkserialization_networkpayload.isEmpty()) {
         Registry<T> registry = this.registryAccess.compositeAccess().<T>registry(resourcekey).orElseThrow(() -> new IllegalStateException("Unknown registry " + resourcekey));
         Map<TagKey<T>, List<Holder<T>>> map = new HashMap<>();
         TagNetworkSerialization.deserializeTagsFromNetwork(resourcekey, registry, tagnetworkserialization_networkpayload, map::put);
         registry.bindTags(map);
      }
   }

   public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundplayercombatendpacket) {
   }

   public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundplayercombatenterpacket) {
   }

   public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundplayercombatkillpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayercombatkillpacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundplayercombatkillpacket.getPlayerId());
      if (entity == this.minecraft.player) {
         if (this.minecraft.player.shouldShowDeathScreen()) {
            this.minecraft.setScreen(new DeathScreen(clientboundplayercombatkillpacket.getMessage(), this.level.getLevelData().isHardcore()));
         } else {
            this.minecraft.player.respawn();
         }
      }

   }

   public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundchangedifficultypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundchangedifficultypacket, this, this.minecraft);
      this.levelData.setDifficulty(clientboundchangedifficultypacket.getDifficulty());
      this.levelData.setDifficultyLocked(clientboundchangedifficultypacket.isLocked());
   }

   public void handleSetCamera(ClientboundSetCameraPacket clientboundsetcamerapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetcamerapacket, this, this.minecraft);
      Entity entity = clientboundsetcamerapacket.getEntity(this.level);
      if (entity != null) {
         this.minecraft.setCameraEntity(entity);
      }

   }

   public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundinitializeborderpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundinitializeborderpacket, this, this.minecraft);
      WorldBorder worldborder = this.level.getWorldBorder();
      worldborder.setCenter(clientboundinitializeborderpacket.getNewCenterX(), clientboundinitializeborderpacket.getNewCenterZ());
      long i = clientboundinitializeborderpacket.getLerpTime();
      if (i > 0L) {
         worldborder.lerpSizeBetween(clientboundinitializeborderpacket.getOldSize(), clientboundinitializeborderpacket.getNewSize(), i);
      } else {
         worldborder.setSize(clientboundinitializeborderpacket.getNewSize());
      }

      worldborder.setAbsoluteMaxSize(clientboundinitializeborderpacket.getNewAbsoluteMaxSize());
      worldborder.setWarningBlocks(clientboundinitializeborderpacket.getWarningBlocks());
      worldborder.setWarningTime(clientboundinitializeborderpacket.getWarningTime());
   }

   public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundsetbordercenterpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetbordercenterpacket, this, this.minecraft);
      this.level.getWorldBorder().setCenter(clientboundsetbordercenterpacket.getNewCenterX(), clientboundsetbordercenterpacket.getNewCenterZ());
   }

   public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundsetborderlerpsizepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetborderlerpsizepacket, this, this.minecraft);
      this.level.getWorldBorder().lerpSizeBetween(clientboundsetborderlerpsizepacket.getOldSize(), clientboundsetborderlerpsizepacket.getNewSize(), clientboundsetborderlerpsizepacket.getLerpTime());
   }

   public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundsetbordersizepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetbordersizepacket, this, this.minecraft);
      this.level.getWorldBorder().setSize(clientboundsetbordersizepacket.getSize());
   }

   public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundsetborderwarningdistancepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetborderwarningdistancepacket, this, this.minecraft);
      this.level.getWorldBorder().setWarningBlocks(clientboundsetborderwarningdistancepacket.getWarningBlocks());
   }

   public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundsetborderwarningdelaypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetborderwarningdelaypacket, this, this.minecraft);
      this.level.getWorldBorder().setWarningTime(clientboundsetborderwarningdelaypacket.getWarningDelay());
   }

   public void handleTitlesClear(ClientboundClearTitlesPacket clientboundcleartitlespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcleartitlespacket, this, this.minecraft);
      this.minecraft.gui.clear();
      if (clientboundcleartitlespacket.shouldResetTimes()) {
         this.minecraft.gui.resetTitleTimes();
      }

   }

   public void handleServerData(ClientboundServerDataPacket clientboundserverdatapacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundserverdatapacket, this, this.minecraft);
      if (this.serverData != null) {
         this.serverData.motd = clientboundserverdatapacket.getMotd();
         clientboundserverdatapacket.getIconBytes().ifPresent(this.serverData::setIconBytes);
         this.serverData.setEnforcesSecureChat(clientboundserverdatapacket.enforcesSecureChat());
         ServerList.saveSingleServer(this.serverData);
         if (!clientboundserverdatapacket.enforcesSecureChat()) {
            SystemToast systemtoast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
            this.minecraft.getToasts().addToast(systemtoast);
         }

      }
   }

   public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundcustomchatcompletionspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcustomchatcompletionspacket, this, this.minecraft);
      this.suggestionsProvider.modifyCustomCompletions(clientboundcustomchatcompletionspacket.action(), clientboundcustomchatcompletionspacket.entries());
   }

   public void setActionBarText(ClientboundSetActionBarTextPacket clientboundsetactionbartextpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetactionbartextpacket, this, this.minecraft);
      this.minecraft.gui.setOverlayMessage(clientboundsetactionbartextpacket.getText(), false);
   }

   public void setTitleText(ClientboundSetTitleTextPacket clientboundsettitletextpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsettitletextpacket, this, this.minecraft);
      this.minecraft.gui.setTitle(clientboundsettitletextpacket.getText());
   }

   public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundsetsubtitletextpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetsubtitletextpacket, this, this.minecraft);
      this.minecraft.gui.setSubtitle(clientboundsetsubtitletextpacket.getText());
   }

   public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundsettitlesanimationpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsettitlesanimationpacket, this, this.minecraft);
      this.minecraft.gui.setTimes(clientboundsettitlesanimationpacket.getFadeIn(), clientboundsettitlesanimationpacket.getStay(), clientboundsettitlesanimationpacket.getFadeOut());
   }

   public void handleTabListCustomisation(ClientboundTabListPacket clientboundtablistpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundtablistpacket, this, this.minecraft);
      this.minecraft.gui.getTabList().setHeader(clientboundtablistpacket.getHeader().getString().isEmpty() ? null : clientboundtablistpacket.getHeader());
      this.minecraft.gui.getTabList().setFooter(clientboundtablistpacket.getFooter().getString().isEmpty() ? null : clientboundtablistpacket.getFooter());
   }

   public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundremovemobeffectpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundremovemobeffectpacket, this, this.minecraft);
      Entity entity = clientboundremovemobeffectpacket.getEntity(this.level);
      if (entity instanceof LivingEntity) {
         ((LivingEntity)entity).removeEffectNoUpdate(clientboundremovemobeffectpacket.getEffect());
      }

   }

   public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket clientboundplayerinforemovepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerinforemovepacket, this, this.minecraft);

      for(UUID uuid : clientboundplayerinforemovepacket.profileIds()) {
         this.minecraft.getPlayerSocialManager().removePlayer(uuid);
         PlayerInfo playerinfo = this.playerInfoMap.remove(uuid);
         if (playerinfo != null) {
            this.listedPlayers.remove(playerinfo);
         }
      }

   }

   public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket clientboundplayerinfoupdatepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerinfoupdatepacket, this, this.minecraft);

      for(ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket_entry : clientboundplayerinfoupdatepacket.newEntries()) {
         PlayerInfo playerinfo = new PlayerInfo(clientboundplayerinfoupdatepacket_entry.profile(), this.enforcesSecureChat());
         if (this.playerInfoMap.putIfAbsent(clientboundplayerinfoupdatepacket_entry.profileId(), playerinfo) == null) {
            this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
         }
      }

      for(ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket_entry1 : clientboundplayerinfoupdatepacket.entries()) {
         PlayerInfo playerinfo1 = this.playerInfoMap.get(clientboundplayerinfoupdatepacket_entry1.profileId());
         if (playerinfo1 == null) {
            LOGGER.warn("Ignoring player info update for unknown player {}", (Object)clientboundplayerinfoupdatepacket_entry1.profileId());
         } else {
            for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket_action : clientboundplayerinfoupdatepacket.actions()) {
               this.applyPlayerInfoUpdate(clientboundplayerinfoupdatepacket_action, clientboundplayerinfoupdatepacket_entry1, playerinfo1);
            }
         }
      }

   }

   private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket_action, ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket_entry, PlayerInfo playerinfo) {
      switch (clientboundplayerinfoupdatepacket_action) {
         case INITIALIZE_CHAT:
            this.initializeChatSession(clientboundplayerinfoupdatepacket_entry, playerinfo);
            break;
         case UPDATE_GAME_MODE:
            if (playerinfo.getGameMode() != clientboundplayerinfoupdatepacket_entry.gameMode() && this.minecraft.player != null && this.minecraft.player.getUUID().equals(clientboundplayerinfoupdatepacket_entry.profileId())) {
               this.minecraft.player.onGameModeChanged(clientboundplayerinfoupdatepacket_entry.gameMode());
            }

            playerinfo.setGameMode(clientboundplayerinfoupdatepacket_entry.gameMode());
            break;
         case UPDATE_LISTED:
            if (clientboundplayerinfoupdatepacket_entry.listed()) {
               this.listedPlayers.add(playerinfo);
            } else {
               this.listedPlayers.remove(playerinfo);
            }
            break;
         case UPDATE_LATENCY:
            playerinfo.setLatency(clientboundplayerinfoupdatepacket_entry.latency());
            break;
         case UPDATE_DISPLAY_NAME:
            playerinfo.setTabListDisplayName(clientboundplayerinfoupdatepacket_entry.displayName());
      }

   }

   private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket_entry, PlayerInfo playerinfo) {
      GameProfile gameprofile = playerinfo.getProfile();
      SignatureValidator signaturevalidator = this.minecraft.getProfileKeySignatureValidator();
      if (signaturevalidator == null) {
         LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)gameprofile.getName());
         playerinfo.clearChatSession(this.enforcesSecureChat());
      } else {
         RemoteChatSession.Data remotechatsession_data = clientboundplayerinfoupdatepacket_entry.chatSession();
         if (remotechatsession_data != null) {
            try {
               RemoteChatSession remotechatsession = remotechatsession_data.validate(gameprofile, signaturevalidator, ProfilePublicKey.EXPIRY_GRACE_PERIOD);
               playerinfo.setChatSession(remotechatsession);
            } catch (ProfilePublicKey.ValidationException var7) {
               LOGGER.error("Failed to validate profile key for player: '{}'", gameprofile.getName(), var7);
               playerinfo.clearChatSession(this.enforcesSecureChat());
            }
         } else {
            playerinfo.clearChatSession(this.enforcesSecureChat());
         }

      }
   }

   private boolean enforcesSecureChat() {
      return this.serverData != null && this.serverData.enforcesSecureChat();
   }

   public void handleKeepAlive(ClientboundKeepAlivePacket clientboundkeepalivepacket) {
      this.sendWhen(new ServerboundKeepAlivePacket(clientboundkeepalivepacket.getId()), () -> !RenderSystem.isFrozenAtPollEvents(), Duration.ofMinutes(1L));
   }

   private void sendWhen(Packet<ServerGamePacketListener> packet, BooleanSupplier booleansupplier, Duration duration) {
      if (booleansupplier.getAsBoolean()) {
         this.send(packet);
      } else {
         this.deferredPackets.add(new ClientPacketListener.DeferredPacket(packet, booleansupplier, Util.getMillis() + duration.toMillis()));
      }

   }

   private void sendDeferredPackets() {
      Iterator<ClientPacketListener.DeferredPacket> iterator = this.deferredPackets.iterator();

      while(iterator.hasNext()) {
         ClientPacketListener.DeferredPacket clientpacketlistener_deferredpacket = iterator.next();
         if (clientpacketlistener_deferredpacket.sendCondition().getAsBoolean()) {
            this.send(clientpacketlistener_deferredpacket.packet);
            iterator.remove();
         } else if (clientpacketlistener_deferredpacket.expirationTime() <= Util.getMillis()) {
            iterator.remove();
         }
      }

   }

   public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundplayerabilitiespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplayerabilitiespacket, this, this.minecraft);
      Player player = this.minecraft.player;
      player.getAbilities().flying = clientboundplayerabilitiespacket.isFlying();
      player.getAbilities().instabuild = clientboundplayerabilitiespacket.canInstabuild();
      player.getAbilities().invulnerable = clientboundplayerabilitiespacket.isInvulnerable();
      player.getAbilities().mayfly = clientboundplayerabilitiespacket.canFly();
      player.getAbilities().setFlyingSpeed(clientboundplayerabilitiespacket.getFlyingSpeed());
      player.getAbilities().setWalkingSpeed(clientboundplayerabilitiespacket.getWalkingSpeed());
   }

   public void handleSoundEvent(ClientboundSoundPacket clientboundsoundpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsoundpacket, this, this.minecraft);
      this.minecraft.level.playSeededSound(this.minecraft.player, clientboundsoundpacket.getX(), clientboundsoundpacket.getY(), clientboundsoundpacket.getZ(), clientboundsoundpacket.getSound(), clientboundsoundpacket.getSource(), clientboundsoundpacket.getVolume(), clientboundsoundpacket.getPitch(), clientboundsoundpacket.getSeed());
   }

   public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundsoundentitypacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsoundentitypacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundsoundentitypacket.getId());
      if (entity != null) {
         this.minecraft.level.playSeededSound(this.minecraft.player, entity, clientboundsoundentitypacket.getSound(), clientboundsoundentitypacket.getSource(), clientboundsoundentitypacket.getVolume(), clientboundsoundentitypacket.getPitch(), clientboundsoundentitypacket.getSeed());
      }
   }

   public void handleResourcePack(ClientboundResourcePackPacket clientboundresourcepackpacket) {
      URL url = parseResourcePackUrl(clientboundresourcepackpacket.getUrl());
      if (url == null) {
         this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
      } else {
         String s = clientboundresourcepackpacket.getHash();
         boolean flag = clientboundresourcepackpacket.isRequired();
         if (this.serverData != null && this.serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
            this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
            this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(url, s, true));
         } else if (this.serverData != null && this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT && (!flag || this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED)) {
            this.send(ServerboundResourcePackPacket.Action.DECLINED);
            if (flag) {
               this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
            }
         } else {
            this.minecraft.execute(() -> this.minecraft.setScreen(new ConfirmScreen((flag3) -> {
                  this.minecraft.setScreen((Screen)null);
                  if (flag3) {
                     if (this.serverData != null) {
                        this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                     }

                     this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                     this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(url, s, true));
                  } else {
                     this.send(ServerboundResourcePackPacket.Action.DECLINED);
                     if (flag) {
                        this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                     } else if (this.serverData != null) {
                        this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                     }
                  }

                  if (this.serverData != null) {
                     ServerList.saveSingleServer(this.serverData);
                  }

               }, flag ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"), preparePackPrompt(flag ? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD) : Component.translatable("multiplayer.texturePrompt.line2"), clientboundresourcepackpacket.getPrompt()), flag ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, (Component)(flag ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO))));
         }

      }
   }

   private static Component preparePackPrompt(Component component, @Nullable Component component1) {
      return (Component)(component1 == null ? component : Component.translatable("multiplayer.texturePrompt.serverPrompt", component, component1));
   }

   @Nullable
   private static URL parseResourcePackUrl(String s) {
      try {
         URL url = new URL(s);
         String s1 = url.getProtocol();
         return !"http".equals(s1) && !"https".equals(s1) ? null : url;
      } catch (MalformedURLException var3) {
         return null;
      }
   }

   private void downloadCallback(CompletableFuture<?> completablefuture) {
      completablefuture.thenRun(() -> this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)).exceptionally((throwable) -> {
         this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
         return null;
      });
   }

   private void send(ServerboundResourcePackPacket.Action serverboundresourcepackpacket_action) {
      this.connection.send(new ServerboundResourcePackPacket(serverboundresourcepackpacket_action));
   }

   public void handleBossUpdate(ClientboundBossEventPacket clientboundbosseventpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundbosseventpacket, this, this.minecraft);
      this.minecraft.gui.getBossOverlay().update(clientboundbosseventpacket);
   }

   public void handleItemCooldown(ClientboundCooldownPacket clientboundcooldownpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcooldownpacket, this, this.minecraft);
      if (clientboundcooldownpacket.getDuration() == 0) {
         this.minecraft.player.getCooldowns().removeCooldown(clientboundcooldownpacket.getItem());
      } else {
         this.minecraft.player.getCooldowns().addCooldown(clientboundcooldownpacket.getItem(), clientboundcooldownpacket.getDuration());
      }

   }

   public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundmovevehiclepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundmovevehiclepacket, this, this.minecraft);
      Entity entity = this.minecraft.player.getRootVehicle();
      if (entity != this.minecraft.player && entity.isControlledByLocalInstance()) {
         entity.absMoveTo(clientboundmovevehiclepacket.getX(), clientboundmovevehiclepacket.getY(), clientboundmovevehiclepacket.getZ(), clientboundmovevehiclepacket.getYRot(), clientboundmovevehiclepacket.getXRot());
         this.connection.send(new ServerboundMoveVehiclePacket(entity));
      }

   }

   public void handleOpenBook(ClientboundOpenBookPacket clientboundopenbookpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundopenbookpacket, this, this.minecraft);
      ItemStack itemstack = this.minecraft.player.getItemInHand(clientboundopenbookpacket.getHand());
      if (itemstack.is(Items.WRITTEN_BOOK)) {
         this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
      }

   }

   public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundcustompayloadpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundcustompayloadpacket, this, this.minecraft);
      ResourceLocation resourcelocation = clientboundcustompayloadpacket.getIdentifier();
      FriendlyByteBuf friendlybytebuf = null;

      try {
         friendlybytebuf = clientboundcustompayloadpacket.getData();
         if (ClientboundCustomPayloadPacket.BRAND.equals(resourcelocation)) {
            String s = friendlybytebuf.readUtf();
            this.minecraft.player.setServerBrand(s);
            this.telemetryManager.onServerBrandReceived(s);
         } else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourcelocation)) {
            int i = friendlybytebuf.readInt();
            float f = friendlybytebuf.readFloat();
            Path path = Path.createFromStream(friendlybytebuf);
            this.minecraft.debugRenderer.pathfindingRenderer.addPath(i, path, f);
         } else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourcelocation)) {
            long j = friendlybytebuf.readVarLong();
            BlockPos blockpos = friendlybytebuf.readBlockPos();
            ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(j, blockpos);
         } else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourcelocation)) {
            DimensionType dimensiontype = this.registryAccess.compositeAccess().<DimensionType>registryOrThrow(Registries.DIMENSION_TYPE).get(friendlybytebuf.readResourceLocation());
            BoundingBox boundingbox = new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
            int k = friendlybytebuf.readInt();
            List<BoundingBox> list = Lists.newArrayList();
            List<Boolean> list1 = Lists.newArrayList();

            for(int l = 0; l < k; ++l) {
               list.add(new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt()));
               list1.add(friendlybytebuf.readBoolean());
            }

            this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingbox, list, list1, dimensiontype);
         } else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(resourcelocation)) {
            ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer).addPos(friendlybytebuf.readBlockPos(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
         } else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(resourcelocation)) {
            int i1 = friendlybytebuf.readInt();

            for(int j1 = 0; j1 < i1; ++j1) {
               this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlybytebuf.readSectionPos());
            }

            int k1 = friendlybytebuf.readInt();

            for(int l1 = 0; l1 < k1; ++l1) {
               this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlybytebuf.readSectionPos());
            }
         } else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourcelocation)) {
            BlockPos blockpos1 = friendlybytebuf.readBlockPos();
            String s1 = friendlybytebuf.readUtf();
            int i2 = friendlybytebuf.readInt();
            BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo = new BrainDebugRenderer.PoiInfo(blockpos1, s1, i2);
            this.minecraft.debugRenderer.brainDebugRenderer.addPoi(braindebugrenderer_poiinfo);
         } else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourcelocation)) {
            BlockPos blockpos2 = friendlybytebuf.readBlockPos();
            this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockpos2);
         } else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourcelocation)) {
            BlockPos blockpos3 = friendlybytebuf.readBlockPos();
            int j2 = friendlybytebuf.readInt();
            this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockpos3, j2);
         } else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourcelocation)) {
            BlockPos blockpos4 = friendlybytebuf.readBlockPos();
            int k2 = friendlybytebuf.readInt();
            int l2 = friendlybytebuf.readInt();
            List<GoalSelectorDebugRenderer.DebugGoal> list2 = Lists.newArrayList();

            for(int i3 = 0; i3 < l2; ++i3) {
               int j3 = friendlybytebuf.readInt();
               boolean flag = friendlybytebuf.readBoolean();
               String s2 = friendlybytebuf.readUtf(255);
               list2.add(new GoalSelectorDebugRenderer.DebugGoal(blockpos4, j3, s2, flag));
            }

            this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(k2, list2);
         } else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourcelocation)) {
            int k3 = friendlybytebuf.readInt();
            Collection<BlockPos> collection = Lists.newArrayList();

            for(int l3 = 0; l3 < k3; ++l3) {
               collection.add(friendlybytebuf.readBlockPos());
            }

            this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(collection);
         } else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(resourcelocation)) {
            double d0 = friendlybytebuf.readDouble();
            double d1 = friendlybytebuf.readDouble();
            double d2 = friendlybytebuf.readDouble();
            Position position = new PositionImpl(d0, d1, d2);
            UUID uuid = friendlybytebuf.readUUID();
            int i4 = friendlybytebuf.readInt();
            String s3 = friendlybytebuf.readUtf();
            String s4 = friendlybytebuf.readUtf();
            int j4 = friendlybytebuf.readInt();
            float f1 = friendlybytebuf.readFloat();
            float f2 = friendlybytebuf.readFloat();
            String s5 = friendlybytebuf.readUtf();
            Path path1 = friendlybytebuf.readNullable(Path::createFromStream);
            boolean flag1 = friendlybytebuf.readBoolean();
            int k4 = friendlybytebuf.readInt();
            BrainDebugRenderer.BrainDump braindebugrenderer_braindump = new BrainDebugRenderer.BrainDump(uuid, i4, s3, s4, j4, f1, f2, position, s5, path1, flag1, k4);
            int l4 = friendlybytebuf.readVarInt();

            for(int i5 = 0; i5 < l4; ++i5) {
               String s6 = friendlybytebuf.readUtf();
               braindebugrenderer_braindump.activities.add(s6);
            }

            int j5 = friendlybytebuf.readVarInt();

            for(int k5 = 0; k5 < j5; ++k5) {
               String s7 = friendlybytebuf.readUtf();
               braindebugrenderer_braindump.behaviors.add(s7);
            }

            int l5 = friendlybytebuf.readVarInt();

            for(int i6 = 0; i6 < l5; ++i6) {
               String s8 = friendlybytebuf.readUtf();
               braindebugrenderer_braindump.memories.add(s8);
            }

            int j6 = friendlybytebuf.readVarInt();

            for(int k6 = 0; k6 < j6; ++k6) {
               BlockPos blockpos5 = friendlybytebuf.readBlockPos();
               braindebugrenderer_braindump.pois.add(blockpos5);
            }

            int l6 = friendlybytebuf.readVarInt();

            for(int i7 = 0; i7 < l6; ++i7) {
               BlockPos blockpos6 = friendlybytebuf.readBlockPos();
               braindebugrenderer_braindump.potentialPois.add(blockpos6);
            }

            int j7 = friendlybytebuf.readVarInt();

            for(int k7 = 0; k7 < j7; ++k7) {
               String s9 = friendlybytebuf.readUtf();
               braindebugrenderer_braindump.gossips.add(s9);
            }

            this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(braindebugrenderer_braindump);
         } else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourcelocation)) {
            double d3 = friendlybytebuf.readDouble();
            double d4 = friendlybytebuf.readDouble();
            double d5 = friendlybytebuf.readDouble();
            Position position1 = new PositionImpl(d3, d4, d5);
            UUID uuid1 = friendlybytebuf.readUUID();
            int l7 = friendlybytebuf.readInt();
            BlockPos blockpos7 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
            BlockPos blockpos8 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
            int i8 = friendlybytebuf.readInt();
            Path path2 = friendlybytebuf.readNullable(Path::createFromStream);
            BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo = new BeeDebugRenderer.BeeInfo(uuid1, l7, position1, path2, blockpos7, blockpos8, i8);
            int j8 = friendlybytebuf.readVarInt();

            for(int k8 = 0; k8 < j8; ++k8) {
               String s10 = friendlybytebuf.readUtf();
               beedebugrenderer_beeinfo.goals.add(s10);
            }

            int l8 = friendlybytebuf.readVarInt();

            for(int i9 = 0; i9 < l8; ++i9) {
               BlockPos blockpos9 = friendlybytebuf.readBlockPos();
               beedebugrenderer_beeinfo.blacklistedHives.add(blockpos9);
            }

            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beedebugrenderer_beeinfo);
         } else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourcelocation)) {
            BlockPos blockpos10 = friendlybytebuf.readBlockPos();
            String s11 = friendlybytebuf.readUtf();
            int j9 = friendlybytebuf.readInt();
            int k9 = friendlybytebuf.readInt();
            boolean flag2 = friendlybytebuf.readBoolean();
            BeeDebugRenderer.HiveInfo beedebugrenderer_hiveinfo = new BeeDebugRenderer.HiveInfo(blockpos10, s11, j9, k9, flag2, this.level.getGameTime());
            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(beedebugrenderer_hiveinfo);
         } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourcelocation)) {
            this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
         } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourcelocation)) {
            BlockPos blockpos11 = friendlybytebuf.readBlockPos();
            int l9 = friendlybytebuf.readInt();
            String s12 = friendlybytebuf.readUtf();
            int i10 = friendlybytebuf.readInt();
            this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockpos11, l9, s12, i10);
         } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(resourcelocation)) {
            GameEvent gameevent = BuiltInRegistries.GAME_EVENT.get(new ResourceLocation(friendlybytebuf.readUtf()));
            Vec3 vec3 = new Vec3(friendlybytebuf.readDouble(), friendlybytebuf.readDouble(), friendlybytebuf.readDouble());
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameevent, vec3);
         } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(resourcelocation)) {
            ResourceLocation resourcelocation1 = friendlybytebuf.readResourceLocation();
            PositionSource positionsource = BuiltInRegistries.POSITION_SOURCE_TYPE.getOptional(resourcelocation1).orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourcelocation1)).read(friendlybytebuf);
            int j10 = friendlybytebuf.readVarInt();
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(positionsource, j10);
         } else {
            LOGGER.warn("Unknown custom packed identifier: {}", (Object)resourcelocation);
         }
      } finally {
         if (friendlybytebuf != null) {
            friendlybytebuf.release();
         }

      }

   }

   public void handleAddObjective(ClientboundSetObjectivePacket clientboundsetobjectivepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetobjectivepacket, this, this.minecraft);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = clientboundsetobjectivepacket.getObjectiveName();
      if (clientboundsetobjectivepacket.getMethod() == 0) {
         scoreboard.addObjective(s, ObjectiveCriteria.DUMMY, clientboundsetobjectivepacket.getDisplayName(), clientboundsetobjectivepacket.getRenderType());
      } else if (scoreboard.hasObjective(s)) {
         Objective objective = scoreboard.getObjective(s);
         if (clientboundsetobjectivepacket.getMethod() == 1) {
            scoreboard.removeObjective(objective);
         } else if (clientboundsetobjectivepacket.getMethod() == 2) {
            objective.setRenderType(clientboundsetobjectivepacket.getRenderType());
            objective.setDisplayName(clientboundsetobjectivepacket.getDisplayName());
         }
      }

   }

   public void handleSetScore(ClientboundSetScorePacket clientboundsetscorepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetscorepacket, this, this.minecraft);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = clientboundsetscorepacket.getObjectiveName();
      switch (clientboundsetscorepacket.getMethod()) {
         case CHANGE:
            Objective objective = scoreboard.getOrCreateObjective(s);
            Score score = scoreboard.getOrCreatePlayerScore(clientboundsetscorepacket.getOwner(), objective);
            score.setScore(clientboundsetscorepacket.getScore());
            break;
         case REMOVE:
            scoreboard.resetPlayerScore(clientboundsetscorepacket.getOwner(), scoreboard.getObjective(s));
      }

   }

   public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundsetdisplayobjectivepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetdisplayobjectivepacket, this, this.minecraft);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = clientboundsetdisplayobjectivepacket.getObjectiveName();
      Objective objective = s == null ? null : scoreboard.getOrCreateObjective(s);
      scoreboard.setDisplayObjective(clientboundsetdisplayobjectivepacket.getSlot(), objective);
   }

   public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundsetplayerteampacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetplayerteampacket, this, this.minecraft);
      Scoreboard scoreboard = this.level.getScoreboard();
      ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket_action = clientboundsetplayerteampacket.getTeamAction();
      PlayerTeam playerteam;
      if (clientboundsetplayerteampacket_action == ClientboundSetPlayerTeamPacket.Action.ADD) {
         playerteam = scoreboard.addPlayerTeam(clientboundsetplayerteampacket.getName());
      } else {
         playerteam = scoreboard.getPlayerTeam(clientboundsetplayerteampacket.getName());
         if (playerteam == null) {
            LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", clientboundsetplayerteampacket.getName(), clientboundsetplayerteampacket.getTeamAction(), clientboundsetplayerteampacket.getPlayerAction());
            return;
         }
      }

      Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = clientboundsetplayerteampacket.getParameters();
      optional.ifPresent((clientboundsetplayerteampacket_parameters) -> {
         playerteam.setDisplayName(clientboundsetplayerteampacket_parameters.getDisplayName());
         playerteam.setColor(clientboundsetplayerteampacket_parameters.getColor());
         playerteam.unpackOptions(clientboundsetplayerteampacket_parameters.getOptions());
         Team.Visibility team_visibility = Team.Visibility.byName(clientboundsetplayerteampacket_parameters.getNametagVisibility());
         if (team_visibility != null) {
            playerteam.setNameTagVisibility(team_visibility);
         }

         Team.CollisionRule team_collisionrule = Team.CollisionRule.byName(clientboundsetplayerteampacket_parameters.getCollisionRule());
         if (team_collisionrule != null) {
            playerteam.setCollisionRule(team_collisionrule);
         }

         playerteam.setPlayerPrefix(clientboundsetplayerteampacket_parameters.getPlayerPrefix());
         playerteam.setPlayerSuffix(clientboundsetplayerteampacket_parameters.getPlayerSuffix());
      });
      ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket_action1 = clientboundsetplayerteampacket.getPlayerAction();
      if (clientboundsetplayerteampacket_action1 == ClientboundSetPlayerTeamPacket.Action.ADD) {
         for(String s : clientboundsetplayerteampacket.getPlayers()) {
            scoreboard.addPlayerToTeam(s, playerteam);
         }
      } else if (clientboundsetplayerteampacket_action1 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
         for(String s1 : clientboundsetplayerteampacket.getPlayers()) {
            scoreboard.removePlayerFromTeam(s1, playerteam);
         }
      }

      if (clientboundsetplayerteampacket_action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
         scoreboard.removePlayerTeam(playerteam);
      }

   }

   public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundlevelparticlespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundlevelparticlespacket, this, this.minecraft);
      if (clientboundlevelparticlespacket.getCount() == 0) {
         double d0 = (double)(clientboundlevelparticlespacket.getMaxSpeed() * clientboundlevelparticlespacket.getXDist());
         double d1 = (double)(clientboundlevelparticlespacket.getMaxSpeed() * clientboundlevelparticlespacket.getYDist());
         double d2 = (double)(clientboundlevelparticlespacket.getMaxSpeed() * clientboundlevelparticlespacket.getZDist());

         try {
            this.level.addParticle(clientboundlevelparticlespacket.getParticle(), clientboundlevelparticlespacket.isOverrideLimiter(), clientboundlevelparticlespacket.getX(), clientboundlevelparticlespacket.getY(), clientboundlevelparticlespacket.getZ(), d0, d1, d2);
         } catch (Throwable var17) {
            LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundlevelparticlespacket.getParticle());
         }
      } else {
         for(int i = 0; i < clientboundlevelparticlespacket.getCount(); ++i) {
            double d3 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getXDist();
            double d4 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getYDist();
            double d5 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getZDist();
            double d6 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getMaxSpeed();
            double d7 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getMaxSpeed();
            double d8 = this.random.nextGaussian() * (double)clientboundlevelparticlespacket.getMaxSpeed();

            try {
               this.level.addParticle(clientboundlevelparticlespacket.getParticle(), clientboundlevelparticlespacket.isOverrideLimiter(), clientboundlevelparticlespacket.getX() + d3, clientboundlevelparticlespacket.getY() + d4, clientboundlevelparticlespacket.getZ() + d5, d6, d7, d8);
            } catch (Throwable var16) {
               LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundlevelparticlespacket.getParticle());
               return;
            }
         }
      }

   }

   public void handlePing(ClientboundPingPacket clientboundpingpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundpingpacket, this, this.minecraft);
      this.send(new ServerboundPongPacket(clientboundpingpacket.getId()));
   }

   public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundupdateattributespacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundupdateattributespacket, this, this.minecraft);
      Entity entity = this.level.getEntity(clientboundupdateattributespacket.getEntityId());
      if (entity != null) {
         if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
         } else {
            AttributeMap attributemap = ((LivingEntity)entity).getAttributes();

            for(ClientboundUpdateAttributesPacket.AttributeSnapshot clientboundupdateattributespacket_attributesnapshot : clientboundupdateattributespacket.getValues()) {
               AttributeInstance attributeinstance = attributemap.getInstance(clientboundupdateattributespacket_attributesnapshot.getAttribute());
               if (attributeinstance == null) {
                  LOGGER.warn("Entity {} does not have attribute {}", entity, BuiltInRegistries.ATTRIBUTE.getKey(clientboundupdateattributespacket_attributesnapshot.getAttribute()));
               } else {
                  attributeinstance.setBaseValue(clientboundupdateattributespacket_attributesnapshot.getBase());
                  attributeinstance.removeModifiers();

                  for(AttributeModifier attributemodifier : clientboundupdateattributespacket_attributesnapshot.getModifiers()) {
                     attributeinstance.addTransientModifier(attributemodifier);
                  }
               }
            }

         }
      }
   }

   public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundplaceghostrecipepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundplaceghostrecipepacket, this, this.minecraft);
      AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;
      if (abstractcontainermenu.containerId == clientboundplaceghostrecipepacket.getContainerId()) {
         this.recipeManager.byKey(clientboundplaceghostrecipepacket.getRecipe()).ifPresent((recipe) -> {
            if (this.minecraft.screen instanceof RecipeUpdateListener) {
               RecipeBookComponent recipebookcomponent = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
               recipebookcomponent.setupGhostRecipe(recipe, abstractcontainermenu.slots);
            }

         });
      }
   }

   public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundlightupdatepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundlightupdatepacket, this, this.minecraft);
      int i = clientboundlightupdatepacket.getX();
      int j = clientboundlightupdatepacket.getZ();
      ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = clientboundlightupdatepacket.getLightData();
      this.level.queueLightUpdate(() -> this.applyLightData(i, j, clientboundlightupdatepacketdata));
   }

   private void applyLightData(int i, int j, ClientboundLightUpdatePacketData clientboundlightupdatepacketdata) {
      LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
      BitSet bitset = clientboundlightupdatepacketdata.getSkyYMask();
      BitSet bitset1 = clientboundlightupdatepacketdata.getEmptySkyYMask();
      Iterator<byte[]> iterator = clientboundlightupdatepacketdata.getSkyUpdates().iterator();
      this.readSectionList(i, j, levellightengine, LightLayer.SKY, bitset, bitset1, iterator);
      BitSet bitset2 = clientboundlightupdatepacketdata.getBlockYMask();
      BitSet bitset3 = clientboundlightupdatepacketdata.getEmptyBlockYMask();
      Iterator<byte[]> iterator1 = clientboundlightupdatepacketdata.getBlockUpdates().iterator();
      this.readSectionList(i, j, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1);
      levellightengine.setLightEnabled(new ChunkPos(i, j), true);
   }

   public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundmerchantofferspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundmerchantofferspacket, this, this.minecraft);
      AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;
      if (clientboundmerchantofferspacket.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof MerchantMenu merchantmenu) {
         merchantmenu.setOffers(new MerchantOffers(clientboundmerchantofferspacket.getOffers().createTag()));
         merchantmenu.setXp(clientboundmerchantofferspacket.getVillagerXp());
         merchantmenu.setMerchantLevel(clientboundmerchantofferspacket.getVillagerLevel());
         merchantmenu.setShowProgressBar(clientboundmerchantofferspacket.showProgress());
         merchantmenu.setCanRestock(clientboundmerchantofferspacket.canRestock());
      }

   }

   public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundsetchunkcacheradiuspacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetchunkcacheradiuspacket, this, this.minecraft);
      this.serverChunkRadius = clientboundsetchunkcacheradiuspacket.getRadius();
      this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
      this.level.getChunkSource().updateViewRadius(clientboundsetchunkcacheradiuspacket.getRadius());
   }

   public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundsetsimulationdistancepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetsimulationdistancepacket, this, this.minecraft);
      this.serverSimulationDistance = clientboundsetsimulationdistancepacket.simulationDistance();
      this.level.setServerSimulationDistance(this.serverSimulationDistance);
   }

   public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundsetchunkcachecenterpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundsetchunkcachecenterpacket, this, this.minecraft);
      this.level.getChunkSource().updateViewCenter(clientboundsetchunkcachecenterpacket.getX(), clientboundsetchunkcachecenterpacket.getZ());
   }

   public void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundblockchangedackpacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundblockchangedackpacket, this, this.minecraft);
      this.level.handleBlockChangedAck(clientboundblockchangedackpacket.sequence());
   }

   public void handleBundlePacket(ClientboundBundlePacket clientboundbundlepacket) {
      PacketUtils.ensureRunningOnSameThread(clientboundbundlepacket, this, this.minecraft);

      for(Packet<ClientGamePacketListener> packet : clientboundbundlepacket.subPackets()) {
         packet.handle(this);
      }

   }

   private void readSectionList(int i, int j, LevelLightEngine levellightengine, LightLayer lightlayer, BitSet bitset, BitSet bitset1, Iterator<byte[]> iterator) {
      for(int k = 0; k < levellightengine.getLightSectionCount(); ++k) {
         int l = levellightengine.getMinLightSection() + k;
         boolean flag = bitset.get(k);
         boolean flag1 = bitset1.get(k);
         if (flag || flag1) {
            levellightengine.queueSectionData(lightlayer, SectionPos.of(i, l, j), flag ? new DataLayer((byte[])iterator.next().clone()) : new DataLayer());
            this.level.setSectionDirtyWithNeighbors(i, l, j);
         }
      }

   }

   public Connection getConnection() {
      return this.connection;
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }

   public Collection<PlayerInfo> getListedOnlinePlayers() {
      return this.listedPlayers;
   }

   public Collection<PlayerInfo> getOnlinePlayers() {
      return this.playerInfoMap.values();
   }

   public Collection<UUID> getOnlinePlayerIds() {
      return this.playerInfoMap.keySet();
   }

   @Nullable
   public PlayerInfo getPlayerInfo(UUID uuid) {
      return this.playerInfoMap.get(uuid);
   }

   @Nullable
   public PlayerInfo getPlayerInfo(String s) {
      for(PlayerInfo playerinfo : this.playerInfoMap.values()) {
         if (playerinfo.getProfile().getName().equals(s)) {
            return playerinfo;
         }
      }

      return null;
   }

   public GameProfile getLocalGameProfile() {
      return this.localGameProfile;
   }

   public ClientAdvancements getAdvancements() {
      return this.advancements;
   }

   public CommandDispatcher<SharedSuggestionProvider> getCommands() {
      return this.commands;
   }

   public ClientLevel getLevel() {
      return this.level;
   }

   public DebugQueryHandler getDebugQueryHandler() {
      return this.debugQueryHandler;
   }

   public UUID getId() {
      return this.id;
   }

   public Set<ResourceKey<Level>> levels() {
      return this.levels;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess.compositeAccess();
   }

   public void markMessageAsProcessed(PlayerChatMessage playerchatmessage, boolean flag) {
      MessageSignature messagesignature = playerchatmessage.signature();
      if (messagesignature != null && this.lastSeenMessages.addPending(messagesignature, flag) && this.lastSeenMessages.offset() > 64) {
         this.sendChatAcknowledgement();
      }

   }

   private void sendChatAcknowledgement() {
      int i = this.lastSeenMessages.getAndClearOffset();
      if (i > 0) {
         this.send(new ServerboundChatAckPacket(i));
      }

   }

   public void sendChat(String s) {
      Instant instant = Instant.now();
      long i = Crypt.SaltSupplier.getLong();
      LastSeenMessagesTracker.Update lastseenmessagestracker_update = this.lastSeenMessages.generateAndApplyUpdate();
      MessageSignature messagesignature = this.signedMessageEncoder.pack(new SignedMessageBody(s, instant, i, lastseenmessagestracker_update.lastSeen()));
      this.send(new ServerboundChatPacket(s, instant, i, messagesignature, lastseenmessagestracker_update.update()));
   }

   public void sendCommand(String s) {
      Instant instant = Instant.now();
      long i = Crypt.SaltSupplier.getLong();
      LastSeenMessagesTracker.Update lastseenmessagestracker_update = this.lastSeenMessages.generateAndApplyUpdate();
      ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(s)), (s1) -> {
         SignedMessageBody signedmessagebody = new SignedMessageBody(s1, instant, i, lastseenmessagestracker_update.lastSeen());
         return this.signedMessageEncoder.pack(signedmessagebody);
      });
      this.send(new ServerboundChatCommandPacket(s, instant, i, argumentsignatures, lastseenmessagestracker_update.update()));
   }

   public boolean sendUnsignedCommand(String s) {
      if (SignableCommand.of(this.parseCommand(s)).arguments().isEmpty()) {
         LastSeenMessagesTracker.Update lastseenmessagestracker_update = this.lastSeenMessages.generateAndApplyUpdate();
         this.send(new ServerboundChatCommandPacket(s, Instant.now(), 0L, ArgumentSignatures.EMPTY, lastseenmessagestracker_update.update()));
         return true;
      } else {
         return false;
      }
   }

   private ParseResults<SharedSuggestionProvider> parseCommand(String s) {
      return this.commands.parse(s, this.suggestionsProvider);
   }

   public void tick() {
      if (this.connection.isEncrypted()) {
         ProfileKeyPairManager profilekeypairmanager = this.minecraft.getProfileKeyPairManager();
         if (profilekeypairmanager.shouldRefreshKeyPair()) {
            profilekeypairmanager.prepareKeyPair().thenAcceptAsync((optional) -> optional.ifPresent(this::setKeyPair), this.minecraft);
         }
      }

      this.sendDeferredPackets();
      this.telemetryManager.tick();
   }

   public void setKeyPair(ProfileKeyPair profilekeypair) {
      if (this.localGameProfile.getId().equals(this.minecraft.getUser().getProfileId())) {
         if (this.chatSession == null || !this.chatSession.keyPair().equals(profilekeypair)) {
            this.chatSession = LocalChatSession.create(profilekeypair);
            this.signedMessageEncoder = this.chatSession.createMessageEncoder(this.localGameProfile.getId());
            this.send(new ServerboundChatSessionUpdatePacket(this.chatSession.asRemote().asData()));
         }
      }
   }

   @Nullable
   public ServerData getServerData() {
      return this.serverData;
   }

   public FeatureFlagSet enabledFeatures() {
      return this.enabledFeatures;
   }

   public boolean isFeatureEnabled(FeatureFlagSet featureflagset) {
      return featureflagset.isSubsetOf(this.enabledFeatures());
   }

   static record DeferredPacket(Packet<ServerGamePacketListener> packet, BooleanSupplier sendCondition, long expirationTime) {
      final Packet<ServerGamePacketListener> packet;
   }
}
