package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class CherryTrunkPlacer extends TrunkPlacer {
   private static final Codec<UniformInt> BRANCH_START_CODEC = ExtraCodecs.validate(UniformInt.CODEC, (uniformint) -> uniformint.getMaxValue() - uniformint.getMinValue() < 1 ? DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches") : DataResult.success(uniformint));
   public static final Codec<CherryTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(IntProvider.codec(1, 3).fieldOf("branch_count").forGetter((cherrytrunkplacer3) -> cherrytrunkplacer3.branchCount), IntProvider.codec(2, 16).fieldOf("branch_horizontal_length").forGetter((cherrytrunkplacer2) -> cherrytrunkplacer2.branchHorizontalLength), IntProvider.codec(-16, 0, BRANCH_START_CODEC).fieldOf("branch_start_offset_from_top").forGetter((cherrytrunkplacer1) -> cherrytrunkplacer1.branchStartOffsetFromTop), IntProvider.codec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter((cherrytrunkplacer) -> cherrytrunkplacer.branchEndOffsetFromTop))).apply(recordcodecbuilder_instance, CherryTrunkPlacer::new));
   private final IntProvider branchCount;
   private final IntProvider branchHorizontalLength;
   private final UniformInt branchStartOffsetFromTop;
   private final UniformInt secondBranchStartOffsetFromTop;
   private final IntProvider branchEndOffsetFromTop;

   public CherryTrunkPlacer(int i, int j, int k, IntProvider intprovider, IntProvider intprovider1, UniformInt uniformint, IntProvider intprovider2) {
      super(i, j, k);
      this.branchCount = intprovider;
      this.branchHorizontalLength = intprovider1;
      this.branchStartOffsetFromTop = uniformint;
      this.secondBranchStartOffsetFromTop = UniformInt.of(uniformint.getMinValue(), uniformint.getMaxValue() - 1);
      this.branchEndOffsetFromTop = intprovider2;
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.CHERRY_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos.below(), treeconfiguration);
      int j = Math.max(0, i - 1 + this.branchStartOffsetFromTop.sample(randomsource));
      int k = Math.max(0, i - 1 + this.secondBranchStartOffsetFromTop.sample(randomsource));
      if (k >= j) {
         ++k;
      }

      int l = this.branchCount.sample(randomsource);
      boolean flag = l == 3;
      boolean flag1 = l >= 2;
      int i1;
      if (flag) {
         i1 = i;
      } else if (flag1) {
         i1 = Math.max(j, k) + 1;
      } else {
         i1 = j + 1;
      }

      for(int l1 = 0; l1 < i1; ++l1) {
         this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos.above(l1), treeconfiguration);
      }

      List<FoliagePlacer.FoliageAttachment> list = new ArrayList<>();
      if (flag) {
         list.add(new FoliagePlacer.FoliageAttachment(blockpos.above(i1), 0, false));
      }

      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
      Function<BlockState, BlockState> function = (blockstate) -> blockstate.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis());
      list.add(this.generateBranch(levelsimulatedreader, biconsumer, randomsource, i, blockpos, treeconfiguration, function, direction, j, j < i1 - 1, blockpos_mutableblockpos));
      if (flag1) {
         list.add(this.generateBranch(levelsimulatedreader, biconsumer, randomsource, i, blockpos, treeconfiguration, function, direction.getOpposite(), k, k < i1 - 1, blockpos_mutableblockpos));
      }

      return list;
   }

   private FoliagePlacer.FoliageAttachment generateBranch(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration, Function<BlockState, BlockState> function, Direction direction, int j, boolean flag, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      blockpos_mutableblockpos.set(blockpos).move(Direction.UP, j);
      int k = i - 1 + this.branchEndOffsetFromTop.sample(randomsource);
      boolean flag1 = flag || k < j;
      int l = this.branchHorizontalLength.sample(randomsource) + (flag1 ? 1 : 0);
      BlockPos blockpos1 = blockpos.relative(direction, l).above(k);
      int i1 = flag1 ? 2 : 1;

      for(int j1 = 0; j1 < i1; ++j1) {
         this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos.move(direction), treeconfiguration, function);
      }

      Direction direction1 = blockpos1.getY() > blockpos_mutableblockpos.getY() ? Direction.UP : Direction.DOWN;

      while(true) {
         int k1 = blockpos_mutableblockpos.distManhattan(blockpos1);
         if (k1 == 0) {
            return new FoliagePlacer.FoliageAttachment(blockpos1.above(), 0, false);
         }

         float f = (float)Math.abs(blockpos1.getY() - blockpos_mutableblockpos.getY()) / (float)k1;
         boolean flag2 = randomsource.nextFloat() < f;
         blockpos_mutableblockpos.move(flag2 ? direction1 : direction);
         this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos, treeconfiguration, flag2 ? Function.identity() : function);
      }
   }
}
