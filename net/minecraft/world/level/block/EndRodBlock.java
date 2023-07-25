package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class EndRodBlock extends RodBlock {
   protected EndRodBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Direction direction = blockplacecontext.getClickedFace();
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().relative(direction.getOpposite()));
      return blockstate.is(this) && blockstate.getValue(FACING) == direction ? this.defaultBlockState().setValue(FACING, direction.getOpposite()) : this.defaultBlockState().setValue(FACING, direction);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      Direction direction = blockstate.getValue(FACING);
      double d0 = (double)blockpos.getX() + 0.55D - (double)(randomsource.nextFloat() * 0.1F);
      double d1 = (double)blockpos.getY() + 0.55D - (double)(randomsource.nextFloat() * 0.1F);
      double d2 = (double)blockpos.getZ() + 0.55D - (double)(randomsource.nextFloat() * 0.1F);
      double d3 = (double)(0.4F - (randomsource.nextFloat() + randomsource.nextFloat()) * 0.4F);
      if (randomsource.nextInt(5) == 0) {
         level.addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getStepX() * d3, d1 + (double)direction.getStepY() * d3, d2 + (double)direction.getStepZ() * d3, randomsource.nextGaussian() * 0.005D, randomsource.nextGaussian() * 0.005D, randomsource.nextGaussian() * 0.005D);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING);
   }
}
