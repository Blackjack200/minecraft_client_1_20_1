package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class DispenserBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = DirectionalBlock.FACING;
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(new Object2ObjectOpenHashMap<>(), (object2objectopenhashmap) -> object2objectopenhashmap.defaultReturnValue(new DefaultDispenseItemBehavior()));
   private static final int TRIGGER_DURATION = 4;

   public static void registerBehavior(ItemLike itemlike, DispenseItemBehavior dispenseitembehavior) {
      DISPENSER_REGISTRY.put(itemlike.asItem(), dispenseitembehavior);
   }

   protected DispenserBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof DispenserBlockEntity) {
            player.openMenu((DispenserBlockEntity)blockentity);
            if (blockentity instanceof DropperBlockEntity) {
               player.awardStat(Stats.INSPECT_DROPPER);
            } else {
               player.awardStat(Stats.INSPECT_DISPENSER);
            }
         }

         return InteractionResult.CONSUME;
      }
   }

   protected void dispenseFrom(ServerLevel serverlevel, BlockPos blockpos) {
      BlockSourceImpl blocksourceimpl = new BlockSourceImpl(serverlevel, blockpos);
      DispenserBlockEntity dispenserblockentity = blocksourceimpl.getEntity();
      int i = dispenserblockentity.getRandomSlot(serverlevel.random);
      if (i < 0) {
         serverlevel.levelEvent(1001, blockpos, 0);
         serverlevel.gameEvent(GameEvent.BLOCK_ACTIVATE, blockpos, GameEvent.Context.of(dispenserblockentity.getBlockState()));
      } else {
         ItemStack itemstack = dispenserblockentity.getItem(i);
         DispenseItemBehavior dispenseitembehavior = this.getDispenseMethod(itemstack);
         if (dispenseitembehavior != DispenseItemBehavior.NOOP) {
            dispenserblockentity.setItem(i, dispenseitembehavior.dispense(blocksourceimpl, itemstack));
         }

      }
   }

   protected DispenseItemBehavior getDispenseMethod(ItemStack itemstack) {
      return DISPENSER_REGISTRY.get(itemstack.getItem());
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      boolean flag1 = level.hasNeighborSignal(blockpos) || level.hasNeighborSignal(blockpos.above());
      boolean flag2 = blockstate.getValue(TRIGGERED);
      if (flag1 && !flag2) {
         level.scheduleTick(blockpos, this, 4);
         level.setBlock(blockpos, blockstate.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
      } else if (!flag1 && flag2) {
         level.setBlock(blockpos, blockstate.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
      }

   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      this.dispenseFrom(serverlevel, blockpos);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new DispenserBlockEntity(blockpos, blockstate);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getNearestLookingDirection().getOpposite());
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (itemstack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)blockentity).setCustomName(itemstack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof DispenserBlockEntity) {
            Containers.dropContents(level, blockpos, (DispenserBlockEntity)blockentity);
            level.updateNeighbourForOutputSignal(blockpos, this);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public static Position getDispensePosition(BlockSource blocksource) {
      Direction direction = blocksource.getBlockState().getValue(FACING);
      double d0 = blocksource.x() + 0.7D * (double)direction.getStepX();
      double d1 = blocksource.y() + 0.7D * (double)direction.getStepY();
      double d2 = blocksource.z() + 0.7D * (double)direction.getStepZ();
      return new PositionImpl(d0, d1, d2);
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockpos));
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, TRIGGERED);
   }
}
