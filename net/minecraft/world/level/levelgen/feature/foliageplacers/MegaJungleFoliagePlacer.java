package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaJungleFoliagePlacer extends FoliagePlacer {
   public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> foliagePlacerParts(recordcodecbuilder_instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((megajunglefoliageplacer) -> megajunglefoliageplacer.height)).apply(recordcodecbuilder_instance, MegaJungleFoliagePlacer::new));
   protected final int height;

   public MegaJungleFoliagePlacer(IntProvider intprovider, IntProvider intprovider1, int i) {
      super(intprovider, intprovider1);
      this.height = i;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader levelsimulatedreader, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, RandomSource randomsource, TreeConfiguration treeconfiguration, int i, FoliagePlacer.FoliageAttachment foliageplacer_foliageattachment, int j, int k, int l) {
      int i1 = foliageplacer_foliageattachment.doubleTrunk() ? j : 1 + randomsource.nextInt(2);

      for(int j1 = l; j1 >= l - i1; --j1) {
         int k1 = k + foliageplacer_foliageattachment.radiusOffset() + 1 - j1;
         this.placeLeavesRow(levelsimulatedreader, foliageplacer_foliagesetter, randomsource, treeconfiguration, foliageplacer_foliageattachment.pos(), k1, j1, foliageplacer_foliageattachment.doubleTrunk());
      }

   }

   public int foliageHeight(RandomSource randomsource, int i, TreeConfiguration treeconfiguration) {
      return this.height;
   }

   protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
      if (i + k >= 7) {
         return true;
      } else {
         return i * i + k * k > l * l;
      }
   }
}
