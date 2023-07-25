package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
   public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final VoxelShape SHAPE_POST = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
   public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
   public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
   public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
   public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D), Block.box(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D), Block.box(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D), Block.box(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D), Block.box(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(10.666667D, 10.0D, 0.0D, 15.0D, 14.0D, 16.0D), Block.box(6.333333D, 12.0D, 0.0D, 10.666667D, 16.0D, 16.0D), Block.box(2.0D, 14.0D, 0.0D, 6.333333D, 18.0D, 16.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(0.0D, 10.0D, 10.666667D, 16.0D, 14.0D, 15.0D), Block.box(0.0D, 12.0D, 6.333333D, 16.0D, 16.0D, 10.666667D), Block.box(0.0D, 14.0D, 2.0D, 16.0D, 18.0D, 6.333333D), SHAPE_COMMON);
   private static final int PAGE_CHANGE_IMPULSE_TICKS = 2;

   protected LecternBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(false)));
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public VoxelShape getOcclusionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return SHAPE_COMMON;
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Level level = blockplacecontext.getLevel();
      ItemStack itemstack = blockplacecontext.getItemInHand();
      Player player = blockplacecontext.getPlayer();
      boolean flag = false;
      if (!level.isClientSide && player != null && player.canUseGameMasterBlocks()) {
         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
         if (compoundtag != null && compoundtag.contains("Book")) {
            flag = true;
         }
      }

      return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite()).setValue(HAS_BOOK, Boolean.valueOf(flag));
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE_COLLISION;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch ((Direction)blockstate.getValue(FACING)) {
         case NORTH:
            return SHAPE_NORTH;
         case SOUTH:
            return SHAPE_SOUTH;
         case EAST:
            return SHAPE_EAST;
         case WEST:
            return SHAPE_WEST;
         default:
            return SHAPE_COMMON;
      }
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, POWERED, HAS_BOOK);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new LecternBlockEntity(blockpos, blockstate);
   }

   public static boolean tryPlaceBook(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate, ItemStack itemstack) {
      if (!blockstate.getValue(HAS_BOOK)) {
         if (!level.isClientSide) {
            placeBook(entity, level, blockpos, blockstate, itemstack);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void placeBook(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate, ItemStack itemstack) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof LecternBlockEntity lecternblockentity) {
         lecternblockentity.setBook(itemstack.split(1));
         resetBookState(entity, level, blockpos, blockstate, true);
         level.playSound((Player)null, blockpos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

   }

   public static void resetBookState(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
      BlockState blockstate1 = blockstate.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(flag));
      level.setBlock(blockpos, blockstate1, 3);
      level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
      updateBelow(level, blockpos, blockstate);
   }

   public static void signalPageChange(Level level, BlockPos blockpos, BlockState blockstate) {
      changePowered(level, blockpos, blockstate, true);
      level.scheduleTick(blockpos, blockstate.getBlock(), 2);
      level.levelEvent(1043, blockpos, 0);
   }

   private static void changePowered(Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
      level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag)), 3);
      updateBelow(level, blockpos, blockstate);
   }

   private static void updateBelow(Level level, BlockPos blockpos, BlockState blockstate) {
      level.updateNeighborsAt(blockpos.below(), blockstate.getBlock());
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      changePowered(serverlevel, blockpos, blockstate, false);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(HAS_BOOK)) {
            this.popBook(blockstate, level, blockpos);
         }

         if (blockstate.getValue(POWERED)) {
            level.updateNeighborsAt(blockpos.below(), this);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   private void popBook(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof LecternBlockEntity lecternblockentity) {
         Direction direction = blockstate.getValue(FACING);
         ItemStack itemstack = lecternblockentity.getBook().copy();
         float f = 0.25F * (float)direction.getStepX();
         float f1 = 0.25F * (float)direction.getStepZ();
         ItemEntity itementity = new ItemEntity(level, (double)blockpos.getX() + 0.5D + (double)f, (double)(blockpos.getY() + 1), (double)blockpos.getZ() + 0.5D + (double)f1, itemstack);
         itementity.setDefaultPickUpDelay();
         level.addFreshEntity(itementity);
         lecternblockentity.clearContent();
      }

   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return direction == Direction.UP && blockstate.getValue(POWERED) ? 15 : 0;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      if (blockstate.getValue(HAS_BOOK)) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof LecternBlockEntity) {
            return ((LecternBlockEntity)blockentity).getRedstoneSignal();
         }
      }

      return 0;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (blockstate.getValue(HAS_BOOK)) {
         if (!level.isClientSide) {
            this.openScreen(level, blockpos, player);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         return !itemstack.isEmpty() && !itemstack.is(ItemTags.LECTERN_BOOKS) ? InteractionResult.CONSUME : InteractionResult.PASS;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      return !blockstate.getValue(HAS_BOOK) ? null : super.getMenuProvider(blockstate, level, blockpos);
   }

   private void openScreen(Level level, BlockPos blockpos, Player player) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof LecternBlockEntity) {
         player.openMenu((LecternBlockEntity)blockentity);
         player.awardStat(Stats.INTERACT_WITH_LECTERN);
      }

   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
