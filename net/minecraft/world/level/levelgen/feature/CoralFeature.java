package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
   public CoralFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      RandomSource randomsource = featureplacecontext.random();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      Optional<Block> optional = BuiltInRegistries.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap((holderset_named) -> holderset_named.getRandomElement(randomsource)).map(Holder::value);
      return optional.isEmpty() ? false : this.placeFeature(worldgenlevel, randomsource, blockpos, optional.get().defaultBlockState());
   }

   protected abstract boolean placeFeature(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate);

   protected boolean placeCoralBlock(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate1 = levelaccessor.getBlockState(blockpos);
      if ((blockstate1.is(Blocks.WATER) || blockstate1.is(BlockTags.CORALS)) && levelaccessor.getBlockState(blockpos1).is(Blocks.WATER)) {
         levelaccessor.setBlock(blockpos, blockstate, 3);
         if (randomsource.nextFloat() < 0.25F) {
            BuiltInRegistries.BLOCK.getTag(BlockTags.CORALS).flatMap((holderset_named1) -> holderset_named1.getRandomElement(randomsource)).map(Holder::value).ifPresent((block1) -> levelaccessor.setBlock(blockpos1, block1.defaultBlockState(), 2));
         } else if (randomsource.nextFloat() < 0.05F) {
            levelaccessor.setBlock(blockpos1, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(randomsource.nextInt(4) + 1)), 2);
         }

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (randomsource.nextFloat() < 0.2F) {
               BlockPos blockpos2 = blockpos.relative(direction);
               if (levelaccessor.getBlockState(blockpos2).is(Blocks.WATER)) {
                  BuiltInRegistries.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap((holderset_named) -> holderset_named.getRandomElement(randomsource)).map(Holder::value).ifPresent((block) -> {
                     BlockState blockstate2 = block.defaultBlockState();
                     if (blockstate2.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockstate2 = blockstate2.setValue(BaseCoralWallFanBlock.FACING, direction);
                     }

                     levelaccessor.setBlock(blockpos2, blockstate2, 2);
                  });
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
