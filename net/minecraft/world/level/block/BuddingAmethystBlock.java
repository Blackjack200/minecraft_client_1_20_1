package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingAmethystBlock extends AmethystBlock {
   public static final int GROWTH_CHANCE = 5;
   private static final Direction[] DIRECTIONS = Direction.values();

   public BuddingAmethystBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(5) == 0) {
         Direction direction = DIRECTIONS[randomsource.nextInt(DIRECTIONS.length)];
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
         Block block = null;
         if (canClusterGrowAtState(blockstate1)) {
            block = Blocks.SMALL_AMETHYST_BUD;
         } else if (blockstate1.is(Blocks.SMALL_AMETHYST_BUD) && blockstate1.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.MEDIUM_AMETHYST_BUD;
         } else if (blockstate1.is(Blocks.MEDIUM_AMETHYST_BUD) && blockstate1.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.LARGE_AMETHYST_BUD;
         } else if (blockstate1.is(Blocks.LARGE_AMETHYST_BUD) && blockstate1.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.AMETHYST_CLUSTER;
         }

         if (block != null) {
            BlockState blockstate2 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate1.getFluidState().getType() == Fluids.WATER));
            serverlevel.setBlockAndUpdate(blockpos1, blockstate2);
         }

      }
   }

   public static boolean canClusterGrowAtState(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER) && blockstate.getFluidState().getAmount() == 8;
   }
}
