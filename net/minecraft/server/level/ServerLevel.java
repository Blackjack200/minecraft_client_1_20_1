package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTicks;
import org.slf4j.Logger;

public class ServerLevel extends Level implements WorldGenLevel {
   public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
   public static final IntProvider RAIN_DELAY = UniformInt.of(12000, 180000);
   public static final IntProvider RAIN_DURATION = UniformInt.of(12000, 24000);
   private static final IntProvider THUNDER_DELAY = UniformInt.of(12000, 180000);
   public static final IntProvider THUNDER_DURATION = UniformInt.of(3600, 15600);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int EMPTY_TIME_NO_TICK = 300;
   private static final int MAX_SCHEDULED_TICKS_PER_TICK = 65536;
   final List<ServerPlayer> players = Lists.newArrayList();
   private final ServerChunkCache chunkSource;
   private final MinecraftServer server;
   private final ServerLevelData serverLevelData;
   final EntityTickList entityTickList = new EntityTickList();
   private final PersistentEntitySectionManager<Entity> entityManager;
   private final GameEventDispatcher gameEventDispatcher;
   public boolean noSave;
   private final SleepStatus sleepStatus;
   private int emptyTime;
   private final PortalForcer portalForcer;
   private final LevelTicks<Block> blockTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
   private final LevelTicks<Fluid> fluidTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
   final Set<Mob> navigatingMobs = new ObjectOpenHashSet<>();
   volatile boolean isUpdatingNavigations;
   protected final Raids raids;
   private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
   private final List<BlockEventData> blockEventsToReschedule = new ArrayList<>(64);
   private boolean handlingTick;
   private final List<CustomSpawner> customSpawners;
   @Nullable
   private EndDragonFight dragonFight;
   final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<>();
   private final StructureManager structureManager;
   private final StructureCheck structureCheck;
   private final boolean tickTime;
   private final RandomSequences randomSequences;

   public ServerLevel(MinecraftServer minecraftserver, Executor executor, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, ServerLevelData serverleveldata, ResourceKey<Level> resourcekey, LevelStem levelstem, ChunkProgressListener chunkprogresslistener, boolean flag, long i, List<CustomSpawner> list, boolean flag1, @Nullable RandomSequences randomsequences) {
      super(serverleveldata, resourcekey, minecraftserver.registryAccess(), levelstem.type(), minecraftserver::getProfiler, false, flag, i, minecraftserver.getMaxChainedNeighborUpdates());
      this.tickTime = flag1;
      this.server = minecraftserver;
      this.customSpawners = list;
      this.serverLevelData = serverleveldata;
      ChunkGenerator chunkgenerator = levelstem.generator();
      boolean flag2 = minecraftserver.forceSynchronousWrites();
      DataFixer datafixer = minecraftserver.getFixerUpper();
      EntityPersistentStorage<Entity> entitypersistentstorage = new EntityStorage(this, levelstoragesource_levelstorageaccess.getDimensionPath(resourcekey).resolve("entities"), datafixer, flag2, minecraftserver);
      this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new ServerLevel.EntityCallbacks(), entitypersistentstorage);
      this.chunkSource = new ServerChunkCache(this, levelstoragesource_levelstorageaccess, datafixer, minecraftserver.getStructureManager(), executor, chunkgenerator, minecraftserver.getPlayerList().getViewDistance(), minecraftserver.getPlayerList().getSimulationDistance(), flag2, chunkprogresslistener, this.entityManager::updateChunkStatus, () -> minecraftserver.overworld().getDataStorage());
      this.chunkSource.getGeneratorState().ensureStructuresGenerated();
      this.portalForcer = new PortalForcer(this);
      this.updateSkyBrightness();
      this.prepareWeather();
      this.getWorldBorder().setAbsoluteMaxSize(minecraftserver.getAbsoluteMaxWorldSize());
      this.raids = this.getDataStorage().computeIfAbsent((compoundtag1) -> Raids.load(this, compoundtag1), () -> new Raids(this), Raids.getFileId(this.dimensionTypeRegistration()));
      if (!minecraftserver.isSingleplayer()) {
         serverleveldata.setGameType(minecraftserver.getDefaultGameType());
      }

      long j = minecraftserver.getWorldData().worldGenOptions().seed();
      this.structureCheck = new StructureCheck(this.chunkSource.chunkScanner(), this.registryAccess(), minecraftserver.getStructureManager(), resourcekey, chunkgenerator, this.chunkSource.randomState(), this, chunkgenerator.getBiomeSource(), j, datafixer);
      this.structureManager = new StructureManager(this, minecraftserver.getWorldData().worldGenOptions(), this.structureCheck);
      if (this.dimension() == Level.END && this.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
         this.dragonFight = new EndDragonFight(this, j, minecraftserver.getWorldData().endDragonFightData());
      } else {
         this.dragonFight = null;
      }

