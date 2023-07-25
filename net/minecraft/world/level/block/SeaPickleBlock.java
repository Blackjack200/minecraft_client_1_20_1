package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeaPickleBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   public static final int MAX_PICKLES = 4;
   public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape ONE_AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);
   protected static final VoxelShape TWO_AABB = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 6.0D, 13.0D);
   protected static final VoxelShape THREE_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
   protected static final VoxelShape FOUR_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 7.0D, 14.0D);

   protected SeaPickleBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(PICKLES, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(true)));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos());
      if (blockstate.is(this)) {
         return blockstate.setValue(PICKLES, Integer.valueOf(Math.min(4, blockstate.getValue(PICKLES) + 1)));
      } else {
         FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
         boolean flag = fluidstate.getType() == Fluids.WATER;
         return super.getStateForPlacement(blockplacecontext).setValue(WATERLOGGED, Boolean.valueOf(flag));
      }
   }

   public static boolean isDead(BlockState blockstate) {
      return !blockstate.getValue(WATERLOGGED);
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return !blockstate.getCollisionShape(blockgetter, blockpos).getFaceShape(Direction.UP).isEmpty() || blockstate.isFaceSturdy(blockgetter, blockpos, Direction.UP);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      return this.mayPlaceOn(levelreader.getBlockState(blockpos1), levelreader, blockpos1);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!blockstate.canSurvive(levelaccessor, blockpos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (blockstate.getValue(WATERLOGGED)) {
            levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
         }

         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return !blockplacecontext.isSecondaryUseActive() && blockplacecontext.getItemInHand().is(this.asItem()) && blockstate.getValue(PICKLES) < 4 ? true : super.canBeReplaced(blockstate, blockplacecontext);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch (blockstate.getValue(PICKLES)) {
         case 1:
         default:
            return ONE_AABB;
         case 2:
            return TWO_AABB;
         case 3:
            return THREE_AABB;
         case 4:
            return FOUR_AABB;
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(PICKLES, WATERLOGGED);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return true;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      if (!isDead(blockstate) && serverlevel.getBlockState(blockpos.below()).is(BlockTags.CORAL_BLOCKS)) {
         int i = 5;
         int j = 1;
         int k = 2;
         int l = 0;
         int i1 = blockpos.getX() - 2;
         int j1 = 0;

         for(int k1 = 0; k1 < 5; ++k1) {
            for(int l1 = 0; l1 < j; ++l1) {
               int i2 = 2 + blockpos.getY() - 1;

               for(int j2 = i2 - 2; j2 < i2; ++j2) {
                  BlockPos blockpos1 = new BlockPos(i1 + k1, j2, blockpos.getZ() - j1 + l1);
                  if (blockpos1 != blockpos && randomsource.nextInt(6) == 0 && serverlevel.getBlockState(blockpos1).is(Blocks.WATER)) {
                     BlockState blockstate1 = serverlevel.getBlockState(blockpos1.below());
                     if (blockstate1.is(BlockTags.CORAL_BLOCKS)) {
                        serverlevel.setBlock(blockpos1, Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, Integer.valueOf(randomsource.nextInt(4) + 1)), 3);
                     }
                  }
               }
            }

            if (l < 2) {
               j += 2;
               ++j1;
            } else {
               j -= 2;
               --j1;
            }

            ++l;
         }

         serverlevel.setBlock(blockpos, blockstate.setValue(PICKLES, Integer.valueOf(4)), 2);
      }

   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
