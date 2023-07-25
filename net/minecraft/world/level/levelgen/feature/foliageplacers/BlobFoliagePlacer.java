package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
   public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> blobParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, BlobFoliagePlacer::new));
   protected final int height;

   protected static <P extends BlobFoliagePlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider, Integer> blobParts(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return foliagePlacerParts(recordcodecbuilder_instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((blobfoliageplacer) -> blobfoliageplacer.height));
   }

   public BlobFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, int i) {
      super(intprovider, intprovider1);
      this.height = i;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      for(int i1 = l; i1 >= l - j; --i1) {
         int j1 = Math.max(k + foliageplacer_foliageattachment.radiusOffset() - 1 - i1 / 2, 0);
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, foliageplacer_foliageattachment.pos(), j1, i1, foliageplacer_foliageattachment.doubleTrunk());
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.height;
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      return i == l && k == l && (randomsource.nextInt(2) == 0 || j == 0);
   }
}
