package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldingBlock extends Block implements SimpleWaterloggedBlock {
   private static final int TICK_DELAY = 1;
   private static final VoxelShape STABLE_SHAPE;
   private static final VoxelShape UNSTABLE_SHAPE;
   private static final VoxelShape UNSTABLE_SHAPE_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   private static final VoxelShape BELOW_BLOCK = Shapes.block().move(0.0D, -1.0D, 0.0D);
   public static final int STABILITY_MAX_DISTANCE = 7;
   public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;

   protected ScaffoldingBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(BOTTOM, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(DISTANCE, WATERLOGGED, BOTTOM);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (!collisioncontext.isHoldingItem(blockstate.getBlock().asItem())) {
         return blockstate.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
      } else {
         return Shapes.block();
      }
   }

   public VoxelShape getInteractionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Shapes.block();
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return blockplacecontext.getItemInHand().is(this.asItem());
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Level level = blockplacecontext.getLevel();
      int i = getDistance(level, blockpos);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(level.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(DISTANCE, Integer.valueOf(i)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(level, blockpos, i)));
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!level.isClientSide) {
         level.scheduleTick(blockpos, this, 1);
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      if (!levelaccessor.isClientSide()) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return blockstate;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = getDistance(serverlevel, blockpos);
      BlockState blockstate1 = blockstate.setValue(DISTANCE, Integer.valueOf(i)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(serverlevel, blockpos, i)));
      if (blockstate1.getValue(DISTANCE) == 7) {
         if (blockstate.getValue(DISTANCE) == 7) {
            FallingBlockEntity.fall(serverlevel, blockpos, blockstate1);
         } else {
            serverlevel.destroyBlock(blockpos, true);
         }
      } else if (blockstate != blockstate1) {
         serverlevel.setBlock(blockpos, blockstate1, 3);
      }

   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return getDistance(levelreader, blockpos) < 7;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (collisioncontext.isAbove(Shapes.block(), blockpos, true) && !collisioncontext.isDescending()) {
         return STABLE_SHAPE;
      } else {
         return blockstate.getValue(DISTANCE) != 0 && blockstate.getValue(BOTTOM) && collisioncontext.isAbove(BELOW_BLOCK, blockpos, true) ? UNSTABLE_SHAPE_BOTTOM : Shapes.empty();
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   private boolean isBottom(BlockGetter blockgetter, BlockPos blockpos, int i) {
      return i > 0 && !blockgetter.getBlockState(blockpos.below()).is(this);
   }

   public static int getDistance(BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable().move(Direction.DOWN);
      BlockState blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
      int i = 7;
      if (blockstate.is(Blocks.SCAFFOLDING)) {
         i = blockstate.getValue(DISTANCE);
      } else if (blockstate.isFaceSturdy(blockgetter, blockpos_mutableblockpos, Direction.UP)) {
         return 0;
      }

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockState blockstate1 = blockgetter.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos, direction));
         if (blockstate1.is(Blocks.SCAFFOLDING)) {
            i = Math.min(i, blockstate1.getValue(DISTANCE) + 1);
            if (i == 1) {
               break;
            }
         }
      }

      return i;
   }

   static {
      VoxelShape voxelshape = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
      VoxelShape voxelshape1 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 2.0D);
      VoxelShape voxelshape2 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, 0.0D, 14.0D, 2.0D, 16.0D, 16.0D);
      VoxelShape voxelshape4 = Block.box(14.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
      STABLE_SHAPE = Shapes.or(voxelshape, voxelshape1, voxelshape2, voxelshape3, voxelshape4);
      VoxelShape voxelshape5 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 2.0D, 16.0D);
      VoxelShape voxelshape6 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
      VoxelShape voxelshape7 = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 2.0D, 16.0D);
      VoxelShape voxelshape8 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 2.0D);
      UNSTABLE_SHAPE = Shapes.or(UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, voxelshape6, voxelshape5, voxelshape8, voxelshape7);
   }
}
