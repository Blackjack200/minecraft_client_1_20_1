package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature extends Feature<RootSystemConfiguration> {
   public RootSystemFeature(Codec<RootSystemConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RootSystemConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      if (!worldgenlevel.getBlockState(blockpos).isAir()) {
         return false;
      } else {
         RandomSource randomsource = featureplacecontext.random();
         BlockPos blockpos1 = featureplacecontext.origin();
         RootSystemConfiguration rootsystemconfiguration = featureplacecontext.config();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos1.mutable();
         if (placeDirtAndTree(worldgenlevel, featureplacecontext.chunkGenerator(), rootsystemconfiguration, randomsource, blockpos_mutableblockpos, blockpos1)) {
            placeRoots(worldgenlevel, rootsystemconfiguration, randomsource, blockpos1, blockpos_mutableblockpos);
         }

         return true;
      }
   }

   private static boolean spaceForTree(WorldGenLevel worldgenlevel, RootSystemConfiguration rootsystemconfiguration, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int i = 1; i <= rootsystemconfiguration.requiredVerticalSpaceForTree; ++i) {
         blockpos_mutableblockpos.move(Direction.UP);
         BlockState blockstate = worldgenlevel.getBlockState(blockpos_mutableblockpos);
         if (!isAllowedTreeSpace(blockstate, i, rootsystemconfiguration.allowedVerticalWaterForTree)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isAllowedTreeSpace(BlockState blockstate, int i, int j) {
      if (blockstate.isAir()) {
         return true;
      } else {
         int k = i + 1;
         return k <= j && blockstate.getFluidState().is(FluidTags.WATER);
      }
   }

   private static boolean placeDirtAndTree(WorldGenLevel worldgenlevel, ChunkGenerator chunkgenerator, RootSystemConfiguration rootsystemconfiguration, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, BlockPos blockpos) {
      for(int i = 0; i < rootsystemconfiguration.rootColumnMaxHeight; ++i) {
         blockpos_mutableblockpos.move(Direction.UP);
         if (rootsystemconfiguration.allowedTreePosition.test(worldgenlevel, blockpos_mutableblockpos) && spaceForTree(worldgenlevel, rootsystemconfiguration, blockpos_mutableblockpos)) {
            BlockPos blockpos1 = blockpos_mutableblockpos.below();
            if (worldgenlevel.getFluidState(blockpos1).is(FluidTags.LAVA) || !worldgenlevel.getBlockState(blockpos1).isSolid()) {
               return false;
            }

            if (rootsystemconfiguration.treeFeature.value().place(worldgenlevel, chunkgenerator, randomsource, blockpos_mutableblockpos)) {
               placeDirt(blockpos, blockpos.getY() + i, worldgenlevel, rootsystemconfiguration, randomsource);
               return true;
            }
         }
      }

      return false;
   }

   private static void placeDirt(BlockPos blockpos, int i, WorldGenLevel worldgenlevel, RootSystemConfiguration rootsystemconfiguration, RandomSource randomsource) {
      int j = blockpos.getX();
      int k = blockpos.getZ();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int l = blockpos.getY(); l < i; ++l) {
         placeRootedDirt(worldgenlevel, rootsystemconfiguration, randomsource, j, k, blockpos_mutableblockpos.set(j, l, k));
      }

   }

   private static void placeRootedDirt(WorldGenLevel worldgenlevel, RootSystemConfiguration rootsystemconfiguration, RandomSource randomsource, int i, int j, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      int k = rootsystemconfiguration.rootRadius;
      Predicate<BlockState> predicate = (blockstate) -> blockstate.is(rootsystemconfiguration.rootReplaceable);

      for(int l = 0; l < rootsystemconfiguration.rootPlacementAttempts; ++l) {
         blockpos_mutableblockpos.setWithOffset(blockpos_mutableblockpos, randomsource.nextInt(k) - randomsource.nextInt(k), 0, randomsource.nextInt(k) - randomsource.nextInt(k));
         if (predicate.test(worldgenlevel.getBlockState(blockpos_mutableblockpos))) {
            worldgenlevel.setBlock(blockpos_mutableblockpos, rootsystemconfiguration.rootStateProvider.getState(randomsource, blockpos_mutableblockpos), 2);
         }

         blockpos_mutableblockpos.setX(i);
         blockpos_mutableblockpos.setZ(j);
      }

   }

   private static void placeRoots(WorldGenLevel worldgenlevel, RootSystemConfiguration rootsystemconfiguration, RandomSource randomsource, BlockPos blockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      int i = rootsystemconfiguration.hangingRootRadius;
      int j = rootsystemconfiguration.hangingRootsVerticalSpan;

      for(int k = 0; k < rootsystemconfiguration.hangingRootPlacementAttempts; ++k) {
         blockpos_mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(i) - randomsource.nextInt(i), randomsource.nextInt(j) - randomsource.nextInt(j), randomsource.nextInt(i) - randomsource.nextInt(i));
         if (worldgenlevel.isEmptyBlock(blockpos_mutableblockpos)) {
            BlockState blockstate = rootsystemconfiguration.hangingRootStateProvider.getState(randomsource, blockpos_mutableblockpos);
            if (blockstate.canSurvive(worldgenlevel, blockpos_mutableblockpos) && worldgenlevel.getBlockState(blockpos_mutableblockpos.above()).isFaceSturdy(worldgenlevel, blockpos_mutableblockpos, Direction.DOWN)) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate, 2);
            }
         }
      }

   }
}
