package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;

public class DoublePlantBlock extends BushBlock {
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

   public DoublePlantBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      DoubleBlockHalf doubleblockhalf = blockstate.getValue(HALF);
      if (direction.getAxis() != Direction.Axis.Y || doubleblockhalf == DoubleBlockHalf.LOWER != (direction == Direction.UP) || blockstate1.is(this) && blockstate1.getValue(HALF) != doubleblockhalf) {
         return doubleblockhalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Level level = blockplacecontext.getLevel();
      return blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(blockplacecontext) ? super.getStateForPlacement(blockplacecontext) : null;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      BlockPos blockpos1 = blockpos.above();
      level.setBlock(blockpos1, copyWaterloggedFrom(level, blockpos1, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER)), 3);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      if (blockstate.getValue(HALF) != DoubleBlockHalf.UPPER) {
         return super.canSurvive(blockstate, levelreader, blockpos);
      } else {
         BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
         return blockstate1.is(this) && blockstate1.getValue(HALF) == DoubleBlockHalf.LOWER;
      }
   }

   public static void placeAt(LevelAccessor levelaccessor, BlockState blockstate, BlockPos blockpos, int i) {
      BlockPos blockpos1 = blockpos.above();
      levelaccessor.setBlock(blockpos, copyWaterloggedFrom(levelaccessor, blockpos, blockstate.setValue(HALF, DoubleBlockHalf.LOWER)), i);
      levelaccessor.setBlock(blockpos1, copyWaterloggedFrom(levelaccessor, blockpos1, blockstate.setValue(HALF, DoubleBlockHalf.UPPER)), i);
   }

   public static BlockState copyWaterloggedFrom(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
      return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(levelreader.isWaterAt(blockpos))) : blockstate;
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide) {
         if (player.isCreative()) {
            preventCreativeDropFromBottomPart(level, blockpos, blockstate, player);
         } else {
            dropResources(blockstate, level, blockpos, (BlockEntity)null, player, player.getMainHandItem());
         }
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public void playerDestroy(Level level, Player player, BlockPos blockpos, BlockState blockstate, @Nullable BlockEntity blockentity, ItemStack itemstack) {
      super.playerDestroy(level, player, blockpos, Blocks.AIR.defaultBlockState(), blockentity, itemstack);
   }

   protected static void preventCreativeDropFromBottomPart(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      DoubleBlockHalf doubleblockhalf = blockstate.getValue(HALF);
      if (doubleblockhalf == DoubleBlockHalf.UPPER) {
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = level.getBlockState(blockpos1);
         if (blockstate1.is(blockstate.getBlock()) && blockstate1.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockState blockstate2 = blockstate1.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            level.setBlock(blockpos1, blockstate2, 35);
            level.levelEvent(player, 2001, blockpos1, Block.getId(blockstate1));
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HALF);
   }

   public long getSeed(BlockState blockstate, BlockPos blockpos) {
      return Mth.getSeed(blockpos.getX(), blockpos.below(blockstate.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), blockpos.getZ());
   }
}
