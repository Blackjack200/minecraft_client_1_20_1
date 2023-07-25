package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoorBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
   protected static final float AABB_DOOR_THICKNESS = 3.0F;
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
   private final BlockSetType type;

   protected DoorBlock(BlockBehaviour.Properties blockbehaviour_properties, BlockSetType blocksettype) {
      super(blockbehaviour_properties.sound(blocksettype.soundType()));
      this.type = blocksettype;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, Boolean.valueOf(false)).setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, Boolean.valueOf(false)).setValue(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockSetType type() {
      return this.type;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      Direction direction = blockstate.getValue(FACING);
      boolean flag = !blockstate.getValue(OPEN);
      boolean flag1 = blockstate.getValue(HINGE) == DoorHingeSide.RIGHT;
      switch (direction) {
         case EAST:
         default:
            return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
         case SOUTH:
            return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
         case WEST:
            return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
         case NORTH:
            return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      DoubleBlockHalf doubleblockhalf = blockstate.getValue(HALF);
      if (direction.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
         return blockstate1.is(this) && blockstate1.getValue(HALF) != doubleblockhalf ? blockstate.setValue(FACING, blockstate1.getValue(FACING)).setValue(OPEN, blockstate1.getValue(OPEN)).setValue(HINGE, blockstate1.getValue(HINGE)).setValue(POWERED, blockstate1.getValue(POWERED)) : Blocks.AIR.defaultBlockState();
      } else {
         return doubleblockhalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide && player.isCreative()) {
         DoublePlantBlock.preventCreativeDropFromBottomPart(level, blockpos, blockstate, player);
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      switch (pathcomputationtype) {
         case LAND:
            return blockstate.getValue(OPEN);
         case WATER:
            return false;
         case AIR:
            return blockstate.getValue(OPEN);
         default:
            return false;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Level level = blockplacecontext.getLevel();
      if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(blockplacecontext)) {
         boolean flag = level.hasNeighborSignal(blockpos) || level.hasNeighborSignal(blockpos.above());
         return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection()).setValue(HINGE, this.getHinge(blockplacecontext)).setValue(POWERED, Boolean.valueOf(flag)).setValue(OPEN, Boolean.valueOf(flag)).setValue(HALF, DoubleBlockHalf.LOWER);
      } else {
         return null;
      }
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      level.setBlock(blockpos.above(), blockstate.setValue(HALF, DoubleBlockHalf.UPPER), 3);
   }

   private DoorHingeSide getHinge(BlockPlaceContext blockplacecontext) {
      BlockGetter blockgetter = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Direction direction = blockplacecontext.getHorizontalDirection();
      BlockPos blockpos1 = blockpos.above();
      Direction direction1 = direction.getCounterClockWise();
      BlockPos blockpos2 = blockpos.relative(direction1);
      BlockState blockstate = blockgetter.getBlockState(blockpos2);
      BlockPos blockpos3 = blockpos1.relative(direction1);
      BlockState blockstate1 = blockgetter.getBlockState(blockpos3);
      Direction direction2 = direction.getClockWise();
      BlockPos blockpos4 = blockpos.relative(direction2);
      BlockState blockstate2 = blockgetter.getBlockState(blockpos4);
      BlockPos blockpos5 = blockpos1.relative(direction2);
      BlockState blockstate3 = blockgetter.getBlockState(blockpos5);
      int i = (blockstate.isCollisionShapeFullBlock(blockgetter, blockpos2) ? -1 : 0) + (blockstate1.isCollisionShapeFullBlock(blockgetter, blockpos3) ? -1 : 0) + (blockstate2.isCollisionShapeFullBlock(blockgetter, blockpos4) ? 1 : 0) + (blockstate3.isCollisionShapeFullBlock(blockgetter, blockpos5) ? 1 : 0);
      boolean flag = blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
      boolean flag1 = blockstate2.is(this) && blockstate2.getValue(HALF) == DoubleBlockHalf.LOWER;
      if ((!flag || flag1) && i <= 0) {
         if ((!flag1 || flag) && i >= 0) {
            int j = direction.getStepX();
            int k = direction.getStepZ();
            Vec3 vec3 = blockplacecontext.getClickLocation();
            double d0 = vec3.x - (double)blockpos.getX();
            double d1 = vec3.z - (double)blockpos.getZ();
            return (j >= 0 || !(d1 < 0.5D)) && (j <= 0 || !(d1 > 0.5D)) && (k >= 0 || !(d0 > 0.5D)) && (k <= 0 || !(d0 < 0.5D)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
         } else {
            return DoorHingeSide.LEFT;
         }
      } else {
         return DoorHingeSide.RIGHT;
      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (!this.type.canOpenByHand()) {
         return InteractionResult.PASS;
      } else {
         blockstate = blockstate.cycle(OPEN);
         level.setBlock(blockpos, blockstate, 10);
         this.playSound(player, level, blockpos, blockstate.getValue(OPEN));
         level.gameEvent(player, this.isOpen(blockstate) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockpos);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public boolean isOpen(BlockState blockstate) {
      return blockstate.getValue(OPEN);
   }

   public void setOpen(@Nullable Entity entity, Level level, BlockState blockstate, BlockPos blockpos, boolean flag) {
      if (blockstate.is(this) && blockstate.getValue(OPEN) != flag) {
         level.setBlock(blockpos, blockstate.setValue(OPEN, Boolean.valueOf(flag)), 10);
         this.playSound(entity, level, blockpos, flag);
         level.gameEvent(entity, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockpos);
      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      boolean flag1 = level.hasNeighborSignal(blockpos) || level.hasNeighborSignal(blockpos.relative(blockstate.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
      if (!this.defaultBlockState().is(block) && flag1 != blockstate.getValue(POWERED)) {
         if (flag1 != blockstate.getValue(OPEN)) {
            this.playSound((Entity)null, level, blockpos, flag1);
            level.gameEvent((Entity)null, flag1 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockpos);
         }

         level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag1)).setValue(OPEN, Boolean.valueOf(flag1)), 2);
      }

   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      return blockstate.getValue(HALF) == DoubleBlockHalf.LOWER ? blockstate1.isFaceSturdy(levelreader, blockpos1, Direction.UP) : blockstate1.is(this);
   }

   private void playSound(@Nullable Entity entity, Level level, BlockPos blockpos, boolean flag) {
      level.playSound(entity, blockpos, flag ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return mirror == Mirror.NONE ? blockstate : blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING))).cycle(HINGE);
   }

   public long getSeed(BlockState blockstate, BlockPos blockpos) {
      return Mth.getSeed(blockpos.getX(), blockpos.below(blockstate.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), blockpos.getZ());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HALF, FACING, OPEN, HINGE, POWERED);
   }

   public static boolean isWoodenDoor(Level level, BlockPos blockpos) {
      return isWoodenDoor(level.getBlockState(blockpos));
   }

   public static boolean isWoodenDoor(BlockState blockstate) {
      Block var2 = blockstate.getBlock();
      if (var2 instanceof DoorBlock doorblock) {
         if (doorblock.type().canOpenByHand()) {
            return true;
         }
      }

      return false;
   }
}
