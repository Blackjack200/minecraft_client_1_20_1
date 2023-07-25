package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public enum DragonRespawnAnimation {
   START {
      public void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos) {
         BlockPos blockpos1 = new BlockPos(0, 128, 0);

         for(EndCrystal endcrystal : list) {
            endcrystal.setBeamTarget(blockpos1);
         }

         enddragonfight.setRespawnStage(b);
      }
   },
   PREPARING_TO_SUMMON_PILLARS {
      public void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos) {
         if (i < 100) {
            if (i == 0 || i == 50 || i == 51 || i == 52 || i >= 95) {
               serverlevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }
         } else {
            enddragonfight.setRespawnStage(c);
         }

      }
   },
   SUMMONING_PILLARS {
      public void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos) {
         int j = 40;
         boolean flag = i % 40 == 0;
         boolean flag1 = i % 40 == 39;
         if (flag || flag1) {
            List<SpikeFeature.EndSpike> list1 = SpikeFeature.getSpikesForLevel(serverlevel);
            int k = i / 40;
            if (k < list1.size()) {
               SpikeFeature.EndSpike spikefeature_endspike = list1.get(k);
               if (flag) {
                  for(EndCrystal endcrystal : list) {
                     endcrystal.setBeamTarget(new BlockPos(spikefeature_endspike.getCenterX(), spikefeature_endspike.getHeight() + 1, spikefeature_endspike.getCenterZ()));
                  }
               } else {
                  int l = 10;

                  for(BlockPos blockpos1 : BlockPos.betweenClosed(new BlockPos(spikefeature_endspike.getCenterX() - 10, spikefeature_endspike.getHeight() - 10, spikefeature_endspike.getCenterZ() - 10), new BlockPos(spikefeature_endspike.getCenterX() + 10, spikefeature_endspike.getHeight() + 10, spikefeature_endspike.getCenterZ() + 10))) {
                     serverlevel.removeBlock(blockpos1, false);
                  }

                  serverlevel.explode((Entity)null, (double)((float)spikefeature_endspike.getCenterX() + 0.5F), (double)spikefeature_endspike.getHeight(), (double)((float)spikefeature_endspike.getCenterZ() + 0.5F), 5.0F, Level.ExplosionInteraction.BLOCK);
                  SpikeConfiguration spikeconfiguration = new SpikeConfiguration(true, ImmutableList.of(spikefeature_endspike), new BlockPos(0, 128, 0));
                  Feature.END_SPIKE.place(spikeconfiguration, serverlevel, serverlevel.getChunkSource().getGenerator(), RandomSource.create(), new BlockPos(spikefeature_endspike.getCenterX(), 45, spikefeature_endspike.getCenterZ()));
               }
            } else if (flag) {
               enddragonfight.setRespawnStage(d);
            }
         }

      }
   },
   SUMMONING_DRAGON {
      public void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos) {
         if (i >= 100) {
            enddragonfight.setRespawnStage(e);
            enddragonfight.resetSpikeCrystals();

            for(EndCrystal endcrystal : list) {
               endcrystal.setBeamTarget((BlockPos)null);
               serverlevel.explode(endcrystal, endcrystal.getX(), endcrystal.getY(), endcrystal.getZ(), 6.0F, Level.ExplosionInteraction.NONE);
               endcrystal.discard();
            }
         } else if (i >= 80) {
            serverlevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
         } else if (i == 0) {
            for(EndCrystal endcrystal1 : list) {
               endcrystal1.setBeamTarget(new BlockPos(0, 128, 0));
            }
         } else if (i < 5) {
            serverlevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
         }

      }
   },
   END {
      public void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos) {
      }
   };

   public abstract void tick(ServerLevel serverlevel, EndDragonFight enddragonfight, List<EndCrystal> list, int i, BlockPos blockpos);
}
