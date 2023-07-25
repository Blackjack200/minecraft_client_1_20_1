package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList {
   public static final File USERBANLIST_FILE = new File("banned-players.json");
   public static final File IPBANLIST_FILE = new File("banned-ips.json");
   public static final File OPLIST_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SEND_PLAYER_INFO_INTERVAL = 600;
   private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer server;
   private final List<ServerPlayer> players = Lists.newArrayList();
   private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
   private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
   private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
   private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
   private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
   private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
   private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
   private final PlayerDataStorage playerIo;
   private boolean doWhiteList;
   private final LayeredRegistryAccess<RegistryLayer> registries;
   private final RegistryAccess.Frozen synchronizedRegistries;
   protected final int maxPlayers;
   private int viewDistance;
   private int simulationDistance;
   private boolean allowCheatsForAllPlayers;
   private static final boolean ALLOW_LOGOUTIVATOR = false;
   private int sendAllPlayerInfoIn;

   public PlayerList(MinecraftServer minecraftserver, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, PlayerDataStorage playerdatastorage, int i) {
      this.server = minecraftserver;
      this.registries = layeredregistryaccess;
      this.synchronizedRegistries = (new RegistryAccess.ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(layeredregistryaccess))).freeze();
      this.maxPlayers = i;
      this.playerIo = playerdatastorage;
   }

   public void placeNewPlayer(Connection connection, ServerPlayer serverplayer) {
      GameProfile gameprofile = serverplayer.getGameProfile();
      GameProfileCache gameprofilecache = this.server.getProfileCache();
      String s;
      if (gameprofilecache != null) {
         Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
         s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
         gameprofilecache.add(gameprofile);
      } else {
         s = gameprofile.getName();
      }

      CompoundTag compoundtag = this.load(serverplayer);
      ResourceKey<Level> resourcekey = compoundtag != null ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD) : Level.OVERWORLD;
      ServerLevel serverlevel = this.server.getLevel(resourcekey);
      ServerLevel serverlevel1;
      if (serverlevel == null) {
         LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)resourcekey);
         serverlevel1 = this.server.overworld();
      } else {
         serverlevel1 = serverlevel;
      }

      serverplayer.setServerLevel(serverlevel1);
      String s2 = "local";
      if (connection.getRemoteAddress() != null) {
         s2 = connection.getRemoteAddress().toString();
      }

      LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", serverplayer.getName().getString(), s2, serverplayer.getId(), serverplayer.getX(), serverplayer.getY(), serverplayer.getZ());
      LevelData leveldata = serverlevel1.getLevelData();
      serverplayer.loadGameTypes(compoundtag);
      ServerGamePacketListenerImpl servergamepacketlistenerimpl = new ServerGamePacketListenerImpl(this.server, connection, serverplayer);
      GameRules gamerules = serverlevel1.getGameRules();
      boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
      boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
      servergamepacketlistenerimpl.send(new ClientboundLoginPacket(serverplayer.getId(), leveldata.isHardcore(), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), this.server.levelKeys(), this.synchronizedRegistries, serverlevel1.dimensionTypeId(), serverlevel1.dimension(), BiomeManager.obfuscateSeed(serverlevel1.getSeed()), this.getMaxPlayers(), this.viewDistance, this.simulationDistance, flag1, !flag, serverlevel1.isDebug(), serverlevel1.isFlat(), serverplayer.getLastDeathLocation(), serverplayer.getPortalCooldown()));
      servergamepacketlistenerimpl.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(serverlevel1.enabledFeatures())));
      servergamepacketlistenerimpl.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(this.getServer().getServerModName())));
      servergamepacketlistenerimpl.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
      servergamepacketlistenerimpl.send(new ClientboundPlayerAbilitiesPacket(serverplayer.getAbilities()));
      servergamepacketlistenerimpl.send(new ClientboundSetCarriedItemPacket(serverplayer.getInventory().selected));
      servergamepacketlistenerimpl.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
      servergamepacketlistenerimpl.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
      this.sendPlayerPermissionLevel(serverplayer);
      serverplayer.getStats().markAllDirty();
      serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
      this.updateEntireScoreboard(serverlevel1.getScoreboard(), serverplayer);
      this.server.invalidateStatus();
      MutableComponent mutablecomponent;
      if (serverplayer.getGameProfile().getName().equalsIgnoreCase(s)) {
         mutablecomponent = Component.translatable("multiplayer.player.joined", serverplayer.getDisplayName());
      } else {
         mutablecomponent = Component.translatable("multiplayer.player.joined.renamed", serverplayer.getDisplayName(), s);
      }

      this.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);
      servergamepacketlistenerimpl.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
      ServerStatus serverstatus = this.server.getStatus();
      if (serverstatus != null) {
         serverplayer.sendServerStatus(serverstatus);
      }

      serverplayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
      this.players.add(serverplayer);
      this.playersByUUID.put(serverplayer.getUUID(), serverplayer);
      this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(serverplayer)));
      this.sendLevelInfo(serverplayer, serverlevel1);
      serverlevel1.addNewPlayer(serverplayer);
      this.server.getCustomBossEvents().onPlayerConnect(serverplayer);
      this.server.getServerResourcePack().ifPresent((minecraftserver_serverresourcepackinfo) -> serverplayer.sendTexturePack(minecraftserver_serverresourcepackinfo.url(), minecraftserver_serverresourcepackinfo.hash(), minecraftserver_serverresourcepackinfo.isRequired(), minecraftserver_serverresourcepackinfo.prompt()));

      for(MobEffectInstance mobeffectinstance : serverplayer.getActiveEffects()) {
         servergamepacketlistenerimpl.send(new ClientboundUpdateMobEffectPacket(serverplayer.getId(), mobeffectinstance));
      }

      if (compoundtag != null && compoundtag.contains("RootVehicle", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("RootVehicle");
         Entity entity = EntityType.loadEntityRecursive(compoundtag1.getCompound("Entity"), serverlevel1, (entity3) -> !serverlevel1.addWithUUID(entity3) ? null : entity3);
         if (entity != null) {
            UUID uuid;
            if (compoundtag1.hasUUID("Attach")) {
               uuid = compoundtag1.getUUID("Attach");
            } else {
               uuid = null;
            }

            if (entity.getUUID().equals(uuid)) {
               serverplayer.startRiding(entity, true);
            } else {
               for(Entity entity1 : entity.getIndirectPassengers()) {
                  if (entity1.getUUID().equals(uuid)) {
                     serverplayer.startRiding(entity1, true);
                     break;
                  }
               }
            }

            if (!serverplayer.isPassenger()) {
               LOGGER.warn("Couldn't reattach entity to player");
               entity.discard();

               for(Entity entity2 : entity.getIndirectPassengers()) {
                  entity2.discard();
               }
            }
         }
      }

      serverplayer.initInventoryMenu();
   }

   protected void updateEntireScoreboard(ServerScoreboard serverscoreboard, ServerPlayer serverplayer) {
      Set<Objective> set = Sets.newHashSet();

      for(PlayerTeam playerteam : serverscoreboard.getPlayerTeams()) {
         serverplayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, true));
      }

      for(int i = 0; i < 19; ++i) {
         Objective objective = serverscoreboard.getDisplayObjective(i);
         if (objective != null && !set.contains(objective)) {
            for(Packet<?> packet : serverscoreboard.getStartTrackingPackets(objective)) {
               serverplayer.connection.send(packet);
            }

            set.add(objective);
         }
      }

   }

   public void addWorldborderListener(ServerLevel serverlevel) {
      serverlevel.getWorldBorder().addListener(new BorderChangeListener() {
         public void onBorderSizeSet(WorldBorder worldborder, double d0) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(worldborder));
         }

         public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder));
         }

         public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(worldborder));
         }

         public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder));
         }

         public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder));
         }

         public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {
         }

         public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {
         }
      });
   }

   @Nullable
   public CompoundTag load(ServerPlayer serverplayer) {
      CompoundTag compoundtag = this.server.getWorldData().getLoadedPlayerTag();
      CompoundTag compoundtag1;
      if (this.server.isSingleplayerOwner(serverplayer.getGameProfile()) && compoundtag != null) {
         compoundtag1 = compoundtag;
         serverplayer.load(compoundtag);
         LOGGER.debug("loading single player");
      } else {
         compoundtag1 = this.playerIo.load(serverplayer);
      }

      return compoundtag1;
   }

   protected void save(ServerPlayer serverplayer) {
      this.playerIo.save(serverplayer);
      ServerStatsCounter serverstatscounter = this.stats.get(serverplayer.getUUID());
      if (serverstatscounter != null) {
         serverstatscounter.save();
      }

      PlayerAdvancements playeradvancements = this.advancements.get(serverplayer.getUUID());
      if (playeradvancements != null) {
         playeradvancements.save();
      }

   }

   public void remove(ServerPlayer serverplayer) {
      ServerLevel serverlevel = serverplayer.serverLevel();
      serverplayer.awardStat(Stats.LEAVE_GAME);
      this.save(serverplayer);
      if (serverplayer.isPassenger()) {
         Entity entity = serverplayer.getRootVehicle();
         if (entity.hasExactlyOnePlayerPassenger()) {
            LOGGER.debug("Removing player mount");
            serverplayer.stopRiding();
            entity.getPassengersAndSelf().forEach((entity1) -> entity1.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
         }
      }

      serverplayer.unRide();
      serverlevel.removePlayerImmediately(serverplayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
      serverplayer.getAdvancements().stopListening();
      this.players.remove(serverplayer);
      this.server.getCustomBossEvents().onPlayerDisconnect(serverplayer);
      UUID uuid = serverplayer.getUUID();
      ServerPlayer serverplayer1 = this.playersByUUID.get(uuid);
      if (serverplayer1 == serverplayer) {
         this.playersByUUID.remove(uuid);
         this.stats.remove(uuid);
         this.advancements.remove(uuid);
      }

      this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(serverplayer.getUUID())));
   }

   @Nullable
   public Component canPlayerLogin(SocketAddress socketaddress, GameProfile gameprofile) {
      if (this.bans.isBanned(gameprofile)) {
         UserBanListEntry userbanlistentry = this.bans.get(gameprofile);
         MutableComponent mutablecomponent = Component.translatable("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());
         if (userbanlistentry.getExpires() != null) {
            mutablecomponent.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
         }

         return mutablecomponent;
      } else if (!this.isWhiteListed(gameprofile)) {
         return Component.translatable("multiplayer.disconnect.not_whitelisted");
      } else if (this.ipBans.isBanned(socketaddress)) {
         IpBanListEntry ipbanlistentry = this.ipBans.get(socketaddress);
         MutableComponent mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());
         if (ipbanlistentry.getExpires() != null) {
            mutablecomponent1.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
         }

         return mutablecomponent1;
      } else {
         return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameprofile) ? Component.translatable("multiplayer.disconnect.server_full") : null;
      }
   }

   public ServerPlayer getPlayerForLogin(GameProfile gameprofile) {
      UUID uuid = UUIDUtil.getOrCreatePlayerUUID(gameprofile);
      List<ServerPlayer> list = Lists.newArrayList();

      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayer serverplayer = this.players.get(i);
         if (serverplayer.getUUID().equals(uuid)) {
            list.add(serverplayer);
         }
      }

      ServerPlayer serverplayer1 = this.playersByUUID.get(gameprofile.getId());
      if (serverplayer1 != null && !list.contains(serverplayer1)) {
         list.add(serverplayer1);
      }

      for(ServerPlayer serverplayer2 : list) {
         serverplayer2.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
      }

      return new ServerPlayer(this.server, this.server.overworld(), gameprofile);
   }

   public ServerPlayer respawn(ServerPlayer serverplayer, boolean flag) {
      this.players.remove(serverplayer);
      serverplayer.serverLevel().removePlayerImmediately(serverplayer, Entity.RemovalReason.DISCARDED);
      BlockPos blockpos = serverplayer.getRespawnPosition();
      float f = serverplayer.getRespawnAngle();
      boolean flag1 = serverplayer.isRespawnForced();
      ServerLevel serverlevel = this.server.getLevel(serverplayer.getRespawnDimension());
      Optional<Vec3> optional;
      if (serverlevel != null && blockpos != null) {
         optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag1, flag);
      } else {
         optional = Optional.empty();
      }

      ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : this.server.overworld();
      ServerPlayer serverplayer1 = new ServerPlayer(this.server, serverlevel1, serverplayer.getGameProfile());
      serverplayer1.connection = serverplayer.connection;
      serverplayer1.restoreFrom(serverplayer, flag);
      serverplayer1.setId(serverplayer.getId());
      serverplayer1.setMainArm(serverplayer.getMainArm());

      for(String s : serverplayer.getTags()) {
         serverplayer1.addTag(s);
      }

      boolean flag2 = false;
      if (optional.isPresent()) {
         BlockState blockstate = serverlevel1.getBlockState(blockpos);
         boolean flag3 = blockstate.is(Blocks.RESPAWN_ANCHOR);
         Vec3 vec3 = optional.get();
         float f2;
         if (!blockstate.is(BlockTags.BEDS) && !flag3) {
            f2 = f;
         } else {
            Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
            f2 = (float)Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI) - 90.0D);
         }

         serverplayer1.moveTo(vec3.x, vec3.y, vec3.z, f2, 0.0F);
         serverplayer1.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag1, false);
         flag2 = !flag && flag3;
      } else if (blockpos != null) {
         serverplayer1.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
      }

      while(!serverlevel1.noCollision(serverplayer1) && serverplayer1.getY() < (double)serverlevel1.getMaxBuildHeight()) {
         serverplayer1.setPos(serverplayer1.getX(), serverplayer1.getY() + 1.0D, serverplayer1.getZ());
      }

      byte b0 = (byte)(flag ? 1 : 0);
      LevelData leveldata = serverplayer1.level().getLevelData();
      serverplayer1.connection.send(new ClientboundRespawnPacket(serverplayer1.level().dimensionTypeId(), serverplayer1.level().dimension(), BiomeManager.obfuscateSeed(serverplayer1.serverLevel().getSeed()), serverplayer1.gameMode.getGameModeForPlayer(), serverplayer1.gameMode.getPreviousGameModeForPlayer(), serverplayer1.level().isDebug(), serverplayer1.serverLevel().isFlat(), b0, serverplayer1.getLastDeathLocation(), serverplayer1.getPortalCooldown()));
      serverplayer1.connection.teleport(serverplayer1.getX(), serverplayer1.getY(), serverplayer1.getZ(), serverplayer1.getYRot(), serverplayer1.getXRot());
      serverplayer1.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
      serverplayer1.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
      serverplayer1.connection.send(new ClientboundSetExperiencePacket(serverplayer1.experienceProgress, serverplayer1.totalExperience, serverplayer1.experienceLevel));
      this.sendLevelInfo(serverplayer1, serverlevel1);
      this.sendPlayerPermissionLevel(serverplayer1);
      serverlevel1.addRespawnedPlayer(serverplayer1);
      this.players.add(serverplayer1);
      this.playersByUUID.put(serverplayer1.getUUID(), serverplayer1);
      serverplayer1.initInventoryMenu();
      serverplayer1.setHealth(serverplayer1.getHealth());
      if (flag2) {
         serverplayer1.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F, serverlevel1.getRandom().nextLong()));
      }

      return serverplayer1;
   }

   public void sendPlayerPermissionLevel(ServerPlayer serverplayer) {
      GameProfile gameprofile = serverplayer.getGameProfile();
      int i = this.server.getProfilePermissions(gameprofile);
      this.sendPlayerPermissionLevel(serverplayer, i);
   }

   public void tick() {
      if (++this.sendAllPlayerInfoIn > 600) {
         this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players));
         this.sendAllPlayerInfoIn = 0;
      }

   }

   public void broadcastAll(Packet<?> packet) {
      for(ServerPlayer serverplayer : this.players) {
         serverplayer.connection.send(packet);
      }

   }

   public void broadcastAll(Packet<?> packet, ResourceKey<Level> resourcekey) {
      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.level().dimension() == resourcekey) {
            serverplayer.connection.send(packet);
         }
      }

   }

   public void broadcastSystemToTeam(Player player, Component component) {
      Team team = player.getTeam();
      if (team != null) {
         for(String s : team.getPlayers()) {
            ServerPlayer serverplayer = this.getPlayerByName(s);
            if (serverplayer != null && serverplayer != player) {
               serverplayer.sendSystemMessage(component);
            }
         }

      }
   }

   public void broadcastSystemToAllExceptTeam(Player player, Component component) {
      Team team = player.getTeam();
      if (team == null) {
         this.broadcastSystemMessage(component, false);
      } else {
         for(int i = 0; i < this.players.size(); ++i) {
            ServerPlayer serverplayer = this.players.get(i);
            if (serverplayer.getTeam() != team) {
               serverplayer.sendSystemMessage(component);
            }
         }

      }
   }

   public String[] getPlayerNamesArray() {
      String[] astring = new String[this.players.size()];

      for(int i = 0; i < this.players.size(); ++i) {
         astring[i] = this.players.get(i).getGameProfile().getName();
      }

      return astring;
   }

   public UserBanList getBans() {
      return this.bans;
   }

   public IpBanList getIpBans() {
      return this.ipBans;
   }

   public void op(GameProfile gameprofile) {
      this.ops.add(new ServerOpListEntry(gameprofile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(gameprofile)));
      ServerPlayer serverplayer = this.getPlayer(gameprofile.getId());
      if (serverplayer != null) {
         this.sendPlayerPermissionLevel(serverplayer);
      }

   }

   public void deop(GameProfile gameprofile) {
      this.ops.remove(gameprofile);
      ServerPlayer serverplayer = this.getPlayer(gameprofile.getId());
      if (serverplayer != null) {
         this.sendPlayerPermissionLevel(serverplayer);
      }

   }

   private void sendPlayerPermissionLevel(ServerPlayer serverplayer, int i) {
      if (serverplayer.connection != null) {
         byte b0;
         if (i <= 0) {
            b0 = 24;
         } else if (i >= 4) {
            b0 = 28;
         } else {
            b0 = (byte)(24 + i);
         }

         serverplayer.connection.send(new ClientboundEntityEventPacket(serverplayer, b0));
      }

      this.server.getCommands().sendCommands(serverplayer);
   }

   public boolean isWhiteListed(GameProfile gameprofile) {
      return !this.doWhiteList || this.ops.contains(gameprofile) || this.whitelist.contains(gameprofile);
   }

   public boolean isOp(GameProfile gameprofile) {
      return this.ops.contains(gameprofile) || this.server.isSingleplayerOwner(gameprofile) && this.server.getWorldData().getAllowCommands() || this.allowCheatsForAllPlayers;
   }

   @Nullable
   public ServerPlayer getPlayerByName(String s) {
      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.getGameProfile().getName().equalsIgnoreCase(s)) {
            return serverplayer;
         }
      }

      return null;
   }

   public void broadcast(@Nullable Player player, double d0, double d1, double d2, double d3, ResourceKey<Level> resourcekey, Packet<?> packet) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayer serverplayer = this.players.get(i);
         if (serverplayer != player && serverplayer.level().dimension() == resourcekey) {
            double d4 = d0 - serverplayer.getX();
            double d5 = d1 - serverplayer.getY();
            double d6 = d2 - serverplayer.getZ();
            if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
               serverplayer.connection.send(packet);
            }
         }
      }

   }

   public void saveAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         this.save(this.players.get(i));
      }

   }

   public UserWhiteList getWhiteList() {
      return this.whitelist;
   }

   public String[] getWhiteListNames() {
      return this.whitelist.getUserList();
   }

   public ServerOpList getOps() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getUserList();
   }

   public void reloadWhiteList() {
   }

   public void sendLevelInfo(ServerPlayer serverplayer, ServerLevel serverlevel) {
      WorldBorder worldborder = this.server.overworld().getWorldBorder();
      serverplayer.connection.send(new ClientboundInitializeBorderPacket(worldborder));
      serverplayer.connection.send(new ClientboundSetTimePacket(serverlevel.getGameTime(), serverlevel.getDayTime(), serverlevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
      serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel.getSharedSpawnPos(), serverlevel.getSharedSpawnAngle()));
      if (serverlevel.isRaining()) {
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, serverlevel.getRainLevel(1.0F)));
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, serverlevel.getThunderLevel(1.0F)));
      }

   }

   public void sendAllPlayerInfo(ServerPlayer serverplayer) {
      serverplayer.inventoryMenu.sendAllDataToRemote();
      serverplayer.resetSentInfo();
      serverplayer.connection.send(new ClientboundSetCarriedItemPacket(serverplayer.getInventory().selected));
   }

   public int getPlayerCount() {
      return this.players.size();
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public boolean isUsingWhitelist() {
      return this.doWhiteList;
   }

   public void setUsingWhiteList(boolean flag) {
      this.doWhiteList = flag;
   }

   public List<ServerPlayer> getPlayersWithAddress(String s) {
      List<ServerPlayer> list = Lists.newArrayList();

      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.getIpAddress().equals(s)) {
            list.add(serverplayer);
         }
      }

      return list;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   @Nullable
   public CompoundTag getSingleplayerData() {
      return null;
   }

   public void setAllowCheatsForAllPlayers(boolean flag) {
      this.allowCheatsForAllPlayers = flag;
   }

   public void removeAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         (this.players.get(i)).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
      }

   }

   public void broadcastSystemMessage(Component component, boolean flag) {
      this.broadcastSystemMessage(component, (serverplayer) -> component, flag);
   }

   public void broadcastSystemMessage(Component component, Function<ServerPlayer, Component> function, boolean flag) {
      this.server.sendSystemMessage(component);

      for(ServerPlayer serverplayer : this.players) {
         Component component1 = function.apply(serverplayer);
         if (component1 != null) {
            serverplayer.sendSystemMessage(component1, flag);
         }
      }

   }

   public void broadcastChatMessage(PlayerChatMessage playerchatmessage, CommandSourceStack commandsourcestack, ChatType.Bound chattype_bound) {
      this.broadcastChatMessage(playerchatmessage, commandsourcestack::shouldFilterMessageTo, commandsourcestack.getPlayer(), chattype_bound);
   }

   public void broadcastChatMessage(PlayerChatMessage playerchatmessage, ServerPlayer serverplayer, ChatType.Bound chattype_bound) {
      this.broadcastChatMessage(playerchatmessage, serverplayer::shouldFilterMessageTo, serverplayer, chattype_bound);
   }

   private void broadcastChatMessage(PlayerChatMessage playerchatmessage, Predicate<ServerPlayer> predicate, @Nullable ServerPlayer serverplayer, ChatType.Bound chattype_bound) {
      boolean flag = this.verifyChatTrusted(playerchatmessage);
      this.server.logChatMessage(playerchatmessage.decoratedContent(), chattype_bound, flag ? null : "Not Secure");
      OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(playerchatmessage);
      boolean flag1 = false;

      for(ServerPlayer serverplayer1 : this.players) {
         boolean flag2 = predicate.test(serverplayer1);
         serverplayer1.sendChatMessage(outgoingchatmessage, flag2, chattype_bound);
         flag1 |= flag2 && playerchatmessage.isFullyFiltered();
      }

      if (flag1 && serverplayer != null) {
         serverplayer.sendSystemMessage(CHAT_FILTERED_FULL);
      }

   }

   private boolean verifyChatTrusted(PlayerChatMessage playerchatmessage) {
      return playerchatmessage.hasSignature() && !playerchatmessage.hasExpiredServer(Instant.now());
   }

   public ServerStatsCounter getPlayerStats(Player player) {
      UUID uuid = player.getUUID();
      ServerStatsCounter serverstatscounter = this.stats.get(uuid);
      if (serverstatscounter == null) {
         File file = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
         File file1 = new File(file, uuid + ".json");
         if (!file1.exists()) {
            File file2 = new File(file, player.getName().getString() + ".json");
            Path path = file2.toPath();
            if (FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path) && path.startsWith(file.getPath()) && file2.isFile()) {
               file2.renameTo(file1);
            }
         }

         serverstatscounter = new ServerStatsCounter(this.server, file1);
         this.stats.put(uuid, serverstatscounter);
      }

      return serverstatscounter;
   }

   public PlayerAdvancements getPlayerAdvancements(ServerPlayer serverplayer) {
      UUID uuid = serverplayer.getUUID();
      PlayerAdvancements playeradvancements = this.advancements.get(uuid);
      if (playeradvancements == null) {
         Path path = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json");
         playeradvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), path, serverplayer);
         this.advancements.put(uuid, playeradvancements);
      }

      playeradvancements.setPlayer(serverplayer);
      return playeradvancements;
   }

   public void setViewDistance(int i) {
      this.viewDistance = i;
      this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(i));

      for(ServerLevel serverlevel : this.server.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.getChunkSource().setViewDistance(i);
         }
      }

   }

   public void setSimulationDistance(int i) {
      this.simulationDistance = i;
      this.broadcastAll(new ClientboundSetSimulationDistancePacket(i));

      for(ServerLevel serverlevel : this.server.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.getChunkSource().setSimulationDistance(i);
         }
      }

   }

   public List<ServerPlayer> getPlayers() {
      return this.players;
   }

   @Nullable
   public ServerPlayer getPlayer(UUID uuid) {
      return this.playersByUUID.get(uuid);
   }

   public boolean canBypassPlayerLimit(GameProfile gameprofile) {
      return false;
   }

   public void reloadResources() {
      for(PlayerAdvancements playeradvancements : this.advancements.values()) {
         playeradvancements.reload(this.server.getAdvancements());
      }

      this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
      ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

      for(ServerPlayer serverplayer : this.players) {
         serverplayer.connection.send(clientboundupdaterecipespacket);
         serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
      }

   }

   public boolean isAllowCheatsForAllPlayers() {
      return this.allowCheatsForAllPlayers;
   }
}
