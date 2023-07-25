package net.minecraft.world.entity.npc;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner implements CustomSpawner {
   private static final int DEFAULT_TICK_DELAY = 1200;
   public static final int DEFAULT_SPAWN_DELAY = 24000;
   private static final int MIN_SPAWN_CHANCE = 25;
   private static final int MAX_SPAWN_CHANCE = 75;
   private static final int SPAWN_CHANCE_INCREASE = 25;
   private static final int SPAWN_ONE_IN_X_CHANCE = 10;
   private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
   private final RandomSource random = RandomSource.create();
   private final ServerLevelData serverLevelData;
   private int tickDelay;
   private int spawnDelay;
   private int spawnChance;

   public WanderingTraderSpawner(ServerLevelData serverleveldata) {
      this.serverLevelData = serverleveldata;
      this.tickDelay = 1200;
      this.spawnDelay = serverleveldata.getWanderingTraderSpawnDelay();
      this.spawnChance = serverleveldata.getWanderingTraderSpawnChance();
      if (this.spawnDelay == 0 && this.spawnChance == 0) {
         this.spawnDelay = 24000;
         serverleveldata.setWanderingTraderSpawnDelay(this.spawnDelay);
         this.spawnChance = 25;
         serverleveldata.setWanderingTraderSpawnChance(this.spawnChance);
      }

   }

   public int tick(ServerLevel serverlevel, boolean flag, boolean flag1) {
      if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
         return 0;
      } else if (--this.tickDelay > 0) {
         return 0;
      } else {
         this.tickDelay = 1200;
         this.spawnDelay -= 1200;
         this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
         if (this.spawnDelay > 0) {
            return 0;
         } else {
            this.spawnDelay = 24000;
            if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
               return 0;
            } else {
               int i = this.spawnChance;
               this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
               this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
               if (this.random.nextInt(100) > i) {
                  return 0;
               } else if (this.spawn(serverlevel)) {
                  this.spawnChance = 25;
                  return 1;
               } else {
                  return 0;
               }
            }
         }
      }
   }

   private boolean spawn(ServerLevel serverlevel) {
      Player player = serverlevel.getRandomPlayer();
      if (player == null) {
         return true;
      } else if (this.random.nextInt(10) != 0) {
         return false;
      } else {
         BlockPos blockpos = player.blockPosition();
         int i = 48;
         PoiManager poimanager = serverlevel.getPoiManager();
         Optional<BlockPos> optional = poimanager.find((holder) -> holder.is(PoiTypes.MEETING), (blockpos3) -> true, blockpos, 48, PoiManager.Occupancy.ANY);
         BlockPos blockpos1 = optional.orElse(blockpos);
         BlockPos blockpos2 = this.findSpawnPositionNear(serverlevel, blockpos1, 48);
         if (blockpos2 != null && this.hasEnoughSpace(serverlevel, blockpos2)) {
            if (serverlevel.getBiome(blockpos2).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
               return false;
            }

            WanderingTrader wanderingtrader = EntityType.WANDERING_TRADER.spawn(serverlevel, blockpos2, MobSpawnType.EVENT);
            if (wanderingtrader != null) {
               for(int j = 0; j < 2; ++j) {
                  this.tryToSpawnLlamaFor(serverlevel, wanderingtrader, 4);
               }

               this.serverLevelData.setWanderingTraderId(wanderingtrader.getUUID());
               wanderingtrader.setDespawnDelay(48000);
               wanderingtrader.setWanderTarget(blockpos1);
               wanderingtrader.restrictTo(blockpos1, 16);
               return true;
            }
         }

         return false;
      }
   }

   private void tryToSpawnLlamaFor(ServerLevel serverlevel, WanderingTrader wanderingtrader, int i) {
      BlockPos blockpos = this.findSpawnPositionNear(serverlevel, wanderingtrader.blockPosition(), i);
      if (blockpos != null) {
         TraderLlama traderllama = EntityType.TRADER_LLAMA.spawn(serverlevel, blockpos, MobSpawnType.EVENT);
         if (traderllama != null) {
            traderllama.setLeashedTo(wanderingtrader, true);
         }
      }
   }

   @Nullable
   private BlockPos findSpawnPositionNear(LevelReader levelreader, BlockPos blockpos, int i) {
      BlockPos blockpos1 = null;

      for(int j = 0; j < 10; ++j) {
         int k = blockpos.getX() + this.random.nextInt(i * 2) - i;
         int l = blockpos.getZ() + this.random.nextInt(i * 2) - i;
         int i1 = levelreader.getHeight(Heightmap.Types.WORLD_SURFACE, k, l);
         BlockPos blockpos2 = new BlockPos(k, i1, l);
         if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, levelreader, blockpos2, EntityType.WANDERING_TRADER)) {
            blockpos1 = blockpos2;
            break;
         }
      }

      return blockpos1;
   }

   private boolean hasEnoughSpace(BlockGetter blockgetter, BlockPos blockpos) {
      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos, blockpos.offset(1, 2, 1))) {
         if (!blockgetter.getBlockState(blockpos1).getCollisionShape(blockgetter, blockpos1).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
