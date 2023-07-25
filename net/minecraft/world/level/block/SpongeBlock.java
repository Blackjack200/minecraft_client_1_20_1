package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SpongeBlock extends Block {
   public static final int MAX_DEPTH = 6;
   public static final int MAX_COUNT = 64;
   private static final Direction[] ALL_DIRECTIONS = Direction.values();

   protected SpongeBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         this.tryAbsorbWater(level, blockpos);
      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      this.tryAbsorbWater(level, blockpos);
      super.neighborChanged(blockstate, level, blockpos, block, blockpos1, flag);
   }

   protected void tryAbsorbWater(Level level, BlockPos blockpos) {
      if (this.removeWaterBreadthFirstSearch(level, blockpos)) {
         level.setBlock(blockpos, Blocks.WET_SPONGE.defaultBlockState(), 2);
         level.levelEvent(2001, blockpos, Block.getId(Blocks.WATER.defaultBlockState()));
      }

   }

   private boolean removeWaterBreadthFirstSearch(Level level, BlockPos blockpos) {
      return BlockPos.breadthFirstTraversal(blockpos, 6, 65, (blockpos3, consumer) -> {
         for(Direction direction : ALL_DIRECTIONS) {
            consumer.accept(blockpos3.relative(direction));
         }

      }, (blockpos2) -> {
         if (blockpos2.equals(blockpos)) {
            return true;
         } else {
            BlockState blockstate = level.getBlockState(blockpos2);
            FluidState fluidstate = level.getFluidState(blockpos2);
            if (!fluidstate.is(FluidTags.WATER)) {
               return false;
            } else {
               Block block = blockstate.getBlock();
               if (block instanceof BucketPickup) {
                  BucketPickup bucketpickup = (BucketPickup)block;
                  if (!bucketpickup.pickupBlock(level, blockpos2, blockstate).isEmpty()) {
                     return true;
                  }
               }

               if (blockstate.getBlock() instanceof LiquidBlock) {
                  level.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 3);
               } else {
                  if (!blockstate.is(Blocks.KELP) && !blockstate.is(Blocks.KELP_PLANT) && !blockstate.is(Blocks.SEAGRASS) && !blockstate.is(Blocks.TALL_SEAGRASS)) {
                     return false;
                  }

                  BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(blockpos2) : null;
                  dropResources(blockstate, level, blockpos2, blockentity);
                  level.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 3);
               }

               return true;
            }
         }
      }) > 1;
   }
}
