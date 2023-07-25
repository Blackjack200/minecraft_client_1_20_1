package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Raid {
   private static final int SECTION_RADIUS_FOR_FINDING_NEW_VILLAGE_CENTER = 2;
   private static final int ATTEMPT_RAID_FARTHEST = 0;
   private static final int ATTEMPT_RAID_CLOSE = 1;
   private static final int ATTEMPT_RAID_INSIDE = 2;
   private static final int VILLAGE_SEARCH_RADIUS = 32;
   private static final int RAID_TIMEOUT_TICKS = 48000;
   private static final int NUM_SPAWN_ATTEMPTS = 3;
   private static final String OMINOUS_BANNER_PATTERN_NAME = "block.minecraft.ominous_banner";
   private static final String RAIDERS_REMAINING = "event.minecraft.raid.raiders_remaining";
   public static final int VILLAGE_RADIUS_BUFFER = 16;
   private static final int POST_RAID_TICK_LIMIT = 40;
   private static final int DEFAULT_PRE_RAID_TICKS = 300;
   public static final int MAX_NO_ACTION_TIME = 2400;
   public static final int MAX_CELEBRATION_TICKS = 600;
   private static final int OUTSIDE_RAID_BOUNDS_TIMEOUT = 30;
   public static final int TICKS_PER_DAY = 24000;
   public static final int DEFAULT_MAX_BAD_OMEN_LEVEL = 5;
   private static final int LOW_MOB_THRESHOLD = 2;
   private static final Component RAID_NAME_COMPONENT = Component.translatable("event.minecraft.raid");
   private static final Component VICTORY = Component.translatable("event.minecraft.raid.victory");
   private static final Component DEFEAT = Component.translatable("event.minecraft.raid.defeat");
   private static final Component RAID_BAR_VICTORY_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(VICTORY);
   private static final Component RAID_BAR_DEFEAT_COMPONENT = RAID_NAME_COMPONENT.copy().append(" - ").append(DEFEAT);
   private static final int HERO_OF_THE_VILLAGE_DURATION = 48000;
   public static final int VALID_RAID_RADIUS_SQR = 9216;
   public static final int RAID_REMOVAL_THRESHOLD_SQR = 12544;
   private final Map<Integer, Raider> groupToLeaderMap = Maps.newHashMap();
   private final Map<Integer, Set<Raider>> groupRaiderMap = Maps.newHashMap();
   private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
   private long ticksActive;
   private BlockPos center;
   private final ServerLevel level;
   private boolean started;
   private final int id;
   private float totalHealth;
   private int badOmenLevel;
   private boolean active;
   private int groupsSpawned;
   private final ServerBossEvent raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
   private int postRaidTicks;
   private int raidCooldownTicks;
   private final RandomSource random = RandomSource.create();
   private final int numGroups;
   private Raid.RaidStatus status;
   private int celebrationTicks;
   private Optional<BlockPos> waveSpawnPos = Optional.empty();

   public Raid(int i, ServerLevel serverlevel, BlockPos blockpos) {
      this.id = i;
      this.level = serverlevel;
      this.active = true;
      this.raidCooldownTicks = 300;
      this.raidEvent.setProgress(0.0F);
      this.center = blockpos;
      this.numGroups = this.getNumGroups(serverlevel.getDifficulty());
      this.status = Raid.RaidStatus.ONGOING;
   }

   public Raid(ServerLevel serverlevel, CompoundTag compoundtag) {
      this.level = serverlevel;
      this.id = compoundtag.getInt("Id");
      this.started = compoundtag.getBoolean("Started");
      this.active = compoundtag.getBoolean("Active");
      this.ticksActive = compoundtag.getLong("TicksActive");
      this.badOmenLevel = compoundtag.getInt("BadOmenLevel");
      this.groupsSpawned = compoundtag.getInt("GroupsSpawned");
      this.raidCooldownTicks = compoundtag.getInt("PreRaidTicks");
      this.postRaidTicks = compoundtag.getInt("PostRaidTicks");
      this.totalHealth = compoundtag.getFloat("TotalHealth");
      this.center = new BlockPos(compoundtag.getInt("CX"), compoundtag.getInt("CY"), compoundtag.getInt("CZ"));
      this.numGroups = compoundtag.getInt("NumGroups");
      this.status = Raid.RaidStatus.getByName(compoundtag.getString("Status"));
      this.heroesOfTheVillage.clear();
      if (compoundtag.contains("HeroesOfTheVillage", 9)) {
         ListTag listtag = compoundtag.getList("HeroesOfTheVillage", 11);

         for(int i = 0; i < listtag.size(); ++i) {
            this.heroesOfTheVillage.add(NbtUtils.loadUUID(listtag.get(i)));
         }
      }

   }

   public boolean isOver() {
      return this.isVictory() || this.isLoss();
   }

   public boolean isBetweenWaves() {
      return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
   }

   public boolean hasFirstWaveSpawned() {
      return this.groupsSpawned > 0;
   }

   public boolean isStopped() {
      return this.status == Raid.RaidStatus.STOPPED;
   }

   public boolean isVictory() {
      return this.status == Raid.RaidStatus.VICTORY;
   }

   public boolean isLoss() {
      return this.status == Raid.RaidStatus.LOSS;
   }

   public float getTotalHealth() {
      return this.totalHealth;
   }

   public Set<Raider> getAllRaiders() {
      Set<Raider> set = Sets.newHashSet();

      for(Set<Raider> set1 : this.groupRaiderMap.values()) {
         set.addAll(set1);
      }

      return set;
   }

   public Level getLevel() {
      return this.level;
   }

   public boolean isStarted() {
      return this.started;
   }

   public int getGroupsSpawned() {
      return this.groupsSpawned;
   }

   private Predicate<ServerPlayer> validPlayer() {
      return (serverplayer) -> {
         BlockPos blockpos = serverplayer.blockPosition();
         return serverplayer.isAlive() && this.level.getRaidAt(blockpos) == this;
      };
   }

   private void updatePlayers() {
      Set<ServerPlayer> set = Sets.newHashSet(this.raidEvent.getPlayers());
      List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());

      for(ServerPlayer serverplayer : list) {
         if (!set.contains(serverplayer)) {
            this.raidEvent.addPlayer(serverplayer);
         }
      }

      for(ServerPlayer serverplayer1 : set) {
         if (!list.contains(serverplayer1)) {
            this.raidEvent.removePlayer(serverplayer1);
         }
      }

   }

   public int getMaxBadOmenLevel() {
      return 5;
   }

   public int getBadOmenLevel() {
      return this.badOmenLevel;
   }

   public void setBadOmenLevel(int i) {
      this.badOmenLevel = i;
   }

   public void absorbBadOmen(Player player) {
      if (player.hasEffect(MobEffects.BAD_OMEN)) {
         this.badOmenLevel += player.getEffect(MobEffects.BAD_OMEN).getAmplifier() + 1;
         this.badOmenLevel = Mth.clamp(this.badOmenLevel, 0, this.getMaxBadOmenLevel());
      }

      player.removeEffect(MobEffects.BAD_OMEN);
   }

   public void stop() {
      this.active = false;
      this.raidEvent.removeAllPlayers();
      this.status = Raid.RaidStatus.STOPPED;
   }

   public void tick() {
      if (!this.isStopped()) {
         if (this.status == Raid.RaidStatus.ONGOING) {
            boolean flag = this.active;
            this.active = this.level.hasChunkAt(this.center);
            if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
               this.stop();
               return;
            }

            if (flag != this.active) {
               this.raidEvent.setVisible(this.active);
            }

            if (!this.active) {
               return;
            }

            if (!this.level.isVillage(this.center)) {
               this.moveRaidCenterToNearbyVillageSection();
            }

            if (!this.level.isVillage(this.center)) {
               if (this.groupsSpawned > 0) {
                  this.status = Raid.RaidStatus.LOSS;
               } else {
                  this.stop();
               }
            }

            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
               this.stop();
               return;
            }

            int i = this.getTotalRaidersAlive();
            if (i == 0 && this.hasMoreWaves()) {
               if (this.raidCooldownTicks <= 0) {
                  if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                     this.raidCooldownTicks = 300;
                     this.raidEvent.setName(RAID_NAME_COMPONENT);
                     return;
                  }
               } else {
                  boolean flag1 = this.waveSpawnPos.isPresent();
                  boolean flag2 = !flag1 && this.raidCooldownTicks % 5 == 0;
                  if (flag1 && !this.level.isPositionEntityTicking(this.waveSpawnPos.get())) {
                     flag2 = true;
                  }

                  if (flag2) {
                     int j = 0;
                     if (this.raidCooldownTicks < 100) {
                        j = 1;
                     } else if (this.raidCooldownTicks < 40) {
                        j = 2;
                     }

                     this.waveSpawnPos = this.getValidSpawnPos(j);
                  }

                  if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                     this.updatePlayers();
                  }

                  --this.raidCooldownTicks;
                  this.raidEvent.setProgress(Mth.clamp((float)(300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
               }
            }

            if (this.ticksActive % 20L == 0L) {
               this.updatePlayers();
               this.updateRaiders();
               if (i > 0) {
                  if (i <= 2) {
                     this.raidEvent.setName(RAID_NAME_COMPONENT.copy().append(" - ").append(Component.translatable("event.minecraft.raid.raiders_remaining", i)));
                  } else {
                     this.raidEvent.setName(RAID_NAME_COMPONENT);
                  }
               } else {
                  this.raidEvent.setName(RAID_NAME_COMPONENT);
               }
            }

            boolean flag3 = false;
            int k = 0;

            while(this.shouldSpawnGroup()) {
               BlockPos blockpos = this.waveSpawnPos.isPresent() ? this.waveSpawnPos.get() : this.findRandomSpawnPos(k, 20);
               if (blockpos != null) {
                  this.started = true;
                  this.spawnGroup(blockpos);
                  if (!flag3) {
                     this.playSound(blockpos);
                     flag3 = true;
                  }
               } else {
                  ++k;
               }

               if (k > 3) {
                  this.stop();
                  break;
               }
            }

            if (this.isStarted() && !this.hasMoreWaves() && i == 0) {
               if (this.postRaidTicks < 40) {
                  ++this.postRaidTicks;
               } else {
                  this.status = Raid.RaidStatus.VICTORY;

                  for(UUID uuid : this.heroesOfTheVillage) {
                     Entity entity = this.level.getEntity(uuid);
                     if (entity instanceof LivingEntity && !entity.isSpectator()) {
                        LivingEntity livingentity = (LivingEntity)entity;
                        livingentity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (livingentity instanceof ServerPlayer) {
                           ServerPlayer serverplayer = (ServerPlayer)livingentity;
                           serverplayer.awardStat(Stats.RAID_WIN);
                           CriteriaTriggers.RAID_WIN.trigger(serverplayer);
                        }
                     }
                  }
               }
            }

            this.setDirty();
         } else if (this.isOver()) {
            ++this.celebrationTicks;
            if (this.celebrationTicks >= 600) {
               this.stop();
               return;
            }

            if (this.celebrationTicks % 20 == 0) {
               this.updatePlayers();
               this.raidEvent.setVisible(true);
               if (this.isVictory()) {
                  this.raidEvent.setProgress(0.0F);
                  this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
               } else {
                  this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
               }
            }
         }

      }
   }

   private void moveRaidCenterToNearbyVillageSection() {
      Stream<SectionPos> stream = SectionPos.cube(SectionPos.of(this.center), 2);
      stream.filter(this.level::isVillage).map(SectionPos::center).min(Comparator.comparingDouble((blockpos1) -> blockpos1.distSqr(this.center))).ifPresent(this::setCenter);
   }

   private Optional<BlockPos> getValidSpawnPos(int i) {
      for(int j = 0; j < 3; ++j) {
         BlockPos blockpos = this.findRandomSpawnPos(i, 1);
         if (blockpos != null) {
            return Optional.of(blockpos);
         }
      }

      return Optional.empty();
   }

   private boolean hasMoreWaves() {
      if (this.hasBonusWave()) {
         return !this.hasSpawnedBonusWave();
      } else {
         return !this.isFinalWave();
      }
   }

   private boolean isFinalWave() {
      return this.getGroupsSpawned() == this.numGroups;
   }

   private boolean hasBonusWave() {
      return this.badOmenLevel > 1;
   }

   private boolean hasSpawnedBonusWave() {
      return this.getGroupsSpawned() > this.numGroups;
   }

   private boolean shouldSpawnBonusGroup() {
      return this.isFinalWave() && this.getTotalRaidersAlive() == 0 && this.hasBonusWave();
   }

   private void updateRaiders() {
      Iterator<Set<Raider>> iterator = this.groupRaiderMap.values().iterator();
      Set<Raider> set = Sets.newHashSet();

      while(iterator.hasNext()) {
         Set<Raider> set1 = iterator.next();

         for(Raider raider : set1) {
            BlockPos blockpos = raider.blockPosition();
            if (!raider.isRemoved() && raider.level().dimension() == this.level.dimension() && !(this.center.distSqr(blockpos) >= 12544.0D)) {
               if (raider.tickCount > 600) {
                  if (this.level.getEntity(raider.getUUID()) == null) {
                     set.add(raider);
                  }

                  if (!this.level.isVillage(blockpos) && raider.getNoActionTime() > 2400) {
                     raider.setTicksOutsideRaid(raider.getTicksOutsideRaid() + 1);
                  }

                  if (raider.getTicksOutsideRaid() >= 30) {
                     set.add(raider);
                  }
               }
            } else {
               set.add(raider);
            }
         }
      }

      for(Raider raider1 : set) {
         this.removeFromRaid(raider1, true);
      }

   }

   private void playSound(BlockPos blockpos) {
      float f = 13.0F;
      int i = 64;
      Collection<ServerPlayer> collection = this.raidEvent.getPlayers();
      long j = this.random.nextLong();

      for(ServerPlayer serverplayer : this.level.players()) {
         Vec3 vec3 = serverplayer.position();
         Vec3 vec31 = Vec3.atCenterOf(blockpos);
         double d0 = Math.sqrt((vec31.x - vec3.x) * (vec31.x - vec3.x) + (vec31.z - vec3.z) * (vec31.z - vec3.z));
         double d1 = vec3.x + 13.0D / d0 * (vec31.x - vec3.x);
         double d2 = vec3.z + 13.0D / d0 * (vec31.z - vec3.z);
         if (d0 <= 64.0D || collection.contains(serverplayer)) {
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, d1, serverplayer.getY(), d2, 64.0F, 1.0F, j));
         }
      }

   }

   private void spawnGroup(BlockPos blockpos) {
      boolean flag = false;
      int i = this.groupsSpawned + 1;
      this.totalHealth = 0.0F;
      DifficultyInstance difficultyinstance = this.level.getCurrentDifficultyAt(blockpos);
      boolean flag1 = this.shouldSpawnBonusGroup();

      for(Raid.RaiderType raid_raidertype : Raid.RaiderType.VALUES) {
         int j = this.getDefaultNumSpawns(raid_raidertype, i, flag1) + this.getPotentialBonusSpawns(raid_raidertype, this.random, i, difficultyinstance, flag1);
         int k = 0;

         for(int l = 0; l < j; ++l) {
            Raider raider = raid_raidertype.entityType.create(this.level);
            if (raider == null) {
               break;
            }

            if (!flag && raider.canBeLeader()) {
               raider.setPatrolLeader(true);
               this.setLeader(i, raider);
               flag = true;
            }

            this.joinRaid(i, raider, blockpos, false);
            if (raid_raidertype.entityType == EntityType.RAVAGER) {
               Raider raider1 = null;
               if (i == this.getNumGroups(Difficulty.NORMAL)) {
                  raider1 = EntityType.PILLAGER.create(this.level);
               } else if (i >= this.getNumGroups(Difficulty.HARD)) {
                  if (k == 0) {
                     raider1 = EntityType.EVOKER.create(this.level);
                  } else {
                     raider1 = EntityType.VINDICATOR.create(this.level);
                  }
               }

               ++k;
               if (raider1 != null) {
                  this.joinRaid(i, raider1, blockpos, false);
                  raider1.moveTo(blockpos, 0.0F, 0.0F);
                  raider1.startRiding(raider);
               }
            }
         }
      }

      this.waveSpawnPos = Optional.empty();
      ++this.groupsSpawned;
      this.updateBossbar();
      this.setDirty();
   }

   public void joinRaid(int i, Raider raider, @Nullable BlockPos blockpos, boolean flag) {
      boolean flag1 = this.addWaveMob(i, raider);
      if (flag1) {
         raider.setCurrentRaid(this);
         raider.setWave(i);
         raider.setCanJoinRaid(true);
         raider.setTicksOutsideRaid(0);
         if (!flag && blockpos != null) {
            raider.setPos((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 1.0D, (double)blockpos.getZ() + 0.5D);
            raider.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(blockpos), MobSpawnType.EVENT, (SpawnGroupData)null, (CompoundTag)null);
            raider.applyRaidBuffs(i, false);
            raider.setOnGround(true);
            this.level.addFreshEntityWithPassengers(raider);
         }
      }

   }

   public void updateBossbar() {
      this.raidEvent.setProgress(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0F, 1.0F));
   }

   public float getHealthOfLivingRaiders() {
      float f = 0.0F;

      for(Set<Raider> set : this.groupRaiderMap.values()) {
         for(Raider raider : set) {
            f += raider.getHealth();
         }
      }

      return f;
   }

   private boolean shouldSpawnGroup() {
      return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getTotalRaidersAlive() == 0;
   }

   public int getTotalRaidersAlive() {
      return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
   }

   public void removeFromRaid(Raider raider, boolean flag) {
      Set<Raider> set = this.groupRaiderMap.get(raider.getWave());
      if (set != null) {
         boolean flag1 = set.remove(raider);
         if (flag1) {
            if (flag) {
               this.totalHealth -= raider.getHealth();
            }

            raider.setCurrentRaid((Raid)null);
            this.updateBossbar();
            this.setDirty();
         }
      }

   }

   private void setDirty() {
      this.level.getRaids().setDirty();
   }

   public static ItemStack getLeaderBannerInstance() {
      ItemStack itemstack = new ItemStack(Items.WHITE_BANNER);
      CompoundTag compoundtag = new CompoundTag();
      ListTag listtag = (new BannerPattern.Builder()).addPattern(BannerPatterns.RHOMBUS_MIDDLE, DyeColor.CYAN).addPattern(BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).addPattern(BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).addPattern(BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).addPattern(BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).addPattern(BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).addPattern(BannerPatterns.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY).addPattern(BannerPatterns.BORDER, DyeColor.BLACK).toListTag();
      compoundtag.put("Patterns", listtag);
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
      itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
      itemstack.setHoverName(Component.translatable("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD));
      return itemstack;
   }

   @Nullable
   public Raider getLeader(int i) {
      return this.groupToLeaderMap.get(i);
   }

   @Nullable
   private BlockPos findRandomSpawnPos(int i, int j) {
      int k = i == 0 ? 2 : 2 - i;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = 0; l < j; ++l) {
         float f = this.level.random.nextFloat() * ((float)Math.PI * 2F);
         int i1 = this.center.getX() + Mth.floor(Mth.cos(f) * 32.0F * (float)k) + this.level.random.nextInt(5);
         int j1 = this.center.getZ() + Mth.floor(Mth.sin(f) * 32.0F * (float)k) + this.level.random.nextInt(5);
         int k1 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, i1, j1);
         blockpos_mutableblockpos.set(i1, k1, j1);
         if (!this.level.isVillage(blockpos_mutableblockpos) || i >= 2) {
            int l1 = 10;
            if (this.level.hasChunksAt(blockpos_mutableblockpos.getX() - 10, blockpos_mutableblockpos.getZ() - 10, blockpos_mutableblockpos.getX() + 10, blockpos_mutableblockpos.getZ() + 10) && this.level.isPositionEntityTicking(blockpos_mutableblockpos) && (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, blockpos_mutableblockpos, EntityType.RAVAGER) || this.level.getBlockState(blockpos_mutableblockpos.below()).is(Blocks.SNOW) && this.level.getBlockState(blockpos_mutableblockpos).isAir())) {
               return blockpos_mutableblockpos;
            }
         }
      }

      return null;
   }

   private boolean addWaveMob(int i, Raider raider) {
      return this.addWaveMob(i, raider, true);
   }

   public boolean addWaveMob(int i, Raider raider, boolean flag) {
      this.groupRaiderMap.computeIfAbsent(i, (integer) -> Sets.newHashSet());
      Set<Raider> set = this.groupRaiderMap.get(i);
      Raider raider1 = null;

      for(Raider raider2 : set) {
         if (raider2.getUUID().equals(raider.getUUID())) {
            raider1 = raider2;
            break;
         }
      }

      if (raider1 != null) {
         set.remove(raider1);
         set.add(raider);
      }

      set.add(raider);
      if (flag) {
         this.totalHealth += raider.getHealth();
      }

      this.updateBossbar();
      this.setDirty();
      return true;
   }

   public void setLeader(int i, Raider raider) {
      this.groupToLeaderMap.put(i, raider);
      raider.setItemSlot(EquipmentSlot.HEAD, getLeaderBannerInstance());
      raider.setDropChance(EquipmentSlot.HEAD, 2.0F);
   }

   public void removeLeader(int i) {
      this.groupToLeaderMap.remove(i);
   }

   public BlockPos getCenter() {
      return this.center;
   }

   private void setCenter(BlockPos blockpos) {
      this.center = blockpos;
   }

   public int getId() {
      return this.id;
   }

   private int getDefaultNumSpawns(Raid.RaiderType raid_raidertype, int i, boolean flag) {
      return flag ? raid_raidertype.spawnsPerWaveBeforeBonus[this.numGroups] : raid_raidertype.spawnsPerWaveBeforeBonus[i];
   }

   private int getPotentialBonusSpawns(Raid.RaiderType raid_raidertype, RandomSource randomsource, int i, DifficultyInstance difficultyinstance, boolean flag) {
      Difficulty difficulty = difficultyinstance.getDifficulty();
      boolean flag1 = difficulty == Difficulty.EASY;
      boolean flag2 = difficulty == Difficulty.NORMAL;
      int k;
      switch (raid_raidertype) {
         case WITCH:
            if (flag1 || i <= 2 || i == 4) {
               return 0;
            }

            k = 1;
            break;
         case PILLAGER:
         case VINDICATOR:
            if (flag1) {
               k = randomsource.nextInt(2);
            } else if (flag2) {
               k = 1;
            } else {
               k = 2;
            }
            break;
         case RAVAGER:
            k = !flag1 && flag ? 1 : 0;
            break;
         default:
            return 0;
      }

      return k > 0 ? randomsource.nextInt(k + 1) : 0;
   }

   public boolean isActive() {
      return this.active;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putInt("Id", this.id);
      compoundtag.putBoolean("Started", this.started);
      compoundtag.putBoolean("Active", this.active);
      compoundtag.putLong("TicksActive", this.ticksActive);
      compoundtag.putInt("BadOmenLevel", this.badOmenLevel);
      compoundtag.putInt("GroupsSpawned", this.groupsSpawned);
      compoundtag.putInt("PreRaidTicks", this.raidCooldownTicks);
      compoundtag.putInt("PostRaidTicks", this.postRaidTicks);
      compoundtag.putFloat("TotalHealth", this.totalHealth);
      compoundtag.putInt("NumGroups", this.numGroups);
      compoundtag.putString("Status", this.status.getName());
      compoundtag.putInt("CX", this.center.getX());
      compoundtag.putInt("CY", this.center.getY());
      compoundtag.putInt("CZ", this.center.getZ());
      ListTag listtag = new ListTag();

      for(UUID uuid : this.heroesOfTheVillage) {
         listtag.add(NbtUtils.createUUID(uuid));
      }

      compoundtag.put("HeroesOfTheVillage", listtag);
      return compoundtag;
   }

   public int getNumGroups(Difficulty difficulty) {
      switch (difficulty) {
         case EASY:
            return 3;
         case NORMAL:
            return 5;
         case HARD:
            return 7;
         default:
            return 0;
      }
   }

   public float getEnchantOdds() {
      int i = this.getBadOmenLevel();
      if (i == 2) {
         return 0.1F;
      } else if (i == 3) {
         return 0.25F;
      } else if (i == 4) {
         return 0.5F;
      } else {
         return i == 5 ? 0.75F : 0.0F;
      }
   }

   public void addHeroOfTheVillage(Entity entity) {
      this.heroesOfTheVillage.add(entity.getUUID());
   }

   static enum RaidStatus {
      ONGOING,
      VICTORY,
      LOSS,
      STOPPED;

      private static final Raid.RaidStatus[] VALUES = values();

      static Raid.RaidStatus getByName(String s) {
         for(Raid.RaidStatus raid_raidstatus : VALUES) {
            if (s.equalsIgnoreCase(raid_raidstatus.name())) {
               return raid_raidstatus;
            }
         }

         return ONGOING;
      }

      public String getName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }

   static enum RaiderType {
      VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
      EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
      PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
      WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
      RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

      static final Raid.RaiderType[] VALUES = values();
      final EntityType<? extends Raider> entityType;
      final int[] spawnsPerWaveBeforeBonus;

      private RaiderType(EntityType<? extends Raider> entitytype, int[] aint) {
         this.entityType = entitytype;
         this.spawnsPerWaveBeforeBonus = aint;
      }
   }
}
