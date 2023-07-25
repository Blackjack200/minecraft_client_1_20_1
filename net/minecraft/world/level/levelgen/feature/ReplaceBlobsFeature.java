package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
   public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ReplaceSphereConfiguration> featureplacecontext) {
      ReplaceSphereConfiguration replacesphereconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      Block block = replacesphereconfiguration.targetState.getBlock();
      BlockPos blockpos = findTarget(worldgenlevel, featureplacecontext.origin().mutable().clamp(Direction.Axis.Y, worldgenlevel.getMinBuildHeight() + 1, worldgenlevel.getMaxBuildHeight() - 1), block);
      if (blockpos == null) {
         return false;
      } else {
         int i = replacesphereconfiguration.radius().sample(randomsource);
         int j = replacesphereconfiguration.radius().sample(randomsource);
         int k = replacesphereconfiguration.radius().sample(randomsource);
         int l = Math.max(i, Math.max(j, k));
         boolean flag = false;

         for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, i, j, k)) {
            if (blockpos1.distManhattan(blockpos) > l) {
               break;
            }

            BlockState blockstate = worldgenlevel.getBlockState(blockpos1);
            if (blockstate.is(block)) {
               this.setBlock(worldgenlevel, blockpos1, replacesphereconfiguration.replaceState);
               flag = true;
            }
         }

         return flag;
      }
   }

   @Nullable
   private static BlockPos findTarget(LevelAccessor levelaccessor, BlockPos.MutableBlockPos blockpos_mutableblockpos, Block block) {
      while(blockpos_mutableblockpos.getY() > levelaccessor.getMinBuildHeight() + 1) {
         BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos);
         if (blockstate.is(block)) {
            return blockpos_mutableblockpos;
         }

         blockpos_mutableblockpos.move(Direction.DOWN);
      }

      return null;
   }
}
