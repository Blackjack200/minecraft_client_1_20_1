package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final int MAX_AGE = 15;
   protected static final int AABB_OFFSET = 1;
   protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
   protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   protected CactusBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockPos blockpos1 = blockpos.above();
      if (serverlevel.isEmptyBlock(blockpos1)) {
         int i;
         for(i = 1; serverlevel.getBlockState(blockpos.below(i)).is(this); ++i) {
         }

         if (i < 3) {
            int j = blockstate.getValue(AGE);
            if (j == 15) {
               serverlevel.setBlockAndUpdate(blockpos1, this.defaultBlockState());
               BlockState blockstate1 = blockstate.setValue(AGE, Integer.valueOf(0));
               serverlevel.setBlock(blockpos, blockstate1, 4);
               serverlevel.neighborChanged(blockstate1, blockpos1, this, blockpos, false);
            } else {
               serverlevel.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(j + 1)), 4);
            }

         }
      }
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return OUTLINE_SHAPE;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockState blockstate1 = levelreader.getBlockState(blockpos.relative(direction));
         if (blockstate1.isSolid() || levelreader.getFluidState(blockpos.relative(direction)).is(FluidTags.LAVA)) {
            return false;
         }
      }

      BlockState blockstate2 = levelreader.getBlockState(blockpos.below());
      return (blockstate2.is(Blocks.CACTUS) || blockstate2.is(BlockTags.SAND)) && !levelreader.getBlockState(blockpos.above()).liquid();
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      entity.hurt(level.damageSources().cactus(), 1.0F);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
