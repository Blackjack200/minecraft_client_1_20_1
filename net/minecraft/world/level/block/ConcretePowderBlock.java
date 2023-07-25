package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
   private final BlockState concrete;

   public ConcretePowderBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.concrete = block.defaultBlockState();
   }

   public void onLand(Level level, BlockPos blockpos, BlockState blockstate, BlockState blockstate1, FallingBlockEntity fallingblockentity) {
      if (shouldSolidify(level, blockpos, blockstate1)) {
         level.setBlock(blockpos, this.concrete, 3);
      }

   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockGetter blockgetter = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      return shouldSolidify(blockgetter, blockpos, blockstate) ? this.concrete : super.getStateForPlacement(blockplacecontext);
   }

   private static boolean shouldSolidify(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return canSolidify(blockstate) || touchesLiquid(blockgetter, blockpos);
   }

   private static boolean touchesLiquid(BlockGetter blockgetter, BlockPos blockpos) {
      boolean flag = false;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(Direction direction : Direction.values()) {
         BlockState blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
         if (direction != Direction.DOWN || canSolidify(blockstate)) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction);
            blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
            if (canSolidify(blockstate) && !blockstate.isFaceSturdy(blockgetter, blockpos, direction.getOpposite())) {
               flag = true;
               break;
            }
         }
      }

      return flag;
   }

   private static boolean canSolidify(BlockState blockstate) {
      return blockstate.getFluidState().is(FluidTags.WATER);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return touchesLiquid(levelaccessor, blockpos) ? this.concrete : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public int getDustColor(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.getMapColor(blockgetter, blockpos).col;
   }
}
