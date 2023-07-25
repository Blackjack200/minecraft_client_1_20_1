package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
   public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockColumnConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockColumnConfiguration blockcolumnconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      int i = blockcolumnconfiguration.layers().size();
      int[] aint = new int[i];
      int j = 0;

      for(int k = 0; k < i; ++k) {
         aint[k] = blockcolumnconfiguration.layers().get(k).height().sample(randomsource);
         j += aint[k];
      }

      if (j == 0) {
         return false;
      } else {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = featureplacecontext.origin().mutable();
         BlockPos.MutableBlockPos blockpos_mutableblockpos1 = blockpos_mutableblockpos.mutable().move(blockcolumnconfiguration.direction());

         for(int l = 0; l < j; ++l) {
            if (!blockcolumnconfiguration.allowedPlacement().test(worldgenlevel, blockpos_mutableblockpos1)) {
               truncate(aint, j, l, blockcolumnconfiguration.prioritizeTip());
               break;
            }

            blockpos_mutableblockpos1.move(blockcolumnconfiguration.direction());
         }

         for(int i1 = 0; i1 < i; ++i1) {
            int j1 = aint[i1];
            if (j1 != 0) {
               BlockColumnConfiguration.Layer blockcolumnconfiguration_layer = blockcolumnconfiguration.layers().get(i1);

               for(int k1 = 0; k1 < j1; ++k1) {
                  worldgenlevel.setBlock(blockpos_mutableblockpos, blockcolumnconfiguration_layer.state().getState(randomsource, blockpos_mutableblockpos), 2);
                  blockpos_mutableblockpos.move(blockcolumnconfiguration.direction());
               }
            }
         }

         return true;
      }
   }

   private static void truncate(int[] aint, int i, int j, boolean flag) {
      int k = i - j;
      int l = flag ? 1 : -1;
      int i1 = flag ? 0 : aint.length - 1;
      int j1 = flag ? aint.length : -1;

      for(int k1 = i1; k1 != j1 && k > 0; k1 += l) {
         int l1 = aint[k1];
         int i2 = Math.min(l1, k);
         k -= i2;
         aint[k1] -= i2;
      }

   }
}