      this.sleepStatus = new SleepStatus();
      this.gameEventDispatcher = new GameEventDispatcher(this);
      this.randomSequences = Objects.requireNonNullElseGet(randomsequences, () -> this.getDataStorage().computeIfAbsent((compoundtag) -> RandomSequences.load(j, compoundtag), () -> new RandomSequences(j), "random_sequences"));
   }

   /** @deprecated */
   @Deprecated
   @VisibleForTesting
   public void setDragonFight(@Nullable EndDragonFight enddragonfight) {
      this.dragonFight = enddragonfight;
   }

   public void setWeatherParameters(int i, int j, boolean flag, boolean flag1) {
      this.serverLevelData.setClearWeatherTime(i);
      this.serverLevelData.setRainTime(j);
      this.serverLevelData.setThunderTime(j);
      this.serverLevelData.setRaining(flag);
      this.serverLevelData.setThundering(flag1);
   }

   public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
      return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(i, j, k, this.getChunkSource().randomState().sampler());
   }

   public StructureManager structureManager() {
      return this.structureManager;
   }

   public void tick(BooleanSupplier booleansupplier) {
      ProfilerFiller profilerfiller = this.getProfiler();
      this.handlingTick = true;
      profilerfiller.push("world border");
      this.getWorldBorder().tick();
      profilerfiller.popPush("weather");
      this.advanceWeatherCycle();
      int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
      if (this.sleepStatus.areEnoughSleeping(i) && this.sleepStatus.areEnoughDeepSleeping(i, this.players)) {
         if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long j = this.levelData.getDayTime() + 24000L;
            this.setDayTime(j - j % 24000L);
         }

         this.wakeUpAllPlayers();
         if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && this.isRaining()) {
            this.resetWeatherCycle();
         }
      }

      this.updateSkyBrightness();
      this.tickTime();
      profilerfiller.popPush("tickPending");
      if (!this.isDebug()) {
         long k = this.getGameTime();
         profilerfiller.push("blockTicks");
         this.blockTicks.tick(k, 65536, this::tickBlock);
         profilerfiller.popPush("fluidTicks");
         this.fluidTicks.tick(k, 65536, this::tickFluid);
         profilerfiller.pop();
      }

      profilerfiller.popPush("raid");
      this.raids.tick();
      profilerfiller.popPush("chunkSource");
      this.getChunkSource().tick(booleansupplier, true);
      profilerfiller.popPush("blockEvents");
      this.runBlockEvents();
      this.handlingTick = false;
      profilerfiller.pop();
      boolean flag = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
      if (flag) {
         this.resetEmptyTime();
      }

      if (flag || this.emptyTime++ < 300) {
         profilerfiller.push("entities");
         if (this.dragonFight != null) {
            profilerfiller.push("dragonFight");
            this.dragonFight.tick();
            profilerfiller.pop();
         }

         this.entityTickList.forEach((entity) -> {
            if (!entity.isRemoved()) {
               if (this.shouldDiscardEntity(entity)) {
                  entity.discard();
               } else {
                  profilerfiller.push("checkDespawn");
                  entity.checkDespawn();
                  profilerfiller.pop();
                  if (this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) {
                     Entity entity1 = entity.getVehicle();
                     if (entity1 != null) {
                        if (!entity1.isRemoved() && entity1.hasPassenger(entity)) {
                           return;
                        }

                        entity.stopRiding();
                     }

                     profilerfiller.push("tick");
                     this.guardEntityTick(this::tickNonPassenger, entity);
                     profilerfiller.pop();
                  }
               }
            }
         });
         profilerfiller.pop();
         this.tickBlockEntities();
      }

      profilerfiller.push("entityManagement");
      this.entityManager.tick();
      profilerfiller.pop();
   }

   public boolean shouldTickBlocksAt(long i) {
      return this.chunkSource.chunkMap.getDistanceManager().inBlockTickingRange(i);
   }

   protected void tickTime() {
      if (this.tickTime) {
         long i = this.levelData.getGameTime() + 1L;
         this.serverLevelData.setGameTime(i);
         this.serverLevelData.getScheduledEvents().tick(this.server, i);
         if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
         }

      }
   }

   public void setDayTime(long i) {
      this.serverLevelData.setDayTime(i);
   }

   public void tickCustomSpawners(boolean flag, boolean flag1) {
      for(CustomSpawner customspawner : this.customSpawners) {
         customspawner.tick(this, flag, flag1);
      }

   }

   private boolean shouldDiscardEntity(Entity entity) {
      if (this.server.isSpawningAnimals() || !(entity instanceof Animal) && !(entity instanceof WaterAnimal)) {
         return !this.server.areNpcsEnabled() && entity instanceof Npc;
      } else {
         return true;
      }
   }

   private void wakeUpAllPlayers() {
      this.sleepStatus.removeAllSleepers();
      this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach((serverplayer) -> serverplayer.stopSleepInBed(false, false));
   }

   public void tickChunk(LevelChunk levelchunk, int i) {
      ChunkPos chunkpos = levelchunk.getPos();
      boolean flag = this.isRaining();
      int j = chunkpos.getMinBlockX();
      int k = chunkpos.getMinBlockZ();
      ProfilerFiller profilerfiller = this.getProfiler();
      profilerfiller.push("thunder");
      if (flag && this.isThundering() && this.random.nextInt(100000) == 0) {
         BlockPos blockpos = this.findLightningTargetAround(this.getBlockRandomPos(j, 0, k, 15));
         if (this.isRainingAt(blockpos)) {
            DifficultyInstance difficultyinstance = this.getCurrentDifficultyAt(blockpos);
            boolean flag1 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double)difficultyinstance.getEffectiveDifficulty() * 0.01D && !this.getBlockState(blockpos.below()).is(Blocks.LIGHTNING_ROD);
            if (flag1) {
               SkeletonHorse skeletonhorse = EntityType.SKELETON_HORSE.create(this);
               if (skeletonhorse != null) {
                  skeletonhorse.setTrap(true);
                  skeletonhorse.setAge(0);
                  skeletonhorse.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                  this.addFreshEntity(skeletonhorse);
               }
            }

            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(this);
            if (lightningbolt != null) {
               lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
               lightningbolt.setVisualOnly(flag1);
               this.addFreshEntity(lightningbolt);
            }
         }
      }

      profilerfiller.popPush("iceandsnow");
      if (this.random.nextInt(16) == 0) {
         BlockPos blockpos1 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(j, 0, k, 15));
         BlockPos blockpos2 = blockpos1.below();
         Biome biome = this.getBiome(blockpos1).value();
         if (biome.shouldFreeze(this, blockpos2)) {
            this.setBlockAndUpdate(blockpos2, Blocks.ICE.defaultBlockState());
         }

         if (flag) {
            int l = this.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT);
            if (l > 0 && biome.shouldSnow(this, blockpos1)) {
               BlockState blockstate = this.getBlockState(blockpos1);
               if (blockstate.is(Blocks.SNOW)) {
                  int i1 = blockstate.getValue(SnowLayerBlock.LAYERS);
                  if (i1 < Math.min(l, 8)) {
                     BlockState blockstate1 = blockstate.setValue(SnowLayerBlock.LAYERS, Integer.valueOf(i1 + 1));
                     Block.pushEntitiesUp(blockstate, blockstate1, this, blockpos1);
                     this.setBlockAndUpdate(blockpos1, blockstate1);
                  }
               } else {
                  this.setBlockAndUpdate(blockpos1, Blocks.SNOW.defaultBlockState());
               }
            }

            Biome.Precipitation biome_precipitation = biome.getPrecipitationAt(blockpos2);
            if (biome_precipitation != Biome.Precipitation.NONE) {
               BlockState blockstate2 = this.getBlockState(blockpos2);
               blockstate2.getBlock().handlePrecipitation(blockstate2, this, blockpos2, biome_precipitation);
            }
         }
      }

      profilerfiller.popPush("tickBlocks");
      if (i > 0) {
         LevelChunkSection[] alevelchunksection = levelchunk.getSections();

         for(int j1 = 0; j1 < alevelchunksection.length; ++j1) {
            LevelChunkSection levelchunksection = alevelchunksection[j1];
            if (levelchunksection.isRandomlyTicking()) {
               int k1 = levelchunk.getSectionYFromSectionIndex(j1);
               int l1 = SectionPos.sectionToBlockCoord(k1);

               for(int i2 = 0; i2 < i; ++i2) {
                  BlockPos blockpos3 = this.getBlockRandomPos(j, l1, k, 15);
                  profilerfiller.push("randomTick");
                  BlockState blockstate3 = levelchunksection.getBlockState(blockpos3.getX() - j, blockpos3.getY() - l1, blockpos3.getZ() - k);
                  if (blockstate3.isRandomlyTicking()) {
                     blockstate3.randomTick(this, blockpos3, this.random);
                  }

                  FluidState fluidstate = blockstate3.getFluidState();
                  if (fluidstate.isRandomlyTicking()) {
                     fluidstate.randomTick(this, blockpos3, this.random);
                  }

                  profilerfiller.pop();
               }
            }
         }
      }

      profilerfiller.pop();
   }

   private Optional<BlockPos> findLightningRod(BlockPos blockpos) {
      Optional<BlockPos> optional = this.getPoiManager().findClosest((holder) -> holder.is(PoiTypes.LIGHTNING_ROD), (blockpos2) -> blockpos2.getY() == this.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos2.getX(), blockpos2.getZ()) - 1, blockpos, 128, PoiManager.Occupancy.ANY);
      return optional.map((blockpos1) -> blockpos1.above(1));
   }

   protected BlockPos findLightningTargetAround(BlockPos blockpos) {
      BlockPos blockpos1 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos);
      Optional<BlockPos> optional = this.findLightningRod(blockpos1);
      if (optional.isPresent()) {
         return optional.get();
      } else {
         AABB aabb = (new AABB(blockpos1, new BlockPos(blockpos1.getX(), this.getMaxBuildHeight(), blockpos1.getZ()))).inflate(3.0D);
         List<LivingEntity> list = this.getEntitiesOfClass(LivingEntity.class, aabb, (livingentity) -> livingentity != null && livingentity.isAlive() && this.canSeeSky(livingentity.blockPosition()));
         if (!list.isEmpty()) {
            return list.get(this.random.nextInt(list.size())).blockPosition();
         } else {
            if (blockpos1.getY() == this.getMinBuildHeight() - 1) {
               blockpos1 = blockpos1.above(2);
            }

            return blockpos1;
         }
      }
   }

   public boolean isHandlingTick() {
      return this.handlingTick;
   }

   public boolean canSleepThroughNights() {
      return this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) <= 100;
   }

   private void announceSleepStatus() {
      if (this.canSleepThroughNights()) {
         if (!this.getServer().isSingleplayer() || this.getServer().isPublished()) {
            int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
            Component component;
            if (this.sleepStatus.areEnoughSleeping(i)) {
               component = Component.translatable("sleep.skipping_night");
            } else {
               component = Component.translatable("sleep.players_sleeping", this.sleepStatus.amountSleeping(), this.sleepStatus.sleepersNeeded(i));
            }

            for(ServerPlayer serverplayer : this.players) {
               serverplayer.displayClientMessage(component, true);
            }

         }
      }
   }

   public void updateSleepingPlayerList() {
      if (!this.players.isEmpty() && this.sleepStatus.update(this.players)) {
         this.announceSleepStatus();
      }

   }

   public ServerScoreboard getScoreboard() {
      return this.server.getScoreboard();
   }

   private void advanceWeatherCycle() {
      boolean flag = this.isRaining();
      if (this.dimensionType().hasSkyLight()) {
         if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            int i = this.serverLevelData.getClearWeatherTime();
            int j = this.serverLevelData.getThunderTime();
            int k = this.serverLevelData.getRainTime();
            boolean flag1 = this.levelData.isThundering();
            boolean flag2 = this.levelData.isRaining();
            if (i > 0) {
               --i;
               j = flag1 ? 0 : 1;
               k = flag2 ? 0 : 1;
               flag1 = false;
               flag2 = false;
            } else {
               if (j > 0) {
                  --j;
                  if (j == 0) {
                     flag1 = !flag1;
                  }
               } else if (flag1) {
                  j = THUNDER_DURATION.sample(this.random);
               } else {
                  j = THUNDER_DELAY.sample(this.random);
               }

               if (k > 0) {
                  --k;
                  if (k == 0) {
                     flag2 = !flag2;
                  }
               } else if (flag2) {
                  k = RAIN_DURATION.sample(this.random);
               } else {
                  k = RAIN_DELAY.sample(this.random);
               }
            }

            this.serverLevelData.setThunderTime(j);
            this.serverLevelData.setRainTime(k);
            this.serverLevelData.setClearWeatherTime(i);
            this.serverLevelData.setThundering(flag1);
            this.serverLevelData.setRaining(flag2);
         }

         this.oThunderLevel = this.thunderLevel;
         if (this.levelData.isThundering()) {
            this.thunderLevel += 0.01F;
         } else {
            this.thunderLevel -= 0.01F;
         }

         this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
         this.oRainLevel = this.rainLevel;
         if (this.levelData.isRaining()) {
            this.rainLevel += 0.01F;
         } else {
            this.rainLevel -= 0.01F;
         }

         this.rainLevel = Mth.clamp(this.rainLevel, 0.0F, 1.0F);
      }

      if (this.oRainLevel != this.rainLevel) {
         this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
      }

      if (this.oThunderLevel != this.thunderLevel) {
         this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
      }

      if (flag != this.isRaining()) {
         if (flag) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
         } else {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
         }

         this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
         this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
      }

   }

   private void resetWeatherCycle() {
      this.serverLevelData.setRainTime(0);
      this.serverLevelData.setRaining(false);
      this.serverLevelData.setThunderTime(0);
      this.serverLevelData.setThundering(false);
   }

   public void resetEmptyTime() {
      this.emptyTime = 0;
   }

   private void tickFluid(BlockPos blockpos, Fluid fluid) {
      FluidState fluidstate = this.getFluidState(blockpos);
      if (fluidstate.is(fluid)) {
         fluidstate.tick(this, blockpos);
      }

   }

   private void tickBlock(BlockPos blockpos1, Block block) {
      BlockState blockstate = this.getBlockState(blockpos1);
      if (blockstate.is(block)) {
         blockstate.tick(this, blockpos1, this.random);
      }

   }

   public void tickNonPassenger(Entity entity) {
      entity.setOldPosAndRot();
      ProfilerFiller profilerfiller = this.getProfiler();
      ++entity.tickCount;
      this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
      profilerfiller.incrementCounter("tickNonPassenger");
      entity.tick();
      this.getProfiler().pop();

      for(Entity entity1 : entity.getPassengers()) {
         this.tickPassenger(entity, entity1);
      }

   }

   private void tickPassenger(Entity entity, Entity entity1) {
      if (!entity1.isRemoved() && entity1.getVehicle() == entity) {
         if (entity1 instanceof Player || this.entityTickList.contains(entity1)) {
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            ProfilerFiller profilerfiller = this.getProfiler();
            profilerfiller.push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            profilerfiller.incrementCounter("tickPassenger");
            entity1.rideTick();
            profilerfiller.pop();

            for(Entity entity2 : entity1.getPassengers()) {
               this.tickPassenger(entity1, entity2);
            }

         }
      } else {
         entity1.stopRiding();
      }
   }

   public boolean mayInteract(Player player, BlockPos blockpos) {
      return !this.server.isUnderSpawnProtection(this, blockpos, player) && this.getWorldBorder().isWithinBounds(blockpos);
   }

   public void save(@Nullable ProgressListener progresslistener, boolean flag, boolean flag1) {
      ServerChunkCache serverchunkcache = this.getChunkSource();
      if (!flag1) {
         if (progresslistener != null) {
            progresslistener.progressStartNoAbort(Component.translatable("menu.savingLevel"));
         }

         this.saveLevelData();
         if (progresslistener != null) {
            progresslistener.progressStage(Component.translatable("menu.savingChunks"));
         }

         serverchunkcache.save(flag);
         if (flag) {
            this.entityManager.saveAll();
         } else {
            this.entityManager.autoSave();
         }

      }
   }

   private void saveLevelData() {
      if (this.dragonFight != null) {
         this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
      }

      this.getChunkSource().getDataStorage().save();
   }

   public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> entitytypetest, Predicate<? super T> predicate) {
      List<T> list = Lists.newArrayList();
      this.getEntities(entitytypetest, predicate, list);
      return list;
   }

   public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, Predicate<? super T> predicate, List<? super T> list) {
      this.getEntities(entitytypetest, predicate, list, Integer.MAX_VALUE);
   }

   public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, Predicate<? super T> predicate, List<? super T> list, int i) {
      this.getEntities().get(entitytypetest, (entity) -> {
         if (predicate.test(entity)) {
            list.add(entity);
            if (list.size() >= i) {
               return AbortableIterationConsumer.Continuation.ABORT;
            }
         }

         return AbortableIterationConsumer.Continuation.CONTINUE;
      });
   }

   public List<? extends EnderDragon> getDragons() {
      return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
   }

   public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
      return this.getPlayers(predicate, Integer.MAX_VALUE);
   }

   public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate, int i) {
      List<ServerPlayer> list = Lists.newArrayList();

      for(ServerPlayer serverplayer : this.players) {
         if (predicate.test(serverplayer)) {
            list.add(serverplayer);
            if (list.size() >= i) {
               return list;
            }
         }
      }

      return list;
   }

   @Nullable
   public ServerPlayer getRandomPlayer() {
      List<ServerPlayer> list = this.getPlayers(LivingEntity::isAlive);
      return list.isEmpty() ? null : list.get(this.random.nextInt(list.size()));
   }

   public boolean addFreshEntity(Entity entity) {
      return this.addEntity(entity);
   }

   public boolean addWithUUID(Entity entity) {
      return this.addEntity(entity);
   }

   public void addDuringTeleport(Entity entity) {
      this.addEntity(entity);
   }

   public void addDuringCommandTeleport(ServerPlayer serverplayer) {
      this.addPlayer(serverplayer);
   }

   public void addDuringPortalTeleport(ServerPlayer serverplayer) {
      this.addPlayer(serverplayer);
   }

   public void addNewPlayer(ServerPlayer serverplayer) {
      this.addPlayer(serverplayer);
   }

   public void addRespawnedPlayer(ServerPlayer serverplayer) {
      this.addPlayer(serverplayer);
   }

   private void addPlayer(ServerPlayer serverplayer) {
      Entity entity = this.getEntities().get(serverplayer.getUUID());
      if (entity != null) {
         LOGGER.warn("Force-added player with duplicate UUID {}", (Object)serverplayer.getUUID().toString());
         entity.unRide();
         this.removePlayerImmediately((ServerPlayer)entity, Entity.RemovalReason.DISCARDED);
      }

      this.entityManager.addNewEntity(serverplayer);
   }

   private boolean addEntity(Entity entity) {
      if (entity.isRemoved()) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(entity.getType()));
         return false;
      } else {
         return this.entityManager.addNewEntity(entity);
      }
   }

   public boolean tryAddFreshEntityWithPassengers(Entity entity) {
      if (entity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
         return false;
      } else {
         this.addFreshEntityWithPassengers(entity);
         return true;
      }
   }

   public void unload(LevelChunk levelchunk) {
      levelchunk.clearAllBlockEntities();
      levelchunk.unregisterTickContainerFromLevel(this);
   }

   public void removePlayerImmediately(ServerPlayer serverplayer, Entity.RemovalReason entity_removalreason) {
      serverplayer.remove(entity_removalreason);
   }

   public void destroyBlockProgress(int i, BlockPos blockpos, int j) {
      for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
         if (serverplayer != null && serverplayer.level() == this && serverplayer.getId() != i) {
            double d0 = (double)blockpos.getX() - serverplayer.getX();
            double d1 = (double)blockpos.getY() - serverplayer.getY();
            double d2 = (double)blockpos.getZ() - serverplayer.getZ();
            if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
               serverplayer.connection.send(new ClientboundBlockDestructionPacket(i, blockpos, j));
            }
         }
      }

   }

   public void playSeededSound(@Nullable Player player, double d0, double d1, double d2, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i) {
      this.server.getPlayerList().broadcast(player, d0, d1, d2, (double)holder.value().getRange(f), this.dimension(), new ClientboundSoundPacket(holder, soundsource, d0, d1, d2, f, f1, i));
   }

   public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i) {
      this.server.getPlayerList().broadcast(player, entity.getX(), entity.getY(), entity.getZ(), (double)holder.value().getRange(f), this.dimension(), new ClientboundSoundEntityPacket(holder, soundsource, entity, f, f1, i));
   }

   public void globalLevelEvent(int i, BlockPos blockpos, int j) {
      if (this.getGameRules().getBoolean(GameRules.RULE_GLOBAL_SOUND_EVENTS)) {
         this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(i, blockpos, j, true));
      } else {
         this.levelEvent((Player)null, i, blockpos, j);
      }

   }

   public void levelEvent(@Nullable Player player, int i, BlockPos blockpos, int j) {
      this.server.getPlayerList().broadcast(player, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 64.0D, this.dimension(), new ClientboundLevelEventPacket(i, blockpos, j, false));
   }

   public int getLogicalHeight() {
      return this.dimensionType().logicalHeight();
   }

   public void gameEvent(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context) {
      this.gameEventDispatcher.post(gameevent, vec3, gameevent_context);
   }

   public void sendBlockUpdated(BlockPos blockpos, BlockState blockstate, BlockState blockstate1, int i) {
      if (this.isUpdatingNavigations) {
         String s = "recursive call to sendBlockUpdated";
         Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
      }

      this.getChunkSource().blockChanged(blockpos);
      VoxelShape voxelshape = blockstate.getCollisionShape(this, blockpos);
      VoxelShape voxelshape1 = blockstate1.getCollisionShape(this, blockpos);
      if (Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.NOT_SAME)) {
         List<PathNavigation> list = new ObjectArrayList<>();

         for(Mob mob : this.navigatingMobs) {
            PathNavigation pathnavigation = mob.getNavigation();
            if (pathnavigation.shouldRecomputePath(blockpos)) {
               list.add(pathnavigation);
            }
         }

         try {
            this.isUpdatingNavigations = true;

            for(PathNavigation pathnavigation1 : list) {
               pathnavigation1.recomputePath();
            }
         } finally {
            this.isUpdatingNavigations = false;
         }

      }
   }

   public void updateNeighborsAt(BlockPos blockpos, Block block) {
      this.neighborUpdater.updateNeighborsAtExceptFromFacing(blockpos, block, (Direction)null);
   }

   public void updateNeighborsAtExceptFromFacing(BlockPos blockpos, Block block, Direction direction) {
      this.neighborUpdater.updateNeighborsAtExceptFromFacing(blockpos, block, direction);
   }

   public void neighborChanged(BlockPos blockpos, Block block, BlockPos blockpos1) {
      this.neighborUpdater.neighborChanged(blockpos, block, blockpos1);
   }

   public void neighborChanged(BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      this.neighborUpdater.neighborChanged(blockstate, blockpos, block, blockpos1, flag);
   }

   public void broadcastEntityEvent(Entity entity, byte b0) {
      this.getChunkSource().broadcastAndSend(entity, new ClientboundEntityEventPacket(entity, b0));
   }

   public void broadcastDamageEvent(Entity entity, DamageSource damagesource) {
      this.getChunkSource().broadcastAndSend(entity, new ClientboundDamageEventPacket(entity, damagesource));
   }

   public ServerChunkCache getChunkSource() {
      return this.chunkSource;
   }

   public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Level.ExplosionInteraction level_explosioninteraction) {
      Explosion explosion = this.explode(entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, level_explosioninteraction, false);
      if (!explosion.interactsWithBlocks()) {
         explosion.clearToBlow();
      }

      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.distanceToSqr(d0, d1, d2) < 4096.0D) {
            serverplayer.connection.send(new ClientboundExplodePacket(d0, d1, d2, f, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayer)));
         }
      }

      return explosion;
   }

   public void blockEvent(BlockPos blockpos, Block block, int i, int j) {
      this.blockEvents.add(new BlockEventData(blockpos, block, i, j));
   }

   private void runBlockEvents() {
      this.blockEventsToReschedule.clear();

      while(!this.blockEvents.isEmpty()) {
         BlockEventData blockeventdata = this.blockEvents.removeFirst();
         if (this.shouldTickBlocksAt(blockeventdata.pos())) {
            if (this.doBlockEvent(blockeventdata)) {
               this.server.getPlayerList().broadcast((Player)null, (double)blockeventdata.pos().getX(), (double)blockeventdata.pos().getY(), (double)blockeventdata.pos().getZ(), 64.0D, this.dimension(), new ClientboundBlockEventPacket(blockeventdata.pos(), blockeventdata.block(), blockeventdata.paramA(), blockeventdata.paramB()));
            }
         } else {
            this.blockEventsToReschedule.add(blockeventdata);
         }
      }

      this.blockEvents.addAll(this.blockEventsToReschedule);
   }

   private boolean doBlockEvent(BlockEventData blockeventdata) {
      BlockState blockstate = this.getBlockState(blockeventdata.pos());
      return blockstate.is(blockeventdata.block()) ? blockstate.triggerEvent(this, blockeventdata.pos(), blockeventdata.paramA(), blockeventdata.paramB()) : false;
   }

   public LevelTicks<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public LevelTicks<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   @Nonnull
   public MinecraftServer getServer() {
      return this.server;
   }

   public PortalForcer getPortalForcer() {
      return this.portalForcer;
   }

   public StructureTemplateManager getStructureManager() {
      return this.server.getStructureManager();
   }

   public <T extends ParticleOptions> int sendParticles(T particleoptions, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6) {
      ClientboundLevelParticlesPacket clientboundlevelparticlespacket = new ClientboundLevelParticlesPacket(particleoptions, false, d0, d1, d2, (float)d3, (float)d4, (float)d5, (float)d6, i);
      int j = 0;

      for(int k = 0; k < this.players.size(); ++k) {
         ServerPlayer serverplayer = this.players.get(k);
         if (this.sendParticles(serverplayer, false, d0, d1, d2, clientboundlevelparticlespacket)) {
            ++j;
         }
      }

      return j;
   }

   public <T extends ParticleOptions> boolean sendParticles(ServerPlayer serverplayer, T particleoptions, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6) {
      Packet<?> packet = new ClientboundLevelParticlesPacket(particleoptions, flag, d0, d1, d2, (float)d3, (float)d4, (float)d5, (float)d6, i);
      return this.sendParticles(serverplayer, flag, d0, d1, d2, packet);
   }

   private boolean sendParticles(ServerPlayer serverplayer, boolean flag, double d0, double d1, double d2, Packet<?> packet) {
      if (serverplayer.level() != this) {
         return false;
      } else {
         BlockPos blockpos = serverplayer.blockPosition();
         if (blockpos.closerToCenterThan(new Vec3(d0, d1, d2), flag ? 512.0D : 32.0D)) {
            serverplayer.connection.send(packet);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nullable
   public Entity getEntity(int i) {
      return this.getEntities().get(i);
   }

   /** @deprecated */
   @Deprecated
   @Nullable
   public Entity getEntityOrPart(int i) {
      Entity entity = this.getEntities().get(i);
      return entity != null ? entity : this.dragonParts.get(i);
   }

   @Nullable
   public Entity getEntity(UUID uuid) {
      return this.getEntities().get(uuid);
   }

   @Nullable
   public BlockPos findNearestMapStructure(TagKey<Structure> tagkey, BlockPos blockpos, int i, boolean flag) {
      if (!this.server.getWorldData().worldGenOptions().generateStructures()) {
         return null;
      } else {
         Optional<HolderSet.Named<Structure>> optional = this.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tagkey);
         if (optional.isEmpty()) {
            return null;
         } else {
            Pair<BlockPos, Holder<Structure>> pair = this.getChunkSource().getGenerator().findNearestMapStructure(this, optional.get(), blockpos, i, flag);
            return pair != null ? pair.getFirst() : null;
         }
      }
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(Predicate<Holder<Biome>> predicate, BlockPos blockpos, int i, int j, int k) {
      return this.getChunkSource().getGenerator().getBiomeSource().findClosestBiome3d(blockpos, i, j, k, predicate, this.getChunkSource().randomState().sampler(), this);
   }

   public RecipeManager getRecipeManager() {
      return this.server.getRecipeManager();
   }

   public boolean noSave() {
      return this.noSave;
   }

   public DimensionDataStorage getDataStorage() {
      return this.getChunkSource().getDataStorage();
   }

   @Nullable
   public MapItemSavedData getMapData(String s) {
      return this.getServer().overworld().getDataStorage().get(MapItemSavedData::load, s);
   }

   public void setMapData(String s, MapItemSavedData mapitemsaveddata) {
      this.getServer().overworld().getDataStorage().set(s, mapitemsaveddata);
   }

   public int getFreeMapId() {
      return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
   }

   public void setDefaultSpawnPos(BlockPos blockpos, float f) {
      ChunkPos chunkpos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
      this.levelData.setSpawn(blockpos, f);
      this.getChunkSource().removeRegionTicket(TicketType.START, chunkpos, 11, Unit.INSTANCE);
      this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);
      this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(blockpos, f));
   }

   public LongSet getForcedChunks() {
      ForcedChunksSavedData forcedchunkssaveddata = this.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
      return (LongSet)(forcedchunkssaveddata != null ? LongSets.unmodifiable(forcedchunkssaveddata.getChunks()) : LongSets.EMPTY_SET);
   }

   public boolean setChunkForced(int i, int j, boolean flag) {
      ForcedChunksSavedData forcedchunkssaveddata = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
      ChunkPos chunkpos = new ChunkPos(i, j);
      long k = chunkpos.toLong();
      boolean flag1;
      if (flag) {
         flag1 = forcedchunkssaveddata.getChunks().add(k);
         if (flag1) {
            this.getChunk(i, j);
         }
      } else {
         flag1 = forcedchunkssaveddata.getChunks().remove(k);
      }

      forcedchunkssaveddata.setDirty(flag1);
      if (flag1) {
         this.getChunkSource().updateChunkForced(chunkpos, flag);
      }

      return flag1;
   }

   public List<ServerPlayer> players() {
      return this.players;
   }

   public void onBlockStateChange(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      Optional<Holder<PoiType>> optional = PoiTypes.forState(blockstate);
      Optional<Holder<PoiType>> optional1 = PoiTypes.forState(blockstate1);
      if (!Objects.equals(optional, optional1)) {
         BlockPos blockpos1 = blockpos.immutable();
         optional.ifPresent((holder2) -> this.getServer().execute(() -> {
               this.getPoiManager().remove(blockpos1);
               DebugPackets.sendPoiRemovedPacket(this, blockpos1);
            }));
         optional1.ifPresent((holder) -> this.getServer().execute(() -> {
               this.getPoiManager().add(blockpos1, holder);
               DebugPackets.sendPoiAddedPacket(this, blockpos1);
            }));
      }
   }

   public PoiManager getPoiManager() {
      return this.getChunkSource().getPoiManager();
   }

   public boolean isVillage(BlockPos blockpos) {
      return this.isCloseToVillage(blockpos, 1);
   }

   public boolean isVillage(SectionPos sectionpos) {
      return this.isVillage(sectionpos.center());
   }

   public boolean isCloseToVillage(BlockPos blockpos, int i) {
      if (i > 6) {
         return false;
      } else {
         return this.sectionsToVillage(SectionPos.of(blockpos)) <= i;
      }
   }

   public int sectionsToVillage(SectionPos sectionpos) {
      return this.getPoiManager().sectionsToVillage(sectionpos);
   }

   public Raids getRaids() {
      return this.raids;
   }

   @Nullable
   public Raid getRaidAt(BlockPos blockpos) {
      return this.raids.getNearbyRaid(blockpos, 9216);
   }

   public boolean isRaided(BlockPos blockpos) {
      return this.getRaidAt(blockpos) != null;
   }

   public void onReputationEvent(ReputationEventType reputationeventtype, Entity entity, ReputationEventHandler reputationeventhandler) {
      reputationeventhandler.onReputationEventFrom(reputationeventtype, entity);
   }

   public void saveDebugReport(Path path) throws IOException {
      ChunkMap chunkmap = this.getChunkSource().chunkMap;
      Writer writer = Files.newBufferedWriter(path.resolve("stats.txt"));

      try {
         writer.write(String.format(Locale.ROOT, "spawning_chunks: %d\n", chunkmap.getDistanceManager().getNaturalSpawnChunkCount()));
         NaturalSpawner.SpawnState naturalspawner_spawnstate = this.getChunkSource().getLastSpawnState();
         if (naturalspawner_spawnstate != null) {
            for(Object2IntMap.Entry<MobCategory> object2intmap_entry : naturalspawner_spawnstate.getMobCategoryCounts().object2IntEntrySet()) {
               writer.write(String.format(Locale.ROOT, "spawn_count.%s: %d\n", object2intmap_entry.getKey().getName(), object2intmap_entry.getIntValue()));
            }
         }

         writer.write(String.format(Locale.ROOT, "entities: %s\n", this.entityManager.gatherStats()));
         writer.write(String.format(Locale.ROOT, "block_entity_tickers: %d\n", this.blockEntityTickers.size()));
         writer.write(String.format(Locale.ROOT, "block_ticks: %d\n", this.getBlockTicks().count()));
         writer.write(String.format(Locale.ROOT, "fluid_ticks: %d\n", this.getFluidTicks().count()));
         writer.write("distance_manager: " + chunkmap.getDistanceManager().getDebugStatus() + "\n");
         writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
      } catch (Throwable var22) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var16) {
               var22.addSuppressed(var16);
            }
         }

         throw var22;
      }

      if (writer != null) {
         writer.close();
      }

      CrashReport crashreport = new CrashReport("Level dump", new Exception("dummy"));
      this.fillReportDetails(crashreport);
      Writer writer1 = Files.newBufferedWriter(path.resolve("example_crash.txt"));

      try {
         writer1.write(crashreport.getFriendlyReport());
      } catch (Throwable var21) {
         if (writer1 != null) {
            try {
               writer1.close();
            } catch (Throwable var15) {
               var21.addSuppressed(var15);
            }
         }

         throw var21;
      }

      if (writer1 != null) {
         writer1.close();
      }

      Path path1 = path.resolve("chunks.csv");
      Writer writer2 = Files.newBufferedWriter(path1);

      try {
         chunkmap.dumpChunks(writer2);
      } catch (Throwable var20) {
         if (writer2 != null) {
            try {
               writer2.close();
            } catch (Throwable var14) {
               var20.addSuppressed(var14);
            }
         }

         throw var20;
      }

      if (writer2 != null) {
         writer2.close();
      }

      Path path2 = path.resolve("entity_chunks.csv");
      Writer writer3 = Files.newBufferedWriter(path2);

      try {
         this.entityManager.dumpSections(writer3);
      } catch (Throwable var19) {
         if (writer3 != null) {
            try {
               writer3.close();
            } catch (Throwable var13) {
               var19.addSuppressed(var13);
            }
         }

         throw var19;
      }

      if (writer3 != null) {
         writer3.close();
      }

      Path path3 = path.resolve("entities.csv");
      Writer writer4 = Files.newBufferedWriter(path3);

      try {
         dumpEntities(writer4, this.getEntities().getAll());
      } catch (Throwable var18) {
         if (writer4 != null) {
            try {
               writer4.close();
            } catch (Throwable var12) {
               var18.addSuppressed(var12);
            }
         }

         throw var18;
      }

      if (writer4 != null) {
         writer4.close();
      }

      Path path4 = path.resolve("block_entities.csv");
      Writer writer5 = Files.newBufferedWriter(path4);

      try {
         this.dumpBlockEntityTickers(writer5);
      } catch (Throwable var17) {
         if (writer5 != null) {
            try {
               writer5.close();
            } catch (Throwable var11) {
               var17.addSuppressed(var11);
            }
         }

         throw var17;
      }

      if (writer5 != null) {
         writer5.close();
      }

   }

   private static void dumpEntities(Writer writer, Iterable<Entity> iterable) throws IOException {
      CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").build(writer);

      for(Entity entity : iterable) {
         Component component = entity.getCustomName();
         Component component1 = entity.getDisplayName();
         csvoutput.writeRow(entity.getX(), entity.getY(), entity.getZ(), entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), entity.isAlive(), component1.getString(), component != null ? component.getString() : null);
      }

   }

   private void dumpBlockEntityTickers(Writer writer) throws IOException {
      CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(writer);

      for(TickingBlockEntity tickingblockentity : this.blockEntityTickers) {
         BlockPos blockpos = tickingblockentity.getPos();
         csvoutput.writeRow(blockpos.getX(), blockpos.getY(), blockpos.getZ(), tickingblockentity.getType());
      }

   }

   @VisibleForTesting
   public void clearBlockEvents(BoundingBox boundingbox) {
      this.blockEvents.removeIf((blockeventdata) -> boundingbox.isInside(blockeventdata.pos()));
   }

   public void blockUpdated(BlockPos blockpos, Block block) {
      if (!this.isDebug()) {
         this.updateNeighborsAt(blockpos, block);
      }

   }

   public float getShade(Direction direction, boolean flag) {
      return 1.0F;
   }

   public Iterable<Entity> getAllEntities() {
      return this.getEntities().getAll();
   }

   public String toString() {
      return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
   }

   public boolean isFlat() {
      return this.server.getWorldData().isFlatWorld();
   }

   public long getSeed() {
      return this.server.getWorldData().worldGenOptions().seed();
   }

   @Nullable
   public EndDragonFight getDragonFight() {
      return this.dragonFight;
   }

   public ServerLevel getLevel() {
      return this;
   }

   @VisibleForTesting
   public String getWatchdogStats() {
      return String.format(Locale.ROOT, "players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entityManager.gatherStats(), getTypeCount(this.entityManager.getEntityGetter().getAll(), (entity) -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()), this.blockEntityTickers.size(), getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType), this.getBlockTicks().count(), this.getFluidTicks().count(), this.gatherChunkSourceStats());
   }

   private static <T> String getTypeCount(Iterable<T> iterable, Function<T, String> function) {
      try {
         Object2IntOpenHashMap<String> object2intopenhashmap = new Object2IntOpenHashMap<>();

         for(T object : iterable) {
            String s = function.apply(object);
            object2intopenhashmap.addTo(s, 1);
         }

         return object2intopenhashmap.object2IntEntrySet().stream().sorted(Comparator.comparing(Object2IntMap.Entry::getIntValue).reversed()).limit(5L).map((object2intmap_entry) -> (String)object2intmap_entry.getKey() + ":" + object2intmap_entry.getIntValue()).collect(Collectors.joining(","));
      } catch (Exception var6) {
         return "";
      }
   }

   public static void makeObsidianPlatform(ServerLevel serverlevel) {
      BlockPos blockpos = END_SPAWN_POINT;
      int i = blockpos.getX();
      int j = blockpos.getY() - 2;
      int k = blockpos.getZ();
      BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((blockpos2) -> serverlevel.setBlockAndUpdate(blockpos2, Blocks.AIR.defaultBlockState()));
      BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach((blockpos1) -> serverlevel.setBlockAndUpdate(blockpos1, Blocks.OBSIDIAN.defaultBlockState()));
   }

   protected LevelEntityGetter<Entity> getEntities() {
      return this.entityManager.getEntityGetter();
   }

   public void addLegacyChunkEntities(Stream<Entity> stream) {
      this.entityManager.addLegacyChunkEntities(stream);
   }

   public void addWorldGenChunkEntities(Stream<Entity> stream) {
      this.entityManager.addWorldGenChunkEntities(stream);
   }

   public void startTickingChunk(LevelChunk levelchunk) {
      levelchunk.unpackTicks(this.getLevelData().getGameTime());
   }

   public void onStructureStartsAvailable(ChunkAccess chunkaccess) {
      this.server.execute(() -> this.structureCheck.onStructureLoad(chunkaccess.getPos(), chunkaccess.getAllStarts()));
   }

   public void close() throws IOException {
      super.close();
      this.entityManager.close();
   }

   public String gatherChunkSourceStats() {
      return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
   }

   public boolean areEntitiesLoaded(long i) {
      return this.entityManager.areEntitiesLoaded(i);
   }

   private boolean isPositionTickingWithEntitiesLoaded(long k) {
      return this.areEntitiesLoaded(k) && this.chunkSource.isPositionTicking(k);
   }

   public boolean isPositionEntityTicking(BlockPos blockpos) {
      return this.entityManager.canPositionTick(blockpos) && this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(ChunkPos.asLong(blockpos));
   }

   public boolean isNaturalSpawningAllowed(BlockPos blockpos) {
      return this.entityManager.canPositionTick(blockpos);
   }

   public boolean isNaturalSpawningAllowed(ChunkPos chunkpos) {
      return this.entityManager.canPositionTick(chunkpos);
   }

   public FeatureFlagSet enabledFeatures() {
      return this.server.getWorldData().enabledFeatures();
   }

   public RandomSource getRandomSequence(ResourceLocation resourcelocation) {
      return this.randomSequences.get(resourcelocation);
   }

   public RandomSequences getRandomSequences() {
      return this.randomSequences;
   }

   final class EntityCallbacks implements LevelCallback<Entity> {
      public void onCreated(Entity entity) {
      }

      public void onDestroyed(Entity entity) {
         ServerLevel.this.getScoreboard().entityRemoved(entity);
      }

      public void onTickingStart(Entity entity) {
         ServerLevel.this.entityTickList.add(entity);
      }

      public void onTickingEnd(Entity entity) {
         ServerLevel.this.entityTickList.remove(entity);
      }

      public void onTrackingStart(Entity entity) {
         ServerLevel.this.getChunkSource().addEntity(entity);
         if (entity instanceof ServerPlayer serverplayer) {
            ServerLevel.this.players.add(serverplayer);
            ServerLevel.this.updateSleepingPlayerList();
         }

         if (entity instanceof Mob mob) {
            if (ServerLevel.this.isUpdatingNavigations) {
               String s = "onTrackingStart called during navigation iteration";
               Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
            }

            ServerLevel.this.navigatingMobs.add(mob);
         }

         if (entity instanceof EnderDragon enderdragon) {
            for(EnderDragonPart enderdragonpart : enderdragon.getSubEntities()) {
               ServerLevel.this.dragonParts.put(enderdragonpart.getId(), enderdragonpart);
            }
         }

         entity.updateDynamicGameEventListener(DynamicGameEventListener::add);
      }

      public void onTrackingEnd(Entity entity) {
         ServerLevel.this.getChunkSource().removeEntity(entity);
         if (entity instanceof ServerPlayer serverplayer) {
            ServerLevel.this.players.remove(serverplayer);
            ServerLevel.this.updateSleepingPlayerList();
         }

         if (entity instanceof Mob mob) {
            if (ServerLevel.this.isUpdatingNavigations) {
               String s = "onTrackingStart called during navigation iteration";
               Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
            }

            ServerLevel.this.navigatingMobs.remove(mob);
         }

         if (entity instanceof EnderDragon enderdragon) {
            for(EnderDragonPart enderdragonpart : enderdragon.getSubEntities()) {
               ServerLevel.this.dragonParts.remove(enderdragonpart.getId());
            }
         }

         entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
      }

      public void onSectionChange(Entity entity) {
         entity.updateDynamicGameEventListener(DynamicGameEventListener::move);
      }
   }
}
