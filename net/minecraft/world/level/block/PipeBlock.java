package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeBlock extends Block {
   private static final Direction[] DIRECTIONS = Direction.values();
   public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
   public static final BooleanProperty EAST = BlockStateProperties.EAST;
   public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
   public static final BooleanProperty WEST = BlockStateProperties.WEST;
   public static final BooleanProperty UP = BlockStateProperties.UP;
   public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (enummap) -> {
      enummap.put(Direction.NORTH, NORTH);
      enummap.put(Direction.EAST, EAST);
      enummap.put(Direction.SOUTH, SOUTH);
      enummap.put(Direction.WEST, WEST);
      enummap.put(Direction.UP, UP);
      enummap.put(Direction.DOWN, DOWN);
   }));
   protected final VoxelShape[] shapeByIndex;

   protected PipeBlock(float f, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.shapeByIndex = this.makeShapes(f);
   }

   private VoxelShape[] makeShapes(float f) {
      float f1 = 0.5F - f;
      float f2 = 0.5F + f;
      VoxelShape voxelshape = Block.box((double)(f1 * 16.0F), (double)(f1 * 16.0F), (double)(f1 * 16.0F), (double)(f2 * 16.0F), (double)(f2 * 16.0F), (double)(f2 * 16.0F));
      VoxelShape[] avoxelshape = new VoxelShape[DIRECTIONS.length];

      for(int i = 0; i < DIRECTIONS.length; ++i) {
         Direction direction = DIRECTIONS[i];
         avoxelshape[i] = Shapes.box(0.5D + Math.min((double)(-f), (double)direction.getStepX() * 0.5D), 0.5D + Math.min((double)(-f), (double)direction.getStepY() * 0.5D), 0.5D + Math.min((double)(-f), (double)direction.getStepZ() * 0.5D), 0.5D + Math.max((double)f, (double)direction.getStepX() * 0.5D), 0.5D + Math.max((double)f, (double)direction.getStepY() * 0.5D), 0.5D + Math.max((double)f, (double)direction.getStepZ() * 0.5D));
      }

      VoxelShape[] avoxelshape1 = new VoxelShape[64];

      for(int j = 0; j < 64; ++j) {
         VoxelShape voxelshape1 = voxelshape;

         for(int k = 0; k < DIRECTIONS.length; ++k) {
            if ((j & 1 << k) != 0) {
               voxelshape1 = Shapes.or(voxelshape1, avoxelshape[k]);
            }
         }

         avoxelshape1[j] = voxelshape1;
      }

      return avoxelshape1;
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return false;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapeByIndex[this.getAABBIndex(blockstate)];
   }

   protected int getAABBIndex(BlockState blockstate) {
      int i = 0;

      for(int j = 0; j < DIRECTIONS.length; ++j) {
         if (blockstate.getValue(PROPERTY_BY_DIRECTION.get(DIRECTIONS[j]))) {
            i |= 1 << j;
         }
      }

      return i;
   }
}
