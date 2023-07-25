package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PatrolSpawner implements CustomSpawner {
   private int nextTick;

   public int tick(ServerLevel serverlevel, boolean flag, boolean flag1) {
      if (!flag) {
         return 0;
      } else if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
         return 0;
      } else {
         RandomSource randomsource = serverlevel.random;
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick += 12000 + randomsource.nextInt(1200);
            long i = serverlevel.getDayTime() / 24000L;
            if (i >= 5L && serverlevel.isDay()) {
               if (randomsource.nextInt(5) != 0) {
                  return 0;
               } else {
                  int j = serverlevel.players().size();
                  if (j < 1) {
                     return 0;
                  } else {
                     Player player = serverlevel.players().get(randomsource.nextInt(j));
                     if (player.isSpectator()) {
                        return 0;
                     } else if (serverlevel.isCloseToVillage(player.blockPosition(), 2)) {
                        return 0;
                     } else {
                        int k = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
                        int l = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
                        BlockPos.MutableBlockPos blockpos_mutableblockpos = player.blockPosition().mutable().move(k, 0, l);
                        int i1 = 10;
                        if (!serverlevel.hasChunksAt(blockpos_mutableblockpos.getX() - 10, blockpos_mutableblockpos.getZ() - 10, blockpos_mutableblockpos.getX() + 10, blockpos_mutableblockpos.getZ() + 10)) {
                           return 0;
                        } else {
                           Holder<Biome> holder = serverlevel.getBiome(blockpos_mutableblockpos);
                           if (holder.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
                              return 0;
                           } else {
                              int j1 = 0;
                              int k1 = (int)Math.ceil((double)serverlevel.getCurrentDifficultyAt(blockpos_mutableblockpos).getEffectiveDifficulty()) + 1;

                              for(int l1 = 0; l1 < k1; ++l1) {
                                 ++j1;
                                 blockpos_mutableblockpos.setY(serverlevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos_mutableblockpos).getY());
                                 if (l1 == 0) {
                                    if (!this.spawnPatrolMember(serverlevel, blockpos_mutableblockpos, randomsource, true)) {
                                       break;
                                    }
                                 } else {
                                    this.spawnPatrolMember(serverlevel, blockpos_mutableblockpos, randomsource, false);
                                 }

                                 blockpos_mutableblockpos.setX(blockpos_mutableblockpos.getX() + randomsource.nextInt(5) - randomsource.nextInt(5));
                                 blockpos_mutableblockpos.setZ(blockpos_mutableblockpos.getZ() + randomsource.nextInt(5) - randomsource.nextInt(5));
                              }

                              return j1;
                           }
                        }
                     }
                  }
               }
            } else {
               return 0;
            }
         }
      }
   }

   private boolean spawnPatrolMember(ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource, boolean flag) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      if (!NaturalSpawner.isValidEmptySpawnBlock(serverlevel, blockpos, blockstate, blockstate.getFluidState(), EntityType.PILLAGER)) {
         return false;
      } else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, serverlevel, MobSpawnType.PATROL, blockpos, randomsource)) {
         return false;
      } else {
         PatrollingMonster patrollingmonster = EntityType.PILLAGER.create(serverlevel);
         if (patrollingmonster != null) {
            if (flag) {
               patrollingmonster.setPatrolLeader(true);
               patrollingmonster.findPatrolTarget();
            }

            patrollingmonster.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            patrollingmonster.finalizeSpawn(serverlevel, serverlevel.getCurrentDifficultyAt(blockpos), MobSpawnType.PATROL, (SpawnGroupData)null, (CompoundTag)null);
            serverlevel.addFreshEntityWithPassengers(patrollingmonster);
            return true;
         } else {
            return false;
         }
      }
   }
}
