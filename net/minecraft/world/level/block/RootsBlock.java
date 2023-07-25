package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RootsBlock extends BushBlock {
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

   protected RootsBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(BlockTags.NYLIUM) || blockstate.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(blockstate, blockgetter, blockpos);
   }
}
