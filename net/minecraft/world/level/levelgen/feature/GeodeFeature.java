package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

public class GeodeFeature extends Feature<GeodeConfiguration> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public GeodeFeature(Codec<GeodeConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<GeodeConfiguration> featureplacecontext) {
      GeodeConfiguration geodeconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      int i = geodeconfiguration.minGenOffset;
      int j = geodeconfiguration.maxGenOffset;
      List<Pair<BlockPos, Integer>> list = Lists.newLinkedList();
      int k = geodeconfiguration.distributionPoints.sample(randomsource);
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(worldgenlevel.getSeed()));
      NormalNoise normalnoise = NormalNoise.create(worldgenrandom, -4, 1.0D);
      List<BlockPos> list1 = Lists.newLinkedList();
      double d0 = (double)k / (double)geodeconfiguration.outerWallDistance.getMaxValue();
      GeodeLayerSettings geodelayersettings = geodeconfiguration.geodeLayerSettings;
      GeodeBlockSettings geodeblocksettings = geodeconfiguration.geodeBlockSettings;
      GeodeCrackSettings geodecracksettings = geodeconfiguration.geodeCrackSettings;
      double d1 = 1.0D / Math.sqrt(geodelayersettings.filling);
      double d2 = 1.0D / Math.sqrt(geodelayersettings.innerLayer + d0);
      double d3 = 1.0D / Math.sqrt(geodelayersettings.middleLayer + d0);
      double d4 = 1.0D / Math.sqrt(geodelayersettings.outerLayer + d0);
      double d5 = 1.0D / Math.sqrt(geodecracksettings.baseCrackSize + randomsource.nextDouble() / 2.0D + (k > 3 ? d0 : 0.0D));
      boolean flag = (double)randomsource.nextFloat() < geodecracksettings.generateCrackChance;
      int l = 0;

      for(int i1 = 0; i1 < k; ++i1) {
         int j1 = geodeconfiguration.outerWallDistance.sample(randomsource);
         int k1 = geodeconfiguration.outerWallDistance.sample(randomsource);
         int l1 = geodeconfiguration.outerWallDistance.sample(randomsource);
         BlockPos blockpos1 = blockpos.offset(j1, k1, l1);
         BlockState blockstate = worldgenlevel.getBlockState(blockpos1);
         if (blockstate.isAir() || blockstate.is(BlockTags.GEODE_INVALID_BLOCKS)) {
            ++l;
            if (l > geodeconfiguration.invalidBlocksThreshold) {
               return false;
            }
         }

         list.add(Pair.of(blockpos1, geodeconfiguration.pointOffset.sample(randomsource)));
      }

      if (flag) {
         int i2 = randomsource.nextInt(4);
         int j2 = k * 2 + 1;
         if (i2 == 0) {
            list1.add(blockpos.offset(j2, 7, 0));
            list1.add(blockpos.offset(j2, 5, 0));
            list1.add(blockpos.offset(j2, 1, 0));
         } else if (i2 == 1) {
            list1.add(blockpos.offset(0, 7, j2));
            list1.add(blockpos.offset(0, 5, j2));
            list1.add(blockpos.offset(0, 1, j2));
         } else if (i2 == 2) {
            list1.add(blockpos.offset(j2, 7, j2));
            list1.add(blockpos.offset(j2, 5, j2));
            list1.add(blockpos.offset(j2, 1, j2));
         } else {
            list1.add(blockpos.offset(0, 7, 0));
            list1.add(blockpos.offset(0, 5, 0));
            list1.add(blockpos.offset(0, 1, 0));
         }
      }

      List<BlockPos> list2 = Lists.newArrayList();
      Predicate<BlockState> predicate = isReplaceable(geodeconfiguration.geodeBlockSettings.cannotReplace);

      for(BlockPos blockpos2 : BlockPos.betweenClosed(blockpos.offset(i, i, i), blockpos.offset(j, j, j))) {
         double d6 = normalnoise.getValue((double)blockpos2.getX(), (double)blockpos2.getY(), (double)blockpos2.getZ()) * geodeconfiguration.noiseMultiplier;
         double d7 = 0.0D;
         double d8 = 0.0D;

         for(Pair<BlockPos, Integer> pair : list) {
            d7 += Mth.invSqrt(blockpos2.distSqr(pair.getFirst()) + (double)pair.getSecond().intValue()) + d6;
         }

         for(BlockPos blockpos3 : list1) {
            d8 += Mth.invSqrt(blockpos2.distSqr(blockpos3) + (double)geodecracksettings.crackPointOffset) + d6;
         }

         if (!(d7 < d4)) {
            if (flag && d8 >= d5 && d7 < d1) {
               this.safeSetBlock(worldgenlevel, blockpos2, Blocks.AIR.defaultBlockState(), predicate);

               for(Direction direction : DIRECTIONS) {
                  BlockPos blockpos4 = blockpos2.relative(direction);
                  FluidState fluidstate = worldgenlevel.getFluidState(blockpos4);
                  if (!fluidstate.isEmpty()) {
                     worldgenlevel.scheduleTick(blockpos4, fluidstate.getType(), 0);
                  }
               }
            } else if (d7 >= d1) {
               this.safeSetBlock(worldgenlevel, blockpos2, geodeblocksettings.fillingProvider.getState(randomsource, blockpos2), predicate);
            } else if (d7 >= d2) {
               boolean flag1 = (double)randomsource.nextFloat() < geodeconfiguration.useAlternateLayer0Chance;
               if (flag1) {
                  this.safeSetBlock(worldgenlevel, blockpos2, geodeblocksettings.alternateInnerLayerProvider.getState(randomsource, blockpos2), predicate);
               } else {
                  this.safeSetBlock(worldgenlevel, blockpos2, geodeblocksettings.innerLayerProvider.getState(randomsource, blockpos2), predicate);
               }

               if ((!geodeconfiguration.placementsRequireLayer0Alternate || flag1) && (double)randomsource.nextFloat() < geodeconfiguration.usePotentialPlacementsChance) {
                  list2.add(blockpos2.immutable());
               }
            } else if (d7 >= d3) {
               this.safeSetBlock(worldgenlevel, blockpos2, geodeblocksettings.middleLayerProvider.getState(randomsource, blockpos2), predicate);
            } else if (d7 >= d4) {
               this.safeSetBlock(worldgenlevel, blockpos2, geodeblocksettings.outerLayerProvider.getState(randomsource, blockpos2), predicate);
            }
         }
      }

      List<BlockState> list3 = geodeblocksettings.innerPlacements;

      for(BlockPos blockpos5 : list2) {
         BlockState blockstate1 = Util.getRandom(list3, randomsource);

         for(Direction direction1 : DIRECTIONS) {
            if (blockstate1.hasProperty(BlockStateProperties.FACING)) {
               blockstate1 = blockstate1.setValue(BlockStateProperties.FACING, direction1);
            }

            BlockPos blockpos6 = blockpos5.relative(direction1);
            BlockState blockstate2 = worldgenlevel.getBlockState(blockpos6);
            if (blockstate1.hasProperty(BlockStateProperties.WATERLOGGED)) {
               blockstate1 = blockstate1.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(blockstate2.getFluidState().isSource()));
            }

            if (BuddingAmethystBlock.canClusterGrowAtState(blockstate2)) {
               this.safeSetBlock(worldgenlevel, blockpos6, blockstate1, predicate);
               break;
            }
         }
      }

      return true;
   }
}
