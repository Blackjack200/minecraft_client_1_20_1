package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskFeature extends Feature<DiskConfiguration> {
   public DiskFeature(Codec<DiskConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<DiskConfiguration> featureplacecontext) {
      DiskConfiguration diskconfiguration = featureplacecontext.config();
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      boolean flag = false;
      int i = blockpos.getY();
      int j = i + diskconfiguration.halfHeight();
      int k = i - diskconfiguration.halfHeight() - 1;
      int l = diskconfiguration.radius().sample(randomsource);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-l, 0, -l), blockpos.offset(l, 0, l))) {
         int i1 = blockpos1.getX() - blockpos.getX();
         int j1 = blockpos1.getZ() - blockpos.getZ();
         if (i1 * i1 + j1 * j1 <= l * l) {
            flag |= this.placeColumn(diskconfiguration, worldgenlevel, randomsource, j, k, blockpos_mutableblockpos.set(blockpos1));
         }
      }

      return flag;
   }

   protected boolean placeColumn(DiskConfiguration diskconfiguration, WorldGenLevel worldgenlevel, RandomSource randomsource, int i, int j, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      boolean flag = false;
      BlockState blockstate = null;

      for(int k = i; k > j; --k) {
         blockpos_mutableblockpos.setY(k);
         if (diskconfiguration.target().test(worldgenlevel, blockpos_mutableblockpos)) {
            BlockState blockstate1 = diskconfiguration.stateProvider().getState(worldgenlevel, randomsource, blockpos_mutableblockpos);
            worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate1, 2);
            this.markAboveForPostProcessing(worldgenlevel, blockpos_mutableblockpos);
            flag = true;
         }
      }

      return flag;
   }
}
