package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakFoliagePlacer extends FoliagePlacer {
   public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, DarkOakFoliagePlacer::new));

   public DarkOakFoliagePlacer(IntProvider intprovider, IntProvider intprovider1) {
      super(intprovider, intprovider1);
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      BlockPos blockpos = foliageplacer_foliageattachment.pos().above(l);
      boolean flag = foliageplacer_foliageattachment.doubleTrunk();
      if (flag) {
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + 2, -1, flag);
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + 3, 0, flag);
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + 2, 1, flag);
         if (randomsource.nextBoolean()) {
            this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k, 2, flag);
         }
      } else {
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + 2, -1, flag);
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + 1, 0, flag);
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return 4;
   }

   protected boolean shouldSkipLocationSigned(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return j != 0 || !flag || i != -l && i < l || k != -l && k < l ? super.shouldSkipLocationSigned(randomsource, i, j, k, l, flag) : true;
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      if (j == -1 && !flag) {
         return i == l && k == l;
      } else if (j == 1) {
         return i + k > l * 2 - 2;
      } else {
         return false;
      }
   }
}
