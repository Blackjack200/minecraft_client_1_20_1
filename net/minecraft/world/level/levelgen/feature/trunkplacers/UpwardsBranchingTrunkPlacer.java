package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
   public static final Codec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter((upwardsbranchingtrunkplacer3) -> upwardsbranchingtrunkplacer3.extraBranchSteps), Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter((upwardsbranchingtrunkplacer2) -> upwardsbranchingtrunkplacer2.placeBranchPerLogProbability), IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter((upwardsbranchingtrunkplacer1) -> upwardsbranchingtrunkplacer1.extraBranchLength), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter((upwardsbranchingtrunkplacer) -> upwardsbranchingtrunkplacer.canGrowThrough))).apply(recordcodecbuilder_instance, UpwardsBranchingTrunkPlacer::new));
   private final IntProvider extraBranchSteps;
   private final float placeBranchPerLogProbability;
   private final IntProvider extraBranchLength;
   private final HolderSet<Block> canGrowThrough;

   public UpwardsBranchingTrunkPlacer(int i, int j, int k, IntProvider intprovider, float f, IntProvider intprovider1, HolderSet<Block> holderset) {
      super(i, j, k);
      this.extraBranchSteps = intprovider;
      this.placeBranchPerLogProbability = f;
      this.extraBranchLength = intprovider1;
      this.canGrowThrough = holderset;
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < i; ++j) {
         int k = blockpos.getY() + j;
         if (this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos.set(blockpos.getX(), k, blockpos.getZ()), treeconfiguration) && j < i - 1 && randomsource.nextFloat() < this.placeBranchPerLogProbability) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
            int l = this.extraBranchLength.sample(randomsource);
            int i1 = Math.max(0, l - this.extraBranchLength.sample(randomsource) - 1);
            int j1 = this.extraBranchSteps.sample(randomsource);
            this.placeBranch(levelsimulatedreader, biconsumer, randomsource, i, treeconfiguration, list, blockpos_mutableblockpos, k, direction, i1, j1);
         }

         if (j == i - 1) {
            list.add(new FoliagePlacer.FoliageAttachment(blockpos_mutableblockpos.set(blockpos.getX(), k + 1, blockpos.getZ()), 0, false));
         }
      }

      return list;
   }

   private void placeBranch(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, TreeConfiguration treeconfiguration, List<FoliagePlacer.FoliageAttachment> list, BlockPos.MutableBlockPos blockpos_mutableblockpos, int j, Direction direction, int k, int l) {
      int i1 = j + k;
      int j1 = blockpos_mutableblockpos.getX();
      int k1 = blockpos_mutableblockpos.getZ();

      for(int l1 = k; l1 < i && l > 0; --l) {
         if (l1 >= 1) {
            int i2 = j + l1;
            j1 += direction.getStepX();
            k1 += direction.getStepZ();
            i1 = i2;
            if (this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos.set(j1, i2, k1), treeconfiguration)) {
               i1 = i2 + 1;
            }

            list.add(new FoliagePlacer.FoliageAttachment(blockpos_mutableblockpos.immutable(), 0, false));
         }

         ++l1;
      }

      if (i1 - j > 1) {
         BlockPos blockpos = new BlockPos(j1, i1, k1);
         list.add(new FoliagePlacer.FoliageAttachment(blockpos, 0, false));
         list.add(new FoliagePlacer.FoliageAttachment(blockpos.below(2), 0, false));
      }

   }

   protected boolean validTreePos(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return super.validTreePos(levelsimulatedreader, blockpos) || levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.is(this.canGrowThrough));
   }
}
