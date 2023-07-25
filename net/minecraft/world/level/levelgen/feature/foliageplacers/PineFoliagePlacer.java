package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
   public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(IntProvider.codec(0, 24).fieldOf("height").forGetter((pinefoliageplacer) -> pinefoliageplacer.height)).apply(recordcodecbuilder_instance, PineFoliagePlacer::new));
   private final IntProvider height;

   public PineFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2) {
      super(intprovider, intprovider1);
      this.height = intprovider2;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.PINE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      int i1 = 0;

      for(int j1 = l; j1 >= l - j; --j1) {
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, foliageplacer_foliageattachment.pos(), i1, j1, foliageplacer_foliageattachment.doubleTrunk());
         if (i1 >= 1 && j1 == l - j + 1) {
            --i1;
         } else if (i1 < k + foliageplacer_foliageattachment.radiusOffset()) {
            ++i1;
         }
      }

   }

   public int foliageRadius(RandomSource randomsource, int i) {
      return super.foliageRadius(randomsource, i) + randomsource.nextInt(Math.max(i + 1, 1));
   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.height.sample(randomsource);
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return i == l && k == l && l > 0;
   }
}
