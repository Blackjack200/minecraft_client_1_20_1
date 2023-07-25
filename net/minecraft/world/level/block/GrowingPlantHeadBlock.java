package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock implements BonemealableBlock {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
   public static final int MAX_AGE = 25;
   private final double growPerTickProbability;

   protected GrowingPlantHeadBlock(BlockBehaviour.Properties blockbehaviour_properties, Direction direction, VoxelShape voxelshape, boolean flag, double d0) {
      super(blockbehaviour_properties, direction, voxelshape, flag);
      this.growPerTickProbability = d0;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(LevelAccessor levelaccessor) {
      return this.defaultBlockState().setValue(AGE, Integer.valueOf(levelaccessor.getRandom().nextInt(25)));
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(AGE) < 25;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(AGE) < 25 && randomsource.nextDouble() < this.growPerTickProbability) {
         BlockPos blockpos1 = blockpos.relative(this.growthDirection);
         if (this.canGrowInto(serverlevel.getBlockState(blockpos1))) {
            serverlevel.setBlockAndUpdate(blockpos1, this.getGrowIntoState(blockstate, serverlevel.random));
         }
      }

   }

   protected BlockState getGrowIntoState(BlockState blockstate, RandomSource randomsource) {
      return blockstate.cycle(AGE);
   }

   public BlockState getMaxAgeState(BlockState blockstate) {
      return blockstate.setValue(AGE, Integer.valueOf(25));
   }

   public boolean isMaxAge(BlockState blockstate) {
      return blockstate.getValue(AGE) == 25;
   }

   protected BlockState updateBodyAfterConvertedFromHead(BlockState blockstate, BlockState blockstate1) {
      return blockstate1;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == this.growthDirection.getOpposite() && !blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      if (direction != this.growthDirection || !blockstate1.is(this) && !blockstate1.is(this.getBodyBlock())) {
         if (this.scheduleFluidTicks) {
            levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
         }

         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         return this.updateBodyAfterConvertedFromHead(blockstate, this.getBodyBlock().defaultBlockState());
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return this.canGrowInto(levelreader.getBlockState(blockpos.relative(this.growthDirection)));
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockPos blockpos1 = blockpos.relative(this.growthDirection);
      int i = Math.min(blockstate.getValue(AGE) + 1, 25);
      int j = this.getBlocksToGrowWhenBonemealed(randomsource);

      for(int k = 0; k < j && this.canGrowInto(serverlevel.getBlockState(blockpos1)); ++k) {
         serverlevel.setBlockAndUpdate(blockpos1, blockstate.setValue(AGE, Integer.valueOf(i)));
         blockpos1 = blockpos1.relative(this.growthDirection);
         i = Math.min(i + 1, 25);
      }

   }

   protected abstract int getBlocksToGrowWhenBonemealed(RandomSource randomsource);

   protected abstract boolean canGrowInto(BlockState blockstate);

   protected GrowingPlantHeadBlock getHeadBlock() {
      return this;
   }
}
