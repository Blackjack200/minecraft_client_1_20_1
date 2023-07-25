package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FrostedIceBlock extends IceBlock {
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final int NEIGHBORS_TO_AGE = 4;
   private static final int NEIGHBORS_TO_MELT = 2;

   public FrostedIceBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.tick(blockstate, serverlevel, blockpos, randomsource);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if ((randomsource.nextInt(3) == 0 || this.fewerNeigboursThan(serverlevel, blockpos, 4)) && serverlevel.getMaxLocalRawBrightness(blockpos) > 11 - blockstate.getValue(AGE) - blockstate.getLightBlock(serverlevel, blockpos) && this.slightlyMelt(blockstate, serverlevel, blockpos)) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(Direction direction : Direction.values()) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction);
            BlockState blockstate1 = serverlevel.getBlockState(blockpos_mutableblockpos);
            if (blockstate1.is(this) && !this.slightlyMelt(blockstate1, serverlevel, blockpos_mutableblockpos)) {
               serverlevel.scheduleTick(blockpos_mutableblockpos, this, Mth.nextInt(randomsource, 20, 40));
            }
         }

      } else {
         serverlevel.scheduleTick(blockpos, this, Mth.nextInt(randomsource, 20, 40));
      }
   }

   private boolean slightlyMelt(BlockState blockstate, Level level, BlockPos blockpos) {
      int i = blockstate.getValue(AGE);
      if (i < 3) {
         level.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(i + 1)), 2);
         return false;
      } else {
         this.melt(blockstate, level, blockpos);
         return true;
      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (block.defaultBlockState().is(this) && this.fewerNeigboursThan(level, blockpos, 2)) {
         this.melt(blockstate, level, blockpos);
      }

      super.neighborChanged(blockstate, level, blockpos, block, blockpos1, flag);
   }

   private boolean fewerNeigboursThan(BlockGetter blockgetter, BlockPos blockpos, int i) {
      int j = 0;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.values()) {
         blockpos_mutableblockpos.setWithOffset(blockpos, direction);
         if (blockgetter.getBlockState(blockpos_mutableblockpos).is(this)) {
            ++j;
            if (j >= i) {
               return false;
            }
         }
      }

      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return ItemStack.EMPTY;
   }
}
