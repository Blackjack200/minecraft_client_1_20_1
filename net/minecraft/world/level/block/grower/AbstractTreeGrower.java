package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractTreeGrower {
   @Nullable
   protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomsource, boolean flag);

   public boolean growTree(ServerLevel serverlevel, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockState blockstate, RandomSource randomsource) {
      ResourceKey<ConfiguredFeature<?, ?>> resourcekey = this.getConfiguredFeature(randomsource, this.hasFlowers(serverlevel, blockpos));
      if (resourcekey == null) {
         return false;
      } else {
         Holder<ConfiguredFeature<?, ?>> holder = serverlevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey).orElse((Holder.Reference<ConfiguredFeature<?, ?>>)null);
         if (holder == null) {
            return false;
         } else {
            ConfiguredFeature<?, ?> configuredfeature = holder.value();
            BlockState blockstate1 = serverlevel.getFluidState(blockpos).createLegacyBlock();
            serverlevel.setBlock(blockpos, blockstate1, 4);
            if (configuredfeature.place(serverlevel, chunkgenerator, randomsource, blockpos)) {
               if (serverlevel.getBlockState(blockpos) == blockstate1) {
                  serverlevel.sendBlockUpdated(blockpos, blockstate, blockstate1, 2);
               }

               return true;
            } else {
               serverlevel.setBlock(blockpos, blockstate, 4);
               return false;
            }
         }
      }
   }

   private boolean hasFlowers(LevelAccessor levelaccessor, BlockPos blockpos) {
      for(BlockPos blockpos1 : BlockPos.MutableBlockPos.betweenClosed(blockpos.below().north(2).west(2), blockpos.above().south(2).east(2))) {
         if (levelaccessor.getBlockState(blockpos1).is(BlockTags.FLOWERS)) {
            return true;
         }
      }

      return false;
   }
}
