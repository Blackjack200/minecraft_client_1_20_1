package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int LATENCY_CHECK_INTERVAL = 15000;
   public static final double MAX_INTERACTION_DISTANCE = Mth.square(6.0D);
   private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
   private static final int TRACKED_MESSAGE_DISCONNECT_THRESHOLD = 4096;
   private static final Component CHAT_VALIDATION_FAILED = Component.translatable("multiplayer.disconnect.chat_validation_failed");
   private final Connection connection;
   private final MinecraftServer server;
   public ServerPlayer player;
   private int tickCount;
   private int ackBlockChangesUpTo = -1;
   private long keepAliveTime;
   private boolean keepAlivePending;
   private long keepAliveChallenge;
   private int chatSpamTickCount;
   private int dropSpamTickCount;
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   @Nullable
   private Entity lastVehicle;
   private double vehicleFirstGoodX;
   private double vehicleFirstGoodY;
   private double vehicleFirstGoodZ;
   private double vehicleLastGoodX;
   private double vehicleLastGoodY;
   private double vehicleLastGoodZ;
   @Nullable
   private Vec3 awaitingPositionFromClient;
   private int awaitingTeleport;
   private int awaitingTeleportTime;
   private boolean clientIsFloating;
   private int aboveGroundTickCount;
   private boolean clientVehicleIsFloating;
   private int aboveGroundVehicleTickCount;
   private int receivedMovePacketCount;
   private int knownMovePacketCount;
   private final AtomicReference<Instant> lastChatTimeStamp = new AtomicReference<>(Instant.EPOCH);
   @Nullable
   private RemoteChatSession chatSession;
   private SignedMessageChain.Decoder signedMessageDecoder;
   private final LastSeenMessagesValidator lastSeenMessages = new LastSeenMessagesValidator(20);
   private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
   private final FutureChain chatMessageChain;

   public ServerGamePacketListenerImpl(MinecraftServer minecraftserver, Connection connection, ServerPlayer serverplayer) {
      this.server = minecraftserver;
      this.connection = connection;
      connection.setListener(this);
      this.player = serverplayer;
      serverplayer.connection = this;
      this.keepAliveTime = Util.getMillis();
      serverplayer.getTextFilter().join();
      this.signedMessageDecoder = minecraftserver.enforceSecureProfile() ? SignedMessageChain.Decoder.REJECT_ALL : SignedMessageChain.Decoder.unsigned(serverplayer.getUUID());
      this.chatMessageChain = new FutureChain(minecraftserver);
   }

   public void tick() {
      if (this.ackBlockChangesUpTo > -1) {
         this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
         this.ackBlockChangesUpTo = -1;
      }

      this.resetPosition();
      this.player.xo = this.player.getX();
      this.player.yo = this.player.getY();
      this.player.zo = this.player.getZ();
      this.player.doTick();
      this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
      ++this.tickCount;
      this.knownMovePacketCount = this.receivedMovePacketCount;
      if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
         if (++this.aboveGroundTickCount > 80) {
            LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getName().getString());
            this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
            return;
         }
      } else {
         this.clientIsFloating = false;
         this.aboveGroundTickCount = 0;
      }

      this.lastVehicle = this.player.getRootVehicle();
      if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
         this.vehicleFirstGoodX = this.lastVehicle.getX();
         this.vehicleFirstGoodY = this.lastVehicle.getY();
         this.vehicleFirstGoodZ = this.lastVehicle.getZ();
         this.vehicleLastGoodX = this.lastVehicle.getX();
         this.vehicleLastGoodY = this.lastVehicle.getY();
         this.vehicleLastGoodZ = this.lastVehicle.getZ();
         if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
            if (++this.aboveGroundVehicleTickCount > 80) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
               this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
               return;
            }
         } else {
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
         }
      } else {
         this.lastVehicle = null;
         this.clientVehicleIsFloating = false;
         this.aboveGroundVehicleTickCount = 0;
      }

      this.server.getProfiler().push("keepAlive");
      long i = Util.getMillis();
      if (i - this.keepAliveTime >= 15000L) {
         if (this.keepAlivePending) {
            this.disconnect(Component.translatable("disconnect.timeout"));
         } else {
            this.keepAlivePending = true;
            this.keepAliveTime = i;
            this.keepAliveChallenge = i;
            this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
         }
      }

      this.server.getProfiler().pop();
      if (this.chatSpamTickCount > 0) {
         --this.chatSpamTickCount;
      }

      if (this.dropSpamTickCount > 0) {
         --this.dropSpamTickCount;
      }

      if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > (long)this.server.getPlayerIdleTimeout() * 1000L * 60L) {
         this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
      }

   }

   public void resetPosition() {
      this.firstGoodX = this.player.getX();
      this.firstGoodY = this.player.getY();
      this.firstGoodZ = this.player.getZ();
      this.lastGoodX = this.player.getX();
      this.lastGoodY = this.player.getY();
      this.lastGoodZ = this.player.getZ();
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }

   private boolean isSingleplayerOwner() {
      return this.server.isSingleplayerOwner(this.player.getGameProfile());
   }

   public void disconnect(Component component) {
      this.connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> this.connection.disconnect(component)));
      this.connection.setReadOnly();
      this.server.executeBlocking(this.connection::handleDisconnection);
   }

   private <T, R> CompletableFuture<R> filterTextPacket(T object, BiFunction<TextFilter, T, CompletableFuture<R>> bifunction) {
      return bifunction.apply(this.player.getTextFilter(), object).thenApply((object1) -> {
         if (!this.isAcceptingMessages()) {
            LOGGER.debug("Ignoring packet due to disconnection");
            throw new CancellationException("disconnected");
         } else {
            return object1;
         }
      });
   }

   private CompletableFuture<FilteredText> filterTextPacket(String s) {
      return this.filterTextPacket(s, TextFilter::processStreamMessage);
   }

   private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> list) {
      return this.filterTextPacket(list, TextFilter::processMessageBundle);
   }

   public void handlePlayerInput(ServerboundPlayerInputPacket serverboundplayerinputpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundplayerinputpacket, this, this.player.serverLevel());
      this.player.setPlayerInput(serverboundplayerinputpacket.getXxa(), serverboundplayerinputpacket.getZza(), serverboundplayerinputpacket.isJumping(), serverboundplayerinputpacket.isShiftKeyDown());
   }

   private static boolean containsInvalidValues(double d0, double d1, double d2, float f, float f1) {
      return Double.isNaN(d0) || Double.isNaN(d1) || Double.isNaN(d2) || !Floats.isFinite(f1) || !Floats.isFinite(f);
   }

   private static double clampHorizontal(double d0) {
      return Mth.clamp(d0, -3.0E7D, 3.0E7D);
   }

   private static double clampVertical(double d0) {
      return Mth.clamp(d0, -2.0E7D, 2.0E7D);
   }

   public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundmovevehiclepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundmovevehiclepacket, this, this.player.serverLevel());
      if (containsInvalidValues(serverboundmovevehiclepacket.getX(), serverboundmovevehiclepacket.getY(), serverboundmovevehiclepacket.getZ(), serverboundmovevehiclepacket.getYRot(), serverboundmovevehiclepacket.getXRot())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
      } else {
         Entity entity = this.player.getRootVehicle();
         if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
            ServerLevel serverlevel = this.player.serverLevel();
            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();
            double d3 = clampHorizontal(serverboundmovevehiclepacket.getX());
            double d4 = clampVertical(serverboundmovevehiclepacket.getY());
            double d5 = clampHorizontal(serverboundmovevehiclepacket.getZ());
            float f = Mth.wrapDegrees(serverboundmovevehiclepacket.getYRot());
            float f1 = Mth.wrapDegrees(serverboundmovevehiclepacket.getXRot());
            double d6 = d3 - this.vehicleFirstGoodX;
            double d7 = d4 - this.vehicleFirstGoodY;
            double d8 = d5 - this.vehicleFirstGoodZ;
            double d9 = entity.getDeltaMovement().lengthSqr();
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;
            if (d10 - d9 > 100.0D && !this.isSingleplayerOwner()) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d6, d7, d8);
               this.connection.send(new ClientboundMoveVehiclePacket(entity));
               return;
            }

            boolean flag = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            d6 = d3 - this.vehicleLastGoodX;
            d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
            d8 = d5 - this.vehicleLastGoodZ;
            boolean flag1 = entity.verticalCollisionBelow;
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               if (livingentity.onClimbable()) {
                  livingentity.resetFallDistance();
               }
            }

            entity.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
            d6 = d3 - entity.getX();
            d7 = d4 - entity.getY();
            if (d7 > -0.5D || d7 < 0.5D) {
               d7 = 0.0D;
            }

            d8 = d5 - entity.getZ();
            d10 = d6 * d6 + d7 * d7 + d8 * d8;
            boolean flag2 = false;
            if (d10 > 0.0625D) {
               flag2 = true;
               LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10));
            }

            entity.absMoveTo(d3, d4, d5, f, f1);
            boolean flag3 = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            if (flag && (flag2 || !flag3)) {
               entity.absMoveTo(d0, d1, d2, f, f1);
               this.connection.send(new ClientboundMoveVehiclePacket(entity));
               return;
            }

            this.player.serverLevel().getChunkSource().move(this.player);
            this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
            this.clientVehicleIsFloating = d7 >= -0.03125D && !flag1 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
            this.vehicleLastGoodX = entity.getX();
            this.vehicleLastGoodY = entity.getY();
            this.vehicleLastGoodZ = entity.getZ();
         }

      }
   }

   private boolean noBlocksAround(Entity entity) {
      return entity.level().getBlockStates(entity.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(BlockBehaviour.BlockStateBase::isAir);
   }

   public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundacceptteleportationpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundacceptteleportationpacket, this, this.player.serverLevel());
      if (serverboundacceptteleportationpacket.getId() == this.awaitingTeleport) {
         if (this.awaitingPositionFromClient == null) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
         }

         this.player.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
         this.lastGoodX = this.awaitingPositionFromClient.x;
         this.lastGoodY = this.awaitingPositionFromClient.y;
         this.lastGoodZ = this.awaitingPositionFromClient.z;
         if (this.player.isChangingDimension()) {
            this.player.hasChangedDimension();
         }

         this.awaitingPositionFromClient = null;
      }

   }

   public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundrecipebookseenrecipepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundrecipebookseenrecipepacket, this, this.player.serverLevel());
      this.server.getRecipeManager().byKey(serverboundrecipebookseenrecipepacket.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
   }

   public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundrecipebookchangesettingspacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundrecipebookchangesettingspacket, this, this.player.serverLevel());
      this.player.getRecipeBook().setBookSetting(serverboundrecipebookchangesettingspacket.getBookType(), serverboundrecipebookchangesettingspacket.isOpen(), serverboundrecipebookchangesettingspacket.isFiltering());
   }

   public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundseenadvancementspacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundseenadvancementspacket, this, this.player.serverLevel());
      if (serverboundseenadvancementspacket.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
         ResourceLocation resourcelocation = serverboundseenadvancementspacket.getTab();
         Advancement advancement = this.server.getAdvancements().getAdvancement(resourcelocation);
         if (advancement != null) {
            this.player.getAdvancements().setSelectedTab(advancement);
         }
      }

   }

   public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundcommandsuggestionpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundcommandsuggestionpacket, this, this.player.serverLevel());
      StringReader stringreader = new StringReader(serverboundcommandsuggestionpacket.getCommand());
      if (stringreader.canRead() && stringreader.peek() == '/') {
         stringreader.skip();
      }

      ParseResults<CommandSourceStack> parseresults = this.server.getCommands().getDispatcher().parse(stringreader, this.player.createCommandSourceStack());
      this.server.getCommands().getDispatcher().getCompletionSuggestions(parseresults).thenAccept((suggestions) -> this.connection.send(new ClientboundCommandSuggestionsPacket(serverboundcommandsuggestionpacket.getId(), suggestions)));
   }

   public void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundsetcommandblockpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetcommandblockpacket, this, this.player.serverLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock basecommandblock = null;
         CommandBlockEntity commandblockentity = null;
         BlockPos blockpos = serverboundsetcommandblockpacket.getPos();
         BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
         if (blockentity instanceof CommandBlockEntity) {
            commandblockentity = (CommandBlockEntity)blockentity;
            basecommandblock = commandblockentity.getCommandBlock();
         }

         String s = serverboundsetcommandblockpacket.getCommand();
         boolean flag = serverboundsetcommandblockpacket.isTrackOutput();
         if (basecommandblock != null) {
            CommandBlockEntity.Mode commandblockentity_mode = commandblockentity.getMode();
            BlockState blockstate = this.player.level().getBlockState(blockpos);
            Direction direction = blockstate.getValue(CommandBlock.FACING);
            BlockState blockstate1;
            switch (serverboundsetcommandblockpacket.getMode()) {
               case SEQUENCE:
                  blockstate1 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                  break;
               case AUTO:
                  blockstate1 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                  break;
               case REDSTONE:
               default:
                  blockstate1 = Blocks.COMMAND_BLOCK.defaultBlockState();
            }

            BlockState blockstate4 = blockstate1.setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(serverboundsetcommandblockpacket.isConditional()));
            if (blockstate4 != blockstate) {
               this.player.level().setBlock(blockpos, blockstate4, 2);
               blockentity.setBlockState(blockstate4);
               this.player.level().getChunkAt(blockpos).setBlockEntity(blockentity);
            }

            basecommandblock.setCommand(s);
            basecommandblock.setTrackOutput(flag);
            if (!flag) {
               basecommandblock.setLastOutput((Component)null);
            }

            commandblockentity.setAutomatic(serverboundsetcommandblockpacket.isAutomatic());
            if (commandblockentity_mode != serverboundsetcommandblockpacket.getMode()) {
               commandblockentity.onModeSwitch();
            }

            basecommandblock.onUpdated();
            if (!StringUtil.isNullOrEmpty(s)) {
               this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", s));
            }
         }

      }
   }

   public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundsetcommandminecartpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetcommandminecartpacket, this, this.player.serverLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock basecommandblock = serverboundsetcommandminecartpacket.getCommandBlock(this.player.level());
         if (basecommandblock != null) {
            basecommandblock.setCommand(serverboundsetcommandminecartpacket.getCommand());
            basecommandblock.setTrackOutput(serverboundsetcommandminecartpacket.isTrackOutput());
            if (!serverboundsetcommandminecartpacket.isTrackOutput()) {
               basecommandblock.setLastOutput((Component)null);
            }

            basecommandblock.onUpdated();
            this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", serverboundsetcommandminecartpacket.getCommand()));
         }

      }
   }

   public void handlePickItem(ServerboundPickItemPacket serverboundpickitempacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundpickitempacket, this, this.player.serverLevel());
      this.player.getInventory().pickSlot(serverboundpickitempacket.getSlot());
      this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected)));
      this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, serverboundpickitempacket.getSlot(), this.player.getInventory().getItem(serverboundpickitempacket.getSlot())));
      this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
   }

   public void handleRenameItem(ServerboundRenameItemPacket serverboundrenameitempacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundrenameitempacket, this, this.player.serverLevel());
      AbstractContainerMenu var3 = this.player.containerMenu;
      if (var3 instanceof AnvilMenu anvilmenu) {
         if (!anvilmenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, anvilmenu);
            return;
         }

         anvilmenu.setItemName(serverboundrenameitempacket.getName());
      }

   }

   public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundsetbeaconpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetbeaconpacket, this, this.player.serverLevel());
      AbstractContainerMenu var3 = this.player.containerMenu;
      if (var3 instanceof BeaconMenu beaconmenu) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            return;
         }

         beaconmenu.updateEffects(serverboundsetbeaconpacket.getPrimary(), serverboundsetbeaconpacket.getSecondary());
      }

   }

   public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundsetstructureblockpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetstructureblockpacket, this, this.player.serverLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = serverboundsetstructureblockpacket.getPos();
         BlockState blockstate = this.player.level().getBlockState(blockpos);
         BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
         if (blockentity instanceof StructureBlockEntity) {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)blockentity;
            structureblockentity.setMode(serverboundsetstructureblockpacket.getMode());
            structureblockentity.setStructureName(serverboundsetstructureblockpacket.getName());
            structureblockentity.setStructurePos(serverboundsetstructureblockpacket.getOffset());
            structureblockentity.setStructureSize(serverboundsetstructureblockpacket.getSize());
            structureblockentity.setMirror(serverboundsetstructureblockpacket.getMirror());
            structureblockentity.setRotation(serverboundsetstructureblockpacket.getRotation());
            structureblockentity.setMetaData(serverboundsetstructureblockpacket.getData());
            structureblockentity.setIgnoreEntities(serverboundsetstructureblockpacket.isIgnoreEntities());
            structureblockentity.setShowAir(serverboundsetstructureblockpacket.isShowAir());
            structureblockentity.setShowBoundingBox(serverboundsetstructureblockpacket.isShowBoundingBox());
            structureblockentity.setIntegrity(serverboundsetstructureblockpacket.getIntegrity());
            structureblockentity.setSeed(serverboundsetstructureblockpacket.getSeed());
            if (structureblockentity.hasStructureName()) {
               String s = structureblockentity.getStructureName();
               if (serverboundsetstructureblockpacket.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                  if (structureblockentity.saveStructure()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_failure", s), false);
                  }
               } else if (serverboundsetstructureblockpacket.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                  if (!structureblockentity.isStructureLoadable()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", s), false);
                  } else if (structureblockentity.loadStructure(this.player.serverLevel())) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", s), false);
                  }
               } else if (serverboundsetstructureblockpacket.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                  if (structureblockentity.detectSize()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", serverboundsetstructureblockpacket.getName()), false);
            }

            structureblockentity.setChanged();
            this.player.level().sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundsetjigsawblockpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetjigsawblockpacket, this, this.player.serverLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = serverboundsetjigsawblockpacket.getPos();
         BlockState blockstate = this.player.level().getBlockState(blockpos);
         BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
         if (blockentity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
            jigsawblockentity.setName(serverboundsetjigsawblockpacket.getName());
            jigsawblockentity.setTarget(serverboundsetjigsawblockpacket.getTarget());
            jigsawblockentity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, serverboundsetjigsawblockpacket.getPool()));
            jigsawblockentity.setFinalState(serverboundsetjigsawblockpacket.getFinalState());
            jigsawblockentity.setJoint(serverboundsetjigsawblockpacket.getJoint());
            jigsawblockentity.setChanged();
            this.player.level().sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundjigsawgeneratepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundjigsawgeneratepacket, this, this.player.serverLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = serverboundjigsawgeneratepacket.getPos();
         BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
         if (blockentity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
            jigsawblockentity.generate(this.player.serverLevel(), serverboundjigsawgeneratepacket.levels(), serverboundjigsawgeneratepacket.keepJigsaws());
         }

      }
   }

   public void handleSelectTrade(ServerboundSelectTradePacket serverboundselecttradepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundselecttradepacket, this, this.player.serverLevel());
      int i = serverboundselecttradepacket.getItem();
      AbstractContainerMenu var4 = this.player.containerMenu;
      if (var4 instanceof MerchantMenu merchantmenu) {
         if (!merchantmenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, merchantmenu);
            return;
         }

         merchantmenu.setSelectionHint(i);
         merchantmenu.tryMoveItems(i);
      }

   }

   public void handleEditBook(ServerboundEditBookPacket serverboundeditbookpacket) {
      int i = serverboundeditbookpacket.getSlot();
      if (Inventory.isHotbarSlot(i) || i == 40) {
         List<String> list = Lists.newArrayList();
         Optional<String> optional = serverboundeditbookpacket.getTitle();
         optional.ifPresent(list::add);
         serverboundeditbookpacket.getPages().stream().limit(100L).forEach(list::add);
         Consumer<List<FilteredText>> consumer = optional.isPresent() ? (list2) -> this.signBook(list2.get(0), list2.subList(1, list2.size()), i) : (list1) -> this.updateBookContents(list1, i);
         this.filterTextPacket(list).thenAcceptAsync(consumer, this.server);
      }
   }

   private void updateBookContents(List<FilteredText> list, int i) {
      ItemStack itemstack = this.player.getInventory().getItem(i);
      if (itemstack.is(Items.WRITABLE_BOOK)) {
         this.updateBookPages(list, UnaryOperator.identity(), itemstack);
      }
   }

   private void signBook(FilteredText filteredtext, List<FilteredText> list, int i) {
      ItemStack itemstack = this.player.getInventory().getItem(i);
      if (itemstack.is(Items.WRITABLE_BOOK)) {
         ItemStack itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null) {
            itemstack1.setTag(compoundtag.copy());
         }

         itemstack1.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
         if (this.player.isTextFilteringEnabled()) {
            itemstack1.addTagElement("title", StringTag.valueOf(filteredtext.filteredOrEmpty()));
         } else {
            itemstack1.addTagElement("filtered_title", StringTag.valueOf(filteredtext.filteredOrEmpty()));
            itemstack1.addTagElement("title", StringTag.valueOf(filteredtext.raw()));
         }

         this.updateBookPages(list, (s) -> Component.Serializer.toJson(Component.literal(s)), itemstack1);
         this.player.getInventory().setItem(i, itemstack1);
      }
   }

   private void updateBookPages(List<FilteredText> list, UnaryOperator<String> unaryoperator, ItemStack itemstack) {
      ListTag listtag = new ListTag();
      if (this.player.isTextFilteringEnabled()) {
         list.stream().map((filteredtext1) -> StringTag.valueOf(unaryoperator.apply(filteredtext1.filteredOrEmpty()))).forEach(listtag::add);
      } else {
         CompoundTag compoundtag = new CompoundTag();
         int i = 0;

         for(int j = list.size(); i < j; ++i) {
            FilteredText filteredtext = list.get(i);
            String s = filteredtext.raw();
            listtag.add(StringTag.valueOf(unaryoperator.apply(s)));
            if (filteredtext.isFiltered()) {
               compoundtag.putString(String.valueOf(i), unaryoperator.apply(filteredtext.filteredOrEmpty()));
            }
         }

         if (!compoundtag.isEmpty()) {
            itemstack.addTagElement("filtered_pages", compoundtag);
         }
      }

      itemstack.addTagElement("pages", listtag);
   }

   public void handleEntityTagQuery(ServerboundEntityTagQuery serverboundentitytagquery) {
      PacketUtils.ensureRunningOnSameThread(serverboundentitytagquery, this, this.player.serverLevel());
      if (this.player.hasPermissions(2)) {
         Entity entity = this.player.level().getEntity(serverboundentitytagquery.getEntityId());
         if (entity != null) {
            CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
            this.player.connection.send(new ClientboundTagQueryPacket(serverboundentitytagquery.getTransactionId(), compoundtag));
         }

      }
   }

   public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery serverboundblockentitytagquery) {
      PacketUtils.ensureRunningOnSameThread(serverboundblockentitytagquery, this, this.player.serverLevel());
      if (this.player.hasPermissions(2)) {
         BlockEntity blockentity = this.player.level().getBlockEntity(serverboundblockentitytagquery.getPos());
         CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
         this.player.connection.send(new ClientboundTagQueryPacket(serverboundblockentitytagquery.getTransactionId(), compoundtag));
      }
   }

   public void handleMovePlayer(ServerboundMovePlayerPacket serverboundmoveplayerpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundmoveplayerpacket, this, this.player.serverLevel());
      if (containsInvalidValues(serverboundmoveplayerpacket.getX(0.0D), serverboundmoveplayerpacket.getY(0.0D), serverboundmoveplayerpacket.getZ(0.0D), serverboundmoveplayerpacket.getYRot(0.0F), serverboundmoveplayerpacket.getXRot(0.0F))) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerLevel serverlevel = this.player.serverLevel();
         if (!this.player.wonGame) {
            if (this.tickCount == 0) {
               this.resetPosition();
            }

            if (this.awaitingPositionFromClient != null) {
               if (this.tickCount - this.awaitingTeleportTime > 20) {
                  this.awaitingTeleportTime = this.tickCount;
                  this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
               }

            } else {
               this.awaitingTeleportTime = this.tickCount;
               double d0 = clampHorizontal(serverboundmoveplayerpacket.getX(this.player.getX()));
               double d1 = clampVertical(serverboundmoveplayerpacket.getY(this.player.getY()));
               double d2 = clampHorizontal(serverboundmoveplayerpacket.getZ(this.player.getZ()));
               float f = Mth.wrapDegrees(serverboundmoveplayerpacket.getYRot(this.player.getYRot()));
               float f1 = Mth.wrapDegrees(serverboundmoveplayerpacket.getXRot(this.player.getXRot()));
               if (this.player.isPassenger()) {
                  this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                  this.player.serverLevel().getChunkSource().move(this.player);
               } else {
                  double d3 = this.player.getX();
                  double d4 = this.player.getY();
                  double d5 = this.player.getZ();
                  double d6 = d0 - this.firstGoodX;
                  double d7 = d1 - this.firstGoodY;
                  double d8 = d2 - this.firstGoodZ;
                  double d9 = this.player.getDeltaMovement().lengthSqr();
                  double d10 = d6 * d6 + d7 * d7 + d8 * d8;
                  if (this.player.isSleeping()) {
                     if (d10 > 1.0D) {
                        this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                     }

                  } else {
                     ++this.receivedMovePacketCount;
                     int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                     if (i > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                        i = 1;
                     }

                     if (!this.player.isChangingDimension() && (!this.player.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                        float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                        if (d10 - d9 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                           LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d6, d7, d8);
                           this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                           return;
                        }
                     }

                     AABB aabb = this.player.getBoundingBox();
                     d6 = d0 - this.lastGoodX;
                     d7 = d1 - this.lastGoodY;
                     d8 = d2 - this.lastGoodZ;
                     boolean flag = d7 > 0.0D;
                     if (this.player.onGround() && !serverboundmoveplayerpacket.isOnGround() && flag) {
                        this.player.jumpFromGround();
                     }

                     boolean flag1 = this.player.verticalCollisionBelow;
                     this.player.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
                     d6 = d0 - this.player.getX();
                     d7 = d1 - this.player.getY();
                     if (d7 > -0.5D || d7 < 0.5D) {
                        d7 = 0.0D;
                     }

                     d8 = d2 - this.player.getZ();
                     d10 = d6 * d6 + d7 * d7 + d8 * d8;
                     boolean flag2 = false;
                     if (!this.player.isChangingDimension() && d10 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                        flag2 = true;
                        LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
                     }

                     if (this.player.noPhysics || this.player.isSleeping() || (!flag2 || !serverlevel.noCollision(this.player, aabb)) && !this.isPlayerCollidingWithAnythingNew(serverlevel, aabb, d0, d1, d2)) {
                        this.player.absMoveTo(d0, d1, d2, f, f1);
                        this.clientIsFloating = d7 >= -0.03125D && !flag1 && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isAutoSpinAttack() && this.noBlocksAround(this.player);
                        this.player.serverLevel().getChunkSource().move(this.player);
                        this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, serverboundmoveplayerpacket.isOnGround());
                        this.player.setOnGroundWithKnownMovement(serverboundmoveplayerpacket.isOnGround(), new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5));
                        if (flag) {
                           this.player.resetFallDistance();
                        }

                        this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                        this.lastGoodX = this.player.getX();
                        this.lastGoodY = this.player.getY();
                        this.lastGoodZ = this.player.getZ();
                     } else {
                        this.teleport(d3, d4, d5, f, f1);
                        this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, serverboundmoveplayerpacket.isOnGround());
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerCollidingWithAnythingNew(LevelReader levelreader, AABB aabb, double d0, double d1, double d2) {
      AABB aabb1 = this.player.getBoundingBox().move(d0 - this.player.getX(), d1 - this.player.getY(), d2 - this.player.getZ());
      Iterable<VoxelShape> iterable = levelreader.getCollisions(this.player, aabb1.deflate((double)1.0E-5F));
      VoxelShape voxelshape = Shapes.create(aabb.deflate((double)1.0E-5F));

      for(VoxelShape voxelshape1 : iterable) {
         if (!Shapes.joinIsNotEmpty(voxelshape1, voxelshape, BooleanOp.AND)) {
            return true;
         }
      }

      return false;
   }

   public void teleport(double d0, double d1, double d2, float f, float f1) {
      this.teleport(d0, d1, d2, f, f1, Collections.emptySet());
   }

   public void teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set) {
      double d3 = set.contains(RelativeMovement.X) ? this.player.getX() : 0.0D;
      double d4 = set.contains(RelativeMovement.Y) ? this.player.getY() : 0.0D;
      double d5 = set.contains(RelativeMovement.Z) ? this.player.getZ() : 0.0D;
      float f2 = set.contains(RelativeMovement.Y_ROT) ? this.player.getYRot() : 0.0F;
      float f3 = set.contains(RelativeMovement.X_ROT) ? this.player.getXRot() : 0.0F;
      this.awaitingPositionFromClient = new Vec3(d0, d1, d2);
      if (++this.awaitingTeleport == Integer.MAX_VALUE) {
         this.awaitingTeleport = 0;
      }

      this.awaitingTeleportTime = this.tickCount;
      this.player.absMoveTo(d0, d1, d2, f, f1);
      this.player.connection.send(new ClientboundPlayerPositionPacket(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.awaitingTeleport));
   }

   public void handlePlayerAction(ServerboundPlayerActionPacket serverboundplayeractionpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundplayeractionpacket, this, this.player.serverLevel());
      BlockPos blockpos = serverboundplayeractionpacket.getPos();
      this.player.resetLastActionTime();
      ServerboundPlayerActionPacket.Action serverboundplayeractionpacket_action = serverboundplayeractionpacket.getAction();
      switch (serverboundplayeractionpacket_action) {
         case SWAP_ITEM_WITH_OFFHAND:
            if (!this.player.isSpectator()) {
               ItemStack itemstack = this.player.getItemInHand(InteractionHand.OFF_HAND);
               this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
               this.player.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
               this.player.stopUsingItem();
            }

            return;
         case DROP_ITEM:
            if (!this.player.isSpectator()) {
               this.player.drop(false);
            }

            return;
         case DROP_ALL_ITEMS:
            if (!this.player.isSpectator()) {
               this.player.drop(true);
            }

            return;
         case RELEASE_USE_ITEM:
            this.player.releaseUsingItem();
            return;
         case START_DESTROY_BLOCK:
         case ABORT_DESTROY_BLOCK:
         case STOP_DESTROY_BLOCK:
            this.player.gameMode.handleBlockBreakAction(blockpos, serverboundplayeractionpacket_action, serverboundplayeractionpacket.getDirection(), this.player.level().getMaxBuildHeight(), serverboundplayeractionpacket.getSequence());
            this.player.connection.ackBlockChangesUpTo(serverboundplayeractionpacket.getSequence());
            return;
         default:
            throw new IllegalArgumentException("Invalid player action");
      }
   }

   private static boolean wasBlockPlacementAttempt(ServerPlayer serverplayer, ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return false;
      } else {
         Item item = itemstack.getItem();
         return (item instanceof BlockItem || item instanceof BucketItem) && !serverplayer.getCooldowns().isOnCooldown(item);
      }
   }

   public void handleUseItemOn(ServerboundUseItemOnPacket serverbounduseitemonpacket) {
      PacketUtils.ensureRunningOnSameThread(serverbounduseitemonpacket, this, this.player.serverLevel());
      this.player.connection.ackBlockChangesUpTo(serverbounduseitemonpacket.getSequence());
      ServerLevel serverlevel = this.player.serverLevel();
      InteractionHand interactionhand = serverbounduseitemonpacket.getHand();
      ItemStack itemstack = this.player.getItemInHand(interactionhand);
      if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
         BlockHitResult blockhitresult = serverbounduseitemonpacket.getHitResult();
         Vec3 vec3 = blockhitresult.getLocation();
         BlockPos blockpos = blockhitresult.getBlockPos();
         Vec3 vec31 = Vec3.atCenterOf(blockpos);
         if (!(this.player.getEyePosition().distanceToSqr(vec31) > MAX_INTERACTION_DISTANCE)) {
            Vec3 vec32 = vec3.subtract(vec31);
            double d0 = 1.0000001D;
            if (Math.abs(vec32.x()) < 1.0000001D && Math.abs(vec32.y()) < 1.0000001D && Math.abs(vec32.z()) < 1.0000001D) {
               Direction direction = blockhitresult.getDirection();
               this.player.resetLastActionTime();
               int i = this.player.level().getMaxBuildHeight();
               if (blockpos.getY() < i) {
                  if (this.awaitingPositionFromClient == null && this.player.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) < 64.0D && serverlevel.mayInteract(this.player, blockpos)) {
                     InteractionResult interactionresult = this.player.gameMode.useItemOn(this.player, serverlevel, itemstack, interactionhand, blockhitresult);
                     if (direction == Direction.UP && !interactionresult.consumesAction() && blockpos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
                        Component component = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                        this.player.sendSystemMessage(component, true);
                     } else if (interactionresult.shouldSwing()) {
                        this.player.swing(interactionhand, true);
                     }
                  }
               } else {
                  Component component1 = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                  this.player.sendSystemMessage(component1, true);
               }

               this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos));
               this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos.relative(direction)));
            } else {
               LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), vec3, blockpos);
            }
         }
      }
   }

   public void handleUseItem(ServerboundUseItemPacket serverbounduseitempacket) {
      PacketUtils.ensureRunningOnSameThread(serverbounduseitempacket, this, this.player.serverLevel());
      this.ackBlockChangesUpTo(serverbounduseitempacket.getSequence());
      ServerLevel serverlevel = this.player.serverLevel();
      InteractionHand interactionhand = serverbounduseitempacket.getHand();
      ItemStack itemstack = this.player.getItemInHand(interactionhand);
      this.player.resetLastActionTime();
      if (!itemstack.isEmpty() && itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
         InteractionResult interactionresult = this.player.gameMode.useItem(this.player, serverlevel, itemstack, interactionhand);
         if (interactionresult.shouldSwing()) {
            this.player.swing(interactionhand, true);
         }

      }
   }

   public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundteleporttoentitypacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundteleporttoentitypacket, this, this.player.serverLevel());
      if (this.player.isSpectator()) {
         for(ServerLevel serverlevel : this.server.getAllLevels()) {
            Entity entity = serverboundteleporttoentitypacket.getEntity(serverlevel);
            if (entity != null) {
               this.player.teleportTo(serverlevel, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
               return;
            }
         }
      }

   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundresourcepackpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundresourcepackpacket, this, this.player.serverLevel());
      if (serverboundresourcepackpacket.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
         LOGGER.info("Disconnecting {} due to resource pack rejection", (Object)this.player.getName());
         this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
      }

   }

   public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundpaddleboatpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundpaddleboatpacket, this, this.player.serverLevel());
      Entity entity = this.player.getControlledVehicle();
      if (entity instanceof Boat boat) {
         boat.setPaddleState(serverboundpaddleboatpacket.getLeft(), serverboundpaddleboatpacket.getRight());
      }

   }

   public void handlePong(ServerboundPongPacket serverboundpongpacket) {
   }

   public void onDisconnect(Component component) {
      this.chatMessageChain.close();
      LOGGER.info("{} lost connection: {}", this.player.getName().getString(), component.getString());
      this.server.invalidateStatus();
      this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
      this.player.disconnect();
      this.server.getPlayerList().remove(this.player);
      this.player.getTextFilter().leave();
      if (this.isSingleplayerOwner()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.halt(false);
      }

   }

   public void ackBlockChangesUpTo(int i) {
      if (i < 0) {
         throw new IllegalArgumentException("Expected packet sequence nr >= 0");
      } else {
         this.ackBlockChangesUpTo = Math.max(i, this.ackBlockChangesUpTo);
      }
   }

   public void send(Packet<?> packet) {
      this.send(packet, (PacketSendListener)null);
   }

   public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
      try {
         this.connection.send(packet, packetsendlistener);
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.forThrowable(var6, "Sending packet");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
         crashreportcategory.setDetail("Packet class", () -> packet.getClass().getCanonicalName());
         throw new ReportedException(crashreport);
      }
   }

   public void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundsetcarrieditempacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetcarrieditempacket, this, this.player.serverLevel());
      if (serverboundsetcarrieditempacket.getSlot() >= 0 && serverboundsetcarrieditempacket.getSlot() < Inventory.getSelectionSize()) {
         if (this.player.getInventory().selected != serverboundsetcarrieditempacket.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.player.stopUsingItem();
         }

         this.player.getInventory().selected = serverboundsetcarrieditempacket.getSlot();
         this.player.resetLastActionTime();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getName().getString());
      }
   }

   public void handleChat(ServerboundChatPacket serverboundchatpacket) {
      if (isChatMessageIllegal(serverboundchatpacket.message())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         Optional<LastSeenMessages> optional = this.tryHandleChat(serverboundchatpacket.message(), serverboundchatpacket.timeStamp(), serverboundchatpacket.lastSeenMessages());
         if (optional.isPresent()) {
            this.server.submit(() -> {
               PlayerChatMessage playerchatmessage;
               try {
                  playerchatmessage = this.getSignedMessage(serverboundchatpacket, optional.get());
               } catch (SignedMessageChain.DecodeException var6) {
                  this.handleMessageDecodeFailure(var6);
                  return;
               }

               CompletableFuture<FilteredText> completablefuture = this.filterTextPacket(playerchatmessage.signedContent());
               CompletableFuture<Component> completablefuture1 = this.server.getChatDecorator().decorate(this.player, playerchatmessage.decoratedContent());
               this.chatMessageChain.append((executor) -> CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((ovoid) -> {
                     PlayerChatMessage playerchatmessage4 = playerchatmessage.withUnsignedContent(completablefuture1.join()).filter(completablefuture.join().mask());
                     this.broadcastChatMessage(playerchatmessage4);
                  }, executor));
            });
         }

      }
   }

   public void handleChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket) {
      if (isChatMessageIllegal(serverboundchatcommandpacket.command())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         Optional<LastSeenMessages> optional = this.tryHandleChat(serverboundchatcommandpacket.command(), serverboundchatcommandpacket.timeStamp(), serverboundchatcommandpacket.lastSeenMessages());
         if (optional.isPresent()) {
            this.server.submit(() -> {
               this.performChatCommand(serverboundchatcommandpacket, optional.get());
               this.detectRateSpam();
            });
         }

      }
   }

   private void performChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket, LastSeenMessages lastseenmessages) {
      ParseResults<CommandSourceStack> parseresults = this.parseCommand(serverboundchatcommandpacket.command());

      Map<String, PlayerChatMessage> map;
      try {
         map = this.collectSignedArguments(serverboundchatcommandpacket, SignableCommand.of(parseresults), lastseenmessages);
      } catch (SignedMessageChain.DecodeException var6) {
         this.handleMessageDecodeFailure(var6);
         return;
      }

      CommandSigningContext commandsigningcontext = new CommandSigningContext.SignedArguments(map);
      parseresults = Commands.mapSource(parseresults, (commandsourcestack) -> commandsourcestack.withSigningContext(commandsigningcontext));
      this.server.getCommands().performCommand(parseresults, serverboundchatcommandpacket.command());
   }

   private void handleMessageDecodeFailure(SignedMessageChain.DecodeException signedmessagechain_decodeexception) {
      if (signedmessagechain_decodeexception.shouldDisconnect()) {
         this.disconnect(signedmessagechain_decodeexception.getComponent());
      } else {
         this.player.sendSystemMessage(signedmessagechain_decodeexception.getComponent().copy().withStyle(ChatFormatting.RED));
      }

   }

   private Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandPacket serverboundchatcommandpacket, SignableCommand<?> signablecommand, LastSeenMessages lastseenmessages) throws SignedMessageChain.DecodeException {
      Map<String, PlayerChatMessage> map = new Object2ObjectOpenHashMap<>();

      for(SignableCommand.Argument<?> signablecommand_argument : signablecommand.arguments()) {
         MessageSignature messagesignature = serverboundchatcommandpacket.argumentSignatures().get(signablecommand_argument.name());
         SignedMessageBody signedmessagebody = new SignedMessageBody(signablecommand_argument.value(), serverboundchatcommandpacket.timeStamp(), serverboundchatcommandpacket.salt(), lastseenmessages);
         map.put(signablecommand_argument.name(), this.signedMessageDecoder.unpack(messagesignature, signedmessagebody));
      }

      return map;
   }

   private ParseResults<CommandSourceStack> parseCommand(String s) {
      CommandDispatcher<CommandSourceStack> commanddispatcher = this.server.getCommands().getDispatcher();
      return commanddispatcher.parse(s, this.player.createCommandSourceStack());
   }

   private Optional<LastSeenMessages> tryHandleChat(String s, Instant instant, LastSeenMessages.Update lastseenmessages_update) {
      if (!this.updateChatOrder(instant)) {
         LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), s);
         this.disconnect(Component.translatable("multiplayer.disconnect.out_of_order_chat"));
         return Optional.empty();
      } else {
         Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(lastseenmessages_update);
         if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
            return Optional.empty();
         } else {
            this.player.resetLastActionTime();
            return optional;
         }
      }
   }

   private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update lastseenmessages_update) {
      synchronized(this.lastSeenMessages) {
         Optional<LastSeenMessages> optional = this.lastSeenMessages.applyUpdate(lastseenmessages_update);
         if (optional.isEmpty()) {
            LOGGER.warn("Failed to validate message acknowledgements from {}", (Object)this.player.getName().getString());
            this.disconnect(CHAT_VALIDATION_FAILED);
         }

         return optional;
      }
   }

   private boolean updateChatOrder(Instant instant) {
      Instant instant1;
      do {
         instant1 = this.lastChatTimeStamp.get();
         if (instant.isBefore(instant1)) {
            return false;
         }
      } while(!this.lastChatTimeStamp.compareAndSet(instant1, instant));

      return true;
   }

   private static boolean isChatMessageIllegal(String s) {
      for(int i = 0; i < s.length(); ++i) {
         if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
            return true;
         }
      }

      return false;
   }

   private PlayerChatMessage getSignedMessage(ServerboundChatPacket serverboundchatpacket, LastSeenMessages lastseenmessages) throws SignedMessageChain.DecodeException {
      SignedMessageBody signedmessagebody = new SignedMessageBody(serverboundchatpacket.message(), serverboundchatpacket.timeStamp(), serverboundchatpacket.salt(), lastseenmessages);
      return this.signedMessageDecoder.unpack(serverboundchatpacket.signature(), signedmessagebody);
   }

   private void broadcastChatMessage(PlayerChatMessage playerchatmessage) {
      this.server.getPlayerList().broadcastChatMessage(playerchatmessage, this.player, ChatType.bind(ChatType.CHAT, this.player));
      this.detectRateSpam();
   }

   private void detectRateSpam() {
      this.chatSpamTickCount += 20;
      if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
         this.disconnect(Component.translatable("disconnect.spam"));
      }

   }

   public void handleChatAck(ServerboundChatAckPacket serverboundchatackpacket) {
      synchronized(this.lastSeenMessages) {
         if (!this.lastSeenMessages.applyOffset(serverboundchatackpacket.offset())) {
            LOGGER.warn("Failed to validate message acknowledgements from {}", (Object)this.player.getName().getString());
            this.disconnect(CHAT_VALIDATION_FAILED);
         }

      }
   }

   public void handleAnimate(ServerboundSwingPacket serverboundswingpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundswingpacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      this.player.swing(serverboundswingpacket.getHand());
   }

   public void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundplayercommandpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundplayercommandpacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      switch (serverboundplayercommandpacket.getAction()) {
         case PRESS_SHIFT_KEY:
            this.player.setShiftKeyDown(true);
            break;
         case RELEASE_SHIFT_KEY:
            this.player.setShiftKeyDown(false);
            break;
         case START_SPRINTING:
            this.player.setSprinting(true);
            break;
         case STOP_SPRINTING:
            this.player.setSprinting(false);
            break;
         case STOP_SLEEPING:
            if (this.player.isSleeping()) {
               this.player.stopSleepInBed(false, true);
               this.awaitingPositionFromClient = this.player.position();
            }
            break;
         case START_RIDING_JUMP:
            Entity var7 = this.player.getControlledVehicle();
            if (var7 instanceof PlayerRideableJumping playerrideablejumping) {
               int i = serverboundplayercommandpacket.getData();
               if (playerrideablejumping.canJump() && i > 0) {
                  playerrideablejumping.handleStartJump(i);
               }
            }
            break;
         case STOP_RIDING_JUMP:
            Entity var6 = this.player.getControlledVehicle();
            if (var6 instanceof PlayerRideableJumping playerrideablejumping1) {
               playerrideablejumping1.handleStopJump();
            }
            break;
         case OPEN_INVENTORY:
            Entity i = this.player.getVehicle();
            if (i instanceof HasCustomInventoryScreen hascustominventoryscreen) {
               hascustominventoryscreen.openCustomInventoryScreen(this.player);
            }
            break;
         case START_FALL_FLYING:
            if (!this.player.tryToStartFallFlying()) {
               this.player.stopFallFlying();
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid client command!");
      }

   }

   public void addPendingMessage(PlayerChatMessage playerchatmessage) {
      MessageSignature messagesignature = playerchatmessage.signature();
      if (messagesignature != null) {
         this.messageSignatureCache.push(playerchatmessage);
         int i;
         synchronized(this.lastSeenMessages) {
            this.lastSeenMessages.addPending(messagesignature);
            i = this.lastSeenMessages.trackedMessagesCount();
         }

         if (i > 4096) {
            this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
         }

      }
   }

   public void sendPlayerChatMessage(PlayerChatMessage playerchatmessage, ChatType.Bound chattype_bound) {
      this.send(new ClientboundPlayerChatPacket(playerchatmessage.link().sender(), playerchatmessage.link().index(), playerchatmessage.signature(), playerchatmessage.signedBody().pack(this.messageSignatureCache), playerchatmessage.unsignedContent(), playerchatmessage.filterMask(), chattype_bound.toNetwork(this.player.level().registryAccess())));
      this.addPendingMessage(playerchatmessage);
   }

   public void sendDisguisedChatMessage(Component component, ChatType.Bound chattype_bound) {
      this.send(new ClientboundDisguisedChatPacket(component, chattype_bound.toNetwork(this.player.level().registryAccess())));
   }

   public SocketAddress getRemoteAddress() {
      return this.connection.getRemoteAddress();
   }

   public void handleInteract(ServerboundInteractPacket serverboundinteractpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundinteractpacket, this, this.player.serverLevel());
      final ServerLevel serverlevel = this.player.serverLevel();
      final Entity entity = serverboundinteractpacket.getTarget(serverlevel);
      this.player.resetLastActionTime();
      this.player.setShiftKeyDown(serverboundinteractpacket.isUsingSecondaryAction());
      if (entity != null) {
         if (!serverlevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            return;
         }

         AABB aabb = entity.getBoundingBox();
         if (aabb.distanceToSqr(this.player.getEyePosition()) < MAX_INTERACTION_DISTANCE) {
            serverboundinteractpacket.dispatch(new ServerboundInteractPacket.Handler() {
               private void performInteraction(InteractionHand interactionhand, ServerGamePacketListenerImpl.EntityInteraction servergamepacketlistenerimpl_entityinteraction) {
                  ItemStack itemstack = ServerGamePacketListenerImpl.this.player.getItemInHand(interactionhand);
                  if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
                     ItemStack itemstack1 = itemstack.copy();
                     InteractionResult interactionresult = servergamepacketlistenerimpl_entityinteraction.run(ServerGamePacketListenerImpl.this.player, entity, interactionhand);
                     if (interactionresult.consumesAction()) {
                        CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, itemstack1, entity);
                        if (interactionresult.shouldSwing()) {
                           ServerGamePacketListenerImpl.this.player.swing(interactionhand, true);
                        }
                     }

                  }
               }

               public void onInteraction(InteractionHand interactionhand) {
                  this.performInteraction(interactionhand, Player::interactOn);
               }

               public void onInteraction(InteractionHand interactionhand, Vec3 vec3) {
                  this.performInteraction(interactionhand, (serverplayer, entityx, interactionhand1) -> entityx.interactAt(serverplayer, vec3, interactionhand1));
               }

               public void onAttack() {
                  if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && !(entity instanceof AbstractArrow) && entity != ServerGamePacketListenerImpl.this.player) {
                     ItemStack itemstack = ServerGamePacketListenerImpl.this.player.getItemInHand(InteractionHand.MAIN_HAND);
                     if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
                        ServerGamePacketListenerImpl.this.player.attack(entity);
                     }
                  } else {
                     ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                     ServerGamePacketListenerImpl.LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerGamePacketListenerImpl.this.player.getName().getString());
                  }
               }
            });
         }
      }

   }

   public void handleClientCommand(ServerboundClientCommandPacket serverboundclientcommandpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundclientcommandpacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      ServerboundClientCommandPacket.Action serverboundclientcommandpacket_action = serverboundclientcommandpacket.getAction();
      switch (serverboundclientcommandpacket_action) {
         case PERFORM_RESPAWN:
            if (this.player.wonGame) {
               this.player.wonGame = false;
               this.player = this.server.getPlayerList().respawn(this.player, true);
               CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
            } else {
               if (this.player.getHealth() > 0.0F) {
                  return;
               }

               this.player = this.server.getPlayerList().respawn(this.player, false);
               if (this.server.isHardcore()) {
                  this.player.setGameMode(GameType.SPECTATOR);
                  this.player.level().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
               }
            }
            break;
         case REQUEST_STATS:
            this.player.getStats().sendStats(this.player);
      }

   }

   public void handleContainerClose(ServerboundContainerClosePacket serverboundcontainerclosepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundcontainerclosepacket, this, this.player.serverLevel());
      this.player.doCloseContainer();
   }

   public void handleContainerClick(ServerboundContainerClickPacket serverboundcontainerclickpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundcontainerclickpacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == serverboundcontainerclickpacket.getContainerId()) {
         if (this.player.isSpectator()) {
            this.player.containerMenu.sendAllDataToRemote();
         } else if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            int i = serverboundcontainerclickpacket.getSlotNum();
            if (!this.player.containerMenu.isValidSlotIndex(i)) {
               LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), i, this.player.containerMenu.slots.size());
            } else {
               boolean flag = serverboundcontainerclickpacket.getStateId() != this.player.containerMenu.getStateId();
               this.player.containerMenu.suppressRemoteUpdates();
               this.player.containerMenu.clicked(i, serverboundcontainerclickpacket.getButtonNum(), serverboundcontainerclickpacket.getClickType(), this.player);

               for(Int2ObjectMap.Entry<ItemStack> int2objectmap_entry : Int2ObjectMaps.fastIterable(serverboundcontainerclickpacket.getChangedSlots())) {
                  this.player.containerMenu.setRemoteSlotNoCopy(int2objectmap_entry.getIntKey(), int2objectmap_entry.getValue());
               }

               this.player.containerMenu.setRemoteCarried(serverboundcontainerclickpacket.getCarriedItem());
               this.player.containerMenu.resumeRemoteUpdates();
               if (flag) {
                  this.player.containerMenu.broadcastFullState();
               } else {
                  this.player.containerMenu.broadcastChanges();
               }

            }
         }
      }
   }

   public void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundplacerecipepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundplacerecipepacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      if (!this.player.isSpectator() && this.player.containerMenu.containerId == serverboundplacerecipepacket.getContainerId() && this.player.containerMenu instanceof RecipeBookMenu) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            this.server.getRecipeManager().byKey(serverboundplacerecipepacket.getRecipe()).ifPresent((recipe) -> ((RecipeBookMenu)this.player.containerMenu).handlePlacement(serverboundplacerecipepacket.isShiftDown(), recipe, this.player));
         }
      }
   }

   public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundcontainerbuttonclickpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundcontainerbuttonclickpacket, this, this.player.serverLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == serverboundcontainerbuttonclickpacket.getContainerId() && !this.player.isSpectator()) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            boolean flag = this.player.containerMenu.clickMenuButton(this.player, serverboundcontainerbuttonclickpacket.getButtonId());
            if (flag) {
               this.player.containerMenu.broadcastChanges();
            }

         }
      }
   }

   public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundsetcreativemodeslotpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundsetcreativemodeslotpacket, this, this.player.serverLevel());
      if (this.player.gameMode.isCreative()) {
         boolean flag = serverboundsetcreativemodeslotpacket.getSlotNum() < 0;
         ItemStack itemstack = serverboundsetcreativemodeslotpacket.getItem();
         if (!itemstack.isItemEnabled(this.player.level().enabledFeatures())) {
            return;
         }

         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
         if (!itemstack.isEmpty() && compoundtag != null && compoundtag.contains("x") && compoundtag.contains("y") && compoundtag.contains("z")) {
            BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
            if (this.player.level().isLoaded(blockpos)) {
               BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
               if (blockentity != null) {
                  blockentity.saveToItem(itemstack);
               }
            }
         }

         boolean flag1 = serverboundsetcreativemodeslotpacket.getSlotNum() >= 1 && serverboundsetcreativemodeslotpacket.getSlotNum() <= 45;
         boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
         if (flag1 && flag2) {
            this.player.inventoryMenu.getSlot(serverboundsetcreativemodeslotpacket.getSlotNum()).setByPlayer(itemstack);
            this.player.inventoryMenu.broadcastChanges();
         } else if (flag && flag2 && this.dropSpamTickCount < 200) {
            this.dropSpamTickCount += 20;
            this.player.drop(itemstack, true);
         }
      }

   }

   public void handleSignUpdate(ServerboundSignUpdatePacket serverboundsignupdatepacket) {
      List<String> list = Stream.of(serverboundsignupdatepacket.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
      this.filterTextPacket(list).thenAcceptAsync((list1) -> this.updateSignText(serverboundsignupdatepacket, list1), this.server);
   }

   private void updateSignText(ServerboundSignUpdatePacket serverboundsignupdatepacket, List<FilteredText> list) {
      this.player.resetLastActionTime();
      ServerLevel serverlevel = this.player.serverLevel();
      BlockPos blockpos = serverboundsignupdatepacket.getPos();
      if (serverlevel.hasChunkAt(blockpos)) {
         BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
         if (!(blockentity instanceof SignBlockEntity)) {
            return;
         }

         SignBlockEntity signblockentity = (SignBlockEntity)blockentity;
         signblockentity.updateSignText(this.player, serverboundsignupdatepacket.isFrontText(), list);
      }

   }

   public void handleKeepAlive(ServerboundKeepAlivePacket serverboundkeepalivepacket) {
      if (this.keepAlivePending && serverboundkeepalivepacket.getId() == this.keepAliveChallenge) {
         int i = (int)(Util.getMillis() - this.keepAliveTime);
         this.player.latency = (this.player.latency * 3 + i) / 4;
         this.keepAlivePending = false;
      } else if (!this.isSingleplayerOwner()) {
         this.disconnect(Component.translatable("disconnect.timeout"));
      }

   }

   public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundplayerabilitiespacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundplayerabilitiespacket, this, this.player.serverLevel());
      this.player.getAbilities().flying = serverboundplayerabilitiespacket.isFlying() && this.player.getAbilities().mayfly;
   }

   public void handleClientInformation(ServerboundClientInformationPacket serverboundclientinformationpacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundclientinformationpacket, this, this.player.serverLevel());
      this.player.updateOptions(serverboundclientinformationpacket);
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundcustompayloadpacket) {
   }

   public void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundchangedifficultypacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundchangedifficultypacket, this, this.player.serverLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficulty(serverboundchangedifficultypacket.getDifficulty(), false);
      }
   }

   public void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundlockdifficultypacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundlockdifficultypacket, this, this.player.serverLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficultyLocked(serverboundlockdifficultypacket.isLocked());
      }
   }

   public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundchatsessionupdatepacket) {
      PacketUtils.ensureRunningOnSameThread(serverboundchatsessionupdatepacket, this, this.player.serverLevel());
      RemoteChatSession.Data remotechatsession_data = serverboundchatsessionupdatepacket.chatSession();
      ProfilePublicKey.Data profilepublickey_data = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
      ProfilePublicKey.Data profilepublickey_data1 = remotechatsession_data.profilePublicKey();
      if (!Objects.equals(profilepublickey_data, profilepublickey_data1)) {
         if (profilepublickey_data != null && profilepublickey_data1.expiresAt().isBefore(profilepublickey_data.expiresAt())) {
            this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
         } else {
            try {
               SignatureValidator signaturevalidator = this.server.getProfileKeySignatureValidator();
               if (signaturevalidator == null) {
                  LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)this.player.getGameProfile().getName());
                  return;
               }

               this.resetPlayerChatState(remotechatsession_data.validate(this.player.getGameProfile(), signaturevalidator, Duration.ZERO));
            } catch (ProfilePublicKey.ValidationException var6) {
               LOGGER.error("Failed to validate profile key: {}", (Object)var6.getMessage());
               this.disconnect(var6.getComponent());
            }

         }
      }
   }

   private void resetPlayerChatState(RemoteChatSession remotechatsession) {
      this.chatSession = remotechatsession;
      this.signedMessageDecoder = remotechatsession.createMessageDecoder(this.player.getUUID());
      this.chatMessageChain.append((executor) -> {
         this.player.setChatSession(remotechatsession);
         this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)));
         return CompletableFuture.completedFuture((Object)null);
      });
   }

   public ServerPlayer getPlayer() {
      return this.player;
   }

   @FunctionalInterface
   interface EntityInteraction {
      InteractionResult run(ServerPlayer serverplayer, Entity entity, InteractionHand interactionhand);
   }
}
