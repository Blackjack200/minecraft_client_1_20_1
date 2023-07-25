package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BellBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
   private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
   private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0D, 6.0D, 5.0D, 11.0D, 13.0D, 11.0D);
   private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D);
   private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
   private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 16.0D));
   private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 13.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 13.0D));
   private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 3.0D, 9.0D, 15.0D, 16.0D));
   private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D));
   public static final int EVENT_BELL_RING = 1;

   public BellBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, Boolean.valueOf(false)));
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      boolean flag1 = level.hasNeighborSignal(blockpos);
      if (flag1 != blockstate.getValue(POWERED)) {
         if (flag1) {
            this.attemptToRing(level, blockpos, (Direction)null);
         }

         level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag1)), 3);
      }

   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      Entity entity = projectile.getOwner();
      Player player = entity instanceof Player ? (Player)entity : null;
      this.onHit(level, blockstate, blockhitresult, player, true);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      return this.onHit(level, blockstate, blockhitresult, player, true) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
   }

   public boolean onHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, @Nullable Player player, boolean flag) {
      Direction direction = blockhitresult.getDirection();
      BlockPos blockpos = blockhitresult.getBlockPos();
      boolean flag1 = !flag || this.isProperHit(blockstate, direction, blockhitresult.getLocation().y - (double)blockpos.getY());
      if (flag1) {
         boolean flag2 = this.attemptToRing(player, level, blockpos, direction);
         if (flag2 && player != null) {
            player.awardStat(Stats.BELL_RING);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isProperHit(BlockState blockstate, Direction direction, double d0) {
      if (direction.getAxis() != Direction.Axis.Y && !(d0 > (double)0.8124F)) {
         Direction direction1 = blockstate.getValue(FACING);
         BellAttachType bellattachtype = blockstate.getValue(ATTACHMENT);
         switch (bellattachtype) {
            case FLOOR:
               return direction1.getAxis() == direction.getAxis();
            case SINGLE_WALL:
            case DOUBLE_WALL:
               return direction1.getAxis() != direction.getAxis();
            case CEILING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean attemptToRing(Level level, BlockPos blockpos, @Nullable Direction direction) {
      return this.attemptToRing((Entity)null, level, blockpos, direction);
   }

   public boolean attemptToRing(@Nullable Entity entity, Level level, BlockPos blockpos, @Nullable Direction direction) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (!level.isClientSide && blockentity instanceof BellBlockEntity) {
         if (direction == null) {
            direction = level.getBlockState(blockpos).getValue(FACING);
         }

         ((BellBlockEntity)blockentity).onHit(direction);
         level.playSound((Player)null, blockpos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
         level.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockpos);
         return true;
      } else {
         return false;
      }
   }

   private VoxelShape getVoxelShape(BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      BellAttachType bellattachtype = blockstate.getValue(ATTACHMENT);
      if (bellattachtype == BellAttachType.FLOOR) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
      } else if (bellattachtype == BellAttachType.CEILING) {
         return CEILING_SHAPE;
      } else if (bellattachtype == BellAttachType.DOUBLE_WALL) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
      } else if (direction == Direction.NORTH) {
         return TO_NORTH;
      } else if (direction == Direction.SOUTH) {
         return TO_SOUTH;
      } else {
         return direction == Direction.EAST ? TO_EAST : TO_WEST;
      }
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.getVoxelShape(blockstate);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.getVoxelShape(blockstate);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Direction direction = blockplacecontext.getClickedFace();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Level level = blockplacecontext.getLevel();
      Direction.Axis direction_axis = direction.getAxis();
      if (direction_axis == Direction.Axis.Y) {
         BlockState blockstate = this.defaultBlockState().setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR).setValue(FACING, blockplacecontext.getHorizontalDirection());
         if (blockstate.canSurvive(blockplacecontext.getLevel(), blockpos)) {
            return blockstate;
         }
      } else {
         boolean flag = direction_axis == Direction.Axis.X && level.getBlockState(blockpos.west()).isFaceSturdy(level, blockpos.west(), Direction.EAST) && level.getBlockState(blockpos.east()).isFaceSturdy(level, blockpos.east(), Direction.WEST) || direction_axis == Direction.Axis.Z && level.getBlockState(blockpos.north()).isFaceSturdy(level, blockpos.north(), Direction.SOUTH) && level.getBlockState(blockpos.south()).isFaceSturdy(level, blockpos.south(), Direction.NORTH);
         BlockState blockstate1 = this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(ATTACHMENT, flag ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
         if (blockstate1.canSurvive(blockplacecontext.getLevel(), blockplacecontext.getClickedPos())) {
            return blockstate1;
         }

         boolean flag1 = level.getBlockState(blockpos.below()).isFaceSturdy(level, blockpos.below(), Direction.UP);
         blockstate1 = blockstate1.setValue(ATTACHMENT, flag1 ? BellAttachType.FLOOR : BellAttachType.CEILING);
         if (blockstate1.canSurvive(blockplacecontext.getLevel(), blockplacecontext.getClickedPos())) {
            return blockstate1;
         }
      }

      return null;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      BellAttachType bellattachtype = blockstate.getValue(ATTACHMENT);
      Direction direction1 = getConnectedDirection(blockstate).getOpposite();
      if (direction1 == direction && !blockstate.canSurvive(levelaccessor, blockpos) && bellattachtype != BellAttachType.DOUBLE_WALL) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (direction.getAxis() == blockstate.getValue(FACING).getAxis()) {
            if (bellattachtype == BellAttachType.DOUBLE_WALL && !blockstate1.isFaceSturdy(levelaccessor, blockpos1, direction)) {
               return blockstate.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, direction.getOpposite());
            }

            if (bellattachtype == BellAttachType.SINGLE_WALL && direction1.getOpposite() == direction && blockstate1.isFaceSturdy(levelaccessor, blockpos1, blockstate.getValue(FACING))) {
               return blockstate.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
         }

         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      Direction direction = getConnectedDirection(blockstate).getOpposite();
      return direction == Direction.UP ? Block.canSupportCenter(levelreader, blockpos.above(), Direction.DOWN) : FaceAttachedHorizontalDirectionalBlock.canAttach(levelreader, blockpos, direction);
   }

   private static Direction getConnectedDirection(BlockState blockstate) {
      switch ((BellAttachType)blockstate.getValue(ATTACHMENT)) {
         case FLOOR:
            return Direction.UP;
         case CEILING:
            return Direction.DOWN;
         default:
            return blockstate.getValue(FACING).getOpposite();
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, ATTACHMENT, POWERED);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BellBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.BELL, level.isClientSide ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
