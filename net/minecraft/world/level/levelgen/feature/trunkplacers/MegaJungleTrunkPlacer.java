package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
   public static final Codec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, MegaJungleTrunkPlacer::new));

   public MegaJungleTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      list.addAll(super.placeTrunk(levelsimulatedreader, biconsumer, randomsource, i, blockpos, treeconfiguration));

      for(int j = i - 2 - randomsource.nextInt(4); j > i / 2; j -= 2 + randomsource.nextInt(4)) {
         float f = randomsource.nextFloat() * ((float)Math.PI * 2F);
         int k = 0;
         int l = 0;

         for(int i1 = 0; i1 < 5; ++i1) {
            k = (int)(1.5F + Mth.cos(f) * (float)i1);
            l = (int)(1.5F + Mth.sin(f) * (float)i1);
            BlockPos blockpos1 = blockpos.offset(k, j - 3 + i1 / 2, l);
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos1, treeconfiguration);
         }

         list.add(new FoliagePlacer.FoliageAttachment(blockpos.offset(k, j, l), -2, false));
      }

      return list;
   }
}
