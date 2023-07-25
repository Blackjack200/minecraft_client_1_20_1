package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class CherryFoliagePlacer extends FoliagePlacer {
   public static final Codec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(IntProvider.codec(4, 16).fieldOf("height").forGetter((cherryfoliageplacer4) -> cherryfoliageplacer4.height), Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter((cherryfoliageplacer3) -> cherryfoliageplacer3.wideBottomLayerHoleChance), Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter((cherryfoliageplacer2) -> cherryfoliageplacer2.wideBottomLayerHoleChance), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter((cherryfoliageplacer1) -> cherryfoliageplacer1.hangingLeavesChance), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter((cherryfoliageplacer) -> cherryfoliageplacer.hangingLeavesExtensionChance))).apply(recordcodecbuilder_instance, CherryFoliagePlacer::new));
   private final IntProvider height;
   private final float wideBottomLayerHoleChance;
   private final float cornerHoleChance;
   private final float hangingLeavesChance;
   private final float hangingLeavesExtensionChance;

   public CherryFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2, float f, float f1, float f2, float f3) {
      super(intprovider, intprovider1);
      this.height = intprovider2;
      this.wideBottomLayerHoleChance = f;
      this.cornerHoleChance = f1;
      this.hangingLeavesChance = f2;
      this.hangingLeavesExtensionChance = f3;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      boolean flag = foliageplacer_foliageattachment.doubleTrunk();
      BlockPos blockpos = foliageplacer_foliageattachment.pos().above(l);
      int i1 = k + foliageplacer_foliageattachment.radiusOffset() - 1;
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1 - 2, j - 3, flag);
      this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1 - 1, j - 4, flag);

      for(int j1 = j - 5; j1 >= 0; --j1) {
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1, j1, flag);
      }

      this.placeLeavesRowWithHangingLeavesBelow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1, -1, flag, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
      this.placeLeavesRowWithHangingLeavesBelow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, blockpos, i1 - 1, -2, flag, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.height.sample(randomsource);
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      if (j == -1 && (i == l || k == l) && randomsource.nextFloat() < this.wideBottomLayerHoleChance) {
         return true;
      } else {
         boolean flag1 = i == l && k == l;
         boolean flag2 = l > 2;
         if (flag2) {
            return flag1 || i + k > l * 2 - 2 && randomsource.nextFloat() < this.cornerHoleChance;
         } else {
            return flag1 && randomsource.nextFloat() < this.cornerHoleChance;
         }
      }
   }
}
