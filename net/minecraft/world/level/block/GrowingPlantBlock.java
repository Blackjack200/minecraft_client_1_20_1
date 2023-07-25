package net.minecraft.world.level.block;

import javax.annotation.Nullable;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBlock extends Block {
   protected final Direction growthDirection;
   protected final boolean scheduleFluidTicks;
   protected final VoxelShape shape;

   protected GrowingPlantBlock(BlockBehaviour.Properties blockbehaviour_properties, Direction direction, VoxelShape voxelshape, boolean flag) {
      super(blockbehaviour_properties);
      this.growthDirection = direction;
      this.shape = voxelshape;
      this.scheduleFluidTicks = flag;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().relative(this.growthDirection));
      return !blockstate.is(this.getHeadBlock()) && !blockstate.is(this.getBodyBlock()) ? this.getStateForPlacement(blockplacecontext.getLevel()) : this.getBodyBlock().defaultBlockState();
   }

   public BlockState getStateForPlacement(LevelAccessor levelaccessor) {
      return this.defaultBlockState();
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.relative(this.growthDirection.getOpposite());
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      if (!this.canAttachTo(blockstate1)) {
         return false;
      } else {
         return blockstate1.is(this.getHeadBlock()) || blockstate1.is(this.getBodyBlock()) || blockstate1.isFaceSturdy(levelreader, blockpos1, this.growthDirection);
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   protected boolean canAttachTo(BlockState blockstate) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shape;
   }

   protected abstract GrowingPlantHeadBlock getHeadBlock();

   protected abstract Block getBodyBlock();
}
