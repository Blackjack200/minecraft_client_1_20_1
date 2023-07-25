package net.minecraft.world.level.block.piston;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonHeadBlock extends DirectionalBlock {
   public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
   public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
   public static final float PLATFORM = 4.0F;
   protected static final VoxelShape EAST_AABB = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
   protected static final VoxelShape UP_AABB = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
   protected static final float AABB_OFFSET = 2.0F;
   protected static final float EDGE_MIN = 6.0F;
   protected static final float EDGE_MAX = 10.0F;
   protected static final VoxelShape UP_ARM_AABB = Block.box(6.0D, -4.0D, 6.0D, 10.0D, 12.0D, 10.0D);
   protected static final VoxelShape DOWN_ARM_AABB = Block.box(6.0D, 4.0D, 6.0D, 10.0D, 20.0D, 10.0D);
   protected static final VoxelShape SOUTH_ARM_AABB = Block.box(6.0D, 6.0D, -4.0D, 10.0D, 10.0D, 12.0D);
   protected static final VoxelShape NORTH_ARM_AABB = Block.box(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 20.0D);
   protected static final VoxelShape EAST_ARM_AABB = Block.box(-4.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   protected static final VoxelShape WEST_ARM_AABB = Block.box(4.0D, 6.0D, 6.0D, 20.0D, 10.0D, 10.0D);
   protected static final VoxelShape SHORT_UP_ARM_AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
   protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.box(6.0D, 4.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 12.0D);
   protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.box(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 16.0D);
   protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.box(0.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.box(4.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
   private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
   private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

   private static VoxelShape[] makeShapes(boolean flag) {
      return Arrays.stream(Direction.values()).map((direction) -> calculateShape(direction, flag)).toArray((i) -> new VoxelShape[i]);
   }

   private static VoxelShape calculateShape(Direction direction, boolean flag) {
      switch (direction) {
         case DOWN:
         default:
            return Shapes.or(DOWN_AABB, flag ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
         case UP:
            return Shapes.or(UP_AABB, flag ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
         case NORTH:
            return Shapes.or(NORTH_AABB, flag ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
         case SOUTH:
            return Shapes.or(SOUTH_AABB, flag ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
         case WEST:
            return Shapes.or(WEST_AABB, flag ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
         case EAST:
            return Shapes.or(EAST_AABB, flag ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
      }
   }

   public PistonHeadBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT).setValue(SHORT, Boolean.valueOf(false)));
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return (blockstate.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[blockstate.getValue(FACING).ordinal()];
   }

   private boolean isFittingBase(BlockState blockstate, BlockState blockstate1) {
      Block block = blockstate.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
      return blockstate1.is(block) && blockstate1.getValue(PistonBaseBlock.EXTENDED) && blockstate1.getValue(FACING) == blockstate.getValue(FACING);
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide && player.getAbilities().instabuild) {
         BlockPos blockpos1 = blockpos.relative(blockstate.getValue(FACING).getOpposite());
         if (this.isFittingBase(blockstate, level.getBlockState(blockpos1))) {
            level.destroyBlock(blockpos1, false);
         }
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
         BlockPos blockpos1 = blockpos.relative(blockstate.getValue(FACING).getOpposite());
         if (this.isFittingBase(blockstate, level.getBlockState(blockpos1))) {
            level.destroyBlock(blockpos1, true);
         }

      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction.getOpposite() == blockstate.getValue(FACING) && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.relative(blockstate.getValue(FACING).getOpposite()));
      return this.isFittingBase(blockstate, blockstate1) || blockstate1.is(Blocks.MOVING_PISTON) && blockstate1.getValue(FACING) == blockstate.getValue(FACING);
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (blockstate.canSurvive(level, blockpos)) {
         level.neighborChanged(blockpos.relative(blockstate.getValue(FACING).getOpposite()), block, blockpos1);
      }

   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(blockstate.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, TYPE, SHORT);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
