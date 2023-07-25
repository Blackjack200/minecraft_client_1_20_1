package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
   public SoulFireBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, 2.0F);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return this.canSurvive(blockstate, levelaccessor, blockpos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return canSurviveOnBlock(levelreader.getBlockState(blockpos.below()));
   }

   public static boolean canSurviveOnBlock(BlockState blockstate) {
      return blockstate.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
   }

   protected boolean canBurn(BlockState blockstate) {
      return true;
   }
}
