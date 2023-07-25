package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
   public FillLayerFeature(Codec<LayerConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<LayerConfiguration> featureplacecontext) {
      BlockPos blockpos = featureplacecontext.origin();
      LayerConfiguration layerconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            int k = blockpos.getX() + i;
            int l = blockpos.getZ() + j;
            int i1 = worldgenlevel.getMinBuildHeight() + layerconfiguration.height;
            blockpos_mutableblockpos.set(k, i1, l);
            if (worldgenlevel.getBlockState(blockpos_mutableblockpos).isAir()) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, layerconfiguration.state, 2);
            }
         }
      }

      return true;
   }
}
