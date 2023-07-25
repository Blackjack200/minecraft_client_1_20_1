package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class BambooFeature extends Feature<ProbabilityFeatureConfiguration> {
   private static final BlockState BAMBOO_TRUNK = Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.AGE, Integer.valueOf(1)).setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE).setValue(BambooStalkBlock.STAGE, Integer.valueOf(0));
   private static final BlockState BAMBOO_FINAL_LARGE = BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooStalkBlock.STAGE, Integer.valueOf(1));
   private static final BlockState BAMBOO_TOP_LARGE = BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE);
   private static final BlockState BAMBOO_TOP_SMALL = BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL);

   public BambooFeature(Codec<ProbabilityFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> featureplacecontext) {
      int i = 0;
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      ProbabilityFeatureConfiguration probabilityfeatureconfiguration = featureplacecontext.config();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = blockpos.mutable();
      if (worldgenlevel.isEmptyBlock(blockpos_mutableblockpos)) {
         if (Blocks.BAMBOO.defaultBlockState().canSurvive(worldgenlevel, blockpos_mutableblockpos)) {
            int j = randomsource.nextInt(12) + 5;
            if (randomsource.nextFloat() < probabilityfeatureconfiguration.probability) {
               int k = randomsource.nextInt(4) + 1;

               for(int l = blockpos.getX() - k; l <= blockpos.getX() + k; ++l) {
                  for(int i1 = blockpos.getZ() - k; i1 <= blockpos.getZ() + k; ++i1) {
                     int j1 = l - blockpos.getX();
                     int k1 = i1 - blockpos.getZ();
                     if (j1 * j1 + k1 * k1 <= k * k) {
                        blockpos_mutableblockpos1.set(l, worldgenlevel.getHeight(Heightmap.Types.WORLD_SURFACE, l, i1) - 1, i1);
                        if (isDirt(worldgenlevel.getBlockState(blockpos_mutableblockpos1))) {
                           worldgenlevel.setBlock(blockpos_mutableblockpos1, Blocks.PODZOL.defaultBlockState(), 2);
                        }
                     }
                  }
               }
            }

            for(int l1 = 0; l1 < j && worldgenlevel.isEmptyBlock(blockpos_mutableblockpos); ++l1) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, BAMBOO_TRUNK, 2);
               blockpos_mutableblockpos.move(Direction.UP, 1);
            }

            if (blockpos_mutableblockpos.getY() - blockpos.getY() >= 3) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, BAMBOO_FINAL_LARGE, 2);
               worldgenlevel.setBlock(blockpos_mutableblockpos.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
               worldgenlevel.setBlock(blockpos_mutableblockpos.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
            }
         }

         ++i;
      }

      return i > 0;
   }
}
