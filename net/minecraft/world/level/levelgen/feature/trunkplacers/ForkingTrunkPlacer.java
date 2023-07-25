package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
   public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, ForkingTrunkPlacer::new));

   public ForkingTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.FORKING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos.below(), treeconfiguration);
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
      int j = i - randomsource.nextInt(4) - 1;
      int k = 3 - randomsource.nextInt(3);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int l = blockpos.getX();
      int i1 = blockpos.getZ();
      OptionalInt optionalint = OptionalInt.empty();

      for(int j1 = 0; j1 < i; ++j1) {
         int k1 = blockpos.getY() + j1;
         if (j1 >= j && k > 0) {
            l += direction.getStepX();
            i1 += direction.getStepZ();
            --k;
         }

         if (this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos.set(l, k1, i1), treeconfiguration)) {
            optionalint = OptionalInt.of(k1 + 1);
         }
      }

      if (optionalint.isPresent()) {
         list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalint.getAsInt(), i1), 1, false));
      }

      l = blockpos.getX();
      i1 = blockpos.getZ();
      Direction direction1 = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
      if (direction1 != direction) {
         int l1 = j - randomsource.nextInt(2) - 1;
         int i2 = 1 + randomsource.nextInt(3);
         optionalint = OptionalInt.empty();

         for(int j2 = l1; j2 < i && i2 > 0; --i2) {
            if (j2 >= 1) {
               int k2 = blockpos.getY() + j2;
               l += direction1.getStepX();
               i1 += direction1.getStepZ();
               if (this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos_mutableblockpos.set(l, k2, i1), treeconfiguration)) {
                  optionalint = OptionalInt.of(k2 + 1);
               }
            }

            ++j2;
         }

         if (optionalint.isPresent()) {
            list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalint.getAsInt(), i1), 0, false));
         }
      }

      return list;
   }
}
