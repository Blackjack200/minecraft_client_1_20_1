package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CoralBlock extends Block {
   private final Block deadBlock;

   public CoralBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.deadBlock = block;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!this.scanForWater(serverlevel, blockpos)) {
         serverlevel.setBlock(blockpos, this.deadBlock.defaultBlockState(), 2);
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!this.scanForWater(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 60 + levelaccessor.getRandom().nextInt(40));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   protected boolean scanForWater(BlockGetter blockgetter, BlockPos blockpos) {
      for(Direction direction : Direction.values()) {
         FluidState fluidstate = blockgetter.getFluidState(blockpos.relative(direction));
         if (fluidstate.is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      if (!this.scanForWater(blockplacecontext.getLevel(), blockplacecontext.getClickedPos())) {
         blockplacecontext.getLevel().scheduleTick(blockplacecontext.getClickedPos(), this, 60 + blockplacecontext.getLevel().getRandom().nextInt(40));
      }

      return this.defaultBlockState();
   }
}
