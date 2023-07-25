package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TallSeagrassBlock extends DoublePlantBlock implements LiquidBlockContainer {
   public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

   public TallSeagrassBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.isFaceSturdy(blockgetter, blockpos, Direction.UP) && !blockstate.is(Blocks.MAGMA_BLOCK);
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(Blocks.SEAGRASS);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = super.getStateForPlacement(blockplacecontext);
      if (blockstate != null) {
         FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos().above());
         if (fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8) {
            return blockstate;
         }
      }

      return null;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      if (blockstate.getValue(HALF) == DoubleBlockHalf.UPPER) {
         BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
         return blockstate1.is(this) && blockstate1.getValue(HALF) == DoubleBlockHalf.LOWER;
      } else {
         FluidState fluidstate = levelreader.getFluidState(blockpos);
         return super.canSurvive(blockstate, levelreader, blockpos) && fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8;
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return Fluids.WATER.getSource(false);
   }

   public boolean canPlaceLiquid(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Fluid fluid) {
      return false;
   }

   public boolean placeLiquid(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      return false;
   }
}
