package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallDripleafBlock extends DoublePlantBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

   public SmallDripleafBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(BlockTags.SMALL_DRIPLEAF_PLACEABLE) || blockgetter.getFluidState(blockpos.above()).isSourceOfType(Fluids.WATER) && super.mayPlaceOn(blockstate, blockgetter, blockpos);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = super.getStateForPlacement(blockplacecontext);
      return blockstate != null ? copyWaterloggedFrom(blockplacecontext.getLevel(), blockplacecontext.getClickedPos(), blockstate.setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite())) : null;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (!level.isClientSide()) {
         BlockPos blockpos1 = blockpos.above();
         BlockState blockstate1 = DoublePlantBlock.copyWaterloggedFrom(level, blockpos1, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, blockstate.getValue(FACING)));
         level.setBlock(blockpos1, blockstate1, 3);
      }

   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      if (blockstate.getValue(HALF) == DoubleBlockHalf.UPPER) {
         return super.canSurvive(blockstate, levelreader, blockpos);
      } else {
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = levelreader.getBlockState(blockpos1);
         return this.mayPlaceOn(blockstate1, levelreader, blockpos1);
      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HALF, WATERLOGGED, FACING);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return true;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
         BlockPos blockpos1 = blockpos.above();
         serverlevel.setBlock(blockpos1, serverlevel.getFluidState(blockpos1).createLegacyBlock(), 18);
         BigDripleafBlock.placeWithRandomHeight(serverlevel, randomsource, blockpos, blockstate.getValue(FACING));
      } else {
         BlockPos blockpos2 = blockpos.below();
         this.performBonemeal(serverlevel, randomsource, blockpos2, serverlevel.getBlockState(blockpos2));
      }

   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   public float getMaxVerticalOffset() {
      return 0.1F;
   }
}
