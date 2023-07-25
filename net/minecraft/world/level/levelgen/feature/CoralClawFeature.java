package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature extends CoralFeature {
   public CoralClawFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   protected boolean placeFeature(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      if (!this.placeCoralBlock(levelaccessor, randomsource, blockpos, blockstate)) {
         return false;
      } else {
         Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
         int i = randomsource.nextInt(2) + 2;
         List<Direction> list = Util.toShuffledList(Stream.of(direction, direction.getClockWise(), direction.getCounterClockWise()), randomsource);

         for(Direction direction1 : list.subList(0, i)) {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
            int j = randomsource.nextInt(2) + 1;
            blockpos_mutableblockpos.move(direction1);
            int k;
            Direction direction2;
            if (direction1 == direction) {
               direction2 = direction;
               k = randomsource.nextInt(3) + 2;
            } else {
               blockpos_mutableblockpos.move(Direction.UP);
               Direction[] adirection = new Direction[]{direction1, Direction.UP};
               direction2 = Util.getRandom(adirection, randomsource);
               k = randomsource.nextInt(3) + 3;
            }

            for(int i1 = 0; i1 < j && this.placeCoralBlock(levelaccessor, randomsource, blockpos_mutableblockpos, blockstate); ++i1) {
               blockpos_mutableblockpos.move(direction2);
            }

            blockpos_mutableblockpos.move(direction2.getOpposite());
            blockpos_mutableblockpos.move(Direction.UP);

            for(int j1 = 0; j1 < k; ++j1) {
               blockpos_mutableblockpos.move(direction);
               if (!this.placeCoralBlock(levelaccessor, randomsource, blockpos_mutableblockpos, blockstate)) {
                  break;
               }

               if (randomsource.nextFloat() < 0.25F) {
                  blockpos_mutableblockpos.move(Direction.UP);
               }
            }
         }

         return true;
      }
   }
}
