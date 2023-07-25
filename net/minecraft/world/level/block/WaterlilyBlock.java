package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterlilyBlock extends BushBlock {
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);

   protected WaterlilyBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      super.entityInside(blockstate, level, blockpos, entity);
      if (level instanceof ServerLevel && entity instanceof Boat) {
         level.destroyBlock(new BlockPos(blockpos), true, entity);
      }

   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return AABB;
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      FluidState fluidstate = blockgetter.getFluidState(blockpos);
      FluidState fluidstate1 = blockgetter.getFluidState(blockpos.above());
      return (fluidstate.getType() == Fluids.WATER || blockstate.getBlock() instanceof IceBlock) && fluidstate1.getType() == Fluids.EMPTY;
   }
}
