package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class WaterloggedVegetationPatchFeature extends VegetationPatchFeature {
   public WaterloggedVegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
      super(codec);
   }

   protected Set<BlockPos> placeGroundPatch(WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, RandomSource randomsource, BlockPos blockpos, Predicate<BlockState> predicate, int i, int j) {
      Set<BlockPos> set = super.placeGroundPatch(worldgenlevel, vegetationpatchconfiguration, randomsource, blockpos, predicate, i, j);
      Set<BlockPos> set1 = new HashSet<>();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(BlockPos blockpos1 : set) {
         if (!isExposed(worldgenlevel, set, blockpos1, blockpos_mutableblockpos)) {
            set1.add(blockpos1);
         }
      }

      for(BlockPos blockpos2 : set1) {
         worldgenlevel.setBlock(blockpos2, Blocks.WATER.defaultBlockState(), 2);
      }

      return set1;
   }

   private static boolean isExposed(WorldGenLevel worldgenlevel, Set<BlockPos> set, BlockPos blockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      return isExposedDirection(worldgenlevel, blockpos, blockpos_mutableblockpos, Direction.NORTH) || isExposedDirection(worldgenlevel, blockpos, blockpos_mutableblockpos, Direction.EAST) || isExposedDirection(worldgenlevel, blockpos, blockpos_mutableblockpos, Direction.SOUTH) || isExposedDirection(worldgenlevel, blockpos, blockpos_mutableblockpos, Direction.WEST) || isExposedDirection(worldgenlevel, blockpos, blockpos_mutableblockpos, Direction.DOWN);
   }

   private static boolean isExposedDirection(WorldGenLevel worldgenlevel, BlockPos blockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos, Direction direction) {
      blockpos_mutableblockpos.setWithOffset(blockpos, direction);
      return !worldgenlevel.getBlockState(blockpos_mutableblockpos).isFaceSturdy(worldgenlevel, blockpos_mutableblockpos, direction.getOpposite());
   }

   protected boolean placeVegetation(WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, ChunkGenerator chunkgenerator, RandomSource randomsource, BlockPos blockpos) {
      if (super.placeVegetation(worldgenlevel, vegetationpatchconfiguration, chunkgenerator, randomsource, blockpos.below())) {
         BlockState blockstate = worldgenlevel.getBlockState(blockpos);
         if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && !blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
            worldgenlevel.setBlock(blockpos, blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 2);
         }

         return true;
      } else {
         return false;
      }
   }
}
