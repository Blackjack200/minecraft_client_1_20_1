package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
   public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter((sprucefoliageplacer) -> sprucefoliageplacer.trunkHeight)).apply(recordcodecbuilder_instance, SpruceFoliagePlacer::new));
   private final IntProvider trunkHeight;

   public SpruceFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2) {
      super(intprovider, intprovider1);
      this.trunkHeight = intprovider2;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      BlockPos blockpos = foliageplacer_foliageattachment.pos();
      int i1 = randomsource.nextInt(2);
      int j1 = 1;
      int k1 = 0;

      for(int l1 = l; l1 >= -j; --l1) {
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1, l1, foliageplacer_foliageattachment.doubleTrunk());
         if (i1 >= j1) {
            i1 = k1;
            k1 = 1;
            j1 = Math.min(j1 + 1, k + foliageplacer_foliageattachment.radiusOffset());
         } else {
            ++i1;
         }
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return Math.max(4, i - this.trunkHeight.sample(randomsource));
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return i == l && k == l && l > 0;
   }
}
