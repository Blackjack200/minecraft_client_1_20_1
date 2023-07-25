package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty UP = BlockStateProperties.UP;
   public static final EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
   public static final EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
   public static final EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
   public static final EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final Map<BlockState, VoxelShape> shapeByIndex;
   private final Map<BlockState, VoxelShape> collisionShapeByIndex;
   private static final int WALL_WIDTH = 3;
   private static final int WALL_HEIGHT = 14;
   private static final int POST_WIDTH = 4;
   private static final int POST_COVER_WIDTH = 1;
   private static final int WALL_COVER_START = 7;
   private static final int WALL_COVER_END = 9;
   private static final VoxelShape POST_TEST = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
   private static final VoxelShape NORTH_TEST = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 9.0D);
   private static final VoxelShape SOUTH_TEST = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_TEST = Block.box(0.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
   private static final VoxelShape EAST_TEST = Block.box(7.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

   public WallBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, Boolean.valueOf(true)).setValue(NORTH_WALL, WallSide.NONE).setValue(EAST_WALL, WallSide.NONE).setValue(SOUTH_WALL, WallSide.NONE).setValue(WEST_WALL, WallSide.NONE).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
      this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
   }

   private static VoxelShape applyWallShape(VoxelShape voxelshape, WallSide wallside, VoxelShape voxelshape1, VoxelShape voxelshape2) {
      if (wallside == WallSide.TALL) {
         return Shapes.or(voxelshape, voxelshape2);
      } else {
         return wallside == WallSide.LOW ? Shapes.or(voxelshape, voxelshape1) : voxelshape;
      }
   }

   private Map<BlockState, VoxelShape> makeShapes(float f, float f1, float f2, float f3, float f4, float f5) {
      float f6 = 8.0F - f;
      float f7 = 8.0F + f;
      float f8 = 8.0F - f1;
      float f9 = 8.0F + f1;
      VoxelShape voxelshape = Block.box((double)f6, 0.0D, (double)f6, (double)f7, (double)f2, (double)f7);
      VoxelShape voxelshape1 = Block.box((double)f8, (double)f3, 0.0D, (double)f9, (double)f4, (double)f9);
      VoxelShape voxelshape2 = Block.box((double)f8, (double)f3, (double)f8, (double)f9, (double)f4, 16.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, (double)f3, (double)f8, (double)f9, (double)f4, (double)f9);
      VoxelShape voxelshape4 = Block.box((double)f8, (double)f3, (double)f8, 16.0D, (double)f4, (double)f9);
      VoxelShape voxelshape5 = Block.box((double)f8, (double)f3, 0.0D, (double)f9, (double)f5, (double)f9);
      VoxelShape voxelshape6 = Block.box((double)f8, (double)f3, (double)f8, (double)f9, (double)f5, 16.0D);
      VoxelShape voxelshape7 = Block.box(0.0D, (double)f3, (double)f8, (double)f9, (double)f5, (double)f9);
      VoxelShape voxelshape8 = Block.box((double)f8, (double)f3, (double)f8, 16.0D, (double)f5, (double)f9);
      ImmutableMap.Builder<BlockState, VoxelShape> immutablemap_builder = ImmutableMap.builder();

      for(Boolean obool : UP.getPossibleValues()) {
         for(WallSide wallside : EAST_WALL.getPossibleValues()) {
            for(WallSide wallside1 : NORTH_WALL.getPossibleValues()) {
               for(WallSide wallside2 : WEST_WALL.getPossibleValues()) {
                  for(WallSide wallside3 : SOUTH_WALL.getPossibleValues()) {
                     VoxelShape voxelshape9 = Shapes.empty();
                     voxelshape9 = applyWallShape(voxelshape9, wallside, voxelshape4, voxelshape8);
                     voxelshape9 = applyWallShape(voxelshape9, wallside2, voxelshape3, voxelshape7);
                     voxelshape9 = applyWallShape(voxelshape9, wallside1, voxelshape1, voxelshape5);
                     voxelshape9 = applyWallShape(voxelshape9, wallside3, voxelshape2, voxelshape6);
                     if (obool) {
                        voxelshape9 = Shapes.or(voxelshape9, voxelshape);
                     }

                     BlockState blockstate = this.defaultBlockState().setValue(UP, obool).setValue(EAST_WALL, wallside).setValue(WEST_WALL, wallside2).setValue(NORTH_WALL, wallside1).setValue(SOUTH_WALL, wallside3);
                     immutablemap_builder.put(blockstate.setValue(WATERLOGGED, Boolean.valueOf(false)), voxelshape9);
                     immutablemap_builder.put(blockstate.setValue(WATERLOGGED, Boolean.valueOf(true)), voxelshape9);
                  }
               }
            }
         }
      }

      return immutablemap_builder.build();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapeByIndex.get(blockstate);
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.collisionShapeByIndex.get(blockstate);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   private boolean connectsTo(BlockState blockstate, boolean flag, Direction direction) {
      Block block = blockstate.getBlock();
      boolean flag1 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockstate, direction);
      return blockstate.is(BlockTags.WALLS) || !isExceptionForConnection(blockstate) && flag || block instanceof IronBarsBlock || flag1;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      LevelReader levelreader = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      BlockPos blockpos1 = blockpos.north();
      BlockPos blockpos2 = blockpos.east();
      BlockPos blockpos3 = blockpos.south();
      BlockPos blockpos4 = blockpos.west();
      BlockPos blockpos5 = blockpos.above();
      BlockState blockstate = levelreader.getBlockState(blockpos1);
      BlockState blockstate1 = levelreader.getBlockState(blockpos2);
      BlockState blockstate2 = levelreader.getBlockState(blockpos3);
      BlockState blockstate3 = levelreader.getBlockState(blockpos4);
      BlockState blockstate4 = levelreader.getBlockState(blockpos5);
      boolean flag = this.connectsTo(blockstate, blockstate.isFaceSturdy(levelreader, blockpos1, Direction.SOUTH), Direction.SOUTH);
      boolean flag1 = this.connectsTo(blockstate1, blockstate1.isFaceSturdy(levelreader, blockpos2, Direction.WEST), Direction.WEST);
      boolean flag2 = this.connectsTo(blockstate2, blockstate2.isFaceSturdy(levelreader, blockpos3, Direction.NORTH), Direction.NORTH);
      boolean flag3 = this.connectsTo(blockstate3, blockstate3.isFaceSturdy(levelreader, blockpos4, Direction.EAST), Direction.EAST);
      BlockState blockstate5 = this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
      return this.updateShape(levelreader, blockstate5, blockpos5, blockstate4, flag, flag1, flag2, flag3);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      if (direction == Direction.DOWN) {
         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         return direction == Direction.UP ? this.topUpdate(levelaccessor, blockstate, blockpos1, blockstate1) : this.sideUpdate(levelaccessor, blockpos, blockstate, blockpos1, blockstate1, direction);
      }
   }

   private static boolean isConnected(BlockState blockstate, Property<WallSide> property) {
      return blockstate.getValue(property) != WallSide.NONE;
   }

   private static boolean isCovered(VoxelShape voxelshape, VoxelShape voxelshape1) {
      return !Shapes.joinIsNotEmpty(voxelshape1, voxelshape, BooleanOp.ONLY_FIRST);
   }

   private BlockState topUpdate(LevelReader levelreader, BlockState blockstate, BlockPos blockpos, BlockState blockstate1) {
      boolean flag = isConnected(blockstate, NORTH_WALL);
      boolean flag1 = isConnected(blockstate, EAST_WALL);
      boolean flag2 = isConnected(blockstate, SOUTH_WALL);
      boolean flag3 = isConnected(blockstate, WEST_WALL);
      return this.updateShape(levelreader, blockstate, blockpos, blockstate1, flag, flag1, flag2, flag3);
   }

   private BlockState sideUpdate(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1, Direction direction) {
      Direction direction1 = direction.getOpposite();
      boolean flag = direction == Direction.NORTH ? this.connectsTo(blockstate1, blockstate1.isFaceSturdy(levelreader, blockpos1, direction1), direction1) : isConnected(blockstate, NORTH_WALL);
      boolean flag1 = direction == Direction.EAST ? this.connectsTo(blockstate1, blockstate1.isFaceSturdy(levelreader, blockpos1, direction1), direction1) : isConnected(blockstate, EAST_WALL);
      boolean flag2 = direction == Direction.SOUTH ? this.connectsTo(blockstate1, blockstate1.isFaceSturdy(levelreader, blockpos1, direction1), direction1) : isConnected(blockstate, SOUTH_WALL);
      boolean flag3 = direction == Direction.WEST ? this.connectsTo(blockstate1, blockstate1.isFaceSturdy(levelreader, blockpos1, direction1), direction1) : isConnected(blockstate, WEST_WALL);
      BlockPos blockpos2 = blockpos.above();
      BlockState blockstate2 = levelreader.getBlockState(blockpos2);
      return this.updateShape(levelreader, blockstate, blockpos2, blockstate2, flag, flag1, flag2, flag3);
   }

   private BlockState updateShape(LevelReader levelreader, BlockState blockstate, BlockPos blockpos, BlockState blockstate1, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      VoxelShape voxelshape = blockstate1.getCollisionShape(levelreader, blockpos).getFaceShape(Direction.DOWN);
      BlockState blockstate2 = this.updateSides(blockstate, flag, flag1, flag2, flag3, voxelshape);
      return blockstate2.setValue(UP, Boolean.valueOf(this.shouldRaisePost(blockstate2, blockstate1, voxelshape)));
   }

   private boolean shouldRaisePost(BlockState blockstate, BlockState blockstate1, VoxelShape voxelshape) {
      boolean flag = blockstate1.getBlock() instanceof WallBlock && blockstate1.getValue(UP);
      if (flag) {
         return true;
      } else {
         WallSide wallside = blockstate.getValue(NORTH_WALL);
         WallSide wallside1 = blockstate.getValue(SOUTH_WALL);
         WallSide wallside2 = blockstate.getValue(EAST_WALL);
         WallSide wallside3 = blockstate.getValue(WEST_WALL);
         boolean flag1 = wallside1 == WallSide.NONE;
         boolean flag2 = wallside3 == WallSide.NONE;
         boolean flag3 = wallside2 == WallSide.NONE;
         boolean flag4 = wallside == WallSide.NONE;
         boolean flag5 = flag4 && flag1 && flag2 && flag3 || flag4 != flag1 || flag2 != flag3;
         if (flag5) {
            return true;
         } else {
            boolean flag6 = wallside == WallSide.TALL && wallside1 == WallSide.TALL || wallside2 == WallSide.TALL && wallside3 == WallSide.TALL;
            if (flag6) {
               return false;
            } else {
               return blockstate1.is(BlockTags.WALL_POST_OVERRIDE) || isCovered(voxelshape, POST_TEST);
            }
         }
      }
   }

   private BlockState updateSides(BlockState blockstate, boolean flag, boolean flag1, boolean flag2, boolean flag3, VoxelShape voxelshape) {
      return blockstate.setValue(NORTH_WALL, this.makeWallState(flag, voxelshape, NORTH_TEST)).setValue(EAST_WALL, this.makeWallState(flag1, voxelshape, EAST_TEST)).setValue(SOUTH_WALL, this.makeWallState(flag2, voxelshape, SOUTH_TEST)).setValue(WEST_WALL, this.makeWallState(flag3, voxelshape, WEST_TEST));
   }

   private WallSide makeWallState(boolean flag, VoxelShape voxelshape, VoxelShape voxelshape1) {
      if (flag) {
         return isCovered(voxelshape, voxelshape1) ? WallSide.TALL : WallSide.LOW;
      } else {
         return WallSide.NONE;
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return !blockstate.getValue(WATERLOGGED);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL, WATERLOGGED);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return blockstate.setValue(NORTH_WALL, blockstate.getValue(SOUTH_WALL)).setValue(EAST_WALL, blockstate.getValue(WEST_WALL)).setValue(SOUTH_WALL, blockstate.getValue(NORTH_WALL)).setValue(WEST_WALL, blockstate.getValue(EAST_WALL));
         case COUNTERCLOCKWISE_90:
            return blockstate.setValue(NORTH_WALL, blockstate.getValue(EAST_WALL)).setValue(EAST_WALL, blockstate.getValue(SOUTH_WALL)).setValue(SOUTH_WALL, blockstate.getValue(WEST_WALL)).setValue(WEST_WALL, blockstate.getValue(NORTH_WALL));
         case CLOCKWISE_90:
            return blockstate.setValue(NORTH_WALL, blockstate.getValue(WEST_WALL)).setValue(EAST_WALL, blockstate.getValue(NORTH_WALL)).setValue(SOUTH_WALL, blockstate.getValue(EAST_WALL)).setValue(WEST_WALL, blockstate.getValue(SOUTH_WALL));
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return blockstate.setValue(NORTH_WALL, blockstate.getValue(SOUTH_WALL)).setValue(SOUTH_WALL, blockstate.getValue(NORTH_WALL));
         case FRONT_BACK:
            return blockstate.setValue(EAST_WALL, blockstate.getValue(WEST_WALL)).setValue(WEST_WALL, blockstate.getValue(EAST_WALL));
         default:
            return super.mirror(blockstate, mirror);
      }
   }
}
