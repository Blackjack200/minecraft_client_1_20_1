package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ServerPlayer extends Player {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
   private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
   public ServerGamePacketListenerImpl connection;
   public final MinecraftServer server;
   public final ServerPlayerGameMode gameMode;
   private final PlayerAdvancements advancements;
   private final ServerStatsCounter stats;
   private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
   private int lastRecordedFoodLevel = Integer.MIN_VALUE;
   private int lastRecordedAirLevel = Integer.MIN_VALUE;
   private int lastRecordedArmor = Integer.MIN_VALUE;
   private int lastRecordedLevel = Integer.MIN_VALUE;
   private int lastRecordedExperience = Integer.MIN_VALUE;
   private float lastSentHealth = -1.0E8F;
   private int lastSentFood = -99999999;
   private boolean lastFoodSaturationZero = true;
   private int lastSentExp = -99999999;
   private int spawnInvulnerableTime = 60;
   private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
   private boolean canChatColor = true;
   private long lastActionTime = Util.getMillis();
   @Nullable
   private Entity camera;
   private boolean isChangingDimension;
   private boolean seenCredits;
   private final ServerRecipeBook recipeBook = new ServerRecipeBook();
   @Nullable
   private Vec3 levitationStartPos;
   private int levitationStartTime;
   private boolean disconnected;
   @Nullable
   private Vec3 startingToFallPosition;
   @Nullable
   private Vec3 enteredNetherPosition;
   @Nullable
   private Vec3 enteredLavaOnVehiclePosition;
   private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
   private ResourceKey<Level> respawnDimension = Level.OVERWORLD;
   @Nullable
   private BlockPos respawnPosition;
   private boolean respawnForced;
   private float respawnAngle;
   private final TextFilter textFilter;
   private boolean textFilteringEnabled;
   private boolean allowsListing;
   private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker(0, 0, 0);
   private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer() {
      public void sendInitialData(AbstractContainerMenu abstractcontainermenu, NonNullList<ItemStack> nonnulllist, ItemStack itemstack, int[] aint) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetContentPacket(abstractcontainermenu.containerId, abstractcontainermenu.incrementStateId(), nonnulllist, itemstack));

         for(int i = 0; i < aint.length; ++i) {
            this.broadcastDataValue(abstractcontainermenu, i, aint[i]);
         }

      }

      public void sendSlotChange(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(abstractcontainermenu.containerId, abstractcontainermenu.incrementStateId(), i, itemstack));
      }

      public void sendCarriedChange(AbstractContainerMenu abstractcontainermenu, ItemStack itemstack) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(-1, abstractcontainermenu.incrementStateId(), -1, itemstack));
      }

      public void sendDataChange(AbstractContainerMenu abstractcontainermenu, int i, int j) {
         this.broadcastDataValue(abstractcontainermenu, i, j);
      }

      private void broadcastDataValue(AbstractContainerMenu abstractcontainermenu, int i, int j) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(abstractcontainermenu.containerId, i, j));
      }
   };
   private final ContainerListener containerListener = new ContainerListener() {
      public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
         Slot slot = abstractcontainermenu.getSlot(i);
         if (!(slot instanceof ResultSlot)) {
            if (slot.container == ServerPlayer.this.getInventory()) {
               CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), itemstack);
            }

         }
      }

      public void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j) {
      }
   };
   @Nullable
   private RemoteChatSession chatSession;
   private int containerCounter;
   public int latency;
   public boolean wonGame;

   public ServerPlayer(MinecraftServer minecraftserver, ServerLevel serverlevel, GameProfile gameprofile) {
      super(serverlevel, serverlevel.getSharedSpawnPos(), serverlevel.getSharedSpawnAngle(), gameprofile);
      this.textFilter = minecraftserver.createTextFilterForPlayer(this);
      this.gameMode = minecraftserver.createGameModeForPlayer(this);
      this.server = minecraftserver;
      this.stats = minecraftserver.getPlayerList().getPlayerStats(this);
      this.advancements = minecraftserver.getPlayerList().getPlayerAdvancements(this);
      this.setMaxUpStep(1.0F);
      this.fudgeSpawnLocation(serverlevel);
   }

   private void fudgeSpawnLocation(ServerLevel serverlevel) {
      BlockPos blockpos = serverlevel.getSharedSpawnPos();
      if (serverlevel.dimensionType().hasSkyLight() && serverlevel.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
         int i = Math.max(0, this.server.getSpawnRadius(serverlevel));
         int j = Mth.floor(serverlevel.getWorldBorder().getDistanceToBorder((double)blockpos.getX(), (double)blockpos.getZ()));
         if (j < i) {
            i = j;
         }

         if (j <= 1) {
            i = 1;
         }

         long k = (long)(i * 2 + 1);
         long l = k * k;
         int i1 = l > 2147483647L ? Integer.MAX_VALUE : (int)l;
         int j1 = this.getCoprime(i1);
         int k1 = RandomSource.create().nextInt(i1);

         for(int l1 = 0; l1 < i1; ++l1) {
            int i2 = (k1 + j1 * l1) % i1;
            int j2 = i2 % (i * 2 + 1);
            int k2 = i2 / (i * 2 + 1);
            BlockPos blockpos1 = PlayerRespawnLogic.getOverworldRespawnPos(serverlevel, blockpos.getX() + j2 - i, blockpos.getZ() + k2 - i);
            if (blockpos1 != null) {
               this.moveTo(blockpos1, 0.0F, 0.0F);
               if (serverlevel.noCollision(this)) {
                  break;
               }
            }
         }
      } else {
         this.moveTo(blockpos, 0.0F, 0.0F);

         while(!serverlevel.noCollision(this) && this.getY() < (double)(serverlevel.getMaxBuildHeight() - 1)) {
            this.setPos(this.getX(), this.getY() + 1.0D, this.getZ());
         }
      }

   }

   private int getCoprime(int i) {
      return i <= 16 ? i - 1 : 17;
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("warden_spawn_tracker", 10)) {
         WardenSpawnTracker.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("warden_spawn_tracker"))).resultOrPartial(LOGGER::error).ifPresent((wardenspawntracker) -> this.wardenSpawnTracker = wardenspawntracker);
      }

      if (compoundtag.contains("enteredNetherPosition", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("enteredNetherPosition");
         this.enteredNetherPosition = new Vec3(compoundtag1.getDouble("x"), compoundtag1.getDouble("y"), compoundtag1.getDouble("z"));
      }

      this.seenCredits = compoundtag.getBoolean("seenCredits");
      if (compoundtag.contains("recipeBook", 10)) {
         this.recipeBook.fromNbt(compoundtag.getCompound("recipeBook"), this.server.getRecipeManager());
      }

      if (this.isSleeping()) {
         this.stopSleeping();
      }

      if (compoundtag.contains("SpawnX", 99) && compoundtag.contains("SpawnY", 99) && compoundtag.contains("SpawnZ", 99)) {
         this.respawnPosition = new BlockPos(compoundtag.getInt("SpawnX"), compoundtag.getInt("SpawnY"), compoundtag.getInt("SpawnZ"));
         this.respawnForced = compoundtag.getBoolean("SpawnForced");
         this.respawnAngle = compoundtag.getFloat("SpawnAngle");
         if (compoundtag.contains("SpawnDimension")) {
            this.respawnDimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundtag.get("SpawnDimension")).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD);
         }
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      WardenSpawnTracker.CODEC.encodeStart(NbtOps.INSTANCE, this.wardenSpawnTracker).resultOrPartial(LOGGER::error).ifPresent((tag1) -> compoundtag.put("warden_spawn_tracker", tag1));
      this.storeGameTypes(compoundtag);
      compoundtag.putBoolean("seenCredits", this.seenCredits);
      if (this.enteredNetherPosition != null) {
         CompoundTag compoundtag1 = new CompoundTag();
         compoundtag1.putDouble("x", this.enteredNetherPosition.x);
         compoundtag1.putDouble("y", this.enteredNetherPosition.y);
         compoundtag1.putDouble("z", this.enteredNetherPosition.z);
         compoundtag.put("enteredNetherPosition", compoundtag1);
      }

      Entity entity = this.getRootVehicle();
      Entity entity1 = this.getVehicle();
      if (entity1 != null && entity != this && entity.hasExactlyOnePlayerPassenger()) {
         CompoundTag compoundtag2 = new CompoundTag();
         CompoundTag compoundtag3 = new CompoundTag();
         entity.save(compoundtag3);
         compoundtag2.putUUID("Attach", entity1.getUUID());
         compoundtag2.put("Entity", compoundtag3);
         compoundtag.put("RootVehicle", compoundtag2);
      }

      compoundtag.put("recipeBook", this.recipeBook.toNbt());
      compoundtag.putString("Dimension", this.level().dimension().location().toString());
      if (this.respawnPosition != null) {
         compoundtag.putInt("SpawnX", this.respawnPosition.getX());
         compoundtag.putInt("SpawnY", this.respawnPosition.getY());
         compoundtag.putInt("SpawnZ", this.respawnPosition.getZ());
         compoundtag.putBoolean("SpawnForced", this.respawnForced);
         compoundtag.putFloat("SpawnAngle", this.respawnAngle);
         ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.respawnDimension.location()).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("SpawnDimension", tag));
      }

   }

   public void setExperiencePoints(int i) {
      float f = (float)this.getXpNeededForNextLevel();
      float f1 = (f - 1.0F) / f;
      this.experienceProgress = Mth.clamp((float)i / f, 0.0F, f1);
      this.lastSentExp = -1;
   }

   public void setExperienceLevels(int i) {
      this.experienceLevel = i;
      this.lastSentExp = -1;
   }

   public void giveExperienceLevels(int i) {
      super.giveExperienceLevels(i);
      this.lastSentExp = -1;
   }

   public void onEnchantmentPerformed(ItemStack itemstack, int i) {
      super.onEnchantmentPerformed(itemstack, i);
      this.lastSentExp = -1;
   }

   private void initMenu(AbstractContainerMenu abstractcontainermenu) {
      abstractcontainermenu.addSlotListener(this.containerListener);
      abstractcontainermenu.setSynchronizer(this.containerSynchronizer);
   }

   public void initInventoryMenu() {
      this.initMenu(this.inventoryMenu);
   }

   public void onEnterCombat() {
      super.onEnterCombat();
      this.connection.send(new ClientboundPlayerCombatEnterPacket());
   }

   public void onLeaveCombat() {
      super.onLeaveCombat();
      this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
   }

   protected void onInsideBlock(BlockState blockstate) {
      CriteriaTriggers.ENTER_BLOCK.trigger(this, blockstate);
   }

   protected ItemCooldowns createItemCooldowns() {
      return new ServerItemCooldowns(this);
   }

   public void tick() {
      this.gameMode.tick();
      this.wardenSpawnTracker.tick();
      --this.spawnInvulnerableTime;
      if (this.invulnerableTime > 0) {
         --this.invulnerableTime;
      }

      this.containerMenu.broadcastChanges();
      if (!this.level().isClientSide && !this.containerMenu.stillValid(this)) {
         this.closeContainer();
         this.containerMenu = this.inventoryMenu;
      }

      Entity entity = this.getCamera();
      if (entity != this) {
         if (entity.isAlive()) {
            this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            this.serverLevel().getChunkSource().move(this);
            if (this.wantsToStopRiding()) {
               this.setCamera(this);
            }
         } else {
            this.setCamera(this);
         }
      }

      CriteriaTriggers.TICK.trigger(this);
      if (this.levitationStartPos != null) {
         CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
      }

      this.trackStartFallingPosition();
      this.trackEnteredOrExitedLavaOnVehicle();
      this.advancements.flushDirty(this);
   }

   public void doTick() {
      try {
         if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
            super.tick();
         }

         for(int i = 0; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (itemstack.getItem().isComplex()) {
               Packet<?> packet = ((ComplexItem)itemstack.getItem()).getUpdatePacket(itemstack, this.level(), this);
               if (packet != null) {
                  this.connection.send(packet);
               }
            }
         }

         if (this.getHealth() != this.lastSentHealth || this.lastSentFood != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
            this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
            this.lastSentHealth = this.getHealth();
            this.lastSentFood = this.foodData.getFoodLevel();
            this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
         }

         if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
            this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
            this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
         }

         if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
            this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
            this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
         }

         if (this.getAirSupply() != this.lastRecordedAirLevel) {
            this.lastRecordedAirLevel = this.getAirSupply();
            this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
         }

         if (this.getArmorValue() != this.lastRecordedArmor) {
            this.lastRecordedArmor = this.getArmorValue();
            this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
         }

         if (this.totalExperience != this.lastRecordedExperience) {
            this.lastRecordedExperience = this.totalExperience;
            this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
         }

         if (this.experienceLevel != this.lastRecordedLevel) {
            this.lastRecordedLevel = this.experienceLevel;
            this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
         }

         if (this.totalExperience != this.lastSentExp) {
            this.lastSentExp = this.totalExperience;
            this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
         }

         if (this.tickCount % 20 == 0) {
            CriteriaTriggers.LOCATION.trigger(this);
         }

      } catch (Throwable var4) {
         CrashReport crashreport = CrashReport.forThrowable(var4, "Ticking player");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Player being ticked");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public void resetFallDistance() {
      if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
         CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
      }

      this.startingToFallPosition = null;
      super.resetFallDistance();
   }

   public void trackStartFallingPosition() {
      if (this.fallDistance > 0.0F && this.startingToFallPosition == null) {
         this.startingToFallPosition = this.position();
      }

   }

   public void trackEnteredOrExitedLavaOnVehicle() {
      if (this.getVehicle() != null && this.getVehicle().isInLava()) {
         if (this.enteredLavaOnVehiclePosition == null) {
            this.enteredLavaOnVehiclePosition = this.position();
         } else {
            CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
         }
      }

      if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
         this.enteredLavaOnVehiclePosition = null;
      }

   }

   private void updateScoreForCriteria(ObjectiveCriteria objectivecriteria, int i) {
      this.getScoreboard().forAllObjectives(objectivecriteria, this.getScoreboardName(), (score) -> score.setScore(i));
   }

   public void die(DamageSource damagesource) {
      this.gameEvent(GameEvent.ENTITY_DIE);
      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
      if (flag) {
         Component component = this.getCombatTracker().getDeathMessage();
         this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), component), PacketSendListener.exceptionallySend(() -> {
            int i = 256;
            String s = component.getString(256);
            Component component2 = Component.translatable("death.attack.message_too_long", Component.literal(s).withStyle(ChatFormatting.YELLOW));
            Component component3 = Component.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
            return new ClientboundPlayerCombatKillPacket(this.getId(), component3);
         }));
         Team team = this.getTeam();
         if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
            if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
               this.server.getPlayerList().broadcastSystemToTeam(this, component);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
               this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, component);
            }
         } else {
            this.server.getPlayerList().broadcastSystemMessage(component, false);
         }
      } else {
         this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
      }

      this.removeEntitiesOnShoulder();
      if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
         this.tellNeutralMobsThatIDied();
      }

      if (!this.isSpectator()) {
         this.dropAllDeathLoot(damagesource);
      }

      this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
      LivingEntity livingentity = this.getKillCredit();
      if (livingentity != null) {
         this.awardStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
         livingentity.awardKillScore(this, this.deathScore, damagesource);
         this.createWitherRose(livingentity);
      }

      this.level().broadcastEntityEvent(this, (byte)3);
      this.awardStat(Stats.DEATHS);
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      this.clearFire();
      this.setTicksFrozen(0);
      this.setSharedFlagOnFire(false);
      this.getCombatTracker().recheckStatus();
      this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
   }

   private void tellNeutralMobsThatIDied() {
      AABB aabb = (new AABB(this.blockPosition())).inflate(32.0D, 10.0D, 32.0D);
      this.level().getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS).stream().filter((mob1) -> mob1 instanceof NeutralMob).forEach((mob) -> ((NeutralMob)mob).playerDied(this));
   }

   public void awardKillScore(Entity entity, int i, DamageSource damagesource) {
      if (entity != this) {
         super.awardKillScore(entity, i, damagesource);
         this.increaseScore(i);
         String s = this.getScoreboardName();
         String s1 = entity.getScoreboardName();
         this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, s, Score::increment);
         if (entity instanceof Player) {
            this.awardStat(Stats.PLAYER_KILLS);
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, s, Score::increment);
         } else {
            this.awardStat(Stats.MOB_KILLS);
         }

         this.handleTeamKill(s, s1, ObjectiveCriteria.TEAM_KILL);
         this.handleTeamKill(s1, s, ObjectiveCriteria.KILLED_BY_TEAM);
         CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damagesource);
      }
   }

   private void handleTeamKill(String s, String s1, ObjectiveCriteria[] aobjectivecriteria) {
      PlayerTeam playerteam = this.getScoreboard().getPlayersTeam(s1);
      if (playerteam != null) {
         int i = playerteam.getColor().getId();
         if (i >= 0 && i < aobjectivecriteria.length) {
            this.getScoreboard().forAllObjectives(aobjectivecriteria[i], s, Score::increment);
         }
      }

   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         boolean flag = this.server.isDedicatedServer() && this.isPvpAllowed() && damagesource.is(DamageTypeTags.IS_FALL);
         if (!flag && this.spawnInvulnerableTime > 0 && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
         } else {
            Entity entity = damagesource.getEntity();
            if (entity instanceof Player) {
               Player player = (Player)entity;
               if (!this.canHarmPlayer(player)) {
                  return false;
               }
            }

            if (entity instanceof AbstractArrow) {
               AbstractArrow abstractarrow = (AbstractArrow)entity;
               Entity entity1 = abstractarrow.getOwner();
               if (entity1 instanceof Player) {
                  Player player1 = (Player)entity1;
                  if (!this.canHarmPlayer(player1)) {
                     return false;
                  }
               }
            }

            return super.hurt(damagesource, f);
         }
      }
   }

   public boolean canHarmPlayer(Player player) {
      return !this.isPvpAllowed() ? false : super.canHarmPlayer(player);
   }

   private boolean isPvpAllowed() {
      return this.server.isPvpAllowed();
   }

   @Nullable
   protected PortalInfo findDimensionEntryPoint(ServerLevel serverlevel) {
      PortalInfo portalinfo = super.findDimensionEntryPoint(serverlevel);
      if (portalinfo != null && this.level().dimension() == Level.OVERWORLD && serverlevel.dimension() == Level.END) {
         Vec3 vec3 = portalinfo.pos.add(0.0D, -1.0D, 0.0D);
         return new PortalInfo(vec3, Vec3.ZERO, 90.0F, 0.0F);
      } else {
         return portalinfo;
      }
   }

   @Nullable
   public Entity changeDimension(ServerLevel serverlevel) {
      this.isChangingDimension = true;
      ServerLevel serverlevel1 = this.serverLevel();
      ResourceKey<Level> resourcekey = serverlevel1.dimension();
      if (resourcekey == Level.END && serverlevel.dimension() == Level.OVERWORLD) {
         this.unRide();
         this.serverLevel().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
         if (!this.wonGame) {
            this.wonGame = true;
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, this.seenCredits ? 0.0F : 1.0F));
            this.seenCredits = true;
         }

         return this;
      } else {
         LevelData leveldata = serverlevel.getLevelData();
         this.connection.send(new ClientboundRespawnPacket(serverlevel.dimensionTypeId(), serverlevel.dimension(), BiomeManager.obfuscateSeed(serverlevel.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), serverlevel.isDebug(), serverlevel.isFlat(), (byte)3, this.getLastDeathLocation(), this.getPortalCooldown()));
         this.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
         PlayerList playerlist = this.server.getPlayerList();
         playerlist.sendPlayerPermissionLevel(this);
         serverlevel1.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
         this.unsetRemoved();
         PortalInfo portalinfo = this.findDimensionEntryPoint(serverlevel);
         if (portalinfo != null) {
            serverlevel1.getProfiler().push("moving");
            if (resourcekey == Level.OVERWORLD && serverlevel.dimension() == Level.NETHER) {
               this.enteredNetherPosition = this.position();
            } else if (serverlevel.dimension() == Level.END) {
               this.createEndPlatform(serverlevel, BlockPos.containing(portalinfo.pos));
            }

            serverlevel1.getProfiler().pop();
            serverlevel1.getProfiler().push("placing");
            this.setServerLevel(serverlevel);
            this.connection.teleport(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, portalinfo.xRot);
            this.connection.resetPosition();
            serverlevel.addDuringPortalTeleport(this);
            serverlevel1.getProfiler().pop();
            this.triggerDimensionChangeTriggers(serverlevel1);
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
            playerlist.sendLevelInfo(this, serverlevel);
            playerlist.sendAllPlayerInfo(this);

            for(MobEffectInstance mobeffectinstance : this.getActiveEffects()) {
               this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobeffectinstance));
            }

            this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
            this.lastSentExp = -1;
            this.lastSentHealth = -1.0F;
            this.lastSentFood = -1;
         }

         return this;
      }
   }

   private void createEndPlatform(ServerLevel serverlevel, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            for(int k = -1; k < 3; ++k) {
               BlockState blockstate = k == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
               serverlevel.setBlockAndUpdate(blockpos_mutableblockpos.set(blockpos).move(j, k, i), blockstate);
            }
         }
      }

   }

   protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel serverlevel, BlockPos blockpos, boolean flag, WorldBorder worldborder) {
      Optional<BlockUtil.FoundRectangle> optional = super.getExitPortal(serverlevel, blockpos, flag, worldborder);
      if (optional.isPresent()) {
         return optional;
      } else {
         Direction.Axis direction_axis = this.level().getBlockState(this.portalEntrancePos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
         Optional<BlockUtil.FoundRectangle> optional1 = serverlevel.getPortalForcer().createPortal(blockpos, direction_axis);
         if (!optional1.isPresent()) {
            LOGGER.error("Unable to create a portal, likely target out of worldborder");
         }

         return optional1;
      }
   }

   private void triggerDimensionChangeTriggers(ServerLevel serverlevel) {
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      ResourceKey<Level> resourcekey1 = this.level().dimension();
      CriteriaTriggers.CHANGED_DIMENSION.trigger(this, resourcekey, resourcekey1);
      if (resourcekey == Level.NETHER && resourcekey1 == Level.OVERWORLD && this.enteredNetherPosition != null) {
         CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
      }

      if (resourcekey1 != Level.NETHER) {
         this.enteredNetherPosition = null;
      }

   }

   public boolean broadcastToPlayer(ServerPlayer serverplayer) {
      if (serverplayer.isSpectator()) {
         return this.getCamera() == this;
      } else {
         return this.isSpectator() ? false : super.broadcastToPlayer(serverplayer);
      }
   }

   public void take(Entity entity, int i) {
      super.take(entity, i);
      this.containerMenu.broadcastChanges();
   }

   public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockpos) {
      Direction direction = this.level().getBlockState(blockpos).getValue(HorizontalDirectionalBlock.FACING);
      if (!this.isSleeping() && this.isAlive()) {
         if (!this.level().dimensionType().natural()) {
            return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
         } else if (!this.bedInRange(blockpos, direction)) {
            return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
         } else if (this.bedBlocked(blockpos, direction)) {
            return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
         } else {
            this.setRespawnPosition(this.level().dimension(), blockpos, this.getYRot(), false, true);
            if (this.level().isDay()) {
               return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            } else {
               if (!this.isCreative()) {
                  double d0 = 8.0D;
                  double d1 = 5.0D;
                  Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
                  List<Monster> list = this.level().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0D, vec3.y() - 5.0D, vec3.z() - 8.0D, vec3.x() + 8.0D, vec3.y() + 5.0D, vec3.z() + 8.0D), (monster) -> monster.isPreventingPlayerRest(this));
                  if (!list.isEmpty()) {
                     return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                  }
               }

               Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(blockpos).ifRight((unit) -> {
                  this.awardStat(Stats.SLEEP_IN_BED);
                  CriteriaTriggers.SLEPT_IN_BED.trigger(this);
               });
               if (!this.serverLevel().canSleepThroughNights()) {
                  this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
               }

               ((ServerLevel)this.level()).updateSleepingPlayerList();
               return either;
            }
         }
      } else {
         return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
      }
   }

   public void startSleeping(BlockPos blockpos) {
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      super.startSleeping(blockpos);
   }

   private boolean bedInRange(BlockPos blockpos, Direction direction) {
      return this.isReachableBedBlock(blockpos) || this.isReachableBedBlock(blockpos.relative(direction.getOpposite()));
   }

   private boolean isReachableBedBlock(BlockPos blockpos) {
      Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
      return Math.abs(this.getX() - vec3.x()) <= 3.0D && Math.abs(this.getY() - vec3.y()) <= 2.0D && Math.abs(this.getZ() - vec3.z()) <= 3.0D;
   }

   private boolean bedBlocked(BlockPos blockpos, Direction direction) {
      BlockPos blockpos1 = blockpos.above();
      return !this.freeAt(blockpos1) || !this.freeAt(blockpos1.relative(direction.getOpposite()));
   }

   public void stopSleepInBed(boolean flag, boolean flag1) {
      if (this.isSleeping()) {
         this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
      }

      super.stopSleepInBed(flag, flag1);
      if (this.connection != null) {
         this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
      }

   }

   public void dismountTo(double d0, double d1, double d2) {
      this.removeVehicle();
      this.setPos(d0, d1, d2);
   }

   public boolean isInvulnerableTo(DamageSource damagesource) {
      return super.isInvulnerableTo(damagesource) || this.isChangingDimension();
   }

   protected void checkFallDamage(double d0, boolean flag, BlockState blockstate, BlockPos blockpos) {
   }

   protected void onChangedBlock(BlockPos blockpos) {
      if (!this.isSpectator()) {
         super.onChangedBlock(blockpos);
      }

   }

   public void doCheckFallDamage(double d0, double d1, double d2, boolean flag) {
      if (!this.touchingUnloadedChunk()) {
         this.checkSupportingBlock(flag, new Vec3(d0, d1, d2));
         BlockPos blockpos = this.getOnPosLegacy();
         super.checkFallDamage(d1, flag, this.level().getBlockState(blockpos), blockpos);
      }
   }

   public void openTextEdit(SignBlockEntity signblockentity, boolean flag) {
      this.connection.send(new ClientboundBlockUpdatePacket(this.level(), signblockentity.getBlockPos()));
      this.connection.send(new ClientboundOpenSignEditorPacket(signblockentity.getBlockPos(), flag));
   }

   private void nextContainerCounter() {
      this.containerCounter = this.containerCounter % 100 + 1;
   }

   public OptionalInt openMenu(@Nullable MenuProvider menuprovider) {
      if (menuprovider == null) {
         return OptionalInt.empty();
      } else {
         if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
         }

         this.nextContainerCounter();
         AbstractContainerMenu abstractcontainermenu = menuprovider.createMenu(this.containerCounter, this.getInventory(), this);
         if (abstractcontainermenu == null) {
            if (this.isSpectator()) {
               this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
            }

            return OptionalInt.empty();
         } else {
            this.connection.send(new ClientboundOpenScreenPacket(abstractcontainermenu.containerId, abstractcontainermenu.getType(), menuprovider.getDisplayName()));
            this.initMenu(abstractcontainermenu);
            this.containerMenu = abstractcontainermenu;
            return OptionalInt.of(this.containerCounter);
         }
      }
   }

   public void sendMerchantOffers(int i, MerchantOffers merchantoffers, int j, int k, boolean flag, boolean flag1) {
      this.connection.send(new ClientboundMerchantOffersPacket(i, merchantoffers, j, k, flag, flag1));
   }

   public void openHorseInventory(AbstractHorse abstracthorse, Container container) {
      if (this.containerMenu != this.inventoryMenu) {
         this.closeContainer();
      }

      this.nextContainerCounter();
      this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, container.getContainerSize(), abstracthorse.getId()));
      this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, abstracthorse);
      this.initMenu(this.containerMenu);
   }

   public void openItemGui(ItemStack itemstack, InteractionHand interactionhand) {
      if (itemstack.is(Items.WRITTEN_BOOK)) {
         if (WrittenBookItem.resolveBookComponents(itemstack, this.createCommandSourceStack(), this)) {
            this.containerMenu.broadcastChanges();
         }

         this.connection.send(new ClientboundOpenBookPacket(interactionhand));
      }

   }

   public void openCommandBlock(CommandBlockEntity commandblockentity) {
      this.connection.send(ClientboundBlockEntityDataPacket.create(commandblockentity, BlockEntity::saveWithoutMetadata));
   }

   public void closeContainer() {
      this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
      this.doCloseContainer();
   }

   public void doCloseContainer() {
      this.containerMenu.removed(this);
      this.inventoryMenu.transferState(this.containerMenu);
      this.containerMenu = this.inventoryMenu;
   }

   public void setPlayerInput(float f, float f1, boolean flag, boolean flag1) {
      if (this.isPassenger()) {
         if (f >= -1.0F && f <= 1.0F) {
            this.xxa = f;
         }

         if (f1 >= -1.0F && f1 <= 1.0F) {
            this.zza = f1;
         }

         this.jumping = flag;
         this.setShiftKeyDown(flag1);
      }

   }

   public void awardStat(Stat<?> stat, int i) {
      this.stats.increment(this, stat, i);
      this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), (score) -> score.add(i));
   }

   public void resetStat(Stat<?> stat) {
      this.stats.setValue(this, stat, 0);
      this.getScoreboard().forAllObjectives(stat, this.getScoreboardName(), Score::reset);
   }

   public int awardRecipes(Collection<Recipe<?>> collection) {
      return this.recipeBook.addRecipes(collection, this);
   }

   public void triggerRecipeCrafted(Recipe<?> recipe, List<ItemStack> list) {
      CriteriaTriggers.RECIPE_CRAFTED.trigger(this, recipe.getId(), list);
   }

   public void awardRecipesByKey(ResourceLocation[] aresourcelocation) {
      List<Recipe<?>> list = Lists.newArrayList();

      for(ResourceLocation resourcelocation : aresourcelocation) {
         this.server.getRecipeManager().byKey(resourcelocation).ifPresent(list::add);
      }

      this.awardRecipes(list);
   }

   public int resetRecipes(Collection<Recipe<?>> collection) {
      return this.recipeBook.removeRecipes(collection, this);
   }

   public void giveExperiencePoints(int i) {
      super.giveExperiencePoints(i);
      this.lastSentExp = -1;
   }

   public void disconnect() {
      this.disconnected = true;
      this.ejectPassengers();
      if (this.isSleeping()) {
         this.stopSleepInBed(true, false);
      }

   }

   public boolean hasDisconnected() {
      return this.disconnected;
   }

   public void resetSentInfo() {
      this.lastSentHealth = -1.0E8F;
   }

   public void displayClientMessage(Component component, boolean flag) {
      this.sendSystemMessage(component, flag);
   }

   protected void completeUsingItem() {
      if (!this.useItem.isEmpty() && this.isUsingItem()) {
         this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
         super.completeUsingItem();
      }

   }

   public void lookAt(EntityAnchorArgument.Anchor entityanchorargument_anchor, Vec3 vec3) {
      super.lookAt(entityanchorargument_anchor, vec3);
      this.connection.send(new ClientboundPlayerLookAtPacket(entityanchorargument_anchor, vec3.x, vec3.y, vec3.z));
   }

   public void lookAt(EntityAnchorArgument.Anchor entityanchorargument_anchor, Entity entity, EntityAnchorArgument.Anchor entityanchorargument_anchor1) {
      Vec3 vec3 = entityanchorargument_anchor1.apply(entity);
      super.lookAt(entityanchorargument_anchor, vec3);
      this.connection.send(new ClientboundPlayerLookAtPacket(entityanchorargument_anchor, entity, entityanchorargument_anchor1));
   }

   public void restoreFrom(ServerPlayer serverplayer, boolean flag) {
      this.wardenSpawnTracker = serverplayer.wardenSpawnTracker;
      this.textFilteringEnabled = serverplayer.textFilteringEnabled;
      this.chatSession = serverplayer.chatSession;
      this.gameMode.setGameModeForPlayer(serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer());
      this.onUpdateAbilities();
      if (flag) {
         this.getInventory().replaceWith(serverplayer.getInventory());
         this.setHealth(serverplayer.getHealth());
         this.foodData = serverplayer.foodData;
         this.experienceLevel = serverplayer.experienceLevel;
         this.totalExperience = serverplayer.totalExperience;
         this.experienceProgress = serverplayer.experienceProgress;
         this.setScore(serverplayer.getScore());
         this.portalEntrancePos = serverplayer.portalEntrancePos;
      } else if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || serverplayer.isSpectator()) {
         this.getInventory().replaceWith(serverplayer.getInventory());
         this.experienceLevel = serverplayer.experienceLevel;
         this.totalExperience = serverplayer.totalExperience;
         this.experienceProgress = serverplayer.experienceProgress;
         this.setScore(serverplayer.getScore());
      }

      this.enchantmentSeed = serverplayer.enchantmentSeed;
      this.enderChestInventory = serverplayer.enderChestInventory;
      this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, serverplayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
      this.lastSentExp = -1;
      this.lastSentHealth = -1.0F;
      this.lastSentFood = -1;
      this.recipeBook.copyOverData(serverplayer.recipeBook);
      this.seenCredits = serverplayer.seenCredits;
      this.enteredNetherPosition = serverplayer.enteredNetherPosition;
      this.setShoulderEntityLeft(serverplayer.getShoulderEntityLeft());
      this.setShoulderEntityRight(serverplayer.getShoulderEntityRight());
      this.setLastDeathLocation(serverplayer.getLastDeathLocation());
   }

   protected void onEffectAdded(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      super.onEffectAdded(mobeffectinstance, entity);
      this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobeffectinstance));
      if (mobeffectinstance.getEffect() == MobEffects.LEVITATION) {
         this.levitationStartTime = this.tickCount;
         this.levitationStartPos = this.position();
      }

      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
   }

   protected void onEffectUpdated(MobEffectInstance mobeffectinstance, boolean flag, @Nullable Entity entity) {
      super.onEffectUpdated(mobeffectinstance, flag, entity);
      this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobeffectinstance));
      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
   }

   protected void onEffectRemoved(MobEffectInstance mobeffectinstance) {
      super.onEffectRemoved(mobeffectinstance);
      this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobeffectinstance.getEffect()));
      if (mobeffectinstance.getEffect() == MobEffects.LEVITATION) {
         this.levitationStartPos = null;
      }

      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, (Entity)null);
   }

   public void teleportTo(double d0, double d1, double d2) {
      this.connection.teleport(d0, d1, d2, this.getYRot(), this.getXRot(), RelativeMovement.ROTATION);
   }

   public void teleportRelative(double d0, double d1, double d2) {
      this.connection.teleport(this.getX() + d0, this.getY() + d1, this.getZ() + d2, this.getYRot(), this.getXRot(), RelativeMovement.ALL);
   }

   public boolean teleportTo(ServerLevel serverlevel, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1) {
      ChunkPos chunkpos = new ChunkPos(BlockPos.containing(d0, d1, d2));
      serverlevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, this.getId());
      this.stopRiding();
      if (this.isSleeping()) {
         this.stopSleepInBed(true, true);
      }

      if (serverlevel == this.level()) {
         this.connection.teleport(d0, d1, d2, f, f1, set);
      } else {
         this.teleportTo(serverlevel, d0, d1, d2, f, f1);
      }

      this.setYHeadRot(f);
      return true;
   }

   public void moveTo(double d0, double d1, double d2) {
      super.moveTo(d0, d1, d2);
      this.connection.resetPosition();
   }

   public void crit(Entity entity) {
      this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 4));
   }

   public void magicCrit(Entity entity) {
      this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 5));
   }

   public void onUpdateAbilities() {
      if (this.connection != null) {
         this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
         this.updateInvisibilityStatus();
      }
   }

   public ServerLevel serverLevel() {
      return (ServerLevel)this.level();
   }

   public boolean setGameMode(GameType gametype) {
      if (!this.gameMode.changeGameModeForPlayer(gametype)) {
         return false;
      } else {
         this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)gametype.getId()));
         if (gametype == GameType.SPECTATOR) {
            this.removeEntitiesOnShoulder();
            this.stopRiding();
         } else {
            this.setCamera(this);
         }

         this.onUpdateAbilities();
         this.updateEffectVisibility();
         return true;
      }
   }

   public boolean isSpectator() {
      return this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      return this.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
   }

   public void sendSystemMessage(Component component) {
      this.sendSystemMessage(component, false);
   }

   public void sendSystemMessage(Component component, boolean flag) {
      if (this.acceptsSystemMessages(flag)) {
         this.connection.send(new ClientboundSystemChatPacket(component, flag), PacketSendListener.exceptionallySend(() -> {
            if (this.acceptsSystemMessages(false)) {
               int i = 256;
               String s = component.getString(256);
               Component component2 = Component.literal(s).withStyle(ChatFormatting.YELLOW);
               return new ClientboundSystemChatPacket(Component.translatable("multiplayer.message_not_delivered", component2).withStyle(ChatFormatting.RED), false);
            } else {
               return null;
            }
         }));
      }
   }

   public void sendChatMessage(OutgoingChatMessage outgoingchatmessage, boolean flag, ChatType.Bound chattype_bound) {
      if (this.acceptsChatMessages()) {
         outgoingchatmessage.sendToPlayer(this, flag, chattype_bound);
      }

   }

   public String getIpAddress() {
      SocketAddress socketaddress = this.connection.getRemoteAddress();
      if (socketaddress instanceof InetSocketAddress inetsocketaddress) {
         return InetAddresses.toAddrString(inetsocketaddress.getAddress());
      } else {
         return "<unknown>";
      }
   }

   public void updateOptions(ServerboundClientInformationPacket serverboundclientinformationpacket) {
      this.chatVisibility = serverboundclientinformationpacket.chatVisibility();
      this.canChatColor = serverboundclientinformationpacket.chatColors();
      this.textFilteringEnabled = serverboundclientinformationpacket.textFilteringEnabled();
      this.allowsListing = serverboundclientinformationpacket.allowsListing();
      this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)serverboundclientinformationpacket.modelCustomisation());
      this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)(serverboundclientinformationpacket.mainHand() == HumanoidArm.LEFT ? 0 : 1));
   }

   public boolean canChatInColor() {
      return this.canChatColor;
   }

   public ChatVisiblity getChatVisibility() {
      return this.chatVisibility;
   }

   private boolean acceptsSystemMessages(boolean flag) {
      return this.chatVisibility == ChatVisiblity.HIDDEN ? flag : true;
   }

   private boolean acceptsChatMessages() {
      return this.chatVisibility == ChatVisiblity.FULL;
   }

   public void sendTexturePack(String s, String s1, boolean flag, @Nullable Component component) {
      this.connection.send(new ClientboundResourcePackPacket(s, s1, flag, component));
   }

   public void sendServerStatus(ServerStatus serverstatus) {
      this.connection.send(new ClientboundServerDataPacket(serverstatus.description(), serverstatus.favicon().map(ServerStatus.Favicon::iconBytes), serverstatus.enforcesSecureChat()));
   }

   protected int getPermissionLevel() {
      return this.server.getProfilePermissions(this.getGameProfile());
   }

   public void resetLastActionTime() {
      this.lastActionTime = Util.getMillis();
   }

   public ServerStatsCounter getStats() {
      return this.stats;
   }

   public ServerRecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   protected void updateInvisibilityStatus() {
      if (this.isSpectator()) {
         this.removeEffectParticles();
         this.setInvisible(true);
      } else {
         super.updateInvisibilityStatus();
      }

   }

   public Entity getCamera() {
      return (Entity)(this.camera == null ? this : this.camera);
   }

   public void setCamera(@Nullable Entity entity) {
      Entity entity1 = this.getCamera();
      this.camera = (Entity)(entity == null ? this : entity);
      if (entity1 != this.camera) {
         Level var4 = this.camera.level();
         if (var4 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)var4;
            this.teleportTo(serverlevel, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot());
         }

         if (entity != null) {
            this.serverLevel().getChunkSource().move(this);
         }

         this.connection.send(new ClientboundSetCameraPacket(this.camera));
         this.connection.resetPosition();
      }

   }

   protected void processPortalCooldown() {
      if (!this.isChangingDimension) {
         super.processPortalCooldown();
      }

   }

   public void attack(Entity entity) {
      if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
         this.setCamera(entity);
      } else {
         super.attack(entity);
      }

   }

   public long getLastActionTime() {
      return this.lastActionTime;
   }

   @Nullable
   public Component getTabListDisplayName() {
      return null;
   }

   public void swing(InteractionHand interactionhand) {
      super.swing(interactionhand);
      this.resetAttackStrengthTicker();
   }

   public boolean isChangingDimension() {
      return this.isChangingDimension;
   }

   public void hasChangedDimension() {
      this.isChangingDimension = false;
   }

   public PlayerAdvancements getAdvancements() {
      return this.advancements;
   }

   public void teleportTo(ServerLevel serverlevel, double d0, double d1, double d2, float f, float f1) {
      this.setCamera(this);
      this.stopRiding();
      if (serverlevel == this.level()) {
         this.connection.teleport(d0, d1, d2, f, f1);
      } else {
         ServerLevel serverlevel1 = this.serverLevel();
         LevelData leveldata = serverlevel.getLevelData();
         this.connection.send(new ClientboundRespawnPacket(serverlevel.dimensionTypeId(), serverlevel.dimension(), BiomeManager.obfuscateSeed(serverlevel.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), serverlevel.isDebug(), serverlevel.isFlat(), (byte)3, this.getLastDeathLocation(), this.getPortalCooldown()));
         this.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
         this.server.getPlayerList().sendPlayerPermissionLevel(this);
         serverlevel1.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
         this.unsetRemoved();
         this.moveTo(d0, d1, d2, f, f1);
         this.setServerLevel(serverlevel);
         serverlevel.addDuringCommandTeleport(this);
         this.triggerDimensionChangeTriggers(serverlevel1);
         this.connection.teleport(d0, d1, d2, f, f1);
         this.server.getPlayerList().sendLevelInfo(this, serverlevel);
         this.server.getPlayerList().sendAllPlayerInfo(this);
      }

   }

   @Nullable
   public BlockPos getRespawnPosition() {
      return this.respawnPosition;
   }

   public float getRespawnAngle() {
      return this.respawnAngle;
   }

   public ResourceKey<Level> getRespawnDimension() {
      return this.respawnDimension;
   }

   public boolean isRespawnForced() {
      return this.respawnForced;
   }

   public void setRespawnPosition(ResourceKey<Level> resourcekey, @Nullable BlockPos blockpos, float f, boolean flag, boolean flag1) {
      if (blockpos != null) {
         boolean flag2 = blockpos.equals(this.respawnPosition) && resourcekey.equals(this.respawnDimension);
         if (flag1 && !flag2) {
            this.sendSystemMessage(Component.translatable("block.minecraft.set_spawn"));
         }

         this.respawnPosition = blockpos;
         this.respawnDimension = resourcekey;
         this.respawnAngle = f;
         this.respawnForced = flag;
      } else {
         this.respawnPosition = null;
         this.respawnDimension = Level.OVERWORLD;
         this.respawnAngle = 0.0F;
         this.respawnForced = false;
      }

   }

   public void trackChunk(ChunkPos chunkpos, Packet<?> packet) {
      this.connection.send(packet);
   }

   public void untrackChunk(ChunkPos chunkpos) {
      if (this.isAlive()) {
         this.connection.send(new ClientboundForgetLevelChunkPacket(chunkpos.x, chunkpos.z));
      }

   }

   public SectionPos getLastSectionPos() {
      return this.lastSectionPos;
   }

   public void setLastSectionPos(SectionPos sectionpos) {
      this.lastSectionPos = sectionpos;
   }

   public void playNotifySound(SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      this.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundevent), soundsource, this.getX(), this.getY(), this.getZ(), f, f1, this.random.nextLong()));
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddPlayerPacket(this);
   }

   public ItemEntity drop(ItemStack itemstack, boolean flag, boolean flag1) {
      ItemEntity itementity = super.drop(itemstack, flag, flag1);
      if (itementity == null) {
         return null;
      } else {
         this.level().addFreshEntity(itementity);
         ItemStack itemstack1 = itementity.getItem();
         if (flag1) {
            if (!itemstack1.isEmpty()) {
               this.awardStat(Stats.ITEM_DROPPED.get(itemstack1.getItem()), itemstack.getCount());
            }

            this.awardStat(Stats.DROP);
         }

         return itementity;
      }
   }

   public TextFilter getTextFilter() {
      return this.textFilter;
   }

   public void setServerLevel(ServerLevel serverlevel) {
      this.setLevel(serverlevel);
      this.gameMode.setLevel(serverlevel);
   }

   @Nullable
   private static GameType readPlayerMode(@Nullable CompoundTag compoundtag, String s) {
      return compoundtag != null && compoundtag.contains(s, 99) ? GameType.byId(compoundtag.getInt(s)) : null;
   }

   private GameType calculateGameModeForNewPlayer(@Nullable GameType gametype) {
      GameType gametype1 = this.server.getForcedGameType();
      if (gametype1 != null) {
         return gametype1;
      } else {
         return gametype != null ? gametype : this.server.getDefaultGameType();
      }
   }

   public void loadGameTypes(@Nullable CompoundTag compoundtag) {
      this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(readPlayerMode(compoundtag, "playerGameType")), readPlayerMode(compoundtag, "previousPlayerGameType"));
   }

   private void storeGameTypes(CompoundTag compoundtag) {
      compoundtag.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
      GameType gametype = this.gameMode.getPreviousGameModeForPlayer();
      if (gametype != null) {
         compoundtag.putInt("previousPlayerGameType", gametype.getId());
      }

   }

   public boolean isTextFilteringEnabled() {
      return this.textFilteringEnabled;
   }

   public boolean shouldFilterMessageTo(ServerPlayer serverplayer) {
      if (serverplayer == this) {
         return false;
      } else {
         return this.textFilteringEnabled || serverplayer.textFilteringEnabled;
      }
   }

   public boolean mayInteract(Level level, BlockPos blockpos) {
      return super.mayInteract(level, blockpos) && level.mayInteract(this, blockpos);
   }

   protected void updateUsingItem(ItemStack itemstack) {
      CriteriaTriggers.USING_ITEM.trigger(this, itemstack);
      super.updateUsingItem(itemstack);
   }

   public boolean drop(boolean flag) {
      Inventory inventory = this.getInventory();
      ItemStack itemstack = inventory.removeFromSelected(flag);
      this.containerMenu.findSlot(inventory, inventory.selected).ifPresent((i) -> this.containerMenu.setRemoteSlot(i, inventory.getSelected()));
      return this.drop(itemstack, false, true) != null;
   }

   public boolean allowsListing() {
      return this.allowsListing;
   }

   public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
      return Optional.of(this.wardenSpawnTracker);
   }

   public void onItemPickup(ItemEntity itementity) {
      super.onItemPickup(itementity);
      Entity entity = itementity.getOwner();
      if (entity != null) {
         CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, itementity.getItem(), entity);
      }

   }

   public void setChatSession(RemoteChatSession remotechatsession) {
      this.chatSession = remotechatsession;
   }

   @Nullable
   public RemoteChatSession getChatSession() {
      return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
   }

   public void indicateDamage(double d0, double d1) {
      this.hurtDir = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI) - (double)this.getYRot());
      this.connection.send(new ClientboundHurtAnimationPacket(this));
   }

   public boolean startRiding(Entity entity, boolean flag) {
      if (!super.startRiding(entity, flag)) {
         return false;
      } else {
         entity.positionRider(this);
         this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;

            for(MobEffectInstance mobeffectinstance : livingentity.getActiveEffects()) {
               this.connection.send(new ClientboundUpdateMobEffectPacket(entity.getId(), mobeffectinstance));
            }
         }

         return true;
      }
   }

   public void stopRiding() {
      Entity entity = this.getVehicle();
      super.stopRiding();
      if (entity instanceof LivingEntity livingentity) {
         for(MobEffectInstance mobeffectinstance : livingentity.getActiveEffects()) {
            this.connection.send(new ClientboundRemoveMobEffectPacket(entity.getId(), mobeffectinstance.getEffect()));
         }
      }

   }
}
