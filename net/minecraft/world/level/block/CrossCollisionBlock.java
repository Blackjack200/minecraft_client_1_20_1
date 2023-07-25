package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((map_entry) -> map_entry.getKey().getAxis().isHorizontal()).collect(Util.toMap());
   protected final VoxelShape[] collisionShapeByIndex;
   protected final VoxelShape[] shapeByIndex;
   private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

   protected CrossCollisionBlock(float f, float f1, float f2, float f3, float f4, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.collisionShapeByIndex = this.makeShapes(f, f1, f4, 0.0F, f4);
      this.shapeByIndex = this.makeShapes(f, f1, f2, 0.0F, f3);

      for(BlockState blockstate : this.stateDefinition.getPossibleStates()) {
         this.getAABBIndex(blockstate);
      }

   }

   protected VoxelShape[] makeShapes(float f, float f1, float f2, float f3, float f4) {
      float f5 = 8.0F - f;
      float f6 = 8.0F + f;
      float f7 = 8.0F - f1;
      float f8 = 8.0F + f1;
      VoxelShape voxelshape = Block.box((double)f5, 0.0D, (double)f5, (double)f6, (double)f2, (double)f6);
      VoxelShape voxelshape1 = Block.box((double)f7, (double)f3, 0.0D, (double)f8, (double)f4, (double)f8);
      VoxelShape voxelshape2 = Block.box((double)f7, (double)f3, (double)f7, (double)f8, (double)f4, 16.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, (double)f3, (double)f7, (double)f8, (double)f4, (double)f8);
      VoxelShape voxelshape4 = Block.box((double)f7, (double)f3, (double)f7, 16.0D, (double)f4, (double)f8);
      VoxelShape voxelshape5 = Shapes.or(voxelshape1, voxelshape4);
      VoxelShape voxelshape6 = Shapes.or(voxelshape2, voxelshape3);
      VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty(), voxelshape2, voxelshape3, voxelshape6, voxelshape1, Shapes.or(voxelshape2, voxelshape1), Shapes.or(voxelshape3, voxelshape1), Shapes.or(voxelshape6, voxelshape1), voxelshape4, Shapes.or(voxelshape2, voxelshape4), Shapes.or(voxelshape3, voxelshape4), Shapes.or(voxelshape6, voxelshape4), voxelshape5, Shapes.or(voxelshape2, voxelshape5), Shapes.or(voxelshape3, voxelshape5), Shapes.or(voxelshape6, voxelshape5)};

      for(int i = 0; i < 16; ++i) {
         avoxelshape[i] = Shapes.or(voxelshape, avoxelshape[i]);
      }

      return avoxelshape;
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return !blockstate.getValue(WATERLOGGED);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapeByIndex[this.getAABBIndex(blockstate)];
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.collisionShapeByIndex[this.getAABBIndex(blockstate)];
   }

   private static int indexFor(Direction direction) {
      return 1 << direction.get2DDataValue();
   }

   protected int getAABBIndex(BlockState blockstate) {
      return this.stateToIndex.computeIntIfAbsent(blockstate, (blockstate1) -> {
         int i = 0;
         if (blockstate1.getValue(NORTH)) {
            i |= indexFor(Direction.NORTH);
         }

         if (blockstate1.getValue(EAST)) {
            i |= indexFor(Direction.EAST);
         }

         if (blockstate1.getValue(SOUTH)) {
            i |= indexFor(Direction.SOUTH);
         }

         if (blockstate1.getValue(WEST)) {
            i |= indexFor(Direction.WEST);
         }

         return i;
      });
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(EAST, blockstate.getValue(WEST)).setValue(SOUTH, blockstate.getValue(NORTH)).setValue(WEST, blockstate.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(EAST)).setValue(EAST, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(NORTH));
         case CLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(WEST)).setValue(EAST, blockstate.getValue(NORTH)).setValue(SOUTH, blockstate.getValue(EAST)).setValue(WEST, blockstate.getValue(SOUTH));
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(NORTH));
         case FRONT_BACK:
            return blockstate.setValue(EAST, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(EAST));
         default:
            return super.mirror(blockstate, mirror);
      }
   }
}
