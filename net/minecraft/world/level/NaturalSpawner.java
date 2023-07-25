package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class NaturalSpawner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MIN_SPAWN_DISTANCE = 24;
   public static final int SPAWN_DISTANCE_CHUNK = 8;
   public static final int SPAWN_DISTANCE_BLOCK = 128;
   static final int MAGIC_NUMBER = (int)Math.pow(17.0D, 2.0D);
   private static final MobCategory[] SPAWNING_CATEGORIES = Stream.of(MobCategory.values()).filter((mobcategory) -> mobcategory != MobCategory.MISC).toArray((i) -> new MobCategory[i]);

   private NaturalSpawner() {
   }

   public static NaturalSpawner.SpawnState createState(int i, Iterable<Entity> iterable, NaturalSpawner.ChunkGetter naturalspawner_chunkgetter, LocalMobCapCalculator localmobcapcalculator) {
      PotentialCalculator potentialcalculator = new PotentialCalculator();
      Object2IntOpenHashMap<MobCategory> object2intopenhashmap = new Object2IntOpenHashMap<>();
      Iterator var6 = iterable.iterator();

      while(true) {
         Entity entity;
         Mob mob;
         do {
            if (!var6.hasNext()) {
               return new NaturalSpawner.SpawnState(i, object2intopenhashmap, potentialcalculator, localmobcapcalculator);
            }

            entity = (Entity)var6.next();
            if (!(entity instanceof Mob)) {
               break;
            }

            mob = (Mob)entity;
         } while(mob.isPersistenceRequired() || mob.requiresCustomPersistence());

         MobCategory mobcategory = entity.getType().getCategory();
         if (mobcategory != MobCategory.MISC) {
            BlockPos blockpos = entity.blockPosition();
            naturalspawner_chunkgetter.query(ChunkPos.asLong(blockpos), (levelchunk) -> {
               MobSpawnSettings.MobSpawnCost mobspawnsettings_mobspawncost = getRoughBiome(blockpos, levelchunk).getMobSettings().getMobSpawnCost(entity.getType());
               if (mobspawnsettings_mobspawncost != null) {
                  potentialcalculator.addCharge(entity.blockPosition(), mobspawnsettings_mobspawncost.charge());
               }

               if (entity instanceof Mob) {
                  localmobcapcalculator.addMob(levelchunk.getPos(), mobcategory);
               }

               object2intopenhashmap.addTo(mobcategory, 1);
            });
         }
      }
   }

   static Biome getRoughBiome(BlockPos blockpos, ChunkAccess chunkaccess) {
      return chunkaccess.getNoiseBiome(QuartPos.fromBlock(blockpos.getX()), QuartPos.fromBlock(blockpos.getY()), QuartPos.fromBlock(blockpos.getZ())).value();
   }

   public static void spawnForChunk(ServerLevel serverlevel, LevelChunk levelchunk, NaturalSpawner.SpawnState naturalspawner_spawnstate, boolean flag, boolean flag1, boolean flag2) {
      serverlevel.getProfiler().push("spawner");

      for(MobCategory mobcategory : SPAWNING_CATEGORIES) {
         if ((flag || !mobcategory.isFriendly()) && (flag1 || mobcategory.isFriendly()) && (flag2 || !mobcategory.isPersistent()) && naturalspawner_spawnstate.canSpawnForCategory(mobcategory, levelchunk.getPos())) {
            spawnCategoryForChunk(mobcategory, serverlevel, levelchunk, naturalspawner_spawnstate::canSpawn, naturalspawner_spawnstate::afterSpawn);
         }
      }

      serverlevel.getProfiler().pop();
   }

   public static void spawnCategoryForChunk(MobCategory mobcategory, ServerLevel serverlevel, LevelChunk levelchunk, NaturalSpawner.SpawnPredicate naturalspawner_spawnpredicate, NaturalSpawner.AfterSpawnCallback naturalspawner_afterspawncallback) {
      BlockPos blockpos = getRandomPosWithin(serverlevel, levelchunk);
      if (blockpos.getY() >= serverlevel.getMinBuildHeight() + 1) {
         spawnCategoryForPosition(mobcategory, serverlevel, levelchunk, blockpos, naturalspawner_spawnpredicate, naturalspawner_afterspawncallback);
      }
   }

   @VisibleForDebug
   public static void spawnCategoryForPosition(MobCategory mobcategory, ServerLevel serverlevel, BlockPos blockpos) {
      spawnCategoryForPosition(mobcategory, serverlevel, serverlevel.getChunk(blockpos), blockpos, (entitytype, blockpos1, chunkaccess1) -> true, (mob, chunkaccess) -> {
      });
   }

   public static void spawnCategoryForPosition(MobCategory mobcategory, ServerLevel serverlevel, ChunkAccess chunkaccess, BlockPos blockpos, NaturalSpawner.SpawnPredicate naturalspawner_spawnpredicate, NaturalSpawner.AfterSpawnCallback naturalspawner_afterspawncallback) {
      StructureManager structuremanager = serverlevel.structureManager();
      ChunkGenerator chunkgenerator = serverlevel.getChunkSource().getGenerator();
      int i = blockpos.getY();
      BlockState blockstate = chunkaccess.getBlockState(blockpos);
      if (!blockstate.isRedstoneConductor(chunkaccess, blockpos)) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
         int j = 0;

         for(int k = 0; k < 3; ++k) {
            int l = blockpos.getX();
            int i1 = blockpos.getZ();
            int j1 = 6;
            MobSpawnSettings.SpawnerData mobspawnsettings_spawnerdata = null;
            SpawnGroupData spawngroupdata = null;
            int k1 = Mth.ceil(serverlevel.random.nextFloat() * 4.0F);
            int l1 = 0;

            for(int i2 = 0; i2 < k1; ++i2) {
               l += serverlevel.random.nextInt(6) - serverlevel.random.nextInt(6);
               i1 += serverlevel.random.nextInt(6) - serverlevel.random.nextInt(6);
               blockpos_mutableblockpos.set(l, i, i1);
               double d0 = (double)l + 0.5D;
               double d1 = (double)i1 + 0.5D;
               Player player = serverlevel.getNearestPlayer(d0, (double)i, d1, -1.0D, false);
               if (player != null) {
                  double d2 = player.distanceToSqr(d0, (double)i, d1);
                  if (isRightDistanceToPlayerAndSpawnPoint(serverlevel, chunkaccess, blockpos_mutableblockpos, d2)) {
                     if (mobspawnsettings_spawnerdata == null) {
                        Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(serverlevel, structuremanager, chunkgenerator, mobcategory, serverlevel.random, blockpos_mutableblockpos);
                        if (optional.isEmpty()) {
                           break;
                        }

                        mobspawnsettings_spawnerdata = optional.get();
                        k1 = mobspawnsettings_spawnerdata.minCount + serverlevel.random.nextInt(1 + mobspawnsettings_spawnerdata.maxCount - mobspawnsettings_spawnerdata.minCount);
                     }

                     if (isValidSpawnPostitionForType(serverlevel, mobcategory, structuremanager, chunkgenerator, mobspawnsettings_spawnerdata, blockpos_mutableblockpos, d2) && naturalspawner_spawnpredicate.test(mobspawnsettings_spawnerdata.type, blockpos_mutableblockpos, chunkaccess)) {
                        Mob mob = getMobForSpawn(serverlevel, mobspawnsettings_spawnerdata.type);
                        if (mob == null) {
                           return;
                        }

                        mob.moveTo(d0, (double)i, d1, serverlevel.random.nextFloat() * 360.0F, 0.0F);
                        if (isValidPositionForMob(serverlevel, mob, d2)) {
                           spawngroupdata = mob.finalizeSpawn(serverlevel, serverlevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawngroupdata, (CompoundTag)null);
                           ++j;
                           ++l1;
                           serverlevel.addFreshEntityWithPassengers(mob);
                           naturalspawner_afterspawncallback.run(mob, chunkaccess);
                           if (j >= mob.getMaxSpawnClusterSize()) {
                              return;
                           }

                           if (mob.isMaxGroupSizeReached(l1)) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverlevel, ChunkAccess chunkaccess, BlockPos.MutableBlockPos blockpos_mutableblockpos, double d0) {
      if (d0 <= 576.0D) {
         return false;
      } else if (serverlevel.getSharedSpawnPos().closerToCenterThan(new Vec3((double)blockpos_mutableblockpos.getX() + 0.5D, (double)blockpos_mutableblockpos.getY(), (double)blockpos_mutableblockpos.getZ() + 0.5D), 24.0D)) {
         return false;
      } else {
         return Objects.equals(new ChunkPos(blockpos_mutableblockpos), chunkaccess.getPos()) || serverlevel.isNaturalSpawningAllowed(blockpos_mutableblockpos);
      }
   }

   private static boolean isValidSpawnPostitionForType(ServerLevel serverlevel, MobCategory mobcategory, StructureManager structuremanager, ChunkGenerator chunkgenerator, MobSpawnSettings.SpawnerData mobspawnsettings_spawnerdata, BlockPos.MutableBlockPos blockpos_mutableblockpos, double d0) {
      EntityType<?> entitytype = mobspawnsettings_spawnerdata.type;
      if (entitytype.getCategory() == MobCategory.MISC) {
         return false;
      } else if (!entitytype.canSpawnFarFromPlayer() && d0 > (double)(entitytype.getCategory().getDespawnDistance() * entitytype.getCategory().getDespawnDistance())) {
         return false;
      } else if (entitytype.canSummon() && canSpawnMobAt(serverlevel, structuremanager, chunkgenerator, mobcategory, mobspawnsettings_spawnerdata, blockpos_mutableblockpos)) {
         SpawnPlacements.Type spawnplacements_type = SpawnPlacements.getPlacementType(entitytype);
         if (!isSpawnPositionOk(spawnplacements_type, serverlevel, blockpos_mutableblockpos, entitytype)) {
            return false;
         } else if (!SpawnPlacements.checkSpawnRules(entitytype, serverlevel, MobSpawnType.NATURAL, blockpos_mutableblockpos, serverlevel.random)) {
            return false;
         } else {
            return serverlevel.noCollision(entitytype.getAABB((double)blockpos_mutableblockpos.getX() + 0.5D, (double)blockpos_mutableblockpos.getY(), (double)blockpos_mutableblockpos.getZ() + 0.5D));
         }
      } else {
         return false;
      }
   }

   @Nullable
   private static Mob getMobForSpawn(ServerLevel serverlevel, EntityType<?> entitytype) {
      try {
         Entity var3 = entitytype.create(serverlevel);
         if (var3 instanceof Mob) {
            return (Mob)var3;
         }

         LOGGER.warn("Can't spawn entity of type: {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(entitytype));
      } catch (Exception var4) {
         LOGGER.warn("Failed to create mob", (Throwable)var4);
      }

      return null;
   }

   private static boolean isValidPositionForMob(ServerLevel serverlevel, Mob mob, double d0) {
      if (d0 > (double)(mob.getType().getCategory().getDespawnDistance() * mob.getType().getCategory().getDespawnDistance()) && mob.removeWhenFarAway(d0)) {
         return false;
      } else {
         return mob.checkSpawnRules(serverlevel, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(serverlevel);
      }
   }

   private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel serverlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, MobCategory mobcategory, RandomSource randomsource, BlockPos blockpos) {
      Holder<Biome> holder = serverlevel.getBiome(blockpos);
      return mobcategory == MobCategory.WATER_AMBIENT && holder.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && randomsource.nextFloat() < 0.98F ? Optional.empty() : mobsAt(serverlevel, structuremanager, chunkgenerator, mobcategory, blockpos, holder).getRandom(randomsource);
   }

   private static boolean canSpawnMobAt(ServerLevel serverlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, MobCategory mobcategory, MobSpawnSettings.SpawnerData mobspawnsettings_spawnerdata, BlockPos blockpos) {
      return mobsAt(serverlevel, structuremanager, chunkgenerator, mobcategory, blockpos, (Holder<Biome>)null).unwrap().contains(mobspawnsettings_spawnerdata);
   }

   private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(ServerLevel serverlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, MobCategory mobcategory, BlockPos blockpos, @Nullable Holder<Biome> holder) {
      return isInNetherFortressBounds(blockpos, serverlevel, mobcategory, structuremanager) ? NetherFortressStructure.FORTRESS_ENEMIES : chunkgenerator.getMobsAt(holder != null ? holder : serverlevel.getBiome(blockpos), structuremanager, mobcategory, blockpos);
   }

   public static boolean isInNetherFortressBounds(BlockPos blockpos, ServerLevel serverlevel, MobCategory mobcategory, StructureManager structuremanager) {
      if (mobcategory == MobCategory.MONSTER && serverlevel.getBlockState(blockpos.below()).is(Blocks.NETHER_BRICKS)) {
         Structure structure = structuremanager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
         return structure == null ? false : structuremanager.getStructureAt(blockpos, structure).isValid();
      } else {
         return false;
      }
   }

   private static BlockPos getRandomPosWithin(Level level, LevelChunk levelchunk) {
      ChunkPos chunkpos = levelchunk.getPos();
      int i = chunkpos.getMinBlockX() + level.random.nextInt(16);
      int j = chunkpos.getMinBlockZ() + level.random.nextInt(16);
      int k = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
      int l = Mth.randomBetweenInclusive(level.random, level.getMinBuildHeight(), k);
      return new BlockPos(i, l, j);
   }

   public static boolean isValidEmptySpawnBlock(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate, EntityType<?> entitytype) {
      if (blockstate.isCollisionShapeFullBlock(blockgetter, blockpos)) {
         return false;
      } else if (blockstate.isSignalSource()) {
         return false;
      } else if (!fluidstate.isEmpty()) {
         return false;
      } else if (blockstate.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
         return false;
      } else {
         return !entitytype.isBlockDangerous(blockstate);
      }
   }

   public static boolean isSpawnPositionOk(SpawnPlacements.Type spawnplacements_type, LevelReader levelreader, BlockPos blockpos, @Nullable EntityType<?> entitytype) {
      if (spawnplacements_type == SpawnPlacements.Type.NO_RESTRICTIONS) {
         return true;
      } else if (entitytype != null && levelreader.getWorldBorder().isWithinBounds(blockpos)) {
         BlockState blockstate = levelreader.getBlockState(blockpos);
         FluidState fluidstate = levelreader.getFluidState(blockpos);
         BlockPos blockpos1 = blockpos.above();
         BlockPos blockpos2 = blockpos.below();
         switch (spawnplacements_type) {
            case IN_WATER:
               return fluidstate.is(FluidTags.WATER) && !levelreader.getBlockState(blockpos1).isRedstoneConductor(levelreader, blockpos1);
            case IN_LAVA:
               return fluidstate.is(FluidTags.LAVA);
            case ON_GROUND:
            default:
               BlockState blockstate1 = levelreader.getBlockState(blockpos2);
               if (!blockstate1.isValidSpawn(levelreader, blockpos2, entitytype)) {
                  return false;
               } else {
                  return isValidEmptySpawnBlock(levelreader, blockpos, blockstate, fluidstate, entitytype) && isValidEmptySpawnBlock(levelreader, blockpos1, levelreader.getBlockState(blockpos1), levelreader.getFluidState(blockpos1), entitytype);
               }
         }
      } else {
         return false;
      }
   }

   public static void spawnMobsForChunkGeneration(ServerLevelAccessor serverlevelaccessor, Holder<Biome> holder, ChunkPos chunkpos, RandomSource randomsource) {
      MobSpawnSettings mobspawnsettings = holder.value().getMobSettings();
      WeightedRandomList<MobSpawnSettings.SpawnerData> weightedrandomlist = mobspawnsettings.getMobs(MobCategory.CREATURE);
      if (!weightedrandomlist.isEmpty()) {
         int i = chunkpos.getMinBlockX();
         int j = chunkpos.getMinBlockZ();

         while(randomsource.nextFloat() < mobspawnsettings.getCreatureProbability()) {
            Optional<MobSpawnSettings.SpawnerData> optional = weightedrandomlist.getRandom(randomsource);
            if (optional.isPresent()) {
               MobSpawnSettings.SpawnerData mobspawnsettings_spawnerdata = optional.get();
               int k = mobspawnsettings_spawnerdata.minCount + randomsource.nextInt(1 + mobspawnsettings_spawnerdata.maxCount - mobspawnsettings_spawnerdata.minCount);
               SpawnGroupData spawngroupdata = null;
               int l = i + randomsource.nextInt(16);
               int i1 = j + randomsource.nextInt(16);
               int j1 = l;
               int k1 = i1;

               for(int l1 = 0; l1 < k; ++l1) {
                  boolean flag = false;

                  for(int i2 = 0; !flag && i2 < 4; ++i2) {
                     BlockPos blockpos = getTopNonCollidingPos(serverlevelaccessor, mobspawnsettings_spawnerdata.type, l, i1);
                     if (mobspawnsettings_spawnerdata.type.canSummon() && isSpawnPositionOk(SpawnPlacements.getPlacementType(mobspawnsettings_spawnerdata.type), serverlevelaccessor, blockpos, mobspawnsettings_spawnerdata.type)) {
                        float f = mobspawnsettings_spawnerdata.type.getWidth();
                        double d0 = Mth.clamp((double)l, (double)i + (double)f, (double)i + 16.0D - (double)f);
                        double d1 = Mth.clamp((double)i1, (double)j + (double)f, (double)j + 16.0D - (double)f);
                        if (!serverlevelaccessor.noCollision(mobspawnsettings_spawnerdata.type.getAABB(d0, (double)blockpos.getY(), d1)) || !SpawnPlacements.checkSpawnRules(mobspawnsettings_spawnerdata.type, serverlevelaccessor, MobSpawnType.CHUNK_GENERATION, BlockPos.containing(d0, (double)blockpos.getY(), d1), serverlevelaccessor.getRandom())) {
                           continue;
                        }

                        Entity entity;
                        try {
                           entity = mobspawnsettings_spawnerdata.type.create(serverlevelaccessor.getLevel());
                        } catch (Exception var27) {
                           LOGGER.warn("Failed to create mob", (Throwable)var27);
                           continue;
                        }

                        if (entity == null) {
                           continue;
                        }

                        entity.moveTo(d0, (double)blockpos.getY(), d1, randomsource.nextFloat() * 360.0F, 0.0F);
                        if (entity instanceof Mob) {
                           Mob mob = (Mob)entity;
                           if (mob.checkSpawnRules(serverlevelaccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverlevelaccessor)) {
                              spawngroupdata = mob.finalizeSpawn(serverlevelaccessor, serverlevelaccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawngroupdata, (CompoundTag)null);
                              serverlevelaccessor.addFreshEntityWithPassengers(mob);
                              flag = true;
                           }
                        }
                     }

                     l += randomsource.nextInt(5) - randomsource.nextInt(5);

                     for(i1 += randomsource.nextInt(5) - randomsource.nextInt(5); l < i || l >= i + 16 || i1 < j || i1 >= j + 16; i1 = k1 + randomsource.nextInt(5) - randomsource.nextInt(5)) {
                        l = j1 + randomsource.nextInt(5) - randomsource.nextInt(5);
                     }
                  }
               }
            }
         }

      }
   }

   private static BlockPos getTopNonCollidingPos(LevelReader levelreader, EntityType<?> entitytype, int i, int j) {
      int k = levelreader.getHeight(SpawnPlacements.getHeightmapType(entitytype), i, j);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, k, j);
      if (levelreader.dimensionType().hasCeiling()) {
         do {
            blockpos_mutableblockpos.move(Direction.DOWN);
         } while(!levelreader.getBlockState(blockpos_mutableblockpos).isAir());

         do {
            blockpos_mutableblockpos.move(Direction.DOWN);
         } while(levelreader.getBlockState(blockpos_mutableblockpos).isAir() && blockpos_mutableblockpos.getY() > levelreader.getMinBuildHeight());
      }

      if (SpawnPlacements.getPlacementType(entitytype) == SpawnPlacements.Type.ON_GROUND) {
         BlockPos blockpos = blockpos_mutableblockpos.below();
         if (levelreader.getBlockState(blockpos).isPathfindable(levelreader, blockpos, PathComputationType.LAND)) {
            return blockpos;
         }
      }

      return blockpos_mutableblockpos.immutable();
   }

   @FunctionalInterface
   public interface AfterSpawnCallback {
      void run(Mob mob, ChunkAccess chunkaccess);
   }

   @FunctionalInterface
   public interface ChunkGetter {
      void query(long i, Consumer<LevelChunk> consumer);
   }

   @FunctionalInterface
   public interface SpawnPredicate {
      boolean test(EntityType<?> entitytype, BlockPos blockpos, ChunkAccess chunkaccess);
   }

   public static class SpawnState {
      private final int spawnableChunkCount;
      private final Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
      private final PotentialCalculator spawnPotential;
      private final Object2IntMap<MobCategory> unmodifiableMobCategoryCounts;
      private final LocalMobCapCalculator localMobCapCalculator;
      @Nullable
      private BlockPos lastCheckedPos;
      @Nullable
      private EntityType<?> lastCheckedType;
      private double lastCharge;

      SpawnState(int i, Object2IntOpenHashMap<MobCategory> object2intopenhashmap, PotentialCalculator potentialcalculator, LocalMobCapCalculator localmobcapcalculator) {
         this.spawnableChunkCount = i;
         this.mobCategoryCounts = object2intopenhashmap;
         this.spawnPotential = potentialcalculator;
         this.localMobCapCalculator = localmobcapcalculator;
         this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(object2intopenhashmap);
      }

      private boolean canSpawn(EntityType<?> entitytype, BlockPos blockpos, ChunkAccess chunkaccess) {
         this.lastCheckedPos = blockpos;
         this.lastCheckedType = entitytype;
         MobSpawnSettings.MobSpawnCost mobspawnsettings_mobspawncost = NaturalSpawner.getRoughBiome(blockpos, chunkaccess).getMobSettings().getMobSpawnCost(entitytype);
         if (mobspawnsettings_mobspawncost == null) {
            this.lastCharge = 0.0D;
            return true;
         } else {
            double d0 = mobspawnsettings_mobspawncost.charge();
            this.lastCharge = d0;
            double d1 = this.spawnPotential.getPotentialEnergyChange(blockpos, d0);
            return d1 <= mobspawnsettings_mobspawncost.energyBudget();
         }
      }

      private void afterSpawn(Mob mob, ChunkAccess chunkaccess) {
         EntityType<?> entitytype = mob.getType();
         BlockPos blockpos = mob.blockPosition();
         double d0;
         if (blockpos.equals(this.lastCheckedPos) && entitytype == this.lastCheckedType) {
            d0 = this.lastCharge;
         } else {
            MobSpawnSettings.MobSpawnCost mobspawnsettings_mobspawncost = NaturalSpawner.getRoughBiome(blockpos, chunkaccess).getMobSettings().getMobSpawnCost(entitytype);
            if (mobspawnsettings_mobspawncost != null) {
               d0 = mobspawnsettings_mobspawncost.charge();
            } else {
               d0 = 0.0D;
            }
         }

         this.spawnPotential.addCharge(blockpos, d0);
         MobCategory mobcategory = entitytype.getCategory();
         this.mobCategoryCounts.addTo(mobcategory, 1);
         this.localMobCapCalculator.addMob(new ChunkPos(blockpos), mobcategory);
      }

      public int getSpawnableChunkCount() {
         return this.spawnableChunkCount;
      }

      public Object2IntMap<MobCategory> getMobCategoryCounts() {
         return this.unmodifiableMobCategoryCounts;
      }

      boolean canSpawnForCategory(MobCategory mobcategory, ChunkPos chunkpos) {
         int i = mobcategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
         if (this.mobCategoryCounts.getInt(mobcategory) >= i) {
            return false;
         } else {
            return this.localMobCapCalculator.canSpawn(mobcategory, chunkpos);
         }
      }
   }
}
