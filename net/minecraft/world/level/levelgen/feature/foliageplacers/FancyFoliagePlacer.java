package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class FancyFoliagePlacer extends BlobFoliagePlacer {
   public static final Codec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> blobParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, FancyFoliagePlacer::new));

   public FancyFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, int i) {
      super(intprovider, intprovider1, i);
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      for(int i1 = l; i1 >= l - j; --i1) {
         int j1 = k + (i1 != l && i1 != l - j ? 1 : 0);
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, foliageplacer_foliageattachment.pos(), j1, i1, foliageplacer_foliageattachment.doubleTrunk());
      }

   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return Mth.square((float)i + 0.5F) + Mth.square((float)k + 0.5F) > (float)(l * l);
   }
}
