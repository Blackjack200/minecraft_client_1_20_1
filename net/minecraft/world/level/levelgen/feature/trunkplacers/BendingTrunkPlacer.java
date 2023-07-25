package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class BendingTrunkPlacer extends TrunkPlacer {
   public static final Codec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter((bendingtrunkplacer1) -> bendingtrunkplacer1.minHeightForLeaves), IntProvider.codec(1, 64).fieldOf("bend_length").forGetter((bendingtrunkplacer) -> bendingtrunkplacer.bendLength))).apply(recordcodecbuilder_instance, BendingTrunkPlacer::new));
   private final int minHeightForLeaves;
   private final IntProvider bendLength;

   public BendingTrunkPlacer(int i, int j, int k, int l, IntProvider intprovider) {
      super(i, j, k);
      this.minHeightForLeaves = l;
      this.bendLength = intprovider;
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.BENDING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
      int j = i - 1;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      BlockPos blockpos1 = blockpos_mutableblockpos.below();
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1, treeconfiguration);
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();

      for(int k = 0; k <= j; ++k) {
         if (k + 1 >= j + randomsource.nextInt(2)) {
            blockpos_mutableblockpos.move(direction);
         }

         if (TreeFeature.validTreePos(levelsimulatedreader, blockpos_mutableblockpos)) {
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration);
         }

         if (k >= this.minHeightForLeaves) {
            list.add(new FoliagePlacer.FoliageAttachment(blockpos_mutableblockpos.immutable(), 0, false));
         }

         blockpos_mutableblockpos.move(Direction.UP);
      }

      int l = this.bendLength.sample(randomsource);

      for(int i1 = 0; i1 <= l; ++i1) {
         if (TreeFeature.validTreePos(levelsimulatedreader, blockpos_mutableblockpos)) {
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration);
         }

         list.add(new FoliagePlacer.FoliageAttachment(blockpos_mutableblockpos.immutable(), 0, false));
         blockpos_mutableblockpos.move(direction);
      }

      return list;
   }
}
