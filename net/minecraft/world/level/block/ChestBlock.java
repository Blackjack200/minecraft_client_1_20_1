package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final int EVENT_SET_OPEN_COUNT = 1;
   protected static final int AABB_OFFSET = 1;
   protected static final int AABB_HEIGHT = 14;
   protected static final VoxelShape NORTH_AABB = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape EAST_AABB = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>() {
      public Optional<Container> acceptDouble(ChestBlockEntity chestblockentity, ChestBlockEntity chestblockentity1) {
         return Optional.of(new CompoundContainer(chestblockentity, chestblockentity1));
      }

      public Optional<Container> acceptSingle(ChestBlockEntity chestblockentity) {
         return Optional.of(chestblockentity);
      }

      public Optional<Container> acceptNone() {
         return Optional.empty();
      }
   };
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>() {
      public Optional<MenuProvider> acceptDouble(final ChestBlockEntity chestblockentity, final ChestBlockEntity chestblockentity1) {
         final Container container = new CompoundContainer(chestblockentity, chestblockentity1);
         return Optional.of(new MenuProvider() {
            @Nullable
            public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
               if (chestblockentity.canOpen(player) && chestblockentity1.canOpen(player)) {
                  chestblockentity.unpackLootTable(inventory.player);
                  chestblockentity1.unpackLootTable(inventory.player);
                  return ChestMenu.sixRows(i, inventory, container);
               } else {
                  return null;
               }
            }

            public Component getDisplayName() {
               if (chestblockentity.hasCustomName()) {
                  return chestblockentity.getDisplayName();
               } else {
                  return (Component)(chestblockentity1.hasCustomName() ? chestblockentity1.getDisplayName() : Component.translatable("container.chestDouble"));
               }
            }
         });
      }

      public Optional<MenuProvider> acceptSingle(ChestBlockEntity chestblockentity) {
         return Optional.of(chestblockentity);
      }

      public Optional<MenuProvider> acceptNone() {
         return Optional.empty();
      }
   };

   protected ChestBlock(BlockBehaviour.Properties blockbehaviour_properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
      super(blockbehaviour_properties, supplier);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockstate) {
      ChestType chesttype = blockstate.getValue(TYPE);
      if (chesttype == ChestType.SINGLE) {
         return DoubleBlockCombiner.BlockType.SINGLE;
      } else {
         return chesttype == ChestType.RIGHT ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
      }
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      if (blockstate1.is(this) && direction.getAxis().isHorizontal()) {
         ChestType chesttype = blockstate1.getValue(TYPE);
         if (blockstate.getValue(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE && blockstate.getValue(FACING) == blockstate1.getValue(FACING) && getConnectedDirection(blockstate1) == direction.getOpposite()) {
            return blockstate.setValue(TYPE, chesttype.getOpposite());
         }
      } else if (getConnectedDirection(blockstate) == direction) {
         return blockstate.setValue(TYPE, ChestType.SINGLE);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(TYPE) == ChestType.SINGLE) {
         return AABB;
      } else {
         switch (getConnectedDirection(blockstate)) {
            case NORTH:
            default:
               return NORTH_AABB;
            case SOUTH:
               return SOUTH_AABB;
            case WEST:
               return WEST_AABB;
            case EAST:
               return EAST_AABB;
         }
      }
   }

   public static Direction getConnectedDirection(BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      return blockstate.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      ChestType chesttype = ChestType.SINGLE;
      Direction direction = blockplacecontext.getHorizontalDirection().getOpposite();
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      boolean flag = blockplacecontext.isSecondaryUseActive();
      Direction direction1 = blockplacecontext.getClickedFace();
      if (direction1.getAxis().isHorizontal() && flag) {
         Direction direction2 = this.candidatePartnerFacing(blockplacecontext, direction1.getOpposite());
         if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
            direction = direction2;
            chesttype = direction2.getCounterClockWise() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if (chesttype == ChestType.SINGLE && !flag) {
         if (direction == this.candidatePartnerFacing(blockplacecontext, direction.getClockWise())) {
            chesttype = ChestType.LEFT;
         } else if (direction == this.candidatePartnerFacing(blockplacecontext, direction.getCounterClockWise())) {
            chesttype = ChestType.RIGHT;
         }
      }

      return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   @Nullable
   private Direction candidatePartnerFacing(BlockPlaceContext blockplacecontext, Direction direction) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().relative(direction));
      return blockstate.is(this) && blockstate.getValue(TYPE) == ChestType.SINGLE ? blockstate.getValue(FACING) : null;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (itemstack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockentity).setCustomName(itemstack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof Container) {
            Containers.dropContents(level, blockpos, (Container)blockentity);
            level.updateNeighbourForOutputSignal(blockpos, this);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         MenuProvider menuprovider = this.getMenuProvider(blockstate, level, blockpos);
         if (menuprovider != null) {
            player.openMenu(menuprovider);
            player.awardStat(this.getOpenChestStat());
            PiglinAi.angerNearbyPiglins(player, true);
         }

         return InteractionResult.CONSUME;
      }
   }

   protected Stat<ResourceLocation> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.OPEN_CHEST);
   }

   public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
      return this.blockEntityType.get();
   }

   @Nullable
   public static Container getContainer(ChestBlock chestblock, BlockState blockstate, Level level, BlockPos blockpos, boolean flag) {
      return chestblock.combine(blockstate, level, blockpos, flag).<Optional<Container>>apply(CHEST_COMBINER).orElse((Container)null);
   }

   public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockstate, Level level, BlockPos blockpos, boolean flag) {
      BiPredicate<LevelAccessor, BlockPos> bipredicate;
      if (flag) {
         bipredicate = (levelaccessor, blockpos1) -> false;
      } else {
         bipredicate = ChestBlock::isChestBlockedAt;
      }

      return DoubleBlockCombiner.combineWithNeigbour(this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, blockstate, level, blockpos, bipredicate);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      return this.combine(blockstate, level, blockpos, false).<Optional<MenuProvider>>apply(MENU_PROVIDER_COMBINER).orElse((MenuProvider)null);
   }

   public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity lidblockentity) {
      return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
         public Float2FloatFunction acceptDouble(ChestBlockEntity chestblockentity, ChestBlockEntity chestblockentity1) {
            return (f) -> Math.max(chestblockentity.getOpenNess(f), chestblockentity1.getOpenNess(f));
         }

         public Float2FloatFunction acceptSingle(ChestBlockEntity chestblockentity) {
            return chestblockentity::getOpenNess;
         }

         public Float2FloatFunction acceptNone() {
            return lidblockentity::getOpenNess;
         }
      };
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new ChestBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return level.isClientSide ? createTickerHelper(blockentitytype, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) : null;
   }

   public static boolean isChestBlockedAt(LevelAccessor levelaccessor, BlockPos blockpos) {
      return isBlockedChestByBlock(levelaccessor, blockpos) || isCatSittingOnChest(levelaccessor, blockpos);
   }

   private static boolean isBlockedChestByBlock(BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      return blockgetter.getBlockState(blockpos1).isRedstoneConductor(blockgetter, blockpos1);
   }

   private static boolean isCatSittingOnChest(LevelAccessor levelaccessor, BlockPos blockpos) {
      List<Cat> list = levelaccessor.getEntitiesOfClass(Cat.class, new AABB((double)blockpos.getX(), (double)(blockpos.getY() + 1), (double)blockpos.getZ(), (double)(blockpos.getX() + 1), (double)(blockpos.getY() + 2), (double)(blockpos.getZ() + 1)));
      if (!list.isEmpty()) {
         for(Cat cat : list) {
            if (cat.isInSittingPose()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, blockstate, level, blockpos, false));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, TYPE, WATERLOGGED);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
      if (blockentity instanceof ChestBlockEntity) {
         ((ChestBlockEntity)blockentity).recheckOpen();
      }

   }
}
