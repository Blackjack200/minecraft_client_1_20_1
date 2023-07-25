package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallHangingSignBlock extends SignBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final VoxelShape PLANK_NORTHSOUTH = Block.box(0.0D, 14.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   public static final VoxelShape PLANK_EASTWEST = Block.box(6.0D, 14.0D, 0.0D, 10.0D, 16.0D, 16.0D);
   public static final VoxelShape SHAPE_NORTHSOUTH = Shapes.or(PLANK_NORTHSOUTH, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D));
   public static final VoxelShape SHAPE_EASTWEST = Shapes.or(PLANK_EASTWEST, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D));
   private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, SHAPE_NORTHSOUTH, Direction.SOUTH, SHAPE_NORTHSOUTH, Direction.EAST, SHAPE_EASTWEST, Direction.WEST, SHAPE_EASTWEST));

   public WallHangingSignBlock(BlockBehaviour.Properties blockbehaviour_properties, WoodType woodtype) {
      super(blockbehaviour_properties.sound(woodtype.hangingSignSoundType()), woodtype);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity itemstack = level.getBlockEntity(blockpos);
      if (itemstack instanceof SignBlockEntity signblockentity) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (this.shouldTryToChainAnotherHangingSign(blockstate, player, blockhitresult, signblockentity, itemstack)) {
            return InteractionResult.PASS;
         }
      }

      return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
   }

   private boolean shouldTryToChainAnotherHangingSign(BlockState blockstate, Player player, BlockHitResult blockhitresult, SignBlockEntity signblockentity, ItemStack itemstack) {
      return !signblockentity.canExecuteClickCommands(signblockentity.isFacingFrontText(player), player) && itemstack.getItem() instanceof HangingSignItem && !this.isHittingEditableSide(blockhitresult, blockstate);
   }

   private boolean isHittingEditableSide(BlockHitResult blockhitresult, BlockState blockstate) {
      return blockhitresult.getDirection().getAxis() == blockstate.getValue(FACING).getAxis();
   }

   public String getDescriptionId() {
      return this.asItem().getDescriptionId();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return AABBS.get(blockstate.getValue(FACING));
   }

   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.getShape(blockstate, blockgetter, blockpos, CollisionContext.empty());
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch ((Direction)blockstate.getValue(FACING)) {
         case EAST:
         case WEST:
            return PLANK_EASTWEST;
         default:
            return PLANK_NORTHSOUTH;
      }
   }

   public boolean canPlace(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      Direction direction = blockstate.getValue(FACING).getClockWise();
      Direction direction1 = blockstate.getValue(FACING).getCounterClockWise();
      return this.canAttachTo(levelreader, blockstate, blockpos.relative(direction), direction1) || this.canAttachTo(levelreader, blockstate, blockpos.relative(direction1), direction);
   }

   public boolean canAttachTo(LevelReader levelreader, BlockState blockstate, BlockPos blockpos, Direction direction) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos);
      return blockstate1.is(BlockTags.WALL_HANGING_SIGNS) ? blockstate1.getValue(FACING).getAxis().test(blockstate.getValue(FACING)) : blockstate1.isFaceSturdy(levelreader, blockpos, direction, SupportType.FULL);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = this.defaultBlockState();
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      LevelReader levelreader = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();

      for(Direction direction : blockplacecontext.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal() && !direction.getAxis().test(blockplacecontext.getClickedFace())) {
            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(levelreader, blockpos) && this.canPlace(blockstate, levelreader, blockpos)) {
               return blockstate.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
            }
         }
      }

      return null;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction.getAxis() == blockstate.getValue(FACING).getClockWise().getAxis() && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public float getYRotationDegrees(BlockState blockstate) {
      return blockstate.getValue(FACING).toYRot();
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, WATERLOGGED);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new HangingSignBlockEntity(blockpos, blockstate);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }
}
