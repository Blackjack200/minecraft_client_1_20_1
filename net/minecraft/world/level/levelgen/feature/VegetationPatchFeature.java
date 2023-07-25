package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class VegetationPatchFeature extends Feature<VegetationPatchConfiguration> {
   public VegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      VegetationPatchConfiguration vegetationpatchconfiguration = featureplacecontext.config();
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      Predicate<BlockState> predicate = (blockstate) -> blockstate.is(vegetationpatchconfiguration.replaceable);
      int i = vegetationpatchconfiguration.xzRadius.sample(randomsource) + 1;
      int j = vegetationpatchconfiguration.xzRadius.sample(randomsource) + 1;
      Set<BlockPos> set = this.placeGroundPatch(worldgenlevel, vegetationpatchconfiguration, randomsource, blockpos, predicate, i, j);
      this.distributeVegetation(featureplacecontext, worldgenlevel, vegetationpatchconfiguration, randomsource, set, i, j);
      return !set.isEmpty();
   }

   protected Set<BlockPos> placeGroundPatch(WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, RandomSource randomsource, BlockPos blockpos, Predicate<BlockState> predicate, int i, int j) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = blockpos_mutableblockpos.mutable();
      Direction direction = vegetationpatchconfiguration.surface.getDirection();
      Direction direction1 = direction.getOpposite();
      Set<BlockPos> set = new HashSet<>();

      for(int k = -i; k <= i; ++k) {
         boolean flag = k == -i || k == i;

         for(int l = -j; l <= j; ++l) {
            boolean flag1 = l == -j || l == j;
            boolean flag2 = flag || flag1;
            boolean flag3 = flag && flag1;
            boolean flag4 = flag2 && !flag3;
            if (!flag3 && (!flag4 || vegetationpatchconfiguration.extraEdgeColumnChance != 0.0F && !(randomsource.nextFloat() > vegetationpatchconfiguration.extraEdgeColumnChance))) {
               blockpos_mutableblockpos.setWithOffset(blockpos, k, 0, l);

               for(int i1 = 0; worldgenlevel.isStateAtPosition(blockpos_mutableblockpos, BlockBehaviour.BlockStateBase::isAir) && i1 < vegetationpatchconfiguration.verticalRange; ++i1) {
                  blockpos_mutableblockpos.move(direction);
               }

               for(int var25 = 0; worldgenlevel.isStateAtPosition(blockpos_mutableblockpos, (blockstate1) -> !blockstate1.isAir()) && var25 < vegetationpatchconfiguration.verticalRange; ++var25) {
                  blockpos_mutableblockpos.move(direction1);
               }

               blockpos_mutableblockpos1.setWithOffset(blockpos_mutableblockpos, vegetationpatchconfiguration.surface.getDirection());
               BlockState blockstate = worldgenlevel.getBlockState(blockpos_mutableblockpos1);
               if (worldgenlevel.isEmptyBlock(blockpos_mutableblockpos) && blockstate.isFaceSturdy(worldgenlevel, blockpos_mutableblockpos1, vegetationpatchconfiguration.surface.getDirection().getOpposite())) {
                  int j1 = vegetationpatchconfiguration.depth.sample(randomsource) + (vegetationpatchconfiguration.extraBottomBlockChance > 0.0F && randomsource.nextFloat() < vegetationpatchconfiguration.extraBottomBlockChance ? 1 : 0);
                  BlockPos blockpos1 = blockpos_mutableblockpos1.immutable();
                  boolean flag5 = this.placeGround(worldgenlevel, vegetationpatchconfiguration, predicate, randomsource, blockpos_mutableblockpos1, j1);
                  if (flag5) {
                     set.add(blockpos1);
                  }
               }
            }
         }
      }

      return set;
   }

   protected void distributeVegetation(FeaturePlaceContext<VegetationPatchConfiguration> featureplacecontext, WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, RandomSource randomsource, Set<BlockPos> set, int i, int j) {
      for(BlockPos blockpos : set) {
         if (vegetationpatchconfiguration.vegetationChance > 0.0F && randomsource.nextFloat() < vegetationpatchconfiguration.vegetationChance) {
            this.placeVegetation(worldgenlevel, vegetationpatchconfiguration, featureplacecontext.chunkGenerator(), randomsource, blockpos);
         }
      }

   }

   protected boolean placeVegetation(WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, ChunkGenerator chunkgenerator, RandomSource randomsource, BlockPos blockpos) {
      return vegetationpatchconfiguration.vegetationFeature.value().place(worldgenlevel, chunkgenerator, randomsource, blockpos.relative(vegetationpatchconfiguration.surface.getDirection().getOpposite()));
   }

   protected boolean placeGround(WorldGenLevel worldgenlevel, VegetationPatchConfiguration vegetationpatchconfiguration, Predicate<BlockState> predicate, RandomSource randomsource, BlockPos.MutableBlockPos blockpos_mutableblockpos, int i) {
      for(int j = 0; j < i; ++j) {
         BlockState blockstate = vegetationpatchconfiguration.groundState.getState(randomsource, blockpos_mutableblockpos);
         BlockState blockstate1 = worldgenlevel.getBlockState(blockpos_mutableblockpos);
         if (!blockstate.is(blockstate1.getBlock())) {
            if (!predicate.test(blockstate1)) {
               return j != 0;
            }

            worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate, 2);
            blockpos_mutableblockpos.move(vegetationpatchconfiguration.surface.getDirection());
         }
      }

      return true;
   }
}
