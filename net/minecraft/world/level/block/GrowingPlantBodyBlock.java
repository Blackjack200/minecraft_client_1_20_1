package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
   protected GrowingPlantBodyBlock(BlockBehaviour.Properties blockbehaviour_properties, Direction direction, VoxelShape voxelshape, boolean flag) {
      super(blockbehaviour_properties, direction, voxelshape, flag);
   }

   protected BlockState updateHeadAfterConvertedFromBody(BlockState blockstate, BlockState blockstate1) {
      return blockstate1;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == this.growthDirection.getOpposite() && !blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      GrowingPlantHeadBlock growingplantheadblock = this.getHeadBlock();
      if (direction == this.growthDirection && !blockstate1.is(this) && !blockstate1.is(growingplantheadblock)) {
         return this.updateHeadAfterConvertedFromBody(blockstate, growingplantheadblock.getStateForPlacement(levelaccessor));
      } else {
         if (this.scheduleFluidTicks) {
            levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
         }

         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(this.getHeadBlock());
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      Optional<BlockPos> optional = this.getHeadPos(levelreader, blockpos, blockstate.getBlock());
      return optional.isPresent() && this.getHeadBlock().canGrowInto(levelreader.getBlockState(optional.get().relative(this.growthDirection)));
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      Optional<BlockPos> optional = this.getHeadPos(serverlevel, blockpos, blockstate.getBlock());
      if (optional.isPresent()) {
         BlockState blockstate1 = serverlevel.getBlockState(optional.get());
         ((GrowingPlantHeadBlock)blockstate1.getBlock()).performBonemeal(serverlevel, randomsource, optional.get(), blockstate1);
      }

   }

   private Optional<BlockPos> getHeadPos(BlockGetter blockgetter, BlockPos blockpos, Block block) {
      return BlockUtil.getTopConnectedBlock(blockgetter, blockpos, block, this.growthDirection, this.getHeadBlock());
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      boolean flag = super.canBeReplaced(blockstate, blockplacecontext);
      return flag && blockplacecontext.getItemInHand().is(this.getHeadBlock().asItem()) ? false : flag;
   }

   protected Block getBodyBlock() {
      return this;
   }
}
