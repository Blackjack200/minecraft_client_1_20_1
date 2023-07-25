package net.minecraft.world.level.block;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
   protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
   protected static final VoxelShape OCTET_NNN = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
   protected static final VoxelShape OCTET_NNP = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
   protected static final VoxelShape OCTET_NPN = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
   protected static final VoxelShape OCTET_NPP = Block.box(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
   protected static final VoxelShape OCTET_PNN = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
   protected static final VoxelShape OCTET_PNP = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
   protected static final VoxelShape OCTET_PPN = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
   protected static final VoxelShape OCTET_PPP = Block.box(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
   protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
   private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
   private final Block base;
   private final BlockState baseState;

   private static VoxelShape[] makeShapes(VoxelShape voxelshape, VoxelShape voxelshape1, VoxelShape voxelshape2, VoxelShape voxelshape3, VoxelShape voxelshape4) {
      return IntStream.range(0, 16).mapToObj((j) -> makeStairShape(j, voxelshape, voxelshape1, voxelshape2, voxelshape3, voxelshape4)).toArray((i) -> new VoxelShape[i]);
   }

   private static VoxelShape makeStairShape(int i, VoxelShape voxelshape, VoxelShape voxelshape1, VoxelShape voxelshape2, VoxelShape voxelshape3, VoxelShape voxelshape4) {
      VoxelShape voxelshape5 = voxelshape;
      if ((i & 1) != 0) {
         voxelshape5 = Shapes.or(voxelshape, voxelshape1);
      }

      if ((i & 2) != 0) {
         voxelshape5 = Shapes.or(voxelshape5, voxelshape2);
      }

      if ((i & 4) != 0) {
         voxelshape5 = Shapes.or(voxelshape5, voxelshape3);
      }

      if ((i & 8) != 0) {
         voxelshape5 = Shapes.or(voxelshape5, voxelshape4);
      }

      return voxelshape5;
   }

   protected StairBlock(BlockState blockstate, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.base = blockstate.getBlock();
      this.baseState = blockstate;
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return (blockstate.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(blockstate)]];
   }

   private int getShapeIndex(BlockState blockstate) {
      return blockstate.getValue(SHAPE).ordinal() * 4 + blockstate.getValue(FACING).get2DDataValue();
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      this.base.animateTick(blockstate, level, blockpos, randomsource);
   }

   public void attack(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      this.baseState.attack(level, blockpos, player);
   }

   public void destroy(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      this.base.destroy(levelaccessor, blockpos, blockstate);
   }

   public float getExplosionResistance() {
      return this.base.getExplosionResistance();
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate.getBlock())) {
         level.neighborChanged(this.baseState, blockpos, Blocks.AIR, blockpos, false);
         this.base.onPlace(this.baseState, level, blockpos, blockstate1, false);
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         this.baseState.onRemove(level, blockpos, blockstate1, flag);
      }
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      this.base.stepOn(level, blockpos, blockstate, entity);
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return this.base.isRandomlyTicking(blockstate);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.base.randomTick(blockstate, serverlevel, blockpos, randomsource);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.base.tick(blockstate, serverlevel, blockpos, randomsource);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      return this.baseState.use(level, player, interactionhand, blockhitresult);
   }

   public void wasExploded(Level level, BlockPos blockpos, Explosion explosion) {
      this.base.wasExploded(level, blockpos, explosion);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Direction direction = blockplacecontext.getClickedFace();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockpos);
      BlockState blockstate = this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection()).setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || !(blockplacecontext.getClickLocation().y - (double)blockpos.getY() > 0.5D)) ? Half.BOTTOM : Half.TOP).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
      return blockstate.setValue(SHAPE, getStairsShape(blockstate, blockplacecontext.getLevel(), blockpos));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return direction.getAxis().isHorizontal() ? blockstate.setValue(SHAPE, getStairsShape(blockstate, levelaccessor, blockpos)) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   private static StairsShape getStairsShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      Direction direction = blockstate.getValue(FACING);
      BlockState blockstate1 = blockgetter.getBlockState(blockpos.relative(direction));
      if (isStairs(blockstate1) && blockstate.getValue(HALF) == blockstate1.getValue(HALF)) {
         Direction direction1 = blockstate1.getValue(FACING);
         if (direction1.getAxis() != blockstate.getValue(FACING).getAxis() && canTakeShape(blockstate, blockgetter, blockpos, direction1.getOpposite())) {
            if (direction1 == direction.getCounterClockWise()) {
               return StairsShape.OUTER_LEFT;
            }

            return StairsShape.OUTER_RIGHT;
         }
      }

      BlockState blockstate2 = blockgetter.getBlockState(blockpos.relative(direction.getOpposite()));
      if (isStairs(blockstate2) && blockstate.getValue(HALF) == blockstate2.getValue(HALF)) {
         Direction direction2 = blockstate2.getValue(FACING);
         if (direction2.getAxis() != blockstate.getValue(FACING).getAxis() && canTakeShape(blockstate, blockgetter, blockpos, direction2)) {
            if (direction2 == direction.getCounterClockWise()) {
               return StairsShape.INNER_LEFT;
            }

            return StairsShape.INNER_RIGHT;
         }
      }

      return StairsShape.STRAIGHT;
   }

   private static boolean canTakeShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      BlockState blockstate1 = blockgetter.getBlockState(blockpos.relative(direction));
      return !isStairs(blockstate1) || blockstate1.getValue(FACING) != blockstate.getValue(FACING) || blockstate1.getValue(HALF) != blockstate.getValue(HALF);
   }

   public static boolean isStairs(BlockState blockstate) {
      return blockstate.getBlock() instanceof StairBlock;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      Direction direction = blockstate.getValue(FACING);
      StairsShape stairsshape = blockstate.getValue(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            if (direction.getAxis() == Direction.Axis.Z) {
               switch (stairsshape) {
                  case INNER_LEFT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case INNER_RIGHT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case OUTER_LEFT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  default:
                     return blockstate.rotate(Rotation.CLOCKWISE_180);
               }
            }
            break;
         case FRONT_BACK:
            if (direction.getAxis() == Direction.Axis.X) {
               switch (stairsshape) {
                  case INNER_LEFT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case INNER_RIGHT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case OUTER_LEFT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  case STRAIGHT:
                     return blockstate.rotate(Rotation.CLOCKWISE_180);
               }
            }
      }

      return super.mirror(blockstate, mirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, HALF, SHAPE, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
