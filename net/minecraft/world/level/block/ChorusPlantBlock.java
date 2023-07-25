package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ChorusPlantBlock extends PipeBlock {
   protected ChorusPlantBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(0.3125F, blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)).setValue(DOWN, Boolean.valueOf(false)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.getStateForPlacement(blockplacecontext.getLevel(), blockplacecontext.getClickedPos());
   }

   public BlockState getStateForPlacement(BlockGetter blockgetter, BlockPos blockpos) {
      BlockState blockstate = blockgetter.getBlockState(blockpos.below());
      BlockState blockstate1 = blockgetter.getBlockState(blockpos.above());
      BlockState blockstate2 = blockgetter.getBlockState(blockpos.north());
      BlockState blockstate3 = blockgetter.getBlockState(blockpos.east());
      BlockState blockstate4 = blockgetter.getBlockState(blockpos.south());
      BlockState blockstate5 = blockgetter.getBlockState(blockpos.west());
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(blockstate.is(this) || blockstate.is(Blocks.CHORUS_FLOWER) || blockstate.is(Blocks.END_STONE))).setValue(UP, Boolean.valueOf(blockstate1.is(this) || blockstate1.is(Blocks.CHORUS_FLOWER))).setValue(NORTH, Boolean.valueOf(blockstate2.is(this) || blockstate2.is(Blocks.CHORUS_FLOWER))).setValue(EAST, Boolean.valueOf(blockstate3.is(this) || blockstate3.is(Blocks.CHORUS_FLOWER))).setValue(SOUTH, Boolean.valueOf(blockstate4.is(this) || blockstate4.is(Blocks.CHORUS_FLOWER))).setValue(WEST, Boolean.valueOf(blockstate5.is(this) || blockstate5.is(Blocks.CHORUS_FLOWER)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         boolean flag = blockstate1.is(this) || blockstate1.is(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && blockstate1.is(Blocks.END_STONE);
         return blockstate.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(flag));
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
      boolean flag = !levelreader.getBlockState(blockpos.above()).isAir() && !blockstate1.isAir();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate2 = levelreader.getBlockState(blockpos1);
         if (blockstate2.is(this)) {
            if (flag) {
               return false;
            }

            BlockState blockstate3 = levelreader.getBlockState(blockpos1.below());
            if (blockstate3.is(this) || blockstate3.is(Blocks.END_STONE)) {
               return true;
            }
         }
      }

      return blockstate1.is(this) || blockstate1.is(Blocks.END_STONE);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
