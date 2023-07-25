package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtils {
   protected static double getDripstoneHeight(double d0, double d1, double d2, double d3) {
      if (d0 < d3) {
         d0 = d3;
      }

      double d4 = 0.384D;
      double d5 = d0 / d1 * 0.384D;
      double d6 = 0.75D * Math.pow(d5, 1.3333333333333333D);
      double d7 = Math.pow(d5, 0.6666666666666666D);
      double d8 = 0.3333333333333333D * Math.log(d5);
      double d9 = d2 * (d6 - d7 - d8);
      d9 = Math.max(d9, 0.0D);
      return d9 / 0.384D * d1;
   }

   protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel worldgenlevel, BlockPos blockpos, int i) {
      if (isEmptyOrWaterOrLava(worldgenlevel, blockpos)) {
         return false;
      } else {
         float f = 6.0F;
         float f1 = 6.0F / (float)i;

         for(float f2 = 0.0F; f2 < ((float)Math.PI * 2F); f2 += f1) {
            int j = (int)(Mth.cos(f2) * (float)i);
            int k = (int)(Mth.sin(f2) * (float)i);
            if (isEmptyOrWaterOrLava(worldgenlevel, blockpos.offset(j, 0, k))) {
               return false;
            }
         }

         return true;
      }
   }

   protected static boolean isEmptyOrWater(LevelAccessor levelaccessor, BlockPos blockpos) {
      return levelaccessor.isStateAtPosition(blockpos, DripstoneUtils::isEmptyOrWater);
   }

   protected static boolean isEmptyOrWaterOrLava(LevelAccessor levelaccessor, BlockPos blockpos) {
      return levelaccessor.isStateAtPosition(blockpos, DripstoneUtils::isEmptyOrWaterOrLava);
   }

   protected static void buildBaseToTipColumn(Direction direction, int i, boolean flag, Consumer<BlockState> consumer) {
      if (i >= 3) {
         consumer.accept(createPointedDripstone(direction, DripstoneThickness.BASE));

         for(int j = 0; j < i - 3; ++j) {
            consumer.accept(createPointedDripstone(direction, DripstoneThickness.MIDDLE));
         }
      }

      if (i >= 2) {
         consumer.accept(createPointedDripstone(direction, DripstoneThickness.FRUSTUM));
      }

      if (i >= 1) {
         consumer.accept(createPointedDripstone(direction, flag ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
      }

   }

   protected static void growPointedDripstone(LevelAccessor levelaccessor, BlockPos blockpos, Direction direction, int i, boolean flag) {
      if (isDripstoneBase(levelaccessor.getBlockState(blockpos.relative(direction.getOpposite())))) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
         buildBaseToTipColumn(direction, i, flag, (blockstate) -> {
            if (blockstate.is(Blocks.POINTED_DRIPSTONE)) {
               blockstate = blockstate.setValue(PointedDripstoneBlock.WATERLOGGED, Boolean.valueOf(levelaccessor.isWaterAt(blockpos_mutableblockpos)));
            }

            levelaccessor.setBlock(blockpos_mutableblockpos, blockstate, 2);
            blockpos_mutableblockpos.move(direction);
         });
      }
   }

   protected static boolean placeDripstoneBlockIfPossible(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      if (blockstate.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
         levelaccessor.setBlock(blockpos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
         return true;
      } else {
         return false;
      }
   }

   private static BlockState createPointedDripstone(Direction direction, DripstoneThickness dripstonethickness) {
      return Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction).setValue(PointedDripstoneBlock.THICKNESS, dripstonethickness);
   }

   public static boolean isDripstoneBaseOrLava(BlockState blockstate) {
      return isDripstoneBase(blockstate) || blockstate.is(Blocks.LAVA);
   }

   public static boolean isDripstoneBase(BlockState blockstate) {
      return blockstate.is(Blocks.DRIPSTONE_BLOCK) || blockstate.is(BlockTags.DRIPSTONE_REPLACEABLE);
   }

   public static boolean isEmptyOrWater(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER);
   }

   public static boolean isNeitherEmptyNorWater(BlockState blockstate) {
      return !blockstate.isAir() && !blockstate.is(Blocks.WATER);
   }

   public static boolean isEmptyOrWaterOrLava(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER) || blockstate.is(Blocks.LAVA);
   }
}
