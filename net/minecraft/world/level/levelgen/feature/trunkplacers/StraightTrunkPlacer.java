package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class StraightTrunkPlacer extends TrunkPlacer {
   public static final Codec<StraightTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, StraightTrunkPlacer::new));

   public StraightTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos.below(), treeconfiguration);

      for(int j = 0; j < i; ++j) {
         this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos.above(j), treeconfiguration);
      }

      return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockpos.above(i), 0, false));
   }
}
