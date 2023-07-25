package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class DarkOakTrunkPlacer extends TrunkPlacer {
   public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, DarkOakTrunkPlacer::new));

   public DarkOakTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      BlockPos blockpos1 = blockpos.below();
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1, treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.east(), treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.south(), treeconfiguration);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos1.south().east(), treeconfiguration);
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
      int j = i - randomsource.nextInt(4);
      int k = 2 - randomsource.nextInt(3);
      int l = blockpos.getX();
      int i1 = blockpos.getY();
      int j1 = blockpos.getZ();
      int k1 = l;
      int l1 = j1;
      int i2 = i1 + i - 1;

      for(int j2 = 0; j2 < i; ++j2) {
         if (j2 >= j && k > 0) {
            k1 += direction.getStepX();
            l1 += direction.getStepZ();
            --k;
         }

         int k2 = i1 + j2;
         BlockPos blockpos2 = new BlockPos(k1, k2, l1);
         if (TreeFeature.isAirOrLeaves(levelsimulatedreader, blockpos2)) {
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos2, treeconfiguration);
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos2.east(), treeconfiguration);
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos2.south(), treeconfiguration);
            this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos2.east().south(), treeconfiguration);
         }
      }

      list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k1, i2, l1), 0, true));

      for(int l2 = -1; l2 <= 2; ++l2) {
         for(int i3 = -1; i3 <= 2; ++i3) {
            if ((l2 < 0 || l2 > 1 || i3 < 0 || i3 > 1) && randomsource.nextInt(3) <= 0) {
               int j3 = randomsource.nextInt(3) + 2;

               for(int k3 = 0; k3 < j3; ++k3) {
                  this.placeLog(levelsimulatedreader, biconsumer, randomsource, new BlockPos(l + l2, i2 - k3 - 1, j1 + i3), treeconfiguration);
               }

               list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k1 + l2, i2, l1 + i3), 0, false));
            }
         }
      }

      return list;
   }
}
