package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralTreeFeature extends CoralFeature {
   public CoralTreeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   protected boolean placeFeature(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      int i = randomsource.nextInt(3) + 1;

      for(int j = 0; j < i; ++j) {
         if (!this.placeCoralBlock(levelaccessor, randomsource, blockpos_mutableblockpos, blockstate)) {
            return true;
         }

         blockpos_mutableblockpos.move(Direction.UP);
      }

      BlockPos blockpos1 = blockpos_mutableblockpos.immutable();
      int k = randomsource.nextInt(3) + 2;
      List<Direction> list = Direction.Plane.HORIZONTAL.shuffledCopy(randomsource);

      for(Direction direction : list.subList(0, k)) {
         blockpos_mutableblockpos.set(blockpos1);
         blockpos_mutableblockpos.move(direction);
         int l = randomsource.nextInt(5) + 2;
         int i1 = 0;

         for(int j1 = 0; j1 < l && this.placeCoralBlock(levelaccessor, randomsource, blockpos_mutableblockpos, blockstate); ++j1) {
            ++i1;
            blockpos_mutableblockpos.move(Direction.UP);
            if (j1 == 0 || i1 >= 2 && randomsource.nextFloat() < 0.25F) {
               blockpos_mutableblockpos.move(direction);
               i1 = 0;
            }
         }
      }

      return true;
   }
}
