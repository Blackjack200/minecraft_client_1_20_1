package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
   public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(IntProvider.codec(0, 24).fieldOf("crown_height").forGetter((megapinefoliageplacer) -> megapinefoliageplacer.crownHeight)).apply(recordcodecbuilder_instance, MegaPineFoliagePlacer::new));
   private final IntProvider crownHeight;

   public MegaPineFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2) {
      super(intprovider, intprovider1);
      this.crownHeight = intprovider2;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      BlockPos blockpos = foliageplacer_foliageattachment.pos();
      int i1 = 0;

      for(int j1 = blockpos.getY() - j + l; j1 <= blockpos.getY() + l; ++j1) {
         int k1 = blockpos.getY() - j1;
         int l1 = k + foliageplacer_foliageattachment.radiusOffset() + Mth.floor((float)k1 / (float)j * 3.5F);
         int i2;
         if (k1 > 0 && l1 == i1 && (j1 & 1) == 0) {
            i2 = l1 + 1;
         } else {
            i2 = l1;
         }

         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, new BlockPos(blockpos.getX(), j1, blockpos.getZ()), i2, 0, foliageplacer_foliageattachment.doubleTrunk());
         i1 = l1;
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.crownHeight.sample(randomsource);
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      if (i + k >= 7) {
         return true;
      } else {
         return i * i + k * k > l * l;
      }
   }
}
