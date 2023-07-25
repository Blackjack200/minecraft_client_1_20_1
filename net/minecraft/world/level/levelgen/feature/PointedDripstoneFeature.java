package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
   public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> featureplacecontext) {
      LevelAccessor levelaccessor = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      PointedDripstoneConfiguration pointeddripstoneconfiguration = featureplacecontext.config();
      Optional<Direction> optional = getTipDirection(levelaccessor, blockpos, randomsource);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.relative(optional.get().getOpposite());
         createPatchOfDripstoneBlocks(levelaccessor, randomsource, blockpos1, pointeddripstoneconfiguration);
         int i = randomsource.nextFloat() < pointeddripstoneconfiguration.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(levelaccessor.getBlockState(blockpos.relative(optional.get()))) ? 2 : 1;
         DripstoneUtils.growPointedDripstone(levelaccessor, blockpos, optional.get(), i, false);
         return true;
      }
   }

   private static Optional<Direction> getTipDirection(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource) {
      boolean flag = DripstoneUtils.isDripstoneBase(levelaccessor.getBlockState(blockpos.above()));
      boolean flag1 = DripstoneUtils.isDripstoneBase(levelaccessor.getBlockState(blockpos.below()));
      if (flag && flag1) {
         return Optional.of(randomsource.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (flag) {
         return Optional.of(Direction.DOWN);
      } else {
         return flag1 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   private static void createPatchOfDripstoneBlocks(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, PointedDripstoneConfiguration pointeddripstoneconfiguration) {
      DripstoneUtils.placeDripstoneBlockIfPossible(levelaccessor, blockpos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!(randomsource.nextFloat() > pointeddripstoneconfiguration.chanceOfDirectionalSpread)) {
            BlockPos blockpos1 = blockpos.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(levelaccessor, blockpos1);
            if (!(randomsource.nextFloat() > pointeddripstoneconfiguration.chanceOfSpreadRadius2)) {
               BlockPos blockpos2 = blockpos1.relative(Direction.getRandom(randomsource));
               DripstoneUtils.placeDripstoneBlockIfPossible(levelaccessor, blockpos2);
               if (!(randomsource.nextFloat() > pointeddripstoneconfiguration.chanceOfSpreadRadius3)) {
                  BlockPos blockpos3 = blockpos2.relative(Direction.getRandom(randomsource));
                  DripstoneUtils.placeDripstoneBlockIfPossible(levelaccessor, blockpos3);
               }
            }
         }
      }

   }
}
