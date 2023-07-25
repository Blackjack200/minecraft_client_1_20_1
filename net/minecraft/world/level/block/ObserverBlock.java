package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ObserverBlock extends DirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public ObserverBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(POWERED, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, POWERED);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(POWERED)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)), 2);
      } else {
         serverlevel.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)), 2);
         serverlevel.scheduleTick(blockpos, this, 2);
      }

      this.updateNeighborsInFront(serverlevel, blockpos, blockstate);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(FACING) == direction && !blockstate.getValue(POWERED)) {
         this.startSignal(levelaccessor, blockpos);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   private void startSignal(LevelAccessor levelaccessor, BlockPos blockpos) {
      if (!levelaccessor.isClientSide() && !levelaccessor.getBlockTicks().hasScheduledTick(blockpos, this)) {
         levelaccessor.scheduleTick(blockpos, this, 2);
      }

   }

   protected void updateNeighborsInFront(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
      level.neighborChanged(blockpos1, this, blockpos);
      level.updateNeighborsAtExceptFromFacing(blockpos1, this, direction);
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getSignal(blockgetter, blockpos, direction);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) && blockstate.getValue(FACING) == direction ? 15 : 0;
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (!level.isClientSide() && blockstate.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(blockpos, this)) {
            BlockState blockstate2 = blockstate.setValue(POWERED, Boolean.valueOf(false));
            level.setBlock(blockpos, blockstate2, 18);
            this.updateNeighborsInFront(level, blockpos, blockstate2);
         }

      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (!level.isClientSide && blockstate.getValue(POWERED) && level.getBlockTicks().hasScheduledTick(blockpos, this)) {
            this.updateNeighborsInFront(level, blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)));
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getNearestLookingDirection().getOpposite().getOpposite());
   }
}
