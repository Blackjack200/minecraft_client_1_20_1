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

public class GiantTrunkPlacer extends TrunkPlacer {
   public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, GiantTrunkPlacer::new));

   public GiantTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.GIANT_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      BlockPos blockpos1 = blockpos.below();
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1, treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.east(), treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.south(), treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.south().east(), treeconfiguration);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < i; ++j) {
         this.placeLogIfFreeWithOffset(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration, blockpos, 0, j, 0);
         if (j < i - 1) {
            this.placeLogIfFreeWithOffset(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration, blockpos, 1, j, 0);
            this.placeLogIfFreeWithOffset(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration, blockpos, 1, j, 1);
            this.placeLogIfFreeWithOffset(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration, blockpos, 0, j, 1);
         }
      }

      return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockpos.above(i), 0, true));
   }

   private void placeLogIfFreeWithOffset(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, TreeConfiguration treeconfiguration, BlockPos blockpos, int i, int j, int k) {
      blockpos_mutableblockpos.setWithOffset(blockpos, i, j, k);
      this.placeLogIfFree(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration);
   }
}
