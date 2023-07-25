package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralMushroomFeature extends CoralFeature {
   public CoralMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   protected boolean placeFeature(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      int i = randomsource.nextInt(3) + 3;
      int j = randomsource.nextInt(3) + 3;
      int k = randomsource.nextInt(3) + 3;
      int l = randomsource.nextInt(3) + 1;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int i1 = 0; i1 <= j; ++i1) {
         for(int j1 = 0; j1 <= i; ++j1) {
            for(int k1 = 0; k1 <= k; ++k1) {
               blockpos_mutableblockpos.set(i1 + blockpos.getX(), j1 + blockpos.getY(), k1 + blockpos.getZ());
               blockpos_mutableblockpos.move(Direction.DOWN, l);
               if ((i1 != 0 && i1 != j || j1 != 0 && j1 != i) && (k1 != 0 && k1 != k || j1 != 0 && j1 != i) && (i1 != 0 && i1 != j || k1 != 0 && k1 != k) && (i1 == 0 || i1 == j || j1 == 0 || j1 == i || k1 == 0 || k1 == k) && !(randomsource.nextFloat() < 0.1F) && !this.placeCoralBlock(levelaccessor, randomsource, blockpos_mutableblockpos, blockstate)) {
               }
            }
         }
      }

      return true;
   }
}
