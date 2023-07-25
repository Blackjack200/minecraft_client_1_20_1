package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
   public boolean growTree(ServerLevel serverlevel, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockState blockstate, RandomSource randomsource) {
      for(int i = 0; i >= -1; --i) {
         for(int j = 0; j >= -1; --j) {
            if (isTwoByTwoSapling(blockstate, serverlevel, blockpos, i, j)) {
               return this.placeMega(serverlevel, chunkgenerator, blockpos, blockstate, randomsource, i, j);
            }
         }
      }

      return super.growTree(serverlevel, chunkgenerator, blockpos, blockstate, randomsource);
   }

   @Nullable
   protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomsource);

   public boolean placeMega(ServerLevel serverlevel, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockState blockstate, RandomSource randomsource, int i, int j) {
      ResourceKey<ConfiguredFeature<?, ?>> resourcekey = this.getConfiguredMegaFeature(randomsource);
      if (resourcekey == null) {
         return false;
      } else {
         Holder<ConfiguredFeature<?, ?>> holder = serverlevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey).orElse((Holder.Reference<ConfiguredFeature<?, ?>>)null);
         if (holder == null) {
            return false;
         } else {
            ConfiguredFeature<?, ?> configuredfeature = holder.value();
            BlockState blockstate1 = Blocks.AIR.defaultBlockState();
            serverlevel.setBlock(blockpos.offset(i, 0, j), blockstate1, 4);
            serverlevel.setBlock(blockpos.offset(i + 1, 0, j), blockstate1, 4);
            serverlevel.setBlock(blockpos.offset(i, 0, j + 1), blockstate1, 4);
            serverlevel.setBlock(blockpos.offset(i + 1, 0, j + 1), blockstate1, 4);
            if (configuredfeature.place(serverlevel, chunkgenerator, randomsource, blockpos.offset(i, 0, j))) {
               return true;
            } else {
               serverlevel.setBlock(blockpos.offset(i, 0, j), blockstate, 4);
               serverlevel.setBlock(blockpos.offset(i + 1, 0, j), blockstate, 4);
               serverlevel.setBlock(blockpos.offset(i, 0, j + 1), blockstate, 4);
               serverlevel.setBlock(blockpos.offset(i + 1, 0, j + 1), blockstate, 4);
               return false;
            }
         }
      }
   }

   public static boolean isTwoByTwoSapling(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, int i, int j) {
      Block block = blockstate.getBlock();
      return blockgetter.getBlockState(blockpos.offset(i, 0, j)).is(block) && blockgetter.getBlockState(blockpos.offset(i + 1, 0, j)).is(block) && blockgetter.getBlockState(blockpos.offset(i, 0, j + 1)).is(block) && blockgetter.getBlockState(blockpos.offset(i + 1, 0, j + 1)).is(block);
   }
}
