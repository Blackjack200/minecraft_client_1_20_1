package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature extends Feature<OreConfiguration> {
   private static final int MAX_DIST_FROM_ORIGIN = 7;

   ScatteredOreFeature(Codec<OreConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<OreConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      OreConfiguration oreconfiguration = featureplacecontext.config();
      BlockPos blockpos = featureplacecontext.origin();
      int i = randomsource.nextInt(oreconfiguration.size + 1);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < i; ++j) {
         this.offsetTargetPos(blockpos_mutableblockpos, randomsource, blockpos, Math.min(j, 7));
         BlockState blockstate = worldgenlevel.getBlockState(blockpos_mutableblockpos);

         for(OreConfiguration.TargetBlockState oreconfiguration_targetblockstate : oreconfiguration.targetStates) {
            if (OreFeature.canPlaceOre(blockstate, worldgenlevel::getBlockState, randomsource, oreconfiguration, oreconfiguration_targetblockstate, blockpos_mutableblockpos)) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, oreconfiguration_targetblockstate.state, 2);
               break;
            }
         }
      }

      return true;
   }

   private void offsetTargetPos(BlockPos.MutableBlockPos blockpos_mutableblockpos, RandomSource randomsource, BlockPos blockpos, int i) {
      int j = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);
      int k = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);
      int l = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);
      blockpos_mutableblockpos.setWithOffset(blockpos, j, k, l);
   }

   private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource randomsource, int i) {
      return Math.round((randomsource.nextFloat() - randomsource.nextFloat()) * (float)i);
   }
}
