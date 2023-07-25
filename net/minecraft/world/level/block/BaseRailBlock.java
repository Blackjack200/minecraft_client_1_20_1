package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock {
   protected static final VoxelShape FLAT_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final boolean isStraight;

   public static boolean isRail(Level level, BlockPos blockpos) {
      return isRail(level.getBlockState(blockpos));
   }

   public static boolean isRail(BlockState blockstate) {
      return blockstate.is(BlockTags.RAILS) && blockstate.getBlock() instanceof BaseRailBlock;
   }

   protected BaseRailBlock(boolean flag, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.isStraight = flag;
   }

   public boolean isStraight() {
      return this.isStraight;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      RailShape railshape = blockstate.is(this) ? blockstate.getValue(this.getShapeProperty()) : null;
      return railshape != null && railshape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return canSupportRigidBlock(levelreader, blockpos.below());
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         this.updateState(blockstate, level, blockpos, flag);
      }
   }

   protected BlockState updateState(BlockState blockstate, Level level, BlockPos blockpos, boolean flag) {
      blockstate = this.updateDir(level, blockpos, blockstate, true);
      if (this.isStraight) {
         level.neighborChanged(blockstate, blockpos, this, blockpos, flag);
      }

      return blockstate;
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (!level.isClientSide && level.getBlockState(blockpos).is(this)) {
         RailShape railshape = blockstate.getValue(this.getShapeProperty());
         if (shouldBeRemoved(blockpos, level, railshape)) {
            dropResources(blockstate, level, blockpos);
            level.removeBlock(blockpos, flag);
         } else {
            this.updateState(blockstate, level, blockpos, block);
         }

      }
   }

   private static boolean shouldBeRemoved(BlockPos blockpos, Level level, RailShape railshape) {
      if (!canSupportRigidBlock(level, blockpos.below())) {
         return true;
      } else {
         switch (railshape) {
            case ASCENDING_EAST:
               return !canSupportRigidBlock(level, blockpos.east());
            case ASCENDING_WEST:
               return !canSupportRigidBlock(level, blockpos.west());
            case ASCENDING_NORTH:
               return !canSupportRigidBlock(level, blockpos.north());
            case ASCENDING_SOUTH:
               return !canSupportRigidBlock(level, blockpos.south());
            default:
               return false;
         }
      }
   }

   protected void updateState(BlockState blockstate, Level level, BlockPos blockpos, Block block) {
   }

   protected BlockState updateDir(Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
      if (level.isClientSide) {
         return blockstate;
      } else {
         RailShape railshape = blockstate.getValue(this.getShapeProperty());
         return (new RailState(level, blockpos, blockstate)).place(level.hasNeighborSignal(blockpos), flag, railshape).getState();
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag) {
         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
         if (blockstate.getValue(this.getShapeProperty()).isAscending()) {
            level.updateNeighborsAt(blockpos.above(), this);
         }

         if (this.isStraight) {
            level.updateNeighborsAt(blockpos, this);
            level.updateNeighborsAt(blockpos.below(), this);
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      boolean flag = fluidstate.getType() == Fluids.WATER;
      BlockState blockstate = super.defaultBlockState();
      Direction direction = blockplacecontext.getHorizontalDirection();
      boolean flag1 = direction == Direction.EAST || direction == Direction.WEST;
      return blockstate.setValue(this.getShapeProperty(), flag1 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
   }

   public abstract Property<RailShape> getShapeProperty();

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }
}
