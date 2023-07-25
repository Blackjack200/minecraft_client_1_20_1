package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
   public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, AcaciaFoliagePlacer::new));

   public AcaciaFoliagePlacer(IntProvider intprovider, IntProvider intprovider1) {
      super(intprovider, intprovider1);
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      boolean flag = foliageplacer_foliageattachment.doubleTrunk();
      BlockPos blockpos = foliageplacer_foliageattachment.pos().above(l);
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + foliageplacer_foliageattachment.radiusOffset(), -1 - j, flag);
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k - 1, -j, flag);
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, k + foliageplacer_foliageattachment.radiusOffset() - 1, 0, flag);
   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return 0;
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      if (j == 0) {
         return (i > 1 || k > 1) && i != 0 && k != 0;
      } else {
         return i == l && k == l && l > 0;
      }
   }
}
