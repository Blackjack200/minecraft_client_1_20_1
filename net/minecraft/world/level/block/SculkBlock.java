package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
   public SculkBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, ConstantInt.of(1));
   }

   public int attemptUseCharge(SculkSpreader.ChargeCursor sculkspreader_chargecursor, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, SculkSpreader sculkspreader, boolean flag) {
      int i = sculkspreader_chargecursor.getCharge();
      if (i != 0 && randomsource.nextInt(sculkspreader.chargeDecayRate()) == 0) {
         BlockPos blockpos1 = sculkspreader_chargecursor.getPos();
         boolean flag1 = blockpos1.closerThan(blockpos, (double)sculkspreader.noGrowthRadius());
         if (!flag1 && canPlaceGrowth(levelaccessor, blockpos1)) {
            int j = sculkspreader.growthSpawnCost();
            if (randomsource.nextInt(j) < i) {
               BlockPos blockpos2 = blockpos1.above();
               BlockState blockstate = this.getRandomGrowthState(levelaccessor, blockpos2, randomsource, sculkspreader.isWorldGeneration());
               levelaccessor.setBlock(blockpos2, blockstate, 3);
               levelaccessor.playSound((Player)null, blockpos1, blockstate.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return Math.max(0, i - j);
         } else {
            return randomsource.nextInt(sculkspreader.additionalDecayRate()) != 0 ? i : i - (flag1 ? 1 : getDecayPenalty(sculkspreader, blockpos1, blockpos, i));
         }
      } else {
         return i;
      }
   }

   private static int getDecayPenalty(SculkSpreader sculkspreader, BlockPos blockpos, BlockPos blockpos1, int i) {
      int j = sculkspreader.noGrowthRadius();
      float f = Mth.square((float)Math.sqrt(blockpos.distSqr(blockpos1)) - (float)j);
      int k = Mth.square(24 - j);
      float f1 = Math.min(1.0F, f / (float)k);
      return Math.max(1, (int)((float)i * f1 * 0.5F));
   }

   private BlockState getRandomGrowthState(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, boolean flag) {
      BlockState blockstate;
      if (randomsource.nextInt(11) == 0) {
         blockstate = Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(flag));
      } else {
         blockstate = Blocks.SCULK_SENSOR.defaultBlockState();
      }

      return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && !levelaccessor.getFluidState(blockpos).isEmpty() ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : blockstate;
   }

   private static boolean canPlaceGrowth(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos.above());
      if (blockstate.isAir() || blockstate.is(Blocks.WATER) && blockstate.getFluidState().is(Fluids.WATER)) {
         int i = 0;

         for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-4, 0, -4), blockpos.offset(4, 2, 4))) {
            BlockState blockstate1 = levelaccessor.getBlockState(blockpos1);
            if (blockstate1.is(Blocks.SCULK_SENSOR) || blockstate1.is(Blocks.SCULK_SHRIEKER)) {
               ++i;
            }

            if (i > 2) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean canChangeBlockStateOnSpread() {
      return false;
   }
}
