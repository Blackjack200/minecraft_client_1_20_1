package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VineBlock extends Block {
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((map_entry) -> map_entry.getKey() != Direction.DOWN).collect(Util.toMap());
   protected static final float AABB_OFFSET = 1.0F;
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private final Map<BlockState, VoxelShape> shapesCache;

   public VineBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)));
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::calculateShape)));
   }

   private static VoxelShape calculateShape(BlockState blockstate) {
      VoxelShape voxelshape = Shapes.empty();
      if (blockstate.getValue(UP)) {
         voxelshape = UP_AABB;
      }

      if (blockstate.getValue(NORTH)) {
         voxelshape = Shapes.or(voxelshape, NORTH_AABB);
      }

      if (blockstate.getValue(SOUTH)) {
         voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
      }

      if (blockstate.getValue(EAST)) {
         voxelshape = Shapes.or(voxelshape, EAST_AABB);
      }

      if (blockstate.getValue(WEST)) {
         voxelshape = Shapes.or(voxelshape, WEST_AABB);
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapesCache.get(blockstate);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return true;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return this.hasFaces(this.getUpdatedState(blockstate, levelreader, blockpos));
   }

   private boolean hasFaces(BlockState blockstate) {
      return this.countFaces(blockstate) > 0;
   }

   private int countFaces(BlockState blockstate) {
      int i = 0;

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (blockstate.getValue(booleanproperty)) {
            ++i;
         }
      }

      return i;
   }

   private boolean canSupportAtFace(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (direction == Direction.DOWN) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (isAcceptableNeighbour(blockgetter, blockpos1, direction)) {
            return true;
         } else if (direction.getAxis() == Direction.Axis.Y) {
            return false;
         } else {
            BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
            BlockState blockstate = blockgetter.getBlockState(blockpos.above());
            return blockstate.is(this) && blockstate.getValue(booleanproperty);
         }
      }
   }

   public static boolean isAcceptableNeighbour(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return MultifaceBlock.canAttachTo(blockgetter, direction, blockpos, blockgetter.getBlockState(blockpos));
   }

   private BlockState getUpdatedState(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      if (blockstate.getValue(UP)) {
         blockstate = blockstate.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(blockgetter, blockpos1, Direction.DOWN)));
      }

      BlockState blockstate1 = null;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BooleanProperty booleanproperty = getPropertyForFace(direction);
         if (blockstate.getValue(booleanproperty)) {
            boolean flag = this.canSupportAtFace(blockgetter, blockpos, direction);
            if (!flag) {
               if (blockstate1 == null) {
                  blockstate1 = blockgetter.getBlockState(blockpos1);
               }

               flag = blockstate1.is(this) && blockstate1.getValue(booleanproperty);
            }

            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(flag));
         }
      }

      return blockstate;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == Direction.DOWN) {
         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      } else {
         BlockState blockstate2 = this.getUpdatedState(blockstate, levelaccessor, blockpos);
         return !this.hasFaces(blockstate2) ? Blocks.AIR.defaultBlockState() : blockstate2;
      }
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DO_VINES_SPREAD)) {
         if (randomsource.nextInt(4) == 0) {
            Direction direction = Direction.getRandom(randomsource);
            BlockPos blockpos1 = blockpos.above();
            if (direction.getAxis().isHorizontal() && !blockstate.getValue(getPropertyForFace(direction))) {
               if (this.canSpread(serverlevel, blockpos)) {
                  BlockPos blockpos2 = blockpos.relative(direction);
                  BlockState blockstate1 = serverlevel.getBlockState(blockpos2);
                  if (blockstate1.isAir()) {
                     Direction direction1 = direction.getClockWise();
                     Direction direction2 = direction.getCounterClockWise();
                     boolean flag = blockstate.getValue(getPropertyForFace(direction1));
                     boolean flag1 = blockstate.getValue(getPropertyForFace(direction2));
                     BlockPos blockpos3 = blockpos2.relative(direction1);
                     BlockPos blockpos4 = blockpos2.relative(direction2);
                     if (flag && isAcceptableNeighbour(serverlevel, blockpos3, direction1)) {
                        serverlevel.setBlock(blockpos2, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true)), 2);
                     } else if (flag1 && isAcceptableNeighbour(serverlevel, blockpos4, direction2)) {
                        serverlevel.setBlock(blockpos2, this.defaultBlockState().setValue(getPropertyForFace(direction2), Boolean.valueOf(true)), 2);
                     } else {
                        Direction direction3 = direction.getOpposite();
                        if (flag && serverlevel.isEmptyBlock(blockpos3) && isAcceptableNeighbour(serverlevel, blockpos.relative(direction1), direction3)) {
                           serverlevel.setBlock(blockpos3, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true)), 2);
                        } else if (flag1 && serverlevel.isEmptyBlock(blockpos4) && isAcceptableNeighbour(serverlevel, blockpos.relative(direction2), direction3)) {
                           serverlevel.setBlock(blockpos4, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true)), 2);
                        } else if ((double)randomsource.nextFloat() < 0.05D && isAcceptableNeighbour(serverlevel, blockpos2.above(), Direction.UP)) {
                           serverlevel.setBlock(blockpos2, this.defaultBlockState().setValue(UP, Boolean.valueOf(true)), 2);
                        }
                     }
                  } else if (isAcceptableNeighbour(serverlevel, blockpos2, direction)) {
                     serverlevel.setBlock(blockpos, blockstate.setValue(getPropertyForFace(direction), Boolean.valueOf(true)), 2);
                  }

               }
            } else {
               if (direction == Direction.UP && blockpos.getY() < serverlevel.getMaxBuildHeight() - 1) {
                  if (this.canSupportAtFace(serverlevel, blockpos, direction)) {
                     serverlevel.setBlock(blockpos, blockstate.setValue(UP, Boolean.valueOf(true)), 2);
                     return;
                  }

                  if (serverlevel.isEmptyBlock(blockpos1)) {
                     if (!this.canSpread(serverlevel, blockpos)) {
                        return;
                     }

                     BlockState blockstate2 = blockstate;

                     for(Direction direction4 : Direction.Plane.HORIZONTAL) {
                        if (randomsource.nextBoolean() || !isAcceptableNeighbour(serverlevel, blockpos1.relative(direction4), direction4)) {
                           blockstate2 = blockstate2.setValue(getPropertyForFace(direction4), Boolean.valueOf(false));
                        }
                     }

                     if (this.hasHorizontalConnection(blockstate2)) {
                        serverlevel.setBlock(blockpos1, blockstate2, 2);
                     }

                     return;
                  }
               }

               if (blockpos.getY() > serverlevel.getMinBuildHeight()) {
                  BlockPos blockpos5 = blockpos.below();
                  BlockState blockstate3 = serverlevel.getBlockState(blockpos5);
                  if (blockstate3.isAir() || blockstate3.is(this)) {
                     BlockState blockstate4 = blockstate3.isAir() ? this.defaultBlockState() : blockstate3;
                     BlockState blockstate5 = this.copyRandomFaces(blockstate, blockstate4, randomsource);
                     if (blockstate4 != blockstate5 && this.hasHorizontalConnection(blockstate5)) {
                        serverlevel.setBlock(blockpos5, blockstate5, 2);
                     }
                  }
               }

            }
         }
      }
   }

   private BlockState copyRandomFaces(BlockState blockstate, BlockState blockstate1, RandomSource randomsource) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (randomsource.nextBoolean()) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            if (blockstate.getValue(booleanproperty)) {
               blockstate1 = blockstate1.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return blockstate1;
   }

   private boolean hasHorizontalConnection(BlockState blockstate) {
      return blockstate.getValue(NORTH) || blockstate.getValue(EAST) || blockstate.getValue(SOUTH) || blockstate.getValue(WEST);
   }

   private boolean canSpread(BlockGetter blockgetter, BlockPos blockpos) {
      int i = 4;
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(blockpos.getX() - 4, blockpos.getY() - 1, blockpos.getZ() - 4, blockpos.getX() + 4, blockpos.getY() + 1, blockpos.getZ() + 4);
      int j = 5;

      for(BlockPos blockpos1 : iterable) {
         if (blockgetter.getBlockState(blockpos1).is(this)) {
            --j;
            if (j <= 0) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      BlockState blockstate1 = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos());
      if (blockstate1.is(this)) {
         return this.countFaces(blockstate1) < PROPERTY_BY_DIRECTION.size();
      } else {
         return super.canBeReplaced(blockstate, blockplacecontext);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos());
      boolean flag = blockstate.is(this);
      BlockState blockstate1 = flag ? blockstate : this.defaultBlockState();

      for(Direction direction : blockplacecontext.getNearestLookingDirections()) {
         if (direction != Direction.DOWN) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            boolean flag1 = flag && blockstate.getValue(booleanproperty);
            if (!flag1 && this.canSupportAtFace(blockplacecontext.getLevel(), blockplacecontext.getClickedPos(), direction)) {
               return blockstate1.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return flag ? blockstate1 : null;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(UP, NORTH, EAST, SOUTH, WEST);
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

   public static BooleanProperty getPropertyForFace(Direction direction) {
      return PROPERTY_BY_DIRECTION.get(direction);
   }
}
