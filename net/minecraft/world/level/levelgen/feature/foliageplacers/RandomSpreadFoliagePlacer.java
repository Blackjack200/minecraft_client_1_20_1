package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class RandomSpreadFoliagePlacer extends FoliagePlacer {
   public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(IntProvider.codec(1, 512).fieldOf("foliage_height").forGetter((randomspreadfoliageplacer1) -> randomspreadfoliageplacer1.foliageHeight), Codec.intRange(0, 256).fieldOf("leaf_placement_attempts").forGetter((randomspreadfoliageplacer) -> randomspreadfoliageplacer.leafPlacementAttempts))).apply(recordcodecbuilder_instance, RandomSpreadFoliagePlacer::new));
   private final IntProvider foliageHeight;
   private final int leafPlacementAttempts;

   public RandomSpreadFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2, int i) {
      super(intprovider, intprovider1);
      this.foliageHeight = intprovider2;
      this.leafPlacementAttempts = i;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      BlockPos blockpos = foliageplacer_foliageattachment.pos();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int i1 = 0; i1 < this.leafPlacementAttempts; ++i1) {
         blockpos_mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(k) - randomsource.nextInt(k), randomsource.nextInt(j) - randomsource.nextInt(j), randomsource.nextInt(k) - randomsource.nextInt(k));
         tryPlaceLeaf(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos_mutableblockpos);
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.foliageHeight.sample(randomsource);
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return false;
   }
}
