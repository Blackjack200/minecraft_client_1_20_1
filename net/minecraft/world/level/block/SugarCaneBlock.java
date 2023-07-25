package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

   protected SugarCaneBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.isEmptyBlock(blockpos.above())) {
         int i;
         for(i = 1; serverlevel.getBlockState(blockpos.below(i)).is(this); ++i) {
         }

         if (i < 3) {
            int j = blockstate.getValue(AGE);
            if (j == 15) {
               serverlevel.setBlockAndUpdate(blockpos.above(), this.defaultBlockState());
               serverlevel.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(0)), 4);
            } else {
               serverlevel.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(j + 1)), 4);
            }
         }
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
      if (blockstate1.is(this)) {
         return true;
      } else {
         if (blockstate1.is(BlockTags.DIRT) || blockstate1.is(BlockTags.SAND)) {
            BlockPos blockpos1 = blockpos.below();

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               BlockState blockstate2 = levelreader.getBlockState(blockpos1.relative(direction));
               FluidState fluidstate = levelreader.getFluidState(blockpos1.relative(direction));
               if (fluidstate.is(FluidTags.WATER) || blockstate2.is(Blocks.FROSTED_ICE)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }
}
