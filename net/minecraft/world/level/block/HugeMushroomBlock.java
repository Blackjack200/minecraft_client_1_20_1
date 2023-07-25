package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class HugeMushroomBlock extends Block {
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final BooleanProperty DOWN = PipeBlock.DOWN;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

   public HugeMushroomBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(true)).setValue(EAST, Boolean.valueOf(true)).setValue(SOUTH, Boolean.valueOf(true)).setValue(WEST, Boolean.valueOf(true)).setValue(UP, Boolean.valueOf(true)).setValue(DOWN, Boolean.valueOf(true)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockGetter blockgetter = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(!blockgetter.getBlockState(blockpos.below()).is(this))).setValue(UP, Boolean.valueOf(!blockgetter.getBlockState(blockpos.above()).is(this))).setValue(NORTH, Boolean.valueOf(!blockgetter.getBlockState(blockpos.north()).is(this))).setValue(EAST, Boolean.valueOf(!blockgetter.getBlockState(blockpos.east()).is(this))).setValue(SOUTH, Boolean.valueOf(!blockgetter.getBlockState(blockpos.south()).is(this))).setValue(WEST, Boolean.valueOf(!blockgetter.getBlockState(blockpos.west()).is(this)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return blockstate1.is(this) ? blockstate.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(false)) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.NORTH)), blockstate.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.SOUTH)), blockstate.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.EAST)), blockstate.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.WEST)), blockstate.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.UP)), blockstate.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.DOWN)), blockstate.getValue(DOWN));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.NORTH)), blockstate.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.SOUTH)), blockstate.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.EAST)), blockstate.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.WEST)), blockstate.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.UP)), blockstate.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.DOWN)), blockstate.getValue(DOWN));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
   }
}
