package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/** @deprecated */
@Deprecated
public class LakeFeature extends Feature<LakeFeature.Configuration> {
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

   public LakeFeature(Codec<LakeFeature.Configuration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<LakeFeature.Configuration> featureplacecontext) {
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      LakeFeature.Configuration lakefeature_configuration = featureplacecontext.config();
      if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 4) {
         return false;
      } else {
         blockpos = blockpos.below(4);
         boolean[] aboolean = new boolean[2048];
         int i = randomsource.nextInt(4) + 4;

         for(int j = 0; j < i; ++j) {
            double d0 = randomsource.nextDouble() * 6.0D + 3.0D;
            double d1 = randomsource.nextDouble() * 4.0D + 2.0D;
            double d2 = randomsource.nextDouble() * 6.0D + 3.0D;
            double d3 = randomsource.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
            double d4 = randomsource.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
            double d5 = randomsource.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

            for(int k = 1; k < 15; ++k) {
               for(int l = 1; l < 15; ++l) {
                  for(int i1 = 1; i1 < 7; ++i1) {
                     double d6 = ((double)k - d3) / (d0 / 2.0D);
                     double d7 = ((double)i1 - d4) / (d1 / 2.0D);
                     double d8 = ((double)l - d5) / (d2 / 2.0D);
                     double d9 = d6 * d6 + d7 * d7 + d8 * d8;
                     if (d9 < 1.0D) {
                        aboolean[(k * 16 + l) * 8 + i1] = true;
                     }
                  }
               }
            }
         }

         BlockState blockstate = lakefeature_configuration.fluid().getState(randomsource, blockpos);

         for(int j1 = 0; j1 < 16; ++j1) {
            for(int k1 = 0; k1 < 16; ++k1) {
               for(int l1 = 0; l1 < 8; ++l1) {
                  boolean flag = !aboolean[(j1 * 16 + k1) * 8 + l1] && (j1 < 15 && aboolean[((j1 + 1) * 16 + k1) * 8 + l1] || j1 > 0 && aboolean[((j1 - 1) * 16 + k1) * 8 + l1] || k1 < 15 && aboolean[(j1 * 16 + k1 + 1) * 8 + l1] || k1 > 0 && aboolean[(j1 * 16 + (k1 - 1)) * 8 + l1] || l1 < 7 && aboolean[(j1 * 16 + k1) * 8 + l1 + 1] || l1 > 0 && aboolean[(j1 * 16 + k1) * 8 + (l1 - 1)]);
                  if (flag) {
                     BlockState blockstate1 = worldgenlevel.getBlockState(blockpos.offset(j1, l1, k1));
                     if (l1 >= 4 && blockstate1.liquid()) {
                        return false;
                     }

                     if (l1 < 4 && !blockstate1.isSolid() && worldgenlevel.getBlockState(blockpos.offset(j1, l1, k1)) != blockstate) {
                        return false;
                     }
                  }
               }
            }
         }

         for(int i2 = 0; i2 < 16; ++i2) {
            for(int j2 = 0; j2 < 16; ++j2) {
               for(int k2 = 0; k2 < 8; ++k2) {
                  if (aboolean[(i2 * 16 + j2) * 8 + k2]) {
                     BlockPos blockpos1 = blockpos.offset(i2, k2, j2);
                     if (this.canReplaceBlock(worldgenlevel.getBlockState(blockpos1))) {
                        boolean flag1 = k2 >= 4;
                        worldgenlevel.setBlock(blockpos1, flag1 ? AIR : blockstate, 2);
                        if (flag1) {
                           worldgenlevel.scheduleTick(blockpos1, AIR.getBlock(), 0);
                           this.markAboveForPostProcessing(worldgenlevel, blockpos1);
                        }
                     }
                  }
               }
            }
         }

         BlockState blockstate2 = lakefeature_configuration.barrier().getState(randomsource, blockpos);
         if (!blockstate2.isAir()) {
            for(int l2 = 0; l2 < 16; ++l2) {
               for(int i3 = 0; i3 < 16; ++i3) {
                  for(int j3 = 0; j3 < 8; ++j3) {
                     boolean flag2 = !aboolean[(l2 * 16 + i3) * 8 + j3] && (l2 < 15 && aboolean[((l2 + 1) * 16 + i3) * 8 + j3] || l2 > 0 && aboolean[((l2 - 1) * 16 + i3) * 8 + j3] || i3 < 15 && aboolean[(l2 * 16 + i3 + 1) * 8 + j3] || i3 > 0 && aboolean[(l2 * 16 + (i3 - 1)) * 8 + j3] || j3 < 7 && aboolean[(l2 * 16 + i3) * 8 + j3 + 1] || j3 > 0 && aboolean[(l2 * 16 + i3) * 8 + (j3 - 1)]);
                     if (flag2 && (j3 < 4 || randomsource.nextInt(2) != 0)) {
                        BlockState blockstate3 = worldgenlevel.getBlockState(blockpos.offset(l2, j3, i3));
                        if (blockstate3.isSolid() && !blockstate3.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                           BlockPos blockpos2 = blockpos.offset(l2, j3, i3);
                           worldgenlevel.setBlock(blockpos2, blockstate2, 2);
                           this.markAboveForPostProcessing(worldgenlevel, blockpos2);
                        }
                     }
                  }
               }
            }
         }

         if (blockstate.getFluidState().is(FluidTags.WATER)) {
            for(int k3 = 0; k3 < 16; ++k3) {
               for(int l3 = 0; l3 < 16; ++l3) {
                  int i4 = 4;
                  BlockPos blockpos3 = blockpos.offset(k3, 4, l3);
                  if (worldgenlevel.getBiome(blockpos3).value().shouldFreeze(worldgenlevel, blockpos3, false) && this.canReplaceBlock(worldgenlevel.getBlockState(blockpos3))) {
                     worldgenlevel.setBlock(blockpos3, Blocks.ICE.defaultBlockState(), 2);
                  }
               }
            }
         }

         return true;
      }
   }

   private boolean canReplaceBlock(BlockState blockstate) {
      return !blockstate.is(BlockTags.FEATURES_CANNOT_REPLACE);
   }

   public static record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {
      public static final Codec<LakeFeature.Configuration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("fluid").forGetter(LakeFeature.Configuration::fluid), BlockStateProvider.CODEC.fieldOf("barrier").forGetter(LakeFeature.Configuration::barrier)).apply(recordcodecbuilder_instance, LakeFeature.Configuration::new));
   }
}
