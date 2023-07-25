package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner implements CustomSpawner {
   private int nextTick;

   public int tick(ServerLevel serverlevel, boolean flag, boolean flag1) {
      if (!flag) {
         return 0;
      } else if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
         return 0;
      } else {
         RandomSource randomsource = serverlevel.random;
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick += (60 + randomsource.nextInt(60)) * 20;
            if (serverlevel.getSkyDarken() < 5 && serverlevel.dimensionType().hasSkyLight()) {
               return 0;
            } else {
               int i = 0;

               for(ServerPlayer serverplayer : serverlevel.players()) {
                  if (!serverplayer.isSpectator()) {
                     BlockPos blockpos = serverplayer.blockPosition();
                     if (!serverlevel.dimensionType().hasSkyLight() || blockpos.getY() >= serverlevel.getSeaLevel() && serverlevel.canSeeSky(blockpos)) {
                        DifficultyInstance difficultyinstance = serverlevel.getCurrentDifficultyAt(blockpos);
                        if (difficultyinstance.isHarderThan(randomsource.nextFloat() * 3.0F)) {
                           ServerStatsCounter serverstatscounter = serverplayer.getStats();
                           int j = Mth.clamp(serverstatscounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                           int k = 24000;
                           if (randomsource.nextInt(j) >= 72000) {
                              BlockPos blockpos1 = blockpos.above(20 + randomsource.nextInt(15)).east(-10 + randomsource.nextInt(21)).south(-10 + randomsource.nextInt(21));
                              BlockState blockstate = serverlevel.getBlockState(blockpos1);
                              FluidState fluidstate = serverlevel.getFluidState(blockpos1);
                              if (NaturalSpawner.isValidEmptySpawnBlock(serverlevel, blockpos1, blockstate, fluidstate, EntityType.PHANTOM)) {
                                 SpawnGroupData spawngroupdata = null;
                                 int l = 1 + randomsource.nextInt(difficultyinstance.getDifficulty().getId() + 1);

                                 for(int i1 = 0; i1 < l; ++i1) {
                                    Phantom phantom = EntityType.PHANTOM.create(serverlevel);
                                    if (phantom != null) {
                                       phantom.moveTo(blockpos1, 0.0F, 0.0F);
                                       spawngroupdata = phantom.finalizeSpawn(serverlevel, difficultyinstance, MobSpawnType.NATURAL, spawngroupdata, (CompoundTag)null);
                                       serverlevel.addFreshEntityWithPassengers(phantom);
                                       ++i;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return i;
            }
         }
      }
   }
}
